package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class KnefProjectileSpecial extends ThrowableProjectile
{
    public Vec3 prevPos;

    public Color color;

    public KnefProjectileSpecial(EntityType<? extends KnefProjectileSpecial> type, Level world) {
        super(type, world);
        this.color = new Color(100, 255, 178);
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder p_326003_) {
    
    }
    
    @Override
    public void tick() {
        if(this.getOwner() == null) this.discard();
        Vec3 motion = this.getDeltaMovement();
        super.tick();

        setDeltaMovement(motion);
        if(!level().isClientSide) {
            ParticleHelper.spawnParticleLine(this.level(), ParticleUtils.constructSimpleSpark(color, 0.1f, 40, 0.92f),
                    prevPos == null ? this.position() : prevPos, this.position(), 25, 0);
        }

        prevPos = this.position();
    }

    @Override
    public void checkDespawn() {
        if(this.tickCount > 240){
            this.discard();
        }
    }

    public static List<KnefProjectileSpecial> makeList(int count, Level level, Entity owner, Vec3 center, Vec3 move){
        List<KnefProjectileSpecial> list = new ArrayList<>();
        for (int i = 0; i < count; i++){
            KnefProjectileSpecial proj = new KnefProjectileSpecial(EntityRegistry.KNEF_PROJECTILE_SPECIAL, level);
            proj.setOwner(owner);
            proj.setPos(center);
            proj.setDeltaMovement(move);
            proj.color = new Color(100, 255, 178);
            list.add(proj);
        }
        return list;
    }

    @Override
    public boolean canCollideWith(Entity pEntity) {
        return (pEntity instanceof LivingEntity);
    }

    @SubscribeEvent
    public void onLevelUnload(PlayerEvent.PlayerLoggedOutEvent event) {
        this.discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
    }

    @Override
    public boolean shouldRender(double p_20296_, double p_20297_, double p_20298_) {
        return false;
    }
}
