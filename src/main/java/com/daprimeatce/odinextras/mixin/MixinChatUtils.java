package com.daprimeatce.odinextras.mixin;

import com.odtheking.odin.utils.ChatUtilsKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

// Changes the default argument of prefix to OdinExtras' prefix instead of Odin's when modMessage is called in OdinExtras
@Mixin(ChatUtilsKt.class)
public class MixinChatUtils {
    @ModifyArg(method = "modMessage(Ljava/lang/Object;Ljava/lang/String;Lnet/minecraft/network/chat/Style;)V",
            at = @At(
                    value = "INVOKE",
                target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"
            )
    )
    private static String modifyDefaultPrefix(String original) {
        if (calledFromOdinExtras()) {
            return original.replace("§3Odin §8»§r ", "§3Odin§aExtras §8»§r ");
        }
        return original;
    }

    @ModifyArg(method = "modMessage(Lnet/minecraft/network/chat/Component;Ljava/lang/String;Lnet/minecraft/network/chat/Style;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"
            )
    )
    private static String modifyPrefix(String original) {
        if (calledFromOdinExtras()) {
            return "§3Odin§aExtras §8»§r ";
        }
        return original;
    }

    @Unique
    private static boolean calledFromOdinExtras() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("com.daprimeatce")) {
                return true;
            }
        }
        return false;
    }
}