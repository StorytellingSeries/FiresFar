package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.style.ColorScheme;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.UUID;

import static com.qurenie.relics_thirteenflames.style.ColorScheme.BLOOD_COLOR;
import static com.qurenie.relics_thirteenflames.style.ColorScheme.GRAY_COLOR;

public class ShadowMassEntity extends Projectile
{
    @Setter
    @Getter
    private float damage = 4F;

    @Setter
    @Getter
    private int lifetime = 80;

    private UUID ownerUUID;
    private LivingEntity target;

    public ShadowMassEntity(EntityType<? extends ShadowMassEntity> type, Level level)
    {
        super(type, level);
    }

    public ShadowMassEntity(Level level, LivingEntity owner)
    {
        this(EntityRegistry.SHADOW_MASS, level);

        this.setOwner(owner);

        if(owner instanceof GhostBigEntity ghost)
        {
            try
            {
                this.ownerUUID = UUID.fromString(ghost.getOwnerUUID());
            }
            catch(Exception ignored)
            {
            }
        } else
        {
            this.ownerUUID = owner.getUUID();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder)
    {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag)
    {
        damage = tag.getFloat("Damage");
        lifetime = tag.getInt("Lifetime");

        if(tag.hasUUID("Owner"))
        {
            ownerUUID = tag.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag)
    {
        tag.putFloat("Damage", damage);
        tag.putInt("Lifetime", lifetime);

        if(ownerUUID != null)
        {
            tag.putUUID("Owner", ownerUUID);
        }
    }

    @Override
    public void tick()
    {
        super.tick();

        if(level().isClientSide)
        {
            renderParticles();
            return;
        }

        if(tickCount > lifetime)
        {
            explode();
            return;
        }

        if(target == null || !target.isAlive())
        {
            target = findTarget();
        }

        if(target != null)
        {
            homeTo(target);
        }

        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);

        if(hit.getType() != HitResult.Type.MISS)
        {
            onHit(hit);
        }

        Vec3 motion = getDeltaMovement();

        setPos(
                getX() + motion.x,
                getY() + motion.y,
                getZ() + motion.z
        );
    }

    private void renderParticles()
    {
        Vec3 current = this.position();
        Vec3 previous = new Vec3(this.xo, this.yo, this.zo);

        ParticleHelper.spawnParticleLine(
                this.level(),
                ParticleHelper.constructSmoke(
                        GRAY_COLOR,
                        0.7f,
                        18,
                        0.1f
                ).withLightning(false),
                current,
                previous,
                5,
                0.03,
                0.2
        );

        ParticleHelper.spawnParticleLine(
                this.level(),
                ParticleHelper.constructSimpleSpark(
                        BLOOD_COLOR,
                        0.5f,
                        10,
                        0.92f
                ),
                current,
                previous,
                2,
                0.02,
                0.2
        );
    }

    private void homeTo(LivingEntity target)
    {
        Vec3 to = target.getEyePosition().subtract(position());

        if(to.lengthSqr() < 0.001)
            return;

        Vec3 desired = to.normalize().scale(0.45);

        Vec3 blended = getDeltaMovement()
                .scale(0.85)
                .add(desired.scale(0.15));

        blended = blended.normalize().scale(0.45);

        setDeltaMovement(blended);
    }

    private LivingEntity findTarget()
    {
        return level().getEntitiesOfClass(
                        LivingEntity.class,
                        getBoundingBox().inflate(20),
                        e ->
                                e.isAlive()
                                        && e != getOwner()
                                        && !(e instanceof GhostBigEntity)
                                        && !(e instanceof GhostSmallEntity)
                                        && (e instanceof Enemy)
                ).stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(this)))
                .orElse(null);
    }

    @Override
    protected boolean canHitEntity(@NotNull Entity entity)
    {
        if(!super.canHitEntity(entity))
            return false;

        if(entity == getOwner())
            return false;

        if(entity instanceof GhostBigEntity
                || entity instanceof GhostSmallEntity)
            return false;

        return !entity.getUUID().equals(ownerUUID);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result)
    {
        super.onHitEntity(result);

        Entity entity = result.getEntity();

        Entity realOwner = null;

        if(ownerUUID != null && level() instanceof ServerLevel sl)
        {
            realOwner = sl.getEntity(ownerUUID);
        }

        entity.hurt(
                realOwner instanceof LivingEntity living
                        ? damageSources().indirectMagic(this, living)
                        : damageSources().magic(),
                damage
        );

        explode();
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result)
    {
        super.onHitBlock(result);

        explode();
    }

    private void explode()
    {
        if(!(level() instanceof ServerLevel server))
        {
            discard();
            return;
        }

        Vec3 pos = position();

        // =========================================
        // ЗВУК
        // =========================================

        level().playSound(
                null,
                pos.x,
                pos.y,
                pos.z,
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE,
                1.6F,
                0.65F + random.nextFloat() * 0.15F
        );

        // =========================================
        // ГЛАВНЫЙ ВЗРЫВ
        // =========================================

        ParticleHelper.spawnParticles(
                server,
                ParticleHelper.constructSimpleSpark(
                        BLOOD_COLOR,
                        0.4f,
                        60,
                        0.96f
                ).withGravity(0.7f),
                pos,
                45,
                0.45,
                0.45,
                0.45,
                0.15
        );

        // =========================================
        // ТЁМНОЕ ОБЛАКО
        // =========================================

        ParticleHelper.spawnParticles(
                server,
                ParticleHelper.constructSmoke(
                                ColorScheme.GRAY_COLOR,
                                1.2f,
                                70,
                                0.15f
                        )
                        .withLightning(false)
                        .withGravity(-0.01f),
                pos,
                40,
                0.55,
                0.55,
                0.55,
                0.012
        );

        // =========================================
        // КОЛЬЦО ИСКР
        // =========================================

        for(int i = 0; i < 20; i++)
        {
            double angle = (Math.PI * 2D / 20D) * i;

            Vec3 dir = new Vec3(
                    Math.cos(angle),
                    (random.nextDouble() - 0.5) * 0.2,
                    Math.sin(angle)
            );

            ParticleHelper.spawnParticleLine(
                    server,
                    ParticleHelper.constructSimpleSpark(
                            BLOOD_COLOR,
                            0.45f,
                            16,
                            0.90f
                    ),
                    pos,
                    pos.add(dir.scale(3.5)),
                    5,
                    0.02,
                    0.08
            );
        }

        // =========================================
        // ВАНИЛЬНЫЕ ПАРТИКЛЫ ВЗРЫВА
        // =========================================

        server.sendParticles(
                ParticleTypes.EXPLOSION,
                pos.x,
                pos.y,
                pos.z,
                4,
                0.1,
                0.1,
                0.1,
                0.0
        );

        server.sendParticles(
                ParticleTypes.POOF,
                pos.x,
                pos.y,
                pos.z,
                25,
                0.5,
                0.5,
                0.5,
                0.08
        );

        // =========================================
        // УРОН
        // =========================================

        float explosionDamage = damage * 4F;

        AABB hitbox = new AABB(pos, pos).inflate(3.0);

        for(LivingEntity living : server.getEntitiesOfClass(
                LivingEntity.class,
                hitbox,
                e ->
                        e.isAlive()
                                && e != getOwner()
                                && !(e instanceof GhostBigEntity)
                                && !(e instanceof GhostSmallEntity)
        ))
        {
            double dist = living.distanceToSqr(pos);

            float multiplier = (float) Mth.clamp(
                    1.0 - (dist / 9.0),
                    0.15,
                    1.0
            );

            float finalDamage = explosionDamage * multiplier;

            Entity realOwner = ownerUUID != null
                    ? server.getEntity(ownerUUID)
                    : null;

            living.hurt(
                    realOwner instanceof LivingEntity owner
                            ? damageSources().indirectMagic(this, owner)
                            : damageSources().magic(),
                    finalDamage
            );

            Vec3 knockback = living.position()
                    .subtract(pos)
                    .normalize()
                    .scale(0.7);

            living.push(
                    knockback.x,
                    0.25,
                    knockback.z
            );
        }

        // =========================================
        // ОБЛАКО
        // =========================================

        DeathlyFartCloudEntity cloud =
                new DeathlyFartCloudEntity(EntityRegistry.DEATHCLOUD, level());

        cloud.setRadius(3.0F);
        cloud.setLifeTime(140);
        cloud.setPos(pos);

        try
        {
            if(ownerUUID != null)
            {
                Entity owner = server.getEntity(ownerUUID);

                if(owner instanceof LivingEntity living)
                {
                    cloud.setOwner(living);
                }
            }
        }
        catch(Exception ignored)
        {
        }

        level().addFreshEntity(cloud);

        discard();
    }
}