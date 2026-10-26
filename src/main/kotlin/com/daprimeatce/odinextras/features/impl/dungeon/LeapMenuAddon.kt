package com.daprimeatce.odinextras.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.RenderableSetting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.MessageEvent
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.impl.dungeon.LeapMenu
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils


object LeapMenuAddon {
    val leapAnnounce = BooleanSetting("Leap Announce", false, "Customize what happens when reaching 300 score.")
    val leapMessage = StringSetting(
        "Leap Message",
        "Leaped to {name}!",
        128,
        "The message to send when leaping to a player. Use \"{name}\" for specific player names.",
        "Leaped to {name}!"
    ).withDependency { leapAnnounce.value }


    fun register() {
        val settings = LeapMenu.settings

        (settings["Leap Announce"] as? BooleanSetting)?.value = false
        settings.remove("Leap Announce")

        settings["Leap Announce"] = leapAnnounce
        settings["Leap Message"] = leapMessage
    }

    private val leapedRegex = Regex("^You have teleported to (\\w{1,16})!$")

    init {
        on<MessageEvent.Chat> {
            if (leapAnnounce.value && DungeonUtils.inDungeons)
                leapedRegex.find(message)?.groupValues?.get(1)?.let { sendCommand("pc ${leapMessage.value.replace("{name}", it)}") }
        }

        EventBus.subscribe(this)
    }
}