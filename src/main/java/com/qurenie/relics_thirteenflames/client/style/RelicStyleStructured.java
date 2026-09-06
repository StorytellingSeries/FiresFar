package com.qurenie.relics_thirteenflames.client.style;

import it.hurts.octostudios.octolib.util.OctoColor;
import it.hurts.sskirillss.relics.client.style.base.RelicStyle;
import lombok.Builder;
import org.jetbrains.annotations.Nullable;

@Builder
public class RelicStyleStructured
         extends RelicStyle {

    @Nullable
    OctoColor topTooltipBorder;
    @Nullable
    OctoColor bottomTooltipBorder;
    @Nullable
    OctoColor topTooltipBackground;
    @Nullable
    OctoColor bottomTooltipBackground;

    public RelicStyleStructured(@Nullable OctoColor topTooltipBorder, @Nullable OctoColor bottomTooltipBorder, @Nullable OctoColor topTooltipBackground, @Nullable OctoColor bottomTooltipBackground) {
        this.topTooltipBorder = topTooltipBorder;
        this.bottomTooltipBorder = bottomTooltipBorder;
        this.topTooltipBackground = topTooltipBackground;
        this.bottomTooltipBackground = bottomTooltipBackground;
    }
}
