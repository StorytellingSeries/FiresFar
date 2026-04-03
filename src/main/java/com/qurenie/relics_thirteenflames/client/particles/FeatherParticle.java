package com.qurenie.relics_thirteenflames.client.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.qurenie.relics_thirteenflames.init.ParticlesRegistry;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import static net.minecraft.client.particle.ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;

public class FeatherParticle extends TextureSheetParticle {
    
    private float dRoll;
    
    protected FeatherParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, float roll, int lifetime) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.gravity = 0.08f;
        this.dRoll = (float) ((0.5 - Math.random()) * 2 * roll);
        this.roll = (float) (Math.random() * Math.PI * 2);
        this.quadSize = 0.14f;
        this.lifetime = lifetime;
    }
    
    @Override
    public void tick() {
        super.tick();
        Vec3 movement = new Vec3(xd, yd, zd);
        
        if (this.onGround)
            this.dRoll *= 0.7f;
        
        if (movement.lengthSqr() > 0.0003)
            movement = movement.scale(0.98f);
        
        xd = movement.x;
        yd = movement.y;
        zd = movement.z;
        
        if (this.lifetime - age < 30)
            this.alpha = Math.max(0, alpha - 1 / 28f);
        
        this.oRoll = roll;
        this.roll += dRoll;
    }
    
    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return PARTICLE_SHEET_TRANSLUCENT;
    }
    
    public static class Options implements ParticleOptions {
        
        @Getter
        final float roll;
        @Getter
        final int lifetime;
        final Supplier<? extends ParticleType<Options>> type;
        
        public Options(float roll, int lifetime, Supplier<? extends ParticleType<Options>> type) {
            this.roll = roll;
            this.lifetime = lifetime;
            this.type = type;
        }
        
        public Options(float roll, int lifetime) {
            this(roll, lifetime, ParticlesRegistry.JODAH_FEATHER);
        }
        
        @Override
        public @NotNull ParticleType<Options> getType() {
            return type.get();
        }
        
        public static MapCodec<Options> codec(ParticleType<Options> type) {
            return RecordCodecBuilder.mapCodec(instance -> instance
                    .group(
                            Codec.FLOAT.fieldOf("roll").forGetter(Options::getRoll),
                            Codec.INT.fieldOf("lifetime").forGetter(Options::getLifetime))
                    .apply(instance, (roll, lifetime) -> new Options(roll, lifetime, () -> type)));
        }
        
        public static StreamCodec<ByteBuf, Options> streamCodec(Type type) {
            return StreamCodec.composite(
                    ByteBufCodecs.INT, Options::getLifetime,
                    ByteBufCodecs.FLOAT, Options::getRoll,
                    (l, r) -> new Options(r, l, () -> type)
            );
        }
    }
    
    public static class Factory implements ParticleProvider<Options> {
        
        private final SpriteSet sprites;
        
        @Nullable
        public Particle createParticle(Options options, @NotNull ClientLevel world, double xPos, double yPos, double zPos, double xVelocity, double yVelocity, double zVelocity) {
            FeatherParticle particle = new FeatherParticle(world, xPos, yPos, zPos, xVelocity, yVelocity, zVelocity, options.roll, options.lifetime);
            
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
            return Options.codec(this);
        }
        
        @Override
        public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, Options> streamCodec() {
            return Options.streamCodec(this);
        }
        
    }
    
}
