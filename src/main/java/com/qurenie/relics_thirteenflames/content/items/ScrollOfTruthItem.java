package com.qurenie.relics_thirteenflames.content.items;

import com.qurenie.api.IExtRelicItem;
import com.qurenie.relics_thirteenflames.content.container.ScrollOfTruthContainer;
import com.qurenie.relics_thirteenflames.content.entities.GhostBigEntity;
import com.qurenie.relics_thirteenflames.content.entities.GhostSmallEntity;
import com.qurenie.relics_thirteenflames.content.items.misc.ScrollColorMode;
import com.qurenie.relics_thirteenflames.net.ScrollChangeModePacket;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
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
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerlib.HammerLib;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.util.charging.ItemChargeHelper;

import java.util.Collection;
import java.util.List;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.SCROLL_COLOR_MODE;
import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.SCROLL_XP_COUNTER;

@EventBusSubscriber
public class ScrollOfTruthItem extends RelicItem implements IExtRelicItem {
    
    public ScrollOfTruthItem(Properties props) {
        super(props);
    }
    
    public static int getEffectLevel(LivingEntity livingEntity, ItemStack scroll) {
        if (!(scroll.getItem() instanceof IRelicItem relic)) return -1;
        return (int) relic.
                getRelicData(livingEntity, scroll)
                .getAbilitiesData()
                .getAbilityData("passive_effect")
                .getStatData("effectLevel")
                .getValue();
    }
    
    public static float getCostModifier(LivingEntity livingEntity, ItemStack scroll) {
        if (!(scroll.getItem() instanceof IRelicItem relic)) return -1;
        return (float) relic.
                getRelicData(livingEntity, scroll)
                .getAbilitiesData()
                .getAbilityData("enchant")
                .getStatData("costModifier")
                .getValue();
    }
    
    public static int getFullEnchantmentCost(LivingEntity livingEntity, ItemStack scroll, Collection<EnchantmentInstance> instances) {
        float costModifier = getCostModifier(livingEntity, scroll); //0.5 <- 1.5
        float fullCost = 0;
        for (EnchantmentInstance inst : instances) {
            float cost = getFullLevelCost(inst);
            fullCost += cost;
        }
        return Math.round(Math.max(fullCost * costModifier, 0));
    }
    
    public static float getFullLevelCost(EnchantmentInstance inst) {
        if (inst.enchantment.is(EnchantmentTags.CURSE)) return -9f;
        return inst.enchantment.value().definition().minCost().calculate(inst.level);
    }
    
    @SubscribeEvent
    public static void onXPChange(PlayerXpEvent.XpChange event) {
        if (event.getAmount() <= 0) return;
        for (ItemStack stack : event.getEntity().getInventory().items) {
            if (stack.getItem() instanceof ScrollOfTruthItem scroll) {
                int cntXP = stack.getOrDefault(SCROLL_XP_COUNTER, 0);
                cntXP += event.getAmount();
                scroll.addExperience(event.getEntity(), stack, cntXP / 5);
                stack.set(SCROLL_XP_COUNTER, cntXP % 5);
            }
        }
    }

    @Override
    public void gatherCreativeTabContent(CreativeContentConstructor constructor) {
    }

    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (player instanceof ServerPlayer splayer && hand == InteractionHand.MAIN_HAND) {
            ItemStack scroll = player.getItemInHand(hand);
            
            
            splayer.openMenu(new ScrollOfTruthContainer.Provider(scroll), buf -> ItemStack.STREAM_CODEC.encode(buf, scroll));
//            NetworkHooks.openScreen(splayer,new ScrollOfTruthContainer.Provider(scroll),
//                    buf -> ItemStack.STREAM_CODEC.encode(buf, scroll));
            player.startUsingItem(InteractionHand.MAIN_HAND);
            return InteractionResultHolder.consume(scroll);
        }
        return super.use(level, player, hand);
    }

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("enchant")
                                .initialMaxLevel(10)
                                .stat(AbilityStatTemplate.builder("costModifier")
                                        .initialValue(3, 2.5)
                                        .thresholdValue(0.25, 3)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 0.25)
                                        .formatValue(x -> (int) MathUtils.round(x * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("maxLevel")
                                        .initialValue(1, 1.5)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 7)
                                        .thresholdValue(1, 7)
                                        .formatValue(Math::floor)
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_1")
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_2")
                                        .build())
                                .rankModifier(2, "maxup")
                                .build())
                        .ability(AbilityTemplate.builder("passive_effect")
                                .initialMaxLevel(1)
                                .research(ResearchTemplate.builder()
                                        .star(0, 8, 8).star(1, 12, 7).star(2, 13, 11).star(3, 9, 12).star(4, 10, 21).star(5, 8, 18).star(6, 13, 18).star(7, 11, 16).star(8, 19, 14).star(9, 16, 19).star(10, 2, 15).star(11, 4, 18).star(12, 12, 23).star(13, 9, 26)
                                        .link(0, 3).link(3, 2).link(2, 1).link(1, 0).link(7, 5).link(7, 6).link(6, 4).link(4, 5).link(9, 8).link(11, 10).link(12, 13)
                                        .build())
                                .stat(AbilityStatTemplate.builder("effectLevel")
                                        .initialValue(0, 0)
                                        .thresholdValue(0, 3)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 3)
                                        .formatValue(x -> (int) (MathUtils.round(x + 1, 1)))
                                        .build())
                                .rankModifier(1, "addEffect")
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .maxRank(2)
                        .initialCost(100)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder().entry(LootEntries.END_LIKE).build())
                .build();
    }
    
    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);
        if (!level.isClientSide && entity instanceof Player player) {
            ScrollColorMode mode = stack.getOrDefault(SCROLL_COLOR_MODE, ScrollColorMode.GRAY);
            int lvl = getEffectLevel(player, stack);
            player.addEffect(new MobEffectInstance(mode.effect, 25, lvl, true, true));
        }
        
    }
    
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }
    
    @EventBusSubscriber
    public static class EventHandler {

        @SubscribeEvent
        public static void livingDeath(LivingDeathEvent e) {

            if (e.getEntity().level().isClientSide() )
                return;

            DamageSource source = e.getSource();

            if (!(source.getEntity() instanceof Player player))
                return;

            // Проверяем наличие свитка
            ItemStack scroll = null;

            for (ItemStack stack : player.getInventory().items) {
                if (stack.is(ItemsRegistry.SCROLL_OF_TRUTH) && ItemsRegistry.SCROLL_OF_TRUTH.hasRangModifier(player, stack, "passive_effect", "addEffect")) {
                    scroll = stack;
                    break;
                }
            }

            if (scroll == null)
                return;

            int lvl = getEffectLevel(player, scroll);
            ScrollColorMode[] values = ScrollColorMode.values();
            ScrollColorMode randomMode =
                    values[player.getRandom().nextInt(values.length)];

            // 3 секунды = 60 тиков
            MobEffectInstance effect = new MobEffectInstance(
                    randomMode.effect,
                    200,
                    lvl,
                    false,
                    true,
                    true
            );

            player.addEffect(effect);
        }
        
        @SubscribeEvent
        public static void mouseScrolled(InputEvent.MouseScrollingEvent event) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                ItemStack mainHandItem = player.getMainHandItem();
                if (mainHandItem.is(ItemsRegistry.SCROLL_OF_TRUTH) && player.isShiftKeyDown()) {
                    Network.sendToServer(new ScrollChangeModePacket(event.getScrollDeltaY() > 0 ? 1 : -1));
                    event.setCanceled(true);
                }
            }
        }
        
    }
    
}
