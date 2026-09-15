package com.daprimeatce.odinextras.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.ClickGUI
import com.odtheking.odin.clickgui.HudManager

val odinExtrasCommand = Commodore("oe", "odinextras") {

    runs {
        mc.schedule { mc.setScreen(ClickGUI) }
    }

    literal("hud", "gui").runs {
        mc.schedule { mc.setScreen(HudManager) }
    }
}