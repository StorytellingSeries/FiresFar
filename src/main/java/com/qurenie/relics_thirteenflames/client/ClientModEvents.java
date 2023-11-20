package com.qurenie.relics_thirteenflames.client;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.entities.KnefProjectile;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import it.hurts.sskirillss.relics.client.renderer.entities.NullRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;


@Mod.EventBusSubscriber(modid = ThirteenFlames.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {


    @SubscribeEvent
    public static void fmlclientsetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {

            ItemProperties.register(ItemsRegistry.KNEF_BOW, new ResourceLocation("relics_thirteenflames", "pull"), (stack, world, living, a) -> {

                if (living != null && living.isUsingItem()) {
                    //ScriptUtils.sendMessageToPlayers(String.valueOf(living.getUseItem() != stack ? 0.0F : (float)(stack.getUseDuration() - living.getUseItemRemainingTicks()) / 20.0F));
                    return living.getUseItem() != stack ? 0.0F : (float) (stack.getUseDuration() - living.getUseItemRemainingTicks()) / 20.0F;
                } else {
                    return 0.0f;
                }
            });
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(EntityRegistry.KNEF_PROJECTILE_CARRIER, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.KNEF_PROJECTILE, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.KNEF_STORMCALLER, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.KNEF_STORM, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.KNEF_RAINDROP, NullRenderer::new);
    }
}