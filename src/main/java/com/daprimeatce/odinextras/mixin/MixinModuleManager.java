package com.daprimeatce.odinextras.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.odtheking.odin.features.ModuleManager;
import com.odtheking.odin.features.Module;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Removes the ChatCommands module from Odin to replace with ChatCommands+
@Mixin(ModuleManager.class)
public class MixinModuleManager {
    // Treats it as a dev module during registering
    @ModifyExpressionValue(method = "registerModules", at = @At(value = "INVOKE", target = "Lcom/odtheking/odin/features/Module;isDevModule()Z"))
    private boolean odinextras$skipRegisteringChatCommandsDevModuleCheck(boolean original, @Local Module module) {
        return original || module.getName().equals("Chat Commands");
    }

    // Treats it as a player env during registering specifically ChatCommands so the "dev" module won't register for us
    @ModifyExpressionValue(method = "registerModules", at = @At(value = "INVOKE",
            target = "Lnet/fabricmc/loader/api/FabricLoader;isDevelopmentEnvironment()Z"))
    private boolean odinextras$skipRegisteringChatCommandsDevEnvCheck(boolean original, @Local Module module) {
        if (module.getName().equals("Chat Commands")) return false;
        return original;
    }
}