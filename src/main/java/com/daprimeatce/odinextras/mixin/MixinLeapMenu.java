package com.daprimeatce.odinextras.mixin;

import com.daprimeatce.odinextras.accessor.AccessorLeapMenu;
import com.daprimeatce.odinextras.state.StateSharedMixinLeapMenu;
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
abstract class MixinLeapMenu implements AccessorLeapMenu {
    @Invoker("getLeapAnnounce")
    public abstract boolean odinextras$getLeapAnnounce();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$addLeapMessage(CallbackInfo ci) {
        StateSharedMixinLeapMenu.odinextras$customLeapMessage = new StringSetting(
                "Leap Message",
                "Leaped to {name}!",
                128,
                "The message to send when leaping to a player. Use \"{name}\" for specific player names."
        );
        Setting.Companion.withDependency(StateSharedMixinLeapMenu.odinextras$customLeapMessage, () -> ((AccessorLeapMenu)(Object) LeapMenu.INSTANCE).odinextras$getLeapAnnounce());
    }
//
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void odinextras$reorderLeapMenu(CallbackInfo ci) {
        LinkedHashMap<String, Setting<?>> settings = LeapMenu.INSTANCE.getSettings();

        LinkedHashMap<String, Setting<?>> reordered = new LinkedHashMap<>(settings);
        reordered.put("Leap Message", StateSharedMixinLeapMenu.odinextras$customLeapMessage);

        settings.clear();
        settings.putAll(reordered);
    }
}


@Mixin(targets = "com.odtheking.odin.features.impl.dungeon.LeapMenu$6", remap = false)
@SuppressWarnings("unused")
abstract class MixinLeapMenuSendCommand {
    @SuppressWarnings("UnresolvedMixinReference")
    @ModifyArg(
            method = "invoke(Lcom/odtheking/odin/events/ChatPacketEvent;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/odtheking/odin/utils/ChatUtilsKt;sendCommand(Ljava/lang/String;)V"
            ),
            index = 0
    )
    private String modifySendCommandArg(String original) {
        String name = original.split(" ")[3].replace("!", "");
        return "pc " + StateSharedMixinLeapMenu.odinextras$customLeapMessage.getValue().replace("{name}", name);
    }
}