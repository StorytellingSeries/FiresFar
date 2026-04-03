package com.qurenie.relics_thirteenflames.client.gui;

import net.minecraft.world.phys.Vec2;

record CardPosition(float x, float y) {

    public Vec2 substruct(CardPosition cardPosition) {
        return new Vec2(x - cardPosition.x, y - cardPosition.y);
    }

    public CardPosition add(Vec2 vec2) {
        return new CardPosition(x + vec2.x, y + vec2.y);
    }

}
