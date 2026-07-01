package com.daprimeatce.odinextras.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.MessageSentEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.sendChatMessage
import com.odtheking.odin.utils.skyblock.PartyUtils

object ChatCommandsPlus : Module(
    name = "Chat Commands+",
    description = "Adds extra chat commands. Highly recommended to use with base Odin chat commands."
) {
    private val moreChatEmotes by BooleanSetting("More Chat Emotes", false, desc = "Adds more chat emotes. See \"/chatcommandsplus\".")
    private val partyChatCommands by BooleanSetting("Party Commands", true, "Enables party chat commands.")
    private val guildChatCommands by BooleanSetting("Guild Commands", false, "Enables guild chat commands.")
    private val privateChatCommands by BooleanSetting("Private Commands", true, "Enables private chat commands.")
    private val showSettings by DropdownSetting("Show All Settings", false)

    private val disband by BooleanSetting("Disband", false, desc = "Disbands the party.").withDependency { showSettings }
    private val kickRandom by BooleanSetting("Russian Roulette", false, desc = "Kick a random player from the party.").withDependency { showSettings }
    private val hi by BooleanSetting("Hi", false, desc = "Auto replies with \"bye\" to \"hi\"").withDependency { showSettings }
    private val tyfr by BooleanSetting("TYFR", false, desc = "Auto leave party upon saying \"tyfr\" or \"tyfp\".").withDependency { showSettings }
    private val kick by BooleanSetting(":(", false, desc = "Kicks a player through \"[Name] :(\".").withDependency { showSettings }
    private val invite by BooleanSetting(":)", false, desc = "Invites a player through \"[Name] :)\".").withDependency { showSettings }

    private val messageRegex = Regex("^(?:Party > (\\[[^]]*?])? ?(\\w{1,16})(?: [ቾ⚒])?: ?(.+)$|Guild > (\\[[^]]*?])? ?(\\w{1,16})(?: \\[([^]]*?)])?: ?(.+)$|From (\\[[^]]*?])? ?(\\w{1,16}): ?(.+)$)")

    init {
        on<ChatPacketEvent> {
            val result = messageRegex.find(value) ?: return@on
            val channel = when(result.value.split(" ")[0]) {
                "From" -> if (!privateChatCommands) return@on else ChatChannel.PRIVATE
                "Party" -> if (!partyChatCommands)  return@on else ChatChannel.PARTY
                "Guild" -> if (!guildChatCommands)  return@on else ChatChannel.GUILD
                else -> return@on
            }

            val ign = result.groups[2]?.value ?: result.groups[5]?.value ?: result.groups[9]?.value ?: return@on
            val msg = result.groups[3]?.value ?: result.groups[7]?.value ?: result.groups[10]?.value ?: return@on

            schedule(4) {
                handleChatCommands(msg, ign, channel)
            }
        }

        on<MessageSentEvent> {
            if (tyfr && message.lowercase() == "tyfr" || message.lowercase() == "tyfp") {
                schedule(10) {
                    sendCommand("p leave")
                }
                return@on
            }

            if (!moreChatEmotes || (message.startsWith("/") && !listOf("/pc", "/ac", "/gc", "/msg", "/w", "/r").any { message.startsWith(it) })) return@on

            var replaced = false
            val words = message.split(" ").toMutableList()

            for (i in words.indices) {
                replacements[words[i]]?.let {
                    replaced = true
                    words[i] = it
                }
            }

            if (!replaced) return@on

            cancel()
            sendChatMessage(words.joinToString(" "))
        }
    }

    private fun handleChatCommands(message: String, name: String, channel: ChatChannel) {
        val words = message.split(" ").map { it.lowercase() }
        val user = if (words.size >= 2) words[words.size - 2] else null

        when (words.last()) {
            ":(" ->
                if (kick && PartyUtils.isLeader() && channel != ChatChannel.GUILD) {
                    sendCommand("p kick $user")
                    return
                }

            ":)" ->
                if (invite && PartyUtils.isLeader() && channel != ChatChannel.GUILD) {
                    sendCommand("p invite $user")
                    return
                }
        }

        when (words[0]) {
            "hi" ->
                if (hi && channel == ChatChannel.PARTY && name != mc.player?.name?.string) channelMessage("bye", name, channel)

            "!disband" ->
                if (disband && channel == ChatChannel.PARTY && PartyUtils.isLeader()) sendCommand("p disband")

           "!kickrandom" ->
                if (kickRandom && channel == ChatChannel.PARTY && PartyUtils.isLeader()) sendCommand("p kick ${PartyUtils.members.filterNot { it == mc.player?.name?.string }.random()}")
        }
    }

    private fun channelMessage(message: Any, name: String, channel: ChatChannel) {
        when (channel) {
            ChatChannel.GUILD -> sendCommand("gc $message")
            ChatChannel.PARTY -> sendCommand("pc $message")
            ChatChannel.PRIVATE -> sendCommand("msg $name $message")
        }
    }

    val replacements = mapOf(
        ":panda:" to "70sbloodcamp completed a device! (7/7) (100.248s | 100.248s)",
        ":ascent:" to "♿"
    )

    private enum class ChatChannel {
        PARTY, GUILD, PRIVATE
    }
}
