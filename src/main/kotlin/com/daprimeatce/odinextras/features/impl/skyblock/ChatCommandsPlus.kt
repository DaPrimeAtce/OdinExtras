package com.daprimeatce.odinextras.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.MessageSentEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.sendChatMessage
import com.odtheking.odin.utils.skyblock.PartyUtils
import com.daprimeatce.odinextras.utils.RegexUtils
import kotlin.text.format

object ChatCommandsPlus : Module(
    name = "Chat Commands+",
    description = "Adds extra chat commands."
) {
    // These are visible settings that will render under this module in the GUI
    private val moreChatEmotes by BooleanSetting("More Chat Emotes", false, desc = "Adds more chat emotes. See \"/chatcommandsplus\".")
    private val partyChatCommands by BooleanSetting("Party Commands", true, "Enables party chat commands.")
    private val guildChatCommands by BooleanSetting("Guild Commands", false, "Enables guild chat commands.")
    private val privateChatCommands by BooleanSetting("Private Commands", true, "Enables private chat commands.")
    private val showSettings by DropdownSetting("Show Settings", false)

    private val hi by BooleanSetting("Hi", false, desc = "Auto replies with \"bye\" to \"hi\"").withDependency { showSettings }
    private val kickRandom by BooleanSetting("Kick Random", false, desc = "Kick a random player from the party.").withDependency { showSettings }
    private val tyfr by BooleanSetting("TYFR", false, desc = "Auto leave party upon saying \"tyfr\" or \"tyfp\".").withDependency { showSettings }
    private val kickFace by BooleanSetting(":(", false, desc = "Kicks a player through \"[Name] :(\".").withDependency { showSettings }
    private val inviteFace by BooleanSetting(":)", false, desc = "Invites a player through \"[Name] :)\".").withDependency { showSettings }
    private val rng by BooleanSetting("RNG", false, desc = "Will roll from 1 or a given min to a given max, inclusive.").withDependency { showSettings }

    private val alternatePrefixes by DropdownSetting("Alternate Prefixes", false, desc = "Allows alternate prefixes listed as comma separated values, eg \".allinv, allinv, ai\"")
    private val allinv by StringSetting("All Invite", "", desc = "Alternate values to trigger all invite.").withDependency { alternatePrefixes }
    private val inv by StringSetting("Invite", "", desc = "Alternate values to trigger invite.") .withDependency { alternatePrefixes }
    private val warp by StringSetting("Warp", "", desc = "Alternate values to trigger party warp.") .withDependency { alternatePrefixes }
    private val disband by StringSetting("Disband", "", desc = "Alternate values to trigger party disband.") .withDependency { alternatePrefixes }
    private val transfer by StringSetting("Transfer", "", desc = "Alternate values to trigger party transfer.") .withDependency { alternatePrefixes }
    private val kick by StringSetting("Kick", "", desc = "Alternate values to trigger party warp.") .withDependency { alternatePrefixes }
    private val reinv by StringSetting("Reinvite", "", desc = "Alternate values to trigger reinvite.") .withDependency { alternatePrefixes }

    init {
        on<ChatPacketEvent> {
            val result = RegexUtils.messageRegex.find(value) ?: return@on
            val channel = when(result.value.split(" ")[0]) {
                "From" -> if (!privateChatCommands) return@on else ChatChannel.PRIVATE
                "Party" -> if (!partyChatCommands)  return@on else ChatChannel.PARTY
                "Guild" -> if (!guildChatCommands)  return@on else ChatChannel.GUILD
                else -> return@on
            }

            val ign = result.groups[2]?.value ?: result.groups[5]?.value ?: result.groups[10]?.value ?: return@on
            val msg = result.groups[3]?.value ?: result.groups[7]?.value ?: result.groups[11]?.value ?: return@on

            schedule(4) {
                handleChatCommands(msg, ign, channel)
            }
        }

        on<MessageSentEvent> {
            if (tyfr && message.lowercase() in listOf("tyfr", "tyfp", "tyfrs", "gtg")) {
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
                if (kickFace && PartyUtils.isLeader() && channel != ChatChannel.GUILD && user?.isNotEmpty() == true) {
                    sendCommand("p kick $user")
                    return
                }

            ":)" ->
                if (inviteFace && PartyUtils.isLeader() && channel != ChatChannel.GUILD && user?.isNotEmpty() == true) {
                    sendCommand("p invite $user")
                    return
                }
        }

        when (words[0]) {
            "hi" ->
                if (hi && channel == ChatChannel.PARTY && name != mc.player?.name?.string) channelMessage("bye", name, channel)

            "!kickrandom" ->
                if (kickRandom && channel == ChatChannel.PARTY && PartyUtils.isLeader()) sendCommand("p kick ${PartyUtils.members.filterNot { it == mc.player?.name?.string }.random()}")

            "!rng" -> {
                if (!rng) return
                val numFirst = if (words.size > 1) words[1].replace(",", "").toLongOrNull() else null
                val numSecond = if (words.size > 2) words[2].replace(",", "").toLongOrNull() else 1
                if (numFirst != null && numSecond == null) channelMessage("Rolled ${"%,d".format((1..numFirst).random())} from range 1 to ${"%,d".format(numFirst)}.", name, channel)
                else if (numFirst != null && numSecond != null) {
                    val min = numFirst.coerceAtMost(numSecond)
                    val max = numFirst.coerceAtLeast(numSecond)
                    channelMessage("Rolled ${"%,d".format((min..max).random())} from range ${"%,d".format(min)} to ${"%,d".format(max)}.", name, channel)
                }
                else channelMessage("Could not parse a number.", name, channel)
            }

            else -> {
                val trimmedAllInv = allinv.split(",").map { it.trim() }
                val trimmedInv = inv.split(",").map { it.trim() }
                val trimmedWarp = warp.split(",").map { it.trim() }
                val trimmedTransfer = transfer.split(",").map { it.trim() }
                val trimmedDisband = disband.split(",").map { it.trim() }
                val trimmedKick = kick.split(",").map { it.trim() }
                val trimmedReinv = reinv.split(",").map { it.trim() }

                if (PartyUtils.isLeader() && allinv.isNotEmpty() && words[0] in trimmedAllInv && channel == ChatChannel.PARTY) sendCommand("p settings allinvite")
                if ((PartyUtils.isLeader() || !PartyUtils.isInParty) && inv.isNotEmpty() && words[0] in trimmedInv && channel == ChatChannel.PRIVATE) sendCommand("p $name")
                if (PartyUtils.isLeader() && trimmedWarp.isNotEmpty() && words[0] in trimmedWarp && channel == ChatChannel.PARTY) sendCommand("p warp")
                if (PartyUtils.isLeader() && trimmedTransfer.isNotEmpty() && words[0] in trimmedTransfer && channel == ChatChannel.PARTY) {
                    if (words.size > 1) sendCommand("p transfer ${findPartyMember(words[1])}")
                    else sendCommand("p transfer $name")
                }
                if (PartyUtils.isLeader() && trimmedDisband.isNotEmpty() && words[0] in trimmedDisband && channel == ChatChannel.PARTY) sendCommand("p disband")
                if (PartyUtils.isLeader() && trimmedKick.isNotEmpty() && words[0] in trimmedKick && channel == ChatChannel.PARTY && words.size > 1) sendCommand("p kick ${findPartyMember(words[1])}")
                if (PartyUtils.isLeader() && trimmedReinv.isNotEmpty() && words[0] in trimmedReinv && channel == ChatChannel.PARTY) {
                    modMessage("§aReinviting §6$name §ain 5 seconds...")
                    schedule(100) {
                        sendCommand("p invite $name")
                    }
                }
            }
        }


    }

    private fun findPartyMember(partialName: String): String =
        PartyUtils.members.find { it.contains(partialName, true) } ?: partialName

    private fun channelMessage(message: Any, name: String, channel: ChatChannel) {
        when (channel) {
            ChatChannel.GUILD -> sendCommand("gc $message")
            ChatChannel.PARTY -> sendCommand("pc $message")
            ChatChannel.PRIVATE -> sendCommand("msg $name $message")
        }
    }

    val replacements = mapOf(
        ":panda:" to "70sbloodcamp completed a device! (7/7) (100.248s | 100.248s)",
        ":x:" to ":no:", // This replacement assumes the player has MVP++
        "sped" to "♿", // This is only here for the people who get angry and say things like this impulsively without thinking, and get themselves muted as a result. The best filter though, is using your brain and not being toxic.
        ":wheelchair:" to "♿" // might as well
    )

    private enum class ChatChannel {
        PARTY, GUILD, PRIVATE
    }
}
