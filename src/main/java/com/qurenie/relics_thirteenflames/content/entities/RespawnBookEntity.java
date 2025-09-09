package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.client.AnimationsRegistry;
import com.qurenie.relics_thirteenflames.client.particles.FeatherParticle;
import com.qurenie.relics_thirteenflames.content.items.feather.ItemHettFeather;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ParticlesRegistry;
import com.qurenie.relics_thirteenflames.net.PacketPlaySound;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammeranims.api.animation.LoopMode;
import org.zeith.hammeranims.api.animsys.AnimationSystem;
import org.zeith.hammeranims.api.animsys.actions.AnimationAction;
import org.zeith.hammeranims.api.animsys.actions.AnimationActionInstance;
import org.zeith.hammeranims.api.animsys.layer.AnimationLayer;
import org.zeith.hammeranims.api.tile.IAnimatedEntity;
import org.zeith.hammerlib.net.Network;

import java.awt.*;
import java.util.Objects;
import java.util.UUID;

import static com.qurenie.relics_thirteenflames.content.entities.AnimatedEntity.LAYER_ACTION;
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

@Getter
public class RespawnBookEntity extends Mob implements IAnimatedEntity {
    
    private static final EntityDataAccessor<Integer> RADIUS = SynchedEntityData.defineId(RespawnBookEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> START_DEATH_TICK = SynchedEntityData.defineId(RespawnBookEntity.class, EntityDataSerializers.INT);
    
    private static final Color BURN_COLOR = new Color(230, 90, 20);
    private static final Color FEATHER_COLOR = new Color(239, 215, 182);
    
    private static final int DEATH_ANIM_LENGTH = 60;
    
    AnimationSystem system = AnimationSystem.create(this);
    
    @Setter
    private UUID ownerUUID;
    private double xpConsume;
    private double hpConsume;
    
    public RespawnBookEntity(EntityType<RespawnBookEntity> type, Level world) {
        super(type, world);
        this.system.startAnimationAt(LAYER_ACTION, AnimationsRegistry.RESPAWN_BOOK_OPEN.configure().transitionTime(0));
        this.system.startAnimationAt("ANIMATION_1", AnimationsRegistry.RESPAWN_BOOK_IDLE.configure().transitionTime(0));
    }
    
    public RespawnBookEntity(LivingEntity owner, ItemStack feather, double x, double y, double z) {
        this(EntityRegistry.RESPAWN_BOOK, owner.level());
        setOwnerUUID(owner.getUUID());
        setPos(x, y, z);
        
        ItemHettFeather item = (ItemHettFeather) feather.getItem();
        setRadius((int) item.getStatValue(feather, "savepoint", "radius"));
        this.xpConsume = item.getStatValue(feather, "savepoint", "xp_consume") / 100d;
        this.hpConsume = item.getStatValue(feather, "savepoint", "hp_consume") / 100d;
        
        this.system.startAnimationAt(LAYER_ACTION, AnimationsRegistry.RESPAWN_BOOK_OPEN.configure().transitionTime(0));
        this.system.startAnimationAt("ANIMATION_1", AnimationsRegistry.RESPAWN_BOOK_IDLE.configure().transitionTime(0));
    }
    
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.xpConsume = pCompound.getDouble("xp_consume");
        this.hpConsume = pCompound.getDouble("hp_consume");
        setOwnerUUID(pCompound.getUUID("ownerUUID"));
        setRadius(pCompound.getInt("radius"));
    }
    
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putDouble("xp_consume", xpConsume);
        pCompound.putDouble("hp_consume", hpConsume);
        pCompound.putUUID("ownerUUID", getOwnerUUID());
        pCompound.putInt("radius", getRadius());
    }
    
    @Override
    public void tick() {
        super.tick();
        system.tick();
        
        if (tickCount % 40 == 0) {
            AABB dangerousArea = this.getBoundingBox().inflate(20);
            for (Monster monster : level().getEntitiesOfClass(Monster.class, dangerousArea,
                    monster -> monster.getSensing().hasLineOfSight(this) && monster.getTarget() == null)) {
                monster.setTarget(this);
            }
        }
        
        if (!level().isClientSide && tickCount % 3 == 0)
            for (Entity e : this.getPassengers())
                if (e instanceof LivingEntity living)
                    living.heal(1);
        
        int deathTick = getDeathTick();
        if (level().isClientSide) {
            if (this.getPassengers().isEmpty())
                for (int j = 0; j < 2; j++) {
                    if (random.nextBoolean()) {
                        double r = 0.6;
                        
                        double x = (0.5 - Math.random()) * r * 2;
                        double z = Math.sqrt(r * r - x * x) * (0.5 - Math.random()) * 2;
                        Vec3 spawn = position().add(x, 1.3 + random.nextGaussian() * 0.2, z);
                        
                        ParticleHelper.spawnDirectedParticle(level(), ParticleTypes.ENCHANT,
                                spawn, new Vec3(0, Math.random() * 0.1 + 0.06, 0));
                    }
                }
            
            for (int j = 0; j < 4; j++) {
                double r = (0.8 + random.nextGaussian() * 0.1) * (deathTick < 0 ? 1 : (1 - (double) (tickCount - deathTick) / DEATH_ANIM_LENGTH));
                
                double x = (0.5 - Math.random()) * r * 2;
                double z = Math.sqrt(r * r - x * x) * (random.nextBoolean() ? 1 : -1);
                Vec3 spawn = position().add(x, 0.1, z);
                
                if (!isOnFire() || random.nextBoolean()) {
                    ParticleHelper.spawnDirectedParticle(level(), ParticleHelper.constructSimpleSpark(FEATHER_COLOR, 0.2f, 30, 0.93f).withGravity(-0.25f),
                            spawn, new Vec3(0, 0, 0));
                } else
                    for (int i = 0; i < 2; i++) {
                        ParticleHelper.spawnDirectedParticle(level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(BURN_COLOR, random), 0.2f, 30, 0.93f).withGravity(-0.25f),
                                spawn, new Vec3(0, 0, 0));
                    }
                
            }
        }
        
        if (deathTick > 0) {
            if (level().isClientSide && !getPassengers().isEmpty()) {
                for (int i = 0; i < 5; i++) {
                    double y = random.nextDouble() * 2 + 1;
                    Vec3 center = new Vec3(getX(), getY() + y, getZ());
                    
                    for (int j = 0; j < 3; j++) {
                        double r = 1.4 * ((y - 1) / 2) + 0.3;
                        
                        double x = random.nextGaussian() * r;
                        double z = Math.sqrt(r * r - x * x) * (random.nextBoolean() ? 1 : -1);
                        Vec3 spawn = center.add(x, 0, z);
                        
                        Vec3 radius = spawn.subtract(center);
                        Vec3 move = radius.normalize().yRot((float) (230f * Math.PI / 180f)).add(0, 0.02f, 0);
                        
                        if (!isOnFire() || random.nextBoolean())
                            ParticleHelper.spawnDirectedParticle(level(), ParticleHelper.constructSimpleSpark(FEATHER_COLOR, 0.13f, 20, 0.91f),
                                    spawn, move.normalize().scale(0.03 * y));
                        else
                            ParticleHelper.spawnDirectedParticle(level(), ParticleHelper.constructSimpleSpark(BURN_COLOR, 0.13f, 20, 0.91f),
                                    spawn, move.normalize().scale(0.03 * y));
                        
                        
                        if (random.nextBoolean() && tickCount % 3 == 0)
                            ParticleHelper.spawnDirectedParticle(level(), ParticleHelper.constructHeal(FEATHER_COLOR, 0.3f, 20, 0.91f),
                                    spawn, move.normalize().scale(0.03 * y));
                    }
                }
            }
            
            if (tickCount - deathTick > DEATH_ANIM_LENGTH)
                close();
        }
    }
    
    @Override
    public void onDamageTaken(@NotNull DamageContainer damageContainer) {
        ParticleHelper.spawnParticleEntity(new FeatherParticle.Options(0.2f, 70, ParticlesRegistry.HETT_FEATHER),
                this, (int) (damageContainer.getNewDamage() * 7), 0.1f);
        super.onDamageTaken(damageContainer);
    }
    
    @Override
    public Vec3 getPassengerRidingPosition(@NotNull Entity entity) {
        return this.position().add(0, 1.5, 0);
    }
    
    @Override
    protected void positionRider(@NotNull Entity passenger, @NotNull MoveFunction callback) {
        super.positionRider(passenger, callback);
    }
    
    @Override
    public void die(@NotNull DamageSource damageSource) {
        super.die(damageSource);
        
        if (level() instanceof ServerLevel sl) {
            LivingEntity living = (LivingEntity) sl.getEntity(getOwnerUUID());
            if (living != null) {
                living.hurt(damageSource, (float) (living.getMaxHealth() * hpConsume));
                if (living instanceof Player p) {
                    p.giveExperiencePoints((int) (-p.totalExperience * xpConsume));
                    p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100));
                    Network.sendTo(p, new PacketPlaySound(p.position(), SoundEvents.WITHER_HURT, SoundSource.MASTER, 1, 1));
                }
                
                ParticleHelper.spawnParticleLine(level(), ParticleHelper.constructSimpleSpark(FEATHER_COLOR, 1f, 20, 0.95f),
                        this.position(), living.position().add(0, 1, 0), (int) (this.position().distanceTo(living.position()) * 4), 0.02f);
            }
        }
        
        if (level().isClientSide) {
            ParticleHelper.spawnParticleEntity(ParticleTypes.CAMPFIRE_COSY_SMOKE, this, 40, 0.1);
        }
    }
    
    @SubscribeEvent
    public void livingDeathEvent(LivingDeathEvent event) {
        if (event.getEntity().getUUID().equals(getOwnerUUID())) {
            if (!this.isDeadOrDying() && event.getEntity().distanceToSqr(this) < getRadius() * getRadius() && this.getDeathTick() < 0) {
                event.getEntity().startRiding(this);
                event.setCanceled(true);
                event.getEntity().setHealth(1);
                ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(FEATHER_COLOR, 0.8f, 60, 0.97f), event.getEntity(), 200, 0.7);
                ParticleHelper.spawnParticleEntity(ParticleTypes.CAMPFIRE_COSY_SMOKE, event.getEntity(), 30, 0.1);
                startDeath();
                system.startAnimationAt("ANIMATION_2", AnimationsRegistry.RESPAWN_BOOK_RESPAWN_LAYER);
                
                AABB aabb = this.getBoundingBox().inflate(10, 4, 10).expandTowards(0, 6, 0);
                for (Mob mob : level().getEntitiesOfClass(Mob.class, aabb)) {
                    if (!mob.isPushable())
                        continue;
                    
                    if (Objects.equals(mob.getUUID(), this.getOwnerUUID())) continue;
                    Vec3 b = mob.position().subtract(this.position());
                    Vec3 sp = b.normalize().multiply(2, 2, 2).add(0, 0.5, 0);
                    mob.setDeltaMovement(sp);
                }
            } else
                close();
        }
    }
    
    @Override
    public boolean isPushable() {
        return false;
    }
    
    @Override
    public void knockback(double strength, double x, double z) {
    }
    
    @Override
    public boolean isPushedByFluid(@NotNull FluidType type) {
        return false;
    }
    
    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        EVENT_BUS.register(this);
    }
    
    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        EVENT_BUS.unregister(this);
    }
    
    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (getDeathTick() < 0 && player.getUUID().equals(this.getOwnerUUID())) {
            close();
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(RADIUS, 0);
        builder.define(START_DEATH_TICK, -1);
    }
    
    public int getRadius() {
        return this.getEntityData().get(RADIUS);
    }
    
    public void setRadius(int radius) {
        this.getEntityData().set(RADIUS, radius);
    }
    
    public void close() {
        this.ejectPassengers();
        this.system.stopAnimation("ANIMATION_2");
        this.system.startAnimationAt(LAYER_ACTION, AnimationsRegistry.RESPAWN_BOOK_OPEN.configure().reversed()
                .loopMode(LoopMode.ONCE)
                .speed(0.7f)
                .onFinish(new AnimationAction() {
                    @Override
                    public void execute(AnimationActionInstance animationActionInstance, AnimationLayer animationLayer) {
                        RespawnBookEntity book = (RespawnBookEntity) animationLayer.system.owner;
                        book.discard();
                    }
                })
                .next(AnimationsRegistry.RESPAWN_BOOK_OPEN.configure().important().transitionTime(10000)));
    }
    
    public void startDeath() {
        setDeathTick(tickCount);
    }
    
    public int getDeathTick() {
        return this.getEntityData().get(START_DEATH_TICK);
    }
    
    private void setDeathTick(int tick) {
        this.getEntityData().set(START_DEATH_TICK, tick);
    }
    
    @Override
    public void setupSystem(AnimationSystem.Builder builder) {
        FlamesUtils.setupAnimationSystem(builder);
    }
    
    @Override
    public AnimationSystem getAnimationSystem() {
        return system;
    }
    
}
