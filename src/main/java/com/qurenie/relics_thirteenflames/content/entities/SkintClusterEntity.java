package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.api.EntityIgnoreExplosionEvent;
import com.qurenie.relics_thirteenflames.client.AnimationsRegistry;
import com.qurenie.relics_thirteenflames.content.entities.base.NonLivingEntity;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
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
import static com.qurenie.relics_thirteenflames.content.items.ItemJodahMask.GOLD_COLOR;
import static com.qurenie.relics_thirteenflames.content.items.ItemJodahMask.GRAY_COLOR;
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

public class SkintClusterEntity extends NonLivingEntity implements IAnimatedEntity {
    
    public static final String OWNER_TAG = "owner";
    public static final String TYPE_TAG = "skint_type";
    public static final String DAMAGE = "damage";
    public static final String SKINT_BONUS = "bonus";
    public static final String SKINT_COUNT = "skints";
    public static final int MAX_AGE = 70;
    
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
    
    public SkintClusterEntity(EntityType<? extends LivingEntity> entityType, Level level, Type type, Player owner, float damage, int skintCount, int skintLimitBonus) {
        super(entityType, level);
        this.damage = damage;
        this.ownerID = owner.getUUID();
        this.skintBonus = skintLimitBonus;
        this.owner = owner;
        this.skintCount = skintCount;
        this.setYBodyRot(yBodyRot);
        this.setNoGravity(true);
        setSkintType(type);
        system.startAnimationAt(LAYER_ACTION, AnimationsRegistry.SCINT_CLUSTER_APPEAR.configure().speed(0.7f).transitionTime(0));
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
        
        AABB aabb = getBoundingBox().contract(0, getBoundingBox().getYsize(), 0);
        BlockPos pos = blockPosition();
        BlockState state = level().getBlockState(pos);
        if (state.isAir())
            state = level().getBlockState(pos.below());
        
        this.setYBodyRot((float) (Math.random() * Math.PI * 2));
        if (!state.isAir() && !level().isClientSide)
            ParticleHelper.spawnParticleAABB(level(), new BlockParticleOption(ParticleTypes.BLOCK, state), aabb, 80, 0.3);
        EVENT_BUS.register(this);
    }
    
    @Override
    public void tick() {
        system.tick();
        super.tick();
        
        setYBodyRot(yBodyRot);
        
        if (tickCount >= MAX_AGE && !level().isClientSide) {
            this.discard();
            return;
        }
        
        double height = this.getBbHeight() / 6 * Math.min(tickCount, 6);
        AABB aabb = getBoundingBox().contract(0, getBoundingBox().getYsize(), 0).expandTowards(0, height, 0);
        
        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, aabb, e -> !(e instanceof SkintClusterEntity) && e.isAlive() && e.isPickable());
        
        for (var living : entities) {
            if (!level().isClientSide && tickCount % 10 == 0) {
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
                            SkintOrbEntity entity = new SkintOrbEntity(level(), SkintOrbEntity.Type.SKINT, owner, living, skintBonus);
                            level().addFreshEntity(entity);
                        }
                    }
                    case ANTISKINT -> {
                        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3.4),
                                e -> e.getUUID() != ownerID && !(e instanceof SkintClusterEntity) && e.isAlive() && e.isPickable());
                        for (var l : entities) {
                            for (int i = 0; i < skintCount; i++) {
                                level().explode(living, level().damageSources().playerAttack(owner), null, living.position(), 0.4f, false, Level.ExplosionInteraction.MOB);
                                SkintOrbEntity entity = new SkintOrbEntity(level(), SkintOrbEntity.Type.ANTISKINT, l, living, skintBonus);
                                level().addFreshEntity(entity);
                            }
                        }
                    }
                }
                
                if (owner != null) {
                    ItemStack stack = owner.getItemBySlot(EquipmentSlot.HEAD);
                    if (stack.is(ItemsRegistry.JODAH_MASK)) {
                        ItemsRegistry.JODAH_MASK.addRelicExperience(stack, 1);
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
                    40, 0f).withLightning(getSkintType().lightning).withGravity(2f), this, 25, 0.03);
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
        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(getSkintType().color, 0.64f,
                40, 0.9f).withLightning(getSkintType().lightning).withGravity(2f), this, 25, 0.08);
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
    
    public @NotNull SkintOrbEntity.Type getSkintType() {
        return SkintOrbEntity.Type.values()[entityData.get(TYPE)];
    }
    
    public void setSkintType(SkintClusterEntity.Type type) {
        entityData.set(TYPE, type.ordinal());
    }
    
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setSkintType(SkintClusterEntity.Type.values()[compound.getInt(TYPE_TAG)]);
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
