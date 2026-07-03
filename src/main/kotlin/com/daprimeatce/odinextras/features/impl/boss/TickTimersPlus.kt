package com.daprimeatce.odinextras.features.impl.boss

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.toFixed

object TickTimersPlus : Module(
	name = "Tick Timers+",
	description = "Extra tick timers that base Odin doesn't have that might be useful."
) {
	private val timerType by SelectorSetting("Timer Type", "Seconds", listOf("Seconds", "Milliseconds", "Ticks"), desc = "Type of timer.")
	private val symbolDisplay by BooleanSetting("Display Symbol", true, desc = "Displays s, ms, or t after the timers.")
	private val showPrefix by BooleanSetting("Show Prefix", true, desc = "Shows the prefix of the timers.")
	
	private val stormEnrageRegex = Regex("^⚠ Storm is enraged! ⚠$")
	private val witherKingStartRegex = Regex("^\\[BOSS] Wither King: You\\.\\.\\. again\\?$")
	
	private var enrageTriggered = false
	private var enrageTickTime = 66 // Actual value
	
	private val enrageHud by HUD("Storm Enrage Hud", "Displays a timer for when to jump under the Yellow pillar during storm for mage.") {
		if (it)                   textDim(formatTimer(66, 66, "§bJump: "), 0, 0)
		else if (enrageTickTime >= 0 && enrageTriggered) textDim(formatTimer(enrageTickTime, 66, "§bJump: "), 0, 0)
		else 0 to 0
	}

	private var ragTriggered = false
	private var ragTickTime = 100 // Actual value

	private val ragHud by HUD("Dragon Rag Hud", "Displays a timer for when to rag for the first dragon.") {
		if (it)                   textDim(formatTimer(100, 100, "§bRag in: "), 0, 0)
		else if (ragTickTime >= 0 && ragTriggered) textDim(formatTimer(ragTickTime, 100, "§bRag in: "), 0, 0)
		else 0 to 0
	}
	
	init {
		on<ChatPacketEvent> {
			if (enrageHud.enabled && !enrageTriggered && value.matches(stormEnrageRegex)) {
				enrageTriggered = true
			}

			if (enrageHud.enabled && !enrageTriggered && value.matches(witherKingStartRegex)) {
				ragTriggered = true
			}
		}
		
		on<TickEvent.Server> {
			if (!DungeonUtils.inBoss) return@on
			if (enrageTriggered && enrageHud.enabled) enrageTickTime--
			if (ragTriggered && ragHud.enabled) ragTickTime--
		}

		// Reset values on world load, don't forget to change these with the initial values if needed
		on<LevelEvent.Load> {
			enrageTickTime = 66
			enrageTriggered = false
			ragTickTime = 100
			ragTriggered = false
		}
	}
	
	private fun formatTimer(time: Int, max: Int, prefix: String): String {
		val color = when {
			time.toFloat() >= max * 0.66 -> "§a"
			time.toFloat() >= max * 0.33 -> "§6"
			else -> "§c"
		}

		var timeDisplay = when (timerType) {
			0 -> color + (time / 20f).toFixed(2)
			1 -> color + (time * 50).toString()
			else -> color + time.toString()
		}

		if (symbolDisplay) timeDisplay += when (timerType) {
			0 -> "s"
			1 -> "ms"
			else -> "t"
		}

		if (showPrefix) {
			timeDisplay = prefix + timeDisplay
		}

		return timeDisplay
	}
}