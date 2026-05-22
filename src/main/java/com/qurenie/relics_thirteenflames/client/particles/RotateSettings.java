package com.qurenie.relics_thirteenflames.client.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

@Getter
public class RotateSettings {

    public static final Codec<Vec3> VEC3_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("x").forGetter(Vec3::x),
                    Codec.DOUBLE.fieldOf("y").forGetter(Vec3::y),
                    Codec.DOUBLE.fieldOf("z").forGetter(Vec3::z)
            ).apply(instance, Vec3::new)
    );

    public static final StreamCodec<ByteBuf, Vec3> VEC3_STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, Vec3::x,
                    ByteBufCodecs.DOUBLE, Vec3::y,
                    ByteBufCodecs.DOUBLE, Vec3::z,
                    Vec3::new
            );

    public static final MapCodec<RotateSettings> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(

                    VEC3_CODEC.optionalFieldOf(
                            "relative_axis",
                            new Vec3(0, 1, 0)
                    ).forGetter(RotateSettings::getRelativeAxis),

                    VEC3_CODEC.optionalFieldOf(
                            "center",
                            Vec3.ZERO
                    ).forGetter(RotateSettings::getCenter),

                    Codec.DOUBLE.optionalFieldOf(
                            "resize_speed",
                            0D
                    ).forGetter(RotateSettings::getResizeSpeed),

                    Codec.DOUBLE.optionalFieldOf(
                            "angle0",
                            0D
                    ).forGetter(RotateSettings::getAngle0),

                    Codec.DOUBLE.fieldOf(
                            "angle_speed"
                    ).forGetter(RotateSettings::getAngleSpeed)

            ).apply(instance, (axis, center, resizeSpeed, angle0, angleSpeed) ->
                    RotateSettings.builder()
                            .relativeAxis(axis)
                            .center(center)
                            .resizeSpeed(resizeSpeed)
                            .angle0(angle0)
                            .angleSpeed(angleSpeed)
                            .build()
            ));

    public static final StreamCodec<ByteBuf, RotateSettings> STREAM_CODEC =
            StreamCodec.composite(
                    VEC3_STREAM_CODEC,
                    RotateSettings::getRelativeAxis,

                    VEC3_STREAM_CODEC,
                    RotateSettings::getCenter,

                    ByteBufCodecs.DOUBLE,
                    RotateSettings::getResizeSpeed,

                    ByteBufCodecs.DOUBLE,
                    RotateSettings::getAngle0,

                    ByteBufCodecs.DOUBLE,
                    RotateSettings::getAngleSpeed,

                    (axis, center, resizeSpeed, angle0, angleSpeed) ->
                            RotateSettings.builder()
                                    .relativeAxis(axis)
                                    .center(center)
                                    .resizeSpeed(resizeSpeed)
                                    .angle0(angle0)
                                    .angleSpeed(angleSpeed)
                                    .build()
            );

    protected Vec3 relativeAxis = new Vec3(0, 1, 0);

    protected Vec3 center = Vec3.ZERO;

    protected double resizeSpeed = 0D;

    /**
     * Начальный угол частицы.
     */
    protected double angle0 = 0D;

    /**
     * Угловая скорость в радианах за тик.
     */
    protected double angleSpeed;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final RotateSettings settings = new RotateSettings();

        public Builder relativeAxis(Vec3 axis) {
            settings.relativeAxis = axis;
            return this;
        }

        public Builder center(Vec3 center) {
            settings.center = center;
            return this;
        }

        public Builder resizeSpeed(double resizeSpeed) {
            settings.resizeSpeed = resizeSpeed;
            return this;
        }

        public Builder angle0(double angle0) {
            settings.angle0 = angle0;
            return this;
        }

        public Builder angleSpeed(double angleSpeed) {
            settings.angleSpeed = angleSpeed;
            return this;
        }

        public RotateSettings build() {
            return settings;
        }
    }
}