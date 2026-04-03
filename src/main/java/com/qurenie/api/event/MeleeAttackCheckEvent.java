package com.qurenie.api.event;

import lombok.Getter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

@Getter
public class MeleeAttackCheckEvent extends LivingEvent implements ICancellableEvent {
    
    LivingEntity target;
    
    public MeleeAttackCheckEvent(PathfinderMob entity, LivingEntity target) {
        super(entity);
        this.target = target;
    }
    
}
