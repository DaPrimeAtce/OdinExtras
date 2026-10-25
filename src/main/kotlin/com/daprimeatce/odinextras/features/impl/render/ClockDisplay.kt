package com.daprimeatce.odinextras.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.render.ItemStateRenderer.Companion.drawItemStack
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object ClockDisplay: Module(
    name = "Clock Display",
    description = "Displays the current system time."
) {
    enum class TimeFormat { Fancy, Compact }
    private val fullDay by BooleanSetting("24 Hour Format", false, desc = "Displays the clock as 24 hr format instead of AM/PM.")
    private val clockColor by ColorSetting("Clock Color", Color(255, 255, 255, 1f), allowAlpha = true, desc = "The color that the clock should use.")
    private val formatting by SelectorSetting("Format", TimeFormat.Fancy, "The format that the clock should use.", listOf(TimeFormat.Fancy, TimeFormat.Compact))
    @Suppress("unused")
    private val hud by HUD("Clock", "Displays the system time in a HUD.") {
        if (formatting == TimeFormat.Fancy) drawItemStack(ItemStack(Items.CLOCK), -18, -4)
        if (fullDay) textDim("${ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))}", 0 ,0, clockColor)
        else textDim("${ZonedDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ENGLISH))}", 0 ,0, clockColor)
    }
}