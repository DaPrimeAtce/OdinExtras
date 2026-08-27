package com.daprimeatce.odinextras.mixin;

import com.daprimeatce.odinextras.state.StateSharedMixinClickGUIModule;
import com.odtheking.odin.clickgui.settings.Setting;
import com.odtheking.odin.clickgui.settings.impl.NumberSetting;
import com.odtheking.odin.clickgui.widget.ModuleWidget;
import com.odtheking.odin.clickgui.widget.PanelWidget;
import com.odtheking.odin.features.Category;
import com.odtheking.odin.features.impl.render.ClickGUIModule;
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.ranges.IntRange;
import kotlin.ranges.RangesKt;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashMap;
import java.util.List;

@Mixin(ClickGUIModule.class)
@SuppressWarnings("unused")
abstract class MixinClickGUIModule {
    // this lets us change the gui scale to be smoother and change by 0.1 instead of 1
    // but rendering still only takes integers and idk where or how to fix that
    // anyways fine to leave incomplete for now since the base mod now has our old feature

//        @Redirect(
//            method = "<clinit>",
//            at = @At(
//                value = "NEW",
//                target = "Lcom/odtheking/odin/clickgui/settings/impl/NumberSetting;"
//            )
//        )
//        private static NumberSetting<Double> redirectClickGuiScaleSetting(String name, Number defaultValue, IntRange range, Number increment, String desc, String unit, int mask, DefaultConstructorMarker marker) {
//            return new NumberSetting<>(name, 2.0, RangesKt.rangeTo(1.0, 4.0), 0.1, desc, "");
//        }


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