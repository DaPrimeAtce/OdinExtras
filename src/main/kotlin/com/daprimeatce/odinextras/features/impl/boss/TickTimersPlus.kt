package com.daprimeatce.odinextras.features.impl.boss

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.toFixed

object TickTimersPlus : Module(
	name = "Tick Timers+",
	description = "Extra tick timers that base Odin doesn't have that might be useful."
) {
	private val displayInTicks by BooleanSetting("Display in Ticks", false, desc = "Display the timers in ticks instead of seconds.")
	private val symbolDisplay by BooleanSetting("Display Symbol", true, desc = "Displays s or t after the timers.")
	private val showPrefix by BooleanSetting("Show Prefix", true, desc = "Shows the prefix of the timers.")
	
	private val stormEnrageRegex = Regex("^⚠ Storm is enraged! ⚠$")
	
	private var enrageTriggered = false
	private var enrageTickTime = -1
	
	private val enrageHud by HUD("Storm Enrage Hud", "Displays a timer for when to jump under the Yellow pillar during storm for mage.") {
		if (it)                   textDim(formatTimer(74, 74, "§bJump:"), 0, 0, Colors.MINECRAFT_DARK_RED)
		else if (enrageTickTime >= 0) textDim(formatTimer(enrageTickTime, 74, "§bJump:"), 0, 0, Colors.MINECRAFT_DARK_RED)
		else 0 to 0
	}
	
	init {
		on<ChatPacketEvent> {
			when {
				enrageHud.enabled && !enrageTriggered && value.matches(stormEnrageRegex) -> {
                    enrageTriggered = true
                    enrageTickTime = 60
                }
			}
		}
		
		on<TickEvent.Server> {
			if (!DungeonUtils.inBoss) return@on
			if (enrageTickTime >= 0 && enrageHud.enabled) enrageTickTime--
		}
		
		on<LevelEvent.Load> {
			enrageTickTime = -1
			enrageTriggered = false
		}
	}
	
	private fun formatTimer(time: Int, max: Int, prefix: String): String {
		val color = when {
			time.toFloat() >= max * 0.66 -> "§a"
			time.toFloat() >= max * 0.33 -> "§6"
			else -> "§c"
		}
		val timeDisplay = if (displayInTicks) "$time${if (symbolDisplay) "t" else ""}" else "${(time / 20f).toFixed()}${if (symbolDisplay) "s" else ""}"
		return "${if (showPrefix) "$prefix " else ""}$color$timeDisplay"
	}
}