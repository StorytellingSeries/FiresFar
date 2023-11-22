package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.client.particles.circle.CircleTintData;
import it.hurts.sskirillss.relics.client.particles.spark.SparkTintData;
import it.hurts.sskirillss.relics.items.relics.base.utils.AbilityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber
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
        if(!this.level.isClientSide) {
            spark(2, 0.04, 0.4f);
        }
        //-----------------------GRAPHENE_END--------------------------//

        if(this.tickCount > 10) isExploding = true;





    }

    private void spark(int count, double speed, float diam) {
        ParticleHelper.spawnParticleEntity(new CircleTintData(new Color(175, 117, 245), diam, 45, 0.91f, false),
                this, count, speed);
        ParticleHelper.spawnParticleEntity(new CircleTintData(new Color(10, 46, 203), diam, 45, 0.91f, false),
                this, count, speed);

        ParticleHelper.spawnParticleEntity(new SparkTintData(new Color(115, 110, 255), diam, 50),
                this, count, speed);
        ParticleHelper.spawnParticleEntity(new SparkTintData(new Color(245, 152, 255), diam, 50),
                this, count, speed);
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AZALEA_FALL, SoundSource.MASTER, 0.5f, 1.4f + random.nextFloat() * 1.6f);
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {

    }

    @Override
    public void onRemovedFromWorld() {
        spark(9, 0.04, 0.8f);
        AABB box = this.getBoundingBox().inflate(getRadius()).move(0, -getRadius() * 0.5, 0).expandTowards(0, -3, 0);
        List<LivingEntity> targets = new ArrayList<>(this.getLevel().getEntitiesOfClass(LivingEntity.class, box, e -> !(e.equals(this.getOwner()))));
        for(LivingEntity le : targets) {
            Vec3 start = this.position();
            Vec3 end = le.getBoundingBox().getCenter();
            if(!this.level.isClientSide()) {
                drawJaggedLightning(this.level, start, end, 2, 0.3, 0.15f, new Color(187, 145, 255), true);
                drawJaggedLightning(this.level, start, end, 2, 0.3, 0.15f, new Color(222, 127, 255), true);
                drawJaggedLightning(this.level, start, end, 2, 0.3, 0.25f, new Color(154, 96, 255), true);

                le.hurt(DamageSource.thrown(this, this.getOwner()), getDmg());
            }


            this.getLevel().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1, random.nextFloat() * 1.4f + 0.3f);
            this.getLevel().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.03f, random.nextFloat() * 1.3f + 0.5f);

            AABB secondaryBox = new AABB(end, end).inflate(getRadius() * 0.5).move(0, getRadius() * 0.25, 0);
            List<LivingEntity> secondaryTargets = new ArrayList<>(this.getLevel().getEntitiesOfClass(LivingEntity.class, secondaryBox, e -> !(e.equals(this.getOwner()) || e.equals(le))));
            if(!secondaryTargets.isEmpty()) {
                LivingEntity secTarget = secondaryTargets.get(this.random.nextInt(secondaryTargets.size()));
                if(!this.level.isClientSide()) {
                    AABB box2 = secTarget.getBoundingBox();
                    Vec3 end2 = box2.getCenter()
                            .add(MathUtils.randomFloat(random) * box2.getXsize() * 0.4,
                                    MathUtils.randomFloat(random) * box2.getYsize() * 0.4,
                                    MathUtils.randomFloat(random) * box2.getZsize() * 0.4);
                    int segments2 = (int) Math.round(end.distanceTo(end2));
                    drawJaggedLightning(this.level, end, end2, 2, 0.3, 0.15f, new Color(222, 127, 255), true);
                    drawJaggedLightning(this.level, end, end2, 2, 0.3, 0.25f, new Color(154, 96, 255), true);

                    secTarget.hurt(DamageSource.thrown(this, this.getOwner()), getDmg());
                }
            }
        }
        super.onRemovedFromWorld();
    }

    public void drawThinLightning(Level level, Vec3 start, Vec3 end, int segments, double jag, float d, Color color, boolean doStartBurst){
        Vec3 pos = start;
        Vec3 straightPos = start;
        Vec3 prevPos = start;
        if(doStartBurst) {
            ParticleHelper.spawnParticleAABB(this.level, new CircleTintData(new Color(230, 175, 255), 0.6f, 15, 0.68f, false),
                    new AABB(start, start), 10, 0.2);
            ParticleHelper.spawnParticleAABB(this.level, new CircleTintData(new Color(230, 175, 255), 0.3f, 30, 0.82f, false),
                    new AABB(start, start), 10, 0.15);
        }
        double length = end.subtract(start).scale((double) 1 / segments).length();
        for(int i = 0; i < segments; i++) {
            straightPos = straightPos.add(end.subtract(start).scale((double) 1 / segments));
            pos = straightPos.add(new Vec3(MathUtils.randomFloat(random) * jag,  0, MathUtils.randomFloat(random) * jag));
            if(i == segments - 1) pos = end;
            ParticleHelper.spawnParticleLine(level, new CircleTintData(color, d, 40, 0.9f, false),
                    prevPos,
                    pos,
                    (int) Math.round(length * 24), 0);
            prevPos = pos;
        }
        ParticleHelper.spawnParticleAABB(this.level, new SparkTintData(new Color(179, 190, 255), 0.4f, 50),
                new AABB(end, end), 10, 0.1);
        ParticleHelper.spawnParticleAABB(this.level, new SparkTintData(new Color(242, 208, 255), 0.4f, 50),
                new AABB(end, end), 10, 0.1);
        ParticleHelper.spawnParticleAABB(this.level, new CircleTintData(new Color(0, 89, 255), 0.4f, 30, 0.8f, false),
                new AABB(end, end), 10, 0.1);
        ParticleHelper.spawnParticleAABB(this.level, new CircleTintData(new Color(221, 117, 255), 0.4f, 30, 0.8f, false),
                new AABB(end, end), 10, 0.1);
    }

    public void drawJaggedLightning(Level level, Vec3 start, Vec3 end, int sliceIterations, double maxJagMultiplier, float d, Color color, boolean doStartBurst){

        if(doStartBurst) {
            ParticleHelper.spawnParticleAABB(this.level, new CircleTintData(new Color(230, 175, 255), 0.6f, 15, 0.68f, false),
                    new AABB(start, start), 10, 0.2);
            ParticleHelper.spawnParticleAABB(this.level, new CircleTintData(new Color(230, 175, 255), 0.3f, 30, 0.82f, false),
                    new AABB(start, start), 10, 0.15);
        }

        ParticleHelper.spawnRandomJaggedParticleLine(level, start, end, maxJagMultiplier, new CircleTintData(color, d, 35, 0.89f, false), 16, sliceIterations);

        ParticleHelper.spawnParticleAABB(this.level, new SparkTintData(new Color(179, 190, 255), 0.4f, 50),
                new AABB(end, end), 10, 0.06);
        ParticleHelper.spawnParticleAABB(this.level, new SparkTintData(new Color(242, 208, 255), 0.4f, 50),
                new AABB(end, end), 10, 0.06);
        ParticleHelper.spawnParticleAABB(this.level, new CircleTintData(new Color(0, 89, 255), 0.4f, 30, 0.8f, false),
                new AABB(end, end), 10, 0.08);
        ParticleHelper.spawnParticleAABB(this.level, new CircleTintData(new Color(221, 117, 255), 0.4f, 30, 0.8f, false),
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
    protected void defineSynchedData() {
        this.entityData.define(RADIUS, 5F);
        this.entityData.define(DMG, 5F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setRadius(compound.getFloat("radius"));
        setDmg(compound.getFloat("dmg"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("radius", getRadius());
        compound.putFloat("dmg", getDmg());
    }

}
