package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import it.hurts.sskirillss.relics.client.particles.circle.CircleTintData;
import it.hurts.sskirillss.relics.client.particles.spark.SparkTintData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

import java.awt.Color;
import java.util.List;
import java.util.Random;

public class FartCloudEntity extends Projectile {


    
    Random rng = new Random();
    int dmgCD = 0;
    public FartCloudEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(FartCloudEntity.class, EntityDataSerializers.INT);

    public void setLifeTime(int lifetime){
        this.getEntityData().set(LIFETIME, lifetime);
    }

    public int getLifeTime() {
        return this.getEntityData().get(LIFETIME);
    }
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(FartCloudEntity.class, EntityDataSerializers.FLOAT);

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
        if(this.tickCount > getLifeTime()) this.discard();
        float radius = getRadius();
        if(!this.getLevel().isClientSide()){
            ServerLevel level = (ServerLevel) this.getLevel();
            level.sendParticles(new CircleTintData(new Color(85 - rng.nextInt(80) + rng.nextInt(80), 255 - rng.nextInt(160), 0),
                            radius / 5.0f,80, 0.94F, false),
                    this.getX(), this.getY(), this.getZ(), Math.round(radius * radius * 2f), radius, radius, radius, 0.01 * radius);
            level.sendParticles(new SparkTintData(new Color(85 - rng.nextInt(80), 255 - rng.nextInt(160), 0), radius / 5.0f, 60),
                    this.getX(), this.getY(), this.getZ(), Math.round(radius * radius), radius, radius, radius, 0);
//            level.sendParticles(ParticleTypes.ENTITY_EFFECT,
//                    this.getX(), this.getY(), this.getZ(), (int) (radius * 2), radius, radius, radius, 0.01);

            AABB box = new AABB(this.getX(), this.getY(), this.getZ(),this.getX(), this.getY(), this.getZ()).inflate(radius);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box, e -> !(e instanceof Player));
            if(dmgCD == 0) {
                for (LivingEntity e : entities) {
                    e.hurt(DamageSource.MAGIC, (float) (2 + radius));
                    if(e.hasEffect(EffectsRegistry.POISSON)) {
                        if (e.getEffect(EffectsRegistry.POISSON).getAmplifier() < 5) {
                            e.addEffect(new MobEffectInstance(EffectsRegistry.POISSON, 60, e.getEffect(EffectsRegistry.POISSON).getAmplifier()+ 1, false, true, false));
                        }
                        if (e.getEffect(EffectsRegistry.POISSON).getAmplifier() == 5) {
                            e.addEffect(new MobEffectInstance(EffectsRegistry.POISSON, 60, 5, false, true, false));
                        }
                        if (e.getEffect(EffectsRegistry.POISSON).getAmplifier() > 5) {
                            e.addEffect(new MobEffectInstance(EffectsRegistry.POISSON, 60, e.getEffect(EffectsRegistry.POISSON).getAmplifier(), false, true, false));
                        }
                    }
                    else e.addEffect(new MobEffectInstance(EffectsRegistry.POISSON, 60, 0,false, true, false));
                }
                dmgCD = 20;
            }
            if(dmgCD > 0) dmgCD--;
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
    protected void defineSynchedData() {

        this.entityData.define(RADIUS, 5F);
        this.entityData.define(LIFETIME, 20);
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

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
