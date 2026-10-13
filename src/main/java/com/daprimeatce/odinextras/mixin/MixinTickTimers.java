package com.daprimeatce.odinextras.mixin;

import com.daprimeatce.odinextras.utils.RegexUtils;
import com.odtheking.odin.clickgui.settings.impl.*;
import com.odtheking.odin.events.MessageEvent;
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
    private static HUDSetting stormMoveHud;
    @Unique
    private static HUDSetting stormEnrageHud;

    @Unique
    private static boolean stormMoveTriggered = false;
    @Unique
    private static boolean stormEnrageTriggered = false;

    @Unique
    private static int stormMoveTickTime = 98;
    @Unique
    private static int stormEnrageTickTime = 66;

    @Unique
    private static final Pattern stormMoveRegex = RegexUtils.INSTANCE.getStormMoveRegex().toPattern();
    @Unique
    private static final Pattern stormEnrageRegex = RegexUtils.INSTANCE.getStormEnrageRegex().toPattern();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$addExtraTickTimers(CallbackInfo ci) {
        Module module = ((Module)(Object) this);

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
                else if (stormMoveTickTime >= 0 && stormMoveTriggered) graphics.text(Minecraft.getInstance().font, formatTimer(stormMoveTickTime, 98, "§c§lStorm will move in: "), 0, 0, 0xFFFFFFFF);
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

    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void odinextras$reorderTickTimers(CallbackInfo ci) {
        LinkedHashMap<String, Setting<?>> settings = TickTimers.INSTANCE.getSettings();

        LinkedHashMap<String, Setting<?>> reordered = new LinkedHashMap<>(settings);
        reordered.put("Storm Move Hud", stormMoveHud);
        reordered.put("Storm Enrage Hud", stormEnrageHud);

        settings.clear();
        settings.putAll(reordered);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private static void odinextras$tickTimersAddOnEvents(CallbackInfo ci) {
        EventBus.INSTANCE.registerListener(
            TickTimers.class,
            LevelEvent.Load.class,
            0,
            false,
            event -> {
                stormMoveTickTime = 98;
                stormMoveTriggered = false;
                stormEnrageTickTime = 66;
                stormEnrageTriggered = false;
                return null;
            }
        );

        EventBus.INSTANCE.registerListener(
                TickTimers.class,
                MessageEvent.Chat.class,
                0,
                false,
                event -> {
                    if (stormMoveHud.isEnabled() && !stormMoveTriggered && stormMoveRegex.matcher(event.getMessage()).matches()) {
                        stormMoveTriggered = true;
                    }
                    if (stormEnrageHud.isEnabled() && !stormEnrageTriggered && stormEnrageRegex.matcher(event.getMessage()).matches()) {
                        stormEnrageTriggered = true;
                    }
                    return null;
                }
        );

        EventBus.INSTANCE.registerListener(
                TickTimers.class,
                TickEvent.Server.class,
                0,
                false,
                event -> {
                    if (stormMoveTriggered && stormMoveHud.isEnabled()) stormMoveTickTime--;
                    if (stormEnrageTriggered && stormEnrageHud.isEnabled()) stormEnrageTickTime--;
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
