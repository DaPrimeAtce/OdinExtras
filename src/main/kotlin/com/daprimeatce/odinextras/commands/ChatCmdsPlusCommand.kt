package com.daprimeatce.odinextras.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.features.ModuleManager
import com.daprimeatce.odinextras.features.impl.skyblock.ChatCommandsPlus.blacklist
import com.daprimeatce.odinextras.features.impl.skyblock.ChatCommandsPlus.replacements
import com.daprimeatce.odinextras.features.impl.skyblock.ChatCommandsPlus.help

val chatCmdsPlusCommand = Commodore("chatcommands") {

    literal("help").runs {
        modMessage("Commands: ${help.filterValues { it }.keys.joinToString(", ")}")
    }

    literal("emotes").runs {
        replacements.forEach { (key, emote) ->
            modMessage("$key -> ${emote.replacement}")
        }
    }

    // Chat Commands Blacklist code adapted from OdinLegacy under BSD-3 Clause license
    // http://github.com/odtheking/OdinLegacy/blob/main/src/main/kotlin/me/odinmain/commands/impl/ChatCommandsCommand.kt

    literal("blacklist") {

        literal("add").runs { name: String ->
            val lowercase = name.lowercase()
            if (lowercase in blacklist) return@runs modMessage("$name is already in the blacklist.")
            if (name.equals(mc.player?.name?.string, true)) return@runs modMessage("You can't blacklist yourself!")
            modMessage("Added $name to blacklist.")
            blacklist.add(lowercase)
            ModuleManager.saveConfigurations()
        }

        literal("remove").runs { name: String ->
            val lowercase = name.lowercase()
            if (lowercase !in blacklist) return@runs modMessage("$name isn't in the blacklist.")
            modMessage("Removed $name from blacklist.")
            blacklist.remove(lowercase)
            ModuleManager.saveConfigurations()
        }

        literal("clear").runs {
            modMessage("Blacklist cleared.")
            blacklist.clear()
            ModuleManager.saveConfigurations()
        }

        literal("list").runs {
            if (blacklist.isEmpty()) return@runs modMessage("List is empty.")
            modMessage("Chat Commands Blacklist:\n${blacklist.joinToString("\n")}")
        }
    }
}