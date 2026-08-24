package com.daprimeatce.odinextras.mixin;

import com.daprimeatce.odinextras.state.StateSharedMixinClickGUIModule;
import com.odtheking.odin.clickgui.Panel;
import com.odtheking.odin.clickgui.settings.ModuleButton;
import com.odtheking.odin.clickgui.settings.Setting;
import com.odtheking.odin.features.Category;
import com.odtheking.odin.features.impl.render.ClickGUIModule;
import com.odtheking.odin.clickgui.settings.impl.StringSetting;
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashMap;
import java.util.List;

@Mixin(ClickGUIModule.class)
@SuppressWarnings("unused")
abstract class MixinClickGUIModule {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$addClickGUIScaleStringSetting(CallbackInfo ci) {
        StateSharedMixinClickGUIModule.odinextras$clickGUIScale = new StringSetting(
                "Click GUI Scale",
                "1",
                4,
                "Enables a custom Click GUI scale between 0.5 and 2."
        );
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$addalphabeticalSortingBooleanSetting(CallbackInfo ci) {
        StateSharedMixinClickGUIModule.odinextras$alphabeticalSorting = new BooleanSetting(
                "Alphabetical Sorting",
                false,
                "Reorganizes the Click GUI modules based on A-Z instead of by character length."
        );
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void odinextras$reorderLeapMenu(CallbackInfo ci) {
        LinkedHashMap<String, Setting<?>> settings = ClickGUIModule.INSTANCE.getSettings();

        LinkedHashMap<String, Setting<?>> reordered = new LinkedHashMap<>(settings);
        reordered.put("Alphabetical Sorting", StateSharedMixinClickGUIModule.odinextras$alphabeticalSorting);
        reordered.put("Custom GUI Scale", StateSharedMixinClickGUIModule.odinextras$clickGUIScale);

        settings.clear();
        settings.putAll(reordered);
    }

    @Inject(method = "getStandardGuiScale", at = @At("HEAD"), cancellable = true)
    private void getStandardGuiScale(CallbackInfoReturnable<Float> cir) {
        try {
            cir.setReturnValue(Math.clamp(Float.parseFloat(StateSharedMixinClickGUIModule.odinextras$clickGUIScale.getValue()), 0.5f, 2f));
            cir.cancel();
        } catch (Exception _) {
            cir.setReturnValue(1f);
            cir.cancel();
        }
    }
}

@Mixin(value = Panel.class, remap = false)
@SuppressWarnings("unused")
abstract class MixinPanel {
    @Shadow
    @Final
    @Mutable
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
        if (StateSharedMixinClickGUIModule.odinextras$alphabeticalSorting.getValue()) {
            this.moduleButtons = odinextras$alphabeticalOrder;
        } else {
            this.moduleButtons = odinextras$defaultOrder;
        }
    }
}