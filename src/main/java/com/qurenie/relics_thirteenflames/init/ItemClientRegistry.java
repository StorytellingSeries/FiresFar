package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.client.render.item.*;
import com.qurenie.relics_thirteenflames.client.render.item.extension.CustomExtensionRenderer;
import com.qurenie.relics_thirteenflames.client.render.item.extension.RonasShieldExtension;
import com.qurenie.relics_thirteenflames.client.render.item.extension.TravellerSwordExtension;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
    
}
