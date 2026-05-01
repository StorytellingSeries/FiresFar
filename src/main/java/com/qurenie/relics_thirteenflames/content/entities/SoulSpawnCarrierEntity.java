package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.style.ColorScheme;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;

public class SoulSpawnCarrierEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<String> MOB_ID = SynchedEntityData.defineId(SoulSpawnCarrierEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> COST = SynchedEntityData.defineId(SoulSpawnCarrierEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(SoulSpawnCarrierEntity.class, EntityDataSerializers.INT);

    private Vec3 prevPos;

    public SoulSpawnCarrierEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public SoulSpawnCarrierEntity(Level level, Player owner, Vec3 startPos, Vec3 motion, String mobId, int cost, boolean noGravity) {
        this(EntityRegistry.MOB_CARRIER, level);
        this.setPos(startPos);
        this.setDeltaMovement(motion);
        this.entityData.set(MOB_ID, mobId);
        this.entityData.set(COST, cost);
        this.entityData.set(LIFETIME, 70 + level.random.nextInt(20));
        this.setOwner(owner);
        this.setNoGravity(noGravity);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        builder.define(MOB_ID, "");
        builder.define(COST, 1);
        builder.define(LIFETIME, 40);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.02f;
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);

        releaseMob();
        discard();
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);

        releaseMob();
        discard();
    }

    @Override
    public void tick() {
        if (prevPos == null) prevPos = position();

        super.tick();

        if (isNoGravity())
            this.setDeltaMovement(getDeltaMovement().scale(0.97));

        if (level().isClientSide) {
            ParticleHelper.spawnParticleLine(
                    level(),
                    ParticleHelper.constructSimpleSpark(ColorScheme.SOUL_COLOR, 0.5f, 7, 0.93f),
                    position(),
                    prevPos,
                    2,
                    () -> new Vec3(
                            level().random.nextGaussian() * 0.004,
                            0.01 + level().random.nextGaussian() * 0.004,
                            level().random.nextGaussian() * 0.004
                    ),
                    0
            );

            ParticleHelper.spawnParticleLine(
                    level(),
                    ParticleHelper.constructSimpleSpark(ColorScheme.GRAY_COLOR, 0.14f, 7, 0.93f).withLightning(false),
                    position(),
                    prevPos,
                    3,
                    () -> new Vec3(
                            level().random.nextGaussian() * 0.004,
                            0.01 + level().random.nextGaussian() * 0.004,
                            level().random.nextGaussian() * 0.004
                    ),
                    0
            );

            if (tickCount % 8 == 0)
                ParticleHelper.spawnParticles(level(), ParticleTypes.SOUL, position(), 1, 0.1, 0.1, 0.1, 0.04);

            if (tickCount % 5 == 0)
                ParticleHelper.spawnParticles(level(), ParticleTypes.SOUL_FIRE_FLAME, position(), 1, 0.1, 0.1, 0.1, 0.04);


            prevPos = position();
            return;
        }

        if (tickCount > entityData.get(LIFETIME)) {
            releaseMob();
            discard();
        }

    }

    private void releaseMob() {
        if (!(level() instanceof ServerLevel server)) return;

        ResourceLocation id = ResourceLocation.tryParse(entityData.get(MOB_ID));
        if (id == null) return;

        var opt = BuiltInRegistries.ENTITY_TYPE.getOptional(id);
        if (opt.isEmpty()) return;

        EntityType<?> rawType = opt.get();

        Entity entity = rawType.create(server);
        if (!(entity instanceof Mob mob)) return;

        mob.setPos(position());

        int cost = Math.max(1, entityData.get(COST));
        if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
            mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(cost);
        }

        mob.setHealth(Math.min(cost, mob.getMaxHealth()));
        mob.setPersistenceRequired();

        EventHooks.finalizeMobSpawn(mob, server, server.getCurrentDifficultyAt(blockPosition()), MobSpawnType.MOB_SUMMONED, null);

        server.addFreshEntity(mob);

        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSmoke(ColorScheme.SOUL_COLOR, (float) (mob.getBoundingBox().getSize() / 1.5), 50 + level().random.nextInt(10), 0),
                mob, 8, 0.03f);
        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(ColorScheme.GRAY_COLOR, (float) 0.3f, 50 + level().random.nextInt(10), 0.95f).withLightning(false).withGravity(0.4f),
                mob, 15, 0.08f);
        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSmoke(ColorScheme.GRAY_COLOR, (float) (mob.getBoundingBox().getSize() ), 50 + level().random.nextInt(10), 0).withGravity(0.2f).withLightning(false),
                mob, 8, 0.012f);
        ParticleHelper.spawnParticles(server, ParticleTypes.SOUL_FIRE_FLAME, position(), 20, 0.1, 0.1, 0.1, 0.1);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(MOB_ID, tag.getString("MobId"));
        entityData.set(COST, tag.getInt("Cost"));
        entityData.set(LIFETIME, tag.getInt("Life"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("MobId", entityData.get(MOB_ID));
        tag.putInt("Cost", entityData.get(COST));
        tag.putInt("Life", entityData.get(LIFETIME));
    }

}