package com.daprimeatce.odinextras.features.impl.render

import com.daprimeatce.odinextras.utils.RegexUtils
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.events.MessageEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.render.textDim

object Chat: Module(
    name = "Chat",
    description = "Various chat related features"
) {
    private val spamFilter by DropdownSetting("Spam Message Filter", desc = "Removes/compacts various bloat chat messages.")
    private val hideImplosion by BooleanSetting("Hide Implosion", false, desc = "Removes the Implosion messages from chat. (Your Implosion hit (x) enemies for (y) damage.)").withDependency{ spamFilter }
    private val hideTeleport by BooleanSetting("Hide Teleport", false, desc = "Removes the obstructed teleport message from chat. (There are blocks in the way!)").withDependency { spamFilter }
    private val hideInventoryFull by BooleanSetting("Hide Inventory Full", false, desc = "Removes the Inventory Full message. (Inventory full? Don't forget to check out your Storage inside the SkyBlock Menu!)").withDependency{ spamFilter }
    private val hideRadio by BooleanSetting("Hide Blazetekk Radio", false, desc = "Removes the Blazetekk™ Ham Radio message. (Your radio is weak. Find another enjoyer to boost it.)").withDependency{ spamFilter }
    private val hidePickupStash by BooleanSetting("Compact Pickup Stash", false, desc = "Removes an extra line from the Pickup Stash message. (>>> CLICK HERE to pick them up! <<<)").withDependency{ spamFilter }
    private val implosionHud by HUD("Implosion HUD", "Displays the Implosion/Wither Impact's damage message as a custom HUD.") {
        if (it) textDim("§7Implosion hit §c10 §7enemies for §c100,000,000 §7damage", 0,0)
        else if (implosionTime >= System.currentTimeMillis()) textDim("§7Implosion hit §c$enemyCount §7$enemy for §c$damage §7damage", 0,0)
        else textDim("", 0,0)
    }

    var enemyCount: String? = ""
    var enemy: String? = ""
    var damage: String? = ""
    var implosionTime = 0L

    init {
        on<MessageEvent.Chat> {
            if (implosionHud.enabled) {
                RegexUtils.implosionRegex.find(message)?.let {
                    enemyCount = it.groups[1]?.value
                    enemy = it.groups[2]?.value
                    damage = it.groups[3]?.value
                    implosionTime = System.currentTimeMillis() + 5000
                }
            }

            // genuine if statement of doom
            if (hideImplosion && RegexUtils.implosionRegex.matches(message) ||
                hideTeleport && RegexUtils.teleportRegex.matches(message) ||
                hideInventoryFull && RegexUtils.inventoryFullRegex.matches(message) ||
                hideRadio && RegexUtils.blaztekkRadioRegex.matches(message) ||
                hidePickupStash && RegexUtils.pickupStashRegex.matches(message))
                cancel()
        }
    }
}