package com.qurenie.relics_thirteenflames.content.items.misc;

import it.hurts.octostudios.octolib.util.OctoColor;

import static com.qurenie.relics_thirteenflames.style.ColorScheme.GOLD_COLOR;
import static com.qurenie.relics_thirteenflames.style.ColorScheme.GRAY_COLOR;

public enum ScintType {
    SKINT(GOLD_COLOR, true),
    ANTISKINT(GRAY_COLOR, false);

    public final OctoColor color;
    public final boolean lightning;

    ScintType(OctoColor color, boolean lightning) {
        this.color = color;
        this.lightning = lightning;
    }
}
