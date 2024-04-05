package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.content.items.ItemKnefBow;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KnefStormcaller extends ThrowableProjectile
{



    private static final EntityDataAccessor<ItemStack> BOW = SynchedEntityData.defineId(KnefStormcaller.class, EntityDataSerializers.ITEM_STACK);

    public void setBow(ItemStack bow){
        this.getEntityData().set(BOW, bow);
    }

    public ItemStack getBow() {
        return this.getEntityData().get(BOW);
    }

    private static final EntityDataAccessor<String> OWNER_UUID = SynchedEntityData.defineId(KnefStormcaller.class, EntityDataSerializers.STRING);

    public void setOwnerUUID(String uuid){
        this.getEntityData().set(OWNER_UUID, uuid);
    }

    public String getOwnerUUID() {
        return this.getEntityData().get(OWNER_UUID);
    }

    public List<KnefProjectileSpecial> rays = new ArrayList<>();

    public double rad = 0.05;

    public void setRays(List<KnefProjectileSpecial> rays){
        this.rays = rays;
    }

    public Vec3 prevPos1, prevPos2, prevPos3, prevPos4, pos1, pos2, pos3, pos4, shotPos;

    public KnefStormcaller(EntityType<? extends KnefStormcaller> type, Level world) {
        super(type, world);

    }

    @Override
    public void tick() {
        Vec3 motion = this.getDeltaMovement();
        super.tick();
        setDeltaMovement(motion);

        if(shotPos == null) shotPos = this.getPosition(1F);



        //-------------------------GRAPHENE----------------------------//

        for (int i = 0; i < rays.size(); i++){

            double a = 360.0 / rays.size() * i - this.tickCount * 20.0;
            double radius = rad + Math.sin(Math.toRadians(this.tickCount * 20.0) - 90) * 0.04;

            if(i % 2 == 0){
                radius += 0.1;
            }

            Vec3 x = !( motion.normalize().x < 0.001 && motion.normalize().z < 0.001 ) ? motion.normalize().cross(new Vec3(0,1,0)).normalize().scale(radius) : motion.normalize().cross(new Vec3(1,0,0)).normalize().scale(radius);
            Vec3 z = motion.normalize().cross(x).normalize().scale(radius);

            Vec3 pos = this.getPosition(1F)
                    .add(x.scale(Math.cos(Math.toRadians(a))))
                    .add(z.scale(Math.sin(Math.toRadians(a))))
                    ;
            if(i % 2 == 0){
                pos = pos.add(motion.scale(-0.3));
            }

            rays.get(i).setPos(pos);
        }

        pos1 = this.position().add(new Vec3(MathUtils.randomFloat(random) * 0.15, MathUtils.randomFloat(random) * 0.15, MathUtils.randomFloat(random) * 0.15));
        pos2 = this.position().add(new Vec3(MathUtils.randomFloat(random) * 0.15, MathUtils.randomFloat(random) * 0.15, MathUtils.randomFloat(random) * 0.15));
        pos4 = this.position().add(new Vec3(MathUtils.randomFloat(random) * 0.15, MathUtils.randomFloat(random) * 0.15, MathUtils.randomFloat(random) * 0.15));
        pos3 = this.position();

        if(this.tickCount % 2 == 0 && !this.level().isClientSide) {

//            ParticleHelper.spawnParticleLine(this.level, new CircleTintData(new Color(201, 75, 255), 0.25f, 100, 0.94f, false),
//                    prevPos1 == null ? shotPos : prevPos1,
//                    pos1,
//                    25, 0);
//            ParticleHelper.spawnParticleLine(this.level, new CircleTintData(new Color(143, 82, 255), 0.25f, 100, 0.94f, false),
//                    prevPos2 == null ? shotPos : prevPos2,
//                    pos2,
//                    25, 0);
//            ParticleHelper.spawnParticleLine(this.level, new CircleTintData(new Color(167, 106, 255), 0.25f, 100, 0.94f, false),
//                    prevPos4 == null ? shotPos : prevPos4,
//                    pos4,
//                    25, 0);
            ParticleHelper.spawnParticleLine(this.level(), ParticleUtils.constructSimpleSpark(new Color(0, 34, 255), 0.3f, 40, 0.91f),
                    prevPos3 == null ? shotPos : prevPos3,
                    pos3,
                    25, 0);

            prevPos1 = this.pos1;
            prevPos2 = this.pos2;
            prevPos3 = this.pos3;
            prevPos4 = this.pos4;
        }
        //-----------------------GRAPHENE_END--------------------------//

        if(this.getY() > this.shotPos.y + 90){

            //-------------------------GRAPHENE----------------------------//
            if(!this.level().isClientSide()) {
                for (int i = 0; i < 120; i++) {
                    Vec3 direction = new Vec3(1,0,0);
                    direction = direction.yRot((float) Math.toRadians(random.nextFloat() * 360f)).scale(random.nextFloat() * 0.8f);
                    ParticleHelper.spawnDirectedParticle(this.level(), ParticleUtils.constructSimpleSpark(new Color(0, 15, 49), 4.2f, 80, 0.95f),
                            this.getX(), this.getY(), this.getZ(), direction.x, MathUtils.randomFloat(random) * 0.1, direction.z);
                    direction = direction.yRot((float) Math.toRadians(random.nextFloat() * 360f)).normalize().scale(random.nextFloat() * 0.8f);
                    ParticleHelper.spawnDirectedParticle(this.level(), ParticleUtils.constructSimpleSpark(new Color(28, 0, 27), 4.2f, 80, 0.95f),
                            this.getX(), this.getY(), this.getZ(), direction.x, MathUtils.randomFloat(random) * 0.1, direction.z);
//                    this.level.addParticle(new CircleTintData(new Color(0, 24, 80), 4.2f, 130, 0.95f, false),
//                            this.getX(), this.getY(), this.getZ(), MathUtils.randomFloat(random), MathUtils.randomFloat(random) * 0.1, MathUtils.randomFloat(random));
//                    this.level.addParticle(new CircleTintData(new Color(0, 7, 14), 4.2f, 130, 0.95f, false),
//                            this.getX(), this.getY(), this.getZ(), MathUtils.randomFloat(random) * 1.2, MathUtils.randomFloat(random) * 0.1, MathUtils.randomFloat(random) * 1.2);
                }
            }
            //-----------------------GRAPHENE_END--------------------------//




            if(!this.level().isClientSide() && this.getBow().getItem() instanceof ItemKnefBow relic && !this.getOwnerUUID().isEmpty()) {
                Entity owner = ((ServerLevel) this.level()).getEntity(UUID.fromString(this.getOwnerUUID()));

                if(owner != null) {
                    KnefStormEntity storm = new KnefStormEntity(EntityRegistry.KNEF_STORM, this.level());
                    storm.setPos(this.getPosition(1F));
                    storm.setRadius((float) relic.getAbilityValue(getBow(), "storm", "radius"));
                    storm.setLifeTime((int) (relic.getAbilityValue(getBow(), "storm", "dur") * 20));
                    storm.setOwner(owner);
                    storm.setOwnerUUID(owner.getStringUUID());
                    storm.setFreq((int) Math.round(4 - relic.getAbilityPoints(getBow(), "storm") * 0.6));
                    storm.setBow(getBow());
                    storm.setDmg((float) relic.getAbilityValue(getBow(), "storm", "dmg") + getBow().getEnchantmentLevel(Enchantments.POWER_ARROWS) / 2.5f);
                    storm.setHeal((float) relic.getAbilityValue(getBow(), "storm", "heal") / 100);
                    this.level().addFreshEntity(storm);
                    this.level().playSound(null, owner,
                            relic.getAbilityValue(getBow(), "storm", "radius") > 5 ? SoundsRegistry.KNEF_BOW_STORM.get() : SoundsRegistry.KNEF_BOW_STORM_SHORT.get(),
                            SoundSource.PLAYERS, random.nextFloat() * 0.6f + 1f, random.nextFloat() * 0.2f + 0.8f);
                }
            }
            this.discard();

        }




    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        if(!this.level().isClientSide()) {
            Entity owner = ((ServerLevel) this.level()).getEntity(UUID.fromString(this.getOwnerUUID()));
            if(owner != null && this.getBow().getItem() instanceof IRelicItem relic) {
                KnefDischarge discharge = new KnefDischarge(EntityRegistry.KNEF_DISCHARGE, this.level());
                Vec3 pos = this.position();
                discharge.setPos(pos);
                discharge.setOwner(owner);
                discharge.setOwnerUUID(this.getOwnerUUID());
                discharge.shotPos = pos;
                discharge.setRadius((float) (relic.getAbilityValue(getBow(), "storm", "radius") * 0.8f));
                discharge.setDmg((float) (relic.getAbilityValue(getBow(), "storm", "dmg") + getBow().getEnchantmentLevel(Enchantments.POWER_ARROWS) / 2.5f) * 6);
                discharge.shootFromRotation(this, 0, -90, 0.0f, 0.0f, 0);
                this.level().addFreshEntity(discharge);
            }
        }

        this.discard();
    }

    @Override
    public void onRemovedFromWorld() {
        for(Entity e : rays) e.discard();
        rays.clear();
        super.onRemovedFromWorld();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if(!this.level().isClientSide()) {
            Entity owner = ((ServerLevel) this.level()).getEntity(UUID.fromString(this.getOwnerUUID()));
            if (owner != null && this.getBow().getItem() instanceof IRelicItem relic) {
                this.setPos(result.getEntity().getBoundingBox().getCenter());
                KnefDischarge discharge = new KnefDischarge(EntityRegistry.KNEF_DISCHARGE, this.level());
                Vec3 pos = result.getEntity().getBoundingBox().getCenter();
                discharge.setPos(pos);
                discharge.setOwner(owner);
                discharge.setOwnerUUID(this.getOwnerUUID());
                discharge.shotPos = pos;
                discharge.setRadius((float) (relic.getAbilityValue(getBow(), "storm", "radius") * 0.8f));
                discharge.setDmg((float) (relic.getAbilityValue(getBow(), "storm", "dmg") + getBow().getEnchantmentLevel(Enchantments.POWER_ARROWS) / 2.5f) * 6);
                discharge.shootFromRotation(this, 0, -90, 0.0f, 0.0f, 0);
                this.level().addFreshEntity(discharge);
            }
        }
        this.discard();
    }

    @SubscribeEvent
    public void onLevelUnload(PlayerEvent.PlayerLoggedOutEvent event) {
        this.discard();
    }

    @Override
    public void checkDespawn() {
        if(this.tickCount > 160){
            this.discard();
        }
    }

//    @Override
//    public boolean canCollideWith(Entity pEntity) {
//        return (pEntity instanceof LivingEntity && pEntity.equals(this.getOwner()));
//    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(BOW, ItemStack.EMPTY);
        this.entityData.define(OWNER_UUID, "owneruuid");
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setBow(ItemStack.of(compound.getCompound("bow")));
        setOwnerUUID(compound.getString("owneruuid"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.put("bow", getBow().save(new CompoundTag()));
        compound.putString("owneruuid", getOwnerUUID());
    }

}
