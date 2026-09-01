package com.daprimeatce.odinextras.mixin.accessor;

import com.odtheking.odin.features.impl.dungeon.LeapMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LeapMenu.class)
public interface AccessorLeapMenu {
    @Invoker("getLeapAnnounce")
    boolean odinextras$getLeapAnnounce();
}