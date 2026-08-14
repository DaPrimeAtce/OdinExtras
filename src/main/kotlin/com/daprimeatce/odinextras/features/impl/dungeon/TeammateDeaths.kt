package com.daprimeatce.odinextras.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.alert
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.core.on
import com.daprimeatce.odinextras.utils.RegexUtils

object TeammateDeaths: Module(
    name = "Teammate Deaths",
    description = "Various features based on teammate deaths in a dungeon."
) {
    private val deathAlert by BooleanSetting("Alerts", false, desc = "Sends an alert when your team reaches a threshold of deaths in a run.")
    private val threshold by NumberSetting("Alert Threshold", 5f, 0f, 10f, 1f, unit = " deaths", desc = "The amount of deaths before the alert is sent.").withDependency { deathAlert }
    private val displayTitle by BooleanSetting("Alert Title", false, desc = "Displays a title when the threshold of deaths in a run has been reached.").withDependency { deathAlert }
    private val titleText by StringSetting("Title", "&c&lS+ Lost!", desc = "What the title should display.").withDependency { deathAlert && displayTitle }
    private val thresholdMessage by StringSetting("Message on Threshold", "{deaths} Deaths Reached!", desc = "The message to send in party chat when the teammate death threshold has been reached.").withDependency { deathAlert }
    private val message by StringSetting("Message On Death", "", desc = "The message to send in party chat when a teammate dies in a run.")

    var deaths = 0
    var thresholdReached = false

    init {
        on<ChatPacketEvent> {
            if (RegexUtils.playerDeathRegex.matches(value)) {
                deaths++
                if (message.isNotEmpty()) {
                    schedule(1, true) {
                        sendCommand("pc $message")
                    }
                }
                if (deaths == threshold.toInt() && !thresholdReached) {
                    schedule(1, true) {
                        sendCommand("pc " + thresholdMessage.replace("{deaths}", "$deaths"))
                        if (displayTitle) {
                            alert(titleText.replace("&", "§"))
                        }
                    }
                    thresholdReached = true
                }
            }
        }
        on<LevelEvent.Load> {
            thresholdReached = false
            deaths = 0
        }
    }
}