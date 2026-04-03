package com.qurenie.relics_thirteenflames.content.items;

import com.qurenie.api.IActivityContainer;
import com.qurenie.api.IExtRelicItem;
import com.qurenie.api.SettingsContainer;
import com.qurenie.relics_thirteenflames.activity.IActivitySetting;
import com.qurenie.relics_thirteenflames.activity.RelicActivitySetting;
import com.qurenie.relics_thirteenflames.content.blocks.BlockShaking;
import com.qurenie.relics_thirteenflames.content.entities.SkintClusterEntity;
import com.qurenie.relics_thirteenflames.content.entities.UsableFallingBlockEntity;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import com.qurenie.relics_thirteenflames.net.HammerAOEChangePacket;
import com.qurenie.relics_thirteenflames.net.PacketPlaySound;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
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
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerlib.api.items.IColoredFoilItem;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.hammerlib.util.java.tuples.Tuple2;
import org.zeith.hammerlib.util.java.tuples.Tuples;

import java.awt.*;
import java.util.List;
import java.util.*;

import static com.qurenie.relics_thirteenflames.ThirteenFlames.SCHEDULER;
import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.BLOCKS_MINED;
import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.MONTU_AOE;

public class ItemMontuHammer
        extends RelicItem implements IColoredFoilItem, IExtRelicItem, IActivityContainer {
    
    protected final Tool tool;
    protected final TagKey<Block> blocks = BlockTags.MINEABLE_WITH_PICKAXE;
    protected final float speed;
    private final ItemAttributeModifiers defaultModifiers;
    private static final Random RNG = new Random();
    
    
    public ItemMontuHammer(Properties properties, Tier tier) {
        super(properties);
        this.tool = new Tool(List.of(Tool.Rule.deniesDrops(tier.getIncorrectBlocksForDrops()),
                Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_PICKAXE, tier.getSpeed()),
                Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_SHOVEL, tier.getSpeed())),
                1.0F, 1);
        this.speed = tier.getSpeed();
        float attackDamageBaseline = 5 + tier.getAttackDamageBonus();
        defaultModifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, attackDamageBaseline, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.1F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
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
                                .stat(AbilityStatTemplate.builder("recharge")
                                        .initialValue(60, 40)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), -5.0)
                                        .formatValue(x -> (int) MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("radius")
                                        .initialValue(3.0, 4.0)
                                        .thresholdValue(3.0, 10)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 1.5)
                                        .formatValue(x -> (int) MathUtils.round(x, 0))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityTemplate.builder("aoe")
                                .initialMaxLevel(2)
                                .requiredPoints(3)
                                .stat(AbilityStatTemplate.builder("radius")
                                        .initialValue(0.0, 0.0)
                                        .thresholdValue(0.0, 2.0)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 1.0)
                                        .formatValue(x -> (int) MathUtils.round(x + 1, 0))
                                        .build()
                                )
                                .build())
                        .build()
                )
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(100)
                        .maxRank(2)
                        .build())
                .loot(LootTemplate.builder().entry(LootEntries.MINESHAFT).build())
                .build();
    }
    
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext level, @NotNull List<Component> tooltip, @NotNull TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("tooltip.relics_thirteenflames.montu_hammer.lore").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, level, tooltip, isAdvanced);
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
    
    @Override
    public boolean mineBlock(ItemStack stack, @NotNull Level world, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity miner)
    {
        int radius = stack.getComponents().getOrDefault(MONTU_AOE.get(), 0);

        if(radius > getStatValue(miner, stack, "aoe", "radius")) {
            radius = 0;
            stack.set(MONTU_AOE.get(),  radius);
        }
        
        Player player = Cast.cast(miner, Player.class);
        
        if (player == null
                || player.isShiftKeyDown()
                || !stack.canPerformAction(ItemAbilities.PICKAXE_DIG)
        ) return false;
        
        Vec3 view = player.getViewVector(0);
        Vec3 look = player.getEyePosition(0);
        
        var aoe = getMiningArea(pos,
                world.clip(new ClipContext(look, look.add(view), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player))
                        .getDirection().get3DDataValue(),
                radius, 0
        );
        
        var itr = BlockPos.betweenClosedStream(aoe.a(), aoe.b()).map(BlockPos::new).iterator();
        
        int blocksMined = stack.getOrDefault(BLOCKS_MINED, 0);
        while (itr.hasNext()) {
            BlockPos target = itr.next();
            
            if (world.isEmptyBlock(target) || target.equals(pos))
                continue;
            blocksMined++;
            state = world.getBlockState(target);
            if (state.canHarvestBlock(world, target, player) && state.getDestroySpeed(world, pos) >= 0) {
                state.getBlock().playerDestroy(world, player, target, state, world.getBlockEntity(target), stack);
                
                world.destroyBlock(target, false);
            }
        }
        blocksMined++;
        if (blocksMined >= 25) {
            addExperience(player, stack, blocksMined / 25);
            blocksMined %= 25;
        }
        
        stack.set(BLOCKS_MINED, blocksMined);
        return false;
    }
    
    @Override
    public float getDestroySpeed(ItemStack pStack, BlockState pState) {
        return pState.is(BlockTags.MINEABLE_WITH_PICKAXE) || pState.is(BlockTags.MINEABLE_WITH_SHOVEL)
                ? this.speed
                : 1.0F;
    }
    
    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return defaultModifiers;
    }
    
    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL)) && tool.isCorrectForDrops(state);
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
                .build();
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
