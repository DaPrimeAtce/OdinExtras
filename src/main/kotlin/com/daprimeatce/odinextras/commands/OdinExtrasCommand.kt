package com.daprimeatce.odinextras.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.ClickGUI
import com.odtheking.odin.clickgui.HudManager
// import com.daprimeatce.odinextras.features.impl.skyblock.ChatCommandsPlus.replacements
import com.odtheking.odin.utils.modMessage

// Commands are handled via https://github.com/Stivais/Commodore
val odinExtrasCommand = Commodore("oe", "odinextras") {

    runs {
        mc.schedule { mc.setScreenAndShow(ClickGUI) }
    }

//    literal("chatemotes").runs {
//        replacements.forEach { (key, emote) ->
//            modMessage("$key -> ${emote.replacement}")
//        }
//    }

    literal("hud", "gui").runs {
        mc.schedule { mc.setScreenAndShow(HudManager) }
    }
}