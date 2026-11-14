package com.daprimeatce.odinextras.utils

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.util.StringDecomposer

object ChatUtils {
    private val colorCodeField = ChatFormatting::class.java.getDeclaredField("code").apply { isAccessible = true }

    private val colors = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

    private val colorToFormatting: Map<Int, ChatFormatting> =
        ChatFormatting.entries
            .filter { colorCodeField.get(it) as Char in colors }
            .associateBy { TextColor.fromLegacyFormat(it)?.value ?: -1 }


    fun toColoredString(component: Component): String {
        val builder = StringBuilder()
        var lastStyle: Style? = null

        StringDecomposer.iterateFormatted(component, Style.EMPTY) { _, style, codepoint ->
            if (style != lastStyle) {
                builder.append(ChatFormatting.RESET)
                style.color?.value?.let { rgb -> colorToFormatting[rgb]?.let { builder.append(it) } }
                if (style.isBold) builder.append(ChatFormatting.BOLD)
                if (style.isItalic) builder.append(ChatFormatting.ITALIC)
                if (style.isUnderlined) builder.append(ChatFormatting.UNDERLINE)
                if (style.isStrikethrough) builder.append(ChatFormatting.STRIKETHROUGH)
                if (style.isObfuscated) builder.append(ChatFormatting.OBFUSCATED)
                lastStyle = style
            }
            builder.appendCodePoint(codepoint)
            true
        }

        return builder.toString()
    }
}