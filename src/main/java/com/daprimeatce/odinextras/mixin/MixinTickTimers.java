package com.daprimeatce.odinextras.mixin;

import com.daprimeatce.odinextras.accessor.AccessorTickTimers;
import com.odtheking.odin.clickgui.settings.impl.*;
import com.odtheking.odin.features.impl.boss.TickTimers;
import com.odtheking.odin.features.Module;
import com.odtheking.odin.clickgui.settings.Setting;
import net.minecraft.client.Minecraft;
import kotlin.Pair;
import kotlin.jvm.functions.Function2;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.LinkedHashMap;

@Mixin(TickTimers.class)
@SuppressWarnings("unused")
abstract class MixinTickTimers implements AccessorTickTimers {
    @Shadow
    protected abstract boolean getDisplayInTicks();
    @Shadow
    protected abstract boolean getSymbolDisplay();
    @Shadow
    protected abstract boolean getShowPrefix();
    @Unique
    private static HUDSetting ProfessorHud;
    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$addExtraTickTimers(CallbackInfo ci) {
        ProfessorHud = new HUDSetting(
            "Professor Hud",
            10,
            10,
            1f,
            true,
            "Displays a time for when to use the fire freeze staff for the Professor boss in M3.",
            ((Module)(Object) this),
            (Function2<GuiGraphicsExtractor, Boolean, Pair<Integer, Integer>>)
                (graphics, example) -> {
                    // logic whatever idk
                    graphics.text(Minecraft.getInstance().font, formatTimer(104, 104, "§3Fire freeze in: "), 0, 0, 0xFFFFFFFF);
                    return new Pair<>(90, 10);
                }

        );

        var professorTriggered = false;
        var professorTickTime = 104;
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void odinextras$reorderLeapMenu(CallbackInfo ci) {
        LinkedHashMap<String, Setting<?>> settings = TickTimers.INSTANCE.getSettings();

        LinkedHashMap<String, Setting<?>> reordered = new LinkedHashMap<>(settings);
        reordered.put("Professor Hud", ProfessorHud);

        settings.clear();
        settings.putAll(reordered);
    }

    @Unique
    private String formatTimer(int time, int max, String prefix) {
        String color;
        if (time >= max * 0.66) color = "§a";
        else if (time >= max * 0.33) color = "§6";
        else color = "§a";

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
