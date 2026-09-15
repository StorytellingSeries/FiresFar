package com.qurenie.relics_thirteenflames.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.qurenie.api.IBarContainer;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.client.bar.BarDecorator;
import com.qurenie.relics_thirteenflames.client.hand.GlovesHandRenderFactory;
import com.qurenie.relics_thirteenflames.client.hand.RenderableHandRegistry;
import com.qurenie.relics_thirteenflames.client.render.entity.*;
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
import com.qurenie.relics_thirteenflames.content.items.misc.JodahTier;
import com.qurenie.relics_thirteenflames.content.items.misc.MaskState;
import com.qurenie.relics_thirteenflames.content.items.misc.ScintType;
import com.qurenie.relics_thirteenflames.content.items.models.InterworlderMask;
import com.qurenie.relics_thirteenflames.content.items.models.MontuGlovesArmorLeft;
import com.qurenie.relics_thirteenflames.content.items.models.MontuGlovesArmorRight;
import com.qurenie.relics_thirteenflames.init.*;
import com.qurenie.relics_thirteenflames.init.register.RendererFactory;
import it.hurts.sskirillss.relics.client.renderer.entities.NullRenderer;
import net.minecraft.Util;
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
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammeranims.api.geometry.IGeometryContainer;
import org.zeith.hammeranims.api.tile.IAnimatedEntity;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import static com.qurenie.relics_thirteenflames.init.ItemsRegistry.JODAH_MASK;
import static com.qurenie.relics_thirteenflames.init.ItemsRegistry.JODAH_STAFF;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = ThirteenFlames.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void fmlClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(JODAH_MASK, ThirteenFlames.rl("mask_state"),
                    (stack, level, entity, seed) -> stack.getOrDefault(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL).ordinal());
            ItemProperties.register(JODAH_STAFF, ThirteenFlames.rl("active"),
                    (stack, level, entity, seed) -> stack.getOrDefault(ComponentRegistry.ACTIVE_TICK, 0) > 0 ? 1 : 0);
            ItemProperties.register(JODAH_STAFF, ThirteenFlames.rl("rank"),
                    (stack, level, entity, seed) -> JodahTier.values().length - 1 - stack.getOrDefault(ComponentRegistry.JODAH_TIER, JodahTier.D ).ordinal());
        });
    }

    @SubscribeEvent
    public static void menuSetup(RegisterMenuScreensEvent event) {
        event.register(MenuRegistry.SCROLL_OF_TRUTH_MENU.get(), ScrollOfTruthContainerScreen::new);
        event.register(MenuRegistry.MONTU_SMITH_MENU.get(), MontuGlovesScreen::new);
        event.register(MenuRegistry.AURITEKH_BEACON_MENU.get(), BeaconScreen::new);
        event.register(MenuRegistry.MONTU_COMPOSIT_MENU.get(), MontuCompositeScreen::new);
        CuriosRendererRegistry.register(ItemsRegistry.MONTU_GLOVES, MontuGlovesRenderer::new);
        RenderableHandRegistry.register(ItemsRegistry.MONTU_GLOVES, GlovesHandRenderFactory.INSTANCE, GlovesHandRenderFactory.INSTANCE);
    }

    @SubscribeEvent
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        for (int rank = 0; rank <= 3; rank++) {
            event.register(ModelResourceLocation.standalone(
                    ThirteenFlames.rl(String.format("item/jodah_staff_%d_flawless", rank))));
            event.register(ModelResourceLocation.standalone(
                    ThirteenFlames.rl(String.format("item/jodah_staff_%d_active_flawless", rank))));
        }
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
        e.registerEntityRenderer(EntityRegistry.WAVE, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.WITHER_PROJ, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.SHADOW_MASS, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.SOUL_ORB, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.BOOK_ORB, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.MOB_CARRIER, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.POISONWAVE, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.FEATHER_VORTEX_ENTITY, NullRenderer::new);
        e.registerEntityRenderer(EntityRegistry.AIR_VORTEX_ENTITY, NullRenderer:: new);
        e.registerEntityRenderer(EntityRegistry.MONTU_DRILL_ENTITY, NullRenderer::new);
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
        e.registerEntityRenderer(EntityRegistry.JODAH_MARK, NullRenderer::new);
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
        e.registerEntityRenderer(EntityRegistry.SMALL_GHOST,
                rendererProvider(
                        EntityModels.GHOST_SMALL,
                        SmallGhostRenderer::new,
                        ThirteenFlames.rl("textures/entity/small_ghost.png"),
                        1f
                )
        );
        e.registerEntityRenderer(EntityRegistry.BIG_GHOST,
                rendererProvider(
                        EntityModels.GHOST_BIG,
                        BigGhostRenderer::new,
                        ThirteenFlames.rl("textures/entity/big_ghost.png"),
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
                                return entity.getSkintType() == ScintType.SKINT
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
    public static void registerOverlays(RegisterGuiLayersEvent event) {
        event.registerBelow(ResourceLocation.fromNamespaceAndPath("minecraft", "title"),
                ResourceLocation.fromNamespaceAndPath(ThirteenFlames.MODID, "poison_overlay"), new PoisonOverlay());
        event.registerAboveAll(ThirteenFlames.rl("jodah_mask_overlay"), new MaskOverlay());
        event.registerAboveAll(ThirteenFlames.rl("traveller_sword_overlay"), new TravellerOverlay());
        event.registerAboveAll(ThirteenFlames.rl("meteor_overlay"), new MeteorOverlay());
        event.registerAboveAll(ThirteenFlames.rl("jodah_shield_overlay"), new JodahShieldOverlay());
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions context) {
        context.registerLayerDefinition(MontuGlovesArmorLeft.LAYER_LOCATION, MontuGlovesArmorLeft::createBodyLayer);
        context.registerLayerDefinition(MontuGlovesArmorLeft.Flawless.LAYER_LOCATION, MontuGlovesArmorLeft.Flawless::createBodyLayer);
        context.registerLayerDefinition(MontuGlovesArmorRight.LAYER_LOCATION, MontuGlovesArmorRight::createBodyLayer);
        context.registerLayerDefinition(MontuGlovesArmorRight.Flawless.LAYER_LOCATION, MontuGlovesArmorRight.Flawless::createBodyLayer);
        context.registerLayerDefinition(InterworlderMask.LAYER_LOCATION, InterworlderMask::createBodyLayer);
        context.registerLayerDefinition(InterworlderMask.Flawless.LAYER_LOCATION, InterworlderMask.Flawless::createBodyLayer);
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

    public static class MeteorOverlay implements LayeredDraw.Layer {

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker tracker) {

            Minecraft MC = Minecraft.getInstance();
            LocalPlayer player = MC.player;

            ItemJodahMask.isMeteorTargetActive &= player != null;

            if (player == null || player.isSpectator() || MC.options.hideGui)
                return;

            if (!ItemJodahMask.isMeteorTargetActive)
                return;

            EntityHitResult entityResult = ProjectileUtil.getEntityHitResult(
                    player.level(),
                    player,
                    player.getEyePosition(),
                    player.getEyePosition().add(player.getLookAngle().scale(140)),
                    player.getBoundingBox().inflate(2).expandTowards(player.getLookAngle().scale(140)),
                    entity -> !entity.isSpectator() && entity.isPickable()
                            && entity instanceof LivingEntity living && living.isAlive(),
                    0.7f
            );

            if (entityResult == null) {
                return;
            }

            var texture = ResourceLocation.fromNamespaceAndPath("relics_thirteenflames", "textures/hud/jodah_mask/meteor_crosshair.png");
            RenderSystem.setShaderColor(1F, 1F, 1F, 1.0F);
            RenderSystem.setShaderTexture(0, texture);

            int x = (int) Math.ceil(guiGraphics.guiWidth() / 2f);
            int y = (int) Math.ceil(guiGraphics.guiHeight() / 2f);

            guiGraphics.blit(texture, x - 8, y - 8, 15, 15, 0F, 0.0F, 15, 15, 15, 15);
        }
    }

    public static class TravellerOverlay implements LayeredDraw.Layer {

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker tracker) {

            Minecraft MC = Minecraft.getInstance();
            LocalPlayer player = MC.player;

            if (player == null || player.isSpectator() || MC.options.hideGui)
                return;

            ItemStack sword = player.getMainHandItem();
            if (!sword.is(ItemsRegistry.TRAVELLER_SWORD))
                sword = player.getMainHandItem();
            if (!sword.is(ItemsRegistry.TRAVELLER_SWORD))
                return;

            boolean isActive = ItemsRegistry.TRAVELLER_SWORD.isSprintSweepReady(sword);
            if (!isActive)
                return;

            var texture = ResourceLocation.fromNamespaceAndPath("relics_thirteenflames", "textures/hud/traveller_sword/adventurer_crosshair.png");
            RenderSystem.setShaderColor(1F, 1F, 1F, 1.0F);
            RenderSystem.setShaderTexture(0, texture);

            int x = (int) Math.ceil(guiGraphics.guiWidth() / 2f);
            int y = (int) Math.ceil(guiGraphics.guiHeight() / 2f);

            guiGraphics.blit(texture, x - 8, y - 8, 15, 15, 0F, 0.0F, 15, 15, 15, 15);


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

    public static class JodahShieldOverlay implements LayeredDraw.Layer {

        public static final ResourceLocation FULL_HEART = ThirteenFlames.rl("textures/hud/jodah_staff/shield_heart_full.png");
        public static final ResourceLocation HALF_HEART = ThirteenFlames.rl("textures/hud/jodah_staff/shield_heart_half.png");
        public static final ResourceLocation FULL_HEART_MINI = ThirteenFlames.rl("textures/hud/jodah_staff/shield_heart_mini_full.png");
        public static final ResourceLocation HALF_HEART_MINI = ThirteenFlames.rl("textures/hud/jodah_staff/shield_heart_mini_half.png");
        public static final ResourceLocation FULL_HEART_BLINK = ThirteenFlames.rl("textures/hud/jodah_staff/shield_heart_full_blink.png");
        public static final ResourceLocation HALF_HEART_BLINK = ThirteenFlames.rl("textures/hud/jodah_staff/shield_heart_half_blink.png");
        public static final ResourceLocation FULL_HEART_MINI_BLINK = ThirteenFlames.rl("textures/hud/jodah_staff/shield_heart_mini_full_blink.png");
        public static final ResourceLocation HALF_HEART_MINI_BLINK = ThirteenFlames.rl("textures/hud/jodah_staff/shield_heart_mini_half_blink.png");

        int healthBlinkTime;
        long lastHealthTime;
        int lastHealth;
        int displayHealth;

        private final RandomSource random = RandomSource.create();

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker deltaTracker) {
            var minecraft = Minecraft.getInstance();
            if (!minecraft.gameMode.canHurtPlayer() || minecraft.options.hideGui)
                return;

            var gui = minecraft.gui;
            Player player = minecraft.getCameraEntity() instanceof Player p ? p : null;
            var tickCount = gui.getGuiTicks();

            if (player != null) {

                int health = (int) (float) player.getData(AttachmentsRegistry.JODAH_SHEILD);
                boolean flag = this.healthBlinkTime > tickCount && (this.healthBlinkTime - tickCount) / 3L % 2L == 1L;
                long j = Util.getMillis();
                if (health < this.lastHealth && player.invulnerableTime > 0) {
                    this.lastHealthTime = j;
                    this.healthBlinkTime = tickCount + 20;
                } else if (health > this.lastHealth && player.invulnerableTime > 0) {
                    this.lastHealthTime = j;
                    this.healthBlinkTime = tickCount + 10;
                }

                if (j - this.lastHealthTime > 1000L) {
                    this.lastHealth = health;
                    this.displayHealth = health;
                    this.lastHealthTime = j;
                }

                this.lastHealth = health;
                this.random.setSeed(((long) tickCount) * 312871);
                int x = guiGraphics.guiWidth() / 2 - 91;
                int y = guiGraphics.guiHeight() - 39;
                float f = Math.max(displayHealth, health);
                int l1 = Mth.ceil(f / 20.0F);
                int height = Math.max(10 - (l1 - 2), 3);
                float max = Math.max((float) player.getAttributeValue(Attributes.MAX_HEALTH), player.getHealth());
                this.renderHearts(guiGraphics, x, y, height, health, displayHealth, max, flag);
            }
        }

        private void renderHearts(
                GuiGraphics guiGraphics,
                int x,
                int y,
                int height,
                int currentHealth,
                int displayHealth,
                float maxHealth,
                boolean renderHighlight
        ) {
            int remainsDisplay = displayHealth;
            int remainsCurrent = currentHealth;

            int max = Mth.ceil((double) maxHealth / 2.0);

            for (int iter = 0; iter < Math.ceil(Math.max(currentHealth, displayHealth)) / max; iter++) {
                for (int l = max - 1; l >= 0; l--) {
                    int rows = l / 10;
                    int lastrow = l % 10;
                    int lastHeart = x + lastrow * 8;
                    int lastRow = y - rows * height;
//                    if (remainsCurrent <= 4) {
//                        lastRow += this.random.nextInt(2);
//                    }

                    int i2 = l * 2;

                    if (renderHighlight && i2 < remainsDisplay && remainsDisplay <= maxHealth) {
                        boolean flag3 = i2 + 1 == remainsDisplay;
                        this.renderHeart(guiGraphics, lastHeart, lastRow, iter == 0, flag3, true);
                    }

                    if (i2 < remainsCurrent) {
                        boolean flag4 = i2 + 1 == remainsCurrent;
                        this.renderHeart(guiGraphics, lastHeart, lastRow, iter == 0, flag4, false);
                    }
                }

                remainsDisplay -= (int) maxHealth;
                remainsCurrent -= (int) maxHealth;
            }
        }

        private void renderHeart(
                GuiGraphics guiGraphics, int x, int y, boolean small, boolean halfHeart, boolean blinking
        ) {
            RenderSystem.enableBlend();
            guiGraphics.blit(getSprite(small, blinking, halfHeart), x, y, 0, 0, 9, 9, 9, 9);
            RenderSystem.disableBlend();
        }

        private ResourceLocation getSprite(boolean small, boolean blinking, boolean halfHeart) {
            int i = (small ? 1 : 0) + (blinking ? 2 : 0) + (halfHeart ? 4 : 0);
            return switch (i) {
                case 0 -> FULL_HEART;
                case 1 -> FULL_HEART_MINI;
                case 2 -> FULL_HEART_BLINK;
                case 3 -> FULL_HEART_MINI_BLINK;
                case 4 -> HALF_HEART;
                case 5 -> HALF_HEART_MINI;
                case 6 -> HALF_HEART_BLINK;
                default -> HALF_HEART_MINI_BLINK;
            };
        }

    }

}