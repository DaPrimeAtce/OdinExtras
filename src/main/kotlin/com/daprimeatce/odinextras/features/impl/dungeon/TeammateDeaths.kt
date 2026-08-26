package com.daprimeatce.odinextras.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.alert
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.core.on
import com.daprimeatce.odinextras.utils.RegexUtils
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils

object TeammateDeaths: Module(
    name = "Teammate Deaths",
    description = "Various features based on teammate deaths in a dungeon."
) {
    private val deathAlert by DropdownSetting("Alerts", false, desc = "Sends an alert when your team reaches a threshold of deaths in a run.")
    private val threshold by NumberSetting("Alert Threshold", 5f, 0.0..10.0, 1f, unit = " deaths", desc = "The number of deaths required for alerts.").withDependency { deathAlert }
    private val displayTitle by BooleanSetting("Title On Threshold", false, desc = "Displays a title when the threshold of deaths in a run has been reached.").withDependency { deathAlert }
    private val titleText by StringSetting("Title", "&c&lS+ Lost!", desc = "What the title should display.", placeholder = "&c&lS+ Lost!").withDependency { deathAlert && displayTitle }
    private val sendThresholdMessage by BooleanSetting("Message On Threshold", false, desc = "Sends a message in party chat when the threshold of deaths has been reached.").withDependency { deathAlert }
    private val onlyInClear by BooleanSetting("Only In Clear", false, desc = "Only triggers teammate death threshold related features in clear, rather than at any point in the run.").withDependency { deathAlert && ( sendThresholdMessage || displayTitle) }
    private val thresholdMessage by StringSetting("Message", "{deaths} Deaths Reached!", 128, desc = "The message to send in party chat when the teammate death threshold has been reached. Use \"{deaths}\" for the number of deaths.", placeholder = "{deaths} Deaths Reached!").withDependency { deathAlert && sendThresholdMessage }
    private val deathMessage by StringSetting("Death Message", "", 128, desc = "The message to send in party chat when a teammate dies in a run. Use \"{name}\" for the player name.", placeholder = "")

    var thresholdReached = false

    init {
        on<ChatPacketEvent> {
            if (RegexUtils.playerDeathRegex.matches(value)) {
                var ign = RegexUtils.playerDeathRegex.find(value)?.groupValues[2]
                if (ign != null && ign == "You") ign = mc.player?.name?.string
                if (DungeonUtils.deathCount + 1 == threshold.toInt() && !thresholdReached) { // Should work assuming that tab list always updates after the chat message (which it generally should)
                    if (sendThresholdMessage && (!onlyInClear || onlyInClear && DungeonUtils.inClear)) schedule(10) {
                        sendCommand("pc " + thresholdMessage.replace("{deaths}", "${DungeonUtils.deathCount + 1}"))
                    }
                    if (displayTitle && (!onlyInClear || onlyInClear && DungeonUtils.inClear)) alert(titleText.replace("&", "§"))
                    thresholdReached = true
                }
                if (deathMessage.isNotEmpty() && ign != null) sendCommand("pc " + deathMessage.replace("{name}", ign))
            }
        }

        on<LevelEvent.Load> {
            thresholdReached = false
        }
    }
}