package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static com.qurenie.relics_thirteenflames.content.items.ItemJodahMask.*;

@Getter
public class SkintOrbEntity extends Entity {
    
    public static final String TARGET_TAG = "target";
    public static final String TYPE_TAG = "skint_type";
    public static final String LIMIT_BONUS_TAG = "limit_bonus";
    
    private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(SkintOrbEntity.class, EntityDataSerializers.INT);
    
    private int targetID;
    private Entity target;
    private Vec3 prevPos;
    private int limitBonus;
    
    public SkintOrbEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }
    
    public SkintOrbEntity(Level level, Type type, Entity target, double x, double y, double z, int limitBonus) {
        super(EntityRegistry.SKINT_ORB, level);
        this.target = target;
        this.targetID = target.getId();
        setSkintType(type);
        setPos(new Vec3(x, y, z));
        this.limitBonus = limitBonus;
        this.setYRot((float) (Math.random() * Math.PI * 2));
        setDeltaMovement(new Vec3(getRandom().nextGaussian(), getRandom().nextDouble(), getRandom().nextGaussian()).normalize().scale(0.4));
    }
    
    public SkintOrbEntity(Level level, Type type, Entity to, Entity from, int limitBonus) {
        super(EntityRegistry.SKINT_ORB, level);
        this.target = to;
        this.targetID = to.getId();
        setSkintType(type);
        setPos(from.getBoundingBox().getCenter());
        this.limitBonus = limitBonus;
        this.setYRot((float) (Math.random() * Math.PI * 2));
        setDeltaMovement(new Vec3(getRandom().nextGaussian(), getRandom().nextDouble(), getRandom().nextGaussian()).normalize().scale(0.4));
    }
    
    @Override
    public void tick() {
        if (!level().isClientSide && target == null) {
            remove(RemovalReason.DISCARDED);
            return;
        }
        if(prevPos == null) prevPos = this.position();
        
        super.tick();
        
        if (level().isClientSide) {
            ParticleHelper.spawnParticleLine(level(), ParticleHelper.constructSimpleSpark(getSkintType().color, 0.26f,
                    10, 0.93f).withLightning(getSkintType().lightning), position(), prevPos, 3, () -> new Vec3(level().random.nextGaussian() * 0.003, 0.02 + level().random.nextGaussian() * 0.003, level().random.nextGaussian() * 0.003), 0);
            
            ParticleHelper.spawnParticleLine(level(), ParticleHelper.constructSmoke(getSkintType().color, 0.2f, 15, 0).withLightning(getSkintType().lightning),
                    position(), prevPos, 2, () -> new Vec3(level().random.nextGaussian() * 0.002, 0.013 + level().random.nextGaussian() * 0.002, level().random.nextGaussian() * 0.002), 0);
        }
        
        prevPos = this.position();
        if (level().isClientSide)
            return;
        
        if (target.getBoundingBox().inflate(0.3).contains(this.position())) {
            this.kill();
            if (getSkintType() == Type.ANTISKINT)
                FlamesUtils.addAntiskint(target, 1, limitBonus);
            else
                FlamesUtils.addSkint(target, 1, limitBonus);
        }
        
        final double maxSpeed = 0.5;
        Vec3 target = this.target.getBoundingBox().getCenter();
        Vec3 acceleration = target.subtract(this.position()).normalize().scale(0.1);
        Vec3 movement = this.getDeltaMovement().scale(0.95).add(acceleration);
        if (movement.lengthSqr() > maxSpeed * maxSpeed)
            movement = movement.normalize().scale(maxSpeed);
        setDeltaMovement(movement);
        this.moveTo(position().x + getDeltaMovement().x, position().y + getDeltaMovement().y, position().z + getDeltaMovement().z);
        if (this.tickCount > 200)
            this.discard();
    }
    
    @Override
    public void remove(@NotNull RemovalReason reason) {
        super.remove(reason);
        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(getSkintType().color, 0.34f,
                25, 0.95f).withLightning(getSkintType().lightning).withGravity(0.4f), this, 15, 0.07);
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        builder.define(TYPE, 0);
    }
    
    public @NotNull Type getSkintType() {
        return Type.values()[entityData.get(TYPE)];
    }
    
    public void setSkintType(Type type) {
        entityData.set(TYPE, type.ordinal());
    }
    
    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compound) {
        setSkintType(Type.values()[compound.getInt(TYPE_TAG)]);
        this.targetID = compound.getInt(TARGET_TAG);
        this.target = level().getEntity(targetID);
        this.limitBonus = compound.getInt(LIMIT_BONUS_TAG);
    }
    
    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compound) {
        compound.putInt(TYPE_TAG, getSkintType().ordinal());
        compound.putInt(TARGET_TAG, targetID);
        compound.putInt(LIMIT_BONUS_TAG, limitBonus);
    }
    
    @Override
    public boolean isAlive() {
        return false;
    }
    
    public enum Type {
        SKINT(GOLD_COLOR, true),
        ANTISKINT(GRAY_COLOR, false);
        
        final Color color;
        final boolean lightning;
        
        Type(Color color, boolean lightning) {
            this.color = color;
            this.lightning = lightning;
        }
    }
    
}
