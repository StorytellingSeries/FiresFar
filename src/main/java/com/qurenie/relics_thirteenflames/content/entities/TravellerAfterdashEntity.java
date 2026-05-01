package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Vector3f;

import java.awt.*;
import java.util.List;
import java.util.UUID;

import static com.qurenie.relics_thirteenflames.style.ColorScheme.BURN_COLOR;
import static com.qurenie.relics_thirteenflames.style.ColorScheme.CYAN_COLOR;

public class TravellerAfterdashEntity extends Entity {
    
    public static final String OWNER_TAG = "owner";
    public static final String SIZE_TAG = "tr_size";
    
    public static final String ANGLE_X = "dir_x";
    public static final String ANGLE_Z = "dir_z";
    public static final String FIRE_ASPECT_TAG = "fire_aspect";
    public static final String DAMAGE_TAG = "damage_tag";
    public static final String SIZE_DECREASE_TAG = "damage_tag";

    private static final EntityDataAccessor<Vector3f> DIRECTION = SynchedEntityData.defineId(TravellerAfterdashEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(TravellerAfterdashEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> FIRE_ASPECT = SynchedEntityData.defineId(TravellerAfterdashEntity.class, EntityDataSerializers.INT);
    
    UUID ownerUUID;
    Player owner;
    double damage;
    float sizeDecrease;
    
    public TravellerAfterdashEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }
    
    public TravellerAfterdashEntity(EntityType<?> entityType, Level level, Player owner, int fireAspect, int lifetime, double damage) {
        super(entityType, level);
        this.noPhysics = true;
        setFireAspect(fireAspect);
        setDashSize(1.5f);
        this.damage = damage;
        this.sizeDecrease = 1f / lifetime;
        this.owner = owner;
        this.ownerUUID = owner.getUUID();
        Vec3 look = owner.getLookAngle();
        setSwordDirection(new Vector3f((float) look.x, (float) look.y, (float) look.z));
        setPos(owner.position());
    }
    
    @Override
    public void tick() {
        super.tick();
        
        float size = getDashSize();
        this.checkInsideBlocks();
        
        if (size < 0.5 || isInWall()) {
            kill();
            return;
        }
        
        Vec3 direction = new Vec3(getSwordDirection().x, 0, getSwordDirection().z);
        Vec3 end = position().add(direction.normalize().scale(size));
        Vec3 top = position().add(new Vec3(0, 1, 0).scale(size));
        Vec3 side1 = position().add(direction.yRot((float) (Math.PI / 2)).normalize().scale(size / 2));
        Vec3 side2 = position().add(direction.yRot((float) -(Math.PI / 2)).normalize().scale(size / 2));
        int fireAspect = getFireAspect();
        
        if (level().isClientSide) {
            if (fireAspect <= 0) {
                ParticleHelper.spawnParticleTriangle(level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, getRandom()), 0.23f, 8, 0.6f),
                        end, top, side1, 8, direction.normalize().scale(-0.1));
                ParticleHelper.spawnParticleTriangle(level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, getRandom()), 0.23f, 8, 0.6f),
                        end, top, side2, 8, direction.normalize().scale(-0.1));
                ParticleHelper.spawnParticleTriangle(level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, getRandom()), 0.23f, 8, 0.6f),
                        end, side1, side2, 8, direction.normalize().scale(-0.1));
            } else {
                ParticleHelper.spawnParticleTriangle(level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, getRandom()), 0.23f, 8, 0.6f),
                        end, top, side1, 5, direction.normalize().scale(-0.1));
                ParticleHelper.spawnParticleTriangle(level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, getRandom()), 0.23f, 8, 0.6f),
                        end, top, side2, 5, direction.normalize().scale(-0.1));
                ParticleHelper.spawnParticleTriangle(level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, getRandom()), 0.23f, 8, 0.6f),
                        end, side1, side2, 5, direction.normalize().scale(-0.1));
                
                ParticleHelper.spawnParticleTriangle(level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(BURN_COLOR, getRandom()), 0.23f, 8, 0.6f),
                        end, top, side1, 4, direction.normalize().scale(-0.1));
                ParticleHelper.spawnParticleTriangle(level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(BURN_COLOR, getRandom()), 0.23f, 8, 0.6f),
                        end, top, side2, 4, direction.normalize().scale(-0.1));
                ParticleHelper.spawnParticleTriangle(level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(BURN_COLOR, getRandom()), 0.23f, 8, 0.6f),
                        end, side1, side2, 4, direction.normalize().scale(-0.1));
            }
            return;
        }
        
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(size), e -> {
            if (e == owner || e.isSpectator() || (e instanceof Player p && p.isCreative()))
                return false;
            
            AABB bb = e.getBoundingBox();
            return bb.intersects(end, top) || bb.intersects(end, side1) || bb.intersects(end, side2)
                    || bb.intersects(top, side1) || bb.intersects(top, side2);
        });
        
        for (LivingEntity target : targets) {
            DamageSource damageSource = level().damageSources().playerAttack(owner);
            target.hurt(damageSource, (float) damage);
            target.setLastHurtByPlayer(owner);
            
            if (fireAspect > 0)
                target.setRemainingFireTicks(50 * fireAspect);
            
            if (target.isPushable()) {
                Vec3 pushVector = direction.normalize().scale(0.2).add(0, 0.1, 0);
                target.push(pushVector.x, pushVector.y, pushVector.z);
            }
        }
        
        this.move(MoverType.SELF, direction.normalize().scale(0.8f));
        setDashSize(size - sizeDecrease);
    }
    
    @Override
    public boolean isInWall() {
        AABB aabb = this.getBoundingBox();
        return BlockPos.betweenClosedStream(aabb)
                .anyMatch(
                        p_201942_ -> {
                            BlockState blockstate = this.level().getBlockState(p_201942_);
                            return !blockstate.isAir()
                                    && blockstate.isSuffocating(this.level(), p_201942_)
                                    && Shapes.joinIsNotEmpty(
                                    blockstate.getCollisionShape(this.level(), p_201942_)
                                            .move(p_201942_.getX(), p_201942_.getY(), p_201942_.getZ()),
                                    Shapes.create(aabb),
                                    BooleanOp.AND
                            );
                        }
                );
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DIRECTION, new Vector3f());
        builder.define(SIZE, 0f);
        builder.define(FIRE_ASPECT, 0);
    }
    
    private int getFireAspect() {
        return entityData.get(FIRE_ASPECT);
    }
    
    private void setFireAspect(int fireAspect) {
        entityData.set(FIRE_ASPECT, fireAspect);
    }
    
    private float getDashSize() {
        return entityData.get(SIZE);
    }
    
    private void setDashSize(float dashSize) {
        entityData.set(SIZE, dashSize);
    }
    
    private Vector3f getSwordDirection() {
        return this.entityData.get(DIRECTION);
    }
    
    private void setSwordDirection(Vector3f vector3f) {
        this.entityData.set(DIRECTION, vector3f);
    }
    
    private void bindOwner() {
        this.owner = level().getPlayerByUUID(ownerUUID);
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.ownerUUID = compound.getUUID(OWNER_TAG);
        setFireAspect(compound.getInt(FIRE_ASPECT_TAG));
        setDashSize(compound.getFloat(SIZE_TAG));
        setSwordDirection(new Vector3f(compound.getFloat(ANGLE_X), 0, compound.getFloat(ANGLE_Z)));
        this.damage = compound.getDouble(DAMAGE_TAG);
        this.sizeDecrease = compound.getFloat(SIZE_DECREASE_TAG);
        bindOwner();
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putUUID(OWNER_TAG, ownerUUID);
        compound.putInt(FIRE_ASPECT_TAG, getFireAspect());
        compound.putFloat(SIZE_TAG, getDashSize());
        compound.putDouble(DAMAGE_TAG, damage);
        compound.putDouble(ANGLE_X, getSwordDirection().x);
        compound.putDouble(ANGLE_Z, getSwordDirection().z);
        compound.putFloat(SIZE_DECREASE_TAG, sizeDecrease);
    }
    
}
