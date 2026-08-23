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
import com.odtheking.odin.utils.skyblock.Split;
import com.odtheking.odin.utils.skyblock.SplitsManager;
import net.minecraft.client.Minecraft;

import kotlin.Pair;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

import static com.odtheking.odin.utils.ChatUtilsKt.modMessage;
import static com.odtheking.odin.utils.ChatUtilsKt.sendCommand;
import static com.odtheking.odin.utils.Utils.formatTime;
import static com.odtheking.odin.utils.handlers.TickTasksKt.schedule;
import static com.odtheking.odin.utils.render.DrawContextUtilsKt.getStringWidth;

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
    private static SelectorSetting sendTimeLost;
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
    private void odinextras$addExtraSplitsTimers(CallbackInfo ci) {
        Module module = ((Module)(Object) this);

        totalRunTime = new HUDSetting(
                "Total Run Time",
                10,
                10,
                2f,
                true,
                "Shows a split timer for the full run time.",
                module,
                (graphics, example) -> {
                    int totalWidth = getStringWidth("Split 0: 0h 00m 00s" + (getShowTickTime() ? " (0h 00m 00s)" : "") + 2);
                    String exampleTime = "0h 00m 00.00s" + (getShowTickTime() ? " §8(§70s§8)" : "");

                    if (example) {
                        if (getFixedWidth()) {
                            graphics.text(Minecraft.getInstance().font, ("§a§lTotal"), 0, 0, 0xFFFFFFFF);
                            graphics.text(Minecraft.getInstance().font, exampleTime, totalWidth - getStringWidth("0h 00m 00.00s" + (getShowTickTime() ? " (0s)" : "")), 0, 0xFFFFFFFF);
                        } else {
                            graphics.text(Minecraft.getInstance().font, ("§a§lTotal §r" + exampleTime), 0, 0, 0xFFFFFFFF);
                        }
                        return new Pair<>(totalWidth - 4, Minecraft.getInstance().font.lineHeight);
                    }

                    int maxWidth = getMaxSplitsWidth();

                    if (startTimeMs == -1) return new Pair<>(0, 0);

                    String totalTime = formatTime(((endTimeMs > 0) ? endTimeMs : System.currentTimeMillis()) - startTimeMs, 2);
                    String displayText = getShowTickTime() ? totalTime + " §8(§7" + String.format("%.2f", serverTicks / 20f) + "§8)" : totalTime;
                    graphics.text(Minecraft.getInstance().font, ("§a§lTotal"), 0, 0, 0xFFFFFFFF);

                    if (getFixedWidth()) graphics.text(Minecraft.getInstance().font, (displayText), totalWidth - getStringWidth(displayText), 0, 0xFFFFFFFF);
                    else graphics.text(Minecraft.getInstance().font, displayText, maxWidth + 4, 0, 0xFFFFFFFF);

                    return new Pair<>(totalWidth, Minecraft.getInstance().font.lineHeight);
                }
        );

        timeLostToLag = new HUDSetting(
                "Time Lost To Lag",
                10,
                10,
                2f,
                true,
                "Shows a split timer for how much run time is lost to lag.",
                module,
                (graphics, example) -> {
                    int totalWidth = getStringWidth("Split 0: 0h 00m 00s" + (getShowTickTime() ? " (0h 00m 00s)" : "")) + 2;
                    String exampleTimeLost = "00m 00.00s";

                    if (example) {
                        if (getFixedWidth()) {
                            graphics.text(Minecraft.getInstance().font, ("§c§lLost"), 0, 0, 0xFFFFFFFF);
                            graphics.text(Minecraft.getInstance().font, exampleTimeLost, totalWidth - getStringWidth("00m 00.00s"), 0, 0xFFFFFFFF);
                        } else {
                            graphics.text(Minecraft.getInstance().font, ("§c§lLost §r" + exampleTimeLost), 0, 0, 0xFFFFFFFF);
                        }
                        return new Pair<>(totalWidth, Minecraft.getInstance().font.lineHeight);
                    }

                    int maxWidth = getMaxSplitsWidth();

                    if (startTimeMs == -1) return new Pair<>(0, 0);

                    graphics.text(Minecraft.getInstance().font, "§c§lLost", 0, 0, 0xFFFFFFFF);

                    if (getFixedWidth()) graphics.text(Minecraft.getInstance().font, (timeLost), totalWidth - getStringWidth(timeLost), 0, 0xFFFFFFFF);
                    else graphics.text(Minecraft.getInstance().font, (timeLost), maxWidth + 4, 0, 0xFFFFFFFF);

                    return new Pair<>(totalWidth, Minecraft.getInstance().font.lineHeight);
                }
        );

        sendTimeLost = new SelectorSetting(
                "Send Time Lost",
                "Local",
                List.of("None", "Local", "Party"),
                "Sends to the chat the run time lost to lag."
        );
        Setting.Companion.withDependency(sendTimeLost, () -> timeLostToLag.isEnabled());


        stormDps = new HUDSetting(
                "Storm DPS",
                10,
                10,
                2f,
                true,
                "Show the split timer for Storm purple pillar DPS.",
                module,
                (graphics, example) -> {
                    int totalWidth = getStringWidth("Split 0: 0h 00m 00s" + (getShowTickTime() ? " (0h 00m 00s)" : "")) + 2;
                    String exampleTime = "0h 00m 00.00s" + (getShowTickTime() ? " §8(§70s§8)" : "");

                    if (example) {
                        if (getFixedWidth()) {
                            graphics.text(Minecraft.getInstance().font, ("§3Storm DPS"), 0, 0, 0xFFFFFFFF);
                            graphics.text(Minecraft.getInstance().font, exampleTime, totalWidth - getStringWidth("0h 00m 00.00s" + (getShowTickTime() ? " (0s)" : "")), 0, 0xFFFFFFFF);
                        } else {
                            graphics.text(Minecraft.getInstance().font, ("§3Storm DPS §r" + exampleTime), 0, 0, 0xFFFFFFFF);
                        }

                        return new Pair<>(totalWidth, Minecraft.getInstance().font.lineHeight);
                    }

                    int maxWidth = getMaxSplitsWidth();

                    if (startTimeStormMs == -1) return new Pair<>(0, 0);

                    String totalTime = formatTime(((endTimeStormMs > 0) ? endTimeStormMs : System.currentTimeMillis()) - startTimeStormMs, 2);
                    String displayText = getShowTickTime() ? totalTime + " §8(§7" + String.format("%.2f", serverTicksStorm / 20f) + "§8)" : totalTime;
                    graphics.text(Minecraft.getInstance().font, ("§3Storm DPS"), 0, 0, 0xFFFFFFFF);

                    if (getFixedWidth()) graphics.text(Minecraft.getInstance().font, displayText, totalWidth - getStringWidth(displayText), 0, 0xFFFFFFFF);
                    else graphics.text(Minecraft.getInstance().font, displayText, maxWidth + 4, 0, 0xFFFFFFFF);

                    return new Pair<>(totalWidth, Minecraft.getInstance().font.lineHeight);
                }
        );
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void odinextras$reorderSplits(CallbackInfo ci) {
        LinkedHashMap<String, Setting<?>> settings = Splits.INSTANCE.getSettings();

        LinkedHashMap<String, Setting<?>> reordered = new LinkedHashMap<>(settings);
        reordered.put("Total Run Time", totalRunTime);
        reordered.put("Time Lost To Lag", timeLostToLag);
        reordered.put("Send Time Lost", sendTimeLost);
        reordered.put("Storm DPS", stormDps);
        reordered.putAll(settings);

        settings.clear();
        settings.putAll(reordered);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private static void odinextras$splitsAddOnEvents(CallbackInfo ci) {
        EventBus.INSTANCE.registerListener(
                Splits.class,
                TickEvent.Server.class,
                0,
                false,
                event -> {
                    if (startTicking) {
                        serverTicks++;

                        timeLost = formatTime((System.currentTimeMillis() - startTimeMs) - (serverTicks * 50L), 2);
                    }
                    if (startTickingStorm) {
                        serverTicksStorm++;
                    }
                    return null;
                }
        );

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

        EventBus.INSTANCE.registerListener(
                Splits.class,
                ChatPacketEvent.class,
                0,
                false,
                event -> {
                    if (startOfDungeonRegex.matcher(event.getValue()).matches() || startOfKuudraRegex.matcher(event.getValue()).matches()) {
                        startTimeMs = System.currentTimeMillis();
                        startTicking = true;
                        return null;
                    }

                    if (!sentTime && (endOfDungeonRegex.matcher(event.getValue()).matches() || endOfKuudraRegex.matcher(event.getValue()).matches())) {
                        endTimeMs = System.currentTimeMillis();
                        startTicking = false;
                        sentTime = true;
                        schedule(5, true, () -> {
                            if (sendTimeLost.getValue() == 1) modMessage(timeLost + " lost to lag.", "§3Odin§aExtras §8»§r ", null);
                            else if (sendTimeLost.getValue() == 2) sendCommand("pc " + timeLost + " lost to lag");

                            if (stormStartRegex.matcher(event.getValue()).matches()) {
                                startTimeStormMs = System.currentTimeMillis();
                                startTickingStorm = true;
                                return null;
                            }

                            if (stormEnrageRegex.matcher(event.getValue()).matches()) {
                                endTimeStormMs = System.currentTimeMillis();
                                startTickingStorm = false;
                            }
                            return null;
                        });
                    }
                    return null;
                }
        );
    }

    @Unique
    private static int getMaxSplitsWidth() {
        List<Split> splits = new ArrayList<>(SplitsManager.INSTANCE.getCurrentSplits().getSplits());
        if (!splits.isEmpty()) splits.removeLast();
        else return 50;

        return splits.stream().mapToInt(split -> getStringWidth(split.getName())).max().orElse(50);
    }
}