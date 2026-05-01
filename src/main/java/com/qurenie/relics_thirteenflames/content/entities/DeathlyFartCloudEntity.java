package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.content.effects.PoisonEffectInstance;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import com.qurenie.relics_thirteenflames.style.ColorScheme;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class DeathlyFartCloudEntity extends Projectile {



    Random rng = new Random();
    int dmgCD = 0;
    public DeathlyFartCloudEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(DeathlyFartCloudEntity.class, EntityDataSerializers.INT);

    public void setLifeTime(int lifetime){
        this.getEntityData().set(LIFETIME, lifetime);
    }

    public int getLifeTime() {
        return this.getEntityData().get(LIFETIME);
    }
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(DeathlyFartCloudEntity.class, EntityDataSerializers.FLOAT);

    public float getRadius() {
        return this.getEntityData().get(RADIUS);
    }

    public void setRadius(float radius) {
        this.getEntityData().set(RADIUS, radius);
    }

    @Override
    public boolean isAlwaysTicking() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if(this.tickCount > getLifeTime() + 15) this.discard();
        float radius = this.tickCount < 15 ? getRadius() * this.tickCount / 10.0f : getRadius() * (1 - (float) this.tickCount / getLifeTime());
        AABB box = new AABB(this.getPosition(1), this.getPosition(1)).inflate(radius, radius / 2.5, radius);
        if(this.level() instanceof ServerLevel) {

            ParticleHelper.spawnParticleAABB(this.level(), ParticleHelper.constructSimpleSpark(ColorScheme.BLOOD_COLOR,
                    radius / 6.2f + 0.15f, 30, 0.84F), box.inflate(-0.3), Math.round(radius * radius / 2) + 1, 0.01 * radius);

            ParticleHelper.spawnParticleAABB(this.level(),
                    ParticleTypes.SMOKE,
                    box.inflate(2), Math.round(radius * radius * 2f) + 2, 0);

            if (this.tickCount % 3 > 0) ParticleHelper.spawnEnginedParticles(this.level(),
                    ParticleHelper.constructSmoke(FlamesUtils.withAlpha(ColorScheme.GRAY_COLOR, 0.6f), 1.2f, level().random.nextInt(30) + 30).withLightning(false).withGravity(0.2f),
                    box.getCenter(), Math.round(radius * radius * 1.4f), box.getXsize(), box.getYsize(), box.getZsize(), 0.02, 1.2f, level().random.nextInt(30)+ 20,
                    ColorScheme.GRAY_COLOR, 0.6f);

            List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, box.inflate(box.getYsize() * 0.5f), e -> this.getOwner() != null && !e.getUUID().equals(this.getOwner().getUUID())
                    && !(e instanceof LivingFleshEntity)
                    && !(e instanceof GhostSmallEntity));

            if (dmgCD == 0) {
                for (LivingEntity e : entities) {
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1, false, true, true));
                }
                dmgCD = 20;
            }
            if (dmgCD > 0) dmgCD--;
        }
    }

    @Override
    public boolean canCollideWith(Entity p_20303_) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 5F);
        builder.define(LIFETIME, 20);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setRadius(compound.getFloat("radius"));
        setLifeTime(compound.getInt("lifetime"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("radius", getRadius());
        compound.putInt("lifetime", getLifeTime());
    }

}
