package com.qurenie.relics_thirteenflames.client.render.entity;

import net.minecraft.world.entity.LivingEntity;

public interface IJodahGlowed {
    
    boolean hasJodahGlowEffect();
    
    void setJodahGlowEffect(boolean enabled);
    
    static IJodahGlowed of(LivingEntity entity) {
        return (IJodahGlowed) entity;
    }
    
}
