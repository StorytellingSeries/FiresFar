package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.content.effects.PoisonEffectInstance;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

import java.awt.*;
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

    private static final EntityDataAccessor<Integer> MAX_AMP = SynchedEntityData.defineId(FartCloudEntity.class, EntityDataSerializers.INT);

    public void setMaxAmp(int maxAmp){
        this.getEntityData().set(MAX_AMP, maxAmp);
    }

    public int getMaxAmp() {
        return this.getEntityData().get(MAX_AMP);
    }

    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(FartCloudEntity.class, EntityDataSerializers.INT);

    public void setDuration(int duration){
        this.getEntityData().set(DURATION, duration);
    }

    public int getDuration() {
        return this.getEntityData().get(DURATION);
    }
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(FartCloudEntity.class, EntityDataSerializers.FLOAT);

    public float getRadius() {
        return this.getEntityData().get(RADIUS);
    }

    public void setRadius(float radius) {
        this.getEntityData().set(RADIUS, radius);
    }

    private ItemStack sword = ItemStack.EMPTY;

    public void setSword(ItemStack swort){
        this.sword = swort;
    }

    public ItemStack getSword(){
        return  sword;
    }

    @Override
    public boolean isAlwaysTicking() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if(this.tickCount > getLifeTime()) this.discard();
        float radius = getRadius() * (1 - (float) this.tickCount / getLifeTime());
        AABB box = new AABB(this.getPosition(1), this.getPosition(1)).inflate(radius);
        if(this.level() instanceof ServerLevel) {

            ParticleHelper.spawnParticleAABB(this.level(), ParticleUtils.constructSimpleSpark(new Color(55 + rng.nextInt(-50, 10), 175 - rng.nextInt(160), 0),
                    radius / 6.2f + 0.15f, 80, 0.94F), box, Math.round(radius * radius * 2f) + 2, 0.01 * radius);

            if (this.tickCount % 2 == 0) ParticleHelper.spawnParticleAABB(this.level(),
                    ParticleUtils.constructSimpleSpark(new Color(85 - rng.nextInt(80), 255 - rng.nextInt(160), 0), radius / 8.0f, 60, 0.94F),
                    box, Math.round(radius * radius) + 1, 0);


            //AABB box = new AABB(this.getX(), this.getY(), this.getZ(),this.getX(), this.getY(), this.getZ()).inflate(radius);
            List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, box, e -> this.getOwner() != null && !e.getUUID().equals(this.getOwner().getUUID()));

            if (dmgCD == 0) {
                for (LivingEntity e : entities) {
                    int invulTime = e.invulnerableTime;
                    e.hurt(e.level().damageSources().magic(), (float) (2 + radius));
                    e.invulnerableTime = invulTime;
                    int maxAmp = getMaxAmp();
                    int duration = getDuration();
                    if(getSword().getItem() instanceof IRelicItem relic) {
                        if (e.hasEffect(EffectsRegistry.POISSON)) {
                            int appliedAmplifier = e.getEffect(EffectsRegistry.POISSON).getAmplifier() + 1;
                            if (appliedAmplifier <= maxAmp) {
                                e.addEffect(new PoisonEffectInstance(EffectsRegistry.POISSON, duration + appliedAmplifier * 20, appliedAmplifier, false, true, true, getSword()));
                                if (rng.nextFloat() < 0.25f && this.getOwner() instanceof LivingEntity livin) relic.spreadExperience(livin, getSword(), 1);
                            } else {
                                e.addEffect(new PoisonEffectInstance(EffectsRegistry.POISSON, duration + maxAmp * 20, maxAmp, false, true, true, getSword()));
                            }
                        } else {
                            e.addEffect(new PoisonEffectInstance(EffectsRegistry.POISSON, duration, 0, false, true, true, getSword()));
                            if (rng.nextFloat() < 0.25f && this.getOwner() instanceof LivingEntity livin) relic.spreadExperience(livin, getSword(), 1);
                        }
                    }
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
    protected void defineSynchedData() {
        this.entityData.define(RADIUS, 5F);
        this.entityData.define(LIFETIME, 20);
        this.entityData.define(MAX_AMP, 0);
        this.entityData.define(DURATION, 2);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setRadius(compound.getFloat("radius"));
        setLifeTime(compound.getInt("lifetime"));
        setMaxAmp(compound.getInt("maxamp"));
        setDuration(compound.getInt("duration"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("radius", getRadius());
        compound.putInt("lifetime", getLifeTime());
        compound.putInt("maxamp", getMaxAmp());
        compound.putInt("duration", getDuration());
    }

}
