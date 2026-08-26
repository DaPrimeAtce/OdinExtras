package com.daprimeatce.odinextras.features.impl.render

import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.events.InputEvent
import com.odtheking.odin.events.core.on

// Code is adapted from OdinLegacy to work on modern Minecraft versions under the BSD-3 Clause license
// https://github.com/odtheking/OdinLegacy/blob/main/src/main/kotlin/me/odinmain/features/impl/render/CPSDisplay.kt

object CPSDisplay : Module(
    name = "CPS Display",
    description = "Displays your clicks per second."
) {
    enum class ButtonTypes { LEFT, RIGHT, BOTH }
    private val button by SelectorSetting("Button", ButtonTypes.LEFT, "The button to display the CPS of.", listOf(ButtonTypes.LEFT, ButtonTypes.RIGHT,
        ButtonTypes.BOTH))
    private val mouseText by BooleanSetting("Show Button", true, desc = "Shows the button name.")
    private val textColor by ColorSetting("Text Color", Color(239, 239, 239, 1f), allowAlpha = true, desc = "The color of the text.")
    private val textSpacing by NumberSetting("Text Spacing", 15f, 10.0..20.0, 1f, desc = "The spacing between the click counter and the button name.")

    @Suppress("unused")
    private val hud by HUD("Display", "Displays your clicks per second in the HUD.", false) {
        leftClicks.removeAll { System.currentTimeMillis() - it > 1000 }
        rightClicks.removeAll { System.currentTimeMillis() - it > 1000 }

        val value = if (button == ButtonTypes.LEFT) "${leftClicks.size}" else "${rightClicks.size}"

        if (mouseText) {
            if (button == ButtonTypes.BOTH) {
                textDim("LMB", 1, 1, textColor)
                textDim(leftClicks.size.toString(), 7, textSpacing.toInt(), textColor)

                textDim("RMB", 35, 1, textColor)
                textDim(rightClicks.size.toString(), 42, textSpacing.toInt(), textColor)
            } else {
                val text = if (button == ButtonTypes.LEFT) "LMB" else "RMB"
                textDim(text, 1, 1, textColor)
                textDim(value, 7, textSpacing.toInt(), textColor)
            }
        } else {
            if (button == ButtonTypes.BOTH) {
                textDim(leftClicks.size.toString(), 1, 10, textColor)
                textDim(rightClicks.size.toString(), 35, 10, textColor)
            } else textDim(value, 5, 10, textColor)
        }
        if (button == ButtonTypes.BOTH) 54 to 24 else 20 to 24
    }

    private val leftClicks = mutableListOf<Long>()
    private val rightClicks = mutableListOf<Long>()

	init {
        on<InputEvent> {
            if (!enabled) return@on

            if (key.value == InputConstants.MOUSE_BUTTON_LEFT) leftClicks.add(System.currentTimeMillis())
            if (key.value == InputConstants.MOUSE_BUTTON_RIGHT) rightClicks.add(System.currentTimeMillis())
        }
    }
}