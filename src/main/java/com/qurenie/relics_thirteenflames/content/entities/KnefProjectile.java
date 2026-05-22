package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.client.particles.CircleTintParticle;
import com.qurenie.relics_thirteenflames.content.items.ItemKnefBow;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.octostudios.octolib.util.OctoColor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class KnefProjectile extends ThrowableProjectile {

    static OctoColor[] colors = {
            FlamesUtils.fromRGBI(43, 210, 159),
            FlamesUtils.fromRGBI(88, 208, 155),
            FlamesUtils.fromRGBI(49, 203, 138),
            FlamesUtils.fromRGBI(77, 197, 142),
            FlamesUtils.fromRGBI(109, 199, 161),
            FlamesUtils.fromRGBI(102, 204, 172)
    };

    public Vec3 prevPos;
    public LivingEntity target;

    public OctoColor color;

    static Random rng = new Random();

    @Getter
    @Setter
    private ItemStack bow = ItemStack.EMPTY;

    private static final EntityDataAccessor<Integer> POWER_ENCH = SynchedEntityData.defineId(KnefProjectile.class, EntityDataSerializers.INT);


    public void setPowerEnch(int powerEnch) {
        this.getEntityData().set(POWER_ENCH, powerEnch);
    }

    public int getPowerEnch() {
        return this.getEntityData().get(POWER_ENCH);
    }

    private static final EntityDataAccessor<Float> BASE_DMG = SynchedEntityData.defineId(KnefProjectile.class, EntityDataSerializers.FLOAT);

    public void setBaseDmg(float baseDmg) {
        this.getEntityData().set(BASE_DMG, baseDmg);
    }

    public float getBaseDmg() {
        return this.getEntityData().get(BASE_DMG);
    }

    private static final EntityDataAccessor<Integer> PARTICLE_COUNT = SynchedEntityData.defineId(KnefProjectile.class, EntityDataSerializers.INT);

    public void setParticleCount(int particles) {
        this.getEntityData().set(PARTICLE_COUNT, particles);
    }

    public int getParticleCount() {
        return this.getEntityData().get(PARTICLE_COUNT);
    }

    private static final EntityDataAccessor<Boolean> FREE = SynchedEntityData.defineId(KnefProjectile.class, EntityDataSerializers.BOOLEAN);

    public boolean isFree() {
        return this.getEntityData().get(FREE);
    }

    public void setFree(boolean free) {
        this.getEntityData().set(FREE, free);
    }

    private static final EntityDataAccessor<String> OWNER_UUID = SynchedEntityData.defineId(KnefProjectile.class, EntityDataSerializers.STRING);

    public void setOwnerUUID(String uuid) {
        this.getEntityData().set(OWNER_UUID, uuid);
    }

    public String getOwnerUUID() {
        return this.getEntityData().get(OWNER_UUID);
    }

    private static final EntityDataAccessor<Boolean> BOUNCE = SynchedEntityData.defineId(KnefProjectile.class, EntityDataSerializers.BOOLEAN);

    public boolean isBounce() {
        return this.getEntityData().get(BOUNCE);
    }

    public void setBounce(boolean free) {
        this.getEntityData().set(BOUNCE, free);
    }

    private static final EntityDataAccessor<Boolean> HAS_GRAVITY = SynchedEntityData.defineId(KnefProjectile.class, EntityDataSerializers.BOOLEAN);

    public boolean hasGravity() {
        return this.getEntityData().get(HAS_GRAVITY);
    }

    public void setHasGravity(boolean free) {
        this.getEntityData().set(HAS_GRAVITY, free);
    }

    public KnefProjectile(EntityType<? extends KnefProjectile> type, Level world) {
        super(type, world);
        //this.color = new Color(0, 246 - this.random.nextInt(100), 255 - this.random.nextInt(120));
        this.color = colors[rng.nextInt(colors.length)];
    }

    @Override
    protected void applyGravity() {
        if (this.hasGravity())
            this.addDeltaMovement(new Vec3(0, -getGravity(), 0));
    }

    @Override
    public void tick() {
        if (this.getOwnerUUID().isEmpty()) this.discard();
        if (prevPos == null) prevPos = this.position();

        if (level().isClientSide) {
            double distance = this.position().subtract(prevPos == null ? this.position() : prevPos).length();
            ParticleHelper.spawnParticleLine(this.level(), ParticleHelper.constructSimpleSpark(color, 0.1f, 35, 0.89f),
                    prevPos, this.position(), (int) Math.round(distance * getParticleCount()), 0.001);
        }

        this.prevPos = position();
        super.tick();

        if (isFree()) {
            if (this.target != null && target.hasLineOfSight(this) && this.target.isAlive()) {
                final float acceleration = 0.2f;
                Vec3 vec = target.getBoundingBox().getCenter().subtract(this.position());
                this.addDeltaMovement(vec.normalize().scale(acceleration));
                this.setDeltaMovement(getDeltaMovement().scale(0.94f));
            } else if (target == null || !target.isAlive()) {
                List<LivingEntity> targets = new ArrayList<>(this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(7), e -> !(e.getStringUUID().equals(this.getOwnerUUID())) && e.hasLineOfSight(this)));
                if (!targets.isEmpty()) this.target = targets.get(rng.nextInt(targets.size()));
            }
        }

    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        if (!this.level().isClientSide()) {
            Entity owner = ((ServerLevel) this.level()).getEntity(UUID.fromString(this.getOwnerUUID()));
            if (result.getEntity().getStringUUID().equals(this.getOwnerUUID())) return;


            if (result.getEntity().hurt(this.damageSources().thrown(this, owner), getBaseDmg() + getPowerEnch() / 2f)) {

                ParticleHelper.spawnParticles(this.level(), ParticleHelper.constructSimpleSpark(colors[rng.nextInt(colors.length)], 0.1f, 11, 0.8f),
                        this.getPosition(1), 10, 0, 0, 0, 0.08);
                ParticleHelper.spawnParticles(this.level(), ParticleHelper.constructSimpleSpark(colors[rng.nextInt(colors.length)], 0.1f, 11, 0.8f),
                        this.getPosition(1), 10, 0, 0, 0, 0.08);

                if (this.bow.getItem() instanceof ItemKnefBow relic && owner instanceof LivingEntity livin) {
                    relic.addExperience(livin, bow, 1);
                }

                result.getEntity().invulnerableTime = 0;

                if (owner != null) {
                    float vol = (float) (10 / owner.distanceToSqr(this.position()));
                    this.level().playSound(null, owner, SoundsRegistry.KNEF_BOW_SPLASH.get(), SoundSource.PLAYERS, random.nextFloat() * 0.05f * vol + vol, random.nextFloat() * 0.1f + 0.6f);
                }
                this.discard();
            }
        }
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult pResult) {
        setFree(true);

        HitResult result = this.level().clip(new ClipContext(this.position(), this.position().add(this.getDeltaMovement().normalize()),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        if (!this.level().isClientSide()) {
            ParticleHelper.spawnParticleLine(this.level(), ParticleHelper.constructSimpleSpark(color, 0.1f, 80, 0.9f),
                    this.position(), result.getLocation(), (int) Math.round(Math.sqrt(this.position().distanceToSqr(result.getLocation())) * 10), 0.001);

            ParticleHelper.spawnParticles(this.level(), ParticleHelper.constructSimpleSpark(colors[rng.nextInt(colors.length)], 0.1f, 11, 0.8f),
                    result.getLocation().x(), result.getLocation().y(), result.getLocation().z(), 10, 0, 0, 0, 0.08);
            ParticleHelper.spawnParticles(this.level(), ParticleHelper.constructSimpleSpark(colors[rng.nextInt(colors.length)], 0.1f, 11, 0.8f),
                    result.getLocation().x(), result.getLocation().y(), result.getLocation().z(), 10, 0, 0, 0, 0.08);

            Entity owner = ((ServerLevel) this.level()).getEntity(UUID.fromString(this.getOwnerUUID()));
            float vol = owner == null ? 10 : (float) (10 / owner.distanceToSqr(this.position()));
            this.level().playSound(null, owner == null ? this : owner, SoundsRegistry.KNEF_BOW_SPLASH.get(), SoundSource.PLAYERS, random.nextFloat() * 0.05f * vol + vol, random.nextFloat() * 0.1f + 0.6f);
        }

        if (isBounce() && getDeltaMovement().lengthSqr() > 0.06) {
            Direction face = pResult.getDirection();
            Vec3 motion = this.getDeltaMovement();

            double x = motion.x;
            double y = motion.y;
            double z = motion.z;

            // базовое отражение
            switch (face.getAxis()) {
                case X -> x = -x;
                case Y -> y = -y;
                case Z -> z = -z;
            }

            Vec3 reflected = new Vec3(x, y, z);

            // --- ДОБАВЛЯЕМ НЕТOЧНОСТЬ ---
            double inaccuracy = 0.3; // 30%

            Vec3 randomOffset = new Vec3(
                    (rng.nextDouble() - 0.5),
                    (rng.nextDouble() - 0.5),
                    (rng.nextDouble() - 0.5)
            ).normalize().scale(reflected.length() * inaccuracy);

            reflected = reflected.add(randomOffset).normalize().scale(reflected.length());

            // небольшая потеря энергии
            reflected = reflected.scale(0.82);

            // смещение от поверхности, чтобы не залипало
            Vec3 offset = Vec3.atLowerCornerOf(face.getNormal()).scale(0.1);

            this.setPos(
                    pResult.getLocation().x + offset.x,
                    pResult.getLocation().y + offset.y,
                    pResult.getLocation().z + offset.z
            );

            this.setDeltaMovement(reflected);
            this.hasImpulse = true;
            this.prevPos = this.position();
            this.setHasGravity(true);
        }
        else
            this.discard();
    }

    @Override
    public void checkDespawn() {
        if (this.tickCount > 240) {
            this.discard();
        }
    }

    public static List<KnefProjectile> makeList(int count, Level level, @NotNull Entity owner, Vec3 center, Vec3 move, float baseDmg, int powerEnch, ItemStack bow) {
        List<KnefProjectile> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            KnefProjectile proj = new KnefProjectile(EntityRegistry.KNEF_PROJECTILE, level);
            proj.setOwner(owner);
            proj.setOwnerUUID(owner.getStringUUID());
            proj.setPos(center);
            proj.setBaseDmg(baseDmg);
            proj.setDeltaMovement(move);
            //proj.color = new Color(0, 246 - proj.random.nextInt(160), 255 - proj.random.nextInt(120));
            proj.color = colors[rng.nextInt(colors.length)];
            proj.setPowerEnch(powerEnch);
            proj.setBow(bow);
            proj.setParticleCount((i % 2 == 0 && count > 7) ? (i % 4 == 0 && count > 15) ? 3 : 12 : count <= 7 ? 12 : 6);
            list.add(proj);
            proj.setFree(false);
            proj.setBounce(ItemsRegistry.KNEF_BOW.hasRangModifier((LivingEntity) owner, bow, "shot", "bounce"));
        }
        return list;
    }

    @Override
    public boolean canCollideWith(Entity pEntity) {
        return (pEntity instanceof LivingEntity);
    }

    @SubscribeEvent
    public void onLevelUnload(PlayerEvent.PlayerLoggedOutEvent event) {
        this.discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(POWER_ENCH, 0);
        builder.define(BASE_DMG, 2f);
        builder.define(PARTICLE_COUNT, 12);
        builder.define(FREE, false);
        builder.define(BOUNCE, false);
        builder.define(HAS_GRAVITY, false);
        builder.define(OWNER_UUID, "");
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setPowerEnch(compound.getInt("powerench"));
        setBaseDmg(compound.getInt("basedmg"));
        setParticleCount(compound.getInt("particles"));
        setFree(compound.getBoolean("free"));
        setBounce(compound.getBoolean("bounce"));
        setHasGravity(compound.getBoolean("hasGravity"));
        setOwnerUUID(compound.getString("owneruuid"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("powerench", getPowerEnch());
        compound.putFloat("basedmg", getBaseDmg());
        compound.putInt("particles", getParticleCount());
        compound.putBoolean("free", isFree());
        compound.putBoolean("bounce", isBounce());
        compound.putBoolean("hasGravity", hasGravity());
        compound.putString("owneruuid", getOwnerUUID());
    }

    @Override
    public boolean shouldRender(double p_20296_, double p_20297_, double p_20298_) {
        return false;
    }

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return false;
    }

}
