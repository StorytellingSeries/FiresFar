package com.qurenie.relics_thirteenflames.client.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.qurenie.relics_thirteenflames.client.particles.misc.RotationType;
import com.qurenie.relics_thirteenflames.init.ParticlesRegistry;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class RotatingColoredRelicParticle extends ColoredRelicParticle {

    protected final RotateSettings rotateSettings;

    protected Vec3 absoluteAxis;
    protected Vec3 center;

    protected Vec3 baseSpeed;

    protected double startRadius;
    protected boolean initialized;

    public RotatingColoredRelicParticle(
            ClientLevel world,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ,
            Constructor constructor,
            float gravity,
            boolean lightning,
            boolean invisibleOnDisappear,
            RotationType rotationType,
            RotateSettings rotateSettings
    ) {
        super(
                world,
                x,
                y,
                z,
                velocityX,
                velocityY,
                velocityZ,
                constructor,
                gravity,
                lightning,
                invisibleOnDisappear,
                rotationType
        );
        this.rotateSettings = rotateSettings;
        this.baseSpeed = new Vec3(velocityX, velocityY, velocityZ);

        this.center = rotateSettings.center != null
                ? rotateSettings.center
                : new Vec3(x, y, z);

        this.absoluteAxis = rotateSettings.relativeAxis.normalize();

        Vec3 axis = this.absoluteAxis;

        Vec3 position = new Vec3(x, y, z);

        Vec3 centerToPos = position.subtract(center);

        double t = centerToPos.dot(axis);

        Vec3 projection = center.add(axis.scale(t));

        Vec3 radialVector = position.subtract(projection);

        double radius = radialVector.length();

        Vec3 radialDir;

        if (radius < 1.0E-7) {
            radialDir = createPerpendicular(axis);
        } else {
            radialDir = radialVector.normalize();
        }

        Vec3 rotated = rotateAroundAxis(
                radialDir,
                axis,
                rotateSettings.getAngle0()
        ).normalize();

        Vec3 position0 = projection.add(rotated.scale(radius));

        this.setPos(position0.x, position0.y, position0.z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    public void tick() {
        updateRotationMotion();

        super.tick();
    }

    protected void updateRotationMotion() {
        Vec3 position = new Vec3(this.x, this.y, this.z);

        Vec3 axis = absoluteAxis.normalize();

        /*
            Проекция точки на прямую:
            line(t) = center + axis * t
         */

        Vec3 centerToPos = position.subtract(center);

        double t = centerToPos.dot(axis);

        // Точка проекции на ось вращения
        Vec3 projection = center.add(axis.scale(t));

        // Радиальный вектор от оси к позиции
        Vec3 radialVector = position.subtract(projection);

        if (!initialized) {
            this.startRadius = radialVector.length();
            this.initialized = true;
        }

        double theoreticalRadius =
                startRadius + rotateSettings.resizeSpeed * this.age;

        Vec3 radialDir;

        if (radialVector.lengthSqr() < 1.0E-7) {
            radialDir = createPerpendicular(axis);
        } else {
            radialDir = radialVector.normalize();
        }

        /*
            angle(tick) = angle0 + angleSpeed * tick
         */
        double angle = rotateSettings.angleSpeed;

        // Поворот вокруг оси
        Vec3 rotatedDir = rotateAroundAxis(
                radialDir,
                axis,
                angle
        ).normalize();

        // Новая теоретическая позиция:
        // сохраняем положение вдоль оси,
        // меняем только радиальную часть
        Vec3 theoreticalPoint =
                projection.add(rotatedDir.scale(theoreticalRadius));

        Vec3 motion = theoreticalPoint.subtract(position);

        // В конце добавляем базовую скорость
        motion = motion.add(baseSpeed);

        this.xd = motion.x;
        this.yd = motion.y;
        this.zd = motion.z;
    }

    protected static Vec3 rotateAroundAxis(Vec3 vec, Vec3 axis, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        return vec.scale(cos)
                .add(axis.cross(vec).scale(sin))
                .add(axis.scale(axis.dot(vec) * (1.0 - cos)));
    }

    protected static Vec3 createPerpendicular(Vec3 axis) {
        Vec3 other = Math.abs(axis.y) < 0.99
                ? new Vec3(0, 1, 0)
                : new Vec3(1, 0, 0);

        return axis.cross(other).normalize();
    }

    @Getter
    public static class Options extends ColoredRelicParticle.Options {

        private final RotateSettings rotateSettings;

        public Options(
                Constructor data,
                RotateSettings rotateSettings
        ) {
            super(ParticlesRegistry.ROTATIVE_RELIC_PARTICLE, data);

            this.rotateSettings = rotateSettings;
        }

        public Options(
                Supplier<? extends ParticleType<Options>> type,
                Constructor data,
                RotateSettings rotateSettings
        ) {
            super(type, data);

            this.rotateSettings = rotateSettings;
        }

        private Options(
                ParticleType<? extends Options> type,
                Constructor data,
                RotateSettings rotateSettings
        ) {
            super(type, data);

            this.rotateSettings = rotateSettings;
        }

        @Override
        public Options withGravity(float gravity) {
            super.withGravity(gravity);
            return this;
        }

        @Override
        public Options withLightning(boolean lightning) {
            super.withLightning(lightning);
            return this;
        }

        @Override
        public Options withRotType(RotationType type) {
            super.withRotType(type);
            return this;
        }

        private static MapCodec<Options> codec(ParticleType<Options> type) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ConstructorCodecs.CONSTRUCTOR
                            .fieldOf("data")
                            .forGetter(Options::getData),

                    RotateSettings.CODEC
                            .fieldOf("rotate_settings")
                            .forGetter(Options::getRotateSettings),

                    Codec.FLOAT
                            .fieldOf("gravity")
                            .forGetter(Options::getGravity),

                    Codec.BOOL
                            .fieldOf("lightning")
                            .forGetter(Options::isLightningEffect),

                    RotationType.ORDINAL_CODEC
                            .fieldOf("rot_type")
                            .forGetter(Options::getRotationType)

            ).apply(instance, (data, rotateSettings, gravity, lightning, rotType) ->
                    new Options(type, data, rotateSettings)
                            .withGravity(gravity)
                            .withLightning(lightning)
                            .withRotType(rotType)
            ));
        }

        private static StreamCodec<ByteBuf, Options> streamCodec(ParticleType<Options> type) {
            return StreamCodec.composite(
                    ConstructorCodecs.STREAM_CODEC,
                    Options::getData,

                    RotateSettings.STREAM_CODEC,
                    Options::getRotateSettings,

                    ByteBufCodecs.FLOAT,
                    Options::getGravity,

                    ByteBufCodecs.BOOL,
                    Options::isLightningEffect,

                    RotationType.STREAM_CODEC,
                    Options::getRotationType,

                    (data, rotateSettings, gravity, lightning, rotType) ->
                            new Options(type, data, rotateSettings)
                                    .withGravity(gravity)
                                    .withLightning(lightning)
                                    .withRotType(rotType)
            );
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

    public static class Factory implements ParticleProvider<RotatingColoredRelicParticle.Options> {

        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(
                RotatingColoredRelicParticle.Options options,
                @NotNull ClientLevel world,
                double xPos,
                double yPos,
                double zPos,
                double xVelocity,
                double yVelocity,
                double zVelocity
        ) {

            RotatingColoredRelicParticle particle =
                    new RotatingColoredRelicParticle(
                            world,
                            xPos,
                            yPos,
                            zPos,
                            xVelocity,
                            yVelocity,
                            zVelocity,
                            options.getData(),
                            options.getGravity(),
                            options.isLightningEffect(),
                            false,
                            options.getRotationType(),
                            options.getRotateSettings()
                    );

            particle.pickSprite(sprites);

            return particle;
        }
    }

}