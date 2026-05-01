package com.qurenie.relics_thirteenflames.client.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.qurenie.relics_thirteenflames.client.ThirteenRenderTypes;
import com.qurenie.relics_thirteenflames.init.ParticlesRegistry;
import io.netty.buffer.ByteBuf;
import it.hurts.octostudios.octolib.util.OctoColor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

public class CircleTintParticle extends TextureSheetParticle {
    
    float resizeSpeed, diameter;
    int fadeInTime;
    protected float originalSize;

    public CircleTintParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, OctoColor spark, float diameter, int fadeInTime, int lifeTime, float resizeSpeed, boolean shouldCollide) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.resizeSpeed = resizeSpeed;
        this.setColor(spark.r(), spark.g(), spark.b());
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
    
    protected int getLightColor(float partialTick) {
        return LightTexture.pack(15, 15);
    }

    @Nonnull
    public ParticleRenderType getRenderType() {
        return ThirteenRenderTypes.CUSTOM_RENDER_TRANSLUCENT;
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
    
    public static class Factory implements ParticleProvider<Options> {
        
        private final SpriteSet sprites;
        
        @Nullable
        public Particle createParticle(Options options, @NotNull ClientLevel world, double xPos, double yPos, double zPos, double xVelocity, double yVelocity, double zVelocity) {
            CircleTintParticle particle = new CircleTintParticle(world, xPos, yPos, zPos, xVelocity, yVelocity, zVelocity, options.getTint(), options.getDiameter(), options.getFadeInTime(), options.getLifeTime(), options.getResizeSpeed(), options.isShouldCollide());
            
            particle.pickSprite(this.sprites);
            return particle;
        }
        
        public Factory(SpriteSet sprite) {
            this.sprites = sprite;
        }
    }
    
    public static class Type extends ParticleType<Options> {
        
        public Type() {
            super(false);
        }
        
        @Override
        public @NotNull MapCodec<Options> codec() {
            return Options.CODEC;
        }
        
        @Override
        public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, Options> streamCodec() {
            return Options.STREAM_CODEC;
        }
        
    }
    
    @Getter
    @AllArgsConstructor
    public static class Options implements ParticleOptions {
        
        public static final MapCodec<Options> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                Codec.INT.fieldOf("tint").forGetter((d) -> d.tint.getARGB()),
                Codec.FLOAT.fieldOf("diameter").forGetter(Options::getDiameter),
                Codec.INT.fieldOf("fadein_time").forGetter(Options::getFadeInTime),
                Codec.INT.fieldOf("life_time").forGetter(Options::getLifeTime),
                Codec.FLOAT.fieldOf("resize_speed").forGetter(Options::getResizeSpeed),
                Codec.BOOL.fieldOf("should_collide").forGetter(Options::isShouldCollide)
        ).apply(instance, Options::new));
        
        public static final StreamCodec<ByteBuf, Options> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, options -> options.getTint().getARGB(),
                ByteBufCodecs.FLOAT, Options::getDiameter,
                ByteBufCodecs.INT, Options::getFadeInTime,
                ByteBufCodecs.INT, Options::getLifeTime,
                ByteBufCodecs.FLOAT, Options::getResizeSpeed,
                ByteBufCodecs.BOOL, Options::isShouldCollide,
                Options::new
        );
        
        private final OctoColor tint;
        private final float diameter;
        private final int fadeInTime;
        private final int lifeTime;
        private final float resizeSpeed;
        private final boolean shouldCollide;
        
        private Options(int tintARGB, float diameter, int fadeInTime, int lifeTime, float resizeSpeed, boolean shouldCollide) {
            this(new OctoColor(tintARGB), validateDiameter(diameter), fadeInTime, lifeTime, resizeSpeed, shouldCollide);
        }
        
        private static float validateDiameter(float diameter) {
            return (float) Mth.clamp(diameter, 0.05, 5.0);
        }
        
        @Nonnull
        public ParticleType<Options> getType() {
            return ParticlesRegistry.CIRCLE_TINT_PARTICLE.get();
        }
        
    }
    
}
