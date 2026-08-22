package com.daprimeatce.odinextras.mixin;

import com.daprimeatce.odinextras.utils.RegexUtils;
import com.odtheking.odin.clickgui.settings.impl.*;
import com.odtheking.odin.events.ChatPacketEvent;
import com.odtheking.odin.events.LevelEvent;
import com.odtheking.odin.events.TickEvent;
import com.odtheking.odin.events.core.EventBus;
import com.odtheking.odin.features.impl.skyblock.Splits;
import com.odtheking.odin.features.Module;
import com.odtheking.odin.clickgui.settings.Setting;
import net.minecraft.client.Minecraft;
import kotlin.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import static com.odtheking.odin.utils.ChatUtilsKt.modMessage;
import static com.odtheking.odin.utils.ChatUtilsKt.sendCommand;
import static com.odtheking.odin.utils.Utils.formatTime;

@Mixin(Splits.class)
@SuppressWarnings("unused")
abstract class MixinSplits {
    @Shadow
    protected abstract boolean getFixedWidth();
    @Shadow
    public abstract boolean getShowTickTime();

    @Unique
    private static HUDSetting totalRunTime;
    @Unique
    private static HUDSetting timeLostToLag;
    @Unique
    private static HUDSetting stormDps;

    @Unique
    private static long startTimeMs = -1;
    @Unique
    private static long endTimeMs = -1;
    @Unique
    private static long startTimeStormMs = -1;
    @Unique
    private static long endTimeStormMs = -1;
    @Unique
    private static int serverTicks = -1;
    @Unique
    private static int serverTicksStorm = -1;
    @Unique
    private static String timeLost = ""; // updates text per tick instead of per frame so it doesn't jitter as much
    @Unique
    private static boolean startTicking = false;
    @Unique
    private static boolean startTickingStorm = false;
    @Unique
    private static boolean sentTime = false;

    @Unique
    private static final Pattern startOfDungeonRegex = RegexUtils.INSTANCE.getStartOfDungeonRegex().toPattern();
    @Unique
    private static final Pattern startOfKuudraRegex = RegexUtils.INSTANCE.getStartOfKuudraRegex().toPattern();
    @Unique
    private static final Pattern endOfDungeonRegex = RegexUtils.INSTANCE.getEndOfDungeonRegex().toPattern();
    @Unique
    private static final Pattern endOfKuudraRegex = RegexUtils.INSTANCE.getEndOfKuudraRegex().toPattern();
    @Unique
    private static final Pattern stormStartRegex = RegexUtils.INSTANCE.getStormStartRegex().toPattern();
    @Unique
    private static final Pattern stormEnrageRegex = RegexUtils.INSTANCE.getStormEnrageRegex().toPattern();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$addExtraSplitTimers(CallbackInfo ci) {

        Module module = ((Module)(Object)this);
        totalRunTime = new HUDSetting(
                "Total Run Time",
                10,
                10,
                1f,
                true,
                "Shows a split timer for the full run time.",
                module,
                (graphics, example) -> {
                    var totalWidth = getStringWidth("Split 0: 0h 00m 00s" + if (getShowTickTime()) " (0h 00m 00s)" else "") + 2
                    var exampleTime = "0h 00m 00.00s" + if (getShowTickTime()) " §8(§70s§8)" else ""

                    if (example) {
                        if (getFixedWidth()) {
                            graphics.text(Minecraft.getInstance().font, ("§a§lTotal"), 0, 0, 0xFFFFFFFF);
                            graphics.text(Minecraft.getInstance().font, ((exampleTime), totalWidth - getStringWidth("0h 00m 00.00s" + if (getShowTickTime()) " (0s)" else ""), 0)
                        } else {
                            graphics.text(Minecraft.getInstance().font, ("§a§lTotal §r$exampleTime"), 0, 0, 0xFFFFFFFF);
                        }
                        return new Pair<>(105, 10); //totalWidth to mc.font.lineHeight
                    }

                    var maxWidth = currentSplits.splits.dropLast(1).maxOfOrNull { split -> getStringWidth(split.name) } ?: 50

                    if (startTimeMs.toInt() == -1) return new Pair<>(0, 0); //return@HUD 0 to 0

                    var totalTime = formatTime((if (endTimeMs > 0) endTimeMs else System.currentTimeMillis()) - startTimeMs, 2)
                    var displayText = if (getShowTickTime()) "$totalTime §8(§7${(serverTicks / 20f).toFixed(2)}§8)" else totalTime
                    graphics.text(Minecraft.getInstance().font, ("§a§lTotal"), 0, 0, 0xFFFFFFFF);

                    if (getFixedWidth()) graphics.text(Minecraft.getInstance().font, (displayText), totalWidth - getStringWidth(displayText), 0, 0xFFFFFFFF);
                    else graphics.text(Minecraft.getInstance().font, (displayText, maxWidth + 4, 0, 0xFFFFFFFF);

                    totalWidth to mc.font.lineHeight
                }
        );

        timeLostToLag = new HUDSetting(
                "Time Lost To Lag",
                10,
                10,
                1f,
                true,
                "Shows a split timer for how much run time is lost to lag.",
                module,
                (graphics, example) -> {
                    var totalWidth = getStringWidth("Split 0: 0h 00m 00s" + if (getShowTickTime()) " (0h 00m 00s)" else "") + 2
                    var exampleTimeLost = "00m 00.00s"

                    if (example) {
                        if (getFixedWidth()) {
                            graphics.text(Minecraft.getInstance().font, ("§c§lLost"), 0, 0, 0xFFFFFFFF);
                            graphics.text(Minecraft.getInstance().font, (exampleTimeLost), totalWidth - getStringWidth("00m 00.00s"), 0);
                        } else {
                            graphics.text(Minecraft.getInstance().font, ("§c§lLost §r$exampleTimeLost"), 0, 0, 0xFFFFFFFF);
                        }
                        return new Pair<>(105, 10); //totalWidth to mc.font.lineHeight
                    }

                    var maxWidth = currentSplits.splits.dropLast(1).maxOfOrNull { split -> getStringWidth(split.name) } ?: 50

                    if (startTimeMs.toInt() == -1) return new Pair<>(0, 0); //return@HUD 0 to 0

                    graphics.text(Minecraft.getInstance().font, ("§c§lLost", 0, 0,, 0xFFFFFFFF);

                    if (getFixedWidth()) graphics.text(Minecraft.getInstance().font, (timeLost), totalWidth - getStringWidth(timeLost), 0, 0xFFFFFFFF);
                    else graphics.text(Minecraft.getInstance().font, (timeLost), maxWidth + 4, 0, 0xFFFFFFFF);

                    totalWidth to mc.font.lineHeight
                }
        );

        stormDps = new HUDSetting(
                "Storm DPS",
                10,
                10,
                1f,
                true,
                "Show the split timer for Storm purple pillar DPS.",
                module,
                (graphics, example) -> {
                    var totalWidth = getStringWidth("Split 0: 0h 00m 00s" + if (getShowTickTime()) " (0h 00m 00s)" else "") + 2
                    var exampleTime = "0h 00m 00.00s" + if (getShowTickTime()) " §8(§70s§8)" else ""

                    if (it) {
                        if (getFixedWidth()) {
                            graphics.text(Minecraft.getInstance().font, ("§3Storm DPS"), 0, 0, 0xFFFFFFFF);
                            graphics.text(Minecraft.getInstance().font, (exampleTime, totalWidth - getStringWidth("0h 00m 00.00s" + if (getShowTickTime()) " (0s)" else ""), 0);
                        } else {
                            graphics.text(Minecraft.getInstance().font, ("§3Storm DPS §r$exampleTime"), 0, 0, 0xFFFFFFFF);
                        }

                        return new Pair<>(105, 10); //totalWidth to mc.font.lineHeight
                    }

                    var maxWidth = currentSplits.splits.dropLast(1).maxOfOrNull { split -> getStringWidth(split.name) } ?: 50

                    if (startTimeMs.toInt() == -1) return new Pair<>(0, 0); //return@HUD 0 to 0

                    var totalTime = formatTime((if (endTimeStormMs > 0) endTimeStormMs else System.currentTimeMillis()) - startTimeStormMs, 2);
                    var displayText = if (getShowTickTime()) "$totalTime §8(§7${(serverTicksStorm / 20f).toFixed(2)}§8)" else totalTime
                    graphics.text(Minecraft.getInstance().font, ("§3Storm DPS"), 0, 0, 0xFFFFFFFF);

                    if (getFixedWidth()) graphics.text(Minecraft.getInstance().font, (displayText, totalWidth - getStringWidth(displayText), 0, 0xFFFFFFFF);
                    else graphics.text(Minecraft.getInstance().font, (displayText, maxWidth + 4, 0, 0xFFFFFFFF);

                    totalWidth to mc.font.lineHeight
                }
        );
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void odinextras$reorderTickTimers(CallbackInfo ci) {
        LinkedHashMap<String, Setting<?>> settings = Splits.INSTANCE.getSettings();

        LinkedHashMap<String, Setting<?>> reordered = new LinkedHashMap<>(settings);
        reordered.put("Total Run Time", totalRunTime);
        reordered.put("Time Lost To Lag", timeLostToLag);
        reordered.put("Storm DPS", stormDps);
        reordered.putAll(settings);

        settings.clear();
        settings.putAll(reordered);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private static void odinextras$tickTimersAddTickEventServer(CallbackInfo ci) {
        EventBus.INSTANCE.registerListener(
                Splits.class,
                TickEvent.Server.class,
                0,
                false,
                event -> {
                    if (startTicking) {
                        serverTicks++;

                        timeLost = formatTime((System.currentTimeMillis() - startTimeMs) - (serverTicks * 50))
                    }
                    if (startTickingStorm) {
                        serverTicksStorm++;
                    }
                    return null;
                }
        );
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private static void odinextras$tickTimersAddTickLevelEventLoad(CallbackInfo ci) {
        EventBus.INSTANCE.registerListener(
                Splits.class,
                LevelEvent.Load.class,
                0,
                false,
                event -> {
                    startTimeMs = -1;
                    endTimeMs = -1;
                    startTimeStormMs = -1;
                    endTimeStormMs = -1;
                    serverTicks = -1;
                    serverTicksStorm = -1;
                    timeLost = "";
                    startTicking = false;
                    startTickingStorm = false;
                    sentTime = false;
                    return null;
                }
        );
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private static void odinextras$tickTimersAddChatPacketEvent(CallbackInfo ci) {
        EventBus.INSTANCE.registerListener(
                Splits.class,
                ChatPacketEvent.class,
                0,
                false,
                event -> {
                    if (startOfDungeonRegex.matcher(event.getValue()).matches() || startOfKuudraRegex.matcher(event.getValue()).matches()) {
                        startTimeMs = System.currentTimeMillis();
                        startTicking = true;
                        return;
                    }

                    if (!sentTime && (endOfDungeonRegex.matcher(event.getValue()).matches() || endOfKuudraRegex.matcher(event.getValue()).matches())) {
                        endTimeMs = System.currentTimeMillis();
                        startTicking = false;
                        sentTime = true;

                        schedule(5, true) {
                            if (sendTimeLost == 1) modMessage("$timeLost lost to lag.");
                            else if (sendTimeLost == 2) sendCommand("pc $timeLost lost to lag");
                            else if (sendTimeLost == 3) {
                                modMessage("$timeLost lost to lag.");
                                sendCommand("pc $timeLost lost to lag");
                            }
                        }
                    }

                    if (stormStartRegex.matcher(event.getValue()).matches()) {
                        startTimeStormMs = System.currentTimeMillis();
                        startTickingStorm = true;
                        return;
                    }

                    if (stormEnrageRegex.matcher(event.getValue()).matches()) {
                        endTimeStormMs = System.currentTimeMillis();
                        startTickingStorm = false;
                    }
                    return null;
                }
        );
    }
}
