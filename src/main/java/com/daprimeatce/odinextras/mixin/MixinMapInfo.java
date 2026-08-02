package com.daprimeatce.odinextras.mixin;

import com.daprimeatce.odinextras.utils.SharedMixinState;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.odtheking.odin.clickgui.settings.Setting;
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting;
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting;
import com.odtheking.odin.clickgui.settings.impl.StringSetting;
import com.odtheking.odin.features.impl.dungeon.MapInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.LinkedHashMap;

@Mixin(MapInfo.class)
@SuppressWarnings("unused")
abstract class MixinMapInfo {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$add300ScoreDropdownSetting(CallbackInfo ci) {
        SharedMixinState.odinextras$score300Dropdown = new DropdownSetting(
                "300 Score",
                false,
                "Customize what happens when reaching 300 score."
        );
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$add300ScoreCustomTitleStringSetting(CallbackInfo ci) {
        SharedMixinState.odinextras$score300CustomTitle = new StringSetting(
                "Custom Title",
                "&c300 Score",
                64,
                "What to render when reaching 300 score. Use \"&\" for color codes."
        );
        Setting.Companion.withDependency(SharedMixinState.odinextras$score300CustomTitle, () -> SharedMixinState.odinextras$score300Dropdown.getValue());
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void odinextras$reorderScoreTitle(CallbackInfo ci) {
        LinkedHashMap<String, Setting<?>> settings = MapInfo.INSTANCE.getSettings();

        Setting<?> scoreTitle = settings.remove("300 Score Title");
        if (scoreTitle == null) return;

        Setting<?> printScoreTime = settings.remove("Print Score Time");
        if (printScoreTime == null) return;

        // Might as well just register here for ordering
        LinkedHashMap<String, Setting<?>> reordered = new LinkedHashMap<>();
        reordered.put("300 Score", SharedMixinState.odinextras$score300Dropdown);
        reordered.put("300 Score Title", scoreTitle);
        reordered.put("Custom Title", SharedMixinState.odinextras$score300CustomTitle);
        reordered.put("Print Score Time", printScoreTime);
        reordered.putAll(settings);

        settings.clear();
        settings.putAll(reordered);
    }

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "NEW", target = "Lcom/odtheking/odin/clickgui/settings/impl/BooleanSetting;", ordinal = 2))
    private static BooleanSetting odinextras$addBase300ScoreTitleDependency(BooleanSetting original) {
        return Setting.Companion.withDependency(original, () -> SharedMixinState.odinextras$score300Dropdown.getValue());
    }

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "NEW", target = "Lcom/odtheking/odin/clickgui/settings/impl/BooleanSetting;", ordinal = 3))
    private static BooleanSetting odinextras$addBase300ScorePrintWhenScoreDependency(BooleanSetting original) {
        return Setting.Companion.withDependency(original, () -> SharedMixinState.odinextras$score300Dropdown.getValue());
    }
}


@Mixin(targets = "com.odtheking.odin.features.impl.dungeon.MapInfo$2", remap = false)
@SuppressWarnings("unused")
abstract class MixinMapInfo$2 {
    @SuppressWarnings("UnresolvedMixinReference")
    @ModifyArg(
            method = "invoke()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/odtheking/odin/utils/PlayerUtilsKt;alert$default(Ljava/lang/String;ZILjava/lang/Object;)V"
            ),
            index = 0
    )
    private static String odinextras$modifyAlertMessage(String original) {
        return SharedMixinState.odinextras$score300CustomTitle.getValue().replace("&", "§");
    }
}