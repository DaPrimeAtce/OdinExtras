package com.daprimeatce.odinextras.mixin;

import com.daprimeatce.odinextras.utils.SharedMixinState;
import com.odtheking.odin.clickgui.settings.Setting;
import com.odtheking.odin.clickgui.settings.impl.*;
import com.odtheking.odin.features.impl.dungeon.LeapMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.LinkedHashMap;

@Mixin(LeapMenu.class)
@SuppressWarnings("unused")
abstract class MixinLeapMenu {
    @Invoker("getLeapAnnounce")
    public abstract boolean odinextras$LeapAnnounce();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$addLeapMessage(CallbackInfo ci) {
        SharedMixinState.odinextras$LeapMessage = new StringSetting(
                "Leap Message",
                "Leaped to {name}!",
                128,
                "The message to send when leaping to a player. Use \"{name}\" for specific player names."
        );
        Setting.Companion.withDependency(SharedMixinState.odinextras$LeapMessage, () -> SharedMixinState.odinextras$LeapAnnounce.getValue());
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void odinextras$reorderLeapMenu(CallbackInfo ci) {
        LinkedHashMap<String, Setting<?>> settings = LeapMenu.INSTANCE.getSettings();

        LinkedHashMap<String, Setting<?>> reordered = new LinkedHashMap<>();
        reordered.putAll(settings);
        reordered.put("Leap Message", SharedMixinState.odinextras$LeapMessage);

        settings.clear();
        settings.putAll(reordered);
    }

    private static String odinextras$modifyLeapMessage(String original) {
        return SharedMixinState.odinextras$LeapMessage.getValue().replace("{name}", "${it}");
    }
}
