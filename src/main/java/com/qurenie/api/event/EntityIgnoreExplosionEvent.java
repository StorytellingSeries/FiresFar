package com.qurenie.api.event;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.neoforged.neoforge.event.entity.EntityEvent;

@Getter
public class EntityIgnoreExplosionEvent extends EntityEvent {

    @Setter
    boolean shouldIgnore;
    Explosion explosion;
    
    public EntityIgnoreExplosionEvent(Entity entity, Explosion explosion, boolean shouldIgnore) {
        super(entity);
        this.explosion = explosion;
        this.shouldIgnore = shouldIgnore;
    }
    
}
