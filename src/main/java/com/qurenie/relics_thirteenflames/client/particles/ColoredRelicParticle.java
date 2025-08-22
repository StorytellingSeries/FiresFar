package com.qurenie.relics_thirteenflames.client.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.qurenie.relics_thirteenflames.init.ParticlesRegistry;
import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.client.particles.BasicColoredParticle;
import lombok.Getter;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

import java.util.function.Supplier;

import static com.qurenie.relics_thirteenflames.client.ThirteenRenderTypes.CUSTOM_RENDER_TRANSLUCENT;
import static com.qurenie.relics_thirteenflames.client.ThirteenRenderTypes.CUSTOM_RENDER_TRANSLUCENT_LIGHTNING;

public class ColoredRelicParticle extends BasicColoredParticle {
    
    @Getter
    public static class Options implements ParticleOptions {
        
        private final Constructor data;
        float gravity;
        boolean lightningEffect = true;
        private Supplier<? extends ParticleType<Options>> type = ParticlesRegistry.COLORED_RELIC_PARTICLE;
        
        public Options(Constructor data) {
            this.data = data;
        }
        
        public Options(Supplier<? extends ParticleType<Options>> type, Constructor data) {
            this.data = data;
            this.type = type;
        }
        
        private Options(ParticleType<Options> type, Constructor data) {
            this.data = data;
            this.type = () -> type;
        }
        
        public Options withGravity(float gravity) {
            this.gravity = gravity;
            return this;
        }
        
        public Options withLightning(boolean lightning) {
            this.lightningEffect = lightning;
            return this;
        }
        
        @Nonnull
        @Override
        public ParticleType<Options> getType() {
            return type.get();
        }
        
        public static MapCodec<Options> codec(ParticleType<Options> type) {
            return RecordCodecBuilder.mapCodec(instance -> instance
                    .group(ConstructorCodecs.CONSTRUCTOR.fieldOf("data").forGetter(ColoredRelicParticle.Options::getData),
                            Codec.FLOAT.fieldOf("gravity").forGetter(ColoredRelicParticle.Options::getGravity),
                            Codec.BOOL.fieldOf("lightning").forGetter(ColoredRelicParticle.Options::isLightningEffect)
                            )
                    .apply(instance, (data, gravity, l) -> new Options(type, data).withGravity(gravity).withLightning(l))
            );
        }
        
        public static StreamCodec<ByteBuf, Options> streamCodec(ParticleType<Options> type) {
            return StreamCodec.composite(
                    ConstructorCodecs.STREAM_CODEC, Options::getData,
                    ByteBufCodecs.FLOAT, Options::getGravity,
                    ByteBufCodecs.BOOL, Options::isLightningEffect,
                    (data, gravity, l) -> new Options(type, data).withGravity(gravity).withLightning(l)
            );
        }
        
    }
    
    public static class DisappearingParticleFactory implements ParticleProvider<Options> {
        
        private final SpriteSet sprites;
        
        public DisappearingParticleFactory(SpriteSet sprites) {
            this.sprites = sprites;
        }
        
        @Nullable
        @Override
        public Particle createParticle(Options options, @NotNull ClientLevel world, double xPos, double yPos, double zPos, double xVelocity, double yVelocity, double zVelocity) {
            ColoredRelicParticle particle = new ColoredRelicParticle(world, xPos, yPos, zPos, xVelocity, yVelocity, zVelocity, options.getData(), options.getGravity(), options.lightningEffect, true);
            
            particle.pickSprite(sprites);
            
            return particle;
        }
    }
    
    public static class Factory implements ParticleProvider<Options> {
        private final SpriteSet sprites;
        
        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }
        
        @Nullable
        @Override
        public Particle createParticle(Options options, @NotNull ClientLevel world, double xPos, double yPos, double zPos, double xVelocity, double yVelocity, double zVelocity) {
            ColoredRelicParticle particle = new ColoredRelicParticle(world, xPos, yPos, zPos, xVelocity, yVelocity, zVelocity, options.getData(), options.getGravity(), options.lightningEffect, false);
            
            particle.pickSprite(sprites);
            
            return particle;
        }
    }
    
    public static class SpellParticle extends ColoredRelicParticle {
        
        private static final RandomSource RANDOM = RandomSource.create();
        
        public SpellParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, Constructor constructor, boolean lightning) {
            super(world, x, y, z, 0.2 - RANDOM.nextDouble() * 0.4, velocityY, 0.2 - RANDOM.nextDouble() * 0.4, constructor, -1, lightning, true);
            
            this.friction = 0.9F;
            this.speedUpWhenYMotionIsBlocked = true;
            this.yd *= 0.12F;
            if (velocityX == 0.0 && velocityZ == 0.0) {
                this.xd *= 0.1F;
                this.zd *= 0.1F;
            }
            
            this.hasPhysics = false;
        }
        
        @Override
        public void tick() {
            this.xd = this.xd * (double)this.friction;
            this.yd = this.yd * (double)this.friction;
            this.zd = this.zd * (double)this.friction;
            
            super.tick();
            
            this.yd = this.yd - 0.04 * (double)this.gravity;
            if (this.speedUpWhenYMotionIsBlocked && this.y == this.yo) {
                this.xd *= 1.1;
                this.zd *= 1.1;
            }
        }
        
    }
    
    public static class SpellFactory implements ParticleProvider<Options> {
        private final SpriteSet sprites;
        
        public SpellFactory(SpriteSet sprites) {
            this.sprites = sprites;
        }
        
        @Nullable
        @Override
        public Particle createParticle(Options options, @NotNull ClientLevel world, double xPos, double yPos, double zPos, double xVelocity, double yVelocity, double zVelocity) {
            SpellParticle particle = new SpellParticle(world, xPos, yPos, zPos, xVelocity, yVelocity, zVelocity, options.getData(), options.lightningEffect);
            
            particle.pickSprite(sprites);
            
            return particle;
        }
    }
    
    @Getter
    public static class Type extends ParticleType<Options> {
        
        public Type() {
            super(false);
        }
        
        
        @Override
        public @NotNull MapCodec<Options> codec() {
            return Options.codec(this);
        }
        
        @Override
        public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, Options> streamCodec() {
            return Options.streamCodec(this);
        }
        
    }
    
    boolean invisibleOnDisappear;
    boolean lightning;
    double dScale;
    float oldQuadSize;
    float currentQuadSize;
    
    public ColoredRelicParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, Constructor constructor, float gravity, boolean lightning, boolean invisibleOnDisappear) {
        super(world, x, y, z, velocityX, velocityY, velocityZ, constructor);
        this.gravity = gravity / 100f;
        this.invisibleOnDisappear = invisibleOnDisappear;
        this.lightning = lightning;
        this.dScale = constructor.getScaleModifier();
        this.oldQuadSize = this.currentQuadSize = this.quadSize = constructor.getDiameter();
    }
    
    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return lightning ? CUSTOM_RENDER_TRANSLUCENT_LIGHTNING : CUSTOM_RENDER_TRANSLUCENT;
    }
    
    @Override
    public void tick() {
        this.oldQuadSize = this.quadSize;
        this.currentQuadSize *= (float) dScale;
        
        super.tick();
        
        if (invisibleOnDisappear && this.lifetime - age < 30)
            this.alpha = Math.max(0, alpha - 1 / 28f);
        
        this.yd -= gravity;
    }
    
    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        this.quadSize = Mth.lerp(partialTicks, this.oldQuadSize, this.currentQuadSize);
        
        Quaternionf quaternionf = new Quaternionf();
        this.getFacingCameraMode().setRotation(quaternionf, renderInfo, partialTicks);
        if (this.roll != 0.0F) {
            quaternionf.rotateZ(Mth.lerp(partialTicks, this.oRoll, this.roll));
        }
        
        this.renderRotatedQuad(buffer, renderInfo, quaternionf, partialTicks);
    }
    
}
