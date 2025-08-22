package com.qurenie.relics_thirteenflames.content.items;

import com.qurenie.relics_thirteenflames.content.container.ScrollOfTruthContainer;
import com.qurenie.relics_thirteenflames.content.items.misc.ScrollColorMode;
import com.qurenie.relics_thirteenflames.net.ScrollChangeModePacket;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerlib.net.Network;

import java.util.Collection;
import java.util.List;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.SCROLL_COLOR_MODE;
import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.SCROLL_XP_COUNTER;

@EventBusSubscriber
public class ScrollOfTruthItem extends RelicItem {
    
    public ScrollOfTruthItem(Properties props) {
        super(props);
    }
    
    public static int getEffectLevel(ItemStack scroll) {
        if (!(scroll.getItem() instanceof IRelicItem relic)) return -1;
        return (int) relic.getStatValue(scroll, "passive_effect", "effectLevel");
    }
    
    public static float getCostModifier(ItemStack scroll) {
        if (!(scroll.getItem() instanceof IRelicItem relic)) return -1;
        return (float) relic.getStatValue(scroll, "enchant", "costModifier");
    }
    
    public static int getFullEnchantmentCost(ItemStack scroll, Collection<EnchantmentInstance> instances) {
        float costModifier = getCostModifier(scroll); //0.5 <- 1.5
        float fullCost = 0;
        for (EnchantmentInstance inst : instances) {
            float cost = getFullLevelCost(inst);
            fullCost += cost;
        }
        return Math.round(Math.max(fullCost * costModifier, 0));
    }
    
    public static float getFullLevelCost(EnchantmentInstance inst) {
        if (inst.enchantment.is(EnchantmentTags.CURSE)) return -9f;
        
        return inst.enchantment.value().definition().minCost().base()
                + inst.enchantment.value().definition().minCost().perLevelAboveFirst() / 2f;
    }
    
    @SubscribeEvent
    public static void onXPChange(PlayerXpEvent.XpChange event) {
        if (event.getAmount() <= 0) return;
        for (ItemStack stack : event.getEntity().getInventory().items) {
            if (stack.getItem() instanceof ScrollOfTruthItem scroll) {
                int cntXP = stack.getOrDefault(SCROLL_XP_COUNTER, 0);
                cntXP += event.getAmount();
                scroll.spreadRelicExperience(event.getEntity(), stack, cntXP / 5);
                stack.set(SCROLL_XP_COUNTER, cntXP % 5);
            }
        }
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
    public void appendHoverText(ItemStack stack, TooltipContext tooltip, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("tooltip.relics_thirteenflames.scroll_of_truth.lore").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, tooltip, components, flag);
    }
    
    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("enchant")
                                .maxLevel(10)
                                .stat(StatData.builder("costModifier")
                                        .initialValue(3, 2.8)
                                        .upgradeModifier(UpgradeOperation.ADD, -0.2)
                                        .formatValue(x -> (int) MathUtils.round(x * 100, 0))
                                        .build())
                                .build())
                        .ability(AbilityData.builder("passive_effect")
                                .maxLevel(1)
                                .stat(StatData.builder("effectLevel")
                                        .initialValue(0, 0)
                                        .thresholdValue(0, 2)
                                        .upgradeModifier(UpgradeOperation.ADD, 1)
                                        .formatValue(x -> (int) (MathUtils.round(x + 1, 1)))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingData(100, 11, 100))
                .loot(LootData.builder().entry(LootEntries.END_LIKE).build())
                .build();
    }
    
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);
        if (!level.isClientSide && entity instanceof Player player) {
            ScrollColorMode mode = stack.getOrDefault(SCROLL_COLOR_MODE, ScrollColorMode.GRAY);
            int lvl = getEffectLevel(stack);
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
