package com.qurenie.relics_thirteenflames.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qurenie.relics_thirteenflames.client.render.entity.IJodahGlowed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    
    @ModifyArg(
            method = "renderLevel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"),
            index = 6,
            remap = false
    )
    private MultiBufferSource modifyMultiBufferSource(
            Entity entity, double camX, double camY, double camZ, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource
    ) {
        if (!(entity instanceof LivingEntity living))
            return bufferSource;
        
        if (!((LevelRenderer) (Object) this).shouldShowEntityOutlines() || !IJodahGlowed.of(living).hasJodahGlowEffect())
            return bufferSource;
        
        var outline = Minecraft.getInstance().renderBuffers().outlineBufferSource();
        outline.setColor(100, 20, 150, 255);
        ((LevelRenderer) (Object) this).requestOutlineEffect();
        return outline;
    }
    
}
