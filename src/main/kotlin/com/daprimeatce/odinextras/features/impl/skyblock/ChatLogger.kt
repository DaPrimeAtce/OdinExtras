package com.daprimeatce.odinextras.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.daprimeatce.odinextras.utils.messageRegex
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.util.concurrent.CompletableFuture

object ChatLogger : Module(
    name = "Chat Logger",
    description = "Logs chat messages through a Discord webhook."
) {
    private val party by BooleanSetting("Party Messages", true, desc = "Log party messages.")
    private val guild by BooleanSetting("Guild Messages", true, desc = "Log guild messages.")
    private val private by BooleanSetting("Private Messages", true, desc = "Log private messages.")
    private val webhookUrl by StringSetting("Webhook URL", "", 200 ,desc = "Webhook to log messages through.")
    private val webhookName by StringSetting("Webhook Name", "OdinExtras", 32, desc = "The name of the webhook. (WARNING: Leaving this empty will cause messages to not send.)")

    init {
        on<ChatPacketEvent> {
            if (webhookUrl.isEmpty()) return@on

            val result = messageRegex.find(value) ?: return@on
            val channel = when(result.value.split(" ")[0]) {
                "Party" -> Channel.PARTY
                "Guild" -> Channel.GUILD
                "From" -> Channel.PRIVATE_FROM
                "To" -> Channel.PRIVATE_TO
                else -> null
            }

            if (channel == null) return@on
            val ign = result.groups[2]?.value ?: result.groups[5]?.value ?: result.groups[10]?.value ?: return@on
            val msg = result.groups[3]?.value ?: result.groups[7]?.value ?: result.groups[11]?.value ?: return@on

            if (ign == "stash") return@on

            val ignSelf = mc.player?.name?.string ?: "user"

            if (party && channel == Channel.PARTY) sendEmbed(ign, msg, channel)
            if (guild && channel == Channel.GUILD) sendEmbed(ign, msg, channel)
            if (private && channel == Channel.PRIVATE_FROM) sendEmbed(ign, msg, channel)
            if (private && channel == Channel.PRIVATE_TO) sendEmbed(ignSelf, ("To $ign: $msg"), channel)
        }
    }

    enum class Channel {
        PARTY,
        GUILD,
        PRIVATE_FROM,
        PRIVATE_TO
    }

    private val client: HttpClient = HttpClient.newHttpClient()

    private fun sendEmbed(player: String, message: String, channel: Channel) {
        val playerObj = JsonObject().apply {
            addProperty("name", player)
            addProperty("icon_url", "https://www.mc-heads.net/avatar/$player")
        }

        val embed = JsonObject().apply {
            add("author", playerObj)
            addProperty("color", when (channel) {
                Channel.PRIVATE_FROM -> 16711935
                Channel.PRIVATE_TO -> 16711935
                Channel.GUILD -> 32768
                Channel.PARTY -> 255
            })
            addProperty("description", message)
            addProperty("timestamp", Instant.now().toString())
        }

        val embeds = JsonArray().apply { add(embed) }

        val body = JsonObject().apply {
            addProperty("username", webhookName)
            add("embeds", embeds)
        }

        val request = HttpRequest.newBuilder()
            .uri(URI.create(webhookUrl))
            .header("Content-Type", "application/json")
            .header("User-Agent", "Mozilla/5.0")
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .build()

        CompletableFuture.runAsync {
            try {
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                if (response.statusCode() >= 300) {
                    println("Chat Logger failed to send message: ${response.statusCode()} ${response.body()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}