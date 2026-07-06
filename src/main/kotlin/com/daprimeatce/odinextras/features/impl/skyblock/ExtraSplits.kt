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

object ExtraSplits : Module(
	name = "Extra Splits",
	description = "Extra timers for Kuudra and Dungeons that base Odin doesn't have."
) {
	private val fixedWidth by BooleanSetting("Fixed Width", true, desc = "Always use a fixed HUD width, right-aligning the times.")
	private val totalTimeSplits by BooleanSetting("Total Run Time Splits", true, desc = "Shows a split timer for the full run time.")
	private val timeLostToLag by BooleanSetting("Time Lost To Lag", true, desc = "Shows a split timer for how much run time is lost to lag.").withDependency { showTickTime }
	private val sendTimeLost by SelectorSetting("Send Time Lost", "Local", listOf("None", "Local", "Party", "Both"), desc = "Sends to the chat the run time lost to lag. (only works in dungeons)").withDependency { timeLostToLag && showTickTime }

	private val startOfDungeonRegex = Regex("\\[NPC] Mort: Here, I found this map when I first entered the dungeon\\.|\\[NPC] Mort: Right-click the Orb for spells, and Left-click \\(or Drop\\) to use your Ultimate!")
	private val endOfDungeonRegex = Regex("^\\s*☠ Defeated (.+) in 0?([\\dhms ]+?)\\s*(\\(NEW RECORD!\\))?$")
	private val startOfKuudraRegex = Regex("^\\[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!$")
	private val endOfKuudraRegex = Regex("^\\[NPC] Elle: Good job everyone. A hard fought battle come to an end. Let's get out of here before we run into any more trouble!$")

	var startTimeMs: Long = -1
	var endTimeMs: Long = -1
	var serverTicks = -1
	var timeLost = ""
	var startTicking = false

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
		
		if (startTimeMs.toInt() == -1) return@HUD 0 to 0

		val maxWidth = currentSplits?.splits?.dropLast(1)?.maxOfOrNull { getStringWidth(it.name) } ?: 50

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
		}

		on<LevelEvent.Load> {
			startTimeMs = -1
			endTimeMs = -1
			serverTicks = -1
			timeLost = ""
			startTicking = false
		}

		on<TickEvent.Server> {
			if (startTicking) {
				serverTicks++

				timeLost = formatTime((System.currentTimeMillis() - startTimeMs) - (serverTicks * 50))
			}
		}
	}
}
