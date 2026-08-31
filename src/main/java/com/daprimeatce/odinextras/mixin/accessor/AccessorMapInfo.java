package com.daprimeatce.odinextras.mixin.accessor;

import com.odtheking.odin.features.impl.dungeon.MapInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin (MapInfo.class)
public interface AccessorMapInfo {
    @Invoker("getScoreTitle")
    boolean odinextras$getScoreTitle();

    @Invoker("getPrintWhenScore")
    boolean odinextras$getPrintWhenScore();
}