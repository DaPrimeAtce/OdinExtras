package com.daprimeatce.odinextras.utils

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.util.StringDecomposer

object ChatUtils {
    private const val MAX_CACHE = 256

    private val formatCodes = Regex("§.")
    private val codeField = ChatFormatting::class.java.getDeclaredField("code").apply { isAccessible = true }

    private val RESET = ChatFormatting.RESET.toString()
    private val BOLD = ChatFormatting.BOLD.toString()
    private val ITALIC = ChatFormatting.ITALIC.toString()
    private val UNDERLINE = ChatFormatting.UNDERLINE.toString()
    private val STRIKETHROUGH = ChatFormatting.STRIKETHROUGH.toString()
    private val OBFUSCATED = ChatFormatting.OBFUSCATED.toString()

    private val colorToCode: Map<Int, String> =
        ChatFormatting.entries
            .filter { (codeField.get(it) as Char).let { c -> c in '0'..'9' || c in 'a'..'f' } }
            .associate { (TextColor.fromLegacyFormat(it)?.value ?: -1) to it.toString() }

    private val prefixCache = object : LinkedHashMap<Style, String>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Style, String>) = size > MAX_CACHE
    }

    private fun prefixFor(style: Style): String = synchronized(prefixCache) {
        prefixCache.getOrPut(style) {
            buildString {
                append(RESET)
                style.color?.value?.let { colorToCode[it]?.let(::append) }
                if (style.isBold) append(BOLD)
                if (style.isItalic) append(ITALIC)
                if (style.isUnderlined) append(UNDERLINE)
                if (style.isStrikethrough) append(STRIKETHROUGH)
                if (style.isObfuscated) append(OBFUSCATED)
            }
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