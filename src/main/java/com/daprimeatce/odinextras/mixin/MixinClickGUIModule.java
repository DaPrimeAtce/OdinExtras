package com.daprimeatce.odinextras.mixin;

import com.daprimeatce.odinextras.features.impl.render.ClickGUIPlus;
import com.odtheking.odin.features.impl.render.ClickGUIModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClickGUIModule.class, remap = false)
public class MixinClickGUIModule {
    @Inject(method = "getStandardGuiScale", at = @At("HEAD"), cancellable = true)
    private void getStandardGuiScale(CallbackInfoReturnable<Float> cir) {
        if (ClickGUIPlus.INSTANCE.getEnabled()) {
            try {
                cir.setReturnValue(Math.clamp(Float.parseFloat(ClickGUIPlus.INSTANCE.getScale()), 0.5f, 2f));
                cir.cancel();
            } catch (Exception _) {
                cir.setReturnValue(1f);
                cir.cancel();
            }
        }
    }
}