package com.daprimeatce.odinextras.mixin;

import com.daprimeatce.odinextras.features.impl.render.ClickGuiAddon;
import com.odtheking.odin.features.Module;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Comparator;

@Mixin(targets = "com.odtheking.odin.clickgui.ui.PanelKt")
@SuppressWarnings("unused")
abstract class MixinPanel {
    @ModifyArg(
            method = "Panel",
            at = @At(
                    value = "INVOKE",
                    target = "Lkotlin/collections/CollectionsKt;sortedWith(Ljava/lang/Iterable;Ljava/util/Comparator;)Ljava/util/List;"
            ),
            index = 1
    )
    private static Comparator<Module> odinextras$alphabetical(Comparator<Module> original) {
        if (!ClickGuiAddon.alphabetical()) return original;
        return Comparator.comparing(m -> m.getName().toLowerCase());
    }
}
