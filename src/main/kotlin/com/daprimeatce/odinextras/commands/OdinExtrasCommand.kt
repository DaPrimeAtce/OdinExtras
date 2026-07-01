package com.odtheking.odinextras.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.utils.modMessage
import com.odtheking.odinextras.features.impl.skyblock.ChatCommandsPlus.replacements

// Commands are handled via https://github.com/Stivais/Commodore
val odinExtrasCommand = Commodore("chatcommandsplus") {

    runs {
        replacements.forEach { (key, value) ->
            modMessage("$key -> $value")
        }
    }
}
