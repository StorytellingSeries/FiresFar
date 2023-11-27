package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import it.hurts.sskirillss.relics.client.particles.circle.CircleTintData;
import it.hurts.sskirillss.relics.client.particles.spark.SparkTintData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class PoisonWaveEntity extends Projectile {


    public PoisonWaveEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }



    @Override
    public boolean isAlwaysTicking() {
        return true;
    }

    LinkedList<DelayedRunnable> taskQueue = new LinkedList<>();

    public void addTask(int delay, Runnable runnable){
        this.taskQueue.add(new DelayedRunnable(runnable, 0, delay));
    }

    @Override
    public void tick() {
        super.tick();

        if(!this.level.isClientSide()){
            if(!taskQueue.isEmpty()) {
                if (taskQueue.getFirst().startedAt + taskQueue.getFirst().delay <= this.tickCount)
                    taskQueue.pop().runnable.run();
            } else this.discard();
        }
    }

    @Override
    public boolean canCollideWith(Entity p_20303_) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
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
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private class DelayedRunnable {
        Runnable runnable;
        int startedAt;
        int delay;
        DelayedRunnable(Runnable runnable, int startedAt, int delay){
            this.runnable = runnable;
            this.startedAt = startedAt;
            this.delay = delay;
        }
    }
}
