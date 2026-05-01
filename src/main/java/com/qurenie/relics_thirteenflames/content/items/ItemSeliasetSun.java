package com.qurenie.relics_thirteenflames.content.items;

import com.qurenie.api.IExtRelicItem;
import com.qurenie.relics_thirteenflames.content.entities.EntitySeliasetSun;
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
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerlib.api.items.IColoredFoilItem;

import java.util.List;

public class ItemSeliasetSun extends RelicItem implements IExtRelicItem, IColoredFoilItem
{
	public ItemSeliasetSun(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public @NotNull InteractionResult useOn(UseOnContext ctx)
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
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player pPlayer, InteractionHand pUsedHand)
	{
		ItemStack horn = pPlayer.getItemInHand(pUsedHand);
		pPlayer.startUsingItem(pUsedHand);
		return InteractionResultHolder.success(horn);
	}

	@Override
	public void gatherCreativeTabContent(CreativeContentConstructor constructor) {
	}

	@Override
	public RelicTemplate constructDefaultRelicTemplate() {
		return RelicTemplate.builder()
				.abilities(AbilitiesTemplate.builder()
						.ability(AbilityTemplate.builder("blessed_light")
								.initialMaxLevel(5)
								.experienceSources(ExperienceSourcesTemplate.builder()
										.source("source_1")
										.build())
								.stat(AbilityStatTemplate.builder("speed")
										.initialValue(240, 200)
										.upgradeModifier(RelicsScalingModels.ADDITIVE.get(), -35)
										.thresholdValue(20, 220)
										.formatValue(x -> (int) MathUtils.round(x / 20, 0))
										.build())
								.stat(AbilityStatTemplate.builder("breed_chance")
										.initialValue(10, 30)
										.upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 8)
										.thresholdValue(20, 70)
										.formatValue(x -> MathUtils.round(x, 0))
										.build())
								.stat(AbilityStatTemplate.builder("radius")
										.initialValue(8, 12)
										.thresholdValue(10, 30)
										.upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 4F)
										.formatValue(x -> (int) MathUtils.round(x, 0))
										.build())
								.build())
						.ability(AbilityTemplate.builder("heat")
								.initialMaxLevel(5)
								.research(ResearchTemplate.builder()
										.star(0, 6, 26).star(1, 9, 23).star(2, 12, 23).star(3, 15, 26).star(4, 4, 13).star(5, 18, 11).star(6, 8, 9).star(7, 13, 9).star(8, 18, 23).star(9, 3, 23)
										.link(6, 1).link(1, 2).link(2, 7).link(2, 5).link(2, 3).link(1, 0).link(1, 4).link(3, 8).link(0, 9)
										.build())
								.experienceSources(ExperienceSourcesTemplate.builder()
										.source("source_1")
										.build())
								.stat(AbilityStatTemplate.builder("heat_time")
										.initialValue(15, 12)
										.upgradeModifier(RelicsScalingModels.ADDITIVE.get(), -2)
										.thresholdValue(2, 15)
										.formatValue(x -> (int) MathUtils.round(x, 0))
										.build())
								.stat(AbilityStatTemplate.builder("damage")
										.initialValue(2, 5)
										.upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 1)
										.formatValue(x -> (int) MathUtils.round(x, 1))
										.thresholdValue(2, 10)
										.build())
								.stat(AbilityStatTemplate.builder("radius")
										.initialValue(3, 3)
										.thresholdValue(3, 18)
										.upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 3F)
										.formatValue(x -> (int) MathUtils.round(x, 1))
										.build())
								.build())
						.build())
				.leveling(LevelingTemplate.builder()
						.initialCost(100)
						.step(130)
						.maxRank(2)
						.build())
				.loot(LootTemplate.builder()
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