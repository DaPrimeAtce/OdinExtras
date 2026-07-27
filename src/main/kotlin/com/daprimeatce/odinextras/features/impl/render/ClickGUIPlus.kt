package com.daprimeatce.odinextras.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.features.Module

object ClickGUIPlus : Module(
    name = "Click GUI+",
    description = "Extra options for Odin's Click GUI."
) {
    val alphabeticalSorting by BooleanSetting("Alphabetical Sorting", true, desc = "Sorts the module lists alphabetically instead of Odin's default widest-first order.")

    fun azSorting(): Boolean? {
        if (!enabled || !alphabeticalSorting) return null
        return alphabeticalSorting
    }

}
