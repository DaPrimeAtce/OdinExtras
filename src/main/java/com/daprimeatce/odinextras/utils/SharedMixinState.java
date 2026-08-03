package com.daprimeatce.odinextras.utils;

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting;
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting;
import com.odtheking.odin.clickgui.settings.impl.StringSetting;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class SharedMixinState {
    public static DropdownSetting odinextras$score300Dropdown;
    public static StringSetting odinextras$score300CustomTitle;
    public static BooleanSetting odinextras$score300ShouldSendToParty;
    public static StringSetting odinextras$score300PartyMessage;

    public static DropdownSetting odinextras$score270Dropdown;
    public static BooleanSetting odinextras$score270ShouldRenderTitle;
    public static StringSetting odinextras$score270CustomTitle;
    public static BooleanSetting odinextras$score270ShouldPrintScoreTime;
    public static BooleanSetting odinextras$score270ShouldSendToParty;
    public static StringSetting odinextras$score270PartyMessage;
    public static StringSetting odinextras$LeapMessage;
    public static BooleanSetting odinextras$LeapAnnounce;
    public static boolean odinextras$shown270Title = false;

    // On world load
    static {
        ClientPlayConnectionEvents.JOIN.register((_, _, _) -> odinextras$shown270Title = false);
    }
}