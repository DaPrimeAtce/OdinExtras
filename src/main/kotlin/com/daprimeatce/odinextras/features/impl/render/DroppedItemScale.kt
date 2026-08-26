package com.daprimeatce.odinextras.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.features.Module

object DroppedItemScale : Module(
    name = "Dropped Item Scale",
    description = "Lets you change the size of dropped items."
) {
    val scale by NumberSetting("Dropped Item Scale", 1f, 0.5..5.0, 0.1f, desc = "Scale of dropped items.")
}