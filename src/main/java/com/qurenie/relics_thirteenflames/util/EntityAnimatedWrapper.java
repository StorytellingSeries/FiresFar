package com.qurenie.relics_thirteenflames.util;

import lombok.Getter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.zeith.hammeranims.api.animation.interp.BlendMode;
import org.zeith.hammeranims.api.animsys.AnimationSystem;
import org.zeith.hammeranims.api.animsys.IAnimatedObject;
import org.zeith.hammeranims.api.animsys.layer.AnimationLayer;
import org.zeith.hammerlib.abstractions.sources.IObjectSource;

import static com.qurenie.relics_thirteenflames.content.entities.AnimatedEntity.LAYER_ACTION;
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

@Getter
public class EntityAnimatedWrapper implements IAnimatedObject {
    
    protected final AnimationSystem animationSystem;
    protected final Entity entity;
    private boolean closed = false;
    
    public EntityAnimatedWrapper(Entity entity) {
        this.entity = entity;
        animationSystem = AnimationSystem.create(this);
        if (!entity.isRemoved())
            EVENT_BUS.register(this);
    }
    
    @Override
    public void setupSystem(AnimationSystem.Builder builder) {
        LayersList list = new LayersList();
        list.addLast(LAYER_ACTION, BlendMode.OVERRIDE, 1.0F);
        
        int extraLayerCount = 5;
        
        for (int i = 0; i < extraLayerCount; i++) {
            list.addLast("ANIMATION_" + i, BlendMode.ADD, 1.0F);
        }
        
        builder.addLayers(list.getLayers().toArray(AnimationLayer.Builder[]::new));
        builder.autoSync(true);
    }
    
    @Override
    public IObjectSource<?> getAnimationSource() {
        return IObjectSource.ofEntity(entity).get();
    }
    
    @Override
    public float getAnimatedObjectWidth() {
        return entity.getBbWidth();
    }
    
    @Override
    public float getAnimatedObjectHeight() {
        return entity.getBbHeight();
    }
    
    @Override
    public Level getAnimatedObjectWorld() {
        return entity.level();
    }
    
    @Override
    public Vec3 getAnimatedObjectPosition() {
        return entity.position();
    }
    
    @SubscribeEvent
    public void onEntityLeave(EntityLeaveLevelEvent event) {
        if (event.getEntity() == entity)
            EVENT_BUS.unregister(this);
    }
    
    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() == entity)
            close();
    }

    public void close() {
        if (closed) return;
        closed = true;
        EVENT_BUS.unregister(this);
    }
    
}
