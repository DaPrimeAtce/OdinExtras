package com.daprimeatce.odinextras.mixin;

import com.daprimeatce.odinextras.accessor.AccessorMapInfo;
import com.daprimeatce.odinextras.state.StateSharedMixinMapInfo;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.odtheking.odin.clickgui.settings.Setting;
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting;
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting;
import com.odtheking.odin.clickgui.settings.impl.StringSetting;
import com.odtheking.odin.events.ScoreUpdateEvent;
import com.odtheking.odin.events.core.EventBus;
import com.odtheking.odin.features.impl.dungeon.MapInfo;
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.LinkedHashMap;

import static com.odtheking.odin.utils.ChatUtilsKt.modMessage;
import static com.odtheking.odin.utils.ChatUtilsKt.sendCommand;
import static com.odtheking.odin.utils.PlayerUtilsKt.alert;

@Mixin(MapInfo.class)
@SuppressWarnings("unused")
abstract class MixinMapInfo implements AccessorMapInfo {
    @Invoker("getScoreTitle")
    public abstract boolean odinextras$getScoreTitle();

    @Invoker("getPrintWhenScore")
    public abstract boolean odinextras$getPrintWhenScore();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$add300ScoreDropdownSetting(CallbackInfo ci) {
        StateSharedMixinMapInfo.odinextras$score300Dropdown = new DropdownSetting(
                "300 Score",
                false,
                "Customize what happens when reaching 300 score."
        );
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$add300ScoreCustomTitleStringSetting(CallbackInfo ci) {
        StateSharedMixinMapInfo.odinextras$score300CustomTitle = new StringSetting(
                "Custom Title",
                "&a300 Score",
                64,
                "What to render when reaching 300 score. Use \"&\" for color codes."
        );
        Setting.Companion.withDependency(StateSharedMixinMapInfo.odinextras$score300CustomTitle, () -> StateSharedMixinMapInfo.odinextras$score300Dropdown.getValue());
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$add300ScorePartyBooleanSetting(CallbackInfo ci) {
        StateSharedMixinMapInfo.odinextras$score300ShouldSendToParty = new BooleanSetting(
                "Send Party Message",
                false,
                "Sends a custom message on 300 score."
        );
        Setting.Companion.withDependency(StateSharedMixinMapInfo.odinextras$score300ShouldSendToParty, () -> StateSharedMixinMapInfo.odinextras$score300Dropdown.getValue());
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$add300ScorePartyStringSetting(CallbackInfo ci) {
        StateSharedMixinMapInfo.odinextras$score300PartyMessage = new StringSetting(
                "Custom Message",
                "300 Score Reached!",
                64,
                "What to send when reaching 300 score."
        );
        Setting.Companion.withDependency(StateSharedMixinMapInfo.odinextras$score300PartyMessage, () -> StateSharedMixinMapInfo.odinextras$score300Dropdown.getValue());
    }



    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$add270ScoreDropdownSetting(CallbackInfo ci) {
        StateSharedMixinMapInfo.odinextras$score270Dropdown = new DropdownSetting(
                "270 Score",
                false,
                "Customize what happens when reaching 270 score."
        );
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$add270ScoreTitleBooleanSetting(CallbackInfo ci) {
        StateSharedMixinMapInfo.odinextras$score270ShouldRenderTitle = new BooleanSetting(
                "270 Score Title",
                false,
                "Displays a title on 270 score."
        );
        Setting.Companion.withDependency(StateSharedMixinMapInfo.odinextras$score270ShouldRenderTitle, () -> StateSharedMixinMapInfo.odinextras$score270Dropdown.getValue());
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$add270ScoreCustomTitleStringSetting(CallbackInfo ci) {
        StateSharedMixinMapInfo.odinextras$score270CustomTitle = new StringSetting(
                "Custom Title",
                "270 Score",
                64,
                "What to render when reaching 270 score. Use \"&\" for color codes."
        );
        Setting.Companion.withDependency(StateSharedMixinMapInfo.odinextras$score270CustomTitle, () -> StateSharedMixinMapInfo.odinextras$score270Dropdown.getValue());
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$add270ScorePartyBooleanSetting(CallbackInfo ci) {
        StateSharedMixinMapInfo.odinextras$score270ShouldSendToParty = new BooleanSetting(
                "Send Party Message",
                false,
                "Sends a custom message on 270 score."
        );
        Setting.Companion.withDependency(StateSharedMixinMapInfo.odinextras$score270ShouldSendToParty, () -> StateSharedMixinMapInfo.odinextras$score270Dropdown.getValue());
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$add270ScorePartyStringSetting(CallbackInfo ci) {
        StateSharedMixinMapInfo.odinextras$score270PartyMessage = new StringSetting(
                "Custom Message",
                "270 Score Reached!",
                64,
                "What to send when reaching 270 score."
        );
        Setting.Companion.withDependency(StateSharedMixinMapInfo.odinextras$score270PartyMessage, () -> StateSharedMixinMapInfo.odinextras$score270Dropdown.getValue());
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void odinextras$add270ScorePrintScoreTimeBooleanSetting(CallbackInfo ci) {
        StateSharedMixinMapInfo.odinextras$score270ShouldPrintScoreTime = new BooleanSetting(
                "Print Score Time",
                false,
                "Displays a title on 270 score."
        );
        Setting.Companion.withDependency(StateSharedMixinMapInfo.odinextras$score270ShouldPrintScoreTime, () -> StateSharedMixinMapInfo.odinextras$score270Dropdown.getValue());
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void odinextras$reorderScoreTitle(CallbackInfo ci) {
        LinkedHashMap<String, Setting<?>> settings = MapInfo.INSTANCE.getSettings();

        Setting<?> scoreTitle = settings.remove("300 Score Title");
        if (scoreTitle == null) return;

        Setting<?> printScoreTime = settings.remove("Print Score Time");
        if (printScoreTime == null) return;

        // Might as well just register here for ordering
        LinkedHashMap<String, Setting<?>> reordered = new LinkedHashMap<>();
        reordered.put("270 Score", StateSharedMixinMapInfo.odinextras$score270Dropdown);
        reordered.put("270 Score Title", StateSharedMixinMapInfo.odinextras$score270ShouldRenderTitle);
        reordered.put("Custom 270 Score Title", StateSharedMixinMapInfo.odinextras$score270CustomTitle);
        reordered.put("Print 270 Score Time", StateSharedMixinMapInfo.odinextras$score270ShouldPrintScoreTime);
        reordered.put("Send 270 Score Party Message", StateSharedMixinMapInfo.odinextras$score270ShouldSendToParty);
        reordered.put("Custom 270 Score Message", StateSharedMixinMapInfo.odinextras$score270PartyMessage);

        reordered.put("300 Score", StateSharedMixinMapInfo.odinextras$score300Dropdown);
        reordered.put("300 Score Title", scoreTitle);
        reordered.put("Custom 300 Score Title", StateSharedMixinMapInfo.odinextras$score300CustomTitle);
        reordered.put("Print 300 Score Time", printScoreTime);
        reordered.put("Send 300 Score Party Message", StateSharedMixinMapInfo.odinextras$score300ShouldSendToParty);
        reordered.put("Custom 300 Score Message", StateSharedMixinMapInfo.odinextras$score300PartyMessage);
        reordered.putAll(settings);

        settings.clear();
        settings.putAll(reordered);
    }

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "NEW", target = "Lcom/odtheking/odin/clickgui/settings/impl/BooleanSetting;", ordinal = 2))
    private static BooleanSetting odinextras$addBase300ScoreTitleDependency(BooleanSetting original) {
        return Setting.Companion.withDependency(original, () -> StateSharedMixinMapInfo.odinextras$score300Dropdown.getValue());
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void odinextras$addScoreListener(CallbackInfo ci) {
        Function1<ScoreUpdateEvent, Unit> handler = (event) -> {
            if (!MapInfo.INSTANCE.getEnabled() ||
                    !DungeonUtils.INSTANCE.getInDungeons()
                    || StateSharedMixinMapInfo.odinextras$shown270Title
                    || (!StateSharedMixinMapInfo.odinextras$score270ShouldRenderTitle.getValue() && !StateSharedMixinMapInfo.odinextras$score270ShouldPrintScoreTime.getValue() && !StateSharedMixinMapInfo.odinextras$score270ShouldSendToParty.getValue())
                    || DungeonUtils.INSTANCE.getScore() < 270
                    || DungeonUtils.INSTANCE.getScore() >= 300)
                return null;

            if (StateSharedMixinMapInfo.odinextras$score270ShouldRenderTitle.getValue()) alert(StateSharedMixinMapInfo.odinextras$score270CustomTitle.getValue().replace("&", "§"), true);
            if (StateSharedMixinMapInfo.odinextras$score270ShouldPrintScoreTime.getValue() && DungeonUtils.INSTANCE.getFloor() != null) modMessage("§b" + DungeonUtils.INSTANCE.getScore() + " §ascore reached in §6" + DungeonUtils.INSTANCE.getDungeonTime() + " || " + DungeonUtils.INSTANCE.getFloor().name() + ".", "§3Odin§aExtras §8»§r ", null);
            if (StateSharedMixinMapInfo.odinextras$score270ShouldSendToParty.getValue()) sendCommand("pc " + StateSharedMixinMapInfo.odinextras$score270PartyMessage.getValue());
            StateSharedMixinMapInfo.odinextras$shown270Title = true;
            return Unit.INSTANCE;
        };

        EventBus.INSTANCE.registerListener(MapInfo.class, ScoreUpdateEvent.class, 0, false, handler);
    }
}

@SuppressWarnings("unused")
@Mixin(targets = "com.odtheking.odin.features.impl.dungeon.MapInfo$1", remap = false)
abstract class MixinMapInfoOnScoreUpdateEvent {
    /**
     * @author odtheking, adapted by TurtleGD
     * @reason Tweaking if statement logic to check for the new 300 score party message toggle
     */
    @Overwrite
    @SuppressWarnings("DataFlowIssue")
    public void invoke(ScoreUpdateEvent event) {
        boolean scoreTitle = ((AccessorMapInfo)(Object) MapInfo.INSTANCE).odinextras$getScoreTitle();
        boolean printWhenScore = ((AccessorMapInfo)(Object) MapInfo.INSTANCE).odinextras$getPrintWhenScore();

        if (!MapInfo.INSTANCE.getEnabled()
                || !DungeonUtils.INSTANCE.getInDungeons()
                || StateSharedMixinMapInfo.odinextras$shown300Title
                || (!scoreTitle && !printWhenScore && !StateSharedMixinMapInfo.odinextras$score300ShouldSendToParty.getValue())
                || DungeonUtils.INSTANCE.getScore() < 300)
            return;

        if (scoreTitle) alert(StateSharedMixinMapInfo.odinextras$score300CustomTitle.getValue().replace("&", "§"), true);
        if (printWhenScore && DungeonUtils.INSTANCE.getFloor() != null) modMessage("§b" + DungeonUtils.INSTANCE.getScore() + " §ascore reached in §6" + DungeonUtils.INSTANCE.getDungeonTime() + " || " + DungeonUtils.INSTANCE.getFloor().name() + ".", "§3Odin §8»§r ", null);
        if (StateSharedMixinMapInfo.odinextras$score300ShouldSendToParty.getValue()) sendCommand("pc " + StateSharedMixinMapInfo.odinextras$score300PartyMessage.getValue());
        StateSharedMixinMapInfo.odinextras$shown300Title = true;
    }
}