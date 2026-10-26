package com.daprimeatce.odinextras.features.impl.skyblock

import com.daprimeatce.odinextras.utils.RegexUtils
import com.odtheking.odin.clickgui.settings.RenderableSetting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.Setting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.MessageEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.impl.skyblock.Splits
import com.odtheking.odin.utils.formatTime
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.SplitsManager
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor

object SplitsAddon {
    var startTimeMs: Long = -1
    var endTimeMs: Long = -1
    var serverTicks: Int = -1
    var startTicking = false

    var timeLost = "" // updates text per tick instead of per frame so it doesn't jitter as much
    var sentTime = false

    var startTimeStormMs: Long = -1
    var endTimeStormMs: Long = -1
    var serverTicksStorm: Long = -1
    var startTickingStorm = false

    private val fixedWidth: Boolean
        get() = Splits.settings["Fixed Width"]?.value as? Boolean ?: false

    enum class TimeLostTypes { NONE, LOCAL, PARTY }

    val totalRunTime = Splits.HUD("Total Run Time", "Shows a split timer for the full run time.") { example -> drawTotalRunTime(example) }
    val timeLostToLag = Splits.HUD("Time Lost To Lag", "Shows a split timer for how much run time is lost to lag.") { example -> drawTimeLostToLag(example) }
    val sendTimeLost = SelectorSetting("Send Time Lost", TimeLostTypes.LOCAL, "Sends to the chat the run time lost to lag.", listOf(TimeLostTypes.NONE, TimeLostTypes.LOCAL, TimeLostTypes.PARTY)).withDependency { timeLostToLag.isEnabled }
    val stormDps = Splits.HUD("Storm DPS", "Show the split timer for Storm purple pillar DPS.") { example -> drawStormDps(example) }

    fun register() {
        val settings = Splits.settings
        val reordered = LinkedHashMap<String, Setting<*>>()

        reordered["Total Run Time"] = totalRunTime
        reordered["Time Lost To Lag"] = timeLostToLag
        reordered["Send Time Lost"] = sendTimeLost
        reordered["Storm DPS"] = stormDps
        reordered.putAll(settings)

        settings.clear()
        settings.putAll(reordered)

        if (totalRunTime !in ModuleManager.hudSettingsCache) ModuleManager.hudSettingsCache.add(totalRunTime)
        if (timeLostToLag !in ModuleManager.hudSettingsCache) ModuleManager.hudSettingsCache.add(timeLostToLag)
        if (stormDps !in ModuleManager.hudSettingsCache) ModuleManager.hudSettingsCache.add(stormDps)
    }


    private fun GuiGraphicsExtractor.drawTotalRunTime(example: Boolean): Pair<Int, Int> {
        val lineHeight = Minecraft.getInstance().font.lineHeight
        val tick = Splits.showTickTime
        val totalWidth = getStringWidth("Split 0: 0h 00m 00s" + (if (tick) " (0h 00m 00s)" else "")) + 2
        val exampleTime = "0h 00m 00.00s" + (if (tick) " §8(§70s§8)" else "")

        if (example) {
            if (fixedWidth) {
                text(Minecraft.getInstance().font, "§a§lTotal", 0, 0, 0xFFFFFFFF.toInt())
                text(Minecraft.getInstance().font, exampleTime, totalWidth - getStringWidth("0h 00m 00.00s" + (if (tick) " (0s)" else "")), 0, 0xFFFFFFFF.toInt())
            } else {
                text(Minecraft.getInstance().font,"§a§lTotal §r$exampleTime", 0, 0, 0xFFFFFFFF.toInt())
            }
            return (totalWidth - 4) to lineHeight
        }

        if (startTimeMs == -1L) return 0 to 0

        val maxWidth = getMaxSplitsWidth()
        val totalTime = formatTime(if (endTimeMs > 0) endTimeMs else System.currentTimeMillis() - startTimeMs, 2)
        val displayText = if (tick) totalTime + " §8(§7" + String.format("%.2f", serverTicks / 20f) + "§8)" else totalTime

        text(Minecraft.getInstance().font,"§a§lTotal", 0, 0, 0xFFFFFFFF.toInt())
        if (fixedWidth) text(Minecraft.getInstance().font,displayText, totalWidth - getStringWidth(displayText), 0, 0xFFFFFFFF.toInt())
        else text(Minecraft.getInstance().font,displayText, maxWidth + 4, 0, 0xFFFFFFFF.toInt())

        return totalWidth to lineHeight
    }

    private fun GuiGraphicsExtractor.drawTimeLostToLag(example: Boolean): Pair<Int, Int> {
        val lineHeight = Minecraft.getInstance().font.lineHeight
        val tick = Splits.showTickTime
        val totalWidth = getStringWidth("Split 0: 0h 00m 00s" + (if (tick) " (0h 00m 00s)" else "")) + 2
        val exampleTime = "00m 00.00s"

        if (example) {
            if (fixedWidth) {
                text(Minecraft.getInstance().font, ("§c§lLost"), 0, 0, 0xFFFFFFFF.toInt())
                text(Minecraft.getInstance().font, exampleTime, totalWidth - getStringWidth("00m 00.00s"), 0, 0xFFFFFFFF.toInt())
            } else {
                text(Minecraft.getInstance().font, ("§c§lLost §r$exampleTime"), 0, 0, 0xFFFFFFFF.toInt())
            }
            return totalWidth to lineHeight
        }

        val maxWidth = getMaxSplitsWidth()

        if (startTimeMs == -1L) return 0 to 0
        text(Minecraft.getInstance().font, "§c§lLost", 0, 0, 0xFFFFFFFF.toInt())

        if (fixedWidth) text(Minecraft.getInstance().font, timeLost, totalWidth - getStringWidth(timeLost), 0, 0xFFFFFFFF.toInt())
        else text(Minecraft.getInstance().font, timeLost, maxWidth + 4, 0, 0xFFFFFFFF.toInt())

        return totalWidth to lineHeight
    }

    private fun GuiGraphicsExtractor.drawStormDps(example: Boolean): Pair<Int, Int> {
        val lineHeight = Minecraft.getInstance().font.lineHeight
        val tick = Splits.showTickTime
        val totalWidth = getStringWidth("Split 0: 0h 00m 00s" + (if (tick) " (0h 00m 00s)" else "")) + 2
        val exampleTime = "0h 00m 00.00s" + (if (tick) " §8(§70s§8)" else "")

        if (example) {
            if (fixedWidth) {
                text(Minecraft.getInstance().font, ("§3Storm DPS"), 0, 0, 0xFFFFFFFF.toInt())
                text(Minecraft.getInstance().font, exampleTime, totalWidth - getStringWidth("0h 00m 00.00s" + (if (tick) " (0s)" else "")), 0, 0xFFFFFFFF.toInt())
            } else {
                text(Minecraft.getInstance().font, ("§3Storm DPS §r$exampleTime"), 0, 0, 0xFFFFFFFF.toInt())
            }

            return totalWidth to lineHeight
        }

        val maxWidth = getMaxSplitsWidth()

        if (startTimeStormMs == -1L) return 0 to 0

        val totalTime = formatTime((if (endTimeStormMs > 0) endTimeStormMs else System.currentTimeMillis()) - startTimeStormMs, 2)
        val displayText = if (tick)  totalTime + " §8(§7" + String.format("%.2f", serverTicksStorm / 20f) + "§8)" else totalTime
        text(Minecraft.getInstance().font, ("§3Storm DPS"), 0, 0, 0xFFFFFFFF.toInt())

        if (fixedWidth) text(Minecraft.getInstance().font, displayText, totalWidth - getStringWidth(displayText), 0, 0xFFFFFFFF.toInt())
        else text(Minecraft.getInstance().font, displayText, maxWidth + 4, 0, 0xFFFFFFFF.toInt())

        return totalWidth to lineHeight
    }


    init {
        on<LevelEvent.Load> {
            startTimeMs = -1
            endTimeMs = -1
            serverTicks = -1
            startTicking = false

            timeLost = ""
            sentTime = false

            startTimeStormMs = -1
            endTimeStormMs = -1
            serverTicksStorm = -1
            startTickingStorm = false
        }

        on<TickEvent.Server> {
            if (startTicking) {
                serverTicks++
                timeLost = formatTime((System.currentTimeMillis() - startTimeMs) - (serverTicks * 50), 2)
            }
        }

        on<MessageEvent.Chat> {
            if (RegexUtils.startOfDungeonRegex.matches(message) || RegexUtils.startOfKuudraRegex.matches(message)) {
                startTimeMs = System.currentTimeMillis()
                startTicking = true
            }

            if (!sentTime && (RegexUtils.endOfDungeonRegex.matches(message) || RegexUtils.endOfKuudraRegex.matches(message))) {
                endTimeMs = System.currentTimeMillis()
                startTicking = false
                sentTime = true
                schedule(10) {
                    if (sendTimeLost.value == TimeLostTypes.LOCAL) modMessage("$timeLost lost to lag.", "§3Odin§aExtras §8»§r ")
                    else if (sendTimeLost.value == TimeLostTypes.PARTY) sendCommand("pc $timeLost lost to lag")
                }
            }

            if (RegexUtils.stormStartRegex.matches(message)) {
                startTimeStormMs = System.currentTimeMillis()
                startTickingStorm = true
            }

            if (RegexUtils.stormEnrageRegex.matches(message)) {
                endTimeStormMs = System.currentTimeMillis()
                startTickingStorm = false
            }
        }

        EventBus.subscribe(this)
    }

    fun getMaxSplitsWidth(): Int = SplitsManager.currentRows().maxOfOrNull { getStringWidth(it.name) } ?: 50
}