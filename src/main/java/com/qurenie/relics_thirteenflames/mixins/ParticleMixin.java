package com.qurenie.relics_thirteenflames.mixins;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.qurenie.relics_thirteenflames.mixins.client.ParticleEngineAccessor;
import it.hurts.sskirillss.relics.client.particles.BasicColoredParticle;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.*;

import static it.hurts.sskirillss.relics.client.particles.BasicColoredParticle.RENDERER_NO_DEPTH;

@Mixin(BasicColoredParticle.class)
public class ParticleMixin extends TextureSheetParticle {
    
    @Unique
    private final static ParticleRenderType CUSTOM_PARTICLE_SHEET_TRANSLUCENT = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator p_350826_, TextureManager p_107456_) {
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            return p_350826_.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
        }
        
        @Override
        public String toString() {
            return "CUSTOM_PARTICLE_SHEET_TRANSLUCENT";
        }
    };
    
    @Shadow
    private float oldQuadSize;
    
    @Shadow
    private float currentQuadSize;
    
    @Shadow
    @Final
    private BasicColoredParticle.Constructor constructor;
    @Unique
    private Tesselator tesselator;
    
    protected ParticleMixin(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }
    
    @Unique
    private void render_(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        tesselator = new Tesselator(8 * 512);
        
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        BufferBuilder bufferbuilder = getRenderType().begin(tesselator,
                ((ParticleEngineAccessor)(Minecraft.getInstance().particleEngine)).getTextureManager());
     
        this.quadSize = Mth.lerp(partialTicks, this.oldQuadSize, this.currentQuadSize);
        super.render(bufferbuilder == null ? buffer : bufferbuilder, renderInfo, partialTicks);
        
        if (bufferbuilder == null)
            return;
        
        MeshData meshdata = bufferbuilder.build();
        
        if (meshdata != null) {
            BufferUploader.drawWithShader(meshdata);
        }
        
        tesselator.clear();
    }
    
    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        render_(buffer, renderInfo, partialTicks);
    }
    
    @Override
    public ParticleRenderType getRenderType() {
        return this.constructor.isVisibleThroughWalls() ? RENDERER_NO_DEPTH : CUSTOM_PARTICLE_SHEET_TRANSLUCENT;
    }
    
}
