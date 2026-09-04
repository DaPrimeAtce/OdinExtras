package com.daprimeatce.odinextras.mixin;

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting;
import com.odtheking.odin.features.impl.boss.TerminalTitles;
import com.odtheking.odin.utils.skyblock.dungeon.DungeonPlayer;
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils;
import com.odtheking.odin.clickgui.settings.Setting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.LinkedHashMap;

@Mixin(TerminalTitles.class)
@SuppressWarnings("unused")
abstract class MixinTerminalTitles {
    @Unique
    private static BooleanSetting classColors;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$addClassColorTerminalTitles(CallbackInfo ci) {
        classColors = new BooleanSetting(
                "Class Name Colors",
                true,
                "Replaces the color formatting of {name} in the Terminal Titles formatting with the respective class colors of each player."
        );
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void odinextras$reorderTerminalTitles(CallbackInfo ci) {
        LinkedHashMap<String, Setting<?>> settings = TerminalTitles.INSTANCE.getSettings();

        LinkedHashMap<String, Setting<?>> reordered = new LinkedHashMap<>(settings);
        reordered.putAll(settings);
        reordered.put("Class Name Colors", classColors);

        settings.clear();
        settings.putAll(reordered);
    }

    @ModifyArg(method = "handleTitle$lambda$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lkotlin/text/StringsKt;replace$default(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZILjava/lang/Object;)Ljava/lang/String;",
                    ordinal = 0
            ),
            index = 2)

    private static String odinextras$addColorToTerminalTitle(String original) {
        if (classColors.getValue()) {
            DungeonPlayer player = DungeonUtils.INSTANCE.getDungeonTeammates().stream().filter(p -> p.getName().equals(original)).findFirst().orElse(null);
            if (player == null) return original;
            else return "§" + player.getClazz().getColorCode() + original;
        }
        else return original;
    }
}