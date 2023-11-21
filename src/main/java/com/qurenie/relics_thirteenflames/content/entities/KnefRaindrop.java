package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.client.particles.circle.CircleTintData;
import it.hurts.sskirillss.relics.items.relics.base.utils.LevelingUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class KnefRaindrop extends ThrowableProjectile
{

    public Vec3 movement;
    public Vec3 prevPos;

    public Color color;

    private ItemStack bow = ItemStack.EMPTY;

    public void setBow(ItemStack bow){
        this.bow = bow;
    }

    public ItemStack getBow(){
        return  bow;
    }
    private float heal = 0;

    public void setHeal(float heal){
        this.heal = heal;
    }

    public float getHeal(){
        return  heal;
    }

    private float dmg = 0;

    public void setDmg(float dmg){
        this.dmg = dmg;
    }

    public float getDmg(){
        return  dmg;
    }

    private static final EntityDataAccessor<Integer> BASE_DMG = SynchedEntityData.defineId(KnefRaindrop.class, EntityDataSerializers.INT);

    public void setBaseDmg(int baseDmg){
        this.getEntityData().set(BASE_DMG, baseDmg);
    }

    public int getBaseDmg() {
        return this.getEntityData().get(BASE_DMG);
    }

    public KnefRaindrop(EntityType<? extends KnefRaindrop> type, Level world) {
        super(type, world);
        this.color = new Color(0, 86 - this.random.nextInt(80), 255 - this.random.nextInt(90));

    }


    @Override
    public void tick() {
        movement = this.getDeltaMovement();
        super.tick();

        setDeltaMovement(movement);
        if(level.isClientSide) {
            double distance = this.position().subtract(prevPos == null ? this.position() : prevPos).length();
            ParticleHelper.spawnParticleLine(this.level, new CircleTintData(color, 0.1f, 80, 0.85f, false),
                    prevPos == null ? this.position() : prevPos, this.position(), (int) Math.round(distance * this.tickCount * this.tickCount / 156 + 2), 0);
        }

        prevPos = this.position();
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        if(pResult.getEntity() instanceof LivingEntity living) {
            if (this.getOwner() != null && pResult.getEntity().equals(this.getOwner())) {
                living.heal(living.getMaxHealth() * getHeal());
                if(!getBow().isEmpty()) LevelingUtils.addExperience(getBow(), Math.round(Math.min(living.getMaxHealth() * getHeal(), living.getMaxHealth() - living.getHealth())));
            } else {
                pResult.getEntity().hurt(DamageSource.thrown(this, this.getOwner()), getDmg());
                pResult.getEntity().invulnerableTime = 0;
            }

            this.getLevel().playSound(null, pResult.getEntity(), SoundsRegistry.KNEF_BOW_SPLASH.get(), SoundSource.PLAYERS, random.nextFloat() * 0.2f + 0.1f, random.nextFloat() * 0.6f + 0.7f);
        }
    }


    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        HitResult result = this.level.clip(new ClipContext(this.position(), this.position().add(0, -5, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        Vec3 loc = pResult.getLocation();
        this.getLevel().playSound(null, loc.x, loc.y, loc.z, SoundsRegistry.KNEF_BOW_SPLASH.get(), SoundSource.PLAYERS, random.nextFloat() * 0.2f + 0.1f, random.nextFloat() * 0.6f + 0.7f);

        if (result.getType() == HitResult.Type.BLOCK) {
            ParticleHelper.spawnParticleLine(this.level, new CircleTintData(color, 0.1f, 80, 0.9f, false),
                    this.position(), result.getLocation(), (int) Math.round(Math.sqrt(this.position().distanceToSqr(result.getLocation())) * this.tickCount * this.tickCount / 156 + 2), 0);

            ParticleHelper.spawnParticleAABB(this.level, new CircleTintData(new Color(72, 0, 255), 0.2f, 20, 0.65f, false),
                    new AABB(result.getLocation(), result.getLocation()), 15, 0.1);
            ParticleHelper.spawnParticleAABB(this.level, new CircleTintData(new Color(0, 60, 255), 0.2f, 20, 0.65f, false),
                    new AABB(result.getLocation(), result.getLocation()), 15, 0.1);
        }
        this.discard();

    }

    @Override
    public void checkDespawn() {
        if(this.tickCount > 240){
            this.discard();
        }
    }

    @Override
    public boolean canCollideWith(Entity pEntity) {
        return false;
    }

    @SubscribeEvent
    public void onLevelUnload(PlayerEvent.PlayerLoggedOutEvent event) {
        this.discard();
    }

    @Override
    protected void defineSynchedData() {

        this.entityData.define(BASE_DMG, 2);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setBaseDmg(compound.getInt("basedmg"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("basedmg", getBaseDmg());
    }
}
