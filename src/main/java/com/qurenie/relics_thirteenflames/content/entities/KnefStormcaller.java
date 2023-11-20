package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.client.particles.circle.CircleTintData;
import it.hurts.sskirillss.relics.items.relics.base.utils.AbilityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class KnefStormcaller extends ThrowableProjectile
{



    private static final EntityDataAccessor<ItemStack> BOW = SynchedEntityData.defineId(KnefStormcaller.class, EntityDataSerializers.ITEM_STACK);

    public void setBow(ItemStack bow){
        this.getEntityData().set(BOW, bow);
    }

    public ItemStack getBow() {
        return this.getEntityData().get(BOW);
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
        if(this.tickCount % 2 == 0 && !this.level.isClientSide) {
            pos1 = this.position().add(new Vec3(MathUtils.randomFloat(random) * 0.55, MathUtils.randomFloat(random) * 0.45, MathUtils.randomFloat(random) * 0.55));
            pos2 = this.position().add(new Vec3(MathUtils.randomFloat(random) * 0.55, MathUtils.randomFloat(random) * 0.45, MathUtils.randomFloat(random) * 0.55));
            pos4 = this.position().add(new Vec3(MathUtils.randomFloat(random) * 0.55, MathUtils.randomFloat(random) * 0.45, MathUtils.randomFloat(random) * 0.55));
            pos3 = this.position().add(new Vec3(MathUtils.randomFloat(random) * 0.1, MathUtils.randomFloat(random) * 0.1, MathUtils.randomFloat(random) * 0.1));

            ParticleHelper.spawnParticleLine(this.level, new CircleTintData(new Color(201, 75, 255), 0.25f, 100, 0.94f, false),
                    prevPos1 == null ? shotPos : prevPos1,
                    pos1,
                    25, 0);
            ParticleHelper.spawnParticleLine(this.level, new CircleTintData(new Color(143, 82, 255), 0.25f, 100, 0.94f, false),
                    prevPos2 == null ? shotPos : prevPos2,
                    pos2,
                    25, 0);
            ParticleHelper.spawnParticleLine(this.level, new CircleTintData(new Color(167, 106, 255), 0.25f, 100, 0.94f, false),
                    prevPos4 == null ? shotPos : prevPos4,
                    pos4,
                    25, 0);
            ParticleHelper.spawnParticleLine(this.level, new CircleTintData(new Color(0, 34, 255), 0.35f, 100, 0.94f, false),
                    prevPos3 == null ? shotPos : prevPos3,
                    pos3,
                    25, 0);

            prevPos1 = this.pos1;
            prevPos2 = this.pos2;
            prevPos3 = this.pos3;
            prevPos4 = this.pos4;
        }
        //-----------------------GRAPHENE_END--------------------------//

        if(this.getY() > this.shotPos.y + 100){

            //-------------------------GRAPHENE----------------------------//
            if(!this.level.isClientSide()) {
                for (int i = 0; i < 120; i++) {
                    Vec3 direction = new Vec3(1,0,0);
                    direction = direction.yRot((float) Math.toRadians(random.nextFloat() * 360f)).scale(random.nextFloat() * 0.8f);
                    ParticleHelper.spawnDirectedParticle(this.level, new CircleTintData(new Color(0, 15, 49), 4.2f, 130, 0.95f, false),
                            this.getX(), this.getY(), this.getZ(), direction.x, MathUtils.randomFloat(random) * 0.1, direction.z);
                    direction = direction.yRot((float) Math.toRadians(random.nextFloat() * 360f)).normalize().scale(random.nextFloat() * 0.8f);
                    ParticleHelper.spawnDirectedParticle(this.level, new CircleTintData(new Color(28, 0, 27), 4.2f, 130, 0.95f, false),
                            this.getX(), this.getY(), this.getZ(), direction.x, MathUtils.randomFloat(random) * 0.1, direction.z);
//                    this.level.addParticle(new CircleTintData(new Color(0, 24, 80), 4.2f, 130, 0.95f, false),
//                            this.getX(), this.getY(), this.getZ(), MathUtils.randomFloat(random), MathUtils.randomFloat(random) * 0.1, MathUtils.randomFloat(random));
//                    this.level.addParticle(new CircleTintData(new Color(0, 7, 14), 4.2f, 130, 0.95f, false),
//                            this.getX(), this.getY(), this.getZ(), MathUtils.randomFloat(random) * 1.2, MathUtils.randomFloat(random) * 0.1, MathUtils.randomFloat(random) * 1.2);
                }
            }
            //-----------------------GRAPHENE_END--------------------------//




            if(this.getBow().is(ItemsRegistry.KNEF_BOW) && this.getOwner() instanceof Player player) {
                KnefStormEntity storm = new KnefStormEntity(EntityRegistry.KNEF_STORM, this.getLevel());
                storm.setPos(this.getPosition(1F));
                storm.setRadius((float) AbilityUtils.getAbilityValue(getBow(),"storm", "radius"));
                storm.setLifeTime((int) (AbilityUtils.getAbilityValue(getBow(),"storm", "dur") * 20));
                storm.setOwner(player);
                storm.setFreq( (int)Math.round(AbilityUtils.getAbilityValue(getBow(),"storm", "freq")));
                this.getLevel().addFreshEntity(storm);
                if(!this.getLevel().isClientSide()) this.getLevel().playSound(null, this.getOwner(),
                        AbilityUtils.getAbilityValue(getBow(),"storm", "radius") > 5 ? SoundsRegistry.KNEF_BOW_STORM.get() : SoundsRegistry.KNEF_BOW_STORM_SHORT.get(),
                        SoundSource.PLAYERS, random.nextFloat() * 0.6f + 1f, random.nextFloat() * 0.2f + 0.8f);
            }
            this.discard();

        }




    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        if(!this.level.isClientSide()) {
            ParticleHelper.spawnParticleLine(this.level, new CircleTintData(new Color(201, 75, 255), 0.25f, 100, 0.94f, false),
                    prevPos1 == null ? this.position() : prevPos1,
                    this.position(),
                    25, 0);
            ParticleHelper.spawnParticleLine(this.level, new CircleTintData(new Color(143, 82, 255), 0.25f, 100, 0.94f, false),
                    prevPos2 == null ? this.position() : prevPos2,
                    this.position(),
                    25, 0);
            ParticleHelper.spawnParticleLine(this.level, new CircleTintData(new Color(167, 106, 255), 0.25f, 100, 0.94f, false),
                    prevPos4 == null ? this.position() : prevPos4,
                    this.position(),
                    25, 0);
            ParticleHelper.spawnParticleLine(this.level, new CircleTintData(new Color(0, 34, 255), 0.35f, 100, 0.94f, false),
                    prevPos3 == null ? this.position() : prevPos3,
                    pos3,
                    25, 0);

        }

        KnefDischarge discharge = new KnefDischarge(EntityRegistry.KNEF_DISCHARGE, this.level);
        Vec3 pos = this.position();
        discharge.setPos(pos);
        discharge.setOwner(this.getOwner());
        discharge.shotPos = pos;
        discharge.shootFromRotation(this, 0, -90, 0.1f, 6f, 0);
        this.level.addFreshEntity(discharge);

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

    @Override
    public boolean canCollideWith(Entity pEntity) {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(BOW, ItemStack.EMPTY);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setBow(ItemStack.of(compound.getCompound("bow")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.put("bow", getBow().save(new CompoundTag()));
    }

}
