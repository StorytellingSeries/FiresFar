package com.qurenie.relics_thirteenflames.content.items;

import com.qurenie.api.IActivityContainer;
import com.qurenie.api.IExtRelicItem;
import com.qurenie.api.SettingsContainer;
import com.qurenie.relics_thirteenflames.activity.IActivitySetting;
import com.qurenie.relics_thirteenflames.activity.RelicActivitySetting;
import com.qurenie.relics_thirteenflames.content.blocks.BlockShaking;
import com.qurenie.relics_thirteenflames.content.entities.MontuDrillEntity;
import com.qurenie.relics_thirteenflames.content.entities.SkintClusterEntity;
import com.qurenie.relics_thirteenflames.content.entities.UsableFallingBlockEntity;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import com.qurenie.relics_thirteenflames.net.EntityPacket;
import com.qurenie.relics_thirteenflames.net.HammerAOEChangePacket;
import com.qurenie.relics_thirteenflames.net.PacketPlaySound;
import com.qurenie.relics_thirteenflames.style.ColorScheme;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.items.misc.CreativeContentConstructor;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.api.items.IColoredFoilItem;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.net.PacketContext;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.hammerlib.util.java.tuples.Tuple2;
import org.zeith.hammerlib.util.java.tuples.Tuples;

import java.awt.*;
import java.util.List;
import java.util.*;

import static com.qurenie.relics_thirteenflames.ThirteenFlames.SCHEDULER;
import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.*;
import static com.qurenie.relics_thirteenflames.style.ColorScheme.BURN_COLOR;

public class ItemMontuHammer
        extends RelicItem implements IColoredFoilItem, IExtRelicItem, IActivityContainer {
    
    protected final Tool tool;
    protected final TagKey<Block> blocks = BlockTags.MINEABLE_WITH_PICKAXE;
    protected final float speed;
    private final ItemAttributeModifiers defaultModifiers;
    private static final Random RNG = new Random();
    
    public ItemMontuHammer(Properties properties, Tier tier) {
        super(properties.component(DataComponents.TOOL, new Tool(List.of(
                Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_PICKAXE, tier.getSpeed()),
                Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_SHOVEL, tier.getSpeed())),
                1.0F, 1)));
        this.tool = components().get(DataComponents.TOOL);
        this.speed = tier.getSpeed();
        float attackDamageBaseline = 5 + tier.getAttackDamageBonus();
        defaultModifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, attackDamageBaseline, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.1F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public void gatherCreativeTabContent(CreativeContentConstructor constructor) {
    }

    public static Tuple2<BlockPos, BlockPos> getMiningArea(BlockPos pos, int sideHit, int breakRadius, int breakDepth) {
        int xMax = breakRadius;
        int xMin = breakRadius;
        int yMax = breakRadius;
        int yMin = breakRadius;
        int zMax = breakRadius;
        int zMin = breakRadius;
        
        int yOffset = 0;
        
        switch (sideHit) {
            case 0 -> {
                yMax = breakDepth;
                yMin = 0;
            }
            case 1 -> {
                yMin = breakDepth;
                yMax = 0;
            }
            case 2 -> {
                zMin = 0;
                zMax = breakDepth;
                yOffset = breakRadius - 1;
            }
            case 3 -> {
                zMax = 0;
                zMin = breakDepth;
                yOffset = breakRadius - 1;
            }
            case 4 -> {
                xMax = breakDepth;
                xMin = 0;
                yOffset = breakRadius - 1;
            }
            case 5 -> {
                xMin = breakDepth;
                xMax = 0;
                yOffset = breakRadius - 1;
            }
        }
        
        if (breakRadius == 0) {
            yOffset = 0;
        }
        
        return Tuples.immutable(pos.offset(-xMin, yOffset - yMin, -zMin), pos.offset(xMax, yOffset + yMax, zMax));
    }
    
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("slap")
                                .initialMaxLevel(4)
                                .statistic(AbilityStatisticTemplate.builder().build())
                                .stat(AbilityStatTemplate.builder("recharge")
                                        .initialValue(1200, 800)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), -100.0)
                                        .formatValue(x -> (int) MathUtils.round(x / 20f, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("radius")
                                        .initialValue(3.0, 4.0)
                                        .thresholdValue(3.0, 10)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 1.5)
                                        .formatValue(x -> (int) MathUtils.round(x, 0))
                                        .build()
                                )
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_1")
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 3, 9).star(1, 16, 13).star(2, 10, 3).star(3, 15, 4).star(4, 18, 7).star(5, 9, 16).star(6, 4, 18).star(7, 13, 20).star(8, 17, 20).star(9, 8, 22).star(10, 6, 26).star(11, 3, 25).star(12, 12, 27)
                                        .link(1, 3).link(1, 4).link(0, 2).link(0, 5).link(1, 5).link(5, 6).link(5, 7).link(7, 8).link(6, 9).link(9, 10).link(10, 11).link(10, 12)
                                        .build())
                                .build()
                        )
                        .ability(AbilityTemplate.builder("aoe")
                                .initialMaxLevel(2)
                                .requiredPoints(3)
                                .statistic(AbilityStatisticTemplate.builder().build())
                                .stat(AbilityStatTemplate.builder("radius")
                                        .initialValue(0.0, 0.0)
                                        .thresholdValue(0.0, 2.0)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 1.0)
                                        .formatValue(x -> (int) MathUtils.round(x + 1, 0))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("fire")
                                        .initialValue(10, 20)
                                        .thresholdValue(0.0, 100)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 7)
                                        .formatValue(Math::floor)
                                        .build()
                                )
                                .rankModifier(1, "fire")
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_2")
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("drill")
                                .initialMaxLevel(4)
                                .requiredLevel(7)
                                .statistic(AbilityStatisticTemplate.builder().build())
                                .stat(AbilityStatTemplate.builder("recharge")
                                        .initialValue(1200, 1100)
                                        .thresholdValue(100, 1200)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), -100)
                                        .formatValue(x -> MathUtils.round(x / 20, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("strength")
                                        .initialValue(1, 2)
                                        .thresholdValue(1, 20)
                                        .upgradeModifier(RelicsScalingModels.EXPONENTIAL.get(), 0.5)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("capacity")
                                        .initialValue(50, 75)
                                        .thresholdValue(300, 10000)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.3)
                                        .formatValue(Math::floor)
                                        .build()
                                )
                                .build())
                        .build()
                )
                .statistic(RelicStatisticTemplate.builder().build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(100)
                        .maxRank(2)
                        .build())
                .loot(LootTemplate.builder().entry(LootEntries.MINESHAFT).build())
                .build();
    }

    private double getMeltingChance(@Nullable LivingEntity entity, ItemStack stack) {
        return ItemsRegistry.MONTU_HAMMER.getStatValue(entity, stack, "aoe", "fire");
    }

    private boolean hasMeltingRang(@Nullable LivingEntity entity, ItemStack stack) {
        return ItemsRegistry.MONTU_HAMMER.hasRangModifier(entity, stack, "aoe", "fire");
    }

    private static ItemStack getSmeltResult(Level level, ItemStack input) {
        if (input.isEmpty())
            return ItemStack.EMPTY;

        var recipeManager = level.getRecipeManager();

        var recipe = recipeManager.getRecipeFor(
                net.minecraft.world.item.crafting.RecipeType.SMELTING,
                new net.minecraft.world.item.crafting.SingleRecipeInput(input),
                level
        );

        if (recipe.isPresent()) {
            ItemStack result = recipe.get()
                    .value()
                    .assemble(new net.minecraft.world.item.crafting.SingleRecipeInput(input),
                            level.registryAccess());

            if (!result.isEmpty()) {
                result.setCount(input.getCount());
                return result;
            }
        }

        return ItemStack.EMPTY;
    }
    
    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().getBlockState(ctx.getClickedPos()).hasBlockEntity() || ctx.getPlayer() == null || !ctx.getPlayer().isShiftKeyDown())
            return super.useOn(ctx);

        if (!canCast(ctx.getPlayer(), ctx.getItemInHand(), "slap"))
            return InteractionResult.FAIL;

        var radius = (int) Math.round(getStatValue(ctx.getPlayer(), ctx.getItemInHand(), "slap", "radius"));
        
        var level = ctx.getLevel();
        
        if (level.isClientSide())
            return InteractionResult.SUCCESS;
        
        var pos = ctx.getClickedPos();
        var itr = BlockPos.betweenClosedStream(pos.offset(-radius, -radius * 3, -radius), pos.offset(radius, 0, radius))
                .filter(bs -> new Vec3(bs.getX(), pos.getY(), bs.getZ()).distanceTo(new Vec3(pos.getX(), pos.getY(), pos.getZ())) <= radius)
                .map(BlockPos::new)
                .iterator();
        
        int lowestY = level.getMinBuildHeight();
        
        Map<TagKey<Block>, BlockState> replaces = new HashMap<>();
        replaces.put(Tags.Blocks.ORES_IN_GROUND_STONE, Blocks.STONE.defaultBlockState());
        replaces.put(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE, Blocks.DEEPSLATE.defaultBlockState());
        replaces.put(Tags.Blocks.ORES_IN_GROUND_NETHERRACK, Blocks.NETHERRACK.defaultBlockState());
        
        int counter = 70;
        
        Set<BlockPos> pulled = new HashSet<>();
        while (itr.hasNext()) {
            var target = itr.next();
            
            var stateAbove = level.getBlockState(target.above());
            if (target.getY() == pos.getY() && stateAbove.canBeReplaced(Fluids.WATER) || stateAbove.liquid()) // shaking the ground
            {
                int delay = Mth.floor(Mth.sqrt((float) pos.distSqr(target)) * 5);
                
                BlockPos sp = new BlockPos(target);
                while (level.isEmptyBlock(sp) && level.isInWorldBounds(sp) && sp.getY() >= lowestY)
                    sp = sp.below();
                
                BlockPos target0 = sp;
                
                if (!level.isEmptyBlock(target0))
                    SCHEDULER.schedule(delay, () -> {
                        BlockShaking.shake(level, target0, BlockShaking.BEHAVIOR_JUMP.normal());
                        level.getEntitiesOfClass(SkintClusterEntity.class, new AABB(target0.above())).forEach(SkintClusterEntity::destroyByHammer);
                    });
            }
            
            var state = level.getBlockState(target);
            
            if (counter <= 0)
                continue;
            
            final var replaceWith = replaces.entrySet()
                    .stream()
                    .filter(e -> state.is(e.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
            if (replaceWith == null) continue;
            
            counter--;
            
            BlockPos height = target;
            BlockPos cntHeight = height;
            double cntSpd = 0;
            
            int attempts = 8;
            
            while (--attempts > 0) {
                while (level.isInWorldBounds(height) && (!level.isEmptyBlock(height) || height.getY() < pos.getY())) {
                    height = height.above();
                    cntHeight = height;
                }
                
                while (level.isEmptyBlock(height.below())) {
                    height = height.below();
                    cntHeight = height;
                }
                
                while (pulled.contains(cntHeight)) {
                    cntHeight = cntHeight.above();
                    cntSpd += 0.15;
                }
                
                if (level.isEmptyBlock(height)/* && !pulled.contains(height)*/)
                    break;
            }
            if (attempts == 0) continue;
            
            if (level.isEmptyBlock(height)) {
                pulled.add(height);
                final var height0 = height;
                int delay = Mth.floor(
                        Mth.sqrt((float) pos.distSqr(new BlockPos(target.getX(), pos.getY(), target.getZ()))) * 5);
                double finalCntSpd = cntSpd;
                
                
                SCHEDULER.schedule(Math.max(0, delay - 2), () ->
                {
                    addExperience(ctx.getPlayer(), ctx.getItemInHand(), 1);
                    UsableFallingBlockEntity fbe = UsableFallingBlockEntity.createFalling(level, height0, state, ctx.getItemInHand());
                    Vec3 dist = new Vec3(height0.getX(), pos.getY(), height0.getZ()).subtract(new Vec3(pos.getX(), pos.getY(), pos.getZ())).yRot(RNG.nextFloat(-10, 10) * Mth.DEG_TO_RAD);
                    Vec3 move = dist.normalize().scale(Math.min(0.6, 0.2 / dist.length())).add(new Vec3(RNG.nextFloat(0.05f), 0, 0).yRot(RNG.nextFloat(3.14f)));
                    fbe.setDeltaMovement(move.scale(RNG.nextFloat(0.8f, 2.2f) + finalCntSpd * 10).add(0, RNG.nextFloat(0.3f, 0.5f) + finalCntSpd, 0));
                    fbe.setLifeTime(600);
                    level.addFreshEntity(fbe);
                    level.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
                });
                
            }
        }
        Network.sendToAll(new PacketPlaySound(ctx.getClickLocation(), SoundsRegistry.MONTU_SLAP.get(), SoundSource.MASTER, 1, 1));
        setMaxCooldown(ctx.getPlayer(), ctx.getItemInHand(), "slap");
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public @NotNull ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
        return stack;
    }

    private void burnBlock(Level level, BlockPos pos) {
        ParticleHelper.spawnParticleAABB(level, ParticleTypes.FLAME, new AABB(pos), 3, 0.01);
        ParticleHelper.spawnParticleAABB(level, ParticleHelper.constructSimpleSpark(BURN_COLOR, 0.3f, 40, 0.95f), new AABB(pos), 8, 0.01);
    }

    @Override
    public boolean mineBlock(ItemStack stack, @NotNull Level world, @NotNull BlockState state,
                             @NotNull BlockPos pos, @NotNull LivingEntity miner) {
        int radius = stack.getComponents().getOrDefault(MONTU_AOE.get(), 0);

        if (radius > getStatValue(miner, stack, "aoe", "radius")) {
            radius = 0;
            stack.set(MONTU_AOE.get(), radius);
        }

        Player player = Cast.cast(miner, Player.class);

        if (player == null
                || player.isShiftKeyDown()
                || !stack.canPerformAction(ItemAbilities.PICKAXE_DIG)
        ) return false;

        Vec3 view = player.getViewVector(0);
        Vec3 look = player.getEyePosition(0);

        var aoe = getMiningArea(pos,
                world.clip(new ClipContext(look, look.add(view),
                                ClipContext.Block.COLLIDER,
                                ClipContext.Fluid.NONE,
                                player))
                        .getDirection().get3DDataValue(),
                radius, 0
        );

        var itr = BlockPos.betweenClosedStream(aoe.a(), aoe.b())
                .map(BlockPos::new)
                .iterator();

        int blocksMined = stack.getOrDefault(BLOCKS_MINED, 0);

        int fireAspect = stack.getEnchantmentLevel(
                world.registryAccess().holderOrThrow(Enchantments.FIRE_ASPECT)
        );

        boolean canSmelt = hasMeltingRang(player, stack) && fireAspect > 0;

        while (itr.hasNext()) {
            BlockPos target = itr.next();

            if (world.isEmptyBlock(target))
                continue;

            BlockState targetState = world.getBlockState(target);

            if (!targetState.canHarvestBlock(world, target, player)
                    || targetState.getDestroySpeed(world, target) < 0)
                continue;

            blocksMined++;

            // ==== MELTING ====
            boolean doSmelt = canSmelt
                    && RNG.nextDouble() < (getMeltingChance(player, stack) * fireAspect / 100.0);

            if (!world.isClientSide && doSmelt) {

                List<ItemStack> drops = Block.getDrops(
                        targetState,
                        (net.minecraft.server.level.ServerLevel) world,
                        target,
                        world.getBlockEntity(target),
                        player,
                        stack
                );

                // Fortune automatically applies because stack is passed into Block.getDrops(...)
                for (ItemStack drop : drops) {

                    ItemStack result = getSmeltResult(world, drop);

                    if (!result.isEmpty()) {
                        Block.popResource(world, target, result.copy());
                    } else {
                        Block.popResource(world, target, drop.copy());
                    }
                }

                targetState.spawnAfterBreak(
                        (net.minecraft.server.level.ServerLevel) world,
                        target,
                        stack,
                        true
                );

                world.removeBlock(target, false);
                burnBlock(world, target);

            } else {
                // обычное ломание
                targetState.getBlock().playerDestroy(
                        world,
                        player,
                        target,
                        targetState,
                        world.getBlockEntity(target),
                        stack
                );

                world.destroyBlock(target, false);
            }
        }

        blocksMined++;

        if (blocksMined >= 25) {
            addExperience(player, stack, blocksMined / 25);
            blocksMined %= 25;
        }

        stack.set(BLOCKS_MINED, blocksMined);

        return true;
    }
    @Override
    public float getDestroySpeed(@NotNull ItemStack pStack, BlockState pState) {
        return pState.is(BlockTags.MINEABLE_WITH_PICKAXE) || pState.is(BlockTags.MINEABLE_WITH_SHOVEL)
                ? this.speed
                : 1.0F;
    }
    
    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
        return defaultModifiers;
    }
    
    @Override
    public boolean isCorrectToolForDrops(@NotNull ItemStack stack, @NotNull BlockState state) {
        return tool.isCorrectForDrops(state);
    }
    
    @Override
    public boolean canPerformAction(@NotNull ItemStack stack, @NotNull ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(itemAbility);
    }
    
    @Override
    public int getEnchantmentValue(@NotNull ItemStack stack) {
        return 20;
    }
    
    @Override
    public boolean isPrimaryItemFor(@NotNull ItemStack stack, Holder<Enchantment> enchantment) {
        Enchantment.EnchantmentDefinition definition = enchantment.value().definition();
        boolean isPrimary = definition.primaryItems().isPresent() && FlamesUtils.isWeaponOrMiningEnchantment(definition.primaryItems().get());
        boolean supports = FlamesUtils.isWeaponOrMiningEnchantment(definition.supportedItems());
        return isPrimary || supports && !enchantment.is(Enchantments.UNBREAKING) && !enchantment.is(Enchantments.SWEEPING_EDGE);
    }
    
    @Override
    public boolean supportsEnchantment(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        return this.isPrimaryItemFor(stack, enchantment);
    }
    
    @Override
    public boolean isEnchantable(@NotNull ItemStack pStack) {
        return true;
    }
    
    @Override
    public int getFoilColor(@NotNull ItemStack stack) {
        return new Color(116, 229, 0).getRGB();
    }

    @Override
    public SettingsContainer<IActivitySetting> constructActivitySettings() {
        return SettingsContainer.<IActivitySetting>builder()
                .setting(RelicActivitySetting.builderRelic("slap", "recharge").build())
                .setting(RelicActivitySetting.builderRelic("drill", "recharge").color(ColorScheme.BAR_GREEN).build())
                .build();
    }

    @EventBusSubscriber
    public static class LeftClickHandler {

        @SubscribeEvent
        public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
            Network.sendToServer(new EntityPacket(event.getEntity().getId()) {

                @Override
                public void serverExecute(PacketContext ctx) {
                    if (getEntity(ctx.getLevel()) instanceof Player player)
                        handle(player);
                }
            });
        }

        @SubscribeEvent
        public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
            handle(event.getEntity());
        }

        @SubscribeEvent
        public static void onLeftClickEntity(AttackEntityEvent event) {
            handle(event.getEntity());
        }

        private static void handle(Player player) {
            ItemStack stack = player.getMainHandItem();
            Level level = player.level();

            if (stack.getItem() instanceof ItemMontuHammer item) {

                if (player.isShiftKeyDown() && item.canCast(player, stack, "drill")) {

                    int fortune = stack.getEnchantmentLevel(level.registryAccess().holderOrThrow(Enchantments.FORTUNE));
                    int silkTouch = stack.getEnchantmentLevel(level.registryAccess().holderOrThrow(Enchantments.SILK_TOUCH));

                    double capacity = item.getStatValue(player, stack, "drill", "capacity");
                    double strength = item.getStatValue(player, stack, "drill", "strength");

                    if (!player.level().isClientSide) {
                        MontuDrillEntity drillEntity = new MontuDrillEntity(player.level(),
                                player.getEyePosition(), player.getLookAngle().normalize().scale(0.5), (int) capacity,
                                fortune, silkTouch > 0, (float) strength, player);

                        level.addFreshEntity(drillEntity);
                        item.setMaxCooldown(player, stack, "drill");
                    }

                }
            }
        }
    }

    @EventBusSubscriber
    public static class EventHandler {
        
        @SubscribeEvent
        public static void mouseScrolled(InputEvent.MouseScrollingEvent event) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                ItemStack mainHandItem = player.getMainHandItem();
                if (mainHandItem.getItem() instanceof ItemMontuHammer relic && player.isShiftKeyDown()) {
                    int maxAOE = (int) relic.getStatValue(Minecraft.getInstance().player, mainHandItem, "aoe", "radius");
                    int delta = event.getScrollDeltaY() > 0 ? 1 : -1;
                    int aoe = mainHandItem.getOrDefault(MONTU_AOE, 0);
                    
                    Network.sendToServer(new HammerAOEChangePacket(delta, maxAOE));
                    
                    int newAOE;
                    if (aoe + delta >= 0) {
                        newAOE = (aoe + delta) % (maxAOE + 1);
                    } else {
                        newAOE = maxAOE;
                    }
                    player.displayClientMessage(Component.translatable("tooltip.relics_thirteenflames.montu_hammer.aoemessage").append(" " + (newAOE * 2 + 1) + "x" + (newAOE * 2 + 1)), true);
                    event.setCanceled(true);
                }
            }
        }
        
    }
    
}
