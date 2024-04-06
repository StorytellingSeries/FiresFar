package com.qurenie.relics_thirteenflames.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.qurenie.relics_thirteenflames.mixins.client.BakedOverrideAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ZeithTechISTER
        extends BlockEntityWithoutLevelRenderer
{
    protected final BlockEntityRenderDispatcher blockEntRenderDispatcher;

    protected ZeithTechISTER()
    {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.blockEntRenderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
    }

    protected EntityModelSet getEntityModels()
    {
        return Minecraft.getInstance().getEntityModels();
    }

    public void renderOverrride(ItemOverrides.BakedOverride override, @NotNull ItemDisplayContext transformType, @NotNull PoseStack pose, @NotNull ItemStack stack, @NotNull MultiBufferSource bufferSource, @Nullable RenderType overrideType, int uv2, int overlay)
    {
        var mc = Minecraft.getInstance();
        var ir = mc.getItemRenderer();
        var overridenModel = ((BakedOverrideAccessor) override).getModel();

        if(overridenModel != null)
        {
            boolean cull;
            if(transformType != ItemDisplayContext.GUI && !transformType.firstPerson() &&
                    stack.getItem() instanceof BlockItem bi)
            {
                Block block = bi.getBlock();
                cull = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
            } else cull = true;

            for(var model : overridenModel.getRenderPasses(stack, cull))
            {
                for(var type : model.getRenderTypes(stack, cull))
                {
                    if(overrideType != null) type = overrideType;

                    VertexConsumer vertexconsumer;
                    if(cull)
                        vertexconsumer = ItemRenderer.getFoilBufferDirect(bufferSource, type, true, stack.hasFoil());
                    else
                        vertexconsumer = ItemRenderer.getFoilBuffer(bufferSource, type, true, stack.hasFoil());

                    ir.renderModelLists(overridenModel, stack, uv2, overlay, pose, vertexconsumer);
                }
            }
        }
    }

    public void renderAllOverrides(@NotNull ItemStack stack, @NotNull ItemDisplayContext transformType, @NotNull PoseStack pose, @NotNull MultiBufferSource bufferSource, int uv2, int overlay)
    {
        var mc = Minecraft.getInstance();
        var ir = mc.getItemRenderer();

        var isterModel = ir.getModel(stack, mc.level, mc.player, 0);
        var overrides = isterModel.getOverrides().getOverrides();

        for(var override : overrides)
        {
            var overridenModel = ((BakedOverrideAccessor) override).getModel();

            if(overridenModel != null)
            {
                boolean cull;
                if(transformType != ItemDisplayContext.GUI && !transformType.firstPerson() &&
                        stack.getItem() instanceof BlockItem bi)
                {
                    Block block = bi.getBlock();
                    cull = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
                } else cull = true;

                for(var model : overridenModel.getRenderPasses(stack, cull))
                {
                    for(var type : model.getRenderTypes(stack, cull))
                    {
                        VertexConsumer vertexconsumer;
                        if(cull)
                            vertexconsumer = ItemRenderer.getFoilBufferDirect(bufferSource, type, true, stack.hasFoil());
                        else
                            vertexconsumer = ItemRenderer.getFoilBuffer(bufferSource, type, true, stack.hasFoil());

                        ir.renderModelLists(overridenModel, stack, uv2, overlay, pose, vertexconsumer);
                    }
                }
            }
        }
    }

    @Override
    public abstract void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext transformType, @NotNull PoseStack pose, @NotNull MultiBufferSource bufferSource, int i, int j);
}
