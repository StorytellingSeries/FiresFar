package com.qurenie.relics_thirteenflames.content.items;

import com.qurenie.relics_thirteenflames.content.entities.EntitySeliasetSun;
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
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerlib.api.items.IColoredFoilItem;

import java.util.List;

public class ItemSeliasetSun
		extends RelicItem implements IColoredFoilItem
{
	public ItemSeliasetSun(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public InteractionResult useOn(UseOnContext ctx)
	{
		ItemStack item = ctx.getItemInHand();
		Player player = ctx.getPlayer();
		Level level = ctx.getLevel();
		Vec3 loc = ctx.getClickLocation();
		if(!level.isClientSide && player != null)
		{
			EntitySeliasetSun entity = EntitySeliasetSun.create(level, loc, item.copy());
			entity.setOwnerUUID(player.getStringUUID());
			level.addFreshEntity(entity);
			item.setCount(0);
			player.setItemInHand(ctx.getHand(), ItemStack.EMPTY);
		}
		return InteractionResult.CONSUME;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand)
	{
		ItemStack horn = pPlayer.getItemInHand(pUsedHand);
		pPlayer.startUsingItem(pUsedHand);
		return InteractionResultHolder.success(horn);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, List<Component> tooltip, TooltipFlag isAdvanced) {
		tooltip.add(Component.translatable("tooltip.relics_thirteenflames.seliaset_horn.lore").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
		super.appendHoverText(stack, context, tooltip, isAdvanced);
	}

	@Override
	public RelicData constructDefaultRelicData() {
		return RelicData.builder()
				.abilities(AbilitiesData.builder()
						.ability(AbilityData.builder("leveling")
								.maxLevel(10)
								.stat(StatData.builder("speed")
										.initialValue(220, 220)
										.upgradeModifier(UpgradeOperation.ADD, -20)
										.thresholdValue(20, 200)
										.formatValue(x -> (int) MathUtils.round(x / 20, 0))
										.build())
								.stat(StatData.builder("radius")
										.initialValue(10, 10)
										.thresholdValue(10, 30)
										.upgradeModifier(UpgradeOperation.ADD, 2F)
										.formatValue(x -> (int) MathUtils.round(x, 0))
										.build())
								.build())
						.build())
				.leveling(new LevelingData(100, 10, 100))
				.loot(LootData.builder()
						.entry(LootEntries.TROPIC)
						.entry(LootEntries.VILLAGE)
						.build())
				.build();
	}



	@Override
	public int getFoilColor(@NotNull ItemStack itemStack) {
		return 0;
	}
}