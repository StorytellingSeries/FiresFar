package com.qurenie.relics_thirteenflames.data;

import com.qurenie.api.IActivityContainer;
import com.qurenie.api.NamedSettingsContainer;
import com.qurenie.api.SettingsContainer;
import com.qurenie.api.IBarContainer;
import com.qurenie.relics_thirteenflames.activity.IActivitySetting;
import com.qurenie.relics_thirteenflames.client.bar.IBarSetting;

import java.util.HashMap;

public class TempData {

    public static final HashMap<IBarContainer, SettingsContainer<IBarSetting>> BAR_TEMPLATES = new HashMap<>();

    public static final HashMap<IActivityContainer, NamedSettingsContainer<IActivitySetting>> ACTIVITY_TEMPLATE = new HashMap<>();

}
