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
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.sendChatMessage
import com.odtheking.odin.utils.skyblock.PartyUtils
import com.daprimeatce.odinextras.utils.RegexUtils
import com.odtheking.odin.features.impl.dungeon.DungeonQueue
import com.odtheking.odin.utils.ServerUtils
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.capitalizeFirst
import com.odtheking.odin.utils.getPositionString
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.playSoundAtPlayer
import com.odtheking.odin.utils.skyblock.LocationUtils
import com.odtheking.odin.utils.toFixed
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.sounds.SoundEvents
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object ChatCommandsPlus : Module(
    name = "Chat Commands+",
    description = "Adds extra chat commands."
) {
    private val moreChatEmotes by BooleanSetting("Chat Emotes", false, desc = "Adds chat emotes. See \"/oe chatcommandsplus\".")
    private val chatEmotesMvpPlusPlus by BooleanSetting("I'm MVP++", false, desc = "Skips replacing emotes that MVP++ already has access to.").withDependency { moreChatEmotes }

    private val partyChatCommands by BooleanSetting("Party Commands", true, "Enables party chat commands.")
    private val guildChatCommands by BooleanSetting("Guild Commands", false, "Enables guild chat commands.")
    private val privateChatCommands by BooleanSetting("Private Commands", true, "Enables private chat commands.")
    private val booleanSettings by DropdownSetting("Toggle Commands", false, desc = "Enable togglable chat commands")
    private val stringSettings by DropdownSetting("Customizable Commands", false, desc = "Enable chat commands that allows custom keywords as comma separated values, eg \".allinv, allinv, ai\"")

    private val hi by BooleanSetting("Hi", false, desc = "Auto replies with \"bye\" to \"hi\"").withDependency { booleanSettings }
    private val kickRandom by BooleanSetting("Kick Random", false, desc = "Kick a random player from the party.").withDependency { booleanSettings }
    private val kickFace by BooleanSetting(":(", false, desc = "Kicks a player through \"[Name] :(\".").withDependency { booleanSettings }
    private val inviteFace by BooleanSetting(":)", false, desc = "Invites a player through \"[Name] :)\".").withDependency { booleanSettings }
    private val eightball by BooleanSetting("8ball", true, desc = "Sends a random 8ball response.").withDependency { booleanSettings }
    private val dice by BooleanSetting("Dice", true, desc = "Rolls a six sided dice.").withDependency { booleanSettings }
    private val autoConfirm by BooleanSetting("Auto Confirm Invite", true, desc = "Removes the need to confirm a party invite with the !invite command.").withDependency { booleanSettings }
    private val rng by BooleanSetting("RNG", true, desc = "Will roll from 1 or a given min to a given max, inclusive.").withDependency { booleanSettings }
    private val qInstance by BooleanSetting("Queue Instance", true, desc = "Queue instance commands.").withDependency { booleanSettings }

    private val tyfr by StringSetting("TYFR", "", desc = "Auto leave party upon saying a specified key word(s).").withDependency { stringSettings }
    private val tyfrDelay by NumberSetting("TYFR Delay", 10, 5, 40, 1, unit = "t", desc = "The delay in ticks before leaving the party.").withDependency { stringSettings && tyfr.isNotEmpty()}
    private val tyfrWarning by BooleanSetting("TYFR Warning", false, desc = "Sends a local warning message when you trigger the TYFR command.").withDependency { stringSettings && tyfr.isNotEmpty()}
    private val warp by StringSetting("Warp", "!warp, !w", desc = "Executes the /party warp command.") .withDependency { stringSettings }
    private val coords by StringSetting("Coords", "!coords, !co", desc = "Sends your current coordinates.").withDependency { stringSettings }
    private val allinv by StringSetting("All Invite", "!ai, !allinv, !allinvite", desc = "Executes the /party settings allinvite command.").withDependency { stringSettings }
    private val boop by StringSetting("Boop", "!boop", desc = "Executes the /boop command.").withDependency { stringSettings }
    private val kick by StringSetting("Kick", "!kick, !k", desc = "Executes the /p kick command.") .withDependency { stringSettings }
    private val cf by StringSetting("Coinflip", "!cf, !coinflip", desc = "Sends the result of a coinflip.").withDependency { stringSettings }
    private val transfer by StringSetting("Transfer", "!pt, !ptme, !transfer", desc =  "Executes the /party transfer command.") .withDependency { stringSettings }
    private val reinv by StringSetting("Reinvite", "!reinv, !reinvite", desc = "Reinvites the player who sent it a few seconds later.") .withDependency { stringSettings }
    private val ping by StringSetting("Ping", "!ping", desc = "Sends your current Ping.").withDependency { stringSettings }
    private val tps by StringSetting("TPS", "!tps", desc = "Sends your server's current TPS.").withDependency { stringSettings }
    private val fps by StringSetting("FPS", "!fps", desc = "Sends your current FPS.").withDependency { stringSettings }
    private val dt by StringSetting("DT", "!downtime, !dt", desc = "Sets a reminder for the end of the run and cancels auto requeue.").withDependency { stringSettings }
    private val undt by StringSetting("UnDT", "!undowntime, !undt", desc = "Undoes a downtime command from earlier in a run.").withDependency { stringSettings }
    private val inv by StringSetting("Invite", "!invite, !inv", desc = "Invites the player to your party.") .withDependency { stringSettings }
    private val time by StringSetting("Time", "!time", desc = "Sends the current time.").withDependency { stringSettings }
    private val demote by StringSetting("Demote", "!demote", desc = "Executes the /party demote command.").withDependency { stringSettings }
    private val promote by StringSetting("Promote", "!promote", desc = "Executes the /party promote command.").withDependency { stringSettings }
    private val kickOffline by StringSetting("Kick Offline", "!kickoffline, !ko", desc = "Allows you to kick offline players.").withDependency { stringSettings }
    private val location by StringSetting("Location", "!location", desc = "Sends your current location.").withDependency { stringSettings }
    private val holding by StringSetting("Holding", "!holding", desc = "Sends the item you are holding.").withDependency { stringSettings }
    private val disband by StringSetting("Disband", ".disband", desc = "Disbands the party.") .withDependency { stringSettings }

    private val dtReason = mutableListOf<Pair<String, String>>()

    private lateinit var commands: List<ChatCommand>

    fun registerChatCommandsOnInitializeClient() {
        commands = listOf(
            ChatCommand({ allinv }, channelsOf(ChatChannel.PARTY), true) { _, _, _ ->
                sendCommand("p settings allinvite")
            },
            ChatCommand({ inv }, channelsOf(ChatChannel.PRIVATE),  false) { _, name, _ ->
                if (PartyUtils.isInParty) return@ChatCommand
                if (autoConfirm) return@ChatCommand sendCommand("p invite $name")
                modMessage(Component.literal("§aClick on this message to invite $name to your party!").withStyle {
                    it.withClickEvent(ClickEvent.RunCommand("/party invite $name"))
                        .withHoverEvent(HoverEvent.ShowText(Component.literal("§6Click to invite $name to your party.")))
                })
                playSoundAtPlayer(SoundEvents.NOTE_BLOCK_PLING.value())
            },
            ChatCommand({ warp }, channelsOf(ChatChannel.PARTY), true) { _, _, _ ->
                sendCommand("p warp")
            },
            ChatCommand({ transfer }, channelsOf(ChatChannel.PARTY), true) { words, name, _ ->
                val target = if (words.size > 1 && words[1].length <= 16) findPartyMember(words[1]) else name
                sendCommand("p transfer $target")
            },
            ChatCommand({ disband }, channelsOf(ChatChannel.PARTY), true) { _, _, _ ->
                sendCommand("p disband")
            },
            ChatCommand({ kick }, channelsOf(ChatChannel.PARTY), true) { words, name, _ ->
                if (words.size > 1 && words[1].length <= 16) sendCommand("p kick ${findPartyMember(words[1])}")
                else sendCommand("p kick $name")
            },
            ChatCommand({ reinv }, channelsOf(ChatChannel.PARTY), true) { _, name, _ ->
                modMessage("§aReinviting §6$name §ain 5 seconds...")
                schedule(100) { sendCommand("p invite $name") }
            },
            ChatCommand({ tyfr }, channelsOf(ChatChannel.PARTY), false) { _, name, _ ->
                if (name == mc.player?.name?.string) {
                    if (tyfrWarning) modMessage("§c⚠ §eTYFR found, leaving party in §b$tyfrDelay §eticks. §c⚠")
                    schedule(tyfrDelay) {
                        sendCommand("p leave")
                    }
                }
            },
            ChatCommand({ coords }, channelsOf(ChatChannel.PARTY, ChatChannel.PRIVATE), false) { _, name, channel ->
                channelMessage(getPositionString(), name, channel)
            },
            ChatCommand(
                { boop }, channelsOf(ChatChannel.PARTY, ChatChannel.PRIVATE,
                ChatChannel.GUILD), false) { words, name, _ ->
                if (words.size > 1 && words[1].length <= 16) sendCommand("boop ${words[1]}")
                else sendCommand("boop $name")
            },
            ChatCommand(
                { cf }, channelsOf(ChatChannel.PARTY, ChatChannel.PRIVATE,
                ChatChannel.GUILD), false) { _, name, channel ->
                channelMessage(if (Math.random() < 0.5) "Heads" else "Tails", name, channel)
            },
            ChatCommand(
                { ping }, channelsOf(ChatChannel.PARTY, ChatChannel.PRIVATE,
                ChatChannel.GUILD), false) { _, name, channel ->
                channelMessage("Ping: ${ServerUtils.currentPing}ms", name, channel)
            },
            ChatCommand(
                { tps }, channelsOf(ChatChannel.PARTY, ChatChannel.PRIVATE,
                ChatChannel.GUILD), false) { _, name, channel ->
                channelMessage("TPS: ${ServerUtils.averageTps.toFixed(1)}", name, channel)
            },
            ChatCommand(
                { fps }, channelsOf(ChatChannel.PARTY, ChatChannel.PRIVATE,
                ChatChannel.GUILD), false) { _, name, channel ->
                channelMessage("FPS: ${mc.fps}", name, channel)
            },
            ChatCommand({ dt }, channelsOf(ChatChannel.PARTY), false) { words, name, _ ->
                val reason = words.drop(1).joinToString(" ").takeIf { it.isNotBlank() } ?: "No reason given"
                if (dtReason.any { it.first == name }) return@ChatCommand modMessage("§6${name} §calready has a reminder!")
                modMessage("§aReminder set for the end of the run! §7(disabled auto requeue for this run)")
                dtReason.add(name to reason)
                DungeonQueue.disableRequeue = true
            },
            ChatCommand({ undt }, channelsOf(ChatChannel.PARTY), false) { _, name, _ ->
                if (dtReason.none { it.first == name }) return@ChatCommand modMessage("§6${name} §chas no reminder set!")
                modMessage("§aReminder removed!")
                dtReason.removeIf { it.first == name }
                if (dtReason.isEmpty()) DungeonQueue.disableRequeue = false
            },
            ChatCommand(
                { time }, channelsOf(ChatChannel.PARTY, ChatChannel.PRIVATE,
                ChatChannel.GUILD), false) { _, name, channel ->
                channelMessage("Current Time: ${ZonedDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a (z)", Locale.ENGLISH))}", name, channel)
            },
            ChatCommand({ demote }, channelsOf(ChatChannel.PARTY), true) { words, name, _ ->
                if (words.size > 1 && words[1].length <= 16) sendCommand("p demote ${findPartyMember(words[1])}")
                else sendCommand("p demote $name")
            },
            ChatCommand({ promote }, channelsOf(ChatChannel.PARTY), true) { words, name, _ ->
                if (words.size > 1 && words[1].length <= 16) sendCommand("p promote ${findPartyMember(words[1])}")
                else sendCommand("p promote $name")
            },
            ChatCommand({ kickOffline }, channelsOf(ChatChannel.PARTY), true) { _, _, _ ->
                sendCommand("p kickoffline")
            },
            ChatCommand(
                { location }, channelsOf(ChatChannel.PARTY, ChatChannel.PRIVATE,
                ChatChannel.GUILD), false) { _, name, channel ->
                channelMessage("Current Location: ${LocationUtils.currentArea.displayName}", name, channel)
            },
            ChatCommand(
                { holding }, channelsOf(ChatChannel.PARTY, ChatChannel.PRIVATE,
                ChatChannel.GUILD), false) { _, name, channel ->
                channelMessage("Holding: ${mc.player?.mainHandItem?.hoverName?.string?.noControlCodes ?: "Nothing :("}", name, channel)
            }
        )
    }

    init {
        on<ChatPacketEvent> {
            if (value.matches(RegexUtils.endOfDungeonRegex) || value.matches(RegexUtils.endOfKuudraRegex)) {
                if (dt.isEmpty() || dtReason.isEmpty()) return@on
                schedule(30) {
                    dtReason.find { it.first == mc.player?.name?.string }?.let { sendCommand("pc Downtime needed: ${it.second}") }
                    modMessage("DT Reasons: ${dtReason.groupBy({ it.second }, { it.first }).entries.joinToString(", ") { (reason, names) -> "${names.joinToString(", ")}: $reason" }}")
                    alert("§cPlayers need DT")
                    dtReason.clear()
                }
            }

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
                    if (it.isMvpPlusPlusEmote && chatEmotesMvpPlusPlus) return@on
                    replaced = true
                    words[i] = it.replacement
                }
            }

            if (!replaced) return@on

            cancel()
            sendChatMessage(words.joinToString(" "))
        }
    }

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
                if (hi && channel == ChatChannel.PARTY && name != mc.player?.name?.string) channelMessage(
                    "bye",
                    name,
                    channel
                )

            "!8ball" ->
                if (eightball) channelMessage(responses.random(), name, channel)

            "!dice" ->
                if (dice) channelMessage((1..6).random(), name, channel)

            "!kickrandom" ->
                if (kickRandom && channel == ChatChannel.PARTY && PartyUtils.isLeader()) sendCommand(
                    "p kick ${
                        PartyUtils.members.filterNot { it == mc.player?.name?.string }.random()
                    }"
                )

            "!rng" -> {
                if (!rng) return
                val numFirst = if (words.size > 1) words[1].replace(",", "").toLongOrNull() else null
                val numSecond = if (words.size > 2) words[2].replace(",", "").toLongOrNull() else 1
                if (numFirst != null && numSecond == null) channelMessage(
                    "Rolled ${"%,d".format((1..numFirst).random())} from range 1 to ${
                        "%,d".format(
                            numFirst
                        )
                    }.", name, channel
                )
                else if (numFirst != null && numSecond != null) {
                    val min = numFirst.coerceAtMost(numSecond)
                    val max = numFirst.coerceAtLeast(numSecond)
                    channelMessage(
                        "Rolled ${"%,d".format((min..max).random())} from range ${"%,d".format(min)} to ${
                            "%,d".format(
                                max
                            )
                        }.", name, channel
                    )
                } else channelMessage("Could not parse a number.", name, channel)
            }

            "f1", "f2", "f3", "f4", "f5", "f6", "f7", "m1", "m2", "m3", "m4", "m5", "m6", "m7", "t1", "t2", "t3", "t4", "t5" -> {
                if (!qInstance || channel != ChatChannel.PARTY || !PartyUtils.isLeader()) return
                modMessage("§8Entering -> §e${words[0].capitalizeFirst()}")
                sendCommand("odin ${words[0].lowercase()}")
            }
        }


        for (command in commands) {
            if (command.requiresLeader && !PartyUtils.isLeader()) continue
            if (channel in command.channels && commandMatches(command.keywords, words[0])) {
                command.action(words, name, channel)
                return
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

    data class ChatEmote(
        val replacement: String,
        val isMvpPlusPlusEmote: Boolean
    )

    val replacements = mapOf(
        "<3" to ChatEmote("❤", true),
        "o/" to ChatEmote("( ﾟ◡ﾟ)/", true),
        ":star:" to ChatEmote("✮", true),
        ":yes:" to ChatEmote("✔", true),
        ":no:" to ChatEmote("✖", true),
        ":java:" to ChatEmote("☕", true),
        ":arrow:" to ChatEmote("➜", true),
        ":shrug:" to ChatEmote("¯\\_(\u30c4)_/¯", true),
        ":tableflip:" to ChatEmote("(╯°□°）╯︵ ┻━┻", true),
        ":totem:" to ChatEmote("☉_☉", true),
        ":typing:" to ChatEmote("✎...", true),
        ":maths:" to ChatEmote("√(π+x)=L", true),
        ":snail:" to ChatEmote("@'-'", true),
        "ez" to ChatEmote("ｅｚ", false),
        ":thinking:" to ChatEmote("(0.o?)", true),
        ":gimme:" to ChatEmote("༼つ◕_◕༽つ", true),
        ":wizard:" to ChatEmote("('-')⊃━☆ﾟ.*･｡ﾟ", true),
        ":pvp:" to ChatEmote("⚔", true),
        ":peace:" to ChatEmote("✌", true),
        ":puffer:" to ChatEmote("<('O')>", true),
        "h/" to ChatEmote("ヽ(^◇^*)/", true),
        ":sloth:" to ChatEmote("(・⊝・)", true),
        ":dog:" to ChatEmote("(ᵔᴥᵔ)", true),
        ":dj:" to ChatEmote("ヽ(⌐■_■)ノ♬", true),
        ":yey:" to ChatEmote("ヽ (◕◡◕) ﾉ", true),
        ":snow:" to ChatEmote("☃", true),
        ":dab:" to ChatEmote("<o/", true),
        ":cat:" to ChatEmote("= ＾● ⋏ ●＾ =", true),
        ":cute:" to ChatEmote("(✿◠‿◠)", true),
        ":skull:" to ChatEmote("☠", false),
        ":bum:" to ChatEmote("♿", false),

        ":panda:" to ChatEmote("70sbloodcamp completed a device! (7/7) (100.248s | 100.248s)", false),
        ":x:" to ChatEmote(":no:", false),
        ":wheelchair:" to ChatEmote("♿", false)
    )

    enum class ChatChannel {
        PARTY, GUILD, PRIVATE
    }

    data class ChatCommand(
        val keywords: () -> String,
        val channels: Set<ChatChannel>,
        val requiresLeader: Boolean,
        val action: (words: List<String>, name: String, channel: ChatChannel) -> Unit
    )

    fun commandMatches(keywords: () -> String, target: String) =
        keywords().isNotEmpty() && target in keywords().split(",").map { it.trim() }

    fun channelsOf(vararg c: ChatChannel) = c.toSet()
}
