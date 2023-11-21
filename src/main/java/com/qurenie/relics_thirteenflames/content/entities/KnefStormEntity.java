package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.client.particles.circle.CircleTintData;
import it.hurts.sskirillss.relics.client.particles.spark.SparkTintData;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.awt.*;
import java.util.*;
import java.util.List;

public class KnefStormEntity extends Projectile {

    Color[] colors = {
            new Color(0,7,9),
            new Color(0, 0, 16),
            new Color(0, 7, 14),
            new Color(14, 3, 14)
    };
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(KnefStormEntity.class, EntityDataSerializers.INT);

    public void setLifeTime(int lifetime){
        this.getEntityData().set(LIFETIME, lifetime);
    }

    public int getLifeTime() {
        return this.getEntityData().get(LIFETIME);
    }

    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(KnefStormEntity.class, EntityDataSerializers.FLOAT);

    public float getRadius() {
        return this.getEntityData().get(RADIUS);
    }

    public void setRadius(float radius) {
        this.getEntityData().set(RADIUS, radius);
    }

    private static final EntityDataAccessor<Float> DMG = SynchedEntityData.defineId(KnefStormEntity.class, EntityDataSerializers.FLOAT);

    public float getDmg() {
        return this.getEntityData().get(DMG);
    }

    public void setDmg(float damage) {
        this.getEntityData().set(DMG, damage);
    }

    private static final EntityDataAccessor<Float> HEAL = SynchedEntityData.defineId(KnefStormEntity.class, EntityDataSerializers.FLOAT);

    public float getHeal() {
        return this.getEntityData().get(HEAL);
    }

    public void setHeal(float heal) {
        this.getEntityData().set(HEAL, heal);
    }
    private static final EntityDataAccessor<Integer> FREQ = SynchedEntityData.defineId(KnefStormEntity.class, EntityDataSerializers.INT);

    public void setFreq(int freq){
        this.getEntityData().set(FREQ, freq);
    }

    public int getFreq() {
        return this.getEntityData().get(FREQ);
    }

    private ItemStack bow = ItemStack.EMPTY;

    public void setBow(ItemStack bow){
        this.bow = bow;
    }

    public ItemStack getBow(){
        return  bow;
    }
    private double r = 1;


    public KnefStormEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }


    @Override
    public boolean isAlwaysTicking() {
        return true;
    }

    double a = 30,
            b = 0;

    float radius;

    LinkedList<DelayedRunnable> taskQueue = new LinkedList<>();

    @Override
    public void tick() {
        super.tick();

        if(!this.level.isClientSide() && !taskQueue.isEmpty()){
            if(taskQueue.getFirst().startedAt + taskQueue.getFirst().delay <= this.tickCount) taskQueue.pop().runnable.run();
        }

        radius = getRadius();
        int lifetime = getLifeTime();
        int freq = getFreq();
        if(this.tickCount > lifetime) this.discard();

//-------------------------ГРАФОНИЙ----------------------------//


        for(int i = 0; i < r; i++){
            Vec3 direction = new Vec3(1,0,0);
            direction = direction.yRot((float) Math.toRadians(random.nextFloat() * 360f)).scale(MathUtils.randomFloat(random));
            double x = MathUtils.randomFloat(random) * r;
            double z = MathUtils.randomFloat(random) * Math.sqrt(r * r - x * x);
            this.getLevel().addParticle(new CircleTintData(colors[random.nextInt(colors.length)], (float) (1 + r / 2), 80, 0.96f, false), true,
                    this.getX() + x, this.getY() + MathUtils.randomFloat(random) * r / 10, this.getZ() + z, direction.x * 0.46, direction.y * 0.1, direction.z * 0.46);
        }

        drawFrame();
        if(radius >= 10)drawInnerCircles();
        if(radius >= 13)drawLine();
        if(radius >= 16)drawDiamond();
        if(radius >= 19)drawCross();

        if(r < radius * 1.2){
            r += (radius * 1.2 - 1) / 120;
        }
//----------------------КОНЕЦ ГРАФОНИЯ-------------------------//



        if(this.tickCount > 130){
            AABB box = this.getBoundingBox().inflate(radius).inflate(0, 50, 0).move(0, -50, 0);
            List<LivingEntity> targets = new ArrayList<>(this.getLevel().getEntitiesOfClass(LivingEntity.class, box, e -> !(e.equals(this.getOwner()))));

            if(this.tickCount % freq == 0){
                KnefRaindrop drop = new KnefRaindrop(EntityRegistry.KNEF_RAINDROP, this.getLevel());
                Vec3 pos = this.getPosition(1f).add(MathUtils.randomFloat(random) * radius, -1, MathUtils.randomFloat(random) * radius);
                if(random.nextFloat() < 0.2 && !targets.isEmpty()){
                    LivingEntity target = targets.get(random.nextInt(targets.size()));
                    pos = target.getPosition(1f).add(0, this.getY() - target.getY() - 1, 0);
                }
//                else if(random.nextFloat() < 0.2){
//                    List<LivingEntity> allies = new ArrayList<>(this.getLevel().getEntitiesOfClass(LivingEntity.class, box, e -> (e.equals(this.getOwner()))));
//                    if(!allies.isEmpty()) {
//                        LivingEntity target = allies.get(random.nextInt(allies.size()));
//                        pos = target.getPosition(1f).add(0, this.getY() - target.getY() - 1, 0);
//                    }
//                }
                drop.setPos(pos);
                drop.setDeltaMovement(0, -3, 0);
                drop.setOwner(this.getOwner());
                drop.setBow(getBow());
                drop.setHeal(getHeal());
                drop.setDmg(getDmg());
                this.getLevel().addFreshEntity(drop);
                ParticleHelper.spawnParticleEntity(new CircleTintData(new Color(0, 128, 255), 0.2f, 15, 0.83f, false),
                        drop, 15, 0.1);

                Vec3 endpos = pos;
                HitResult result = this.level.clip(new ClipContext(pos, pos.add(0, -160, 0),
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                if (result.getType() == HitResult.Type.BLOCK) {
                    endpos = result.getLocation();
                } else endpos = pos.add(0,-102,0);
                float vol = (float) (20 / (this.getOwner() != null ? this.getOwner().distanceToSqr(pos.subtract(0,pos.y() - this.getOwner().getY(),0)) : 20));

                this.getLevel().playSound(null, endpos.x, endpos.y, endpos.z, SoundsRegistry.KNEF_BOW_RAIN.get(), SoundSource.PLAYERS, random.nextFloat() * 0.15f * vol + vol, random.nextFloat() * 0.6f + 0.7f);

            }
            if(this.tickCount % (35 + Math.round(freq * freq * (freq / 1.9))) == 0){
                Vec3 pos = this.getPosition(1f).add(MathUtils.randomFloat(random) * radius, -1, MathUtils.randomFloat(random) * radius);
                Vec3 endpos = pos;

//                AABB secondaryBox;
//                List<LivingEntity> secondaryTargets = List.of();
//                Vec3 targetCenter = Vec3.ZERO;
                if(!targets.isEmpty()){
                    LivingEntity target = targets.get(random.nextInt(targets.size()));
                    pos = target.getPosition(1F).add(0,this.getY() - target.getY(),0);
                    endpos = target.getPosition(1f);
//                    secondaryBox = new AABB(endpos, endpos).inflate(5, 2, 5).move(0,1,0);
//                    secondaryTargets = new ArrayList<>(this.getLevel().getEntitiesOfClass(LivingEntity.class, secondaryBox, e -> !(e.equals(this.getOwner()) || e.equals(target))));
//                    targetCenter = target.getBoundingBox().getCenter();
                }
                else{
                    HitResult result = this.level.clip(new ClipContext(pos, pos.add(0, -160, 0),
                            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                    if (result.getType() == HitResult.Type.BLOCK) {
                        endpos = result.getLocation();
                    } else endpos = pos.add(0,-102,0);
                }
                Vec3 finalPos = pos;
                Vec3 finalEndpos = endpos;

                if(!this.level.isClientSide()) {
                    taskQueue.add(new DelayedRunnable(() -> drawThinLightning(this.getLevel(), finalPos, finalEndpos, 8, 0.45, 0.55f, new Color(187, 145, 255), 14),
                            this.tickCount, 1));
                    taskQueue.add(new DelayedRunnable(() -> drawThinLightning(this.getLevel(), finalPos, finalEndpos, 20, 0.55, 0.4f, new Color(127, 117, 255), 13),
                            this.tickCount, 2));
                    taskQueue.add(new DelayedRunnable(() -> drawThinLightning(this.getLevel(), finalPos, finalEndpos, 20, 0.55, 0.35f, new Color(154, 96, 255), 13),
                            this.tickCount, 3));
                    taskQueue.add(new DelayedRunnable(() -> drawThinLightning(this.getLevel(), finalPos, finalEndpos, 16, 0.55, 0.3f, new Color(128, 86, 255), 14),
                            this.tickCount, 4));


//                    if(!secondaryTargets.isEmpty()) {
//                        //LivingEntity secTarget = secondaryTargets.get(this.random.nextInt(secondaryTargets.size()));
//                        if(!this.level.isClientSide()) {
//                            for(LivingEntity secTarget : secondaryTargets) {
//                                Vec3 end2 = secTarget.getBoundingBox().getCenter();
//                                int segments2 = (int) Math.round(targetCenter.distanceTo(end2) / 2);
//                                drawThinHorizontalLightning(this.level, targetCenter, end2, segments2, 0.8, 0.15f, new Color(187, 145, 255), true);
//                                drawThinHorizontalLightning(this.level, targetCenter, end2, segments2, 0.8, 0.15f, new Color(222, 127, 255), true);
//                                drawThinHorizontalLightning(this.level, targetCenter, end2, segments2, 0.5, 0.25f, new Color(154, 96, 255), true);
//
//                                secTarget.hurt(DamageSource.thrown(this, this.getOwner()), 10);
//                            }
//                        }
//                        secondaryTargets.clear();
//                    }

                    float vol = (float) (20 / (this.getOwner() != null ? this.getOwner().distanceToSqr(endpos) : 20));
                    if(this.getOwner() == null) {
                        this.getLevel().playSound(null, endpos.x, endpos.y, endpos.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, vol, random.nextFloat() * 0.2f + 0.3f);
                        this.getLevel().playSound(null, endpos.x, endpos.y, endpos.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, vol, random.nextFloat() * 0.3f + 1.5f);
                    } else{
                        this.getLevel().playSound(null, this.getOwner(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, vol, random.nextFloat() * 0.2f + 0.3f);
                        this.getLevel().playSound(null, this.getOwner(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, vol, random.nextFloat() * 0.3f + 1.5f);
                    }
                    for(LivingEntity e : this.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(finalEndpos,finalEndpos).inflate(2.2, 4, 2.2), e -> !(e.equals(this.getOwner()) || e instanceof LocalPlayer))){
                        e.hurt(DamageSource.thrown(this, this.getOwner()), getDmg() * 5);
                    }
                }


            }
        }

    }

    public void drawThinLightning(Level level, Vec3 start, Vec3 end, int segments, double jag, float d, Color color, int particleCount){
        Vec3 pos = start;
        Vec3 straightPos = start;
        Vec3 prevPos = start;
        ParticleHelper.spawnParticleAABB(this.level, new CircleTintData(new Color(230, 175, 255), 0.6f, 15, 0.68f, false),
                new AABB(start, start), 10, 0.2);
        ParticleHelper.spawnParticleAABB(this.level, new CircleTintData(new Color(230, 175, 255), 0.3f, 30, 0.82f, false),
                new AABB(start, start), 10, 0.15);
        double length = end.subtract(start).scale((double) 1 / segments).y();
        for(int i = 0; i < segments; i++) {
            straightPos = straightPos.add(end.subtract(start).scale((double) 1 / segments));
            pos = straightPos.add(new Vec3(MathUtils.randomFloat(random) * jag,  0, MathUtils.randomFloat(random) * jag));
            if(i == segments - 1) pos = end;
            ParticleHelper.spawnParticleLine(level, new CircleTintData(color, d, 30, 0.89f, false),
                    prevPos,
                    pos,
                    (int) Math.round((-length * particleCount) * (0.2 + (double) i * i / (segments - 1) / (segments - 1)) * 0.8), 0);
            prevPos = pos;
        }
        ParticleHelper.spawnParticleAABB(this.level, new SparkTintData(new Color(179, 190, 255), 0.4f, 50),
                new AABB(end, end), 10, 0.15);
        ParticleHelper.spawnParticleAABB(this.level, new SparkTintData(new Color(242, 208, 255), 0.4f, 50),
                new AABB(end, end), 10, 0.15);
        ParticleHelper.spawnParticleAABB(this.level, new CircleTintData(new Color(0, 89, 255), 0.4f, 30, 0.8f, false),
                new AABB(end, end), 8, 0.15);
        ParticleHelper.spawnParticleAABB(this.level, new CircleTintData(new Color(221, 117, 255), 0.4f, 30, 0.8f, false),
                new AABB(end, end), 8, 0.15);
    }

    public void drawThinHorizontalLightning(Level level, Vec3 start, Vec3 end, int segments, double jag, float d, Color color, boolean doStartBurst){
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
            ParticleHelper.spawnParticleLine(level, new CircleTintData(color, d, 50, 0.9f, false),
                    prevPos,
                    pos,
                    (int) Math.round(length * 8), 0);
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

    public void drawFrame(){
        a = 30;
        for(int i = 0; i < 80; i++){
            if(i + 40 <= this.tickCount) {
                Vec3 pos = this.getPosition(1F).subtract(new Vec3(0,0,-2)).add(new Vec3(radius + 7, 0, 0).yRot((float) Math.toRadians(a)));
//                this.getLevel().addParticle(new CircleTintData(new Color(0, 217, 255), (float) (radius / 20.0) * (1 - (float) ((i - 40) * (i - 40)) / 1600) + 0.1f, 2, 0.99f, false), true,
//                        pos.x(), pos.y() - 1 - radius / 6, pos.z(), 0, 0, 0);
                level.addParticle(new CircleTintData(new Color(0, 21, 255), (float) (radius / 20.0) * (1 - (float) ((i - 40) * (i - 40)) / 1600) + 0.1f, 2, 0.99f, false),
                        true,
                        pos.x(), pos.y() - 2 - radius / 6, pos.z(), 0, 0, 0);

                pos = this.getPosition(1F).subtract(new Vec3(0,0,2)).add(new Vec3(radius + 7, 0, 0).yRot((float) Math.toRadians(-a)));
                level.addParticle(new CircleTintData(new Color(0, 21, 255), (float) (radius / 20.0) * (1 - (float) ((i - 40) * (i - 40)) / 1600) + 0.1f, 2, 0.98f, false),
                        true,
                        pos.x(), pos.y() - 2 - radius / 6, pos.z(), 0, 0, 0);
                a += 1.5;
            }
        }
    }

    public void drawLine(){
        a = 0;
        for(int i = 0; i < 90; i++){
            if(i + 120 <= this.tickCount * 2) {
                Vec3 pos = this.getPosition(1F).add(new Vec3(- radius * 1.6 + a, 0, 0));
                level.addParticle(new CircleTintData(new Color(0, 81, 255), (float) (radius / 20.0) * (1 - (float) ((i - 45) * (i - 45)) / 2025) + 0.1f, 2, 0.99f, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z(), 0, 0, 0);

                a += (radius * 1.6) / 45;
            }
        }
    }

    public void drawInnerCircles(){
        a = 0;
        for(int i = 0; i < 40; i++){
            if(i + 70 <= this.tickCount) {
                Vec3 pos = this.getPosition(1F).add(new Vec3(0.2,0,0)).add(new Vec3(radius / 2, 0, 0).yRot((float) Math.toRadians(a)));
                level.addParticle(new CircleTintData(new Color(0, 172, 201), (float) (radius / 20.0) * ((float) ((i - 80) * (i - 80) - 1) / 6400) + 0.05f, 2, 0.99f, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z(), 0, 0, 0);

                pos = this.getPosition(1F).add(new Vec3(0.2,0,0)).add(new Vec3(radius / 2, 0, 0).yRot((float) Math.toRadians(-a)));
                level.addParticle(new CircleTintData(new Color(0, 172, 201), (float) (radius / 20.0) * ((float) ((i - 80) * (i - 80) - 1) / 6400) + 0.05f, 2, 0.98f, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z(), 0, 0, 0);
                a += 2;
            }
        }
        b = 0;
        for(int i = 0; i < 45; i++){
            if(i + 70 <= this.tickCount) {
                Vec3 pos = this.getPosition(1F).add(new Vec3(-radius / 1.7, 0, 0).yRot((float) Math.toRadians(b)));
                level.addParticle(new CircleTintData(new Color(0, 159, 185), (float) (radius / 20.0) * ((float) ((i - 90) * (i - 90) - 1) / 8100) + 0.1f, 2, 0.99f, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z(), 0, 0, 0);
                if(i >= 43) level.addParticle(new CircleTintData(new Color(0, 159, 185), (float) (radius / 20.0) * ((float) ((i - 90) * (i - 90) - 1) / 8100) + 0.1f, 2, 0.99f, false), true,
                        pos.x() + 0.05, pos.y() - 1 - radius / 6, pos.z() + 0.1, 0, 0, 0);
                if(i == 44) level.addParticle(new CircleTintData(new Color(0, 159, 185), (float) (radius / 20.0) * ((float) ((i - 90) * (i - 90) - 1) / 8100) + 0.1f, 2, 0.99f, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z() + 0.3, 0, 0, 0);

                pos = this.getPosition(1F).add(new Vec3(-radius / 1.7, 0, 0).yRot((float) Math.toRadians(-b)));
                level.addParticle(new CircleTintData(new Color(0, 159, 185), (float) (radius / 20.0) * ((float) ((i - 90) * (i - 90) - 1) / 8100) + 0.1f, 2, 0.98f, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z(), 0, 0, 0);
                if(i >= 43) level.addParticle(new CircleTintData(new Color(0, 159, 185), (float) (radius / 20.0) * ((float) ((i - 90) * (i - 90) - 1) / 8100) + 0.1f, 2, 0.99f, false), true,
                        pos.x() + 0.05, pos.y() - 1 - radius / 6, pos.z() - 0.1, 0, 0, 0);
                if(i == 44) level.addParticle(new CircleTintData(new Color(0, 159, 185), (float) (radius / 20.0) * ((float) ((i - 90) * (i - 90) - 1) / 8100) + 0.1f, 2, 0.99f, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z() - 0.3, 0, 0, 0);
                b += 2;
            }
        }
    }

    public void drawDiamond(){
        a = 0;
        b = 0;
        for(int i = 0; i < 20; i++){
            if(i + 80 <= this.tickCount) {
                Vec3 pos = this.getPosition(1F).add(new Vec3(b, 0, radius / 3 - a));
                level.addParticle(new CircleTintData(new Color(0, 81, 255), (float) (radius / 40.0) * (1 - (float) ((i - 20) * (i - 20)) / 400) + 0.05f, 2, 0.99f, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z(), 0, 0, 0);

                pos = this.getPosition(1F).add(new Vec3(-b, 0, radius / 3 - a));
                level.addParticle(new CircleTintData(new Color(0, 81, 255), (float) (radius / 40.0) * (1 - (float) ((i - 20) * (i - 20)) / 400) + 0.05f, 2, 0.98f, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z(), 0, 0, 0);

                pos = this.getPosition(1F).add(new Vec3(b, 0, - radius / 3 + a));
                level.addParticle(new CircleTintData(new Color(0, 81, 255), (float) (radius / 40.0) * (1 - (float) ((i - 20) * (i - 20)) / 400) + 0.05f, 2, 0.98f, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z(), 0, 0, 0);

                pos = this.getPosition(1F).add(new Vec3(-b, 0, - radius / 3 + a));
                level.addParticle(new CircleTintData(new Color(0, 81, 255), (float) (radius / 40.0) * (1 - (float) ((i - 20) * (i - 20)) / 400) + 0.05f, 2, 0.98f, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z(), 0, 0, 0);

                a += radius / 60;
                b += radius / 120;
            }
        }
    }

    public void drawCross(){
        a = 0;
        b = 0;
        for(int i = 0; i < 20; i++){
            if(i + 1200 <= this.tickCount * 10) {
                Vec3 pos = this.getPosition(1F).add(new Vec3(b, 0, -a));
                level.addParticle(new CircleTintData(new Color(222, 127, 255), (float) (radius / 40.0) * ((float) ((i - 20) * (i - 20) - 1) / 400) + 0.05f, 2, 0.99f, false), true,
                        pos.x(), pos.y() - 1.5 - radius / 6, pos.z(), 0, 0, 0);

                pos = this.getPosition(1F).add(new Vec3(-b, 0, -a));
                level.addParticle(new CircleTintData(new Color(222, 127, 255), (float) (radius / 40.0) * ((float) ((i - 20) * (i - 20) - 1) / 400) + 0.05f, 2, 0.98f, false), true,
                        pos.x(), pos.y() - 1.5 - radius / 6, pos.z(), 0, 0, 0);

                pos = this.getPosition(1F).add(new Vec3(b, 0, +a));
                level.addParticle(new CircleTintData(new Color(222, 127, 255), (float) (radius / 40.0) * ((float) ((i - 20) * (i - 20) - 1) / 400) + 0.05f, 2, 0.98f, false), true,
                        pos.x(), pos.y() - 1.5 - radius / 6, pos.z(), 0, 0, 0);

                pos = this.getPosition(1F).add(new Vec3(-b, 0, a));
                level.addParticle(new CircleTintData(new Color(222, 127, 255), (float) (radius / 40.0) * ((float) ((i - 20) * (i - 20) - 1) / 400) + 0.05f, 2, 0.98f, false), true,
                        pos.x(), pos.y() - 1.5 - radius / 6, pos.z(), 0, 0, 0);

                a += radius / 60;
                b += radius / 120;
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(RADIUS, 5F);
        this.entityData.define(FREQ, 5);
        this.entityData.define(LIFETIME, 100);
        this.entityData.define(DMG, 8F);
        this.entityData.define(HEAL, 1F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setRadius(compound.getFloat("radius"));
        setDmg(compound.getFloat("dmg"));
        setHeal(compound.getFloat("heal"));
        setFreq(compound.getInt("freq"));
        setLifeTime(compound.getInt("lifetime"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("radius", getRadius());
        compound.putFloat("dmg", getDmg());
        compound.putFloat("heal", getHeal());
        compound.putInt("freq", getFreq());
        compound.putInt("lifetime", getLifeTime());
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private class DelayedRunnable {
        Runnable runnable;
        int startedAt;
        int delay;
        DelayedRunnable(Runnable runnable, int startedAt, int delay){
            this.runnable = runnable;
            this.startedAt = startedAt;
            this.delay = delay;
        }
    }
}
