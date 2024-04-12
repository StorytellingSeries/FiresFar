package com.qurenie.relics_thirteenflames.content.items;

import com.google.common.base.Suppliers;
import com.qurenie.relics_thirteenflames.client.render.item.SeliasetSunItemRenderer;
import com.qurenie.relics_thirteenflames.content.entities.EntitySeliasetSun;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerlib.api.items.IColoredFoilItem;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemSeliasetSun
		extends RelicItem implements IColoredFoilItem
{
	public ItemSeliasetSun(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer)
	{
		consumer.accept(new IClientItemExtensions()
		{
			final Supplier<SeliasetSunItemRenderer> renderer = Suppliers.memoize(SeliasetSunItemRenderer::new);
			
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer()
			{
				return renderer.get();
			}
		});
	}
	
	@Override
	public InteractionResult useOn(UseOnContext ctx)
	{
		ItemStack item = ctx.getItemInHand();
		Player player = ctx.getPlayer();
		Level level = ctx.getLevel();
		Vec3 loc = ctx.getClickLocation();
		if(!level.isClientSide)
		{
			EntitySeliasetSun entity = EntitySeliasetSun.create(level, loc, item.copy());
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
	public RelicData constructDefaultRelicData() {
		return RelicData.builder()
				.abilities(AbilitiesData.builder()
						.ability(AbilityData.builder("leveling")
								.stat(StatData.builder("speed")
										.initialValue(200, 200)
										.upgradeModifier(UpgradeOperation.ADD, -20)
										.thresholdValue(20, 200)
										.formatValue(x -> Math.round(x / 2F) / 10F)
										.build())
								.stat(StatData.builder("radius")
										.initialValue(10, 10)
										.thresholdValue(10, 30)
										.upgradeModifier(UpgradeOperation.ADD, 2F)
										.formatValue(x -> (int) Math.round(x))
										.build())
								.build())
						.build())
				.leveling(new LevelingData(100, 10, 100))
				.build();
	}



	@Override
	public int getFoilColor(@NotNull ItemStack itemStack) {
		return 0;
	}
}