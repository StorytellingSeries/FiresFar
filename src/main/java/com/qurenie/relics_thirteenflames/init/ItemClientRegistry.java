package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.client.style.RelicStyleStructured;
import com.qurenie.relics_thirteenflames.client.render.item.*;
import com.qurenie.relics_thirteenflames.client.render.item.extension.CustomExtensionRenderer;
import com.qurenie.relics_thirteenflames.client.render.item.extension.RonasShieldExtension;
import com.qurenie.relics_thirteenflames.client.render.item.extension.TravellerSwordExtension;
import com.qurenie.relics_thirteenflames.client.style.ScrollOfTruthStyle;
import it.hurts.sskirillss.relics.init.RelicsRelicStyles;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber
public class ItemClientRegistry {
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerClient(RegisterClientExtensionsEvent event) {
        event.registerItem(new CustomExtensionRenderer(ScrollOfTruthItemRenderer::new), ItemsRegistry.SCROLL_OF_TRUTH);
        event.registerItem(new CustomExtensionRenderer(KnefBowItemRenderer::new), ItemsRegistry.KNEF_BOW);
        event.registerItem(new CustomExtensionRenderer(KnefRoseItemRenderer::new), ItemsRegistry.KNEF_ROSE);
        event.registerItem(new RonasShieldExtension(RonasShieldItemRenderer::new) , ItemsRegistry.RONAS_SHIELD);
        event.registerItem(new CustomExtensionRenderer(EmissiveItemRenderer::new) , ItemsRegistry.RONAS_SWORD);
        event.registerItem(new CustomExtensionRenderer(EmissiveItemRenderer::new) , ItemsRegistry.HETT_FEATHER);
        event.registerItem(new CustomExtensionRenderer(SeliasetHornItemRenderer::new), ItemsRegistry.SELIASET_HORN);
        event.registerItem(new CustomExtensionRenderer(SeliasetSunItemRenderer::new), ItemsRegistry.SELIASET_SUN);
        event.registerItem(new CustomExtensionRenderer(JodahStaffItemRenderer::new), ItemsRegistry.JODAH_STAFF);
        event.registerItem(new TravellerSwordExtension(TravellerItemRenderer::new), ItemsRegistry.TRAVELLER_SWORD);
    }

    @SubscribeEvent
    public static void registerStyles(FMLClientSetupEvent event) {
        RelicsRelicStyles.register(ItemsRegistry.KNEF_BOW, RelicStyleStructured.builder()::build);
        RelicsRelicStyles.register(ItemsRegistry.KNEF_ROSE, RelicStyleStructured.builder()::build);
        RelicsRelicStyles.register(ItemsRegistry.RONAS_SHIELD, RelicStyleStructured.builder()::build);
        RelicsRelicStyles.register(ItemsRegistry.RONAS_SWORD, RelicStyleStructured.builder()::build);
        RelicsRelicStyles.register(ItemsRegistry.TRAVELLER_SWORD, RelicStyleStructured.builder()::build);
        RelicsRelicStyles.register(ItemsRegistry.JODAH_MASK, RelicStyleStructured.builder()::build);
        RelicsRelicStyles.register(ItemsRegistry.JODAH_STAFF, RelicStyleStructured.builder()::build);
        RelicsRelicStyles.register(ItemsRegistry.HETT_FEATHER, RelicStyleStructured.builder()::build);
        RelicsRelicStyles.register(ItemsRegistry.MONTU_GLOVES, RelicStyleStructured.builder()::build);
        RelicsRelicStyles.register(ItemsRegistry.MONTU_HAMMER, RelicStyleStructured.builder()::build);
        RelicsRelicStyles.register(ItemsRegistry.SELIASET_HORN, RelicStyleStructured.builder()::build);
        RelicsRelicStyles.register(ItemsRegistry.SELIASET_SUN, RelicStyleStructured.builder()::build);
        RelicsRelicStyles.register(ItemsRegistry.SCROLL_OF_TRUTH, ScrollOfTruthStyle::new);
        RelicsRelicStyles.init();
    }
    
}
