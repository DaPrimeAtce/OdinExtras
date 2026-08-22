package com.daprimeatce.odinextras.mixin;

import com.daprimeatce.odinextras.utils.RegexUtils;
import com.odtheking.odin.clickgui.settings.impl.*;
import com.odtheking.odin.events.ChatPacketEvent;
import com.odtheking.odin.events.LevelEvent;
import com.odtheking.odin.events.TickEvent;
import com.odtheking.odin.events.core.EventBus;
import com.odtheking.odin.features.impl.boss.TickTimers;
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

@Mixin(TickTimers.class)
@SuppressWarnings("unused")
abstract class MixinTickTimers {
    @Shadow
    protected abstract boolean getDisplayInTicks();
    @Shadow
    protected abstract boolean getSymbolDisplay();
    @Shadow
    protected abstract boolean getShowPrefix();

    @Unique
    private static HUDSetting professorHud;
    @Unique
    private static HUDSetting stormMoveHud;
    @Unique
    private static HUDSetting stormEnrageHud;
    @Unique
    private static HUDSetting ragHud;

    @Unique
    private static boolean professorTriggered = false;
    @Unique
    private static boolean stormMoveTriggered = false;
    @Unique
    private static boolean stormEnrageTriggered = false;
    @Unique
    private static boolean ragTriggered = false;

    @Unique
    private static int professorTickTime = 104;
    @Unique
    private static int stormMoveTickTime = 138;
    @Unique
    private static int stormEnrageTickTime = 66;
    @Unique
    private static int ragTickTime = 100;

    @Unique
    private static final Pattern professorRegex = RegexUtils.INSTANCE.getProfessorRegex().toPattern();
    @Unique
    private static final Pattern stormMoveRegex = RegexUtils.INSTANCE.getStormMoveRegex().toPattern();
    @Unique
    private static final Pattern stormEnrageRegex = RegexUtils.INSTANCE.getStormEnrageRegex().toPattern();
    @Unique
    private static final Pattern ragRegex = RegexUtils.INSTANCE.getWitherKingStartRegex().toPattern();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$addExtraTickTimers(CallbackInfo ci) {

        Module module = ((Module)(Object)this);
        professorHud = new HUDSetting(
            "Professor Hud",
            10,
            10,
            1f,
            true,
            "Displays a time for when to use the fire freeze staff for the Professor boss in M3.",
            module,
            (graphics, example) -> {
                if (example) graphics.text(Minecraft.getInstance().font, formatTimer(104, 104, "§3Fire freeze in: "), 0, 0, 0xFFFFFFFF);
                else if (professorTickTime >= 0 && professorTriggered) graphics.text(Minecraft.getInstance().font, formatTimer(professorTickTime, 104, "§3Fire freeze in: "), 0, 0, 0xFFFFFFFF);
                return new Pair<>(105, 10);
            }
        );

        stormMoveHud = new HUDSetting(
            "Storm Move Hud",
            10,
            10,
            1f,
            true,
            "Displays a timer for when Storm will begin to move after lightning. (Recommended to be used only if your party is crushing purple pillar instead of green pillar.)",
            module,
            (graphics, example) -> {
                if (example) graphics.text(Minecraft.getInstance().font, formatTimer(138, 138, "§c§lStorm will move in: "), 0, 0, 0xFFFFFFFF);
                else if (stormMoveTickTime >= 0 && stormMoveTriggered) graphics.text(Minecraft.getInstance().font, formatTimer(stormMoveTickTime, 138, "§c§lStorm will move in: "), 0, 0, 0xFFFFFFFF);
                return new Pair<>(140, 10);
            }
        );

        stormEnrageHud = new HUDSetting(
            "Storm Enrage Hud",
            10,
            10,
            1f,
            true,
            "Displays a timer for when to jump under the Yellow pillar during storm for mage.",
            module,
            (graphics, example) -> {
                if (example) graphics.text(Minecraft.getInstance().font, formatTimer(66, 66, "§bJump: "), 0, 0, 0xFFFFFFFF);
                else if (stormEnrageTickTime >= 0 && stormEnrageTriggered) graphics.text(Minecraft.getInstance().font, formatTimer(stormEnrageTickTime, 66, "§bJump: "), 0, 0, 0xFFFFFFFF);
                return new Pair<>(60, 10);
            }
        );

        ragHud = new HUDSetting(
            "Dragon Rag Hud",
            10,
            10,
            1f,
            true,
            "Displays a timer for when to rag for the first dragon.",
            module,
            (graphics, example) -> {
                if (example) graphics.text(Minecraft.getInstance().font, formatTimer(100, 100, "§bRag in: "), 0, 0, 0xFFFFFFFF);
                else if (ragTickTime >= 0 && ragTriggered) graphics.text(Minecraft.getInstance().font, formatTimer(ragTickTime, 100, "§bRag in: "), 0, 0, 0xFFFFFFFF);
                return new Pair<>(65, 10);
            }
        );
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void odinextras$reorderTickTimers(CallbackInfo ci) {
        LinkedHashMap<String, Setting<?>> settings = TickTimers.INSTANCE.getSettings();

        LinkedHashMap<String, Setting<?>> reordered = new LinkedHashMap<>(settings);
        reordered.put("Professor Hud", professorHud);
        reordered.put("Storm Move Hud", stormMoveHud);
        reordered.put("Storm Enrage Hud", stormEnrageHud);
        reordered.put("Dragon Rag Hud", ragHud);

        settings.clear();
        settings.putAll(reordered);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private static void odinextras$tickTimersAddTickEventServer(CallbackInfo ci) {
        EventBus.INSTANCE.registerListener(
            TickTimers.class,
            TickEvent.Server.class,
            0,
            false,
            event -> {
                if (professorTriggered && professorHud.isEnabled()) professorTickTime--;
                if (stormMoveTriggered && stormMoveHud.isEnabled()) stormMoveTickTime--;
                if (stormEnrageTriggered && stormEnrageHud.isEnabled()) stormEnrageTickTime--;
                if (ragTriggered && ragHud.isEnabled()) ragTickTime--;
                return null;
            }
        );
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private static void odinextras$tickTimersAddTickLevelEventLoad(CallbackInfo ci) {
        EventBus.INSTANCE.registerListener(
            TickTimers.class,
            LevelEvent.Load.class,
            0,
            false,
            event -> {
                professorTickTime = 104;
                professorTriggered = false;
                stormMoveTickTime = 138;
                stormMoveTriggered = false;
                stormEnrageTickTime = 66;
                stormEnrageTriggered = false;
                ragTickTime = 100;
                ragTriggered = false;
                return null;
            }
        );
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private static void odinextras$tickTimersAddChatPacketEvent(CallbackInfo ci) {
        EventBus.INSTANCE.registerListener(
            TickTimers.class,
            ChatPacketEvent.class,
            0,
            false,
            event -> {
                if (professorHud.isEnabled() && !professorTriggered && professorRegex.matcher(event.getValue()).matches()) {
                    professorTriggered = true;
                }
                if (stormMoveHud.isEnabled() && !stormMoveTriggered && stormMoveRegex.matcher(event.getValue()).matches()) {
                    stormMoveTriggered = true;
                }
                if (stormEnrageHud.isEnabled() && !stormEnrageTriggered && stormEnrageRegex.matcher(event.getValue()).matches()) {
                    stormEnrageTriggered = true;
                }
                if (ragHud.isEnabled() && !ragTriggered && ragRegex.matcher(event.getValue()).matches()) {
                    ragTriggered = true;
                }
                return null;
            }
        );
    }

    @Unique
    private String formatTimer(int time, int max, String prefix) {
        String color;
        if (time >= max * 0.66) color = "§a";
        else if (time >= max * 0.33) color = "§6";
        else color = "§c";

        String timeDisplay;
        if (getDisplayInTicks()) timeDisplay = color + time;
        else timeDisplay = color + String.format("%.2f", time / 20f);

        if (getSymbolDisplay()) {
            if (getDisplayInTicks()) timeDisplay += "t";
            else timeDisplay += "s";
        }

        if (getShowPrefix()) {
            timeDisplay = prefix + timeDisplay;
        }

        return timeDisplay;
    }
}
