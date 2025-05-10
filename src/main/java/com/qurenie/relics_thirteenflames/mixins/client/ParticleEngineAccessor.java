package com.qurenie.relics_thirteenflames.mixins.client;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ParticleEngine.class)
public interface ParticleEngineAccessor {
    
    @Accessor(remap = false)
    TextureManager getTextureManager();
    
}
