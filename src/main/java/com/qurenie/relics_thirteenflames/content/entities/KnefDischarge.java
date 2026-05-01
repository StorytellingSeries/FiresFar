package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.octostudios.octolib.util.OctoColor;
import it.hurts.sskirillss.relics.utils.MathUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//@EventBusSubscriber
public class KnefDischarge extends ThrowableProjectile
{
    
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(KnefDischarge.class, EntityDataSerializers.FLOAT);

    public float getRadius() {
        return this.getEntityData().get(RADIUS);
    }

    public void setRadius(float radius) {
        this.getEntityData().set(RADIUS, radius);
    }

    private static final EntityDataAccessor<Float> DMG = SynchedEntityData.defineId(KnefDischarge.class, EntityDataSerializers.FLOAT);

    public float getDmg() {
        return this.getEntityData().get(DMG);
    }

    public void setDmg(float damage) {
        this.getEntityData().set(DMG, damage);
    }

    private static final EntityDataAccessor<String> OWNER_UUID = SynchedEntityData.defineId(KnefDischarge.class, EntityDataSerializers.STRING);

    public void setOwnerUUID(String uuid){
        this.getEntityData().set(OWNER_UUID, uuid);
    }

    public String getOwnerUUID() {
        return this.getEntityData().get(OWNER_UUID);
    }

    public Vec3 shotPos;

    private boolean isExploding = false;

    public static Random rng = new Random();

    public KnefDischarge(EntityType<? extends KnefDischarge> type, Level world) {
        super(type, world);

    }

    @Override
    public void tick() {
        Vec3 motion = new Vec3(0, 0.6, 0);
        super.tick();
        setDeltaMovement( !isExploding ? motion : Vec3.ZERO);
        if(shotPos == null) shotPos = this.position();



        //-------------------------GRAPHENE----------------------------//
        if(!this.level().isClientSide) {
            spark(2, 0.04, 0.4f);
        }
        //-----------------------GRAPHENE_END--------------------------//

        if(this.tickCount > 10) isExploding = true;





    }

//    Color[] lightningColors = {
//            FlamesUtils.fromRGBI((175, 117, 245),
//            FlamesUtils.fromRGBI((10, 46, 203),
//            FlamesUtils.fromRGBI((115, 110, 255),
//            FlamesUtils.fromRGBI((245, 152, 255)
//    };
    OctoColor[] lightningColors = {
            FlamesUtils.fromRGBI(145, 255, 213),
            FlamesUtils.fromRGBI(117, 223, 255),
            FlamesUtils.fromRGBI(96, 255, 194),
            FlamesUtils.fromRGBI(86, 185, 255)
    };

    private void spark(int count, double speed, float diam) {
        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(lightningColors[0], diam, 45, 0.9f),
                this, count, speed);
        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(lightningColors[1], diam, 45, 0.9f),
                this, count, speed);

        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(lightningColors[2], diam / 2.0f, 50, 0.93f),
                this, count, speed);
        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(lightningColors[3], diam / 2.0f, 50, 0.93f),
                this, count, speed);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AZALEA_FALL, SoundSource.MASTER, 0.5f, 1.4f + random.nextFloat() * 1.6f);
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {

    }

    @Override
    public void onRemovedFromLevel() {
        spark(10, 0.04, 0.15f);
        spark(15, 0.01, 0.15f);
        AABB box = this.getBoundingBox().inflate(getRadius()).move(0, -getRadius() * 0.5, 0).expandTowards(0, -3, 0);
        List<LivingEntity> targets = new ArrayList<>(this.level().getEntitiesOfClass(LivingEntity.class, box, e -> !(e.equals(this.getOwner())) && e.hasLineOfSight(this)));
        for(LivingEntity le : targets) {
            Vec3 start = this.position();
            Vec3 end = le.getBoundingBox().getCenter();
            if(!this.level().isClientSide()) {
                drawJaggedLightning(this.level(), start, end, 3, 0.2, 0.15f, lightningColors[0], true);
                drawJaggedLightning(this.level(), start, end, 3, 0.2, 0.15f, lightningColors[1], true);
                drawJaggedLightning(this.level(), start, end, 3, 0.2, 0.25f, lightningColors[2], true);

                le.hurt(le.level().damageSources().thrown(this, this.getOwner()), getDmg());
            }


            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1, random.nextFloat() * 1.4f + 0.3f);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.03f, random.nextFloat() * 1.3f + 0.5f);

            AABB secondaryBox = new AABB(end, end).inflate(getRadius() * 0.5).move(0, getRadius() * 0.25, 0);
            List<LivingEntity> secondaryTargets = new ArrayList<>(this.level().getEntitiesOfClass(LivingEntity.class, secondaryBox, e -> !(e.equals(this.getOwner()) || e.equals(le))));
            if(!secondaryTargets.isEmpty()) {
                LivingEntity secTarget = secondaryTargets.get(this.random.nextInt(secondaryTargets.size()));
                if(!this.level().isClientSide()) {
                    AABB box2 = secTarget.getBoundingBox();
                    Vec3 end2 = box2.getCenter()
                            .add(MathUtils.randomFloat(random) * box2.getXsize() * 0.4,
                                    MathUtils.randomFloat(random) * box2.getYsize() * 0.4,
                                    MathUtils.randomFloat(random) * box2.getZsize() * 0.4);
                    drawJaggedLightning(this.level(), end, end2, 3, 0.3, 0.15f, lightningColors[0], true);
                    end2 = box2.getCenter()
                            .add(MathUtils.randomFloat(random) * box2.getXsize() * 0.4,
                                    MathUtils.randomFloat(random) * box2.getYsize() * 0.4,
                                    MathUtils.randomFloat(random) * box2.getZsize() * 0.4);
                    drawJaggedLightning(this.level(), end, end2, 3, 0.3, 0.25f, lightningColors[1], true);

                    secTarget.hurt(secTarget.level().damageSources().thrown(this, this.getOwner()), getDmg());
                }
            }
        }
        super.onRemovedFromLevel();
    }

    public void drawJaggedLightning(Level level, Vec3 start, Vec3 end, int sliceIterations, double maxJagMultiplier, float d, OctoColor color, boolean doStartBurst){

        if(doStartBurst) {
            ParticleHelper.spawnParticleAABB(this.level(), ParticleHelper.constructSimpleSpark(lightningColors[0], 0.6f, 15, 0.68f),
                    new AABB(start, start), 10, 0.2);
            ParticleHelper.spawnParticleAABB(this.level(), ParticleHelper.constructSimpleSpark(lightningColors[1], 0.3f, 30, 0.82f),
                    new AABB(start, start), 10, 0.15);
        }

        ParticleHelper.spawnRandomJaggedParticleLine(level, start, end, maxJagMultiplier, ParticleHelper.constructSimpleSpark(color, d, 35, 0.9f), 16, sliceIterations);

        ParticleHelper.spawnParticleAABB(this.level(), ParticleHelper.constructSimpleSpark(lightningColors[0], 0.2f, 50, 0.75f),
                new AABB(end, end), 10, 0.06);
        ParticleHelper.spawnParticleAABB(this.level(), ParticleHelper.constructSimpleSpark(lightningColors[1], 0.2f, 50, 0.75f),
                new AABB(end, end), 10, 0.06);
        ParticleHelper.spawnParticleAABB(this.level(), ParticleHelper.constructSimpleSpark(lightningColors[2], 0.4f, 30, 0.8f),
                new AABB(end, end), 10, 0.08);
        ParticleHelper.spawnParticleAABB(this.level(), ParticleHelper.constructSimpleSpark(lightningColors[3], 0.4f, 30, 0.8f),
                new AABB(end, end), 10, 0.08);
    }


    @Override
    public void checkDespawn() {
        if(this.tickCount > 15){
            this.discard();
        }
    }

    @Override
    public boolean canCollideWith(Entity pEntity) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 5F);
        builder.define(DMG, 5F);
        builder.define(OWNER_UUID, "");
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setRadius(compound.getFloat("radius"));
        setDmg(compound.getFloat("dmg"));
        setOwnerUUID(compound.getString("owneruuid"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("radius", getRadius());
        compound.putFloat("dmg", getDmg());
        compound.putString("owneruuid", getOwnerUUID());
    }

}
