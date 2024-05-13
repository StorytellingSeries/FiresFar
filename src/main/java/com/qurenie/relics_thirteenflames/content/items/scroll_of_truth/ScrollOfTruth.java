package com.qurenie.relics_thirteenflames.content.items.scroll_of_truth;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.screen.ScrollOfTruthContainerScreen;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.NBTUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.net.Network;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Mod.EventBusSubscriber
public class ScrollOfTruth extends RelicItem {

    public ScrollOfTruth(Properties props){
        super(props);
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer splayer && hand == InteractionHand.MAIN_HAND){

            ItemStack scroll = player.getItemInHand(hand);
            NetworkHooks.openScreen(splayer,new ScrollOfTruthContainer.Provider(scroll),buf->{
                buf.writeItem(scroll);
            });
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("tooltip.relics_thirteenflames.scroll_of_truth.lore").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, level, tooltip, isAdvanced);
    }

    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("enchant")
                                .maxLevel(10)
                                .stat(StatData.builder("costModifier")
                                        .initialValue(3, 2.8)
                                        .upgradeModifier(UpgradeOperation.ADD,-0.2)
                                        .formatValue(x -> (int)MathUtils.round(x * 100,0))
                                        .build())
                                .build())
                        .ability(AbilityData.builder("passive_effect")
                                .maxLevel(1)
                                .requiredPoints(5)
                                .stat(StatData.builder("effectLevel")
                                        .initialValue(0,0)
                                        .thresholdValue(0,2)
                                        .upgradeModifier(UpgradeOperation.ADD,1)
                                        .formatValue(x->(int)(MathUtils.round(x+1,1)))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingData(100, 15, 100))

                .build();
    }


    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);
        if (!level.isClientSide && entity instanceof Player player){
            ScrollColorMode mode = ScrollColorMode.fromTag(stack.getOrCreateTag());
            int lvl = getEffectLevel(stack);
            player.addEffect(new MobEffectInstance(mode.effect.get(),25,lvl,true,true));
        }

    }

    public static int getEffectLevel(ItemStack scroll){
        if(!(scroll.getItem() instanceof IRelicItem relic)) return -1;
        return (int) relic.getAbilityValue(scroll,"passive_effect","effectLevel");
    }
    public static float getCostModifier(ItemStack scroll){
        if(!(scroll.getItem() instanceof IRelicItem relic)) return -1;
        return (float) relic.getAbilityValue(scroll,"enchant","costModifier");
    }

    public static ScrollColorMode getMode(ItemStack item){
        return ScrollColorMode.fromTag(item.getOrCreateTag());
    }

    public static int getFullEnchantmentCost(ItemStack scroll, Collection<ScrollOfTruthContainerScreen.EnchantmentInstance> instances){
        float costModifier = getCostModifier(scroll); //0.5 <- 1.5
        float fullCost = 0;
        for (ScrollOfTruthContainerScreen.EnchantmentInstance inst : instances){
            float cost = getFullLevelCost(inst);
            fullCost += cost;
        }
        return Math.round(Math.max(fullCost * costModifier, 0));
    }
    public static float getFullLevelCost(ScrollOfTruthContainerScreen.EnchantmentInstance inst){
        float base = getEnchantmentBaseLevelCost(inst.enchantment);

        return base + base * (inst.lvl - 1) * 0.35f;
    }
    public static float getEnchantmentBaseLevelCost(Enchantment e){
        if(e.isCurse()) return -2.5f;
        switch (e.getRarity()){
            case COMMON -> {
                return 1f;
            }
            case UNCOMMON -> {
                return 1.5f;
            }
            case RARE, VERY_RARE -> {
                return 2.2f;
            }
            default -> {
                return 10;
            }
        }
    }


    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }


    @Mod.EventBusSubscriber(modid = ThirteenFlames.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE,value = Dist.CLIENT)
    public static class EventHandler{

        @SubscribeEvent
        public static void mouseScrolled(InputEvent.MouseScrollingEvent event){
            Player player = Minecraft.getInstance().player;
            if (player != null){
                ItemStack mainHandItem = player.getMainHandItem();
                if (mainHandItem.is(ItemsRegistry.SCROLL_OF_TRUTH) && player.isShiftKeyDown()){
                    Network.sendToServer(new ScrollChangeModePacket(event.getScrollDelta() > 0 ? 1 : -1));
                    event.setCanceled(true);
                }
            }
        }

    }

    @SubscribeEvent
    public static void onXPChange(PlayerXpEvent.XpChange event){
        if(event.getAmount() <= 0) return;
        for(ItemStack stack : event.getEntity().getInventory().items) {
            if(stack.getItem() instanceof  ScrollOfTruth scroll) {
                int cntXP = NBTUtils.getInt(stack, "XPcounter", 0);
                cntXP += event.getAmount();
                scroll.spreadExperience(event.getEntity(), stack, cntXP / 5);
                NBTUtils.setInt(stack, "XPcounter", cntXP % 5);
            }
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IClientItemExtensions() {

            private ScrollOfTruthISTER ister = new ScrollOfTruthISTER();
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ister;
            }
        });
    }
}
