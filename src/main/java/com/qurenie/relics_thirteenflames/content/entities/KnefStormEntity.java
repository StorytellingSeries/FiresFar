package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.client.particles.CircleTintData;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class KnefStormEntity extends Projectile {

//    Color[] colors = {
//            new Color(0,7,9),
//            new Color(0, 0, 16),
//            new Color(0, 7, 14),
//            new Color(14, 3, 14)
//    };

    Color[] colors = {
            new Color(0, 9, 8),
            new Color(4, 0, 16),
            new Color(4, 0, 10),
            new Color(2, 7, 5)
    };

//    Color[] lightningColors = {
//            new Color(187, 145, 255),
//            new Color(127, 117, 255),
//            new Color(154, 96, 255),
//            new Color(128, 86, 255)
//    };

    Color[] lightningColors = {
            new Color(145, 255, 213),
            new Color(117, 223, 255),
            new Color(96, 255, 194),
            new Color(86, 224, 255)
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

    private static final EntityDataAccessor<String> OWNER_UUID = SynchedEntityData.defineId(KnefStormEntity.class, EntityDataSerializers.STRING);

    public void setOwnerUUID(String uuid){
        this.getEntityData().set(OWNER_UUID, uuid);
    }

    public String getOwnerUUID() {
        return this.getEntityData().get(OWNER_UUID);
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

        if(!this.level().isClientSide() && !taskQueue.isEmpty()){
            if(taskQueue.getFirst().startedAt + taskQueue.getFirst().delay <= this.tickCount) taskQueue.pop().runnable.run();
        }

        radius = getRadius();
        int lifetime = getLifeTime();
        int freq = getFreq();
        if(this.tickCount > lifetime) this.discard();

//-------------------------GRAPHENE----------------------------//


        for(int i = 0; i < r; i++){
            Vec3 direction = new Vec3(1,0,0);
            direction = direction.yRot((float) Math.toRadians(random.nextFloat() * 360f)).scale(MathUtils.randomFloat(random));
            double x = MathUtils.randomFloat(random) * r;
            double z = MathUtils.randomFloat(random) * Math.sqrt(r * r - x * x);
            this.level().addParticle(new CircleTintData(colors[random.nextInt(colors.length)], (float) (1 + r / 2), 0, 60, -1, false), true,
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
//-----------------------GRAPHENE END--------------------------//



        if(this.tickCount > 130){
            AABB box = this.getBoundingBox().inflate(radius).inflate(0, 50, 0).move(0, -50, 0);
            List<LivingEntity> targets = new ArrayList<>(this.level().getEntitiesOfClass(LivingEntity.class, box, e -> !(e.equals(this.getOwner()))));

            if(this.tickCount % freq == 0){
                KnefRaindrop drop = new KnefRaindrop(EntityRegistry.KNEF_RAINDROP, this.level());
                Vec3 pos = this.getPosition(1f).add(MathUtils.randomFloat(random) * radius, -1, MathUtils.randomFloat(random) * radius);
                if(random.nextFloat() < 0.2 && !targets.isEmpty()){
                    LivingEntity target = targets.get(random.nextInt(targets.size()));
                    pos = target.getPosition(1f).add(0, this.getY() - target.getY() - 1, 0);
                }

                drop.setPos(pos);
                drop.setDeltaMovement(0, -3, 0);
                drop.setOwner(this.getOwner());
                drop.setBow(getBow());
                drop.setHeal(getHeal());
                drop.setDmg(getDmg());
                this.level().addFreshEntity(drop);
                ParticleHelper.spawnParticleEntity(ParticleUtils.constructSimpleSpark(new Color(0, 217, 255), 0.2f, 15, 0.83f), //new Color(0, 128, 255)
                        drop, 8, 0.1);
                ParticleHelper.spawnParticleEntity(ParticleUtils.constructSimpleSpark(new Color(0, 51, 255), 0.2f, 15, 0.83f), //new Color(0, 128, 255)
                        drop, 7, 0.1);

                Vec3 endpos = pos;
                HitResult result = this.level().clip(new ClipContext(pos, pos.add(0, -160, 0),
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                if (result.getType() == HitResult.Type.BLOCK) {
                    endpos = result.getLocation();
                } else endpos = pos.add(0,-102,0);
                float vol = (float) (20 / (this.getOwner() != null ? this.getOwner().distanceToSqr(pos.subtract(0,pos.y() - this.getOwner().getY(),0)) : 20));

                this.level().playSound(null, endpos.x, endpos.y, endpos.z, SoundsRegistry.KNEF_BOW_RAIN.get(), SoundSource.PLAYERS, random.nextFloat() * 0.15f * vol + vol, random.nextFloat() * 0.6f + 0.7f);

            }
            if(this.tickCount % (35 + Math.round(freq * freq * (freq / 1.9))) == 0){
                Vec3 pos = this.getPosition(1f).add(MathUtils.randomFloat(random) * radius, -1, MathUtils.randomFloat(random) * radius);
                Vec3 endpos = pos;


                if(!targets.isEmpty()){
                    LivingEntity target = targets.get(random.nextInt(targets.size()));
                    pos = target.getPosition(1F).add(0,this.getY() - target.getY(),0);
                    endpos = target.getPosition(1f);
                }
                else{
                    HitResult result = this.level().clip(new ClipContext(pos, pos.add(0, -160, 0),
                            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                    if (result.getType() == HitResult.Type.BLOCK) {
                        endpos = result.getLocation();
                    } else endpos = pos.add(0,-102,0);
                }
                Vec3 finalPos = pos;
                Vec3 finalEndpos = endpos;

                if(!this.level().isClientSide()) {
                    taskQueue.add(new DelayedRunnable(() -> drawThinLightning(this.level(), finalPos, finalEndpos, 8, 0.45, 0.55f, lightningColors[0], 14),
                            this.tickCount, 1));
                    taskQueue.add(new DelayedRunnable(() -> drawThinLightning(this.level(), finalPos, finalEndpos, 20, 0.55, 0.4f, lightningColors[1], 13),
                            this.tickCount, 2));
                    taskQueue.add(new DelayedRunnable(() -> drawThinLightning(this.level(), finalPos, finalEndpos, 20, 0.55, 0.35f, lightningColors[2], 13),
                            this.tickCount, 3));
                    taskQueue.add(new DelayedRunnable(() -> drawThinLightning(this.level(), finalPos, finalEndpos, 16, 0.55, 0.3f, lightningColors[3], 14),
                            this.tickCount, 4));


                    float vol = (float) (20 / (this.getOwner() != null ? this.getOwner().distanceToSqr(endpos) : 20));
                    if(this.getOwner() == null) {
                        this.level().playSound(null, endpos.x, endpos.y, endpos.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, vol, random.nextFloat() * 0.2f + 0.3f);
                        this.level().playSound(null, endpos.x, endpos.y, endpos.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, vol, random.nextFloat() * 0.3f + 1.5f);
                    } else{
                        this.level().playSound(null, this.getOwner(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, vol, random.nextFloat() * 0.2f + 0.3f);
                        this.level().playSound(null, this.getOwner(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, vol, random.nextFloat() * 0.3f + 1.5f);
                    }
                    for(LivingEntity e : this.level().getEntitiesOfClass(LivingEntity.class, new AABB(finalEndpos,finalEndpos).inflate(2.2, 4, 2.2), e -> !e.getStringUUID().equals(this.getOwnerUUID()))){
                        e.hurt(e.damageSources().thrown(this, this.getOwner()), getDmg() * 5);
                    }
                }


            }
        }

    }

    public void drawThinLightning(Level level, Vec3 start, Vec3 end, int segments, double jag, float d, Color color, int particleCount){
        Vec3 pos = start;
        Vec3 straightPos = start;
        Vec3 prevPos = start;
        ParticleHelper.spawnParticleAABB(level, ParticleUtils.constructSimpleSpark(color, 0.6f, 15, 0.68f),
                new AABB(start, start), 10, 0.2);
        ParticleHelper.spawnParticleAABB(level, ParticleUtils.constructSimpleSpark(color, 0.3f, 30, 0.82f),
                new AABB(start, start), 10, 0.15);
        double length = end.subtract(start).scale((double) 1 / segments).y();
        for(int i = 0; i < segments; i++) {
            straightPos = straightPos.add(end.subtract(start).scale((double) 1 / segments));
            pos = straightPos.add(new Vec3(MathUtils.randomFloat(random) * jag,  0, MathUtils.randomFloat(random) * jag));
            if(i == segments - 1) pos = end;
            ParticleHelper.spawnParticleLine(level, new CircleTintData(color, d, 0, 30, 0.89f, false),
                    prevPos,
                    pos,
                    (int) Math.round((-length * particleCount) * (0.2 + (double) i * i / (segments - 1) / (segments - 1)) * 0.8), 0);
            prevPos = pos;
        }
        ParticleHelper.spawnParticleAABB(level, ParticleUtils.constructSimpleSpark(lightningColors[0], 0.3f, 50, 0.85f),
                new AABB(end, end), 10, 0.15);
        ParticleHelper.spawnParticleAABB(level, ParticleUtils.constructSimpleSpark(lightningColors[1], 0.3f, 50, 0.85f),
                new AABB(end, end), 10, 0.15);
        ParticleHelper.spawnParticleAABB(level, ParticleUtils.constructSimpleSpark(lightningColors[2], 0.5f, 30, 0.75f),
                new AABB(end, end), 8, 0.15);
        ParticleHelper.spawnParticleAABB(level, ParticleUtils.constructSimpleSpark(lightningColors[3], 0.5f, 30, 0.75f),
                new AABB(end, end), 8, 0.15);
    }

    public void drawFrame(){
        a = 30;
        Color color = new Color(26, 255, 210);
        for(int i = 0; i < 80; i++){
            if(i + 40 <= this.tickCount) {
                Vec3 pos = this.getPosition(1F).subtract(new Vec3(0,0,-2)).add(new Vec3(radius + 7, 0, 0).yRot((float) Math.toRadians(a)));
                level().addParticle(new CircleTintData(color, (float) (radius / 20.0) * (1 - (float) ((i - 40) * (i - 40)) / 1600) + 0.1f, 0, 1, 1, false),
                        true,
                        pos.x(), pos.y() - 2 - radius / 6, pos.z(), 0, 0, 0);
//new Color(0, 21, 255)
                pos = this.getPosition(1F).subtract(new Vec3(0,0,2)).add(new Vec3(radius + 7, 0, 0).yRot((float) Math.toRadians(-a)));
                level().addParticle(new CircleTintData(color, (float) (radius / 20.0) * (1 - (float) ((i - 40) * (i - 40)) / 1600) + 0.1f, 0, 1, 1, false),
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
                Vec3 pos = this.getPosition(1F).add(new Vec3(radius * 1.6 - a, 0, 0));
                level().addParticle(new CircleTintData(new Color(0, 255, 157), (float) (radius / 20.0) * (1 - (float) ((i - 45) * (i - 45)) / 2025) + 0.1f, 0, 1, 1f, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z(), 0, 0, 0);
//new Color(0, 81, 255)
                a += (radius * 1.6) / 45;
            }
        }
    }

    public void drawInnerCircles(){
        a = 0;
        Color color = new Color(19, 255, 165);
        for(int i = 0; i < 40; i++){
            if(i + 70 <= this.tickCount) {
                Vec3 pos = this.getPosition(1F).add(new Vec3(0.2,0,0)).add(new Vec3(radius / 2, 0, 0).yRot((float) Math.toRadians(a))).add(radius / 30, 0, 0);
                level().addParticle(new CircleTintData(color, (float) (radius / 20.0) * ((float) ((i - 80) * (i - 80) - 1) / 6400) + 0.05f, 0, 1, 1, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z(), 0, 0, 0);
//new Color(0, 172, 201)
                pos = this.getPosition(1F).add(new Vec3(0.2,0,0)).add(new Vec3(radius / 2, 0, 0).yRot((float) Math.toRadians(-a))).add(radius / 30, 0, 0);
                level().addParticle(new CircleTintData(color, (float) (radius / 20.0) * ((float) ((i - 80) * (i - 80) - 1) / 6400) + 0.05f, 0, 1, 1, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z(), 0, 0, 0);
                a += 1.8;
            }
        }
        b = 0;
        for(int i = 0; i < 45; i++){
            if(i + 80 <= this.tickCount) {
                Vec3 pos = this.getPosition(1F).add(new Vec3(-radius / 1.7, 0, 0).yRot((float) Math.toRadians(b)));
                level().addParticle(new CircleTintData(color, (float) (radius / 20.0) * ((float) ((i - 90) * (i - 90) - 1) / 8100) + 0.1f, 0, 1, 1, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z(), 0, 0, 0);
                if(i >= 43) level().addParticle(new CircleTintData(color, (float) (radius / 20.0) * ((float) ((i - 90) * (i - 90) - 1) / 8100) + 0.1f, 0, 1, 1, false), true,
                        pos.x() + 0.05, pos.y() - 1 - radius / 6, pos.z() + 0.1, 0, 0, 0);
                if(i == 44) level().addParticle(new CircleTintData(color, (float) (radius / 20.0) * ((float) ((i - 90) * (i - 90) - 1) / 8100) + 0.1f, 0, 1, 1, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z() + 0.3, 0, 0, 0);

                pos = this.getPosition(1F).add(new Vec3(-radius / 1.7, 0, 0).yRot((float) Math.toRadians(-b)));
                level().addParticle(new CircleTintData(color, (float) (radius / 20.0) * ((float) ((i - 90) * (i - 90) - 1) / 8100) + 0.1f, 0, 1, 1, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z(), 0, 0, 0);
                if(i >= 43) level().addParticle(new CircleTintData(color, (float) (radius / 20.0) * ((float) ((i - 90) * (i - 90) - 1) / 8100) + 0.1f, 0, 1, 1, false), true,
                        pos.x() + 0.05, pos.y() - 1 - radius / 6, pos.z() - 0.1, 0, 0, 0);
                if(i == 44) level().addParticle(new CircleTintData(color, (float) (radius / 20.0) * ((float) ((i - 90) * (i - 90) - 1) / 8100) + 0.1f, 0, 1, 1, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z() - 0.3, 0, 0, 0);
                b += 2;
            }
        }
    }

    public void drawDiamond(){
        Color color = new Color(37, 255, 179);
        a = 0;
        b = 0;
        for(int i = 0; i < 20; i++){
            if(i + 80 <= this.tickCount) {
                Vec3 pos = this.getPosition(1F).add(new Vec3(b, 0, radius / 3 - a));
                level().addParticle(new CircleTintData(color, (float) (radius / 40.0) * (1 - (float) ((i - 20) * (i - 20)) / 400) + 0.05f, 0, 1, 1, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z(), 0, 0, 0);

                pos = this.getPosition(1F).add(new Vec3(-b, 0, radius / 3 - a));
                level().addParticle(new CircleTintData(color, (float) (radius / 40.0) * (1 - (float) ((i - 20) * (i - 20)) / 400) + 0.05f, 0, 1, 1, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z(), 0, 0, 0);

                pos = this.getPosition(1F).add(new Vec3(b, 0, - radius / 3 + a));
                level().addParticle(new CircleTintData(color, (float) (radius / 40.0) * (1 - (float) ((i - 20) * (i - 20)) / 400) + 0.05f, 0, 1, 1, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z(), 0, 0, 0);

                pos = this.getPosition(1F).add(new Vec3(-b, 0, - radius / 3 + a));
                level().addParticle(new CircleTintData(color, (float) (radius / 40.0) * (1 - (float) ((i - 20) * (i - 20)) / 400) + 0.05f, 0, 1, 1, false), true,
                        pos.x(), pos.y() - 1 - radius / 6, pos.z(), 0, 0, 0);

                a += radius / 60;
                b += radius / 120;
            }
        }
    }

    public void drawCross(){
        Color color = new Color(255, 168, 79);
        a = 0;
        b = 0;
        for(int i = 0; i < 40; i++){
            if(i + 1200 <= this.tickCount * 10) {
                Vec3 pos = this.getPosition(1F).add(new Vec3(b, 0, -a));

                float size = (float) (radius / 30.0) * ((float) ((i - 40) * (i - 40) - 1) / 1600) + 0.05f;

                level().addParticle(new CircleTintData(color, size, 0, 1, 1, false), true,
                        pos.x(), pos.y() - 1.5 - radius / 6, pos.z(), 0, 0, 0);

                pos = this.getPosition(1F).add(new Vec3(-b, 0, -a));
                level().addParticle(new CircleTintData(color, size, 0, 1, 1, false), true,
                        pos.x(), pos.y() - 1.5 - radius / 6, pos.z(), 0, 0, 0);

                pos = this.getPosition(1F).add(new Vec3(b, 0, +a));
                level().addParticle(new CircleTintData(color, size, 0, 1, 1, false), true,
                        pos.x(), pos.y() - 1.5 - radius / 6, pos.z(), 0, 0, 0);

                pos = this.getPosition(1F).add(new Vec3(-b, 0, a));
                level().addParticle(new CircleTintData(color, size, 0, 1, 1, false), true,
                        pos.x(), pos.y() - 1.5 - radius / 6, pos.z(), 0, 0, 0);

                if(i % 2 == 0) {
                    pos = this.getPosition(1F).add(new Vec3(-b, 0, 0));
                    level().addParticle(new CircleTintData(color, size, 0, 1, 1, false), true,
                            pos.x(), pos.y() - 1.5 - radius / 6, pos.z(), 0, 0, 0);
                    pos = this.getPosition(1F).add(new Vec3(b, 0, 0));
                    level().addParticle(new CircleTintData(color, size, 0, 1, 1, false), true,
                            pos.x(), pos.y() - 1.5 - radius / 6, pos.z(), 0, 0, 0);
                }

                a += radius / 100;
                b += radius / 190;
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
        this.entityData.define(OWNER_UUID, "");
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setRadius(compound.getFloat("radius"));
        setDmg(compound.getFloat("dmg"));
        setHeal(compound.getFloat("heal"));
        setFreq(compound.getInt("freq"));
        setLifeTime(compound.getInt("lifetime"));
        setOwnerUUID(compound.getString("owneruuid"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("radius", getRadius());
        compound.putFloat("dmg", getDmg());
        compound.putFloat("heal", getHeal());
        compound.putInt("freq", getFreq());
        compound.putInt("lifetime", getLifeTime());
        compound.putString("owneruuid", getOwnerUUID());
    }

    private static class DelayedRunnable {
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
