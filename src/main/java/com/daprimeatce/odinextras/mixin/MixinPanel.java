package com.daprimeatce.odinextras.mixin;

import com.daprimeatce.odinextras.features.impl.render.ClickGUIPlus;
import com.odtheking.odin.clickgui.Panel;
import com.odtheking.odin.clickgui.settings.ModuleButton;
import com.odtheking.odin.features.Category;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(value = Panel.class, remap = false)
public class MixinPanel {
    @Shadow @Final @Mutable
    private List<ModuleButton> moduleButtons;

    @Unique
    private List<ModuleButton> odinextras$defaultOrder;

    @Unique
    private List<ModuleButton> odinextras$alphabeticalOrder;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$captureDefaultOrder(Category category, CallbackInfo ci) {
        odinextras$defaultOrder = List.copyOf(this.moduleButtons);
        odinextras$alphabeticalOrder = List.copyOf(this.moduleButtons).stream().sorted((a, b) -> a.getModule().getName().compareToIgnoreCase(b.getModule().getName())).toList();
    }


    @Inject(method = "draw", at = @At("HEAD"))
    private void odinextras$applySortOrder(float mouseX, float mouseY, CallbackInfo ci) {
        if (ClickGUIPlus.INSTANCE.getAlphabeticalSorting() && ClickGUIPlus.INSTANCE.getEnabled()) {
            this.moduleButtons = odinextras$alphabeticalOrder;
        } else {
            this.moduleButtons = odinextras$defaultOrder;
        }
    }
}
