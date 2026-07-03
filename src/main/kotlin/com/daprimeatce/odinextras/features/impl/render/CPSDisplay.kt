package com.daprimeatce.odinextras.features.impl.render

import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.events.InputEvent
import com.odtheking.odin.events.core.on

object CPSDisplay : Module(
    name = "CPS Display",
    description = "Displays your clicks per second."
) {
    private val button by SelectorSetting("Button", "Both", arrayListOf("Left", "Right", "Both"), desc = "The button to display the CPS of.")
    private val mouseText by BooleanSetting("Show Button", true, desc = "Shows the button name.")
    private val textColor by ColorSetting("Text Color", Color(239, 239, 239, 1f), allowAlpha = true, desc = "The color of the text.")

    private val hud by HUD("Display", "Displays your clicks per second in the HUD.") {
        leftClicks.removeAll { System.currentTimeMillis() - it > 1000 }
        rightClicks.removeAll { System.currentTimeMillis() - it > 1000 }

        val value = if (button == 0) "${leftClicks.size}" else "${rightClicks.size}"

        if (mouseText) {
            if (button == 2) {
                textDim("LMB", 1, 1, textColor)
                textDim(leftClicks.size.toString(), 7, 15, textColor)

                textDim("RMB", 35, 1, textColor)
                textDim(rightClicks.size.toString(), 42, 15, textColor)
            } else {
                val text = if (button == 0) "LMB" else "RMB"
                textDim(text, 1, 1, textColor)
                textDim(value, 7, 15, textColor)
            }
        } else {
            if (button == 2) {
                textDim(leftClicks.size.toString(), 1, 10, textColor)
                textDim(rightClicks.size.toString(), 35, 10, textColor)
            } else textDim(value, 5, 10, textColor)
        }
        if (button == 2) 54 to 24 else 20 to 24
    }

    private val leftClicks = mutableListOf<Long>()
    private val rightClicks = mutableListOf<Long>()

	init {
        on<InputEvent> {
            if (key.value == InputConstants.MOUSE_BUTTON_LEFT) leftClicks.add(System.currentTimeMillis())
            if (key.value == InputConstants.MOUSE_BUTTON_RIGHT) rightClicks.add(System.currentTimeMillis())
        }
    }
}