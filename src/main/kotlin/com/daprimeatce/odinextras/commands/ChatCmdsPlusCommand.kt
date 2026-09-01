package com.daprimeatce.odinextras.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.utils.modMessage
import com.daprimeatce.odinextras.features.impl.skyblock.ChatCommandsPlus.replacements
import com.daprimeatce.odinextras.features.impl.skyblock.ChatCommandsPlus.help

val chatCmdsPlusCommand = Commodore("chatcommands") {

    literal("list", "help").runs {
        modMessage("Commands: ${help.filterValues { it }.keys.joinToString(", ")}")
    }

    literal("emotes").runs {
        replacements.forEach { (key, emote) ->
            modMessage("$key -> ${emote.replacement}")
        }
    }

}