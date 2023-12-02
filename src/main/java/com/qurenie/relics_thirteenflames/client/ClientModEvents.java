package com.qurenie.relics_thirteenflames.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.entities.KnefProjectile;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import it.hurts.sskirillss.relics.client.renderer.entities.NullRenderer;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.zeith.hammerlib.net.Network;
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
        e.registerEntityRenderer(EntityRegistry.KNEF_PROJECTILE_SPECIAL, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.KNEF_STORMCALLER, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.KNEF_DISCHARGE, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.KNEF_STORM, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.KNEF_RAINDROP, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.FARTCLOUD, NullRenderer::new);
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerBelow(new ResourceLocation("minecraft", "title_text"), "poison_overlay", new PoisonOverlay());
    }

    public static class PoisonOverlay implements IGuiOverlay {

        @Override
        public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {

            Minecraft MC = Minecraft.getInstance();
            LocalPlayer player = MC.player;
            Entity looked = MC.crosshairPickEntity;
            /*
            if (player != null && player.getMainHandItem().is(ItemsRegistry.RONAS_SWORD) && looked instanceof LivingEntity livin) {
                int stacks = livin.hasEffect(EffectsRegistry.POISSON) ? livin.getEffect(EffectsRegistry.POISSON).getAmplifier() + 1 : 0;
                TextureManager manager = MC.getTextureManager();
                int scale = 2;
                ResourceLocation textureEmpty, textureFull;
                byte width;
                byte height;
                int x;
                int y;



                textureEmpty = new ResourceLocation("relics_thirteenflames", "textures/hud/acid_drops_empty.png");
                RenderSystem.setShaderColor(0.5F, 0.8F, 0.5F, 1.0F);
                RenderSystem.setShaderTexture(0, textureEmpty);
                RenderSystem.enableBlend();
                poseStack.pushPose();
                width = 81;
                height = 18;



                x = screenWidth / 2 - width / 2 / scale;
                y = screenHeight / 2 + 20;
                manager.bindForSetup(textureEmpty);

                Gui.blit(poseStack, x, y, width / scale, height / scale, 0.0F, 0.0F, width, height, width, height);
                poseStack.popPose();
                RenderSystem.disableBlend();

                int dropWidth = width / 6;
                textureFull = new ResourceLocation("relics_thirteenflames", "textures/hud/acid_drops.png");
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, textureFull);
                RenderSystem.enableBlend();
                poseStack.pushPose();
                x = screenWidth / 2 - width / 2 / scale;
                y = screenHeight / 2 + 20;
                manager.bindForSetup(textureFull);
                Gui.enableScissor(x, y, x + (dropWidth * stacks) / scale, y + height / scale);
                //Gui.fill(poseStack, x, y, );
                Gui.blit(poseStack, x, y, width / scale, height / scale, 0.0F, 0.0F, width, height, width, height);
                poseStack.popPose();
                RenderSystem.disableBlend();
                Gui.disableScissor();

            }
            */
        }

    }
}