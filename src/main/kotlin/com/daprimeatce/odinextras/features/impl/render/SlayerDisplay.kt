package com.daprimeatce.odinextras.features.impl.render

import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.KuudraUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.daprimeatce.odinextras.utils.RegexUtils
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.util.StringDecomposer
import net.minecraft.world.entity.EntityTypes
import java.util.concurrent.ConcurrentHashMap

import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

object SlayerDisplay : Module(
    name = "Slayer Display",
    description = "Displays the info above a slayer boss on your screen, including hellion shield."
) {
    private var armorStands = mutableListOf<ArmorStand>()
    private var nameStand: ArmorStand? = null
    private var healthStand: ArmorStand? = null
    private var timeStand: ArmorStand? = null

    private var currentTick = 0
    private val pendingStands = ConcurrentHashMap<Int, Int>() // id, tick spawned

    private val slayerNames = listOf(
        "Revenant Horror", "Atoned Horror",
        "Tarantula Broodfather", "Primordial Broodfather", "Conjoined Brood",
        "Sven Packmaster",
        "Voidgloom Seraph",
        "Inferno Demonlord",
        "Riftstalker Bloodfiend"
    )

    private val colorCodeField = ChatFormatting::class.java.getDeclaredField("code").apply { isAccessible = true }


        private val hud by HUD(name, "Displays slayer info in the HUD.") {
        if (it) {
            textDim("§c03:00", 0, 0)
            val exampleWidth = textDim("§r§bRevenant Horror I §r§e100§r§c❤", 0, mc.font.lineHeight + 5, shadow = true).first
            return@HUD exampleWidth to 2 * mc.font.lineHeight + 5
        }

        var width = 0
        if (timeStand != null) {
            textDim(("§c" + timeStand?.displayName?.string?.takeLast(5)), 0, 0, shadow = true)

            if (listOf("ASHEN", "AURIC", "CRYSTAL", "SPIRIT").any { timeStand?.displayName?.string?.contains(it) == true }) {
                textDim((timeStand?.displayName?.coloredString()?.dropLast(5)) ?: "", 50, 0, shadow = true)
            }
        }
        if (healthStand != null) {
            width = textDim((healthStand?.displayName?.coloredString()?.substringAfter("☠")?.drop(1)) ?: "", 0, mc.font.lineHeight + 5, shadow = true).first
        }
        width to 2 * mc.font.lineHeight + 5
    }

    fun resetValues() {
        nameStand = null
        healthStand = null
        timeStand = null
    }

    init {
        on<LevelEvent.Load> {
            resetValues()
            currentTick = 0
            pendingStands.clear()
        }

        on<ChatPacketEvent> {
            if (RegexUtils.slayerCompleteRegex.matches(value)) resetValues()
            if (RegexUtils.slayerFailRegex.matches(value)) resetValues()
            if (RegexUtils.slayerCancelRegex.matches(value)) resetValues()
        }

        onReceive<ClientboundAddEntityPacket> {
            if (DungeonUtils.inDungeons || KuudraUtils.inKuudra) return@onReceive

            if (type == EntityTypes.ARMOR_STAND) {
                pendingStands[id] = currentTick
            }
        }

        on<TickEvent.Server> {
            if (DungeonUtils.inDungeons || KuudraUtils.inKuudra) return@on

            if (nameStand != null && nameStand!!.isCustomNameVisible) {
                healthStand = null
                timeStand = null
                handleBossSpawned()
            }

            currentTick++

            if (pendingStands.isEmpty()) return@on

            val toRemove = mutableListOf<Int>()
            pendingStands.forEach { (id, spawnTick) ->
                val entity = mc.level?.getEntity(id) as? ArmorStand ?: run {
                    toRemove.add(id)
                    return@forEach
                }
                val name = entity.customName?.string

                when {
                     name != null && RegexUtils.slayerSpawnerRegex.find(name)?.groups[1]?.value == mc.player?.name?.string -> {
                        toRemove.add(id)
                        nameStand = entity
                        schedule(2  , true) { handleBossSpawned() }
                    }
                    !name.isNullOrEmpty() -> toRemove.add(id)
                    currentTick - spawnTick > 40 -> toRemove.add(id)
                }
            }
            toRemove.forEach { pendingStands.remove(it) }
        }
    }

    private fun handleBossSpawned() {
        val entities = mc.level?.entitiesForRendering() ?: return

        armorStands.clear()
        armorStands = entities.filterIsInstanceTo(armorStands)

        val currentNameStand = nameStand ?: return
        armorStands.retainAll { it.distanceTo(currentNameStand) < 0.5 }

        armorStands.forEach { stand ->
            if (slayerNames.any { stand.displayName.string.contains(it) }) {
                healthStand = stand
                return@forEach
            }
        }

        timeStand = armorStands.filter { it.displayName.string.contains(":") && it != nameStand }.minByOrNull { it.distanceTo(currentNameStand) }
    }

    private val colors = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

    private val colorToFormatting: Map<Int, ChatFormatting> =
        ChatFormatting.entries.filter { colorCodeField.get(it) as Char in colors}.associateBy { TextColor.fromLegacyFormat(it)?.value ?: -1 }

    fun Component.coloredString(): String {
        val builder = StringBuilder()
        var lastStyle: Style? = null

        StringDecomposer.iterateFormatted(this, Style.EMPTY) { _, style, codepoint ->
            if (style != lastStyle) {
                builder.append(ChatFormatting.RESET)
                style.color?.value?.let { rgb -> colorToFormatting[rgb]?.let { builder.append(it) } }
                if (style.isBold) builder.append(ChatFormatting.BOLD)
                if (style.isItalic) builder.append(ChatFormatting.ITALIC)
                if (style.isUnderlined) builder.append(ChatFormatting.UNDERLINE)
                if (style.isStrikethrough) builder.append(ChatFormatting.STRIKETHROUGH)
                if (style.isObfuscated) builder.append(ChatFormatting.OBFUSCATED)
                lastStyle = style
            }
            builder.appendCodePoint(codepoint)
            true
        }

        return builder.toString()
    }
}