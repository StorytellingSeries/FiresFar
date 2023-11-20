package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.client.particles.circle.CircleTintData;
import it.hurts.sskirillss.relics.items.relics.base.utils.LevelingUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KnefProjectile extends ThrowableProjectile
{
    public Vec3 prevPos;
    public LivingEntity target;

    public Color color;

    private ItemStack bow = ItemStack.EMPTY;

    public void setBow(ItemStack bow){
        bow = bow;
    }

    public ItemStack getBow(){
        return  bow;
    }

    private static final EntityDataAccessor<Integer> POWER_ENCH = SynchedEntityData.defineId(KnefProjectile.class, EntityDataSerializers.INT);

    public void setPowerEnch(int powerEnch){
        this.getEntityData().set(POWER_ENCH, powerEnch);
    }

    public int getPowerEnch() {
        return this.getEntityData().get(POWER_ENCH);
    }

    private static final EntityDataAccessor<Integer> BASE_DMG = SynchedEntityData.defineId(KnefProjectile.class, EntityDataSerializers.INT);

    public void setBaseDmg(int baseDmg){
        this.getEntityData().set(BASE_DMG, baseDmg);
    }

    public int getBaseDmg() {
        return this.getEntityData().get(BASE_DMG);
    }

    public KnefProjectile(EntityType<? extends KnefProjectile> type, Level world) {
        super(type, world);
        this.color = new Color(0, 246 - this.random.nextInt(100), 255 - this.random.nextInt(120));

    }

    @Override
    public void tick() {
        if(this.getOwner() == null) this.discard();
        Vec3 motion = this.getDeltaMovement();
        super.tick();

        setDeltaMovement(motion);
        if(!level.isClientSide) {
            ParticleHelper.spawnParticleLine(this.level, new CircleTintData(color, 0.1f, 80, 0.9f, false),
                    prevPos == null ? this.position() : prevPos, this.position(), 15, 0);
        }
        if(this.target != null && target.hasLineOfSight(this)){
            this.setDeltaMovement(getDeltaMovement().add(target.getBoundingBox().getCenter().subtract(this.position()).normalize().scale(0.1f)));
        }

        prevPos = this.position();
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        if(pResult.getEntity() == this.getOwner()) return;
        if(!this.bow.isEmpty()){
            LevelingUtils.addExperience(bow, 1);
        }
        if(!this.level.isClientSide()) {
            ((ServerLevel)this.level).sendParticles(new CircleTintData(new Color(0, 196, 255), 0.2f, 10, 0.55f, false),
                    this.getX(), this.getY(), this.getZ(), 10, 0, 0, 0, 0.1);
            ((ServerLevel)this.level).sendParticles(new CircleTintData(new Color(0, 60, 255), 0.2f, 10, 0.55f, false),
                    this.getX(), this.getY(), this.getZ(), 10, 0, 0, 0, 0.1);

            pResult.getEntity().hurt(DamageSource.thrown(this, this.getOwner()), getBaseDmg() + getPowerEnch() / 2f);
            pResult.getEntity().invulnerableTime = 0;

            float vol = (float) (10 / this.getOwner().distanceToSqr(this.position()));
            this.getLevel().playSound(null, this.getOwner(), SoundsRegistry.KNEF_BOW_SPLASH.get(), SoundSource.PLAYERS, random.nextFloat() * 0.05f * vol + vol, random.nextFloat() * 0.1f + 0.6f);

            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {

        HitResult result = this.level.clip(new ClipContext(this.position(), this.position().add(this.getDeltaMovement().normalize()),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        if(!this.level.isClientSide()) {
            ParticleHelper.spawnParticleLine(this.level, new CircleTintData(color, 0.1f, 80, 0.9f, false),
                    this.position(), result.getLocation(), (int) Math.round(Math.sqrt(this.position().distanceToSqr(result.getLocation())) * 10), 0);
            ((ServerLevel)this.level).sendParticles(new CircleTintData(new Color(0, 196, 255), 0.2f, 10, 0.55f, false),
                    result.getLocation().x(), result.getLocation().y(), result.getLocation().z(), 10, 0, 0, 0, 0.1);
            ((ServerLevel)this.level).sendParticles(new CircleTintData(new Color(0, 60, 255), 0.2f, 10, 0.55f, false),
                    result.getLocation().x(), result.getLocation().y(), result.getLocation().z(), 10, 0, 0, 0, 0.1);
//            ParticleHelper.spawnParticleEntity(new CircleTintData(new Color(0, 196, 255), 0.2f, 10, 0.55f, false),
//                    this, 10, 0.1);
//            ParticleHelper.spawnParticleEntity(new CircleTintData(new Color(0, 60, 255), 0.2f, 10, 0.55f, false),
//                    this, 10, 0.1);
        }
        float vol = (float) (10 / this.getOwner().distanceToSqr(this.position()));
        this.getLevel().playSound(null, this.getOwner(), SoundsRegistry.KNEF_BOW_SPLASH.get(), SoundSource.PLAYERS, random.nextFloat() * 0.05f * vol + vol, random.nextFloat() * 0.1f + 0.6f);

        this.discard();
    }

    @Override
    public void checkDespawn() {
        if(this.tickCount > 240){
            this.discard();
        }
    }

    public static List<KnefProjectile> makeList(int count, Level level, Entity owner, Vec3 center, Vec3 move, int powerEnch, ItemStack bow){
        List<KnefProjectile> list = new ArrayList<>();
        for (int i = 0; i < count; i++){
            KnefProjectile proj = new KnefProjectile(EntityRegistry.KNEF_PROJECTILE, level);
            proj.setOwner(owner);
            proj.setPos(center);
            proj.setDeltaMovement(move);
            proj.color = new Color(0, 246 - proj.random.nextInt(160), 255 - proj.random.nextInt(120));
            proj.setPowerEnch(powerEnch);
            proj.setBow(bow);
            list.add(proj);
        }
        return list;
    }

    @Override
    public boolean canCollideWith(Entity pEntity) {
        return (pEntity instanceof LivingEntity);
    }

    @SubscribeEvent
    public void onLevelUnload(PlayerEvent.PlayerLoggedOutEvent event) {
        this.discard();
    }

    @Override
    protected void defineSynchedData() {

        this.entityData.define(POWER_ENCH, 0);
        this.entityData.define(BASE_DMG, 2);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setPowerEnch(compound.getInt("powerench"));
        setBaseDmg(compound.getInt("basedmg"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("powerench", getPowerEnch());
        compound.putInt("basedmg", getBaseDmg());
    }

    @Override
    public boolean shouldRender(double p_20296_, double p_20297_, double p_20298_) {
        return false;
    }
}
