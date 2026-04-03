package com.qurenie.relics_thirteenflames.client.gui;

import net.minecraft.client.Minecraft;

import static com.qurenie.relics_thirteenflames.data.ActivityState.OPEN_DELAY;

public interface SizeModifier {

    SizeModifier MOUSE_SELECT = c -> {
        Minecraft mc = Minecraft.getInstance();

        double rawX = mc.mouseHandler.xpos();
        double rawY = mc.mouseHandler.ypos();

        return c.mouseSelectedAbsolute(rawX, rawY, false) ? 1.2f : 1;
    };

    SizeModifier OPENNESS = c -> {
        float p = Math.min(OPEN_DELAY, ActivityCallGui.openness) / (float) OPEN_DELAY;
        return p * p;
    };

    SizeModifier TARGET = card -> {
        float dScale = card.getTarget().sizeScale() - card.getPrevTarget().sizeScale();
        float length = card.getTarget().position().substruct(card.getPrevTarget().position()).length();

        if (length == 0) return card.getPrevTarget().sizeScale();

        float distance = card.position.substruct(card.getTarget().position()).length();
        return card.getPrevTarget().sizeScale() + dScale * (1 - distance / length);
    };

    float getSizeModifier(CardGuiEntity card);

}
