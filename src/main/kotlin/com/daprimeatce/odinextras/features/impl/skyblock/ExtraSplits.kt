package com.daprimeatce.odinextras.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.formatTime
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.toFixed
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.features.impl.skyblock.Splits.showTickTime
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.skyblock.SplitsManager.currentSplits
import com.daprimeatce.odinextras.utils.*

object ExtraSplits : Module(
	name = "Extra Splits",
	description = "Extra timers for Kuudra and Dungeons that base Odin doesn't have."
) {
	private val totalTimeSplits by BooleanSetting("Total Run Time Splits", true, desc = "Shows a split timer for the full run time.")
	private val timeLostToLag by BooleanSetting("Time Lost To Lag", true, desc = "Shows a split timer for how much run time is lost to lag.").withDependency { showTickTime }
	private val sendTimeLost by SelectorSetting("Send Time Lost", "Local", listOf("None", "Local", "Party", "Both"), desc = "Sends to the chat the run time lost to lag. (only works in dungeons)").withDependency { timeLostToLag && showTickTime }
	private val stormDPS by BooleanSetting("Storm DPS Splits", true, desc = "Shows an extra split timer for Storm, ending after purple pillar DPS.")
	private val fixedWidth by BooleanSetting("Fixed Width", true, desc = "Always use a fixed HUD width, right-aligning the times.")

	var startTimeMs: Long = -1
	var endTimeMs: Long = -1
	var startTimeStormMs: Long = -1
	var endTimeStormMs: Long = -1
	var serverTicks = -1
	var serverTicksStorm = -1
	var timeLost = ""
	var startTicking = false
	var startTickingStorm = false

	private val extraSplitsHud by HUD("Extra Splits HUD", "Shows the extra split timers.") {

		val totalWidth = getStringWidth("Split 0: 0h 00m 00s" + if (showTickTime) " (0h 00m 00s)" else "") + 2
		val exampleTotalTime = "0h 00m 00.00s" + if (showTickTime) " §8(§70s§8)" else ""
		val exampleTimeLost = "00m 00.00s"
		var count = 0 // number of features toggled on to calculate y value of rendering on the hud

		if (it) {
			if (fixedWidth) {
				if (totalTimeSplits) {
					text("§a§lTotal", 0, 0)
					text(exampleTotalTime, totalWidth - getStringWidth("0h 00m 00.00s" + if (showTickTime) " (0s)" else ""), 0)
					count++
				}
				if (timeLostToLag) {
					text("§c§lLost", 0, count * 9)
					text(exampleTimeLost, totalWidth - getStringWidth("00m 00.00s"), count * 9)
					count++
				}
			} else {
				if (totalTimeSplits) {
					text("§a§lTotal §r$exampleTotalTime", 0, 0)
					count++
				}
				if (timeLostToLag) {
					text("§c§lLost §r$exampleTimeLost", 0, count * 9)
					count++
				}
			}
            return@HUD totalWidth to 9 * count
		}

		val maxWidth = currentSplits?.splits?.dropLast(1)?.maxOfOrNull { getStringWidth(it.name) } ?: 50

		if (startTimeMs.toInt() == -1) return@HUD 0 to 0

		if (totalTimeSplits) {
			val totalTime = formatTime((if (endTimeMs > 0) endTimeMs else System.currentTimeMillis()) - startTimeMs, 2)
			val displayText = if (showTickTime) "$totalTime §8(§7${(serverTicks / 20f).toFixed(2)}§8)" else totalTime
			text("§a§lTotal", 0, 0, Colors.WHITE)

			if (fixedWidth) text(displayText, totalWidth - getStringWidth(displayText), 0, Colors.WHITE)
			else text(displayText, maxWidth + 4, 0, Colors.WHITE)
			count++
		}

		if (timeLostToLag) {
			text("§c§lLost", 0, count * 9, Colors.WHITE)

			if (fixedWidth) text(timeLost, totalWidth - getStringWidth(timeLost), count * 9, Colors.WHITE)
			else text(timeLost, maxWidth + 4, count * 9, Colors.WHITE)
			count++
		}
		totalWidth to 9 * count
	}

	init { 
		on<ChatPacketEvent> {
			if (startOfDungeonRegex.matches(value) || startOfKuudraRegex.matches(value)) {
				startTimeMs = System.currentTimeMillis()
				startTicking = true
				return@on
			}

			if (endOfDungeonRegex.matches(value) || endOfKuudraRegex.matches(value)) {
				endTimeMs = System.currentTimeMillis()
				startTicking = false

				schedule(5, true) {
					if (sendTimeLost == 1) modMessage("$timeLost lost to lag.")
					else if (sendTimeLost == 2) sendCommand("pc $timeLost lost to lag")
					else if (sendTimeLost == 3) {
						modMessage("$timeLost lost to lag.")
						sendCommand("pc $timeLost lost to lag")
					}
				}
			}

			if (stormStartRegex.matches(value)) {
				startTimeStormMs = System.currentTimeMillis()
				startTickingStorm = true
				return@on
			}

			if (stormEnrageRegex.matches(value)) {
				endTimeStormMs = System.currentTimeMillis()
				startTickingStorm = false
			}
		}

		on<LevelEvent.Load> {
			startTimeMs = -1
			endTimeMs = -1
			startTimeStormMs = -1
			endTimeStormMs = -1
			serverTicks = -1
			serverTicksStorm = -1
			timeLost = ""
			startTicking = false
			startTickingStorm = false
		}

		on<TickEvent.Server> {
			if (startTicking) {
				serverTicks++

				timeLost = formatTime((System.currentTimeMillis() - startTimeMs) - (serverTicks * 50))
			}
			if (startTickingStorm) {
				serverTicksStorm++
			}
		}
	}
	private val stormDPSHud by HUD("Storm DPS Hud", "Show the split timer for Storm purple pillar DPS.") {

		val totalWidth = getStringWidth("Split 0: 0h 00m 00s" + if (showTickTime) " (0h 00m 00s)" else "") + 2
		val exampleTotalTime = "0h 00m 00.00s" + if (showTickTime) " §8(§70s§8)" else ""
		var count = 0 // number of features toggled on to calculate y value of rendering on the hud

		if (it) {
			if (fixedWidth) {
				if (stormDPS) {
					text("§3Storm DPS", 0, 0)
					text(exampleTotalTime, totalWidth - getStringWidth("0h 00m 00.00s" + if (showTickTime) " (0s)" else ""), 0)
					count++
				}
			} else {
				if (stormDPS) {
					text("§3Storm DPS §r$exampleTotalTime", 0, 0)
					count++
				}
			}
			return@HUD totalWidth to 9 * count
		}

		val maxWidth = currentSplits?.splits?.dropLast(1)?.maxOfOrNull { getStringWidth(it.name) } ?: 50

		if (startTimeStormMs.toInt() == -1) return@HUD 0 to 0

		if (stormDPS) {
			val totalTime = formatTime((if (endTimeStormMs > 0) endTimeStormMs else System.currentTimeMillis()) - startTimeStormMs, 2)
			val displayText = if (showTickTime) "$totalTime §8(§7${(serverTicksStorm / 20f).toFixed(2)}§8)" else totalTime
			text("§3Storm DPS", 0, 0, Colors.WHITE)

			if (fixedWidth) text(displayText, totalWidth - getStringWidth(displayText), 0, Colors.WHITE)
			else text(displayText, maxWidth + 4, 0, Colors.WHITE)
			count++
		}
		totalWidth to 9 * count
	}
}
