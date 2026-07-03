package com.daprimeatce.odinextras.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.utils.createSoundSettings
import com.odtheking.odin.utils.formatTime
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.playSoundSettings
import net.minecraft.network.chat.Component
import java.time.Instant

object ServerAlert : Module(
    name = "Server Alert",
    description = "Warns you when you join a recently joined server."
) {
    private val thresholdMinutes by NumberSetting("Time Threshold", 15f, 1f, 60f, 1f, desc = "How long in minutes for a rejoin to warn you.")
    private val title by BooleanSetting("Create Title", true, desc = "Whether to create a title alongside the chat message.")

    private val dropdown by DropdownSetting("Sounds", false)
    private val sounds by BooleanSetting("Custom Sounds", false, desc = "Plays the selected custom sound.").withDependency { dropdown }
    private val soundSettings = createSoundSettings("Alert Sound", "block.note_block.pling") { sounds && dropdown }

    private val messageRegex = Regex("^Sending to server (.*)...$")
    private val servers = mutableMapOf<String, Long>()
    private var currentServer = ""

    init {
        on<ChatPacketEvent> {
            val result = messageRegex.find(value) ?: return@on
            val server = result.groups[1]?.value ?: return@on
            if (currentServer.isNotEmpty()) servers[currentServer] = Instant.now().epochSecond
            currentServer = server
        }

        on<LevelEvent.Load> {
            val lastTime = servers[currentServer]
            if (lastTime != null) {
                val secondsSince = Instant.now().epochSecond - lastTime
                if (secondsSince < thresholdMinutes * 60) {
                    schedule(10) {
                        modMessage("Recently joined $currentServer §a${formatTime(secondsSince * 1000, 0)} §rago.") // formatTime is in milliseconds
                        playSoundSettings(soundSettings())
                        if (title) {
                            mc.gui.setTimes(0, 40, 20)
                            mc.gui.setTitle(Component.literal("§cRecent Server"))
                        }
                    }
                }
            }
        }
    }

}