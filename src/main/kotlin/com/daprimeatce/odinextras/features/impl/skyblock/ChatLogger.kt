package com.daprimeatce.odinextras.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
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
    private val webhook by StringSetting("Webhook URL", "", 200 ,desc = "Webhook to log messages through.")

    private val messageRegex = Regex("^(?:Party > (\\[[^]]*?])? ?(\\w{1,16})(?: [ቾ⚒])?: ?(.+)$|Guild > (\\[[^]]*?])? ?(\\w{1,16})(?: \\[([^]]*?)])?: ?(.+)$|From (\\[[^]]*?])? ?(\\w{1,16}): ?(.+)$)")

    init {
        on<ChatPacketEvent> {
            if (webhook.isEmpty()) return@on

            val result = messageRegex.find(value) ?: return@on
            val channel = when(result.value.split(" ")[0]) {
                "From" -> Channel.PRIVATE
                "Party" -> Channel.PARTY
                "Guild" -> Channel.GUILD
                else -> null
            }

            if (channel == null) return@on
            val ign = result.groups[2]?.value ?: result.groups[5]?.value ?: result.groups[9]?.value ?: return@on
            val msg = result.groups[3]?.value ?: result.groups[7]?.value ?: result.groups[10]?.value ?: return@on

            if (ign == "stash") return@on

            if (party && channel == Channel.PARTY) sendEmbed(ign, msg, channel)
            if (guild && channel == Channel.GUILD) sendEmbed(ign, msg, channel)
            if (private && channel == Channel.PRIVATE) sendEmbed(ign, msg, channel)
        }
    }

    enum class Channel {
        PRIVATE,
        PARTY,
        GUILD
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
                Channel.PRIVATE -> 16711935
                Channel.GUILD -> 32768
                Channel.PARTY -> 255
            })
            addProperty("description", message)
            addProperty("timestamp", Instant.now().toString())
        }

        val embeds = JsonArray().apply { add(embed) }

        val body = JsonObject().apply {
            addProperty("username", "OdinExtras")
            add("embeds", embeds)
        }

        val request = HttpRequest.newBuilder()
            .uri(URI.create(webhook))
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