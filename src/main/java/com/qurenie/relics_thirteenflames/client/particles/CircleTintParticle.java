package com.qurenie.relics_thirteenflames.client.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

import javax.annotation.Nonnull;
import java.awt.*;

public class CircleTintParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    float resizeSpeed, diameter;
    int fadeInTime;

    protected float originalSize;
    private static final ParticleRenderType RENDERER = new ParticleRenderType() {
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 1);
            textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES).setBlurMipmap(true, false);
            bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        public void end(Tesselator tessellator) {
            tessellator.end();
            RenderSystem.enableDepthTest();
            Minecraft.getInstance().textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES).restoreLastBlurMipmap();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
        }

        public String toString() {
            return "minitelling:circle_tint";
        }
    };

    public CircleTintParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, Color spark, float diameter, int fadeInTime, int lifeTime, float resizeSpeed, boolean shouldCollide, SpriteSet sprites) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.sprites = sprites;
        this.resizeSpeed = resizeSpeed;
        this.setColor((float)spark.getRed() / 255.0F, (float)spark.getGreen() / 255.0F, (float)spark.getBlue() / 255.0F);
        this.setSize(diameter, diameter);
        this.fadeInTime = fadeInTime;
        this.lifetime = lifeTime;
        this.diameter = diameter;
        this.quadSize = 0;
        this.originalSize = diameter;
        this.alpha = 1.0F;
        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;
        this.hasPhysics = shouldCollide;
    }

    public float getQuadSize(float scaleFactor) {
        return this.quadSize;
    }

    protected int getLightColor(float partialTick) {
        return LightTexture.pack(15, 15);
    }

    @Nonnull
    public ParticleRenderType getRenderType() {
        return RENDERER;
    }

    public void tick() {
        if(this.age < this.fadeInTime) {
            this.quadSize = this.diameter * this.age / (this.fadeInTime + 1);
        } else {
            if (resizeSpeed != -1) {
                this.quadSize = this.diameter;
                this.diameter *= this.resizeSpeed;
            }else{
                this.quadSize = this.diameter;
                this.diameter = originalSize * (1 - ((float)age - this.fadeInTime) / (float)lifetime);
            }
        }
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.move(this.xd, this.yd, this.zd);
        if (this.onGround) {
            this.remove();
        }

        if (this.yo == this.y && this.yd > 0.0) {
            this.remove();
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
        }

    }
}
