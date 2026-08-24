package com.daprimeatce.odinextras.mixin;

import com.daprimeatce.odinextras.features.impl.render.DroppedItemScale;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(ItemEntityRenderer.class)
public class MixinItemEntityRenderer {
    @Inject(method = "submit(" +
                "Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;" +
                "Lcom/mojang/blaze3d/vertex/PoseStack;" +
                "Lnet/minecraft/client/renderer/SubmitNodeCollector;" +
                "Lnet/minecraft/client/renderer/state/level/CameraRenderState;" +
            ")V",
            at = @At(
                value = "INVOKE",
                target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
                shift = At.Shift.AFTER
            )
    )
    public void odinextras$submit(
            final ItemEntityRenderState state,
            final PoseStack poseStack,
            final SubmitNodeCollector submitNodeCollector,
            final CameraRenderState camera,
            final CallbackInfo ci
    ) {
        if (DroppedItemScale.INSTANCE.getEnabled()) {
            float scale = DroppedItemScale.INSTANCE.getScale();
            poseStack.scale(scale, scale, scale);
        }
    }
}