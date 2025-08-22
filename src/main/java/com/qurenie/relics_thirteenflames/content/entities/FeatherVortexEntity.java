package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.client.AnimationsRegistry;
import com.qurenie.relics_thirteenflames.content.entities.base.NonLivingEntity;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import lombok.Getter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammeranims.api.animation.LoopMode;
import org.zeith.hammeranims.api.animsys.AnimationSystem;
import org.zeith.hammeranims.api.tile.IAnimatedEntity;

import java.awt.*;

import static com.qurenie.relics_thirteenflames.content.entities.AnimatedEntity.LAYER_ACTION;
import static com.qurenie.relics_thirteenflames.util.FlamesUtils.setupAnimationSystem;

@Getter
public class FeatherVortexEntity extends NonLivingEntity implements IAnimatedEntity {
    
    private final AnimationSystem animationSystem = AnimationSystem.create(this);
    private int lvl;
    
    public FeatherVortexEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
        lvl = 1;
        setNoGravity(true);
        this.noPhysics = true;
    }
    
    public FeatherVortexEntity(LivingEntity target, Level level, int lvl) {
        this(EntityRegistry.FEATHER_VORTEX_ENTITY, level);
        this.setPos(target.position());
        target.startRiding(this);
        this.lvl = lvl;
        animationSystem.startAnimationAt("ANIMATION_1", AnimationsRegistry.ATTACK_BOOK_IDLE);
        animationSystem.startAnimationAt(LAYER_ACTION, AnimationsRegistry.BOOK_OPEN.configure()
                .speed(0.7f)
                .startTime(0.5f)
                .loopMode(LoopMode.ONCE)
                .next(AnimationsRegistry.BOOK_ATTACK.configure().loopMode(LoopMode.ONCE)));
    }
    
    @Override
    public void tick() {
        animationSystem.tick();
        super.tick();
        
        var currentAnim = animationSystem.getLayer(LAYER_ACTION).currentAnimation;
        if (currentAnim == null) {
            discard();
            return;
        }
        
        if (level().isClientSide) {
            for (int i = 0; i < 5; i++) {
                double y = 0.5 + random.nextDouble() * 1.5;
                Vec3 center = new Vec3(getX(), getY() + y, getZ());
                
                for (int j = 0; j < 3; j++) {
                    double r = y / 1.5 * 0.7;
                    
                    double x = random.nextGaussian() * r;
                    double z = Math.sqrt(r * r - x * x) * (random.nextBoolean() ? 1 : -1);
                    Vec3 spawn = center.add(x, 0, z);
                    
                    Vec3 radius = spawn.subtract(center);
                    Vec3 move = radius.normalize().yRot((float) (230f * Math.PI / 180f)).add(0, 0.02f, 0);
                    ParticleHelper.spawnDirectedParticle(level(), ParticleHelper.constructSimpleSpark(new Color(239, 215, 182), 0.13f, 20, 0.91f),
                            spawn, move.normalize().scale(0.03 * y));
                }
            }
        } else if (currentAnim.config.animation.getLocation().equals(AnimationsRegistry.BOOK_ATTACK.getLocation()) && FlamesUtils.getCompletion(animationSystem, animationSystem.getLayer(LAYER_ACTION)) >= 0.6) {
            for (Entity e : this.getPassengers()) {
                ItemStack stack = ItemsRegistry.HETT_FEATHER_BOOK.getDefaultInstance();
                stack.set(ComponentRegistry.LEVEL, lvl);
                stack.set(ComponentRegistry.TARGET_TYPE, EntityType.getKey(e.getType()).toString());
                ItemEntity item = new ItemEntity(e.level(), e.getX(), e.getY(), e.getZ(), stack);
                e.level().addFreshEntity(item);
                ParticleHelper.spawnParticleEntity(ParticleTypes.CAMPFIRE_COSY_SMOKE, e, 20, 0.05);
//                ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(new Color(239, 215, 182), 0.2f, 60, 0.97f), e, 20, 0.05);
                ParticleHelper.spawnParticleEntity(ParticleHelper.constructSmoke(new Color(239, 215, 182), (e.getBbHeight() + e.getBbWidth()) / 2, 60, 0).withLightning(false), e, 20, 0.03);
                
                e.discard();
            }
            
            discard();
        }
    }
    
    @Override
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }
    
    @Override
    protected void positionRider(@NotNull Entity passenger, @NotNull MoveFunction callback) {
//        super.positionRider(passenger, callback);
        Vec3 target = position().add(new Vec3(0, 2.5 + passenger.getBbHeight(), 0)
                .scale(Mth.sin((tickCount) / 6f))).add(0, 1.5 + passenger.getBbHeight(), 0);
        Vec3 delta = target.subtract(passenger.position()).normalize().scale(0.08);
        callback.accept(passenger, passenger.getX() + delta.x, passenger.getY() + delta.y, passenger.getZ() + delta.z);
        passenger.setYBodyRot((20 * tickCount) % 360);
        passenger.setYHeadRot((20 * tickCount) % 360);
        passenger.setYRot((20 * tickCount) % 360);
    }
    
    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
    }
    
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.lvl = compound.getInt("lvl");
    }
    
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("lvl", lvl);
    }
    
    @Override
    public boolean fireImmune() {
        return true;
    }
    
    @Override
    public boolean isInWall() {
        return false;
    }
    
    @Override
    public void setupSystem(AnimationSystem.Builder builder) {
        setupAnimationSystem(builder);
    }
    
}
