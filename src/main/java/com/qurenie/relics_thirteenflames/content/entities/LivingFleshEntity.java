package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.client.AnimationsRegistry;
import com.qurenie.relics_thirteenflames.content.items.ItemKnefRose;
import com.qurenie.relics_thirteenflames.init.EntityDataSerializersTF;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.zeith.hammeranims.api.animsys.ConfiguredAnimation;
import org.zeith.hammerlib.HammerLib;
import org.zeith.hammerlib.util.java.Cast;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;


public class LivingFleshEntity
		extends AnimatedEntity
{
	private static final EntityDataAccessor<Float> DATA_SCALE = SynchedEntityData.defineId(LivingFleshEntity.class, EntityDataSerializers.FLOAT);

	private static final EntityDataAccessor<String> OWNER_UUID = SynchedEntityData.defineId(LivingFleshEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<ItemKnefRose.RoseStats> DATA_STATS = SynchedEntityData.defineId(LivingFleshEntity.class, EntityDataSerializersTF.KNEF_ROSE_STATS);

	public String getOwnerUUID() {
		return this.getEntityData().get(OWNER_UUID);
	}

	public void setOwnerUUID(String uuid){
		this.getEntityData().set(OWNER_UUID, uuid);
	}

	public LivingFleshEntity(EntityType<? extends AnimatedEntity> type, Level world)
	{
		super(type, world);
		animationSystem.startAnimationAt(LAYER_WALKING,
				AnimationsRegistry.ZERO_SCALE.configure().transitionTime(0)
		);
		canDie = true;
	}
	
	public boolean isAttacking()
	{
		var target = getTarget();
		return target != null && !target.isDeadOrDying() && target.isAddedToWorld();
	}
	
	public int attackCd;

	public int lifetime = 200;
	
	public float getDamage()
	{
		return 5 * getScale();
	}
	
	@Override
	public boolean hurt(DamageSource pSource, float pAmount)
	{
		pAmount = Math.min(4F, pAmount);
		return super.hurt(pSource, pAmount);
	}
	
	@Override
	protected void actuallyHurt(DamageSource pDamageSource, float pDamageAmount)
	{
		pDamageAmount = Math.min(4F, pDamageAmount);
		super.actuallyHurt(pDamageSource, pDamageAmount);
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		if(attackCd > 0) --attackCd;
		
		if(tickCount % 10 == 0 && !isAttacking())
		{
			// find attack target
			var target = (Mob) level().getEntities(this, getBoundingBox().inflate(12),
							e -> (e instanceof Enemy && e instanceof LivingEntity) || targetPriority(e) < 0
					)
					.stream()
					.min(Comparator.comparingDouble(e ->
					{
						double distance = e.distanceTo(this);

						distance += targetPriority(e);
						
						return distance;
					}))
					.orElse(null);

			setTarget(target);
		}
		
		LivingEntity target;
		if(isAttacking() && tickCount % 5 == 0 && (target = getTarget()) != null)
		{
			navigation.moveTo(target, Math.min(0.4, 0.5 / getScale()));
			if(target.getBoundingBox().intersects(getBoundingBox().inflate(0.5)))
			{
				if(attackCd <= 0)
				{
					attackCd = 20;
					target.hurt(this.level().damageSources().mobAttack(this), getDamage());
				}
			}
		}
		
		tickWalking();
		
		if(tickCount >= lifetime)
		{
			if(tickCount % 20 == 0 && !isDeadOrDying())
			{
				hurt(this.level().damageSources().generic(), this.getMaxHealth() / 5f);
			}
		}
	}

	public double targetPriority(Entity e){
		if(e instanceof LivingEntity lE
				&& lE.getLastDamageSource() != null
				&& lE.getLastDamageSource().getEntity() instanceof Player p
				&& p.getStringUUID().equals(this.getOwnerUUID())) {
			return -200;
		}

		if(e instanceof Mob m
				&& Objects.equals(m.getTarget() == null ? null : m.getTarget().getStringUUID(), this.getOwnerUUID()))
			return -100;
		return 0;
	}
	
	protected LivingFleshEntity createSplit()
	{
		var ent = new LivingFleshEntity(Cast.cast(getType()), this.level());
		ent.moveTo(position());
		ent.setDeltaMovement(new Vec3(
				random.nextGaussian() - random.nextGaussian(),
				0,
				random.nextGaussian() - random.nextGaussian()
		).normalize().scale(0.01));
		ent.setOwnerUUID(this.getOwnerUUID());
		return ent;
	}
	
	protected boolean hasSplit;

	@Override
	public void die(DamageSource pDamageSource)
	{
		super.die(pDamageSource);
		
		if(!hasSplit && !this.level().isClientSide())
		{
			hasSplit = true;
			
			var stats = getStats();
			if(stats.counter >= 1) return;

			if(random.nextFloat() < stats.splitChance / 100F)
			{
				int splits = stats.generateSplits(random);
				for(int i = 0; i < splits; i++)
				{
					var splitStats = stats.split(random);
					float splitScale = getScale() * splitStats.splitScale / 100F;
					
					var ent = createSplit()
							.setScale(splitScale, getMaxHealth() * splitStats.splitScale / 100F)
							.setStats(splitStats);
					
					HammerLib.PROXY.queueTask(this.level(), 25, () -> this.level().addFreshEntity(ent));
				}
			}
		}
		DeathlyFartCloudEntity cloud = new DeathlyFartCloudEntity(EntityRegistry.DEATHCLOUD, this.level());
		cloud.setRadius(getScale() * 2);
		cloud.setLifeTime((int) (200 * getScale()));
		cloud.setPos(this.getBoundingBox().getCenter());
		try {
			cloud.setOwner(this.level().getPlayerByUUID(UUID.fromString(getOwnerUUID())));
		} catch (IllegalArgumentException e) {}
		ParticleHelper.spawnEnginedParticles(this.level(), ParticleTypes.CLOUD, this.getBoundingBox().getCenter(), 60, getScale() / 2, getScale() / 2, getScale() / 2, 0.05, 0.6f, 20, new Color(10, 10, 10), 0.8f);
		this.level().addFreshEntity(cloud);
	}
	
	@Override
	public float getScale()
	{
		return entityData.get(DATA_SCALE);
	}
	
	public ItemKnefRose.RoseStats getStats()
	{
		return entityData.get(DATA_STATS);
	}
	
	public LivingFleshEntity setScale(float scale, float maxHP)
	{
		entityData.set(DATA_SCALE, scale);
		setMaxHealth(maxHP);
		refreshDimensions();
		return this;
	}
	
	private LivingFleshEntity setScale(float scale)
	{
		entityData.set(DATA_SCALE, scale);
		refreshDimensions();
		return this;
	}
	
	public LivingFleshEntity setStats(ItemKnefRose.RoseStats stats)
	{
		entityData.set(DATA_STATS, stats);
		return this;
	}
	
	public LivingFleshEntity initPrimary(LivingEntity dead, ItemKnefRose.RoseStats stats)
	{
		return setStats(stats)
				.setScale(
						(float) Mth.clamp(Math.pow(dead.getMaxHealth(), 1 / 3F) / 3F, 0.1F, 5F),
						dead.getMaxHealth() * stats.hpRate
				);
	}
	
	@Override
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		this.entityData.define(DATA_SCALE, 1F);
		this.entityData.define(DATA_STATS, new ItemKnefRose.RoseStats(ItemStack.EMPTY));
		this.entityData.define(OWNER_UUID, "");
	}
	
	@Override
	public ConfiguredAnimation getWalkingAnimation()
	{
		return AnimationsRegistry.FLESH_MOVE.configure();
	}
	
	public ConfiguredAnimation getRunningAnimation()
	{
		return AnimationsRegistry.FLESH_MOVE.configure();
	}
	
	public void tickWalking()
	{
		if(!this.level().isClientSide() && tickCount > 2)
		{
			var anims = getAnimationSystem();
			
			var idle = getIdleAnimation();
			var walking = getWalkingAnimation();
			var run = getRunningAnimation();
			
			// floats of movement can be almost the same (like 0 and 0.000000001), so entity moves a very short distance, which is invisible for eyes.
			// this can be because of converting coords to bytes to send them to client.
			// so checking if it's more than 1/256 of the block will fix the issue
			boolean posChanged = Math.abs(this.position().x - this.xo) >= 1 / 256F
					|| Math.abs(this.position().z - this.zo) >= 1 / 256F;
			
			if(posChanged)
			{
				anims.startAnimationAt(LAYER_WALKING, walking);
			} else
			{
				anims.startAnimationAt(LAYER_WALKING, idle);
			}
		}
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag pCompound)
	{
		pCompound.putFloat("Scale", getScale());
		pCompound.put("Stats", getStats().serializeNBT());
		pCompound.putString("OwnerUUID", getOwnerUUID());
		super.addAdditionalSaveData(pCompound);
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag pCompound)
	{
		setScale(pCompound.getFloat("Scale"));
		setStats(new ItemKnefRose.RoseStats(pCompound.getCompound("Stats")));
		setOwnerUUID(pCompound.getString("OwnerUUID"));
		super.readAdditionalSaveData(pCompound);
	}
}