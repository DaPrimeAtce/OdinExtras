package com.daprimeatce.odinextras.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.features.Module

object ClickGUIPlus : Module(
    name = "Click GUI+",
    description = "Extra options for Odin's Click GUI."
) {
    val alphabeticalSorting by BooleanSetting("Alphabetical Sorting", true, desc = "Sorts the module lists alphabetically instead of Odin's default widest-first order.")
    val scale by StringSetting("GUI Scale", "1", desc = "Enables a custom GUI scale between 0.5 and 1.5.")
}
