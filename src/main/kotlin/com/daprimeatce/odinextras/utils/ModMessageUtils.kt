package com.daprimeatce.odinextras.utils

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style

// This is entirely just the same as base Odin's ModMessage, but with our own prefix instead of using just base Odin's.
// This is done to prevent confusion as to where the ModMessage is coming from, and to be more compliant with base Odin's BSD-3 Clause license.
// Refer to here for base Odin's ModMessage code: https://github.com/odtheking/Odin/blob/main/src/main/kotlin/com/odtheking/odin/utils/ChatUtils.kt

val mc: Minecraft = Minecraft.getInstance()

fun modMessageExtras(message: Any?, prefix: String = "§3Odin§aExtras §8»§r ", chatStyle: Style? = null) {
    val text = Component.literal("$prefix$message")
    chatStyle?.let { text.setStyle(chatStyle) }
    mc.schedule { mc.gui.chat.addClientSystemMessage(text) }
}

fun modMessageExtras(message: Component, prefix: String = "§3Odin§aExtras §8»§r ", chatStyle: Style? = null) {
    val text = Component.literal(prefix).append(message)
    chatStyle?.let { text.setStyle(chatStyle) }
    mc.schedule { mc.gui.chat.addClientSystemMessage(text) }
}