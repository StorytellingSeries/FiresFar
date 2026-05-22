package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.client.AnimationsRegistry;
import com.qurenie.relics_thirteenflames.content.items.ItemKnefRose;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.style.ColorScheme;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammeranims.api.animsys.ConfiguredAnimation;
import org.zeith.hammeranims.core.init.DefaultsHA;
import org.zeith.hammerlib.HammerLib;
import org.zeith.hammerlib.util.java.Cast;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

import static com.qurenie.relics_thirteenflames.init.EntityDataSerializers.ROSE_STATS;

public class GhostBigEntity extends AnimatedEntity {
    private static final EntityDataAccessor<Float> DATA_SCALE = SynchedEntityData.defineId(GhostBigEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> OWNER_UUID = SynchedEntityData.defineId(GhostBigEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<ItemKnefRose.RoseStats> DATA_STATS = SynchedEntityData.defineId(GhostBigEntity.class, ROSE_STATS.get());

    public int shootCd;
    public int shootInterval = 30;
    public int lifetime;

    @Setter
    @Getter
    boolean explosiveBall;
    private double damageModifier;

    public GhostBigEntity(EntityType<? extends AnimatedEntity> type, Level world) {
        super(type, world);
        this.moveControl = new FlyingMoveControl(this, 20, true);

        animationSystem.startAnimationAt(LAYER_WALKING,
                AnimationsRegistry.ZERO_SCALE.configure().transitionTime(0)
        );

        canDie = true;
        this.lifetime = 170 + level().random.nextInt(50);
    }

    public void upgradeWithSouls(int souls, double damagePer, double lifetimePer) {
        lifetime += (int) (((float) souls) * lifetimePer);
        damageModifier = 1 + souls * damagePer;
    }

    protected GhostBigEntity createSplit() {
        var ent = new GhostBigEntity(Cast.cast(getType()), this.level());
        ent.moveTo(position());
        ent.setDeltaMovement(new Vec3(
                random.nextGaussian() - random.nextGaussian(),
                0,
                random.nextGaussian() - random.nextGaussian()
        ).normalize().scale(0.01));
        ent.setOwnerUUID(this.getOwnerUUID());
        if (getStats().rose.getItem() instanceof ItemKnefRose relic) {
            relic.addExperience(this.level().getPlayerByUUID(UUID.fromString(getOwnerUUID())), getStats().rose, 1);
        }
        return ent;
    }

    protected boolean hasSplit;

    @Override
    public void die(@NotNull DamageSource pDamageSource) {
        super.die(pDamageSource);
        this.setPose(Pose.CROAKING);

        if(explosiveBall)
        {
            ShadowMassEntity mass = new ShadowMassEntity(level(), this);

            mass.setPos(
                    getX(),
                    getY() + getBbHeight() * 0.5,
                    getZ()
            );

            mass.setDamage(getDamage() * 0.6F);
            mass.setLifetime(90);

            level().addFreshEntity(mass);

            return;
        }

        if (!hasSplit && !this.level().isClientSide()) {
            hasSplit = true;

            var stats = getStats();
            if (stats.counter >= 1) return;

            if (random.nextFloat() < stats.splitChance / 100F) {
                int splits = stats.generateSplits(random);
                for (int i = 0; i < splits; i++) {
                    var splitStats = stats.split();
                    float splitScale = getScale() * splitStats.splitScale / 100F;

                    var ent = createSplit()
                            .setScale(splitScale, getMaxHealth() * splitStats.splitScale / 100F)
                            .setStats(splitStats);

                    HammerLib.PROXY.queueTask(this.level(), 25, () -> this.level().addFreshEntity(ent));
                }
            }
        }
        DeathlyFartCloudEntity cloud = new DeathlyFartCloudEntity(EntityRegistry.DEATHCLOUD, this.level());
        cloud.setRadius(getScale() * 2);
        cloud.setLifeTime((int) (200 * getScale()));
        cloud.setPos(this.getBoundingBox().getCenter());
        try {
            cloud.setOwner(this.level().getPlayerByUUID(UUID.fromString(getOwnerUUID())));
        } catch (IllegalArgumentException ignored) {
        }
        ParticleHelper.spawnParticleEntity(
                ParticleHelper.constructSmoke(ColorScheme.GRAY_COLOR, 0.9f * getScale(), (int) (random.nextInt(30) + 50 * getScale()))
                        .withLightning(false)
                        .withGravity(0.2f),
                this,
                30,
                0.04 * getScale()
        );
        this.level().addFreshEntity(cloud);
    }

    public GhostBigEntity initPrimary(LivingEntity dead, ItemKnefRose.RoseStats stats) {
        return setStats(stats)
                .setScale(
                        (float) Mth.clamp(Math.pow(dead.getMaxHealth(), 1 / 3F) / 3F, 0.1F, 5F),
                        dead.getMaxHealth() * stats.hpRate
                );
    }

    public void tickWalking() {
        if (!this.level().isClientSide() && tickCount > 2) {
            var anims = getAnimationSystem();

            var idle = getIdleAnimation();
            var walking = getWalkingAnimation();

            // floats of movement can be almost the same (like 0 and 0.000000001), so entity moves a very short distance, which is invisible for eyes.
            // this can be because of converting coords to bytes to send them to client.
            // so checking if it's more than 1/256 of the block will fix the issue
            boolean posChanged = Math.abs(this.position().x - this.xo) >= 1 / 256F
                    || Math.abs(this.position().z - this.zo) >= 1 / 256F;

            if (posChanged) {
                anims.startAnimationAt(LAYER_WALKING, walking);
            } else {
                anims.startAnimationAt(LAYER_WALKING, idle);
            }
        }
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        if (this.isNoGravity()) {
            this.moveRelative(0.02F, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91));
        } else super.travel(travelVector);
    }

    public float getDamage() {
        return (float) (5 * getScale() * damageModifier);
    }

    @Override
    public void tick() {
        super.tick();

        if (shootCd > 0) --shootCd;

        // поиск цели
        if (tickCount % 10 == 0) {
            var target = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(16),
                            e -> e != this && (e instanceof Enemy || targetPriority(e) < 0))
                    .stream()
                    .min(Comparator.comparingDouble(e -> e.distanceTo(this) + targetPriority(e)))
                    .orElse(null);

            setTarget(target);
        }

        LivingEntity target = getTarget();

        if (target != null && target.isAlive()) {
            // орбитальное движение
            double radius = 6.0;

            Vec3 dir = target.position().subtract(this.position()).normalize();
            Vec3 orbit = new Vec3(-dir.z, 0, dir.x);

            Vec3 targetPos = target.position()
                    .add(dir.scale(-radius))
                    .add(orbit.scale(Math.sin(tickCount * 0.2) * 2))
                    .add(0, 1.5 + Math.cos(tickCount * 0.1), 0);

            navigation.moveTo(targetPos.x, targetPos.y, targetPos.z, 0.45);

            double dist = this.distanceTo(target);

            // стрельба
            if (shootCd <= 0 && dist < 18) {
                shootCd = (int) (shootInterval / Math.max(1, getAttributeValue(Attributes.MOVEMENT_SPEED)));
                shootProjectile(target);

                animationSystem.startAnimationAt(LAYER_ACTION, getAttackAnimation());
            }
        }

        if (target == null) {
            if (this.tickCount % 20 == 0) {
                Vec3 randomPos = getRandomFlyPos();
                navigation.moveTo(randomPos.x, randomPos.y, randomPos.z, 0.3);
            }

            // лёгкое "плавание"
            Vec3 drift = new Vec3(
                    random.nextGaussian() * 0.01,
                    random.nextGaussian() * 0.01,
                    random.nextGaussian() * 0.01
            );

            this.setDeltaMovement(this.getDeltaMovement().add(drift));

            // немного гасим скорость
            this.setDeltaMovement(this.getDeltaMovement().scale(0.95));
        }

        tickWalking();

        if (tickCount >= lifetime)
            this.kill();
    }

    private Vec3 getRandomFlyPos() {
        Vec3 motion = this.getDeltaMovement();

        // если почти стоим — берём lookDirection
        Vec3 forward = motion.lengthSqr() > 0.001
                ? motion.normalize()
                : this.getLookAngle();

        // боковой вектор (перпендикуляр)
        Vec3 side = new Vec3(-forward.z, 0, forward.x).normalize();

        // вверх
        Vec3 up = new Vec3(0, 1, 0);

        double forwardDist = 4 + random.nextDouble() * 4; // 4–8 блоков вперёд
        double sideOffset = (random.nextDouble() - 0.5) * 4; // -2..2
        double verticalOffset = (random.nextDouble() - 0.5) * 2; // -1..1

        return this.position()
                .add(forward.scale(forwardDist))
                .add(side.scale(sideOffset))
                .add(up.scale(verticalOffset));
    }

    private void shootProjectile(LivingEntity target) {
        if (this.level().isClientSide) return;

        Vec3 from = this.position().add(0, this.getBbHeight() * 0.5, 0);

        HomingWitherProjectile proj = new HomingWitherProjectile(
                this.level(),
                this,
                target,
                from,
                getDamage(),
                120
        );

        Vec3 predicted = target.position().add(target.getDeltaMovement().scale(5));
        Vec3 dir = predicted.subtract(from).normalize();

        proj.setDeltaMovement(dir.scale(0.35));

        this.level().addFreshEntity(proj);

        // эффект выстрела
        ParticleHelper.spawnParticles(
                level(),
                ParticleHelper.constructSimpleSpark(ColorScheme.BLOOD_COLOR, 0.3f, 20, 0.93f)
                        .withGravity(1f),
                from,
                12,
                0.2, 0.2, 0.2,
                0.02
        );
    }

    public double targetPriority(Entity e) {
        if (e instanceof GhostBigEntity || e instanceof GhostSmallEntity)
            return 0;

        if (e instanceof LivingEntity lE
                && lE.getLastDamageSource() != null
                && lE.getLastDamageSource().getEntity() instanceof Player p
                && p.getStringUUID().equals(this.getOwnerUUID())) return -200;

        if (getTarget() != null && getTarget().getUUID().equals(e.getUUID())) {
            if (getTarget() instanceof Creeper creeper && creeper.getSwelling(0) > 0)
                return 0;

            if (getTarget().hasEffect(MobEffects.WITHER))
                return 50;

            return -150;
        }

        if (e instanceof Mob m
                && Objects.equals(m.getTarget() == null ? null : m.getTarget().getStringUUID(), this.getOwnerUUID()))
            return -100;

        return 0;
    }

    @Override
    public float getScale() {
        return entityData.get(DATA_SCALE);
    }

    public GhostBigEntity setScale(float scale, float maxHP) {
        entityData.set(DATA_SCALE, scale);
        setMaxHealth(maxHP);
        refreshDimensions();
        return this;
    }

    public GhostBigEntity setStats(ItemKnefRose.RoseStats stats) {
        entityData.set(DATA_STATS, stats);
        return this;
    }

    public ItemKnefRose.RoseStats getStats() {
        return entityData.get(DATA_STATS);
    }

    public String getOwnerUUID() {
        return this.getEntityData().get(OWNER_UUID);
    }

    public void setOwnerUUID(String uuid) {
        this.getEntityData().set(OWNER_UUID, uuid);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SCALE, 1F);
        builder.define(DATA_STATS, new ItemKnefRose.RoseStats(null, ItemStack.EMPTY));
        builder.define(OWNER_UUID, "");
    }

    @Override
    public ConfiguredAnimation getWalkingAnimation() {
        return AnimationsRegistry.GHOST_BIG_WALK.configure();
    }

    @Override
    public ConfiguredAnimation getIdleAnimation() {
        return AnimationsRegistry.GHOST_BIG_IDLE.configure();
    }

    public ConfiguredAnimation getAttackAnimation() {
        return AnimationsRegistry.GHOST_BIG_ATTACK.configure()
                .important().next(DefaultsHA.NULL_ANIM.configure());
    }

    @Override
    protected @NotNull FlyingPathNavigation createNavigation(@NotNull Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Scale", getScale());
        tag.putInt("Lifetime", lifetime);
        tag.put("Stats", getStats().serializeNBT(registryAccess()));
        tag.putString("OwnerUUID", getOwnerUUID());
        tag.putDouble("DamageModidier", damageModifier);
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        setScale(tag.getFloat("Scale"), this.getMaxHealth());
        this.lifetime = tag.getInt("Lifetime");
        setStats(new ItemKnefRose.RoseStats(this.registryAccess(), tag.getCompound("Stats")));
        setOwnerUUID(tag.getString("OwnerUUID"));
        this.damageModifier = tag.getDouble("DamageModidier");
        super.readAdditionalSaveData(tag);
    }
}