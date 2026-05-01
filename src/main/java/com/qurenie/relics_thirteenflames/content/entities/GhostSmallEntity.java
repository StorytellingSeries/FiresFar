package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.client.AnimationsRegistry;
import com.qurenie.relics_thirteenflames.content.items.ItemKnefRose;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.style.ColorScheme;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammeranims.api.animsys.ConfiguredAnimation;
import org.zeith.hammeranims.core.init.DefaultsHA;
import org.zeith.hammerlib.HammerLib;
import org.zeith.hammerlib.util.java.Cast;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

import static com.qurenie.relics_thirteenflames.init.EntityDataSerializers.ROSE_STATS;


public class GhostSmallEntity
		extends AnimatedEntity
{
	private static final EntityDataAccessor<Float> DATA_SCALE = SynchedEntityData.defineId(GhostSmallEntity.class, EntityDataSerializers.FLOAT);

	private static final EntityDataAccessor<String> OWNER_UUID = SynchedEntityData.defineId(GhostSmallEntity.class, EntityDataSerializers.STRING);

	private static final EntityDataAccessor<ItemKnefRose.RoseStats> DATA_STATS = SynchedEntityData.defineId(GhostSmallEntity.class, ROSE_STATS.get());

	public String getOwnerUUID() {
		return this.getEntityData().get(OWNER_UUID);
	}

	public void setOwnerUUID(String uuid){
		this.getEntityData().set(OWNER_UUID, uuid);
	}

	@Override
	public void travel(@NotNull Vec3 travelVector)
	{
		if (this.isNoGravity())
		{
			this.moveRelative(0.02F, travelVector);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.91));
		} else
		{
			super.travel(travelVector);
		}
	}

	public GhostSmallEntity(EntityType<? extends AnimatedEntity> type, Level world)
	{
		super(type, world);
		animationSystem.startAnimationAt(LAYER_WALKING,
				AnimationsRegistry.ZERO_SCALE.configure().transitionTime(0)
		);
		this.moveControl = new net.minecraft.world.entity.ai.control.FlyingMoveControl(this, 20, true);
		canDie = true;
		this.lifetime = 170 + level().random.nextInt(50);
	}
	
	public boolean isAttacking()
	{
		var target = getTarget();
		return target != null && !target.isDeadOrDying() && target.isAddedToLevel();
	}
	
	public int attackCd;

	public int lifetime;
	
	public float getDamage()
	{
		return 5 * getScale();
	}
	
	@Override
	public boolean hurt(@NotNull DamageSource pSource, float pAmount)
	{
		pAmount = Math.min(4F, pAmount);
		return super.hurt(pSource, pAmount);
	}
	
	@Override
	protected void actuallyHurt(@NotNull DamageSource pDamageSource, float pDamageAmount)
	{
		pDamageAmount = Math.min(4F, pDamageAmount);
		super.actuallyHurt(pDamageSource, pDamageAmount);
	}
	
	@Override
	protected @NotNull ResourceKey<LootTable> getDefaultLootTable() {
		return super.getDefaultLootTable();
	}

	private Vec3 idleTarget;

	int counter = 0;
	
	@Override
	public void tick()
	{
		super.tick();
		
		if(attackCd > 0) --attackCd;
		
		if(tickCount % 10 == 0)
		{
			// find attack target
			var target = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(12),
							e -> e != this && (e instanceof Enemy || targetPriority(e) < 0)
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
		
		LivingEntity target = getTarget();
		if(isAttacking() && tickCount % 5 == 0 && (target = getTarget()) != null)
		{
			Vec3 targetPos = target.position().add(
					Math.sin(tickCount * 0.1) * 2.5,
					0.5 + Math.cos(tickCount * 0.15),
					Math.cos(tickCount * 0.1) * 2.5
			);

			navigation.moveTo(targetPos.x, targetPos.y, targetPos.z, 0.4);
			if(target.getBoundingBox().intersects(getBoundingBox().inflate(0.8)))
			{
				if(attackCd <= 0)
				{
					attackCd = 20;
					animationSystem.startAnimationAt(LAYER_ACTION, getAttackAnimation());
					target.hurt(this.level().damageSources().mobAttack(this), getDamage());

					Vec3 away = this.position().subtract(target.position()).normalize();

					Vec3 side = new Vec3(
							random.nextGaussian() * 0.3,
							0,
							random.nextGaussian() * 0.3
					);

					Vec3 motion = away.add(side).normalize().scale(0.3 + 0.2 * getScale());
					motion = motion.add(0, 0.1, 0);

					this.setDeltaMovement(this.getDeltaMovement().add(motion));
					this.hasImpulse = true;

					if(getStats().rose.getItem() instanceof ItemKnefRose relic) {
						relic.addExperience(this.level().getPlayerByUUID(UUID.fromString(getOwnerUUID())), getStats().rose, 1);
					}
				}
			}

			counter = 0;
		}

		if (target == null)
			counter++;

		if (counter > 15) {

			// раз в время выбираем новую цель
			if (idleTarget == null || this.tickCount % 40 == 0 || this.position().distanceToSqr(idleTarget) < 1.5) {
				idleTarget = getRandomFlyPos();
			}

			steerTo(idleTarget, 0.05, 0.92);
		}
		
		tickWalking();
		
		if(tickCount >= lifetime)
		{
			this.kill();
		}
	}

	private void steerTo(Vec3 target, double turnRate, double drag) {
		Vec3 to = target.subtract(this.position());

		if (to.lengthSqr() < 0.0001) return;

		Vec3 desiredDir = to.normalize();
		Vec3 motion = this.getDeltaMovement();

		double speed = motion.length();

		// если почти стоим — задаём старт
		if (speed < 0.05) {
			motion = desiredDir.scale(0.08);
			this.setDeltaMovement(motion);
			return;
		}

		Vec3 currentDir = motion.normalize();

		// 🔥 ВАЖНО: плавный поворот, а не добавление
		Vec3 newDir = currentDir.lerp(desiredDir, turnRate).normalize();

		// сохраняем скорость (с лёгкой стабилизацией)
		double newSpeed = Mth.clamp(speed * drag, 0.08, 0.25);

		Vec3 newMotion = newDir.scale(newSpeed);

		this.setDeltaMovement(newMotion);
		this.hasImpulse = true;
	}

	private Vec3 getRandomFlyPos() {
		Vec3 motion = this.getDeltaMovement();

		// если почти стоим — берём lookDirection
		Vec3 forward = motion.lengthSqr() > 0.001
				? motion.normalize()
				: this.getLookAngle();

		// боковой вектор (перпендикуляр)
		Vec3 side = new Vec3(-forward.z, 0, forward.x).normalize();

		// вверх
		Vec3 up = new Vec3(0, 1, 0);

		double forwardDist = 4 + random.nextDouble() * 4; // 4–8 блоков вперёд
		double sideOffset = (random.nextDouble() - 0.5) * 4; // -2..2
		double verticalOffset = (random.nextDouble() - 0.5) * 2; // -1..1

		Vec3 target = this.position()
				.add(forward.scale(forwardDist))
				.add(side.scale(sideOffset))
				.add(up.scale(verticalOffset));

		return target;
	}

	public double targetPriority(Entity e){
		if(e instanceof LivingEntity lE
				&& lE.getLastDamageSource() != null
				&& lE.getLastDamageSource().getEntity() instanceof Player p
				&& p.getStringUUID().equals(this.getOwnerUUID())) {
			return -200;
		}
		if(getTarget() != null && getTarget().getUUID().equals(e.getUUID())) {
			if (getTarget() instanceof Creeper creeper && creeper.getSwelling(0) > 0)
				return 0;
			return -150;
		}
		if(e instanceof Mob m
				&& Objects.equals(m.getTarget() == null ? null : m.getTarget().getStringUUID(), this.getOwnerUUID()))
			return -100;
		return 0;
	}
	
	protected GhostSmallEntity createSplit()
	{
		var ent = new GhostSmallEntity(Cast.cast(getType()), this.level());
		ent.moveTo(position());
		ent.setDeltaMovement(new Vec3(
				random.nextGaussian() - random.nextGaussian(),
				0,
				random.nextGaussian() - random.nextGaussian()
		).normalize().scale(0.01));
		ent.setOwnerUUID(this.getOwnerUUID());
		if(getStats().rose.getItem() instanceof ItemKnefRose relic) {
			relic.addExperience(this.level().getPlayerByUUID(UUID.fromString(getOwnerUUID())), getStats().rose, 1);
		}
		return ent;
	}
	
	protected boolean hasSplit;

	@Override
	public void die(@NotNull DamageSource pDamageSource)
	{
		super.die(pDamageSource);
		this.setPose(Pose.CROAKING);
		
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
					var splitStats = stats.split();
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
		} catch (IllegalArgumentException ignored) {}
		ParticleHelper.spawnParticleEntity(
				ParticleHelper.constructSmoke(ColorScheme.GRAY_COLOR, 0.5f * getScale(), (int) (random.nextInt(30) + 50 * getScale())).withLightning(false).withGravity(0.2f),
				this,
				30,
				0.04 * getScale()
		);
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
	
	public GhostSmallEntity setScale(float scale, float maxHP)
	{
		entityData.set(DATA_SCALE, scale);
		setMaxHealth(maxHP);
		refreshDimensions();
		return this;
	}
	
	private GhostSmallEntity setScale(float scale)
	{
		entityData.set(DATA_SCALE, scale);
		refreshDimensions();
		return this;
	}
	
	public GhostSmallEntity setStats(ItemKnefRose.RoseStats stats)
	{
		entityData.set(DATA_STATS, stats);
		return this;
	}
	
	public GhostSmallEntity initPrimary(LivingEntity dead, ItemKnefRose.RoseStats stats)
	{
		return setStats(stats)
				.setScale(
						(float) Mth.clamp(Math.pow(dead.getMaxHealth(), 1 / 3F) / 3F, 0.1F, 5F),
						dead.getMaxHealth() * stats.hpRate
				);
	}
	
	@Override
	protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
		super.defineSynchedData(builder);
		
		builder.define(DATA_SCALE, 1F);
		builder.define(DATA_STATS, new ItemKnefRose.RoseStats(null, ItemStack.EMPTY));
		builder.define(OWNER_UUID, "");
	}
	
	@Override
	public ConfiguredAnimation getWalkingAnimation()
	{
		return AnimationsRegistry.GHOST_SMALL_WALK.configure();
	}

	@Override
	protected @NotNull FlyingPathNavigation createNavigation(@NotNull Level level)
	{
		return new FlyingPathNavigation(this, level);
	}

	@Override
	public boolean isNoGravity()
	{
		return true;
	}

	@Override
	public ConfiguredAnimation getIdleAnimation() {
		return AnimationsRegistry.GHOST_SMALL_IDLE.configure();
	}

	public ConfiguredAnimation getAttackAnimation() {
		return AnimationsRegistry.GHOST_SMALL_ATTACK.configure()
				.important().next(DefaultsHA.NULL_ANIM.configure());
	}
	
	public void tickWalking()
	{
		if(!this.level().isClientSide() && tickCount > 2)
		{
			var anims = getAnimationSystem();
			
			var idle = getIdleAnimation();
			var walking = getWalkingAnimation();
			
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
		pCompound.put("Stats", getStats().serializeNBT(registryAccess()));
		pCompound.putString("OwnerUUID", getOwnerUUID());
		pCompound.putInt("Lifetime", lifetime);
		super.addAdditionalSaveData(pCompound);
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag pCompound)
	{
		setScale(pCompound.getFloat("Scale"));
		setStats(new ItemKnefRose.RoseStats(this.registryAccess(), pCompound.getCompound("Stats")));
		setOwnerUUID(pCompound.getString("OwnerUUID"));
		this.lifetime = pCompound.getInt("Lifetime");
		super.readAdditionalSaveData(pCompound);
	}
}