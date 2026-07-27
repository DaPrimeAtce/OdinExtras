package com.daprimeatce.odinextras.mixin;

import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.daprimeatce.odinextras.features.impl.skyblock.ResourcePack;

@Mixin(Pack.class)
public class MixinResourcePack {
    @Shadow
    @Final
    private PackLocationInfo location;
    @Inject(method = "isFixedPosition", at = @At("HEAD"), cancellable = true)
    private void moveServerPack(CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(ResourcePack.INSTANCE.moveResourcePack())) {
            if (location.id().startsWith("server/")) {
                cir.setReturnValue(false);
            }
        }
    }
}