package com.qurenie.relics_thirteenflames.content.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class KnefProjCarrier extends ThrowableProjectile
{

    private static final EntityDataAccessor<String> OWNER_UUID = SynchedEntityData.defineId(KnefProjCarrier.class, EntityDataSerializers.STRING);

    public void setOwnerUUID(String uuid){
        this.getEntityData().set(OWNER_UUID, uuid);
    }

    public String getOwnerUUID() {
        return this.getEntityData().get(OWNER_UUID);
    }

    public List<KnefProjectile> rays = new ArrayList<>();

    public double rad = 0.06;
    public KnefProjCarrier(EntityType<? extends KnefProjCarrier> type, Level world) {
        super(type, world);

    }

    public KnefProjCarrier setRays(List<KnefProjectile> rays){
        this.rays = rays;
        return this;
    }


    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_UUID, "");
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setOwnerUUID(compound.getString("owneruuid"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("owneruuid", getOwnerUUID());
    }

    @Override
    public void tick() {
        Vec3 motion = this.getDeltaMovement();

        for (int i = 0; i < rays.size(); i++){
            if(rays.get(i).prevPos == null) {
                double a = 360.0 / rays.size() * i - this.tickCount * 10.0;
                double radius = rad + Math.sin(Math.toRadians(this.tickCount * 20.0) - 90) * 0.04;
                if (i % 2 == 0 && rays.size() > 7) {
                    radius += 0.1;
                    if (i % 4 == 0 && rays.size() > 15) radius -= 0.1;
                }
                Vec3 x = !(motion.normalize().x < 0.001 && motion.normalize().z < 0.001) ? motion.normalize().cross(new Vec3(0, 1, 0)).normalize().scale(radius) : motion.normalize().cross(new Vec3(1, 0, 0)).normalize().scale(radius);
                Vec3 z = motion.normalize().cross(x).normalize().scale(radius);

                Vec3 pos = this.getPosition(1F)
                        .add(x.scale(Math.cos(Math.toRadians(a))))
                        .add(z.scale(Math.sin(Math.toRadians(a))))
                        //.subtract(motion.scale((double) i / rays.size() * 2))
                        ;

                if (i % 2 == 0) {
                    pos = pos.add(motion.scale(0.3));
                    if (i % 4 == 0 && rays.size() > 15) pos = pos.subtract(motion.scale(0.3));
                }
                rays.get(i).prevPos = pos;
            }
        }

        super.tick();

        setDeltaMovement(motion);

        for (int i = 0; i < rays.size(); i++){

            double a = 360.0 / rays.size() * i - this.tickCount * 10.0;
            double radius = rad + Math.sin(Math.toRadians(this.tickCount * 20.0) - 90) * 0.04;
            if(i % 2 == 0 && rays.size() > 7){
                radius += 0.1;
                if(i % 4 == 0 && rays.size() > 15) radius -= 0.1;
            }
            Vec3 x = !( motion.normalize().x < 0.001 && motion.normalize().z < 0.001 ) ? motion.normalize().cross(new Vec3(0,1,0)).normalize().scale(radius) : motion.normalize().cross(new Vec3(1,0,0)).normalize().scale(radius);
            Vec3 z = motion.normalize().cross(x).normalize().scale(radius);

            Vec3 pos = this.getPosition(1F)
                    .add(x.scale(Math.cos(Math.toRadians(a))))
                    .add(z.scale(Math.sin(Math.toRadians(a))))
                    ;

            if(i % 2 == 0){
                pos = pos.add(motion.scale(0.3));
                if(i % 4 == 0 && rays.size() > 15) pos = pos.subtract(motion.scale(0.3));
            }
            rays.get(i).setPos(pos);
        }

        if(!this.level().isClientSide() ) {

            AABB box = this.getBoundingBox().inflate(7);

            List<LivingEntity> targets = new ArrayList<>(this.level().getEntitiesOfClass(LivingEntity.class, box, e -> !(e.getStringUUID().equals(this.getOwnerUUID()) ) && e.isAlive() && e.hasLineOfSight(this)));

            if (!targets.isEmpty()) {
                for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, box.move(motion.scale(20)).inflate(1), e -> !(e.getStringUUID().equals(this.getOwnerUUID()))  && e.hasLineOfSight(this))) {
                    if (!targets.contains(target)) targets.add(target);
                }

                int cap = rays.size() / targets.size();
                if (cap == 0) {
                    for (int i = 0; i < rays.size(); i++) {
                        rays.get(i).target = targets.get(i);
                        rays.get(i).setFree(true);
                    }
                    rays.clear();
                    this.discard();
                } else {
                    for (LivingEntity target : targets) {
                        for (int i = 0; i < cap; i++) {
                            KnefProjectile proj = rays.remove(0);
                            proj.target = target;
                            proj.setFree(true);
                        }
                    }
                    for (int i = 0; i < rays.size(); i++) {
                        rays.get(i).target = targets.get(i);
                        rays.get(i).setFree(true);
                    }
                    rays.clear();
                    this.discard();
                }
            }
        }
    }

    @SubscribeEvent
    public void onLevelUnload(PlayerEvent.PlayerLoggedOutEvent event) {
        this.rays.clear();
        this.discard();
    }
    @Override
    public void checkDespawn() {
        if(this.tickCount > 240 || rays.isEmpty()){
            this.discard();
        }
    }

    @Override
    public boolean canCollideWith(Entity pEntity) {
        return false;
    }
}
