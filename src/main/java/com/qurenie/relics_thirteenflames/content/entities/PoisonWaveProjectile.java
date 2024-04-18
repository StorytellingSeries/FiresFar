package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.content.effects.PoisonEffectInstance;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class PoisonWaveProjectile extends ThrowableProjectile
{
    public Vec3 prevPos;
    Random rng = new Random();

    public double spreadAngle;

    public Vec3 startVec;
    public Vec3 flatluk;

    HashSet<LivingEntity> entityBlackList = new HashSet<>();

    private ItemStack sword = ItemStack.EMPTY;

    public void setSword(ItemStack sword){
        this.sword = sword;
    }

    public ItemStack getSword(){
        return  sword;
    }


    private static final EntityDataAccessor<Float> MAX_RANGE = SynchedEntityData.defineId(PoisonWaveProjectile.class, EntityDataSerializers.FLOAT);

    public void setMaxRange(float range){
        this.getEntityData().set(MAX_RANGE, range);
    }

    public float getMaxRange() {
        return this.getEntityData().get(MAX_RANGE);
    }


    public PoisonWaveProjectile(EntityType<? extends PoisonWaveProjectile> type, Level world) {
        super(type, world);
    }

    @Override
    public void tick() {
        if(this.getOwner() == null) this.discard();
        Vec3 motion = this.getDeltaMovement();
        super.tick();

        setDeltaMovement(motion);
        if(!level().isClientSide() && this.getOwner() instanceof Player p && sword.getItem() instanceof IRelicItem relic) {

            Vec3 dist = this.position().subtract(startVec);
            if(dist.length() > getMaxRange() + 0.5) this.discard();

            int particlesPer = (int) Math.round(Math.PI * dist.length() * spreadAngle / 90.0) * 3;
            particlesPer += particlesPer % 2 + 1;


            for (int j = 0; j < particlesPer; j++) {

                Vec3 vec = startVec.add(
                        flatluk.scale(dist.length() + 0.1).yRot((float) ((-spreadAngle + spreadAngle / particlesPer * j * 2) * Mth.DEG_TO_RAD))
                ).add(this.position().subtract(startVec.add(flatluk.scale(dist.length() + 0.1))));

                int duration = (int) Math.round(relic.getAbilityValue(sword, "spit", "poisondur") * 20);
                int maxAmp = (int) Math.round(relic.getAbilityValue(sword, "spit", "maxstacks") - 1);
                List<LivingEntity> eList = level().getEntitiesOfClass(LivingEntity.class, new AABB(vec, vec).inflate(0.2), e -> !Objects.equals(e, this.getOwner()) && !entityBlackList.contains(e) );
                entityBlackList.addAll(eList);
                for(LivingEntity lE : eList){
                    int invulTime = p.invulnerableTime;
                    lE.hurt(lE.damageSources().thrown(this, p), 1);
                    p.invulnerableTime = invulTime;
                    if (lE.hasEffect(EffectsRegistry.POISSON)) {
                        int appliedAmplifier = lE.getEffect(EffectsRegistry.POISSON).getAmplifier() + 1;
                        if (appliedAmplifier <= maxAmp) {
                            lE.addEffect(new PoisonEffectInstance(EffectsRegistry.POISSON, duration + appliedAmplifier * 20, appliedAmplifier, false, true, false, sword));
                            if (rng.nextFloat() < 0.25f) relic.spreadExperience(p, sword, 1);
                        } else {
                            lE.addEffect(new PoisonEffectInstance(EffectsRegistry.POISSON, duration + maxAmp * 20, maxAmp, false, true, false, sword));
                        }
                    } else {
                        lE.addEffect(new PoisonEffectInstance(EffectsRegistry.POISSON, duration, 0, false, true, false, sword));
                        if (rng.nextFloat() < 0.25f) relic.spreadExperience(p, sword, 1);
                    }
                }


                double randomSpread = 0.01 * this.tickCount;
                ParticleHelper.spawnParticles(level(), ParticleUtils.constructSimpleSpark(new Color(85 + rng.nextInt(80), 255 - rng.nextInt(100), 0),
                                (float) (0.4F + 0.03f * this.tickCount), 20, 0.83F),
                        vec.x, vec.y, vec.z, 1, randomSpread, randomSpread, randomSpread, 0.002 + this.tickCount * 0.008);
                if (rng.nextFloat() < 0.3f)
                    ParticleHelper.spawnParticles(level(), ParticleUtils.constructSimpleSpark(new Color(85 - rng.nextInt(80), 255 - rng.nextInt(100), 0),
                                    (float) (0.25F + 0.0125f * this.tickCount), 20, 0.8f),
                            vec.x, vec.y, vec.z, 1, randomSpread, randomSpread, randomSpread, 0.002 + this.tickCount * 0.008);
            }
        }

        prevPos = this.position();
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
    }

    @Override
    public void checkDespawn() {
        if(this.tickCount > 100){
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
        this.entityData.define(MAX_RANGE, 4f);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setMaxRange(compound.getInt("maxrange"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("maxrange", getMaxRange());
    }

    @Override
    public boolean shouldRender(double p_20296_, double p_20297_, double p_20298_) {
        return false;
    }
}
