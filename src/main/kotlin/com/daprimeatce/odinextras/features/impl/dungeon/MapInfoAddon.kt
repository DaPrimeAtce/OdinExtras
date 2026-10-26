package com.daprimeatce.odinextras.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.RenderableSetting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.Setting
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.ScoreUpdateEvent
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.impl.dungeon.MapInfo
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils


object MapInfoAddon {
    val score300 = BooleanSetting("300 Score", false, "Customize what happens when reaching 300 score.")
    val score300CustomTitle = StringSetting(
        "300 Score Title",
        "&a300 Score",
        64,
        "What to render when reaching 300 score. Use \"&\" for color codes. Leave blank to disable.",
        "&a300 Score"
    ).withDependency { score300.value }
    val score300Message = StringSetting(
        "300 Score Message",
        "300 Score",
        64,
        "What to send in party chat when reaching 300 score. Leave blank to disable.",
        "300 Score"
    ).withDependency { score300.value }

    val score270 = BooleanSetting("270 Score", false, "Customize what happens when reaching 270 score.")
    val score270CustomTitle = StringSetting(
        "270 Score Title",
        "&e270 Score",
        64,
        "What to render when reaching 270 score. Use \"&\" for color codes. Leave blank to disable.",
        "&e270 Score"
    ).withDependency { score270.value }
    val score270Message = StringSetting(
        "270 Score Message",
        "270 Score",
        64,
        "What to send in party chat when reaching 270 score. Leave blank to disable.",
        "270 Score"
    ).withDependency { score270.value }

    var reached270 = false
    var reached300 = false

    fun register() {
        val settings = MapInfo.settings

        (settings["300 Score Alert"] as? BooleanSetting)?.value = false
        settings.remove("300 Score Alert")

        val reordered = LinkedHashMap<String, Setting<*>>()

        reordered["300 Score"] = score300
        reordered["300 Score Title"] = score300CustomTitle
        reordered["300 Score Message"] = score300Message
        reordered["270 Score"] = score270
        reordered["270 Score Title"] = score270CustomTitle
        reordered["270 Score Message"] = score270Message

        reordered.putAll(settings)

        settings.clear()
        settings.putAll(reordered)
    }


    init {
        on<LevelEvent.Load> {
            reached270 = false
            reached300 = false
        }

        on<ScoreUpdateEvent> {
            if (score270.enabled && !reached270 && DungeonUtils.inDungeons && score >= 270) {
                reached270 = true

                if (score270CustomTitle.value.isNotEmpty()) alert(score270CustomTitle.value.replace("&", "§"))
                if (score270Message.value.isNotEmpty()) sendCommand("pc ${score270Message.value}")
                modMessage("§b${DungeonUtils.score} §ascore reached in §6${DungeonUtils.dungeonTime} §8|| §e${DungeonUtils.floor?.name}§8.")
            }

            else if (score300.enabled && !reached300 && DungeonUtils.inDungeons && score >= 300) {
                reached300 = true

                if (score300CustomTitle.value.isNotEmpty()) alert(score300CustomTitle.value.replace("&", "§"))
                if (score300Message.value.isNotEmpty()) sendCommand("pc ${score300Message.value}")
                modMessage("§b${DungeonUtils.score} §ascore reached in §6${DungeonUtils.dungeonTime} §8|| §e${DungeonUtils.floor?.name}§8.")
            }
        }

        EventBus.subscribe(this)
    }


}