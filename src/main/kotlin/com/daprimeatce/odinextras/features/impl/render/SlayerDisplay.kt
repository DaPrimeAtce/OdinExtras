package com.daprimeatce.odinextras.features.impl.render

import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.handlers.TickTask
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.KuudraUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.util.StringDecomposer

object SlayerDisplay : Module(
    name = "Slayer Display",
    description = "Displays the info above a slayer boss on your screen."
) {
    private var armorStands = mutableListOf<ArmorStand>()
    private var nameStand: ArmorStand? = null
    private var healthStand: ArmorStand? = null
    private var timeStand: ArmorStand? = null

    private val nameRegex = Regex("^Spawned by: (.*)$")
    private val completeRegex = Regex("^(\\s*)SLAYER QUEST COMPLETE!$")
    private val failRegex = Regex("^(\\s*)SLAYER QUEST FAILED!$")
    private val cancelRegex = Regex("^Your Slayer Quest has been cancelled!$")

    private val slayerNames = listOf(
        "Revenant Horror", "Atoned Horror",
        "Tarantula Broodfather", "Primordial Broodfather", "Conjoined Brood",
        "Sven Packmaster",
        "Voidgloom Seraph",
        "Inferno Demonlord",
        "Riftstalker Bloodfiend"
    )
    private var cooldownTicks = 0

    private val hud by HUD(name, "Displays slayer info in the HUD.") {
        if (it) {
            textDim("§c03:00", 0, 0)
            val exampleWidth = textDim("§2༕ §r§c☠ §r§bRevenant Horror I §r§e0§r§c❤", 0, mc.font.lineHeight + 5, shadow = true).first
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
            width = textDim(healthStand?.displayName?.coloredString() ?: "", 0, mc.font.lineHeight + 5, shadow = true).first
        }
        width to 2 * mc.font.lineHeight + 5
    }

    fun resetValues() {
        nameStand = null
        healthStand = null
        timeStand = null
        cooldownTicks = 50
    }

    init {
        on<LevelEvent.Load> {
            resetValues()
        }

        on<TickEvent.Server> {
            if (cooldownTicks >= 0) {
                cooldownTicks--
            }
        }

        on<ChatPacketEvent> {
            if (completeRegex.matches(value)) resetValues()
            if (failRegex.matches(value)) resetValues()
            if (cancelRegex.matches(value)) resetValues()
        }

        TickTask(10) {
            if (DungeonUtils.inDungeons || KuudraUtils.inKuudra || cooldownTicks >= 0) return@TickTask

            var foundStand = false
            val entities = mc.level?.entitiesForRendering() ?: return@TickTask

            armorStands.clear()
            armorStands = entities.filterIsInstanceTo(armorStands)

            armorStands.forEach { stand ->
                val displayName = stand.displayName?.string ?: return@forEach
                val result = nameRegex.find(displayName)
                if (result?.groups[1]?.value == mc.player?.name?.string) {
                    nameStand = stand
                    foundStand = true
                    return@forEach
                }
            }

            if (!foundStand) {
                nameStand = null
                healthStand = null
                timeStand = null
                return@TickTask
            }

            val currentNameStand = nameStand ?: return@TickTask

            armorStands.retainAll { it.distanceTo(currentNameStand) < 3 }

            armorStands.forEach { stand ->
                    if (slayerNames.any { stand.displayName?.string?.contains(it) ?: false }) {
                        healthStand = stand
                        return@forEach
                    }
            }

            timeStand = armorStands.find { it.displayName?.string?.contains(":") == true }
        }
    }

    private val colorToFormatting: Map<Int, ChatFormatting> =
        ChatFormatting.entries.filter { it.isColor }.associateBy { it.color ?: -1 }

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