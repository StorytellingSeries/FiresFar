package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.api.event.EntityIgnoreExplosionEvent;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.UUID;

import static com.qurenie.relics_thirteenflames.style.ColorScheme.BURN_COLOR;
import static com.qurenie.relics_thirteenflames.style.ColorScheme.GRAY_COLOR;
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

public class MeteorEntity extends Entity {
    
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> RELEASED = SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.BOOLEAN);
    
    Player owner;
    UUID ownerUUID;
    LivingEntity target;
    private float size;
    private int time;
    
    public MeteorEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }
    
    public MeteorEntity(Level level, Player owner, @Nullable LivingEntity target, float size, int time) {
        super(EntityRegistry.METEOR, level);
        this.owner = owner;
        this.ownerUUID = owner.getUUID();
        this.target = target;
        this.time = target == null ? time : (int) (time * 2.5 + 1);
        setSize(size);

//        while (level.getBlockState(new BlockPos(pos.getX(), pos.getY() + (dy++), pos.getZ())).isAir() && dy <= size * 15)
//            ;
        setPos(owner.getEyePosition());
        owner.startRiding(this);
        this.setDeltaMovement(owner.getLookAngle().normalize().scale(0.01));
//        setNoGravity(true);
    }
    
    @Override
    protected void removePassenger(@NotNull Entity passenger) {
        super.removePassenger(passenger);
        passenger.setInvisible(false);
    }
    
    @SubscribeEvent
    public void onLeave(EntityLeaveLevelEvent event) {
        if (this.getPassengers().contains(event.getEntity()))
            event.getEntity().setInvisible(false);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (!level().isClientSide && (this.getPassengers().isEmpty())) {
            dispel();
            return;
        }
        
        for (var p : getPassengers())
            p.setInvisible(true);
        
        if (this.tickCount >= time && !entityData.get(RELEASED))
            explode();
        
        if (level().isClientSide) {
            Vec3 mov = getDeltaMovement().normalize().scale(-0.04);

            float particleSize = getParticleSize();

            for (int i = 0; i < 10 * getDeltaMovement().length() * 2; i++) {
                double x = (random.nextDouble() * 2 - 1) * particleSize;
                double z = (random.nextDouble() * 2 - 1) * Math.sqrt(particleSize * particleSize - x * x);
                double y = (random.nextBoolean() ? 1 : -1) * Math.sqrt(particleSize * particleSize - x * x - z * z);
                Vec3 pos = position().add(x, y, z);
                if (random.nextBoolean())
                    ParticleHelper.spawnDirectedParticle(level(), ParticleHelper.constructSimpleSpark(GRAY_COLOR, (float) (0.7f + Math.sqrt(particleSize / 2)),
                            40, 0.9f).withLightning(false).withGravity(2f), pos, mov);
                else
                    ParticleHelper.spawnDirectedParticle(level(), ParticleHelper.constructSmoke(GRAY_COLOR, (float) (0.7f + Math.sqrt(particleSize / 2)),
                            40, 0f).withLightning(false).withGravity(2f), pos, mov);
            }
            
            if (random.nextDouble() < getDeltaMovement().length()) {
                double x = (random.nextDouble() * 2 - 1) * particleSize;
                double z = (random.nextDouble() * 2 - 1) * Math.sqrt(particleSize * particleSize - x * x);
                double y = (random.nextBoolean() ? 1 : -1) * Math.sqrt(particleSize * particleSize - x * x - z * z);
                Vec3 pos = position().add(x, y, z);
                for (int i = 0; i < 3 * getDeltaMovement().length() * 2; i++) {
                    if (random.nextBoolean())
                        ParticleHelper.spawnDirectedParticle(level(), ParticleHelper.constructSimpleSpark(BURN_COLOR, (float) (0.7f + Math.sqrt(particleSize / 2)),
                                40, 0.9f).withLightning(false).withGravity(2f), pos, mov);
                    else
                        ParticleHelper.spawnDirectedParticle(level(), ParticleHelper.constructSmoke(BURN_COLOR, (float) (0.7f + Math.sqrt(particleSize / 2)),
                                40, 0f).withLightning(false).withGravity(2f), pos, mov);
                }
            }
            
            return;
        }
        
        List<LivingEntity> livings = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(1, 0, 1), e -> e != owner);
        if (!livings.isEmpty() && !entityData.get(RELEASED)) {
            explode();
            return;
        }
        
        if (!entityData.get(RELEASED)) {
            Vec3 acceleration = target != null ? target.getBoundingBox().getCenter().subtract(this.position()).normalize().scale(0.13)
                    : this.getDeltaMovement().normalize().scale(0.1);
            this.setDeltaMovement(getDeltaMovement().add(acceleration).scale(0.98f));
        } else {
            this.setDeltaMovement(getDeltaMovement().scale(0.84f));
            if (this.getDeltaMovement().length() < 0.2)
                dispel();
        }
        
        Vec3 movement = getDeltaMovement();
        this.moveTo(position().x + movement.x, position().y + movement.y, position().z + movement.z);
        this.checkInsideBlocks();
        
        if (!level().noCollision(getBoundingBox().inflate(0.1))
                || !level().noCollision(getBoundingBox().inflate(0.1).move(getDeltaMovement().scale(-0.5)))) {
            if (!entityData.get(RELEASED))
                explode();
            else
                dispel();
        }
        
    }
    
    protected void explode() {
        this.setDeltaMovement(this.getDeltaMovement().scale(0.7f));
        level().explode(this, damageSources().playerAttack(owner), new ExplosionDamageCalculator(), position(),
                1 + (float) getDeltaMovement().length() * getSize() * 1.5f, false, Level.ExplosionInteraction.TRIGGER);
        level().explode(this, damageSources().playerAttack(owner), new ExplosionDamageCalculator(), position(),
                (float) getDeltaMovement().length() * getSize() / 2, false, Level.ExplosionInteraction.BLOCK);

        var particleSize = getParticleSize();
        ParticleHelper.spawnParticleAABB(level(), ParticleHelper.constructSimpleSpark(GRAY_COLOR, 1f,
                80, 0.95f).withLightning(false).withGravity(2f), this.getBoundingBox().inflate(particleSize - 0.2), 40, 0.08 * getDeltaMovement().length());
        ParticleHelper.spawnParticleAABB(level(), ParticleHelper.constructSmoke(GRAY_COLOR, 1f,
                80, 0f).withLightning(false).withGravity(2f), this.getBoundingBox().inflate(particleSize - 0.2), 40, 0.08 * getDeltaMovement().length());
        this.entityData.set(RELEASED, true);
        
        for (int i = 0; i < 20 * getDeltaMovement().length(); i++)
            if (getDeltaMovement().length() > random.nextDouble())
                ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(BURN_COLOR, (float) (0.7f + Math.sqrt(particleSize / 2)),
                        80, 0.95f).withLightning(true).withGravity(2f), this, 1, 0.08 * getDeltaMovement().length());
    }
    
    @SubscribeEvent
    public void onExplode(EntityIgnoreExplosionEvent explosionEvent) {
        if (explosionEvent.getExplosion().getDirectSourceEntity() == this && this.getPassengers().contains(explosionEvent.getEntity()))
            explosionEvent.setShouldIgnore(true);
    }
    
    protected void dispel() {
        this.discard();
        ParticleHelper.spawnParticleAABB(level(), ParticleHelper.constructSmoke(GRAY_COLOR, 1f,
                70, 0f).withLightning(false).withGravity(2f), this.getBoundingBox().inflate(getParticleSize()), 50, 0.2);
    }
    
    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        EVENT_BUS.register(this);
    }
    
    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        for (var p : this.getPassengers())
            p.setInvisible(false);
        EVENT_BUS.unregister(this);
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        builder.define(SIZE, 0f);
        builder.define(RELEASED, false);
    }

    public float getParticleSize() {
        return 0.6f + getSize() / 2;
    }
    
    public float getSize() {
        return level().isClientSide ? entityData.get(SIZE) : size;
    }
    
    public void setSize(float size) {
        this.size = size;
        entityData.set(SIZE, size);
    }
    
    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compound) {
        setSize(compound.getFloat("damage"));
        this.ownerUUID = compound.getUUID("owner");
        this.time = compound.getInt("time");
        this.owner = level().getPlayerByUUID(ownerUUID);
        if (compound.getBoolean("nullTarget"))
            this.target = (LivingEntity) level().getEntity(compound.getInt("target"));
    }
    
    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compound) {
        compound.putFloat("damage", size);
        compound.putUUID("owner", ownerUUID);
        compound.putInt("time", time);
        if (target != null)
            compound.putInt("target", target.getId());
        else
            compound.putBoolean("nullTarget", true);
    }
    
    @SubscribeEvent
    public void renderRiders(RenderLivingEvent.Pre<?, ?> event) {
        if (getPassengers().contains(event.getEntity()))
            event.setCanceled(true);
    }
    
    @SubscribeEvent
    public void renderRiders(RenderHandEvent event) {
        if (getPassengers().contains(Minecraft.getInstance().player))
            event.setCanceled(true);
    }
    
    @Override
    public @NotNull Vec3 getPassengerRidingPosition(@NotNull Entity entity) {
        return this.position().subtract(0, entity.getEyeHeight() - 0.6, 0);
    }
    
    @Override
    public boolean isOnFire() {
        return false;
    }
    
}
