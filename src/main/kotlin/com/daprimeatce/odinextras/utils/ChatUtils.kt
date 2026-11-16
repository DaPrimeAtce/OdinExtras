package com.daprimeatce.odinextras.utils

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.util.StringDecomposer

object ChatUtils {
    private val formatCodes = Regex("§.")

    private val colorToCode: Map<Int, String> =
        ChatFormatting.entries
            .filter { it.isColor }
            .associate { (TextColor.fromLegacyFormat(it)?.value ?: -1) to "§${it.char}" }

    private val prefixCache = HashMap<Style, String>()

    private fun prefixFor(style: Style): String = prefixCache.getOrPut(style) {
        buildString {
            append("§r")
            style.color?.value?.let { colorToCode[it]?.let(::append) }
            if (style.isBold) append("§l")
            if (style.isItalic) append("§o")
            if (style.isUnderlined) append("§n")
            if (style.isStrikethrough) append("§m")
            if (style.isObfuscated) append("§k")
        }
    }

    fun toColoredString(component: Component): String {
        val builder = StringBuilder(64)
        var lastStyle: Style? = null

        StringDecomposer.iterateFormatted(component, Style.EMPTY) { _, style, codepoint ->
            if (style !== lastStyle) {
                builder.append(prefixFor(style))
                lastStyle = style
            }
            builder.appendCodePoint(codepoint)
            true
        }
        return builder.toString()
    }

    fun removeFormatting(text: String): String =
        if ('§' !in text) text else text.replace(formatCodes, "")

}