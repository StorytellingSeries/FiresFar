package com.qurenie.relics_thirteenflames.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.tiles.AurithecBeaconBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AurithecBeaconRenderer implements BlockEntityRenderer<AurithecBeaconBlockEntity> {
    
    public static final ResourceLocation BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");
    public static final int MAX_RENDER_Y = 1024;
    
    BlockRenderDispatcher renderDispatcher;
    
    public AurithecBeaconRenderer(BlockEntityRendererProvider.Context context) {
        this.renderDispatcher = context.getBlockRenderDispatcher();
    }
    
    @Override
    public void render(AurithecBeaconBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var model = renderDispatcher.getBlockModel(blockEntity.getBlockState());
        renderDispatcher.getModelRenderer().renderModel(poseStack.last(), bufferSource.getBuffer(RenderType.CUTOUT), blockEntity.getBlockState(),
                model, 1, 1, 1, packedLight, packedOverlay, ModelData.EMPTY, RenderType.CUTOUT);
        renderDispatcher.getModelRenderer().renderModel(poseStack.last(), bufferSource.getBuffer(RenderType.entityTranslucentCull(ThirteenFlames.rl("textures/block/auritekh_beacon_emissive.png"))), blockEntity.getBlockState(),
                model, 1, 1, 1, packedLight, packedOverlay, ModelData.EMPTY, RenderType.CUTOUT);
        
        long i = blockEntity.getLevel().getGameTime();
        List<AurithecBeaconBlockEntity.BeaconBeamSection> list = blockEntity.getBeamSections();
        int j = 0;
        
        for (int k = 0; k < list.size(); k++) {
            AurithecBeaconBlockEntity.BeaconBeamSection AurithecBeaconBlockEntity$beaconbeamsection = list.get(k);
            renderBeaconBeam(
                    poseStack,
                    bufferSource,
                    partialTick,
                    i,
                    j,
                    k == list.size() - 1 ? 1024 : AurithecBeaconBlockEntity$beaconbeamsection.getHeight(),
                    AurithecBeaconBlockEntity$beaconbeamsection.getColor()
            );
            j += AurithecBeaconBlockEntity$beaconbeamsection.getHeight();
        }
    }
    
    private static void renderBeaconBeam(
            PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, long gameTime, int yOffset, int height, int color
    ) {
        renderBeaconBeam(poseStack, bufferSource, BEAM_LOCATION, partialTick, 1.0F, gameTime, yOffset, height, color, 0.2F, 0.25F);
    }
    
    public static void renderBeaconBeam(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            ResourceLocation beamLocation,
            float partialTick,
            float textureScale,
            long gameTime,
            int yOffset,
            int height,
            int color,
            float beamRadius,
            float glowRadius
    ) {
        int i = yOffset + height;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        float f = (float)Math.floorMod(gameTime, 40) + partialTick;
        float f1 = height < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float)Mth.floor(f1 * 0.1F));
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(f * 2.25F - 45.0F));
        float f3;
        float f5;
        float f6 = -beamRadius;
        float f9 = -beamRadius;
        float f12 = -1.0F + f2;
        float f13 = (float)height * textureScale * (0.5F / beamRadius) + f12;
        renderPart(
                poseStack,
                bufferSource.getBuffer(RenderType.beaconBeam(beamLocation, false)),
                color,
                yOffset,
                i,
                0.0F,
                beamRadius,
                beamRadius,
                0.0F,
                f6,
                0.0F,
                0.0F,
                f9,
                0.0F,
                1.0F,
                f13,
                f12
        );
        poseStack.popPose();
        f3 = -glowRadius;
        float f4 = -glowRadius;
        f5 = -glowRadius;
        f6 = -glowRadius;
        f12 = -1.0F + f2;
        f13 = (float)height * textureScale + f12;
        renderPart(
                poseStack,
                bufferSource.getBuffer(RenderType.beaconBeam(beamLocation, true)),
                FastColor.ARGB32.color(32, color),
                yOffset,
                i,
                f3,
                f4,
                glowRadius,
                f5,
                f6,
                glowRadius,
                glowRadius,
                glowRadius,
                0.0F,
                1.0F,
                f13,
                f12
        );
        poseStack.popPose();
    }
    
    private static void renderPart(
            PoseStack poseStack,
            VertexConsumer consumer,
            int color,
            int minY,
            int maxY,
            float x1,
            float z1,
            float x2,
            float z2,
            float x3,
            float z3,
            float x4,
            float z4,
            float minU,
            float maxU,
            float minV,
            float maxV
    ) {
        PoseStack.Pose posestack$pose = poseStack.last();
        renderQuad(
                posestack$pose, consumer, color, minY, maxY, x1, z1, x2, z2, minU, maxU, minV, maxV
        );
        renderQuad(
                posestack$pose, consumer, color, minY, maxY, x4, z4, x3, z3, minU, maxU, minV, maxV
        );
        renderQuad(
                posestack$pose, consumer, color, minY, maxY, x2, z2, x4, z4, minU, maxU, minV, maxV
        );
        renderQuad(
                posestack$pose, consumer, color, minY, maxY, x3, z3, x1, z1, minU, maxU, minV, maxV
        );
    }
    
    private static void renderQuad(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int color,
            int minY,
            int maxY,
            float minX,
            float minZ,
            float maxX,
            float maxZ,
            float minU,
            float maxU,
            float minV,
            float maxV
    ) {
        addVertex(pose, consumer, color, maxY, minX, minZ, maxU, minV);
        addVertex(pose, consumer, color, minY, minX, minZ, maxU, maxV);
        addVertex(pose, consumer, color, minY, maxX, maxZ, minU, maxV);
        addVertex(pose, consumer, color, maxY, maxX, maxZ, minU, minV);
    }
    
    private static void addVertex(
            PoseStack.Pose pose, VertexConsumer consumer, int color, int y, float x, float z, float u, float v
    ) {
        consumer.addVertex(pose, x, (float)y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
    
    public boolean shouldRenderOffScreen(@NotNull AurithecBeaconBlockEntity blockEntity) {
        return true;
    }
    
    @Override
    public int getViewDistance() {
        return 256;
    }
    
    public boolean shouldRender(AurithecBeaconBlockEntity blockEntity, Vec3 cameraPos) {
        return Vec3.atCenterOf(blockEntity.getBlockPos()).multiply(1.0, 0.0, 1.0).closerThan(cameraPos.multiply(1.0, 0.0, 1.0), (double)this.getViewDistance());
    }
    
    @Override
    public @NotNull AABB getRenderBoundingBox(AurithecBeaconBlockEntity blockEntity) {
        var pos = blockEntity.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, MAX_RENDER_Y, pos.getZ() + 1.0);
    }
    
}
