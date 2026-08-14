package com.daprimeatce.odinextras.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.ClickGUI
import com.odtheking.odin.clickgui.HudManager
import com.odtheking.odin.utils.handlers.schedule
import com.daprimeatce.odinextras.utils.modMessageExtras
import com.daprimeatce.odinextras.features.impl.skyblock.ChatCommandsPlus.replacements

// Commands are handled via https://github.com/Stivais/Commodore
val odinExtrasCommand = Commodore("oe", "odinextras") {

    runs {
        schedule(0) { mc.setScreen(ClickGUI) }
    }

    literal("chatcommandsplus").runs {
        replacements.forEach { (key, value) ->
            modMessageExtras("$key -> $value")
        }
    }

    literal("hud", "gui").runs{
        schedule(0) { mc.setScreen(HudManager) }
    }
}
