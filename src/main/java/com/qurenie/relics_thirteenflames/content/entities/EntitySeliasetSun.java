package com.qurenie.relics_thirteenflames.content.entities;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.qurenie.relics_thirteenflames.client.AnimationsRegistry;
import com.qurenie.api.event.BabySpawnCountEvent;
import com.qurenie.relics_thirteenflames.content.items.ItemSeliasetSun;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammeranims.api.animsys.AnimationSystem;
import org.zeith.hammeranims.api.animsys.CommonLayerNames;
import org.zeith.hammeranims.api.animsys.layer.AnimationLayer;
import org.zeith.hammeranims.api.tile.IAnimatedEntity;
import org.zeith.hammeranims.core.init.DefaultsHA;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.SELIASET_SUN_DATA;
import static com.qurenie.relics_thirteenflames.style.ColorScheme.BURN_COLOR;
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

public class EntitySeliasetSun extends LivingEntity implements IAnimatedEntity {

    public static final EntityDataAccessor<Boolean> ACTIVE = SynchedEntityData.defineId(EntitySeliasetSun.class, EntityDataSerializers.BOOLEAN);
    public static final Set<Block> BLOCKED_BLOCKS = Set.of(
            Blocks.GRASS_BLOCK,
            Blocks.TALL_GRASS,
            Blocks.MOSS_BLOCK,
            Blocks.SHORT_GRASS,
            Blocks.ROSE_BUSH,
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

    public static final Set<Item> BLOCKED_HEAT_BLOCKS = Set.of(
            Items.REDSTONE
    );

    private static final EntityDataAccessor<String> OWNER_UUID = SynchedEntityData.defineId(EntitySeliasetSun.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<ItemStack> SUN_ITEM = SynchedEntityData.defineId(EntitySeliasetSun.class, EntityDataSerializers.ITEM_STACK);
    public final AnimationSystem animations = AnimationSystem.create(this);
    private final RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck;
    public Tag serializedItem;
    public int growCooldown;
    public int burnUndeadCooldown;
    public int burnMonstersCooldown;
    public int activeTicks;
    private int ticker = 0;
    private final List<ItemStack> fake = new ArrayList<>();
    private Int2IntArrayMap itemsHeat = new Int2IntArrayMap();
    private Object2IntMap<BlockPos> blockHeat = new Object2IntArrayMap<>();

    public EntitySeliasetSun(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.serializedItem = ItemsRegistry.SELIASET_SUN.getDefaultInstance().save(registryAccess());
        setCustomName(ItemsRegistry.SELIASET_SUN.getDefaultInstance().getHoverName());
        this.quickCheck = RecipeManager.createCheck(RecipeType.SMELTING);
    }

    public static EntitySeliasetSun create(Level level, Vec3 pos, ItemStack itemStack) {
        EntitySeliasetSun ent = new EntitySeliasetSun(EntityRegistry.SELIASET_SUN, level);
        ent.setSunItem(itemStack);
        ent.setPos(pos);
        ent.loadItemData(itemStack);
        ent.setCustomName(itemStack.getHoverName());
        return ent;
    }

    public static void growAround(Entity ent, int rad, int max, ItemStack sun) {
        var world = ent.level();
        if (!(world instanceof ServerLevel sl)) return;

        List<BlockPos> positions = new ArrayList<>();

        for (int x = -rad; x <= rad; ++x)
            for (int z = -rad; z <= rad; ++z)
                for (int y = -rad / 2; y <= rad / 2; ++y) {
                    var pos = ent.blockPosition().offset(x, y, z);
                    var state = world.getBlockState(pos);
                    var b = state.getBlock();
                    if (!(b instanceof BonemealableBlock gr)) continue;
                    if (BLOCKED_BLOCKS.contains(b)) continue;
                    if (gr.isValidBonemealTarget(world, pos, state) && gr.isBonemealSuccess(world, world.random, pos, state))
                        positions.add(pos);
                }

        int co = Math.min(ent.level().random.nextInt(max), positions.size());
        for (int i = 0; i < co; ++i) {
            BlockPos pos = positions.remove(ent.level().random.nextInt(positions.size()));
            if (BoneMealItem.applyBonemeal(Items.BONE_MEAL.getDefaultInstance(), world, pos, FakePlayerFactory.getMinecraft(sl))) {

                world.levelEvent(2005, pos, 0);
                if (sun.getItem() instanceof ItemSeliasetSun relic && ent instanceof EntitySeliasetSun ess)
                    relic.addExperience(ess.getOwner(), sun, 1);
            }
        }
    }

    @NotNull
    private LivingEntity getOwner() {
        var p = level().getPlayerByUUID(getUUID());
        return p == null ? this : p;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return false;
    }

    public String getOwnerUUID() {
        return this.getEntityData().get(OWNER_UUID);
    }

    public void setOwnerUUID(String uuid) {
        this.getEntityData().set(OWNER_UUID, uuid);
    }

    public float getActivity(float partialTicks) {
        float add = (isActive() ? partialTicks : -partialTicks) * getActivationSpeed();
        float max = getMax();
        return Mth.clamp(activeTicks + add, 0, max) / 20F;
    }

    @Override
    public @NotNull EntityDimensions getDefaultDimensions(@NotNull Pose pPose) {
        return super.getDefaultDimensions(pPose).scale(1.25F + getActivity(1F) * 3F);
    }

    public int getMax() {
        return 15 + 3 * getBlessedStatLevel();
    }

    public int getActivationSpeed() {
        return 1;
    }

    @Override
    public void tick() {
        animations.tick();

        if (getActivity(0) < getActivity(1)) {
            this.setDeltaMovement(0, 0.15, 0);
        } else if (getActivity(0) > getActivity(1)) {
            this.setDeltaMovement(0, -0.15, 0);
        } else if (this.isActive()) this.setDeltaMovement(Vec3.ZERO);

        super.tick();
        if (this.isActive()) this.setDeltaMovement(Vec3.ZERO);


        if (getOwnerUUID().isEmpty()) this.discard();

        activeTicks += (isActive() ? 1 : -1) * getActivationSpeed();
        activeTicks = Mth.clamp(activeTicks, 0, getMax());
        refreshDimensions();

        if (this.level().isClientSide())
            return;

        if (serializedItem == null) this.remove(RemovalReason.DISCARDED);

        if (this.isActive()) {
            if (animations.startAnimationAt(CommonLayerNames.ACTION, AnimationsRegistry.SUN_SPIN.configure().transitionTime(1F).speed(1 / 4F)))
                animations.sync();

            double radStat = getBlessedRadiusStat();
            int speedStat = getSpeedStat();
            int heatStat = getHeatStatLevel();

            if (--growCooldown < 0) {
                growAround(this, (int) radStat, 2 + getBlessedStatLevel(), getSunItem());

                growCooldown = speedStat;
            }

            if (tickCount % 20 == 0 && !level().isClientSide() && heatStat >= 0) {
                AABB heatSpace = new AABB(blockPosition()).inflate(1.8).expandTowards(0, -15, 0);
                final Object2IntMap<BlockPos> blockHeat$ = new Object2IntArrayMap<>();
                final Int2IntArrayMap itemsHeat$ = new Int2IntArrayMap();

                var itr = BlockPos.betweenClosedStream(heatSpace).iterator();

                double limitHeat = getHeatTimeStat();
                int blockLimit = 50;
                while (itr.hasNext()) {
                    if (blockLimit <= 0)
                        break;

                    BlockPos pos = itr.next();
                    BlockState state = level().getBlockState(pos);

                    ItemStack stack = new ItemStack(state.getBlock().asItem());
                    if (stack.isEmpty())
                        continue;

                    if (state.getBlock() instanceof CropBlock crop && crop.getAge(state) < crop.getMaxAge())
                        continue;

                    var holderOpt = Suppliers.memoize(() -> quickCheck.getRecipeFor(new SingleRecipeInput(stack), level()));

                    if (blockHeat.containsKey(pos) || holderOpt.get().isPresent()) {
                        blockLimit--;
                        int heatLevel = blockHeat.getOrDefault(pos, 0) + 1;
                        if (heatLevel >= limitHeat) {
                            var result = holderOpt.get().get().value().assemble(new SingleRecipeInput(stack), registryAccess());
                            if (result.getCount() == 1 && result.getItem() instanceof BlockItem block && !BLOCKED_HEAT_BLOCKS.contains(block)) {
                                ParticleHelper.spawnParticleOutbox(level(), ParticleTypes.FLAME, pos, 5, 0.005);
                                ParticleHelper.spawnParticleOutbox(level(), ParticleHelper.constructSimpleSpark(BURN_COLOR, 0.3f, 40, 0.95f), pos, 5, 0.005);

                                level().setBlock(pos, block.getBlock().defaultBlockState(), 3);
                            } else {
                                burnBlock(pos);
                                ItemEntity item = new ItemEntity(level(), pos.getCenter().x, pos.getCenter().y, pos.getCenter().z, result);
                                level().addFreshEntity(item);
                            }
                            if (getSunItem().getItem() instanceof ItemSeliasetSun relic &&
                                    level() instanceof ServerLevel sl)
                                relic.addExperience(getOwner(), getSunItem(), 1);
                        } else {
                            ParticleHelper.spawnParticleOutbox(level(), ParticleTypes.FLAME, pos, 2, 0.005);
                            ParticleHelper.spawnParticleOutbox(level(), ParticleHelper.constructSimpleSpark(BURN_COLOR, 0.3f, 40, 0.95f), pos, 2, 0.005);
                            blockHeat$.put(pos.immutable(), heatLevel);
                        }
                    }
                }
                this.blockHeat = blockHeat$;

                for (ItemEntity item : this.level().getEntitiesOfClass(ItemEntity.class, heatSpace)) {
                    ItemStack stack = item.getItem();
                    int id = item.getId();

                    if (stack.isEmpty())
                        return;

                    var holderOpt = Suppliers.memoize(() -> quickCheck.getRecipeFor(new SingleRecipeInput(stack), level()));
                    if (itemsHeat.containsKey(id) || holderOpt.get().isPresent()) {
                        int heatLevel = itemsHeat.getOrDefault(id, 0) + 1;
                        if (heatLevel >= limitHeat) {
                            if (holderOpt.get().isEmpty())
                                continue;

                            int toTransform = Math.min(stack.getCount(), heatStat + 1);
                            stack.shrink(heatStat + 1);
                            fryEntity(item, (double) toTransform / 2);
                            if (stack.getCount() <= 0)
                                item.discard();
                            else
                                item.setItem(stack.copy());

                            var result = holderOpt.get().get().value().assemble(new SingleRecipeInput(stack), registryAccess());
                            for (int i = 0; i < toTransform; i++) {
                                var sun = getSunItem();
                                if (sun.getItem() instanceof ItemSeliasetSun relic &&
                                        level() instanceof ServerLevel)
                                    relic.addExperience(getOwner(), getSunItem(), 1);
                                ItemEntity resultEntity = new ItemEntity(level(), item.getX(), item.getY(), item.getZ(), result.copy());
                                level().addFreshEntity(resultEntity);
                            }
                        } else {
                            itemsHeat$.put(id, heatLevel);
                            fryEntity(item, 0.5);
                        }

                    }
                }
                this.itemsHeat = itemsHeat$;

                for (LivingEntity mob : this.level().getEntitiesOfClass(LivingEntity.class, heatSpace)) {
                    mob.hurt(damageSources().lava(), 1);

                    fryEntity(mob, 0.3);
                }
            }

            double heatRad = getHeatRadiusStat();
            if (tickCount % 10 == 0) {
                for (Monster mon : this.level().getEntitiesOfClass(Monster.class, getBoundingBox().inflate(heatRad * 1.5), mon -> mon.getType().is(EntityTypeTags.UNDEAD))) {
                    double damage = getDamageStat();

                    if (tickCount % 20 == 0) {
                        mon.hurt(damageSources().lava(), (float) damage);
                        fryEntity(mon, Math.sqrt(damage));
                    } else
                        fryEntity(mon, 0.1f);

                    mon.setRemainingFireTicks(200);
                }
            }
        } else {
            if (animations.startAnimationAt(CommonLayerNames.ACTION, DefaultsHA.NULL_ANIMATION.configure().transitionTime(getMax() / 20f)))
                animations.sync();
        }
    }

    private void burnBlock(BlockPos pos) {
        ParticleHelper.spawnParticleAABB(level(), ParticleTypes.FLAME, new AABB(pos), 20, 0.03);
        ParticleHelper.spawnParticleAABB(level(), ParticleHelper.constructSimpleSpark(BURN_COLOR, 0.3f, 40, 0.95f), new AABB(pos), 20, 0.03);
        level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }

    private void fryEntity(Entity entity, double modifier) {
        ParticleHelper.spawnParticleEntity(ParticleTypes.FLAME, entity, (int) (10 * modifier), 0.03);
        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(BURN_COLOR, 0.3f, 40, 0.95f), entity, (int) (10 * modifier), 0.03);
    }

    private double getHeatRadiusStat() {
        ItemStack item = this.getSunItem();
        ItemSeliasetSun relic = (ItemSeliasetSun) item.getItem();
        return relic.getStatValue(getOwner(), item, "heat", "radius");
    }

    private int getHeatTimeStat() {
        ItemStack item = this.getSunItem();
        ItemSeliasetSun relic = (ItemSeliasetSun) item.getItem();
        return (int) relic.getStatValue(getOwner(), item, "heat", "heat_time");
    }

    private double getDamageStat() {
        ItemStack item = this.getSunItem();
        ItemSeliasetSun relic = (ItemSeliasetSun) item.getItem();
        return relic.getStatValue(getOwner(), item, "heat", "damage");
    }

    private double getBreedChanceStat() {
        ItemStack item = this.getSunItem();
        ItemSeliasetSun relic = (ItemSeliasetSun) item.getItem();
        return relic.getStatValue(getOwner(), item, "blessed_light", "breed_chance");
    }

    private double getBlessedRadiusStat() {
        ItemStack item = this.getSunItem();
        ItemSeliasetSun relic = (ItemSeliasetSun) item.getItem();
        return relic.getStatValue(getOwner(), item, "blessed_light", "radius");
    }

    private int getSpeedStat() {
        ItemStack item = this.getSunItem();
        ItemSeliasetSun relic = (ItemSeliasetSun) item.getItem();
        return (int) relic.getStatValue(getOwner(), item, "blessed_light", "speed");
    }

    private int getBlessedStatLevel() {
        ItemStack item = this.getSunItem();
        ItemSeliasetSun relic = (ItemSeliasetSun) item.getItem();
        return relic.getAbilityLevel(getOwner(), item, "blessed_light");
    }

    private int getHeatStatLevel() {
        ItemStack item = this.getSunItem();
        ItemSeliasetSun relic = (ItemSeliasetSun) item.getItem();
        return relic.isAbilityUnlocked(getOwner(), item, "heat") ?
                relic.getAbilityLevel(getOwner(), item, "heat")  : -1;
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!this.level().isClientSide && player.getStringUUID().equals(getOwnerUUID())) {
            var wasActive = this.isActive();
            if (!wasActive) this.ticker = 0;
            this.setActive(!wasActive);
            player.swing(hand);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    public ItemStack getSunItem() {
        return this.getEntityData().get(SUN_ITEM);
    }

    public void setSunItem(ItemStack suus) {
        if (suus.getItem() != ItemsRegistry.SELIASET_SUN) throw new RuntimeException("The fuck?");
        this.getEntityData().set(SUN_ITEM, suus);
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource.getDirectEntity() instanceof ServerPlayer p && getActivity(1F) <= 0.001F && p.getStringUUID().equals(getOwnerUUID())) {
            ItemStack horn = this.getSunItem();

            this.saveItemData(horn);

            ItemEntity item = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), horn);
            this.level().addFreshEntity(item);
            this.remove(RemovalReason.DISCARDED);
        }
        return false;
    }

    @Override
    public boolean save(CompoundTag tag) {
        tag.putBoolean("isActive", isActive());
        tag.putInt("seliaset_ticker", ticker);
        tag.putInt("seliaset_growCooldown", growCooldown);
        tag.putInt("seliaset_burnUndeadCooldown", burnUndeadCooldown);
        tag.putInt("seliaset_burnMonstersCooldown", burnMonstersCooldown);

        CompoundTag heat = new CompoundTag();
        heat.putIntArray("entities", itemsHeat.int2IntEntrySet().stream().flatMap(e -> Stream.of(e.getIntKey(), e.getIntValue())).toList());

        ListTag list = new ListTag();
        for (var entry : blockHeat.object2IntEntrySet()) {
            BlockPos pos = entry.getKey();
            list.add(new IntArrayTag(new int[]{pos.getX(), pos.getY(), pos.getZ(), entry.getIntValue()}));
        }
        heat.put("blocks", list);

        tag.put("heat", heat);

        return super.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        this.setActive(tag.getBoolean("isActive"));
        this.ticker = tag.getInt("seliaset_ticker");
        this.growCooldown = tag.getInt("seliaset_growCooldown");
        this.burnUndeadCooldown = tag.getInt("seliaset_burnUndeadCooldown");
        this.burnMonstersCooldown = tag.getInt("seliaset_burnMonstersCooldown");

        CompoundTag heat = tag.getCompound("heat");
        Int2IntArrayMap map = new Int2IntArrayMap();
        var iterator = Arrays.stream(heat.getIntArray("entities")).iterator();
        while (iterator.hasNext())
            map.put((int) iterator.next(), (int) iterator.next());
        this.itemsHeat = map;

        ListTag list = heat.getList("blocks", Tag.TAG_INT_ARRAY);
        Object2IntMap<BlockPos> blockMap = new Object2IntArrayMap<>();
        for (int i = 0; i < list.size(); i++) {
            int[] array = list.getIntArray(i);
            blockMap.put(new BlockPos(array[0], array[1], array[2]), array[3]);
        }
        this.blockHeat = blockMap;

        super.load(tag);
    }

    public void saveItemData(ItemStack stack) {
        stack.set(SELIASET_SUN_DATA, new SeliasetSunData(ticker, growCooldown, burnUndeadCooldown, burnMonstersCooldown));
    }

    public void loadItemData(ItemStack stack) {
        SeliasetSunData data = stack.getOrDefault(SELIASET_SUN_DATA, SeliasetSunData.INSTANCE);
        ticker = data.ticker;
        growCooldown = data.growCooldown;
        burnUndeadCooldown = data.burnUndeadCooldown;
        burnMonstersCooldown = data.burnMonstersCooldown;
    }

    public boolean isActive() {
        return entityData.get(ACTIVE);
    }

    public void setActive(boolean state) {
        entityData.set(ACTIVE, state);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTIVE, false);
        builder.define(OWNER_UUID, "");
        builder.define(SUN_ITEM, ItemStack.EMPTY);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setActive(tag.getBoolean("is_active"));
        setOwnerUUID(tag.getString("owner_uuid"));
        setSunItem(ItemStack.parse(registryAccess(), tag.getCompound("sun_item")).get());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("is_active", isActive());
        tag.putString("owner_uuid", this.getOwnerUUID());
        tag.put("sun_item", getSunItem().save(registryAccess(), new CompoundTag()));
    }

    @Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return true;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return fake;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot pSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {
    }

    @Override
    public boolean canCollideWith(Entity pEntity) {
        return false;
    }

    @Override
    public void push(double pX, double pY, double pZ) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean isAlive() {

        return false;
    }

    @Override
    public void setupSystem(AnimationSystem.Builder builder) {
        builder.addLayers(new AnimationLayer.Builder(CommonLayerNames.ACTION));
    }

    @Override
    public AnimationSystem getAnimationSystem() {
        return animations;
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void countBabyEvent(BabySpawnCountEvent event) {
        if (event.getParentA().level().isClientSide)
            return;

        if (random.nextDouble() > getBreedChanceStat())
            return;

        double radStat = getBlessedRadiusStat();
        if (event.getParentA().distanceToSqr(event.getParentA()) <= radStat * radStat
                || event.getParentA().distanceToSqr(event.getParentB()) <= radStat * radStat)
            event.setCount(event.getCount() + 1);
    }

    public record SeliasetSunData(int ticker, int growCooldown, int burnUndeadCooldown, int burnMonstersCooldown) {

        public static final SeliasetSunData INSTANCE = new SeliasetSunData(0, 0, 0, 0);

        public static final StreamCodec<RegistryFriendlyByteBuf, SeliasetSunData> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, SeliasetSunData>() {
            @Override
            public SeliasetSunData decode(@NotNull RegistryFriendlyByteBuf buf) {
                return new SeliasetSunData(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull SeliasetSunData data) {
                buf.writeInt(data.ticker);
                buf.writeInt(data.growCooldown);
                buf.writeInt(data.burnUndeadCooldown);
                buf.writeInt(data.burnMonstersCooldown);
            }
        };

        public static final Codec<SeliasetSunData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.fieldOf("tint").forGetter(SeliasetSunData::ticker),
                Codec.INT.fieldOf("growCooldown").forGetter(SeliasetSunData::growCooldown),
                Codec.INT.fieldOf("burnUndeadCooldown").forGetter(SeliasetSunData::burnUndeadCooldown),
                Codec.INT.fieldOf("burnMonstersCooldown").forGetter(SeliasetSunData::burnMonstersCooldown)
        ).apply(inst, SeliasetSunData::new));

    }

}