package com.daprimeatce.odinextras.features.impl.render

import com.daprimeatce.odinextras.utils.ChatUtils
import com.daprimeatce.odinextras.utils.RegexUtils
import com.odtheking.odin.events.EntityEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.MessageEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.KuudraUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.world.entity.decoration.ArmorStand
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

    private val timeKeywords = listOf("ASHEN", "AURIC", "CRYSTAL", "SPIRIT")

    private var bossSpawned = false
    private var tickCount = 0
    private var cocoonTicks = 0

    private var healthStand: ArmorStand? = null
    private var timeStand: ArmorStand? = null

    private var healthText: String? = null
    private var timeText: String? = null
    private var timeExtra: String? = null

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

        val time = timeText
        if (time != null) {
            textDim("§c$time", 0, 0, shadow = true)
            timeExtra?.let { textDim(it, 40, 0, shadow = true) }
        }

        val health = healthText
        val width = if (health != null) textDim(health, 0, mc.font.lineHeight + 5, shadow = true).first else 0
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
            if (cocoonTicks <= 0 &&
                (RegexUtils.slayerCompleteRegex.matches(message)
                        || RegexUtils.slayerFailRegex.matches(message)
                        || RegexUtils.slayerCancelRegex.matches(message))
            ) resetValues()

            if (RegexUtils.slayerSpawnRegex.matches(message)) bossSpawned = true
            if (RegexUtils.slayerCocoonRegex.matches(message)) cocoonTicks = 160 // Lasts 6.4s according to SkyHanni, 8s should be safe
        }

        on<TickEvent.Server> {
            if (DungeonUtils.inDungeons || KuudraUtils.inKuudra) return@on
            if (cocoonTicks > 0) cocoonTicks--

            val periodic = tickCount++ % 10 == 0
            if (periodic) armorStands.keys.removeIf { mc.level?.getEntity(it) !is ArmorStand }

            if (bossSpawned) {
                val h = healthStand
                if (periodic || h == null || h.isRemoved || isDead(h)) findStands()
                refreshText()
            }
        }
    }


    private class BossGroup(val name: ArmorStand?, val timer: ArmorStand?)

    private fun nearest(from: ArmorStand, candidates: List<ArmorStand>, taken: Set<ArmorStand>): ArmorStand? {
        var best: ArmorStand? = null
        var bestDist = 1.0
        for (s in candidates) {
            if (s in taken) continue
            val d = from.distanceToSqr(s)
            if (d < bestDist) {
                bestDist = d
                best = s
            }
        }
        return best
    }

    private fun findStands() {
        val tags = ArrayList<ArmorStand>(2)
        val names = ArrayList<ArmorStand>(2)
        val timers = ArrayList<ArmorStand>(2)

        for (stand in armorStands.values) {
            val text = stand.customName?.string ?: continue
            if (RegexUtils.slayerNameRegex.matches(text)) tags += stand
            if (slayerNames.any { it in text }) names += stand
            if ("IMMUNE" in text || RegexUtils.slayerTimeRegex.containsMatchIn(text)) timers += stand
        }
        if (tags.isEmpty()) {
            healthStand = null
            timeStand = null
            return
        }

        val usedNames = HashSet<ArmorStand>()
        val usedTimers = HashSet<ArmorStand>()
        val groups = tags.map { tag ->
            val name = nearest(tag, names, usedNames)?.also { usedNames += it }
            val timer = nearest(tag, timers, usedTimers)?.also { usedTimers += it }
            BossGroup(name, timer)
        }

        val active = groups.firstOrNull { g ->
            g.name != null &&
                    !isDead(g.name) &&
                    "IMMUNE" !in g.name.customName?.string.orEmpty() &&
                    "IMMUNE" !in g.timer?.customName?.string.orEmpty()
        }

        healthStand = active?.name
        timeStand = active?.timer
    }

    private fun refreshText() {
        val h = healthStand
        val alive = h != null && !h.isRemoved && !isDead(h)

        healthText = if (alive) {
            ChatUtils.toColoredString(h.displayName).replace(slayerPrefix) { m ->
                m.groupValues[2].ifEmpty { m.groupValues[1] }
            }
        } else null

        val t = timeStand
        if (alive && t != null && !t.isRemoved) {
            val full = ChatUtils.toColoredString(t.displayName)
            timeText = full.takeLast(5)
            timeExtra = if (timeKeywords.any { it in t.displayName.string }) full.dropLast(5) else null
        } else {
            timeText = null
            timeExtra = null
        }
    }

    fun resetValues() {
        armorStands.clear()
        healthStand = null
        timeStand = null
        healthText = null
        timeText = null
        timeExtra = null
        bossSpawned = false
        tickCount = 0
    }

    private fun isDead(stand: ArmorStand?): Boolean {
        val text = stand?.customName?.string?: return false
        return " 0/" in ChatUtils.removeFormatting(text)
    }
}