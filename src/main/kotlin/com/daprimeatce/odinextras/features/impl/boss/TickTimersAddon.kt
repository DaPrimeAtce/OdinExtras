package com.daprimeatce.odinextras.features.impl.boss

import com.daprimeatce.odinextras.utils.RegexUtils
import com.odtheking.odin.clickgui.settings.Setting
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.MessageEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.impl.boss.TickTimers
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor

object TickTimersAddon {
    var stormMoveTriggered = false
    var stormEnrageTriggered = false

    var stormMoveTickTime = 138
    var stormEnrageTickTime = 66

    val stormMove = TickTimers.HUD("Storm Move Hud", "Displays a timer for when Storm will begin to move after lightning. (Recommended to be used only if your party is crushing purple pillar instead of green pillar.)") { example -> drawStormMove(example) }
    val stormEnrage = TickTimers.HUD("Storm Enrage Hud", "Displays a timer for when to jump under the Yellow pillar during storm for mage.") { example -> drawStormEnrage(example) }

    private val displayInTicks get() = TickTimers.settings["Display in Ticks"]?.value == true
    private val displaySymbol get() = TickTimers.settings["Display Symbol"]?.value == true
    private val showPrefix get() = TickTimers.settings["Show Prefix"]?.value == true

    fun register() {
        val settings = TickTimers.settings
        val reordered = LinkedHashMap<String, Setting<*>>()

        reordered["Storm Move Hud"] = stormMove
        reordered["Storm Enrage Hud"] = stormEnrage
        reordered.putAll(settings)

        settings.clear()
        settings.putAll(reordered)

        if (stormMove !in ModuleManager.hudSettingsCache) ModuleManager.hudSettingsCache.add(stormMove)
        if (stormEnrage !in ModuleManager.hudSettingsCache) ModuleManager.hudSettingsCache.add(stormEnrage)
    }


    private fun GuiGraphicsExtractor.drawStormMove(example: Boolean): Pair<Int, Int> {
        if (example) text(Minecraft.getInstance().font, formatTimer(138, 138, "§c§lStorm will move in: "), 0, 0, 0xFFFFFFFF.toInt())
        else if (stormMoveTickTime >= 0 && stormMoveTriggered) text(Minecraft.getInstance().font, formatTimer(stormMoveTickTime, 98, "§c§lStorm will move in: "), 0, 0, 0xFFFFFFFF.toInt())
        return 140 to 10
    }

    private fun GuiGraphicsExtractor.drawStormEnrage(example: Boolean): Pair<Int, Int> {
        if (example) text(Minecraft.getInstance().font, formatTimer(66, 66, "§bJump: "), 0, 0, 0xFFFFFFFF.toInt())
        else if (stormEnrageTickTime >= 0 && stormEnrageTriggered) text(Minecraft.getInstance().font, formatTimer(stormEnrageTickTime, 66, "§bJump: "), 0, 0, 0xFFFFFFFF.toInt())
        return 60 to 10
    }


    init {
        on<LevelEvent.Load> {
            stormMoveTickTime = 98
            stormMoveTriggered = false
            stormEnrageTickTime = 66
            stormEnrageTriggered = false
        }

        on<TickEvent.Server> {
            if (stormMoveTriggered && stormMove.isEnabled) stormMoveTickTime--
            if (stormEnrageTriggered && stormEnrage.isEnabled) stormEnrageTickTime--
        }

        on<MessageEvent.Chat> {
            if (stormMove.isEnabled && !stormMoveTriggered && RegexUtils.stormMoveRegex.matches(message)) stormMoveTriggered = true
            if (stormEnrage.isEnabled && !stormEnrageTriggered && RegexUtils.stormEnrageRegex.matches(message)) stormEnrageTriggered = true
        }

        EventBus.subscribe(this)
    }

    private fun formatTimer(time: Int, max: Int, prefix: String?): String {
        val color = if (time >= max * 0.66) "§a"
        else if (time >= max * 0.33) "§6"
        else "§c"

        var timeDisplay: String
        timeDisplay = if (displayInTicks) color + time
        else color + String.format("%.2f", time / 20f)

        if (displaySymbol) {
            timeDisplay += if (displayInTicks) "t"
            else "s"
        }

        if (showPrefix) {
            timeDisplay = prefix + timeDisplay
        }

        return timeDisplay
    }
}