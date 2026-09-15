package com.daprimeatce.odinextras.mixin.accessor;

import com.odtheking.odin.utils.skyblock.SplitsGroup;
import com.odtheking.odin.utils.skyblock.SplitsManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SplitsManager.class)
public interface AccessorSplitsManager {

    @Accessor("currentSplits")
    SplitsGroup odinextras$getCurrentSplits();
}