package com.qurenie.api;

import com.qurenie.relics_thirteenflames.client.bar.IBarSetting;
import com.qurenie.relics_thirteenflames.data.TempData;

public interface IBarContainer {

    SettingsContainer<IBarSetting> constructBarSettings();

    default SettingsContainer<IBarSetting> getBarSettings() {
        return TempData.BAR_TEMPLATES.computeIfAbsent(
                this,
                key -> constructBarSettings()
        );
    }

}
