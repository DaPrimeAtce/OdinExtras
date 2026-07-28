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
import com.daprimeatce.odinextras.utils.RegexUtils

object ExtraSplits : Module(
	name = "Extra Splits",
	description = "Extra timers for Kuudra and Dungeons that base Odin doesn't have."
) {
	private val fixedWidth by BooleanSetting("Fixed Width", true, desc = "Always use a fixed HUD width, right-aligning the times.")

	private var startTimeMs: Long = -1
	private var endTimeMs: Long = -1
	private var startTimeStormMs: Long = -1
	private var endTimeStormMs: Long = -1
	private var serverTicks = -1
	private var serverTicksStorm = -1
	private var timeLost = "" // updates text per tick instead of per frame so it doesn't jitter as much
	private var startTicking = false
	private var startTickingStorm = false
	private var sentTime = false

	private val timeLostToLag by HUD("Time Lost To Lag", "Shows a split timer for how much run time is lost to lag.") {
		val totalWidth = getStringWidth("Split 0: 0h 00m 00s" + if (showTickTime) " (0h 00m 00s)" else "") + 2
		val exampleTimeLost = "00m 00.00s"

		if (it) {
			if (fixedWidth) {
				text("§c§lLost", 0, 0)
				text(exampleTimeLost, totalWidth - getStringWidth("00m 00.00s"), 0)
			} else {
				text("§c§lLost §r$exampleTimeLost", 0, 0)
			}
			return@HUD totalWidth to mc.font.lineHeight
		}

		val maxWidth = currentSplits?.splits?.dropLast(1)?.maxOfOrNull { getStringWidth(it.name) } ?: 50

		if (startTimeMs.toInt() == -1) return@HUD 0 to 0

		text("§c§lLost", 0, 0, Colors.WHITE)

		if (fixedWidth) text(timeLost, totalWidth - getStringWidth(timeLost), 0, Colors.WHITE)
		else text(timeLost, maxWidth + 4, 0, Colors.WHITE)

		totalWidth to mc.font.lineHeight
	}
	private val sendTimeLost by SelectorSetting("Send Time Lost", "Local", listOf("None", "Local", "Party", "Both"), desc = "Sends to the chat the run time lost to lag.").withDependency { timeLostToLag.enabled }

	private val totalRunTime by HUD("Total Run Time", "Shows a split timer for the full run time.") {
		val totalWidth = getStringWidth("Split 0: 0h 00m 00s" + if (showTickTime) " (0h 00m 00s)" else "") + 2
		val exampleTime = "0h 00m 00.00s" + if (showTickTime) " §8(§70s§8)" else ""

		if (it) {
			if (fixedWidth) {
				text("§a§lTotal", 0, 0)
				text(exampleTime, totalWidth - getStringWidth("0h 00m 00.00s" + if (showTickTime) " (0s)" else ""), 0)
			} else {
				text("§a§lTotal §r$exampleTime", 0, 0)
			}
			return@HUD totalWidth to mc.font.lineHeight
		}

		val maxWidth = currentSplits?.splits?.dropLast(1)?.maxOfOrNull { getStringWidth(it.name) } ?: 50

		if (startTimeMs.toInt() == -1) return@HUD 0 to 0

		val totalTime = formatTime((if (endTimeMs > 0) endTimeMs else System.currentTimeMillis()) - startTimeMs, 2)
		val displayText = if (showTickTime) "$totalTime §8(§7${(serverTicks / 20f).toFixed(2)}§8)" else totalTime
		text("§a§lTotal", 0, 0, Colors.WHITE)

		if (fixedWidth) text(displayText, totalWidth - getStringWidth(displayText), 0, Colors.WHITE)
		else text(displayText, maxWidth + 4, 0, Colors.WHITE)

		totalWidth to mc.font.lineHeight
	}

	private val stormDps by HUD("Storm DPS", "Show the split timer for Storm purple pillar DPS.") {
		val totalWidth = getStringWidth("Split 0: 0h 00m 00s" + if (showTickTime) " (0h 00m 00s)" else "") + 2
		val exampleTime = "0h 00m 00.00s" + if (showTickTime) " §8(§70s§8)" else ""

		if (it) {
			if (fixedWidth) {
				text("§3Storm DPS", 0, 0)
				text(exampleTime, totalWidth - getStringWidth("0h 00m 00.00s" + if (showTickTime) " (0s)" else ""), 0)
			} else {
				text("§3Storm DPS §r$exampleTime", 0, 0)
			}

			return@HUD totalWidth to mc.font.lineHeight
		}

		val maxWidth = currentSplits?.splits?.dropLast(1)?.maxOfOrNull { getStringWidth(it.name) } ?: 50

		if (startTimeStormMs.toInt() == -1) return@HUD 0 to 0

		val totalTime = formatTime((if (endTimeStormMs > 0) endTimeStormMs else System.currentTimeMillis()) - startTimeStormMs, 2)
		val displayText = if (showTickTime) "$totalTime §8(§7${(serverTicksStorm / 20f).toFixed(2)}§8)" else totalTime
		text("§3Storm DPS", 0, 0, Colors.WHITE)

		if (fixedWidth) text(displayText, totalWidth - getStringWidth(displayText), 0, Colors.WHITE)
		else text(displayText, maxWidth + 4, 0, Colors.WHITE)

		totalWidth to mc.font.lineHeight
	}

	init { 
		on<ChatPacketEvent> {
			if (RegexUtils.startOfDungeonRegex.matches(value) || RegexUtils.startOfKuudraRegex.matches(value)) {
				startTimeMs = System.currentTimeMillis()
				startTicking = true
				return@on
			}

			if (!sentTime && (RegexUtils.endOfDungeonRegex.matches(value) || RegexUtils.endOfKuudraRegex.matches(value))) {
				endTimeMs = System.currentTimeMillis()
				startTicking = false
				sentTime = true

				schedule(5, true) {
					if (sendTimeLost == 1) modMessage("$timeLost lost to lag.")
					else if (sendTimeLost == 2) sendCommand("pc $timeLost lost to lag")
					else if (sendTimeLost == 3) {
						modMessage("$timeLost lost to lag.")
						sendCommand("pc $timeLost lost to lag")
					}
				}
			}

			if (RegexUtils.stormStartRegex.matches(value)) {
				startTimeStormMs = System.currentTimeMillis()
				startTickingStorm = true
				return@on
			}

			if (RegexUtils.stormEnrageRegex.matches(value)) {
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
			sentTime = false
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

}
