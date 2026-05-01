package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WaveEntity extends Entity {

    private final float MAX_RADIUS = 10f;
    private final float SPEED = 0.6f;

    private UUID ownerUUID;
    private LivingEntity owner;

    public WaveEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    public WaveEntity(Level level, LivingEntity owner) {
        this(EntityRegistry.WAVE, level);
        this.owner = owner;
        this.ownerUUID = owner.getUUID();
        this.setPos(owner.position()); // фиксируем центр здесь
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner"))
            ownerUUID = tag.getUUID("Owner");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null)
            tag.putUUID("Owner", ownerUUID);
    }

    @Nullable
    public LivingEntity getOwner() {
        if (owner == null && ownerUUID != null && level() instanceof ServerLevel server) {
            Entity e = server.getEntity(ownerUUID);
            if (e instanceof LivingEntity living)
                owner = living;
        }
        return owner;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            tickWave();
        } else {
            spawnParticles();
        }

        if (tickCount >= getMaxLife()) {
            discard();
        }
    }

    float getMaxLife() {
        return MAX_RADIUS / SPEED;
    }

    float getRadius(int tickCount) {
        return tickCount / getMaxLife() * MAX_RADIUS;
    }

    private void tickWave() {
        if (tickCount == 1) {
            releaseWave();
        }

        double radius = getRadius(tickCount + 1);
        double innerRadius = getRadius(tickCount - 2);
        Vec3 center = position();

        AABB box = new AABB(
                -radius, -radius, -radius,
                radius, radius, radius
        ).move(center);

        List<Entity> targets = new ArrayList<>();
        targets.addAll(level().getEntitiesOfClass(LivingEntity.class, box));
        targets.addAll(level().getEntitiesOfClass(ItemEntity.class, box));
        targets.addAll(level().getEntitiesOfClass(ExperienceOrb.class, box));

        LivingEntity owner = getOwner();

         for (Entity e : targets) {

            if (e.getUUID().equals(ownerUUID)) continue;

            if (e instanceof LivingEntity le && !le.isPushable())
                continue;

            if (e.position().distanceToSqr(center) > radius * radius)
                continue;

            if (owner != null && e instanceof LivingEntity living) {
                if (owner.isAlliedTo(living)) continue;
            }

            Vec3 sub = e.position().subtract(center);
            Vec3 dir = sub.normalize();
            double l = sub.length();

            if (l < innerRadius)
                continue;

            Vec3 velocity = dir.scale(SPEED * (l - innerRadius))
                    .add(0, 0.2 * SPEED * (l - innerRadius), 0);

            if (e instanceof ItemEntity || e instanceof ExperienceOrb)
                velocity = velocity.scale(0.2);

            e.setDeltaMovement(velocity);

            if (e instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        40,
                        3
                ));
            }
        }
    }

    private void releaseWave() {
        level().playSound(
                null,
                blockPosition(),
                SoundsRegistry.SELI_HORN_WAVE.get(),
                SoundSource.PLAYERS,
                1f,
                1f
        );

    }

    private void spawnParticles() {
        if (!level().isClientSide) return;
        var rng = level().getRandom();

        if (tickCount == 1) {
            double angle = Math.PI * 2 / 60;
            for (float g = -0.5f; g <= 0.5; g += 0.25f) {
                for (int i = 0; i <= 60; i++) {
                    double vangle = angle * i + (g * angle);
                    double x = Math.sin(vangle);
                    double y = Math.cos(vangle);


                    float md = (1 - 2.5f / Math.abs(g)) * 0.9f;
                    Vec3 dir = new Vec3(x * md, g, y * md);
                    Vec3 ppos = position().add(dir.scale(0.3)).add(0, 0.2, 0);

                    Vec3 speed = dir.normalize().scale(SPEED);
                    for (int k = 0; k <= 3; k++) {
                        Vec3 ppos1 = ppos.add(
                                rng.nextFloat() * 0.5 - 0.25,
                                rng.nextFloat() * 0.5 - 0.25,
                                rng.nextFloat() * 0.5 - 0.25
                        );
                        ParticleHelper.spawnDirectedParticle(level(), ParticleTypes.CLOUD, ppos1.x, ppos1.y, ppos1.z, speed.x, speed.y, speed.z);
                    }

                }
            }
        }

        double r = getRadius(tickCount - 1);
        int count = (int) Math.round(2 * Math.PI * r * SPEED * SPEED * 6);
        double r2 = r + 0.270;
        int count2 = (int) Math.round(2 * Math.PI * r2 * SPEED * SPEED * 6);
        if (getMaxLife() - tickCount <= 3)
            for (int j = 0; j < count * 2; j++) {
                Vec3 pos = position().add(new Vec3(r, 0, 0).yRot((float) Math.toRadians(360.0 / count * j)));
                Vec3 sped = pos.subtract(position().add(new Vec3(0, 0.3, 0))).normalize().scale(0.11);


                ParticleHelper.spawnDirectedParticle(level(), ParticleTypes.CLOUD,
                        pos.x, pos.y, pos.z, sped.scale(2).x, 0, sped.scale(2).z);
            }

        for (int j = 0; j < count2; j++) {
            Vec3 pos = position().add(new Vec3(r2 + rng.nextDouble() * 0.1, 0, 0).yRot((float) Math.toRadians(360.0 / count2 * j)));
            Vec3 sped = pos.subtract(position().add(new Vec3(0, 0.3, 0))).normalize().scale(0.11 * (1 - tickCount / 14f) + rng.nextDouble() * 0.03);
            ParticleHelper.spawnDirectedParticle(level(), ParticleHelper.constructSimpleSpark(FlamesUtils.fromRGBI(63, 60, 39), 0.3f + ((tickCount / 14f) * 0.2f), (int) Math.round(10 + r2 * 2), 0.9f),
                    pos.x + rng.nextDouble() * 0.4 - 0.2, pos.y + 0.2 + rng.nextDouble() * 0.2, pos.z + rng.nextDouble() * 0.4 - 0.2, sped.x, 0, sped.z);

        }
    }

}