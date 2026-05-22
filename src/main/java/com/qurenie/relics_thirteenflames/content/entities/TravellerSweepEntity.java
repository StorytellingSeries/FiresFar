package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.client.AnimationsRegistry;
import com.qurenie.relics_thirteenflames.content.entities.base.NonLivingEntity;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.zeith.hammeranims.api.animsys.AnimationSystem;
import org.zeith.hammeranims.api.tile.IAnimatedEntity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static com.qurenie.relics_thirteenflames.content.entities.AnimatedEntity.LAYER_ACTION;
import static com.qurenie.relics_thirteenflames.content.entities.TravellerCutEntity.directionToYBodyRot;
import static com.qurenie.relics_thirteenflames.style.ColorScheme.BURN_COLOR;
import static com.qurenie.relics_thirteenflames.style.ColorScheme.CYAN_COLOR;

public class TravellerSweepEntity extends NonLivingEntity implements IAnimatedEntity {
    
    public static final String OWNER_TAG = "owner";
    public static final String COMPLETION_TAG = "completion";
    public static final String DAMAGE_TAG = "damage";
    public static final String ANGLE_X = "dir_x";
    public static final String ANGLE_Z = "dir_z";
    public static final String STACK_TAG = "sword";
    public static final String FIRE_ASPECT_TAG = "fire_aspect";
    public static final String FULL_CIRCLE_TAG = "full_circle";
    public static final int ANIM_LENGTH = 8;
    public static final int SWORD_RANGE = 5;
    private static final EntityDataAccessor<Vector3f> DIRECTION = SynchedEntityData.defineId(TravellerSweepEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Float> COMPLETION = SynchedEntityData.defineId(TravellerSweepEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> FIRE_ASPECT = SynchedEntityData.defineId(TravellerSweepEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FULL_CIRCLE = SynchedEntityData.defineId(TravellerSweepEntity.class, EntityDataSerializers.BOOLEAN);
    AnimationSystem system = AnimationSystem.create(this);
    UUID ownerUUID;
    Player owner;
    double damageMultiplier;
    ItemStack stack;
    List<LivingEntity> targets = new ArrayList<>();
    
    public TravellerSweepEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
        this.yBodyRot = -directionToYBodyRot(this.getSwordDirection());
    }
    
    public TravellerSweepEntity(EntityType<? extends LivingEntity> entityType, Level level, Player owner, ItemStack stack, double damageMultiplier, int fireAspect, boolean fullCircle) {
        super(entityType, level);
        this.owner = owner;
        this.ownerUUID = owner.getUUID();
        Vec3 direction = owner.getLookAngle().multiply(1, 0, 1).add(0.0001f, 0, 0).normalize();
        setSwordDirection(new Vector3f((float) direction.x, 0f, (float) direction.z));
        this.noPhysics = true;
        this.damageMultiplier = damageMultiplier;
        this.stack = stack;
        detectTargets();
        setFireAspect(fireAspect);
        setFullCircle(fullCircle);
        
        this.setYBodyRot(owner.yHeadRot);
        this.noPhysics = true;
        system.startAnimationAt(LAYER_ACTION, AnimationsRegistry.ADVENTURER_SWORD_BIG_RUN_PIERCE
                .configure().transitionTime(0).speed(fullCircle ? 0.88f : 1.33f));
    }

    private double getAnimationLength() {
        return isFullCircle() ? ANIM_LENGTH * 1.33f / 0.88f : ANIM_LENGTH;
    }

    private double getFullRotationAngle() {
        return isFullCircle() ? Math.PI * 2 : Math.PI;
    }
    
    private List<Float> getAngles() {
        double fullAngle = getFullRotationAngle();
        double startAngle = -Math.PI / 2;
        float angle = (float) (startAngle + fullAngle * getCompletion());
        final float rot = (float) (getFullRotationAngle() / getAnimationLength());
        final float damageRot = (float) (Math.PI / 2f / 60f);
        
        return Stream.iterate((float) Math.max(startAngle, angle - rot - damageRot), f -> f <= Math.min(startAngle + fullAngle, angle + rot + damageRot), f -> f + damageRot)
                .map(Mth::wrapDegrees).toList();
    }
    
    @Override
    public void tick() {
        system.tick();
        system.sync();
        super.tick();
        
        if (!this.level().isClientSide && this.owner == null
                || getCompletion() > 1) {
            this.discard();
            return;
        }

        double fullAngle = getFullRotationAngle();
        double startAngle = Math.PI / 2;
        float rotAngle = (float) (startAngle + fullAngle * getCompletion());
        
        final float rot = (float) (1f / getAnimationLength());
        final var angles = getAngles();
        this.yBodyRot = (float) (Math.toDegrees(startAngle + Math.PI / 4) + Mth.wrapDegrees(directionToYBodyRot(this.getSwordDirection())
                        - (float) Math.toDegrees(rotAngle)));
        Vec3 direction = new Vec3(getSwordDirection().x, 0, getSwordDirection().z);
        
        if (level().isClientSide) {
            if (getCompletion() <= 1)
                for (float angle : angles) {
                    angle -= 0.07f;
                    Vec3 end = position().add(direction.yRot(angle).normalize().scale(SWORD_RANGE)).add(0, 0.65, 0);
                    Vec3 start = position().add(direction.yRot(angle).normalize()).add(0, 0.65, 0);
                    if (getFireAspect() > 0) {
                        ParticleHelper.spawnParticleLine(level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, getRandom()), 0.23f, 8 + random.nextInt(2), (float) (0.6f + random.nextDouble() * 0.1)),
                                start, end, 3, 0.04, 0.1);
                        ParticleHelper.spawnParticleLine(level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(BURN_COLOR, getRandom()), 0.23f, 8 + random.nextInt(2), (float) (0.6f + random.nextDouble() * 0.1)),
                                start, end, 3, 0.04, 0.1);
                    } else
                        ParticleHelper.spawnParticleLine(level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, getRandom()), 0.23f, 8 + random.nextInt(2), (float) (0.6f + random.nextDouble() * 0.1)),
                                start, end, 4, 0.04, 0.1);
                }
            
            return;
        }


        this.setPos(owner.position().add(0, 0.5, 0).add(direction.normalize().scale(1).yRot(rotAngle))
                .add(owner.getDeltaMovement().scale(2)));
        
        if (tickCount % 4 == 0)
            detectTargets();
        
        if (getCompletion() <= 1)
            targets.removeIf(target -> {
                boolean intersects = getAngles().stream().anyMatch(angle -> {
                    Vec3 end = position().add(direction.yRot(angle).normalize().scale(SWORD_RANGE + 0.6));
                    Vec3 start = position();
                    return target.getBoundingBox().inflate(0.6, 0.6, 0.6).intersects(start, end);
                });
                if (!intersects)
                    return false;
                target.setLastHurtByPlayer(owner);
                DamageSource source = owner.damageSources().playerAttack(owner);
                target.hurt(source, (float) (damageMultiplier * getPlayerDamage(owner, level(), target, stack, source)));
                target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), getFireAspect() * 80));
                
                if (stack.getItem() == ItemsRegistry.TRAVELLER_SWORD)
                    ItemsRegistry.TRAVELLER_SWORD.addExperience(owner, stack, 4);
                
                return true;
            });
        
        setCompletion(getCompletion() + rot);
    }
    
    private float getPlayerDamage(Player p, Level level, Entity entity, ItemStack stack, DamageSource source) {
        float f = (float) p.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (level instanceof ServerLevel serverlevel) {
            f = EnchantmentHelper.modifyDamage(serverlevel, stack, entity, source, f);
        }
        return f;
    }
    
    private int getFireAspect() {
        return entityData.get(FIRE_ASPECT);
    }
    
    private void setFireAspect(int fireAspect) {
        entityData.set(FIRE_ASPECT, fireAspect);
    }
    
    private float getCompletion() {
        return entityData.get(COMPLETION);
    }
    
    private void setCompletion(float completion) {
        entityData.set(COMPLETION, completion);
    }
    
    private Vector3f getSwordDirection() {
        return this.entityData.get(DIRECTION);
    }
    
    private void setSwordDirection(Vector3f vector3f) {
        this.entityData.set(DIRECTION, vector3f);
    }

    private boolean isFullCircle() {
        return entityData.get(FULL_CIRCLE);
    }

    private void setFullCircle(boolean fullCircle) {
        entityData.set(FULL_CIRCLE, fullCircle);
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DIRECTION, new Vector3f());
        builder.define(COMPLETION, 0f);
        builder.define(FIRE_ASPECT, 0);
        builder.define(FULL_CIRCLE, false);
    }
    
    private void detectTargets() {
        targets = level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(SWORD_RANGE + 2, 3, SWORD_RANGE + 2), entity -> entity != owner);
    }
    
    private void bindOwner() {
        this.owner = level().getPlayerByUUID(ownerUUID);
    }
    
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setFullCircle(compound.getBoolean(FULL_CIRCLE_TAG));
        this.ownerUUID = compound.getUUID(OWNER_TAG);
        setFireAspect(compound.getInt(FIRE_ASPECT_TAG));
        setCompletion(compound.getFloat(COMPLETION_TAG));
        setSwordDirection(new Vector3f(compound.getFloat(ANGLE_X), 0, compound.getFloat(ANGLE_Z)));
        this.damageMultiplier = compound.getDouble(DAMAGE_TAG);
        this.stack = ItemStack.parse(level().registryAccess(), Objects.requireNonNull(compound.get(STACK_TAG))).orElse(ItemStack.EMPTY);
        bindOwner();
    }
    
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putUUID(OWNER_TAG, ownerUUID);
        compound.putBoolean(FULL_CIRCLE_TAG, isFullCircle());
        compound.putInt(FIRE_ASPECT_TAG, getFireAspect());
        compound.putFloat(COMPLETION_TAG, getCompletion());
        compound.putDouble(ANGLE_X, getSwordDirection().x);
        compound.putDouble(ANGLE_Z, getSwordDirection().z);
        compound.putDouble(DAMAGE_TAG, damageMultiplier);
        Tag stacktag = stack.save(level().registryAccess(), new CompoundTag());
        compound.put(STACK_TAG, stacktag);
    }
    
    @Override
    public void setupSystem(AnimationSystem.Builder builder) {
        FlamesUtils.setupAnimationSystem(builder);
    }
    
    @Override
    public AnimationSystem getAnimationSystem() {
        return system;
    }
    
}
