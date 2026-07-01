package com.daprimeatce.odinextras.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.utils.modMessage
import com.daprimeatce.odinextras.features.impl.skyblock.ChatCommandsPlus.replacements

// Commands are handled via https://github.com/Stivais/Commodore
val commands = mutableListOf(

    Commodore("chatcommandsplus") {
        runs {
            replacements.forEach { (key, value) ->
                modMessage("$key -> $value")
            }
        }
    },

    Commodore("odinextrastest") {
        runs {
            modMessage("Test")
        }
    }

)