package com.qurenie.relics_thirteenflames.client.render.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.client.AnimationsRegistry;
import com.qurenie.relics_thirteenflames.client.particles.FeatherParticle;
import com.qurenie.relics_thirteenflames.client.render.entity.SimpleBedrockModel;
import com.qurenie.relics_thirteenflames.init.EntityModels;
import com.qurenie.relics_thirteenflames.init.register.RendererFactory;
import com.qurenie.relics_thirteenflames.util.EntityAnimatedWrapper;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.NotNull;

import static com.qurenie.relics_thirteenflames.content.entities.AnimatedEntity.LAYER_ACTION;
import static com.qurenie.relics_thirteenflames.init.AttachmentsRegistry.WINGS_LAYER_DATA;

public class JodahWingsLayer <T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    
    private final SimpleBedrockModel<?> wingsLayer;
    private EntityAnimatedWrapper livingWrapper;
    public static final int ANIMATION_LENGTH = 180;
    private static final ResourceLocation TEXTURE = ThirteenFlames.rl("textures/entity/jodah_wings.png");
    
    public JodahWingsLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
        this.wingsLayer = new SimpleBedrockModel<>(RendererFactory.ModelConfiguration.builder()
                .model(EntityModels.JODAH_WINGS)
                .build());

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void clientTick(PlayerTickEvent.Pre event) {
        var player = event.getEntity();

        int activeTicks = player.hasData(WINGS_LAYER_DATA) ? player.getData(WINGS_LAYER_DATA) : 0;
        if (activeTicks == 0 || !player.level().isClientSide)
            return;

        if (livingWrapper == null || livingWrapper.getEntity() != player)
            livingWrapper = new EntityAnimatedWrapper(player);

        this.getParentModel().copyPropertiesTo((EntityModel<T>) wingsLayer);

        if (activeTicks == ANIMATION_LENGTH) {
//            livingWrapper.getAnimationSystem().stopAnimation(LAYER_ACTION, 0);
            livingWrapper.getAnimationSystem().startAnimationAt(LAYER_ACTION, AnimationsRegistry.JODAH_WINGS_OPEN.configure().important().speed(0.8f));
//            livingWrapper.getAnimationSystem().sync();
        } else if (activeTicks == 50) {
//            livingWrapper.getAnimationSystem().stopAnimation(LAYER_ACTION, 0);
            livingWrapper.getAnimationSystem().startAnimationAt(LAYER_ACTION, AnimationsRegistry.JODAH_WINGS_OPEN
                    .configure().reversed().important().speed(0.8f).startTime(1.2f).transitionTime(0.4f));
        } else if (activeTicks == ANIMATION_LENGTH - 27) {
            Vec3[] corners = getWingCorners(player);
            double rad = Math.toRadians(player.yBodyRot);
            Vec3 right = new Vec3(Math.cos(rad), 0, Math.sin(rad)).normalize().scale(0.2f);
            Vec3 left = right.scale(-1).normalize().scale(0.2f);
            var random = player.level().random;

            ParticleHelper.spawnParticleTriangle(player.level(), new FeatherParticle.Options(0.3f, 50),
                    corners[2], corners[2].add(left.normalize().scale(-1)).add(0, 0.7, 0), corners[0].add(0, 0.6, 0), 3, () -> right.add(random.nextGaussian() * 0.07, random.nextGaussian() * 0.1, random.nextGaussian() * 0.07).normalize().scale(0.15));
            ParticleHelper.spawnParticleTriangle(player.level(), new FeatherParticle.Options(0.3f, 50),
                    corners[3],  corners[3].add(right.normalize().scale(-1)).add(0, 0.7, 0), corners[1].add(0, 0.6, 0), 3, () -> left.add(random.nextGaussian() * 0.07, random.nextGaussian() * 0.1, random.nextGaussian() * 0.07).normalize().scale(0.15));
        } else if (activeTicks == ANIMATION_LENGTH - 50) {
            Vec3[] corners = getWingCorners(player);
            double rad = Math.toRadians(player.yBodyRot);
            Vec3 right = new Vec3(Math.cos(rad), 0, Math.sin(rad)).normalize().scale(0.2f);
            Vec3 left = right.scale(-1).normalize().scale(0.2f);
            var random = player.level().random;

            ParticleHelper.spawnParticleTriangle(player.level(), new FeatherParticle.Options(0.3f, 125),
                    corners[2], corners[2].add(left.normalize().scale(-1)).add(0, 1.4, 0), corners[0].add(0, 0.6, 0), 4, () -> right.add(random.nextGaussian() * 0.3, random.nextGaussian() * 0.14 + 0.5, random.nextGaussian() * 0.3).normalize().scale(0.15));
            ParticleHelper.spawnParticleTriangle(player.level(), new FeatherParticle.Options(0.3f, 125),
                    corners[3],  corners[3].add(right.normalize().scale(-1)).add(0, 1.4, 0), corners[1].add(0, 0.6, 0), 4, () -> left.add(random.nextGaussian() * 0.3, random.nextGaussian() * 0.14 + 0.5, random.nextGaussian() * 0.3).normalize().scale(0.15));
        }

    }
    
    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight,
                       @NotNull T livingEntity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        int activeTicks = livingEntity.hasData(WINGS_LAYER_DATA) ? livingEntity.getData(WINGS_LAYER_DATA) : 0;
        if (activeTicks == 0)
            return;
        poseStack.pushPose();
        
        poseStack.scale(1, -1, 1);
        poseStack.translate(0.0F, -1.5f, 0F);
        if (getParentModel() instanceof HumanoidModel<?> humanoidModel) {
            ModelPart body = humanoidModel.body;
            body.translateAndRotate(poseStack);
        } else {
            poseStack.translate(0.0F, livingEntity.getBbHeight() * 0.5F, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(-livingEntity.yBodyRot));
        }

        wingsLayer.applyAnimations(livingWrapper.getAnimationSystem(), partialTick);
        wingsLayer.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF
        );
        
        poseStack.popPose();
    }
    
    public static Vec3[] getWingCorners(LivingEntity living) {
        // Поворот тела в радианах
        float bodyYaw = living.yBodyRot;
        double rad = Math.toRadians(bodyYaw);
        
        // Векторы относительно тела
        Vec3 back = new Vec3(-Math.sin(rad), 0, Math.cos(rad)).normalize(); // "назад" от тела
        Vec3 right = new Vec3(Math.cos(rad), 0, Math.sin(rad)).normalize(); // "вправо" от тела
        Vec3 up = new Vec3(0, 1, 0);
        
        // Размеры крыльев
        double halfWidth = 1.5;
        double halfHeight = 0.4;
        double backOffset = -0.35;
        
        // Центр спины (на уровне плеч)
        Vec3 base = living.position().add(0, living.getBbHeight() * 0.8, 0);
        Vec3 center = base.add(back.scale(backOffset)); // немного позади спины
        
        // Смещения
        Vec3 upVec = up.scale(halfHeight);
        Vec3 rightVec = right.scale(halfWidth);
        
        // Углы (по часовой стрелке)
        Vec3 topLeft = center.add(upVec).subtract(rightVec);
        Vec3 topRight = center.add(upVec).add(rightVec);
        Vec3 bottomRight = center.subtract(upVec).add(rightVec);
        Vec3 bottomLeft = center.subtract(upVec).subtract(rightVec);
        
        return new Vec3[] { topLeft, topRight, bottomRight, bottomLeft };
    }
    
}
