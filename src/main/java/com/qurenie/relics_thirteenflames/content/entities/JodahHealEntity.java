package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.UUID;

public class JodahHealEntity extends Entity {
    
    public static final String OWNER_TAG = "owner";
    public static final String TARGET_TAG = "target";
    public static final String XP_POWER_TAG = "xp_power";
    public static final String HEAL_POWER_TAG = "heal_power";
    public static final String AGE_TAG = "age";
    private static final double RADIUS = 4;
    private static final double MAX_AGE = 600;
    private static final double SUCK_IN_RADIUS = 0.3;
    private static final double SPEED = 0.8;
    private static final Color PURPLE_COLOR = new Color(160, 20, 120);
    float healingPower;
    int age = 0;
    int xpPower;
    Vec3 clientPreviousPos = Vec3.ZERO;
    @Getter
    private Player owner;
    private UUID ownerUUID;
    @Getter
    private LivingEntity target;
    private int targetId;
    
    public JodahHealEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }
    
    public JodahHealEntity(EntityType<?> entityType, Level level, float healingPower, int xpPower, Player owner, LivingEntity target) {
        super(entityType, level);
        this.healingPower = healingPower;
        this.owner = owner;
        this.target = target;
        this.xpPower = xpPower;
        
        this.ownerUUID = owner.getUUID();
        this.targetId = target.getId();
        this.noPhysics = true;
        this.setPos(getTargetHeardPos().add(new Vec3(getRandom().nextDouble() * 0.4 - 0.2, getRandom().nextDouble() * 0.4 - 0.2, getRandom().nextDouble() * 0.4 - 0.2)));
        this.clientPreviousPos = position();
    }
    
    @Override
    public void setPos(double x, double y, double z) {
        if (level().isClientSide) {
            clientPreviousPos = position();
        }
        super.setPos(x, y, z);
    }
    
    @Override
    public void tick() {
        super.tick();
        age++;
//
        if (this.age > MAX_AGE) {
            kill();
        }
        
        if (this.level().isClientSide) {
            ParticleHelper.spawnParticleLine(level(), ParticleHelper.constructSimpleSpark(PURPLE_COLOR, 0.36f,
                    30, 0.95f), position(), clientPreviousPos, 5, 0.005, 0.02);
            if (tickCount % 2 == 0)
                ParticleHelper.spawnParticleLine(level(), ParticleHelper.constructHeal(PURPLE_COLOR, (float) (0.2f + level().random.nextDouble() * 0.12f),
                        40, 0.95f), position(), clientPreviousPos, 1, 0.008, 0.02);
            return;
        }
        
        if (this.owner == null)
            bindOwner();
        
        if (this.owner == null || owner.isDeadOrDying() || !this.owner.level().dimension().location().equals(this.level().dimension().location())
                || this.distanceToSqr(owner) >= 100 * 100)
            return;
        
        if (this.distanceToSqr(getOwnerHeardPos()) <= SUCK_IN_RADIUS) {
            owner.heal(healingPower);
            owner.giveExperiencePoints(xpPower);
            kill();
            return;
        }
        
        Vec3 acceleration = getRawMovement();
        setDeltaMovement(getDeltaMovement().add(acceleration.scale(1f)).normalize().scale(SPEED));
        this.moveTo(position().x + getDeltaMovement().x, position().y + getDeltaMovement().y, position().z + getDeltaMovement().z);
    }
    
    protected Vec3 getRawMovement() {
        if (this.distanceToSqr(getOwnerHeardPos()) <= SUCK_IN_RADIUS * SUCK_IN_RADIUS) {
            owner.heal(healingPower);
            owner.giveExperiencePoints(xpPower);
            kill();
        } else if (this.distanceToSqr(getOwnerHeardPos()) <= RADIUS * RADIUS) {
            Vec3 toOwner = getOwnerHeardPos().subtract(position()).normalize();
            Vec3 orbit = new Vec3(toOwner.z, toOwner.y / 3, -toOwner.x);
            Vec3 combined = orbit.add(toOwner.scale(4 / Math.pow(this.position().distanceToSqr(getOwnerHeardPos()), 1.1)));
            
            return combined.normalize().scale(SPEED * 1.5);
        } else if (target.isAlive() && this.distanceToSqr(getTargetHeardPos()) <= RADIUS * RADIUS) {
            Vec3 toTarget = getTargetHeardPos().subtract(position()).normalize();
            Vec3 orbit = new Vec3(-toTarget.z, 0.2, toTarget.x);
            Vec3 combined = orbit.add(toTarget.scale(1 / this.position().distanceTo(getTargetHeardPos())));
            
            return combined.normalize().scale(SPEED * 1.2);
        }
        
        return getOwnerHeardPos().subtract(position()).normalize().scale(SPEED);
    }
    
    private Vec3 getOwnerHeardPos() {
        return owner.getBoundingBox().getCenter();
    }
    
    private Vec3 getTargetHeardPos() {
        return target.getBoundingBox().getCenter();
    }
    
    private void bindOwner() {
        this.owner = level().getPlayerByUUID(ownerUUID);
    }
    
    private void bingTarget() {
        this.target = (LivingEntity) level().getEntity(targetId);
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
    }
    
    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compound) {
        this.ownerUUID = compound.getUUID(OWNER_TAG);
        this.targetId = compound.getInt(TARGET_TAG);
        this.xpPower = compound.getInt(XP_POWER_TAG);
        this.age = compound.getInt(AGE_TAG);
        this.healingPower = compound.getFloat(HEAL_POWER_TAG);
        
        bingTarget();
        bindOwner();
    }
    
    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compound) {
        compound.putUUID(OWNER_TAG, ownerUUID);
        compound.putInt(TARGET_TAG, targetId);
        compound.putInt(XP_POWER_TAG, xpPower);
        compound.putInt(AGE_TAG, age);
        compound.putFloat(HEAL_POWER_TAG, healingPower);
    }
    
}
