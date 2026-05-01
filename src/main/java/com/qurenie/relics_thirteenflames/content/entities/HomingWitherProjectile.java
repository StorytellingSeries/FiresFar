package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.style.ColorScheme;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.qurenie.relics_thirteenflames.style.ColorScheme.*;

public class HomingWitherProjectile extends Projectile {
    private static final String NBT_DAMAGE = "Damage";
    private static final String NBT_LIFETIME = "Lifetime";
    private static final String NBT_AGE = "Age";

    private float damage = 6.0F;
    @Setter
    @Getter
    private int lifetime = 120;
    private int age = 0;

    private static final String NBT_TARGET = "Target";
    private UUID targetUUID;

    private static final String NBT_OWNER = "RealOwner";
    private UUID ownerUUID;

    LivingEntity target;

    public HomingWitherProjectile(EntityType<? extends HomingWitherProjectile> type, Level level) {
        super(type, level);
    }

    public HomingWitherProjectile(Level level,
                                  LivingEntity owner,
                                  LivingEntity target,
                                  Vec3 position,
                                  float damage,
                                  int lifetime) {
        this(EntityRegistry.WITHER_PROJ, level);
        this.setOwner(owner);
        this.target = target;

        // 🔥 ключевая строка
        if (owner instanceof GhostBigEntity ghost) {
            try {
                this.ownerUUID = UUID.fromString(ghost.getOwnerUUID());
            } catch (Exception ignored) {}
        } else {
            this.ownerUUID = owner.getUUID();
        }

        this.setPos(position.x, position.y, position.z);
        this.damage = damage;
        this.lifetime = lifetime;
    }

    public float getDamageAmount() {
        return this.damage;
    }

    public void setDamageAmount(float damage) {
        this.damage = damage;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Пока не нужно синхронизировать ничего дополнительно.
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag input) {
        this.damage = input.getFloat(NBT_DAMAGE);
        this.lifetime = input.getInt(NBT_LIFETIME);
        this.age = input.getInt(NBT_AGE);

        if (input.hasUUID(NBT_TARGET)) {
            targetUUID = input.getUUID(NBT_TARGET);
        }

        if (input.hasUUID(NBT_OWNER)) {
            ownerUUID = input.getUUID(NBT_OWNER);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag output) {
        output.putFloat(NBT_DAMAGE, this.damage);
        output.putInt(NBT_LIFETIME, this.lifetime);
        output.putInt(NBT_AGE, this.age);

        if (target != null) {
            output.putUUID(NBT_TARGET, target.getUUID());
        }

        if (ownerUUID != null) {
            output.putUUID(NBT_OWNER, ownerUUID);
        }
    }

    @Override
    public void tick() {
        super.tick();


        if (this.level().isClientSide) {
            Vec3 current = this.position();
            Vec3 previous = new Vec3(this.xo, this.yo, this.zo);

            ParticleHelper.spawnParticleLine(
                    this.level(),
                    ParticleHelper.constructSimpleSpark(BLOOD_COLOR, 0.23f, 10, 0.84f),
                    current,
                    previous,
                    1,
                    0.03,
                    0.05
            );

            ParticleHelper.spawnParticleLine(
                    this.level(),
                    ParticleHelper.constructSmoke(ColorScheme.GRAY_COLOR, 0.2f, 14, 0.3f).withLightning(false),
                    current,
                    previous,
                    5,
                    0.01,
                    0.05
            );


            return;
        }

        if (target == null && targetUUID != null && this.level() instanceof ServerLevel sl) {
            Entity e = sl.getEntity(targetUUID);
            if (e instanceof LivingEntity le) {
                target = le;
            }
        }

        if (++this.age > this.lifetime) {
            this.discard();
            return;
        }

        if (this.target != null && this.target.isAlive()) {
            this.homeTo(this.target);
        } else {
            // fallback — летим прямо
            Vec3 motion = this.getDeltaMovement();
            if (motion.lengthSqr() < 0.0001D) {
                this.discard();
            }
        }

        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);

        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
        }

        Vec3 motion = this.getDeltaMovement();
        this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);

        double speed = motion.length();
        if (speed < 0.08D) {
            Vec3 look = this.getLookAngle();
            if (look.lengthSqr() > 0.0D) {
                this.setDeltaMovement(look.scale(0.08D));
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity)) return false;

        // не бьём прямого владельца
        if (entity == this.getOwner()) return false;

        if (entity instanceof GhostBigEntity || entity instanceof GhostSmallEntity) return false;

        // не бьём "реального владельца" (игрока)
        return !entity.getUUID().equals(ownerUUID);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity target = hitResult.getEntity();
        Entity realOwner = null;

        if (ownerUUID != null && this.level() instanceof ServerLevel sl) {
            realOwner = sl.getEntity(ownerUUID);
        }

        boolean damaged = target.hurt(
                realOwner != null
                        ? this.damageSources().indirectMagic(this, realOwner)
                        : this.damageSources().wither(),
                this.damage
        );

        if (damaged && target instanceof LivingEntity livingTarget) {
            livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 50, 1));
        }

        this.spawnImpact(serverLevel, hitResult.getLocation());
        this.discard();
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);

        if (this.level() instanceof ServerLevel serverLevel) {
            this.spawnImpact(serverLevel, result.getLocation());
        }

        this.discard();
    }

    private void homeTo(LivingEntity target) {
        Vec3 from = this.position();
        Vec3 to = target.getEyePosition().subtract(from);

        if (to.lengthSqr() < 0.0001D) {
            return;
        }

        Vec3 desired = to.normalize().scale(0.42D);
        Vec3 current = this.getDeltaMovement();

        Vec3 blended = current.scale(0.82D).add(desired.scale(0.18D));

        if (blended.lengthSqr() < 0.0001D) {
            blended = desired;
        }

        blended = blended.normalize().scale(Math.min(Math.max(current.length(), 0.22D), 0.65D));

        this.setDeltaMovement(blended);

        this.setYRot((float) (Mth.atan2(blended.x, blended.z) * Mth.RAD_TO_DEG));
        this.setXRot((float) (Mth.atan2(blended.y, blended.horizontalDistance()) * Mth.RAD_TO_DEG));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    private LivingEntity findTarget(double radius) {
        AABB searchBox = this.getBoundingBox().inflate(radius);

        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;

        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                living -> living.isAlive() && living != this.getOwner() && !living.isSpectator())) {
            double dist = candidate.distanceToSqr(this);
            if (dist < bestDist) {
                bestDist = dist;
                best = candidate;
            }
        }

        return best;
    }

    private void spawnImpact(ServerLevel level, Vec3 pos) {
        ParticleHelper.spawnRandomJaggedParticleLine(
                level,
                this.position(),
                pos,
                0.18D,
                ParticleHelper.constructSmoke(GRAY_COLOR, 0.20f, 20, 0.0f).withLightning(false),
                10,
                3
        );

        ParticleHelper.spawnParticles(
                level,
                ParticleHelper.constructSimpleSpark(BLOOD_COLOR, 0.34f, 20, 0.95f),
                pos,
                12,
                0.18D,
                0.18D,
                0.18D,
                0.1D
        );
    }
}