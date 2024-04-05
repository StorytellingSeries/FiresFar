package com.qurenie.relics_thirteenflames.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qurenie.relics_thirteenflames.content.tiles.TileShaking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.zeith.hammerlib.client.render.tile.IBESR;

public class TESRShaking
        implements IBESR<TileShaking>
{
    private final BlockRenderDispatcher dispatcher;

    public TESRShaking()
    {
        this.dispatcher = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(TileShaking entity, float partial, PoseStack matrix, MultiBufferSource buf, int lighting, int overlay)
    {
        var vec = entity.getOffset(partial);
        matrix.translate(vec.x + 0.5, vec.y, vec.z + 0.5);

        var pos = entity.getBlockPos();

        var shiftedPos = new BlockPos(
                Vec3.atCenterOf(pos)
                        .add(entity.getOffset(partial))
        );

        renderBlock(entity.getBlock(), entity.level(), shiftedPos, pos, matrix, buf);
    }

    public void renderBlock(BlockState state, Level level, BlockPos shiftedPos, BlockPos pos, PoseStack pose, MultiBufferSource src)
    {
        if(state.getRenderShape() != RenderShape.INVISIBLE)
        {
            pose.pushPose();
            pose.translate(-0.5D, 0.0D, -0.5D);
            var model = this.dispatcher.getBlockModel(state);
            for(var tType : model.getRenderTypes(state, RandomSource.create(state.getSeed(pos)), ModelData.EMPTY))
                this.dispatcher.getModelRenderer().tesselateBlock(
                        level, model, state, shiftedPos, pose,
                        src.getBuffer(tType), false,
                        RandomSource.create(),
                        state.getSeed(pos),
                        OverlayTexture.NO_OVERLAY, ModelData.EMPTY,
                        tType
                );
            pose.popPose();
        }
    }
}
