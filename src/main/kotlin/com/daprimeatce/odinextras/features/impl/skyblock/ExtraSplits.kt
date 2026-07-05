package com.daprimeatce.odinextras.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.skyblock.Splits.showTickTime
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.formatTime
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.skyblock.SplitsManager.currentSplits
import com.odtheking.odin.utils.skyblock.SplitsManager.getAndUpdateSplitsTimes
import com.odtheking.odin.utils.toFixed

object ExtraSplits : Module(
	name = "Extra Splits",
	description = "Extra timers for Kuudra and Dungeons that base Odin doesn't have."
) {
	private val fixedWidth by BooleanSetting("Fixed Width", true, desc = "Always use a fixed HUD width, right-aligning the times.")
	private val totalTimeSplits by BooleanSetting("Total Run Time Splits", true, desc = "Shows a split timer for the full run time.")
	private val timeLostToLag by BooleanSetting("Time Lost To Lag", true, desc = "Shows a split timer for how much run time is lost to lag.").withDependency { showTickTime }
	private val sendTimeLost by SelectorSetting("Send Time Lost", "Local", listOf("None", "Local", "Party", "Both"), desc = "Sends to the chat the run time lost to lag.").withDependency { timeLostToLag && showTickTime }
//	private val endOfRunRegex = Regex("^\\s*☠ Defeated (.+) in 0?([\\dhms ]+?)\\s*(\\(NEW RECORD!\\))?$"), "§1Total")
	private val extraSplitsHud by HUD("Extra Splits HUD", "Shows the extra split timers.") { extraSplitsExample ->
		val totalWidth = getStringWidth("Split 0: 0h 00m 00s" + if (showTickTime) " (0h 00m 00s)" else "") + 2

		if (extraSplitsExample) {
			repeat(1) { i ->
				val exampleTime = "0h 00m 00.00s" + if (showTickTime) " §8(§70s§8)" else ""
				if (fixedWidth) {
					text("§a§lRun Time:", 0, i * 9, Colors.WHITE)
					text(exampleTime, totalWidth - getStringWidth("0h 00m 00.00s" + if (showTickTime) " (0s)" else ""), i * 9, Colors.WHITE)
				} else {
					text("§a§lRun Time: §r$exampleTime", 0, i * 9, Colors.WHITE)
				}
			}
            return@HUD totalWidth to 9 * 1
		}

		val (times, tickTimes, current) = getAndUpdateSplitsTimes(currentSplits)
		if (currentSplits.splits.isEmpty()) return@HUD 0 to 0
		
		val maxWidth = currentSplits.splits.dropLast(1).maxOf { getStringWidth(it.name) }
		
		if (totalTimeSplits && currentSplits.splits.size > 9) {
			text("§a§lRun Time", 0, (currentSplits.splits.size - 1) * 1, Colors.WHITE)

			val totalTime = formatTime(times.take(9).sum())
			val displayText = if (showTickTime) "$totalTime §8(§7${(tickTimes.take(9).sum() / 20f).toFixed()}§8)" else totalTime
			val timeX = if (fixedWidth) totalWidth - getStringWidth(displayText) else maxWidth + 4

			text(displayText, timeX, (currentSplits.splits.size - 1) * 1, Colors.WHITE)
		}

		if (timeLostToLag && totalTimeSplits && showTickTime) {
			text("§c§lTime Lost", 0, (currentSplits.splits.size - 1) * 2, Colors.WHITE)

			val totalTime = times.take(9).sum() / 1000f
			val totalTickTime = tickTimes.take(9).sum() / 20f
			val timeDifference = totalTime - totalTickTime
			val displayText = timeDifference.toFixed()
			val timeX = if (fixedWidth) totalWidth - getStringWidth(displayText) else maxWidth + 4

			text(displayText, timeX, (currentSplits.splits.size - 1) * 2, Colors.WHITE)

			if (sendTimeLost == 1 && timeLostToLag) {
				modMessage("$displayText lost to lag.")
			}
		}

		totalWidth to 9 * (currentSplits.splits.size + 0)
	}
}