package com.daprimeatce.odinextras.mixin;

import com.daprimeatce.odinextras.features.impl.dungeon.TeammateNametags;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public class MixinAvatarRenderer {
    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/Avatar;D)Z", at = @At("HEAD"), cancellable = true)
    private void odinextras$hideTeammateNametag(Avatar entity, double distanceToCameraSq, CallbackInfoReturnable<Boolean> cir) {
        if (TeammateNametags.shouldHideVanillaNametag(entity)) cir.setReturnValue(false);
    }
}