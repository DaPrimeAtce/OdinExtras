package com.daprimeatce.odinextras.features.impl.render

import com.daprimeatce.odinextras.utils.ChatUtils
import com.daprimeatce.odinextras.utils.RegexUtils
import com.odtheking.odin.clickgui.settings.RenderableSetting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.events.MessageEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.render.textDim
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import kotlin.math.pow

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

    private const val MESSAGE_LIFETIME = 3000 // milliseconds
    private var lastRemovedTime = -1L
    private val implosionHudMessages = ArrayDeque<ImplosionHudMessage>()
    private val implosionHud by HUD("Implosion HUD", "Displays the Implosion/Wither Impact's damage message as a custom HUD.") {
        if (it) textDim("§7Implosion hit §c10 §7enemies for §c100,000,000 §7damage", 0,0)
        else {
            // i dont even know bro

            val currentTime = System.currentTimeMillis()
            while (implosionHudMessages.isNotEmpty() && currentTime - implosionHudMessages.first().messageTime > MESSAGE_LIFETIME) {
                implosionHudMessages.removeFirst()
                lastRemovedTime = currentTime
            }

            val lineHeight = Minecraft.getInstance().font.lineHeight
            val yOffset = (lineHeight * (1f - easeOut((currentTime - lastRemovedTime) / 300f))).toInt()

            implosionHudMessages.forEachIndexed { index, message ->
                val lifetime = currentTime - message.messageTime
                val xOffset = (100 * ((1f - easeOut(lifetime / 300f)) + easeIn((lifetime - (MESSAGE_LIFETIME - 300)) / 300f))).toInt()

                textDim(ChatUtils.toColoredString(message.message), xOffset, index * lineHeight + yOffset)
            }

            textDim("", 0, 0)
        }
    }

    init {
        on<MessageEvent.Chat> {
            if (implosionHud.enabled && RegexUtils.implosionRegex.matches(message)) {
                implosionHudMessages.add(ImplosionHudMessage(component, System.currentTimeMillis()))
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

    fun easeOut(progress: Float): Float {
        val p = progress.coerceIn(0f, 1f)
        return 1f - (1f - p).pow(2f)
    }

    fun easeIn(progress: Float): Float {
        return progress.coerceIn(0f, 1f).pow(2f)
    }


    data class ImplosionHudMessage(
        val message: Component,
        val messageTime: Long
    )
}