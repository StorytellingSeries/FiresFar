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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.*;

public class KnefDischarge extends ThrowableProjectile
{



    private static final EntityDataAccessor<ItemStack> BOW = SynchedEntityData.defineId(KnefDischarge.class, EntityDataSerializers.ITEM_STACK);

    public void setBow(ItemStack bow){
        this.getEntityData().set(BOW, bow);
    }

    public ItemStack getBow() {
        return this.getEntityData().get(BOW);
    }

    public Vec3 shotPos;

    private boolean isExploding = false;

    public KnefDischarge(EntityType<? extends KnefDischarge> type, Level world) {
        super(type, world);

    }

    @Override
    public void tick() {
        Vec3 motion = this.getDeltaMovement();
        super.tick();
        setDeltaMovement( isExploding ? motion : Vec3.ZERO);
        if(shotPos == null) shotPos = this.position();



        //-------------------------GRAPHENE----------------------------//
        if(!this.level.isClientSide) {
            spark(2);
        }
        //-----------------------GRAPHENE_END--------------------------//

        if(this.tickCount > 30) this.discard();





    }

    private void spark(int count) {
        ParticleHelper.spawnParticleEntity(new CircleTintData(new Color(167, 106, 255), 0.4f, 60, 0.91f, false),
                this, count, 0.04);
        ParticleHelper.spawnParticleEntity(new CircleTintData(new Color(68, 38, 203), 0.4f, 60, 0.91f, false),
                this, count, 0.04);

        ParticleHelper.spawnParticleEntity(new SparkTintData(new Color(179, 190, 255), 0.4f, 184),
                this, count, 0.04);
        ParticleHelper.spawnParticleEntity(new SparkTintData(new Color(242, 208, 255), 0.4f, 184),
                this, count, 0.04);
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        isExploding = true;

    }

    @Override
    public void onRemovedFromWorld() {
        spark(8);
        super.onRemovedFromWorld();
    }

    @SubscribeEvent
    public void onLevelUnload(PlayerEvent.PlayerLoggedOutEvent event) {
        this.discard();
    }

    @Override
    public void checkDespawn() {
        if(this.tickCount > 30){
            this.discard();
        }
    }

    @Override
    public boolean canCollideWith(Entity pEntity) {
        return false;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
    }

}
