package com.daprimeatce.odinextras.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.features.Module

object ResourcePack : Module(
    name = "Resource Pack",
    description = "(WIP) Modify the Skyblock Resource Pack."
) {
    val movePack by BooleanSetting("Move Pack", true, "Allows you to move the resource pack instead of it being defaulted to the top of the resource pack list.")

    fun moveResourcePack(): Boolean? {
        if (!enabled || !movePack) return null
        return movePack
    }
}