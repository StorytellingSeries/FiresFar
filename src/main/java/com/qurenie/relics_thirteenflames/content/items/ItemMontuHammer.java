package com.qurenie.relics_thirteenflames.content.items;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.blocks.BlockShaking;
import com.qurenie.relics_thirteenflames.content.entities.UsableFallingBlockEntity;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import com.qurenie.relics_thirteenflames.net.HammerAOEChangePacket;
import com.qurenie.relics_thirteenflames.net.PacketPlaySound;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
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
        extends RelicItem implements IColoredFoilItem {
    
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
                zMax = breakRadius;
            }
            case 1 -> {
                yMin = breakDepth;
                yMax = 0;
                zMax = breakRadius;
            }
            case 2 -> {
                xMax = breakRadius;
                zMin = 0;
                zMax = breakDepth;
                yOffset = breakRadius - 1;
            }
            case 3 -> {
                xMax = breakRadius;
                zMax = 0;
                zMin = breakDepth;
                yOffset = breakRadius - 1;
            }
            case 4 -> {
                xMax = breakDepth;
                xMin = 0;
                zMax = breakRadius;
                yOffset = breakRadius - 1;
            }
            case 5 -> {
                xMin = breakDepth;
                xMax = 0;
                zMax = breakRadius;
                yOffset = breakRadius - 1;
            }
        }
        
        if (breakRadius == 0) {
            yOffset = 0;
        }
        
        return Tuples.immutable(pos.offset(-xMin, yOffset - yMin, -zMin), pos.offset(xMax, yOffset + yMax, zMax));
    }
    
    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("slap")
                                .maxLevel(4)
                                .stat(StatData.builder("cooldown")
                                        .initialValue(40.0, 60.0)
                                        .upgradeModifier(UpgradeOperation.ADD, -5.0)
                                        .formatValue(x -> (int) MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(StatData.builder("radius")
                                        .initialValue(3.0, 4.0)
                                        .thresholdValue(3.0, 10)
                                        .upgradeModifier(UpgradeOperation.ADD, 1.5)
                                        .formatValue(x -> (int) MathUtils.round(x, 0))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityData.builder("aoe")
                                .maxLevel(2)
                                .requiredPoints(3)
                                .stat(StatData.builder("radius")
                                        .initialValue(0.0, 0.0)
                                        .thresholdValue(0.0, 2.0)
                                        .upgradeModifier(UpgradeOperation.ADD, 1.0)
                                        .formatValue(x -> (int) MathUtils.round(x + 1, 0))
                                        .build()
                                )
                                .build())
                        .build()
                )
                .leveling(new LevelingData(100, 10, 100))
                .loot(LootData.builder().entry(LootEntries.MINESHAFT).entry(LootEntries.END_LIKE).build())
                .build();
    }
    
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext level, @NotNull List<Component> tooltip, @NotNull TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("tooltip.relics_thirteenflames.montu_hammer.lore").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, level, tooltip, isAdvanced);
    }
    
    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().getBlockState(ctx.getClickedPos()).hasBlockEntity() || ctx.getPlayer() == null || !ctx.getPlayer().isShiftKeyDown())
            return super.useOn(ctx);
        
        var cooldown = getStatValue(ctx.getItemInHand(), "slap", "cooldown");
        var radius = (int) Math.round(getStatValue(ctx.getItemInHand(), "slap", "radius"));
        
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
                    SCHEDULER.schedule(delay, () -> BlockShaking.shake(level, target0, BlockShaking.BEHAVIOR_JUMP.normal()));
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
                    spreadRelicExperience(ctx.getPlayer(), ctx.getItemInHand(), 1);
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
        ctx.getPlayer().getCooldowns().addCooldown(this, (int) (0 * cooldown));
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

        if(radius > getStatValue(stack, "aoe", "radius")) {
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
            spreadRelicExperience(player, stack, blocksMined / 25);
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
        boolean isPrimary = definition.primaryItems().isPresent() && isWeaponOrMiningEnchantment(definition.primaryItems().get());
        boolean supports = isWeaponOrMiningEnchantment(definition.supportedItems());
        return isPrimary || supports && !enchantment.is(Enchantments.UNBREAKING) && !enchantment.is(Enchantments.SWEEPING_EDGE);
    }
    
    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return super.isPrimaryItemFor(stack, enchantment);
    }
    
    private static boolean isWeaponOrMiningEnchantment(HolderSet<Item> holders) {
        return holders.unwrapKey().map(tag -> tag == ItemTags.SWORD_ENCHANTABLE || tag == ItemTags.MINING_ENCHANTABLE ||
                tag == ItemTags.MINING_LOOT_ENCHANTABLE).orElse(false) ;
    }
    
    @Override
    public boolean isEnchantable(@NotNull ItemStack pStack) {
        return true;
    }
    
    @Override
    public int getFoilColor(@NotNull ItemStack stack) {
        return new Color(116, 229, 0).getRGB();
    }
    
    @EventBusSubscriber
    public static class EventHandler {
        
        @SubscribeEvent
        public static void mouseScrolled(InputEvent.MouseScrollingEvent event) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                ItemStack mainHandItem = player.getMainHandItem();
                if (mainHandItem.getItem() instanceof ItemMontuHammer relic && player.isShiftKeyDown()) {
                    int maxAOE = (int) relic.getStatValue(mainHandItem, "aoe", "radius");
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
