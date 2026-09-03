package com.daprimeatce.odinextras.mixin;

import com.odtheking.odin.features.impl.boss.TerminalTitles;
import com.odtheking.odin.utils.skyblock.dungeon.DungeonPlayer;
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TerminalTitles.class)
@SuppressWarnings("unused")
abstract class MixinTerminalTitles {
    @ModifyArg(method = "handleTitle$lambda$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lkotlin/text/StringsKt;replace$default(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZILjava/lang/Object;)Ljava/lang/String;",
                    ordinal = 0
            ),
            index = 2)
    private static String odinextras$addColorToTerminalTitle(String original) {
        DungeonPlayer player = DungeonUtils.INSTANCE.getDungeonTeammates().stream().filter(p -> p.getName().equals(original)).findFirst().orElse(null);
        if (player == null) return original;
        else return "§" + player.getClazz().getColorCode() + original;
    }
}