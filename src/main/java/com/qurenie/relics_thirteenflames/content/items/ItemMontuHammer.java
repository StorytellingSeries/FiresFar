package com.qurenie.relics_thirteenflames.content.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.blocks.BlockShaking;
import com.qurenie.relics_thirteenflames.content.entities.UsableFallingBlockEntity;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
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
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.Scheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.api.items.IColoredFoilItem;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.hammerlib.util.java.tuples.Tuple2;
import org.zeith.hammerlib.util.java.tuples.Tuples;

import java.awt.*;
import java.util.List;
import java.util.*;

public class ItemMontuHammer
        extends RelicItem implements IColoredFoilItem
{
    protected final Tier tier;
    protected final TagKey<Block> blocks = BlockTags.MINEABLE_WITH_PICKAXE;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;
    protected final float speed;
    private Random rng = new Random();


    public ItemMontuHammer(Properties properties, Tier tier)
    {
        super(properties);
        this.tier = tier;
        this.speed = tier.getSpeed();
        float attackDamageBaseline = 5 + tier.getAttackDamageBonus();
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", attackDamageBaseline, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -3.1F, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("slap")
                                .maxLevel(4)
                                .stat(StatData.builder("cooldown")
                                        .initialValue(60.0, 40.0)
                                        .upgradeModifier(UpgradeOperation.ADD, -5.0)
                                        .formatValue(x -> (int) MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(StatData.builder("radius")
                                        .initialValue(3.0, 4.0)
                                        .thresholdValue(3.0, 8.0)
                                        .upgradeModifier(UpgradeOperation.ADD, 1)
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
                .build();
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("tooltip.relics_thirteenflames.montu_hammer.lore").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, level, tooltip, isAdvanced);
    }
    @Override
    public InteractionResult useOn(UseOnContext ctx)
    {
        if(ctx.getLevel().getBlockState(ctx.getClickedPos()).hasBlockEntity() || ctx.getPlayer() == null || !ctx.getPlayer().isShiftKeyDown()) return super.useOn(ctx);

        var cooldown = getAbilityValue(ctx.getItemInHand(), "slap", "cooldown");
        var radius = (int) Math.round(getAbilityValue(ctx.getItemInHand(), "slap", "radius"));

        var level = ctx.getLevel();

        if(level.isClientSide())
            return InteractionResult.SUCCESS;

        var pos = ctx.getClickedPos();
        var itr = BlockPos.betweenClosedStream(pos.offset(-radius, -radius * 3, -radius), pos.offset(radius, 0, radius))
                .filter( bs -> new Vec3(bs.getX(), pos.getY(), bs.getZ()).distanceTo(new Vec3(pos.getX(), pos.getY(), pos.getZ())) <= radius)
                .map(BlockPos::new)
                .iterator();

        int lowestY = level.getMinBuildHeight();

        Map<TagKey<Block>, BlockState> replaces = new HashMap<>();
        replaces.put(Tags.Blocks.ORES_IN_GROUND_STONE, Blocks.STONE.defaultBlockState());
        replaces.put(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE, Blocks.DEEPSLATE.defaultBlockState());
        replaces.put(Tags.Blocks.ORES_IN_GROUND_NETHERRACK, Blocks.NETHERRACK.defaultBlockState());

        Set<BlockPos> pulled = new HashSet<>();
        while(itr.hasNext())
        {
            var target = itr.next();

            if(target.getY() == pos.getY() && level.getBlockState(target.above()).canBeReplaced(Fluids.WATER)) // shaking the ground
            {
                int delay = Mth.floor(Mth.sqrt((float) pos.distSqr(target)) * 5);

                BlockPos sp = new BlockPos(target);
                while(level.isEmptyBlock(sp) && level.isInWorldBounds(sp) && sp.getY() >= lowestY)
                    sp = sp.below();

                BlockPos target0 = sp;

                if(!level.isEmptyBlock(target0))
                    Scheduler.schedule(delay, () ->
                    {
                        BlockShaking.shake(level, target0, BlockShaking.BEHAVIOR_JUMP.normal());
                    });
            }

            var state = level.getBlockState(target);

            final var replaceWith = replaces.entrySet()
                    .stream()
                    .filter(e -> state.is(e.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
            if(replaceWith == null) continue;

            BlockPos height = target;
            BlockPos cntHeight = height;
            double cntSpd = 0;

            int attempts = 16;
            while(--attempts > 0)
            {
                while(level.isInWorldBounds(height) && (!level.isEmptyBlock(height) || height.getY() < pos.getY())) {
                    height = height.above();
                    cntHeight = height;
                }

                while(level.isEmptyBlock(height.below())) {
                    height = height.below();
                    cntHeight = height;
                }

                while(pulled.contains(cntHeight)) {
                    cntHeight = cntHeight.above();
                    cntSpd += 0.15;
                }

                if(level.isEmptyBlock(height)/* && !pulled.contains(height)*/)
                    break;
            }
            if(attempts == 0) continue;

            if(level.isEmptyBlock(height))
            {
                pulled.add(height);
                final var height0 = height;
                int delay = Mth.floor(
                        Mth.sqrt((float) pos.distSqr(new BlockPos(target.getX(), pos.getY(), target.getZ()))) * 5);
                double finalCntSpd = cntSpd;



                Scheduler.schedule(Math.max(0, delay - 2), () ->
                {
                    spreadExperience(ctx.getPlayer(), ctx.getItemInHand(), 1);
                    UsableFallingBlockEntity fbe = UsableFallingBlockEntity.createFalling(level, height0, state, ctx.getItemInHand());
                    Vec3 dist = new Vec3(height0.getX(), pos.getY(), height0.getZ()).subtract(new Vec3(pos.getX(), pos.getY(), pos.getZ())).yRot(rng.nextFloat(-10, 10) * Mth.DEG_TO_RAD);
                    Vec3 move = dist.normalize().scale(Math.min(0.6, 0.2 / dist.length())).add(new Vec3(rng.nextFloat(0.05f),0,0).yRot(rng.nextFloat(3.14f)));
                    fbe.setDeltaMovement(move.scale(rng.nextFloat(0.8f, 2.2f) + finalCntSpd * 10).add(0, rng.nextFloat(0.3f, 0.5f) + finalCntSpd, 0));
                    fbe.setLifeTime(600);
                    level.addFreshEntity(fbe);
                    level.setBlockAndUpdate(target, replaceWith);
                });

            }
        }
        Network.sendToAll(new PacketPlaySound(ctx.getClickLocation(), SoundsRegistry.MONTU_SLAP.get(), SoundSource.MASTER, 1, 1));
        ctx.getPlayer().getCooldowns().addCooldown(this, (int) (20 * cooldown));
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner)
    {
        var radius = stack.getOrCreateTag().getInt("montuAOE");

        if(radius > getAbilityValue(stack, "aoe", "radius")) {
            radius = 0;
            stack.getOrCreateTag().putInt("montuAOE", 0);
        }
        Player player = Cast.cast(miner, Player.class);

        if(player == null
                || player.isShiftKeyDown()
                || !stack.canPerformAction(ToolActions.PICKAXE_DIG)
        ) return false;

        Vec3 view = player.getViewVector(0);
        Vec3 look = player.getEyePosition(0);

        var aoe = getMiningArea(pos,
                world.clip(new ClipContext(look, look.add(view), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player))
                        .getDirection().get3DDataValue(),
                radius, 0
        );

        var itr = BlockPos.betweenClosedStream(aoe.a(), aoe.b()).map(BlockPos::new).iterator();

        int blocksMined = stack.getOrCreateTag().getInt("blocksmined");
        while(itr.hasNext())
        {
            BlockPos target = itr.next();

            if(world.isEmptyBlock(target) || target.equals(pos))
                continue;
            blocksMined++;
            state = world.getBlockState(target);
            if(state.canHarvestBlock(world, target, player) && state.getDestroySpeed(world, pos) >= 0)
            {
                state.getBlock().playerDestroy(world, player, target, state, world.getBlockEntity(target), stack);

                world.destroyBlock(target, false);
            }
        }
        blocksMined++;
        if(blocksMined >= 25){
            spreadExperience(player, stack, blocksMined / 25);
            blocksMined %= 25;
        }
        stack.getOrCreateTag().putInt("blocksmined", blocksMined);
        return false;
    }

    @Override
    public float getDestroySpeed(ItemStack pStack, BlockState pState)
    {
        return pState.is(BlockTags.MINEABLE_WITH_PICKAXE) || pState.is(BlockTags.MINEABLE_WITH_SHOVEL)
                ? this.speed
                : 1.0F;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot)
    {
        return slot == EquipmentSlot.MAINHAND
                ? this.defaultModifiers
                : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState pBlock)
    {
        if(net.minecraftforge.common.TierSortingRegistry.isTierSorted(tier))
        {
            return net.minecraftforge.common.TierSortingRegistry.isCorrectTierForDrops(tier, pBlock) &&
                    (pBlock.is(BlockTags.MINEABLE_WITH_PICKAXE) || pBlock.is(BlockTags.MINEABLE_WITH_SHOVEL));
        }
        int i = this.tier.getLevel();
        if(i < 3 && pBlock.is(BlockTags.NEEDS_DIAMOND_TOOL))
        {
            return false;
        } else if(i < 2 && pBlock.is(BlockTags.NEEDS_IRON_TOOL))
        {
            return false;
        } else
        {
            return i < 1 && pBlock.is(BlockTags.NEEDS_STONE_TOOL)
                    ? false
                    : (pBlock.is(BlockTags.MINEABLE_WITH_PICKAXE) || pBlock.is(BlockTags.MINEABLE_WITH_SHOVEL));
        }
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state)
    {
        return (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL)) &&
                net.minecraftforge.common.TierSortingRegistry.isCorrectTierForDrops(tier, state);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction)
    {
        return net.minecraftforge.common.ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(toolAction);
    }

    public static Tuple2<BlockPos, BlockPos> getMiningArea(BlockPos pos, int sideHit, int breakRadius, int breakDepth)
    {
        int xMax = breakRadius;
        int xMin = breakRadius;
        int yMax = breakRadius;
        int yMin = breakRadius;
        int zMax = breakRadius;
        int zMin = breakRadius;

        int yOffset = 0;

        switch(sideHit)
        {
            case 0 ->
            {
                yMax = breakDepth;
                yMin = 0;
                zMax = breakRadius;
            }
            case 1 ->
            {
                yMin = breakDepth;
                yMax = 0;
                zMax = breakRadius;
            }
            case 2 ->
            {
                xMax = breakRadius;
                zMin = 0;
                zMax = breakDepth;
                yOffset = breakRadius - 1;
            }
            case 3 ->
            {
                xMax = breakRadius;
                zMax = 0;
                zMin = breakDepth;
                yOffset = breakRadius - 1;
            }
            case 4 ->
            {
                xMax = breakDepth;
                xMin = 0;
                zMax = breakRadius;
                yOffset = breakRadius - 1;
            }
            case 5 ->
            {
                xMin = breakDepth;
                xMax = 0;
                zMax = breakRadius;
                yOffset = breakRadius - 1;
            }
        }

        if(breakRadius == 0)
        {
            yOffset = 0;
        }

        return Tuples.immutable(pos.offset(-xMin, yOffset - yMin, -zMin), pos.offset(xMax, yOffset + yMax, zMax));
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 20;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.category == EnchantmentCategory.WEAPON && enchantment != Enchantments.SWEEPING_EDGE || enchantment.category == EnchantmentCategory.DIGGER && enchantment != Enchantments.UNBREAKING;
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return true;
    }

    @Mod.EventBusSubscriber(modid = ThirteenFlames.MODID,bus = Mod.EventBusSubscriber.Bus.FORGE,value = Dist.CLIENT)
    public static class EventHandler{

        @SubscribeEvent
        public static void mouseScrolled(InputEvent.MouseScrollingEvent event){
            Player player = Minecraft.getInstance().player;
            if (player != null){
                ItemStack mainHandItem = player.getMainHandItem();
                if (mainHandItem.getItem() instanceof ItemMontuHammer relic && player.isShiftKeyDown()){
                    int maxAOE = (int) relic.getAbilityValue(mainHandItem, "aoe", "radius");
                    int delta = event.getScrollDelta() > 0 ? 1 : -1;
                    int aoe = mainHandItem.getOrCreateTag().getInt("montuAOE");

                    Network.sendToServer(new HammerAOEChangePacket(delta, maxAOE));

                    int newAOE;
                    if (aoe + delta >= 0) {
                        newAOE = (aoe + delta) % (maxAOE + 1);
                    }else{
                        newAOE = maxAOE;
                    }
                    player.displayClientMessage(Component.translatable("tooltip.relics_thirteenflames.montu_hammer.aoemessage").append(" " + (newAOE * 2 + 1) + "x" + (newAOE * 2 + 1)), true);
                    event.setCanceled(true);
                }
            }
        }

    }

    @Override
    public int getFoilColor(@NotNull ItemStack stack) {
        return new Color(116, 229, 0).getRGB();
    }

}
