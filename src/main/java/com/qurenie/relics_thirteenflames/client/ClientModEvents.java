package com.qurenie.relics_thirteenflames.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.client.particles.CircleTintFactory;
import com.qurenie.relics_thirteenflames.client.render.entity.CommonRenderer;
import com.qurenie.relics_thirteenflames.client.render.entity.FallingRenderer;
import com.qurenie.relics_thirteenflames.client.render.entity.LivingFleshRenderer;
import com.qurenie.relics_thirteenflames.content.items.ItemRonasSword;
import com.qurenie.relics_thirteenflames.client.render.entity.EntityRendererSeliasetSun;
import com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.ScrollOfTruthInit;
import com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.screen.ScrollOfTruthContainerScreen;
import com.qurenie.relics_thirteenflames.init.*;
import com.qurenie.relics_thirteenflames.init.register.RendererFactory;
import it.hurts.sskirillss.relics.client.renderer.entities.NullRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.zeith.hammeranims.api.geometry.IGeometryContainer;
import org.zeith.hammeranims.api.tile.IAnimatedEntity;


@Mod.EventBusSubscriber(modid = ThirteenFlames.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {


    @SubscribeEvent
    public static void fmlclientsetup(FMLClientSetupEvent event) {
        MenuScreens.register(ScrollOfTruthInit.SCROLL_OF_TRUTH_MENU.get(), ScrollOfTruthContainerScreen::new);
        event.enqueueWork(() -> {

            ItemProperties.register(ItemsRegistry.KNEF_BOW, new ResourceLocation("relics_thirteenflames", "pull"), (stack, world, living, a) -> {

                if (living != null && living.isUsingItem() && living.getUseItem() == stack) {
                    return (float) (stack.getUseDuration() - living.getUseItemRemainingTicks()) / 20.0F;
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
        e.registerEntityRenderer(EntityRegistry.DEATHCLOUD, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.POISONWAVE, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.USABLE_FALLING, FallingRenderer::new);
        e.registerEntityRenderer(EntityRegistry.SELIASET_SUN, EntityRendererSeliasetSun::new);
        e.registerEntityRenderer(EntityRegistry.LIVING_FLESH,
                rendererProvider(
                        EntityModels.LIVING_FLESH,
                        LivingFleshRenderer::new,
                        ThirteenFlames.rl("textures/entity/living_flesh.png"),
                        1f
                )
        );
    }

    public static <T extends LivingEntity & IAnimatedEntity, R extends CommonRenderer<T>> EntityRendererProvider<T> rendererProvider(IGeometryContainer model, RendererFactory.WithScale<T, R> renderer, ResourceLocation texture, float scale) {
        return manager -> renderer.create(manager, new RendererFactory.ModelConfiguration(model), 0.5F, texture, scale);
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerBelow(new ResourceLocation("minecraft", "title_text"), "poison_overlay", new PoisonOverlay());
    }

    public static class PoisonOverlay implements IGuiOverlay {

        @Override
        public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {

            Minecraft MC = Minecraft.getInstance();
            LocalPlayer player = MC.player;
            Entity looked = MC.crosshairPickEntity;

            if (player != null && player.getMainHandItem().getItem() instanceof ItemRonasSword relic && looked instanceof LivingEntity livin) {
                int stacks = livin.hasEffect(EffectsRegistry.POISSON) ? livin.getEffect(EffectsRegistry.POISSON).getAmplifier() + 1 : 0;
                TextureManager manager = MC.getTextureManager();
                int scale = 2;
                ResourceLocation textureEmpty, textureFull;
                byte width;
                byte height;
                int x;
                int y;



                textureEmpty = new ResourceLocation("relics_thirteenflames", "textures/hud/ronas_sword/acid_drops_empty.png");
                RenderSystem.setShaderColor(0.5F, 0.8F, 0.5F, 1.0F);
                RenderSystem.setShaderTexture(0, textureEmpty);
                RenderSystem.enableBlend();
                guiGraphics.pose().pushPose();
                width = 72;
                height = 16;

                int maxStacks = (int) relic.getAbilityValue(player.getMainHandItem(), "spit", "maxstacks");

                x = screenWidth / 2 - (width - 12 * (6 - maxStacks)) / 2 / scale;
                y = screenHeight / 2 + 20;
                manager.bindForSetup(textureEmpty);

                int croppedWidth = width - 12 * (6 - maxStacks);

                guiGraphics.blit(textureEmpty, x, y, croppedWidth / scale, height / scale, 0F, 0.0F, croppedWidth, height, width, height);
                guiGraphics.pose().popPose();
                RenderSystem.disableBlend();

                int dropWidth = width / 6;
                textureFull = new ResourceLocation("relics_thirteenflames", "textures/hud/ronas_sword/acid_drops.png");
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, textureFull);
                RenderSystem.enableBlend();
                guiGraphics.pose().pushPose();
                manager.bindForSetup(textureFull);

                guiGraphics.enableScissor(x, y, x + (dropWidth * stacks) / scale, y + height / scale);
                guiGraphics.blit(textureFull, x, y, croppedWidth / scale, height / scale, 0F, 0.0F, croppedWidth, height, width, height);
                guiGraphics.pose().popPose();
                RenderSystem.disableBlend();
                guiGraphics.disableScissor();

            }

        }

    }
}