//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.qurenie.relics_thirteenflames.client.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.qurenie.relics_thirteenflames.init.ParticlesRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Locale;

public class CircleTintData implements ParticleOptions {
    private final Color tint;
    private final float diameter;
    private final int fadeInTime;
    private final int lifeTime;
    private final float resizeSpeed;
    private final boolean shouldCollide;
    public static final Codec<CircleTintData> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(Codec.INT.fieldOf("tint").forGetter((d) -> {
            return d.tint.getRGB();
        }), Codec.FLOAT.fieldOf("diameter").forGetter((d) -> {
            return d.diameter;
        }), Codec.INT.fieldOf("fadein_time").forGetter((d) -> {
            return d.fadeInTime;
        }), Codec.INT.fieldOf("life_time").forGetter((d) -> {
            return d.lifeTime;
        }), Codec.FLOAT.fieldOf("resize_speed").forGetter((d) -> {
            return d.resizeSpeed;
        }), Codec.BOOL.fieldOf("should_collide").forGetter((d) -> {
            return d.shouldCollide;
        })).apply(instance, CircleTintData::new);
    });
    public static final Deserializer<CircleTintData> DESERIALIZER = new Deserializer<CircleTintData>() {
        @Nonnull
        public CircleTintData fromCommand(@Nonnull ParticleType<CircleTintData> type, @Nonnull StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            int red = Mth.clamp(reader.readInt(), 0, 255);
            reader.expect(' ');
            int green = Mth.clamp(reader.readInt(), 0, 255);
            reader.expect(' ');
            int blue = Mth.clamp(reader.readInt(), 0, 255);
            reader.expect(' ');
            float diameter = CircleTintData.validateDiameter(reader.readFloat());
            reader.expect(' ');
            int fadeInTime = reader.readInt();
            reader.expect(' ');
            int lifeTime = reader.readInt();
            reader.expect(' ');
            float resizeSpeed = reader.readFloat();
            reader.expect(' ');
            boolean shouldCollide = reader.readBoolean();
            return new CircleTintData(new Color(red, green, blue), diameter, fadeInTime, lifeTime, resizeSpeed, shouldCollide);
        }

        public CircleTintData fromNetwork(@Nonnull ParticleType<CircleTintData> type, FriendlyByteBuf buf) {
            int red = Mth.clamp(buf.readInt(), 0, 255);
            int green = Mth.clamp(buf.readInt(), 0, 255);
            int blue = Mth.clamp(buf.readInt(), 0, 255);
            Color color = new Color(red, green, blue);
            float diameter = CircleTintData.validateDiameter(buf.readFloat());
            int fadeInTime = buf.readInt();
            int lifeTime = buf.readInt();
            float resizeSpeed = buf.readFloat();
            boolean shouldCollide = buf.readBoolean();
            return new CircleTintData(color, diameter, fadeInTime, lifeTime, resizeSpeed, shouldCollide);
        }
    };

    public CircleTintData(Color tint, float diameter, int fadeInTime, int lifeTime, float resizeSpeed, boolean shouldCollide) {
        this.tint = tint;
        this.fadeInTime = fadeInTime;
        this.lifeTime = lifeTime;
        this.diameter = validateDiameter(diameter);
        this.resizeSpeed = resizeSpeed;
        this.shouldCollide = shouldCollide;
    }

    public Color getTint() {
        return this.tint;
    }

    public int getFadeInTime() {
        return fadeInTime;
    }

    public int getLifeTime() {
        return this.lifeTime;
    }

    public float getDiameter() {
        return this.diameter;
    }

    public float getResizeSpeed() {
        return this.resizeSpeed;
    }

    public boolean shouldCollide() {
        return this.shouldCollide;
    }

    @Nonnull
    public ParticleType<CircleTintData> getType() {
        return ParticlesRegistry.CIRCLE_TINT.get();
    }

    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeInt(this.tint.getRed());
        buf.writeInt(this.tint.getGreen());
        buf.writeInt(this.tint.getBlue());
        buf.writeFloat(this.diameter);
        buf.writeInt(this.fadeInTime);
        buf.writeInt(this.lifeTime);
        buf.writeFloat(this.resizeSpeed);
        buf.writeBoolean(this.shouldCollide);
    }

    @Nonnull
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %d %d %d %.2f %d %f %b", ForgeRegistries.PARTICLE_TYPES.getKey(this.getType()), this.tint.getRed(), this.tint.getGreen(), this.tint.getBlue(), this.diameter, this.lifeTime, this.resizeSpeed, this.shouldCollide);
    }

    private static float validateDiameter(float diameter) {
        return (float)Mth.clamp((double)diameter, 0.05, 5.0);
    }

    private CircleTintData(int tintRGB, float diameter, int fadeInTime, int lifeTime, float resizeSpeed, boolean shouldCollide) {
        this.tint = new Color(tintRGB);
        this.fadeInTime = fadeInTime;
        this.lifeTime = lifeTime;
        this.diameter = validateDiameter(diameter);
        this.resizeSpeed = resizeSpeed;
        this.shouldCollide = shouldCollide;
    }
}
