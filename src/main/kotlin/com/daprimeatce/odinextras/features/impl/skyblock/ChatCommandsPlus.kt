package com.daprimeatce.odinextras.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.MessageSentEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.dungeon.DungeonQueue
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.sendChatMessage
import com.odtheking.odin.utils.skyblock.PartyUtils
import com.odtheking.odin.utils.skyblock.LocationUtils
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import com.odtheking.odin.utils.modMessage
import net.minecraft.sounds.SoundEvents
import com.odtheking.odin.utils.*
import com.daprimeatce.odinextras.utils.RegexUtils
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.text.format

object ChatCommandsPlus : Module(
    name = "Chat Commands+",
    description = "Adds chat commands from base Odin and expands upon chat command features that base Odin doesn't have."
) {
    // Q: Why did we override the Chat Commands module in base Odin entirely with our own?
    // A: We had our own changes we wanted to make to Chat Commands, however mixins were not going to cut it for the changes we wanted to make.
    // Instead of having two modules with the same features having slightly different functionality, we just disable base Odin's Chat Commands module.
    // That said, almost all the commands here are copied over from base Odin and adapted to our changes in Chat Commands+.
    // See https://github.com/odtheking/Odin/blob/main/src/main/kotlin/com/odtheking/odin/features/impl/skyblock/ChatCommands.kt for base Odin's list of chat commands we used.

    private val moreChatEmotes by BooleanSetting("Chat Emotes", false, desc = "Adds more chat emotes. See \"/chatcommands\".")
    private val partyChatCommands by BooleanSetting("Party Commands", true, "Enables party chat commands.")
    private val guildChatCommands by BooleanSetting("Guild Commands", false, "Enables guild chat commands.")
    private val privateChatCommands by BooleanSetting("Private Commands", true, "Enables private chat commands.")
    private val showSettings by DropdownSetting("Show Settings", false, desc = "Enables chat commands and allows prefixes for most chat commands listed as comma separated values, eg \".allinv, allinv, ai\"")

    private val hi by BooleanSetting("Hi", false, desc = "Auto replies with \"bye\" to \"hi\"").withDependency { showSettings }
    private val kickRandom by BooleanSetting("Kick Random", false, desc = "Kick a random player from the party.").withDependency { showSettings }
    private val tyfr by BooleanSetting("TYFR", false, desc = "Auto leave party upon saying \"tyfr\" or \"tyfp\".").withDependency { showSettings }
    private val tyfrDelay by NumberSetting("TYFR Delay", 10, 5, 40, 1, unit = "t", desc = "The delay in ticks before leaving the party.").withDependency { showSettings && tyfr}
    private val tyfrWarning by BooleanSetting("TYFR Warning", false, desc = "Sends a local warning message when you trigger the TYFR command.").withDependency { showSettings && tyfr}
    private val kickFace by BooleanSetting(":(", false, desc = "Kicks a player through \"[Name] :(\".").withDependency { showSettings }
    private val inviteFace by BooleanSetting(":)", false, desc = "Invites a player through \"[Name] :)\".").withDependency { showSettings }
    private val eightball by BooleanSetting("8ball", true, desc = "Sends a random 8ball response.").withDependency { showSettings }
    private val dice by BooleanSetting("Dice", true, desc = "Rolls a six sided dice.").withDependency { showSettings }
    private val autoConfirm by BooleanSetting("Auto Confirm Invite", true, desc = "Removes the need to confirm a party invite with the !invite command.").withDependency { showSettings }
    private val rng by BooleanSetting("RNG", true, desc = "Will roll from 1 or a given min to a given max, inclusive.").withDependency { showSettings }
    private val warp by StringSetting("Warp", "!warp, !w", desc = "Executes the /party warp command.") .withDependency { showSettings }
    private val coords by StringSetting("Coords (coords)", "!coords, !co", desc = "Sends your current coordinates.").withDependency { showSettings }
    private val allinv by StringSetting("All Invite", "!ai, !allinv, !allinvite", desc = "Executes the /party settings allinvite command.").withDependency { showSettings }
    private val boop by StringSetting("Boop", "!boop", desc = "Executes the /boop command.").withDependency { showSettings }
    private val kick by StringSetting("Kick", "!kick, !k", desc = "Executes the /p kick command.") .withDependency { showSettings }
    private val cf by StringSetting("Coinflip (cf)", "!cf", desc = "Sends the result of a coinflip.").withDependency { showSettings }
    private val transfer by StringSetting("Transfer", "!pt, !ptme, !transfer", desc =  "Executes the /party transfer command.") .withDependency { showSettings }
    private val reinv by StringSetting("Reinvite", "!reinv, !reinvite", desc = "Reinvites the player who sent it a few seconds later.") .withDependency { showSettings }
    private val ping by StringSetting("Ping", "!ping", desc = "Sends your current Ping.").withDependency { showSettings }
    private val tps by StringSetting("Tps", "!tps", desc = "Sends your server's current TPS.").withDependency { showSettings }
    private val fps by StringSetting("FPS", "!fps", desc = "Sends your current FPS.").withDependency { showSettings }
    private val dt by StringSetting("DT", "!downtime, !dt", desc = "Sets a reminder for the end of the run.").withDependency { showSettings }
    private val inv by StringSetting("Invite", "!invite, !inv", desc = "Invites the player to your party.") .withDependency { showSettings }
    private val qInstance by StringSetting("Queue instance cmds", "!f1, !f2, !f3, !f4, !f5, !f6, !f7, !m1, !m2, !m3, !m4, !m5, !m6, !m7, !t1, !t2, !t3, !t4, !t5", desc = "Queue dungeons commands.").withDependency { showSettings }
    private val time by StringSetting("Time", "!time", desc = "Sends the current time.").withDependency { showSettings }
    private val demote by StringSetting("Demote", "!demote", desc = "Executes the /party demote command.").withDependency { showSettings }
    private val promote by StringSetting("Promote", "!promote", desc = "Executes the /party promote command.").withDependency { showSettings }
    private val kickOffline by StringSetting("Kick Offline", "!kickoffline, !ko", desc = "Allows you to kick offline players.").withDependency { showSettings }
    private val location by StringSetting("Location", "!location", desc = "Sends your current location.").withDependency { showSettings }
    private val holding by StringSetting("Holding", "!holding", desc = "Sends the item you are holding.").withDependency { showSettings }
    private val disband by StringSetting("Disband", ".disband", desc = "Disbands the party.") .withDependency { showSettings }

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

    private val dtReason = mutableListOf<Pair<String, String>>()

    private fun handleChatCommands(message: String, name: String, channel: ChatChannel) {
        val words = message.split(" ").map { it.lowercase() }
        val faceUser = words.getOrNull(0)?.takeIf { it.length <= 16 }

        when (words.last()) {
            ":(" ->
                if (kickFace && PartyUtils.isLeader() && channel != ChatChannel.GUILD && faceUser?.isNotEmpty() == true) {
                    sendCommand("p kick $faceUser")
                    return
                }

            ":)" ->
                if (inviteFace && PartyUtils.isLeader() && channel != ChatChannel.GUILD && faceUser?.isNotEmpty() == true) {
                    sendCommand("p invite $faceUser")
                    return
                }
        }

        when (words[0]) {
            "hi" ->
                if (hi && channel == ChatChannel.PARTY && name != mc.player?.name?.string) channelMessage("bye", name, channel)

            "8ball" ->
                if (eightball) channelMessage(responses.random(), name, channel)

            "dice" ->
                if (dice) channelMessage((1..6).random(), name, channel)

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

            "tyfr", "tyfp", "tyfrs", "gtg" -> {
                if (tyfr && channel == ChatChannel.PARTY && name == mc.player?.name?.string) {
                    if (tyfrWarning) modMessage("§c⚠ §eTYFR found, leaving party in §b$tyfrDelay §eticks. §c⚠")
                    schedule(tyfrDelay) {
                        sendCommand("p leave")
                    }
                }
            }

            else -> {
                val trimmedWarp = warp.split(",").map { it.trim() }
                val trimmedCoords = coords.split(",").map { it.trim() }
                val trimmedAllInv = allinv.split(",").map { it.trim() }
                val trimmedBoop = boop.split(",").map { it.trim() }
                val trimmedKick = kick.split(",").map { it.trim() }
                val trimmedCf = cf.split(",").map { it.trim() }
                val trimmedTransfer = transfer.split(",").map { it.trim() }
                val trimmedReinv = reinv.split(",").map { it.trim() }
                val trimmedPing = ping.split(",").map { it.trim() }
                val trimmedTPS = tps.split(",").map { it.trim() }
                val trimmedFPS = fps.split(",").map { it.trim() }
                val trimmedDT = dt.split(",").map { it.trim() }
                val trimmedInv = inv.split(",").map { it.trim() }
                val trimmedQInstance = qInstance.split(",").map { it.trim() }
                val trimmedTime = time.split(",").map { it.trim() }
                val trimmedDemote = demote.split(",").map { it.trim() }
                val trimmedPromote = promote.split(",").map { it.trim() }
                val trimmedKickOffline = kickOffline.split(",").map { it.trim() }
                val trimmedLocation = location.split(",").map { it.trim() }
                val trimmedHolding = holding.split(",").map { it.trim() }
                val trimmedDisband = disband.split(",").map { it.trim() }


                if (PartyUtils.isLeader() && trimmedWarp.isNotEmpty() && words[0] in trimmedWarp && channel == ChatChannel.PARTY) sendCommand("p warp")
                if (trimmedCoords.isNotEmpty() && words[0] in trimmedCoords) channelMessage(getPositionString(), name, channel)
                if (PartyUtils.isLeader() && allinv.isNotEmpty() && words[0] in trimmedAllInv && channel == ChatChannel.PARTY) sendCommand("p settings allinvite")
                if (trimmedBoop.isNotEmpty() && words[0] in trimmedBoop) sendCommand("boop $name")
                if (PartyUtils.isLeader() && trimmedKick.isNotEmpty() && words[0] in trimmedKick && channel == ChatChannel.PARTY && words.size > 1 && words[1].length <= 16) sendCommand("p kick ${findPartyMember(words[1])}")
                if (trimmedCf.isNotEmpty() && words[0] in trimmedCf) channelMessage(if (Math.random() < 0.5) "Heads" else "Tails", name, channel)
                if (PartyUtils.isLeader() && trimmedTransfer.isNotEmpty() && words[0] in trimmedTransfer && channel == ChatChannel.PARTY) {
                    if (words.size > 1 && words[1].length <= 16) sendCommand("p transfer ${findPartyMember(words[1])}")
                    else sendCommand("p transfer $name")
                }
                if (trimmedReinv.isNotEmpty() && words[0] in trimmedReinv && channel == ChatChannel.PARTY) {
                    modMessage("§aReinviting §6$name §ain 5 seconds...")
                    schedule(100) {
                        sendCommand("p invite $name")
                    }
                }
                if (trimmedPing.isNotEmpty() && words[0] in trimmedPing && channel == ChatChannel.PARTY) channelMessage("Ping: ${ServerUtils.currentPing}ms", name, channel)
                if (trimmedTPS.isNotEmpty() && words[0] in trimmedTPS && channel == ChatChannel.PARTY) channelMessage("TPS: ${ServerUtils.averageTps.toFixed(1)}", name, channel)
                if (trimmedFPS.isNotEmpty() && words[0] in trimmedFPS && channel == ChatChannel.PARTY) channelMessage("FPS: ${mc.fps}", name, channel)
                if ((PartyUtils.isLeader() || !PartyUtils.isInParty) && inv.isNotEmpty() && words[0] in trimmedInv && channel == ChatChannel.PRIVATE) {
                    if (autoConfirm) return sendCommand("p invite $name")
                    modMessage(Component.literal("§aClick on this message to invite $name to your party!").withStyle {
                        it.withClickEvent(ClickEvent.RunCommand("/party invite $name"))
                            .withHoverEvent(HoverEvent.ShowText(Component.literal("§6Click to invite $name to your party.")))
                    })
                    playSoundAtPlayer(SoundEvents.NOTE_BLOCK_PLING.value())
                }
                if (trimmedDT.isNotEmpty() && words[0] in trimmedDT && channel == ChatChannel.PARTY) {
                    val reason = words.drop(1).joinToString(" ").takeIf { it.isNotBlank() } ?: "No reason given"
                    if (dtReason.any { it.first == name }) return modMessage("§6${name} §calready has a reminder!")
                    modMessage("§aReminder set for the end of the run! §7(disabled auto requeue for this run)")
                    dtReason.add(name to reason)
                    DungeonQueue.disableRequeue = true
                }
                if (trimmedQInstance.isNotEmpty() && words[0] in trimmedQInstance && channel == ChatChannel.PARTY) {
                    modMessage("§8Entering -> §e${words[0].capitalizeFirst()}")
                    sendCommand("odin ${words[0].lowercase()}")
                }
                if (trimmedTime.isNotEmpty() && words[0] in trimmedTime) channelMessage("Current Time: ${ZonedDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss z", Locale.ENGLISH))}", name, channel)
                if (trimmedDemote.isNotEmpty() && words[0] in trimmedDemote && channel == ChatChannel.PARTY) sendCommand("party demote $name")
                if (trimmedPromote.isNotEmpty() && words[0] in trimmedPromote && channel == ChatChannel.PARTY) sendCommand("party promote $name")
                if (trimmedKickOffline.isNotEmpty() && words[0] in trimmedKickOffline && channel == ChatChannel.PARTY) sendCommand("p kickoffline")
                if (trimmedLocation.isNotEmpty() && words[0] in trimmedLocation && channel == ChatChannel.PARTY) channelMessage("Current Location: ${LocationUtils.currentArea.displayName}", name, channel)
                if (trimmedHolding.isNotEmpty() && words[0] in trimmedHolding && channel == ChatChannel.PARTY) channelMessage("Holding: ${mc.player?.mainHandItem?.hoverName?.string?.noControlCodes ?: "Nothing :("}", name, channel)
                if (PartyUtils.isLeader() && trimmedDisband.isNotEmpty() && words[0] in trimmedDisband && channel == ChatChannel.PARTY) sendCommand("p disband")
            }
        }
    }

    private fun findPartyMember(partialName: String): String =
        PartyUtils.members.find { it.contains(partialName, true) } ?: partialName.take(16)

    private fun channelMessage(message: Any, name: String, channel: ChatChannel) {
        when (channel) {
            ChatChannel.GUILD -> sendCommand("gc $message")
            ChatChannel.PARTY -> sendCommand("pc $message")
            ChatChannel.PRIVATE -> sendCommand("msg $name $message")
        }
    }

    private val responses = arrayOf(
        "It is certain.", "It is decidedly so.", "Without a doubt.",
        "Yes definitely.", "You may rely on it.", "As I see it, yes.",
        "Most likely.", "Outlook good.", "Yes.", "Signs point to yes.",
        "Reply hazy try again.", "Ask again later.", "Better not tell you now.",
        "Cannot predict now.", "Concentrate and ask again.", "Don't count on it.",
        "My reply is no.", "My sources say no.", "Outlook not so good.", "Very doubtful."
    )

    val replacements = mapOf(
        "<3" to "❤",
        "o/" to "( ﾟ◡ﾟ)/",
        ":star:" to "✮",
        ":yes:" to "✔",
        ":no:" to "✖",
        ":java:" to "☕",
        ":arrow:" to "➜",
        ":shrug:" to "¯\\_(\u30c4)_/¯",
        ":tableflip:" to "(╯°□°）╯︵ ┻━┻",
        ":totem:" to "☉_☉",
        ":typing:" to "✎...",
        ":maths:" to "√(π+x)=L",
        ":snail:" to "@'-'",
        "ez" to "ｅｚ",
        ":thinking:" to "(0.o?)",
        ":gimme:" to "༼つ◕_◕༽つ",
        ":wizard:" to "('-')⊃━☆ﾟ.*･｡ﾟ",
        ":pvp:" to "⚔",
        ":peace:" to "✌",
        ":puffer:" to "<('O')>",
        "h/" to "ヽ(^◇^*)/",
        ":sloth:" to "(・⊝・)",
        ":dog:" to "(ᵔᴥᵔ)",
        ":dj:" to "ヽ(⌐■_■)ノ♬",
        ":yey:" to "ヽ (◕◡◕) ﾉ",
        ":snow:" to "☃",
        ":dab:" to "<o/",
        ":cat:" to "= ＾● ⋏ ●＾ =",
        ":cute:" to "(✿◠‿◠)",
        ":skull:" to "☠",
        ":bum:" to "♿",
        ":panda:" to "70sbloodcamp completed a device! (7/7) (100.248s | 100.248s)",
        ":x:" to ":no:", // This replacement assumes the player has MVP++
        ":wheelchair:" to "♿" // might as well
    )

    private enum class ChatChannel {
        PARTY, GUILD, PRIVATE
    }
}
