package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.client.AnimationsRegistry;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.zeith.hammeranims.api.animsys.AnimationSystem;
import org.zeith.hammeranims.api.animsys.CommonLayerNames;
import org.zeith.hammeranims.api.animsys.layer.AnimationLayer;
import org.zeith.hammeranims.api.tile.IAnimatedEntity;
import org.zeith.hammeranims.core.init.DefaultsHA;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EntitySeliasetSun
		extends LivingEntity
		implements IAnimatedEntity
{
	public static final EntityDataAccessor<Boolean> ACTIVE = SynchedEntityData.defineId(EntitySeliasetSun.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<Boolean> OVERPOWERED = SynchedEntityData.defineId(EntitySeliasetSun.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<Boolean> FASTER_ACTIVATION = SynchedEntityData.defineId(EntitySeliasetSun.class, EntityDataSerializers.BOOLEAN);
	
	public final AnimationSystem animations = AnimationSystem.create(this);
	
	public CompoundTag serializedItem;
	
	private int ticker = 0;
	
	public int growCooldown;
	public int burnUndeadCooldown;
	public int burnMonstersCooldown;
	
	public int activeTicks;
	
	public EntitySeliasetSun(EntityType<? extends LivingEntity> pEntityType, Level pLevel)
	{
		super(pEntityType, pLevel);
		this.serializedItem = ItemsRegistry.SELIASET_SUN.getDefaultInstance().serializeNBT();
		setCustomName(ItemsRegistry.SELIASET_SUN.getDefaultInstance().getHoverName());
	}
	
	public static EntitySeliasetSun create(Level level, Vec3 pos, ItemStack itemStack)
	{
		EntitySeliasetSun ent = new EntitySeliasetSun(EntityRegistry.SELIASET_SUN, level);
		ent.setSunItem(itemStack);
		ent.setPos(pos);
		ent.loadItemData(itemStack.getOrCreateTag().getCompound("itemData"));
		ent.setCustomName(itemStack.getHoverName());
		return ent;
	}

	public float getActivity(float partialTicks)
	{
		float add = (isActive() ? partialTicks : -partialTicks) * getActivationSpeed();
		float max = getMax();
		return Mth.clamp(activeTicks + add, 0, max) / 20F;
	}
	
	@Override
	public EntityDimensions getDimensions(Pose pPose)
	{
		return super.getDimensions(pPose).scale(1.25F + getActivity(1F) * 3F);
	}
	
	public int getMax()
	{
		return isOverpowered() ? 80 : 20;
	}
	
	public int getActivationSpeed()
	{
		return isFasterAct() ? 8 : 1;
	}

	@Override
	public void tick()
	{
		animations.tick();

		if(this.isActive()) this.setDeltaMovement(Vec3.ZERO);
		super.tick();
		if(this.isActive()) this.setDeltaMovement(Vec3.ZERO);
		
		activeTicks += (isActive() ? 1 : -1) * getActivationSpeed();
		activeTicks = Mth.clamp(activeTicks, 0, getMax());
		refreshDimensions();
		
		if(this.level().isClientSide())
			return;
		
		if(serializedItem == null) this.remove(RemovalReason.DISCARDED);
		
		if(this.isActive())
		{
			if(animations.startAnimationAt(CommonLayerNames.ACTION, AnimationsRegistry.SUN_SPIN.configure().transitionTime(1F).speed(1 / 4F)))
				animations.sync();
			
			double radStat = getRadiusStat();
			int speedStat = getSpeedStat();
			
			if(--growCooldown < 0)
			{
				growAround(this, (int) radStat, 2 + getStatLevel(), getSunItem());

				growCooldown = speedStat;
			}
			
			if(--burnUndeadCooldown < 0 && tickCount % 5 == 0)
			{
				boolean gen = false;
				
				for(Monster mon : this.level().getEntitiesOfClass(Monster.class, getBoundingBox().inflate(radStat * 1.5F), mon -> mon.getMobType() == MobType.UNDEAD))
				{
					mon.setSecondsOnFire(10);
					gen = true;
				}
				
				if(gen)
					burnUndeadCooldown = speedStat;
			}
			
			if(--burnMonstersCooldown < 0 && tickCount % 5 == 0)
			{
				boolean gen = false;
				
				for(Monster mon : this.level().getEntitiesOfClass(Monster.class, getBoundingBox().inflate(radStat * 2), mon -> mon.getMobType() != MobType.UNDEAD && mon.getTarget() instanceof Player))
				{
					mon.setSecondsOnFire(10);
					gen = true;
				}
				
				if(gen)
					burnMonstersCooldown = speedStat;
			}
		}
		else
		{
			if(animations.startAnimationAt(CommonLayerNames.ACTION, DefaultsHA.NULL_ANIMATION.configure().transitionTime(1F)))
				animations.sync();
		}
	}
	
	private double getRadiusStat()
	{
		ItemStack item = this.getSunItem();
		IRelicItem relic = (IRelicItem) item.getItem();
		return relic.getAbilityValue(item, "leveling", "radius");
	}
	
	private int getSpeedStat()
	{
		ItemStack item = this.getSunItem();
		IRelicItem relic = (IRelicItem) item.getItem();
		return (int) relic.getAbilityValue(item, "leveling", "speed");
	}
	
	private int getStatLevel()
	{
		ItemStack item = this.getSunItem();
		IRelicItem relic = (IRelicItem) item.getItem();
		return relic.getAbilityPoints(item, "leveling");
	}
	
	@Override
	public InteractionResult interact(Player player, InteractionHand hand)
	{
		if(!this.level().isClientSide)
		{
			var wasActive = this.isActive();
			if(!wasActive) this.ticker = 0;
			this.setActive(!wasActive);
			player.swing(hand);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public HumanoidArm getMainArm()
	{
		return HumanoidArm.RIGHT;
	}
	
	public void setSunItem(ItemStack item)
	{
		if(item.getItem() != ItemsRegistry.SELIASET_SUN) throw new RuntimeException("The fuck?");
		this.serializedItem = item.serializeNBT();
	}
	
	public ItemStack getSunItem()
	{
		if(serializedItem == null) return null;
		return ItemStack.of(serializedItem);
	}
	
	@Override
	public boolean hurt(DamageSource pSource, float pAmount)
	{
		if(pSource.getEntity() instanceof ServerPlayer && getActivity(1F) <= 0.001F)
		{
			ItemStack horn = this.getSunItem();
			CompoundTag itemData = new CompoundTag();
			this.saveItemData(itemData);
			horn.getOrCreateTag().put("itemData", itemData);
			ItemEntity item = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), horn);
			this.level().addFreshEntity(item);
			this.remove(RemovalReason.DISCARDED);
		}
		return false;
	}
	
	@Override
	public boolean save(CompoundTag tag)
	{
		tag.putBoolean("isActive", isActive());
		tag.put("theItem", this.serializedItem);
		this.saveItemData(tag);
		return super.save(tag);
	}
	
	@Override
	public void load(CompoundTag tag)
	{
		this.setActive(tag.getBoolean("isActive"));
		this.serializedItem = tag.getCompound("theItem");
		this.loadItemData(tag);
		super.load(tag);
	}

	public void saveItemData(CompoundTag tag)
	{
		tag.putInt("ticker", ticker);
		tag.putInt("growCooldown", growCooldown);
		tag.putInt("burnUndeadCooldown", burnUndeadCooldown);
		tag.putInt("burnMonstersCooldown", burnMonstersCooldown);
	}
	
	public void loadItemData(CompoundTag tag)
	{
		ticker = tag.getInt("ticker");
		growCooldown = tag.getInt("growCooldown");
		burnUndeadCooldown = tag.getInt("burnUndeadCooldown");
		burnMonstersCooldown = tag.getInt("burnMonstersCooldown");
	}
	
	public boolean isActive()
	{
		return this.entityData.get(ACTIVE);
	}
	
	public void setActive(boolean state)
	{
		this.entityData.set(ACTIVE, state);
	}
	
	public boolean isOverpowered()
	{
		return this.entityData.get(OVERPOWERED);
	}
	
	public void setOverpowered(boolean state)
	{
		this.entityData.set(OVERPOWERED, state);
	}
	
	public boolean isFasterAct()
	{
		return this.entityData.get(FASTER_ACTIVATION);
	}
	
	public void setFasterAct(boolean state)
	{
		this.entityData.set(FASTER_ACTIVATION, state);
	}
	
	@Override
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		entityData.define(ACTIVE, false);
		entityData.define(OVERPOWERED, false);
		entityData.define(FASTER_ACTIVATION, false);
	}
	
	@Override
	public boolean shouldRender(double pX, double pY, double pZ)
	{
		return true;
	}
	
	@Override
	public boolean shouldRenderAtSqrDistance(double pDistance)
	{
		return true;
	}
	
	private List<ItemStack> fake = new ArrayList<>();
	
	@Override
	public Iterable<ItemStack> getArmorSlots()
	{
		return fake;
	}

	@Override
	public ItemStack getItemBySlot(EquipmentSlot pSlot)
	{
		return ItemStack.EMPTY;
	}
	
	@Override
	public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack)
	{
	}

	
	@Override
	public boolean canCollideWith(Entity pEntity)
	{
		return false;
	}
	
	@Override
	public void push(double pX, double pY, double pZ)
	{
	}

	@Override
	protected void pushEntities()
	{
	}
	
	@Override
	public boolean canBeCollidedWith()
	{
		return false;
	}
	
	@Override
	public void setupSystem(AnimationSystem.Builder builder)
	{
		builder.addLayers(new AnimationLayer.Builder(CommonLayerNames.ACTION));
	}
	
	@Override
	public AnimationSystem getAnimationSystem()
	{
		return animations;
	}
	
	public static final Set<Block> BLOCKED_BLOCKS = Set.of(
			Blocks.GRASS_BLOCK,
			Blocks.GRASS,
			Blocks.MOSS_BLOCK,
			Blocks.MOSS_CARPET,
			Blocks.AZALEA,
			Blocks.FLOWERING_AZALEA,
			Blocks.FERN,
			Blocks.SEAGRASS,
			Blocks.BIG_DRIPLEAF_STEM,
			Blocks.BIG_DRIPLEAF,
			Blocks.SMALL_DRIPLEAF,
			Blocks.GLOW_LICHEN
	);
	
	public static void growAround(Entity ent, int rad, int max, ItemStack sun)
	{
		var world = ent.level();
		if(!(world instanceof ServerLevel sl)) return;
		
		List<BlockPos> positions = new ArrayList<>();
		
		for(int x = -rad; x <= rad; ++x)
			for(int z = -rad; z <= rad; ++z)
				for(int y = -rad / 2; y <= rad / 2; ++y)
				{
					var pos = ent.blockPosition().offset(x, y, z);
					var state = world.getBlockState(pos);
					var b = state.getBlock();
					if(!(b instanceof BonemealableBlock gr)) continue;
					if(BLOCKED_BLOCKS.contains(b)) continue;
					if(gr.isValidBonemealTarget(world, pos, state, world.isClientSide) && gr.isBonemealSuccess(world, world.random, pos, state))
						positions.add(pos);
				}
		
		int co = Math.min(ent.level().random.nextInt(max), positions.size());
		for(int i = 0; i < co; ++i)
		{
			BlockPos pos = positions.remove(ent.level().random.nextInt(positions.size()));
			if(BoneMealItem.applyBonemeal(Items.BONE_MEAL.getDefaultInstance(), world, pos, FakePlayerFactory.getMinecraft(sl))) {
				world.levelEvent(2005, pos, 0);
				if(sun.getItem() instanceof IRelicItem relic) relic.dropAllocableExperience(world, ent.getBoundingBox().getCenter(), sun, 1);
			}
		}
	}
}