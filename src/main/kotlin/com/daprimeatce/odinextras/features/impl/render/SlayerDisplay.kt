package com.daprimeatce.odinextras.features.impl.render

import com.daprimeatce.odinextras.utils.ChatUtils
import com.odtheking.odin.events.MessageEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.KuudraUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.daprimeatce.odinextras.utils.RegexUtils
import com.odtheking.odin.events.EntityEvent
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.Entity
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.PlayerTeam
import java.util.concurrent.ConcurrentHashMap

object SlayerDisplay : Module(
    name = "Slayer Display",
    description = "Displays the info above a slayer boss on your screen, including hellion shield."
) {
    private val slayerNames = listOf(
        "Revenant Horror", "Atoned Horror",
        "Tarantula Broodfather", "Primordial Broodfather", "Conjoined Brood",
        "Sven Packmaster",
        "Voidgloom Seraph",
        "Inferno Demonlord", "ⓉⓎⓅⒽⓄⒺⓊⓈ", "ⓆⓊⒶⓏⒾⒾ",
        "Riftstalker Bloodfiend"
    )

    private var bossSpawned = false

    private var healthStand: Entity? = null
    private var timeStand: Entity? = null

    private val armorStands = ConcurrentHashMap<Int, ArmorStand>() // id, stand

    private val slayerPrefix = Regex(
        "^.*?]\\s*((?:§.)*)(?:[^\\s§]+\\s*)?((?:§.)*)\\s*(?=" +
                slayerNames.joinToString("|") { Regex.escape(it) } + ")"
    )


    @Suppress("unused")
    private val hud by HUD(name, "Displays slayer info in the HUD.") { example ->
        if (example) {
            textDim("§c03:00", 0, 0)
            val exampleWidth =
                textDim("§r§2Revenant Horror I §c0§r/§a500§r§c❤", 0, mc.font.lineHeight + 5, shadow = true).first
            return@HUD exampleWidth to 2 * mc.font.lineHeight + 5
        }

        var width = 0
        if (timeStand != null) {
            textDim(("§c" + ChatUtils.toColoredString(timeStand!!.displayName).takeLast(5)), 0, 0, shadow = true)

            if (listOf("ASHEN", "AURIC", "CRYSTAL", "SPIRIT").any { timeStand!!.displayName.string.contains(it) }) {
                textDim((ChatUtils.toColoredString(timeStand!!.displayName).dropLast(5)), 40, 0, shadow = true)
            }
        }
        if (healthStand != null) {
            val name = (ChatUtils.toColoredString(healthStand!!.displayName))
            val prefixlessName = name.replace(slayerPrefix) { m ->
                m.groupValues[2].ifEmpty { m.groupValues[1] }
            }

            width = textDim(prefixlessName, 0, mc.font.lineHeight + 5, shadow = true).first
        }
        width to 2 * mc.font.lineHeight + 5
    }


    init {
        on<EntityEvent.Add> {
            if (DungeonUtils.inDungeons || KuudraUtils.inKuudra) return@on

            val e = entity
            if (e is ArmorStand) {
                armorStands[e.id] = e
            }
        }

        on<EntityEvent.Remove> {
            armorStands.remove(entity.id)
        }

        on<LevelEvent.Load> {
            resetValues()
        }

        on<MessageEvent.Chat> {
            if (RegexUtils.slayerCompleteRegex.matches(message)) resetValues()
            if (RegexUtils.slayerFailRegex.matches(message)) resetValues()
            if (RegexUtils.slayerCancelRegex.matches(message)) resetValues()
        }

        var tickCount = 0
        on<TickEvent.Server> {
            if (DungeonUtils.inDungeons || KuudraUtils.inKuudra) return@on

            if (tickCount++ % 20 == 0) {
                bossSpawned = getScoreboardLines().any { "Slay the boss" in it }
            }

            if (tickCount % 10 == 0) {
                if (bossSpawned) {
                    val player = mc.player!!
                    val nearby = armorStands.values
                        .filter { it.distanceTo(player) < 20 }

                    val healthStands = nearby
                        .filter { stand ->
                            val name = stand.displayName.string
                            slayerNames.any { it in name }
                        }
                        .sortedBy { it.distanceTo(player) }

                    val timeStands = nearby.filter { stand ->
                        val name = stand.customName?.string ?: return@filter false
                        RegexUtils.slayerTimeRegex.containsMatchIn(name) || "IMMUNE" in name
                    }

                    var foundHealth: ArmorStand? = null
                    var foundTime: ArmorStand? = null

                    for (health in healthStands) {
                        val time = timeStands
                            .filter { it.distanceTo(health) < 1 }
                            .minByOrNull { it.distanceTo(health) }
                        val timeName = time?.customName?.string.orEmpty()
                        if ("IMMUNE" in timeName) continue

                        foundHealth = health
                        foundTime = time
                        break
                    }

                    healthStand = foundHealth
                    timeStand = foundTime
                }
            }

            armorStands.keys.removeIf { mc.level?.getEntity(it) !is ArmorStand }
        }
    }

    fun resetValues() {
        armorStands.clear()
        healthStand = null
        timeStand = null
    }

    fun getScoreboardLines(): List<String> {
        val scoreboard = mc.level?.scoreboard ?: return emptyList()
        val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return emptyList()

        return scoreboard.listPlayerScores(objective)
            .filter { !it.isHidden }                    // skips "#"-prefixed hidden entries
            .sortedByDescending { it.value() }          // top of the sidebar first
            .map { entry ->
                val team = scoreboard.getPlayersTeam(entry.owner())
                PlayerTeam.formatNameForTeam(team, entry.ownerName()).string
            }
    }


}