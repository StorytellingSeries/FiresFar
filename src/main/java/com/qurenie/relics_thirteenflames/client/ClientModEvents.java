package com.qurenie.relics_thirteenflames.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.qurenie.api.IBarContainer;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.client.bar.BarDecorator;
import com.qurenie.relics_thirteenflames.client.render.entity.CommonRenderer;
import com.qurenie.relics_thirteenflames.client.render.entity.EntityRendererSeliasetSun;
import com.qurenie.relics_thirteenflames.client.render.entity.FallingRenderer;
import com.qurenie.relics_thirteenflames.client.render.entity.LivingFleshRenderer;
import com.qurenie.relics_thirteenflames.client.render.item.MontuGlovesRenderer;
import com.qurenie.relics_thirteenflames.client.render.misc.AuritekhElytraLayer;
import com.qurenie.relics_thirteenflames.client.render.misc.JodahMaskLayer;
import com.qurenie.relics_thirteenflames.client.render.misc.JodahWingsLayer;
import com.qurenie.relics_thirteenflames.client.screen.gloves.MontuCompositeScreen;
import com.qurenie.relics_thirteenflames.client.screen.gloves.MontuGlovesScreen;
import com.qurenie.relics_thirteenflames.client.screen.scroll.ScrollOfTruthContainerScreen;
import com.qurenie.relics_thirteenflames.content.entities.*;
import com.qurenie.relics_thirteenflames.content.items.ItemJodahMask;
import com.qurenie.relics_thirteenflames.content.items.ItemRonasSword;
import com.qurenie.relics_thirteenflames.content.items.misc.MaskState;
import com.qurenie.relics_thirteenflames.content.items.models.InterworlderMask;
import com.qurenie.relics_thirteenflames.content.items.models.MontuGlovesArmorLeft;
import com.qurenie.relics_thirteenflames.content.items.models.MontuGlovesArmorRight;
import com.qurenie.relics_thirteenflames.init.*;
import com.qurenie.relics_thirteenflames.init.register.RendererFactory;
import it.hurts.sskirillss.relics.client.renderer.entities.NullRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammeranims.api.geometry.IGeometryContainer;
import org.zeith.hammeranims.api.tile.IAnimatedEntity;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import java.util.Map;

import static com.qurenie.relics_thirteenflames.init.ItemsRegistry.JODAH_MASK;


@EventBusSubscriber(modid = ThirteenFlames.MODID, value = Dist.CLIENT)
public class ClientModEvents {
    
    @SubscribeEvent
    public static void fmlClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(JODAH_MASK, ThirteenFlames.rl("mask_state"),
                (stack, level, entity, seed) -> stack.getOrDefault(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL).ordinal()));
    }
    
    @SubscribeEvent
    public static void menuSetup(RegisterMenuScreensEvent event) {
        event.register(MenuRegistry.SCROLL_OF_TRUTH_MENU.get(), ScrollOfTruthContainerScreen::new);
        event.register(MenuRegistry.MONTU_SMITH_MENU.get(), MontuGlovesScreen::new);
        event.register(MenuRegistry.AURITEKH_BEACON_MENU.get(), BeaconScreen::new);
        event.register(MenuRegistry.MONTU_COMPOSIT_MENU.get(), MontuCompositeScreen::new);
        CuriosRendererRegistry.register(ItemsRegistry.MONTU_GLOVES, MontuGlovesRenderer::new);
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
        e.registerEntityRenderer(EntityRegistry.FEATHER_VORTEX_ENTITY, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.USABLE_FALLING, FallingRenderer::new);
        e.registerEntityRenderer(EntityRegistry.SELIASET_SUN, EntityRendererSeliasetSun::new);
        e.registerEntityRenderer(EntityRegistry.TRAVELLER_SWEEP, rendererProvider(
                EntityModels.ADVENTURER_SWORD_FLIPPED,
                (p1, p2, p3, p4) -> new CommonRenderer<TravellerSweepEntity>(p1, p2, p3, p4) {
                    @Override
                    protected @NotNull RenderType getRenderType(@NotNull TravellerSweepEntity livingEntity, boolean bodyVisible, boolean translucent, boolean glowing) {
                        return RenderType.entityTranslucentEmissive(getTextureLocation(livingEntity));
                    }
                    
                    @Override
                    protected float getShadowRadius(@NotNull TravellerSweepEntity entity) {
                        return 0;
                    }
                },
                ThirteenFlames.rl("textures/entity/adventurer_sword_big_ghost.png")
        ));
        e.registerEntityRenderer(EntityRegistry.JODAH_HEAL, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.TRAVELLER_CUT, rendererProvider(
                EntityModels.ADVENTURER_SWORD,
                (p1, p2, p3, p4) -> new CommonRenderer<TravellerCutEntity>(p1, p2, p3, p4) {
                    @Override
                    protected @NotNull RenderType getRenderType(@NotNull TravellerCutEntity livingEntity, boolean bodyVisible, boolean translucent, boolean glowing) {
                        return RenderType.entityTranslucentEmissive(getTextureLocation(livingEntity));
                    }
                    
                    @Override
                    protected float getShadowRadius(@NotNull TravellerCutEntity entity) {
                        return 0;
                    }
                },
                ThirteenFlames.rl("textures/entity/adventurer_sword_big_ghost.png")
        ));
        e.registerEntityRenderer(EntityRegistry.SKINT_ORB, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.METEOR, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.TRAVELLER_AFTERDASH, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.LIVING_FLESH,
                rendererProvider(
                        EntityModels.LIVING_FLESH,
                        LivingFleshRenderer::new,
                        ThirteenFlames.rl("textures/entity/living_flesh.png"),
                        1f
                )
        );
        e.registerEntityRenderer(EntityRegistry.FEATHER_VORTEX_ENTITY,
                rendererProvider(
                        EntityModels.ATTACK_BOOK,
                        CommonRenderer::new,
                        ThirteenFlames.rl("textures/entity/attack_book.png")
                )
        );
        e.registerEntityRenderer(EntityRegistry.SKINT_CLUSTER,
                rendererProvider(
                        EntityModels.SKINT_CLUSTER,
                        (p1, p2, p3, p4) -> new CommonRenderer<SkintClusterEntity>(p1, p2, p3, p4) {
                            @Override
                            public @NotNull ResourceLocation getTextureLocation(@NotNull SkintClusterEntity entity) {
                                return entity.getSkintType() == SkintOrbEntity.Type.SKINT
                                        ? super.getTextureLocation(entity) : ThirteenFlames.rl("textures/entity/skintonit_cluster.png");
                            }
                        },
                        ThirteenFlames.rl("textures/entity/skint_cluster.png")
                )
        );
        e.registerEntityRenderer(EntityRegistry.RESPAWN_BOOK, rendererProvider(
                EntityModels.RESPAWN_BOOK,
                (p1, p2, p3, p4) -> new CommonRenderer<RespawnBookEntity>(p1, p2, p3, p4) {
                    @Override
                    public void render(RespawnBookEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
                        poseStack.pushPose();
                        poseStack.translate(0, 0.7, 0);
                        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
                        poseStack.popPose();
                    }
                },
                ThirteenFlames.rl("textures/entity/respawn_book.png")
        ));
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        KeyBindRegistry.register(event);
    }

    @SubscribeEvent
    public static void registerDecorators(RegisterItemDecorationsEvent event) {
        for (var entry : BuiltInRegistries.ITEM.entrySet())
            if (entry.getValue() instanceof IBarContainer)
                event.register(entry.getValue(), BarDecorator.INSTANCE);
    }
    
    public static <T extends LivingEntity & IAnimatedEntity, R extends CommonRenderer<T>> EntityRendererProvider<T> rendererProvider(IGeometryContainer model, RendererFactory<T, R> renderer, ResourceLocation texture) {
        return manager -> renderer.create(manager, new RendererFactory.ModelConfiguration(model), 0.5F, texture);
    }
    
    public static <T extends LivingEntity & IAnimatedEntity, R extends CommonRenderer<T>> EntityRendererProvider<T> rendererProvider(IGeometryContainer model, RendererFactory.WithScale<T, R> renderer, ResourceLocation texture, float scale) {
        return manager -> renderer.create(manager, new RendererFactory.ModelConfiguration(model), 0.5F, texture, scale);
    }
    
    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        
        
    }
    
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiLayersEvent event) {
        event.registerBelow(ResourceLocation.fromNamespaceAndPath("minecraft", "title"),
                ResourceLocation.fromNamespaceAndPath(ThirteenFlames.MODID, "poison_overlay"), new PoisonOverlay());
        event.registerAboveAll(ThirteenFlames.rl("jodah_mask_overlay"), new MaskOverlay());
    }
    
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions context) {
        context.registerLayerDefinition(MontuGlovesArmorLeft.LAYER_LOCATION, MontuGlovesArmorLeft::createBodyLayer);
        context.registerLayerDefinition(MontuGlovesArmorRight.LAYER_LOCATION, MontuGlovesArmorRight::createBodyLayer);
        context.registerLayerDefinition(InterworlderMask.LAYER_LOCATION, InterworlderMask::createBodyLayer);
    }
    
    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skinType : event.getSkins()) {
            var renderer = event.getSkin(skinType); // RenderPlayer
            if (renderer instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new JodahWingsLayer<>(playerRenderer));
                playerRenderer.addLayer(new JodahMaskLayer<>(playerRenderer));
                playerRenderer.addLayer(new AuritekhElytraLayer(playerRenderer));
            }
        }
    }
    
    public static class MaskOverlay implements LayeredDraw.Layer {
        
        @Override
        public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker tracker) {
            
            Minecraft MC = Minecraft.getInstance();
            LocalPlayer player = MC.player;
            
            if (player == null || player.isSpectator() || MC.options.hideGui)
                return;
            
            ItemStack stack = player.getItemBySlot(EquipmentSlot.HEAD);
            if (stack.getItem() instanceof ItemJodahMask) {
                MaskState state = stack.getOrDefault(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL);
                
                int screenWidth = guiGraphics.guiWidth();
                int screenHeight = guiGraphics.guiHeight();
                
                int x = screenWidth / 2 - 6;
                int y = screenHeight - (player.isCreative() ? 36 : 49);
                
                final ResourceLocation TEXTURE = ThirteenFlames.rl(String.format("textures/hud/jodah_mask/mask_icon%s.png", state.getTexturePostfix()));
                
                guiGraphics.blit(TEXTURE, x, y, 11, 11, 0, 0, 40, 40, 40, 40);
            }
            
        }
        
    }
    
    public static class PoisonOverlay implements LayeredDraw.Layer {
        
        @Override
        public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker tracker) {
            
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
                
                textureEmpty = ResourceLocation.fromNamespaceAndPath("relics_thirteenflames", "textures/hud/ronas_sword/acid_drops_empty.png");
                RenderSystem.setShaderColor(0.5F, 0.8F, 0.5F, 1.0F);
                RenderSystem.setShaderTexture(0, textureEmpty);
                RenderSystem.enableBlend();
                guiGraphics.pose().pushPose();
                width = 72;
                height = 16;
                
                int maxStacks = (int) relic.getStatValue(player, player.getMainHandItem(), "spit", "maxstacks");
                
                x = guiGraphics.guiWidth() / 2 - (width - 12 * (6 - maxStacks)) / 2 / scale;
                y = guiGraphics.guiHeight() / 2 + 20;
                manager.bindForSetup(textureEmpty);
                
                int croppedWidth = width - 12 * (6 - maxStacks);
                
                guiGraphics.blit(textureEmpty, x, y, croppedWidth / scale, height / scale, 0F, 0.0F, croppedWidth, height, width, height);
                guiGraphics.pose().popPose();
                RenderSystem.disableBlend();
                
                int dropWidth = width / 6;
                textureFull = ResourceLocation.fromNamespaceAndPath("relics_thirteenflames", "textures/hud/ronas_sword/acid_drops.png");
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