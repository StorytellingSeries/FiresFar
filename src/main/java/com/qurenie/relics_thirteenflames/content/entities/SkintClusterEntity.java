package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.api.event.EntityIgnoreExplosionEvent;
import com.qurenie.relics_thirteenflames.client.AnimationsRegistry;
import com.qurenie.relics_thirteenflames.content.entities.base.NonLivingEntity;
import com.qurenie.relics_thirteenflames.content.items.misc.ScintType;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import com.qurenie.relics_thirteenflames.mixins.EntityAccessor;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammeranims.api.animsys.AnimationSystem;
import org.zeith.hammeranims.api.tile.IAnimatedEntity;

import java.awt.*;
import java.util.List;
import java.util.UUID;

import static com.qurenie.relics_thirteenflames.content.entities.AnimatedEntity.LAYER_ACTION;
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

public class SkintClusterEntity extends NonLivingEntity implements IAnimatedEntity {
    
    public static final String OWNER_TAG = "owner";
    public static final String TYPE_TAG = "skint_type";
    public static final String DAMAGE = "damage";
    public static final String SKINT_BONUS = "bonus";
    public static final String SKINT_COUNT = "skints";
    public static final int MAX_AGE = 40;

    public static final int CLUSTER_BORN_TICK = 19;

    private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(SkintClusterEntity.class, EntityDataSerializers.INT);
    AnimationSystem system = AnimationSystem.create(this);
    float damage;
    int skintCount;
    int skintBonus;
    UUID ownerID;
    @Nullable Player owner;
    
    final float yBodyRot = (float) (Math.random() * Math.PI * 2);
    
    public SkintClusterEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }
    
    public SkintClusterEntity(EntityType<? extends LivingEntity> entityType, Level level, ScintType type, Player owner, float damage, int skintCount, int skintLimitBonus) {
        super(entityType, level);
        this.damage = damage;
        this.ownerID = owner.getUUID();
        this.skintBonus = skintLimitBonus;
        this.owner = owner;
        this.skintCount = skintCount;
        this.setYBodyRot(yBodyRot);
        this.setNoGravity(true);
        setSkintType(type);
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return tickCount >= CLUSTER_BORN_TICK;
    }

    private void startAnimation(boolean reversed) {
        system.startAnimationAt(LAYER_ACTION, AnimationsRegistry.SCINT_CLUSTER_APPEAR.configure()
                .speed(1f).transitionTime(0).reversed(reversed));
    }

    private double getAnimCompletion() {
        var layer = system.getLayer(LAYER_ACTION);
        if (layer == null || layer.getCurrentAnimation() == null)
            return -1;

        double time = system.getTime(0);
        return ((time - layer.getCurrentAnimation().activationTime) * (double)layer.getCurrentAnimation().config.speed) / layer.getCurrentAnimation().getLengthSeconds();

    }

    private boolean hasAnimation() {
        var layer = system.getLayer(LAYER_ACTION);
        if (layer == null)
            return false;

        double time = system.getTime(1);
        return layer.getCurrentAnimation() != null &&
                (time - layer.getCurrentAnimation().activationTime) * (double)layer.getCurrentAnimation().config.speed < layer.getCurrentAnimation().getLengthSeconds();
    }
    
    @Override
    public boolean ignoreExplosion(@NotNull Explosion explosion) {
        return true;
    }
    
    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        EVENT_BUS.unregister(this);
    }
    
    @Override
    public boolean shouldShowName() {
        return false;
    }
    
    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        this.setYBodyRot((float) (Math.random() * Math.PI * 2));
        EVENT_BUS.register(this);
    }

    private void spawnBlockParticles(int count, double maxSpeed) {
        AABB aabb = getBoundingBox().contract(0, getBoundingBox().getYsize(), 0);
        BlockPos.MutableBlockPos pos = blockPosition().mutable();
        BlockState state = level().getBlockState(pos);
        if (!state.isAir())
            ParticleHelper.spawnParticleAABB(level(), new BlockParticleOption(ParticleTypes.BLOCK, state), aabb, count, maxSpeed);

        state = level().getBlockState(pos.below());
        if (!state.isAir())
            ParticleHelper.spawnParticleAABB(level(), new BlockParticleOption(ParticleTypes.BLOCK, state), aabb, count, maxSpeed);

    }

    private int getMaxAge() {
        return MAX_AGE + CLUSTER_BORN_TICK;
    }

    private int activeTickCount() {
        return Math.max(0, tickCount - CLUSTER_BORN_TICK);
    }
    
    @Override
    public void tick() {
        system.tick();
        super.tick();
        
        setYBodyRot(yBodyRot);

        if (level().isClientSide)
            return;

        if (tickCount < CLUSTER_BORN_TICK) {
                spawnBlockParticles(20, 0.1);
            return;
        } else if (tickCount == CLUSTER_BORN_TICK) {
            startAnimation(false);
            playSound(SoundsRegistry.CRYSTAL_APPEAR.get(), 0.7f, (float) (Math.random() * 0.4f + 0.8f));
            spawnBlockParticles(100, 0.3);
        } else if (tickCount == getMaxAge()) {
            startAnimation(true);
            playSound(SoundsRegistry.CRYSTAL_DISSAPPEAR.get(), 0.7f, (float) (Math.random() * 0.4f + 0.8f));
        } else if (tickCount >= getMaxAge() && !level().isClientSide) {
            if (getAnimCompletion() > 0.7)
                ParticleHelper.spawnParticleAABB(level(), ParticleHelper.constructSmoke(getSkintType().color, 1.1f,
                        20).withLightning(getSkintType().lightning), this.getBoundingBox().contract(0, 2, 0), 8, 0.02);
            if (!hasAnimation())
                discard();
            return;
        }
        
        double height = this.getBbHeight() / 6 * Math.min(activeTickCount(), 5);
        AABB aabb = getBoundingBox().contract(0, getBoundingBox().getYsize(), 0).expandTowards(0, height, 0);
        
        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, aabb, e -> !(e instanceof SkintClusterEntity) && e.isAlive() && e.isPickable());
        
        for (var living : entities) {
            if (!level().isClientSide && activeTickCount() % 10 == 0) {
                if (living.getUUID() != ownerID)
                    hurtEntity(living, damage);
                else
                    hurtEntity(living, 1);
            }
            ((EntityAccessor) living).setStuckSpeedMultiplier(new Vec3(0.07, 0.03F, 0.07));
        }
    }
    
    private void hurtEntity(LivingEntity living, float damage) {
        DamageSource source = owner == null ? living.damageSources().generic() : living.damageSources().playerAttack(owner);
        living.hurt(source, damage);
        
        if (owner != null) {
            living.setLastHurtByPlayer(owner);
            
            if (living.isDeadOrDying()) {
                switch (getSkintType()) {
                    case SKINT -> {
                        for (int i = 0; i < skintCount; i++) {
                            SkintOrbEntity entity = new SkintOrbEntity(level(), ScintType.SKINT, owner, living, skintBonus);
                            level().addFreshEntity(entity);
                        }
                    }
                    case ANTISKINT -> {
                        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3.4),
                                e -> e.getUUID() != ownerID && !(e instanceof SkintClusterEntity) && e.isAlive() && e.isPickable());
                        level().explode(living, level().damageSources().playerAttack(owner), null, living.position(), 0.4f, false, Level.ExplosionInteraction.MOB);
                        for (var l : entities) {
                            for (int i = 0; i < skintCount; i++) {
                                SkintOrbEntity entity = new SkintOrbEntity(level(), ScintType.ANTISKINT, l, living, skintBonus);
                                level().addFreshEntity(entity);
                            }
                        }
                    }
                }
                
                if (owner != null) {
                    ItemStack stack = owner.getItemBySlot(EquipmentSlot.HEAD);
                    if (stack.is(ItemsRegistry.JODAH_MASK)) {
                        ItemsRegistry.JODAH_MASK.addExperience(owner, stack, 1);
                    }
                }
            }
        }
        
        living.push(0, 0.2, 0);
    }
    
    public void destroyByHammer() {
        if (owner != null) {
            level().explode(this, damageSources().playerAttack(owner), new ExplosionDamageCalculator() {
                        @Override
                        public float getKnockbackMultiplier(Entity entity) {
                            return 0;
                        }
                        
                        @Override
                        public float getEntityDamageAmount(@NotNull Explosion explosion, @NotNull Entity entity) {
                            float f = explosion.radius() * 2.0F;
                            Vec3 vec3 = explosion.center();
                            double d0 = Math.sqrt(entity.distanceToSqr(vec3)) / (double)f;
                            double d1 = (1.0 - d0) * (double)Explosion.getSeenPercent(vec3, entity);
                            return (float)((d1 * d1 + d1) / 2.0 * 7.0 * (double)f + 1.0) * 4;
                        }
                    }, position(),
                    (float) (0.5 + Math.sqrt(damage)), false, Level.ExplosionInteraction.TRIGGER);
            ParticleHelper.spawnParticleEntity(ParticleHelper.constructSmoke(getSkintType().color, 0.64f,
                    40, 0f).withLightning(getSkintType().lightning).withGravity(2f), this, 13, 0.015);
        }
        remove(RemovalReason.DISCARDED);
    }
    
    @SubscribeEvent
    public void onExplode(EntityIgnoreExplosionEvent explosionEvent) {
        if (explosionEvent.getExplosion().getDirectSourceEntity() == this && explosionEvent.getEntity().getUUID().equals(ownerID))
            explosionEvent.setShouldIgnore(true);
    }
    
    @Override
    public void remove(@NotNull RemovalReason reason) {
        ParticleHelper.spawnParticleAABB(level(), ParticleHelper.constructSimpleSpark(getSkintType().color, 1f,
                40, 0.93f).withLightning(getSkintType().lightning).withGravity(2f), this.getBoundingBox().contract(0, 2, 0), 15, 0.1);
        ParticleHelper.spawnParticleAABB(level(), ParticleHelper.constructSmoke(getSkintType().color, 0.8f,
                40).withLightning(getSkintType().lightning), this.getBoundingBox().contract(0, 2, 0), 10, 0.03);
        super.remove(reason);
    }
    
    @Override
    public void setupSystem(AnimationSystem.Builder builder) {
        FlamesUtils.setupAnimationSystem(builder);
    }
    
    @Override
    public AnimationSystem getAnimationSystem() {
        return system;
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TYPE, 0);
    }
    
    public @NotNull ScintType getSkintType() {
        return ScintType.values()[entityData.get(TYPE)];
    }
    
    public void setSkintType(ScintType type) {
        entityData.set(TYPE, type.ordinal());
    }
    
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setSkintType(ScintType.values()[compound.getInt(TYPE_TAG)]);
        this.damage = compound.getInt(DAMAGE);
        this.ownerID = compound.getUUID(OWNER_TAG);
        this.skintBonus = compound.getInt(SKINT_BONUS);
        this.skintCount = compound.getInt(SKINT_COUNT);
        this.owner = level().getPlayerByUUID(uuid);
    }
    
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt(TYPE_TAG, getSkintType().ordinal());
        compound.putInt(SKINT_BONUS, skintBonus);
        compound.putFloat(DAMAGE, damage);
        compound.putUUID(OWNER_TAG, ownerID);
        compound.putInt(SKINT_COUNT, skintCount);
    }
    
}
