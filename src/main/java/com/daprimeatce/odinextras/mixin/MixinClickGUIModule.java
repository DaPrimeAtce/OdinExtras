package com.daprimeatce.odinextras.mixin;

import com.daprimeatce.odinextras.state.StateSharedMixinClickGUIModule;
import com.odtheking.odin.clickgui.ClickGUI;
import com.odtheking.odin.clickgui.settings.Setting;
import com.odtheking.odin.clickgui.settings.impl.NumberSetting;
import com.odtheking.odin.clickgui.widget.ModuleWidget;
import com.odtheking.odin.clickgui.widget.PanelWidget;
import com.odtheking.odin.features.Category;
import com.odtheking.odin.features.impl.render.ClickGUIModule;
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting;
import kotlin.ranges.RangesKt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashMap;
import java.util.List;

@Mixin(ClickGUIModule.class)
@SuppressWarnings("unused")
abstract class MixinClickGUIModule {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$addClickGUIScale(CallbackInfo ci) {
        StateSharedMixinClickGUIModule.odinextras$clickGUISize = new NumberSetting<>(
                "Click GUI Size",
                2.0,
                RangesKt.rangeTo(1.0, 4.0),
                0.1,
                "GUI scale the Click GUI is drawn at, whatever the video setting says",
                ""
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
    private static void odinextras$reorderClickGUI(CallbackInfo ci) {
        LinkedHashMap<String, Setting<?>> settings = ClickGUIModule.INSTANCE.getSettings();

        LinkedHashMap<String, Setting<?>> reordered = new LinkedHashMap<>(settings);
        reordered.putFirst("Click GUI Size", StateSharedMixinClickGUIModule.odinextras$clickGUISize);
        reordered.put("Alphabetical Sorting", StateSharedMixinClickGUIModule.odinextras$alphabeticalSorting);

        settings.clear();
        settings.putAll(reordered);
    }
}

@Mixin(value = PanelWidget.class, remap = false)
@SuppressWarnings("unused")
abstract class MixinPanel {
    @Shadow
    @Final
    @Mutable
    private List<ModuleWidget> modules;

    @Unique
    private List<ModuleWidget> odinextras$defaultOrder;

    @Unique
    private List<ModuleWidget> odinextras$alphabeticalOrder;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$captureDefaultOrder(Category category, CallbackInfo ci) {
        odinextras$defaultOrder = List.copyOf(this.modules);
        odinextras$alphabeticalOrder = List.copyOf(this.modules).stream().sorted((a, b) -> a.getModule().getName().compareToIgnoreCase(b.getModule().getName())).toList();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void odinextras$applySortOrder(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (StateSharedMixinClickGUIModule.odinextras$alphabeticalSorting.getValue()) {
            this.modules = odinextras$alphabeticalOrder;
        } else {
            this.modules = odinextras$defaultOrder;
        }
    }
}

@Mixin(value = ClickGUI.class, remap = false)
@SuppressWarnings("unused")
abstract class MixinClickGUI {
    @Shadow
    private static float scale;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lcom/odtheking/odin/clickgui/widget/SearchBarWidget;place(II)V"))
    private static void odinextras$applyCustomGUISize(CallbackInfo ci) {
        scale = (float) (StateSharedMixinClickGUIModule.odinextras$clickGUISize.getValue() / Minecraft.getInstance().getWindow().getGuiScale());
    }
}

