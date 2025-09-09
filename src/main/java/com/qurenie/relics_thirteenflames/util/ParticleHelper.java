package com.qurenie.relics_thirteenflames.util;

import com.qurenie.relics_thirteenflames.client.particles.ColoredRelicParticle;
import com.qurenie.relics_thirteenflames.client.particles.misc.RotationType;
import com.qurenie.relics_thirteenflames.init.ParticlesRegistry;
import com.qurenie.relics_thirteenflames.mixins.client.ParticleAccessor;
import com.qurenie.relics_thirteenflames.net.PacketEnginedParticle;
import com.qurenie.relics_thirteenflames.net.PacketSpawnParticle;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.zeith.hammerlib.net.Network;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

@UtilityClass
public class ParticleHelper {
    
    static Random rng = new Random();
    
    public static ColoredRelicParticle.Options constructSimpleSpark(Color color, float diameter, int lifetime, float scaleModifier) {
        return new ColoredRelicParticle.Options(ColoredRelicParticle.Constructor.builder().color(color.getRGB()).diameter(diameter).lifetime(lifetime).scaleModifier(scaleModifier).visibleThroughWalls(false).physical(false).roll(0.5F).build());
    }
    
    public static ColoredRelicParticle.Options constructHeal(Color color, float diameter, int lifetime, float scaleModifier) {
        return new ColoredRelicParticle.Options(ParticlesRegistry.COLORED_HEAL, ColoredRelicParticle.Constructor.builder().color(color.getRGB()).diameter(diameter).lifetime(lifetime).scaleModifier(scaleModifier).visibleThroughWalls(false).physical(false).roll(0.05F).build());
    }
    
    public static ColoredRelicParticle.Options constructFigure(Color color, float diameter, int lifetime, float scaleModifier) {
        return new ColoredRelicParticle.Options(ParticlesRegistry.FIGURES, ColoredRelicParticle.Constructor.builder().color(color.getRGB()).diameter(diameter).lifetime(lifetime).scaleModifier(scaleModifier).visibleThroughWalls(false).roll(0.3F).physical(false).build()).withRotType(RotationType.SIDE_RANDOM);
    }
    
    public static ColoredRelicParticle.Options constructEye(Color color, float diameter, int lifetime, float scaleModifier) {
        return new ColoredRelicParticle.Options(ParticlesRegistry.COLORED_EYE, ColoredRelicParticle.Constructor.builder().color(color.getRGB()).diameter(diameter).lifetime(lifetime).scaleModifier(scaleModifier).visibleThroughWalls(false).physical(false).roll(0F).build());
    }
    
    public static ColoredRelicParticle.Options constructSmoke(Color color, float diameter, int lifetime) {
        return new ColoredRelicParticle.Options(ParticlesRegistry.COLORED_SMOKE, ColoredRelicParticle.Constructor.builder().color(color.getRGB()).diameter(diameter).lifetime(lifetime).scaleModifier(1).visibleThroughWalls(false).physical(true).roll(0F).build());
    }
    
    public static ColoredRelicParticle.Options constructSmoke(Color color, float diameter, int lifetime, float roll) {
        return new ColoredRelicParticle.Options(ParticlesRegistry.COLORED_SMOKE, ColoredRelicParticle.Constructor.builder().color(color.getRGB()).diameter(diameter).lifetime(lifetime).scaleModifier(1).visibleThroughWalls(false).physical(true).roll(roll).build());
    }
    
    public static void spawnParticleEntity(ParticleOptions particleOptions, Entity entity, int count, double maxSpeed) {
        spawnParticleAABB(entity.level(), particleOptions, entity.getBoundingBox(), count, maxSpeed);
    }
    
    public static void spawnParticleTriangle(Level level, ParticleOptions options, Vec3 point1, Vec3 point2, Vec3 point3, int countPerSide, double speed) {
        Vec3 side1 = point2.subtract(point1);
        Vec3 side2 = point3.subtract(point1);
        
        double d1 = side1.length() / countPerSide;
        double d2 = side2.length() / countPerSide;
        
        Vec3 side1Normalized = side1.normalize();
        Vec3 side2Normalized = side2.normalize();
        
        for (int i = 0; i <= countPerSide; i++) {
            Vec3 start = point1.add(side1Normalized.scale(d1 * i));
            Vec3 end = point1.add(side2Normalized.scale(d2 * i));
            
            spawnParticleLine(level, options, start, end, i + 1, speed, 0);
        }
    }
    
    public static void spawnParticleTriangle(Level level, ParticleOptions options, Vec3 point1, Vec3 point2, Vec3 point3, int countPerSide, Vec3 speed) {
        spawnParticleTriangle(level, options, point1, point2, point3, countPerSide, () -> speed);
    }
    
    public static void spawnParticleTriangle(Level level, ParticleOptions options, Vec3 point1, Vec3 point2, Vec3 point3, int countPerSide, Supplier<Vec3> speed) {
        Vec3 side1 = point2.subtract(point1);
        Vec3 side2 = point3.subtract(point1);
        
        double d1 = side1.length() / countPerSide;
        double d2 = side2.length() / countPerSide;
        
        Vec3 side1Normalized = side1.normalize();
        Vec3 side2Normalized = side2.normalize();
        
        for (int i = 0; i <= countPerSide; i++) {
            Vec3 start = point1.add(side1Normalized.scale(d1 * i));
            Vec3 end = point1.add(side2Normalized.scale(d2 * i));
            
            spawnParticleLine(level, options, start, end, i + 1, speed, 0);
        }
    }
    
    public static void spawnParticleAABB(Level level, ParticleOptions particleOptions, AABB box, int count, double maxSpeed) {
        Vec3 center = box.getCenter();
        double deltaX = box.getXsize() / 2d;
        double deltaY = box.getYsize() / 2d;
        double deltaZ = box.getZsize() / 2d;
        
        spawnParticles(level, particleOptions, center.x, center.y, center.z, count, deltaX, deltaY, deltaZ, maxSpeed);
    }
    
    public static void spawnParticleOutbox(Level level, ParticleOptions particleOptions, BlockPos pos, int countOnSide, double maxSpeed) {
        for (int i = 0; i < 6; i++) {
            Vec3 offset = new Vec3(i % 3 == 0 ? i % 2 : 0.5, i % 3 == 1 ? i % 2 : 0.5, i % 3 == 2 ? i % 2 : 0.5);
            Vec3 diff = new Vec3(i % 3 == 0 ? 0 : 0.25, i % 3 == 1 ? 0 : 0.25, i % 3 == 2 ? 0 : 0.25);
            
            spawnParticles(level, particleOptions, pos.getX() + offset.x, pos.getY() + offset.y, pos.getZ() + offset.z,
                    countOnSide, diff.x, diff.y, diff.z, maxSpeed);
        }
    }
    
    
    public static void spawnParticleLine(Level level, ParticleOptions particle, Vec3 start, Vec3 end, int particleCount, double speed) {
        spawnParticleLine(level, particle, start, end, particleCount, speed, 0);
    }
    
    public static void spawnParticleLine(Level level, ParticleOptions particle, Vec3 start, Vec3 end, int particleCount, Vec3 speed, double spread) {
        spawnParticleLine(level, particle, start, end, particleCount, () -> speed, spread);
    }
    
    public static void spawnParticleLine(Level level, ParticleOptions particle, Vec3 start, Vec3 end, int particleCount, Supplier<Vec3> speed, double spread) {
        Vec3 delta = end.subtract(start);
        Vec3 dir = delta.normalize();
        double len = delta.length();
        
        for (int i = 0; i < particleCount; ++i) {
            Vec3 v = speed.get().scale(level.random.nextDouble());;
            double progress = i * len / particleCount;
            spawnDirectedParticle(level, particle, start.x + dir.x * progress + rng.nextGaussian() * spread, start.y + dir.y * progress + rng.nextGaussian() * spread,
                    start.z + dir.z * progress + rng.nextGaussian() * spread, v.x, v.y, v.z);
        }
    }
    
    public static void spawnParticleLine(Level level, ParticleOptions particle, Vec3 start, Vec3 end, int particleCount, double speed, double spread) {
        Vec3 delta = end.subtract(start);
        Vec3 dir = delta.normalize();
        double len = delta.length();
        
        for (int i = 0; i < particleCount; ++i) {
            double progress = i * len / particleCount;
            spawnParticles(level, particle, start.x + dir.x * progress, start.y + dir.y * progress, start.z + dir.z * progress,
                    1, spread, spread, spread, speed);
        }
    }
    
    public static void spawnDirectedParticle(Level level, ParticleOptions options, double x, double y, double z, double moveX, double moveY, double moveZ) {
        if (level.isClientSide)
            level.addParticle(options, true, x, y, z, moveX, moveY, moveZ);
        else
            Network.sendToAll(new PacketSpawnParticle(options, x, y, z, moveX, moveY, moveZ));
    }
    
    public static void spawnDirectedParticle(Level level, ParticleOptions options, Vec3 pos, double moveX, double moveY, double moveZ) {
        spawnDirectedParticle(level, options, pos.x, pos.y, pos.z, moveX, moveY, moveZ);
    }
    
    public static void spawnDirectedParticle(Level level, ParticleOptions options, Vec3 pos, Vec3 move) {
        spawnDirectedParticle(level, options, pos, move.x, move.y, move.z);
    }
    
    public static void spawnParticles(Level level, ParticleOptions options, double x, double y, double z, int count, double dx, double dy, double dz, double maxSpeed) {
        if (level.isClientSide)
            if (count == 0) {
                double d0 = maxSpeed * dx;
                double d2 = maxSpeed * dy;
                double d4 = maxSpeed * dz;
                level.addParticle(options, x, y, z, d0, d2, d4);
            } else {
                for (int i = 0; i < count; ++i) {
                    double d1 = level.random.nextGaussian() * dx;
                    double d3 = level.random.nextGaussian() * dy;
                    double d5 = level.random.nextGaussian() * dz;
                    double d6 = level.random.nextGaussian() * maxSpeed;
                    double d7 = level.random.nextGaussian() * maxSpeed;
                    double d8 = level.random.nextGaussian() * maxSpeed;
                    level.addParticle(options, x + d1, y + d3, z + d5, d6, d7, d8);
                }
            }
        else for (int i = 0; i < count; ++i) {
            double d1 = level.random.nextGaussian() * dx;
            double d3 = level.random.nextGaussian() * dy;
            double d5 = level.random.nextGaussian() * dz;
            double d6 = level.random.nextGaussian() * maxSpeed;
            double d7 = level.random.nextGaussian() * maxSpeed;
            double d8 = level.random.nextGaussian() * maxSpeed;
            Network.sendToAll(new PacketSpawnParticle(options, x + d1, y + d3, z + d5, d6, d7, d8));
        }
    }
    
    public static void spawnParticles(Level level, ParticleOptions options, Vec3 pos, int count, double dx, double dy, double dz, double maxSpeed) {
        spawnParticles(level, options, pos.x, pos.y, pos.z, count, dx, dy, dz, maxSpeed);
    }
    
    public static void spawnRandomJaggedParticleLine(Level level, Vec3 begin, Vec3 end, double maxJagMultiplier, ParticleOptions options, int particlesPerBlock, int sliceIterations) {
        List<Vec3> locs = new ArrayList<>(List.of(begin));
        locs.addAll(getJagged(begin, end, sliceIterations, maxJagMultiplier));
        locs.add(end);
        for (int i = 1; i < locs.size(); i++) {
            double distance = locs.get(i).subtract(locs.get(i - 1)).length();
            ParticleHelper.spawnParticleLine(level, options,
                    locs.get(i - 1), locs.get(i), (int) Math.round(distance * particlesPerBlock), 0);
        }
    }
    
    public static void spawnEnginedParticle(Level level, ParticleOptions options, Vec3 pos, double moveX, double moveY, double moveZ, float scale, int lifetime, Color color, float alpha) {
        if (level.isClientSide()) {
            useParticleEngine(level, options, pos, moveX, moveY, moveZ, scale, lifetime, color, alpha);
        } else {
            Network.sendToAll(new PacketEnginedParticle(options, pos.x, pos.y, pos.z,
                    moveX, moveY, moveZ,
                    scale, lifetime,
                    color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f,
                    alpha)
            );
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void useParticleEngine(Level level, ParticleOptions options, Vec3 pos, double moveX, double moveY, double moveZ, float scale, int lifetime, Color color, float alpha) {
        Particle particle = Minecraft.getInstance().particleEngine
                .createParticle(options,
                        pos.x, pos.y, pos.z, moveX, moveY, moveZ);
        if (particle != null) {
            particle.scale(scale);
            particle.setLifetime(lifetime);
            particle.setColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f);
            ((ParticleAccessor) particle).setAlpha(alpha);
        }
    }
    
    public static void spawnEnginedParticle(Level level, ParticleOptions options, double x, double y, double z, double moveX, double moveY, double moveZ, float scale, int lifetime, Color color, float alpha) {
        spawnEnginedParticle(level, options, new Vec3(x, y, z), moveX, moveY, moveZ, scale, lifetime, color, alpha);
    }
    
    public static void spawnEnginedParticles(Level level, ParticleOptions options, double x, double y, double z, int count, double dx, double dy, double dz, double maxSpeed, float scale, int lifetime, Color color, float alpha) {
        for (int i = 0; i < count; ++i) {
            double d1 = level.random.nextGaussian() * dx;
            double d3 = level.random.nextGaussian() * dy;
            double d5 = level.random.nextGaussian() * dz;
            double d6 = level.random.nextGaussian() * maxSpeed;
            double d7 = level.random.nextGaussian() * maxSpeed;
            double d8 = level.random.nextGaussian() * maxSpeed;
            spawnEnginedParticle(level, options, x + d1, y + d3, z + d5, d6, d7, d8, scale, lifetime, color, alpha);
        }
    }
    
    public static void spawnEnginedParticles(Level level, ParticleOptions options, Vec3 pos, int count, double dx, double dy, double dz, double maxSpeed, float scale, int lifetime, Color color, float alpha) {
        spawnEnginedParticles(level, options, pos.x, pos.y, pos.z, count, dx, dy, dz, maxSpeed, scale, lifetime, color, alpha);
    }
    
    
    private static ArrayList<Vec3> getJagged(Vec3 begin, Vec3 end, int slicesLeft, double maxJagMultiplier) {
        float a = 0.4f + rng.nextFloat(0.2f);
        Vec3 mid = begin.add(end.subtract(begin).scale(a)).add(getRandomRotNormal(end.subtract(begin)).normalize().scale(rng.nextDouble(end.subtract(begin).length() * maxJagMultiplier)));
        if (slicesLeft > 0) {
            ArrayList<Vec3> left = getJagged(begin, mid, slicesLeft - 1, maxJagMultiplier);
            ArrayList<Vec3> right = getJagged(mid, end, slicesLeft - 1, maxJagMultiplier);
            left.add(mid);
            left.addAll(right);
            return left;
        } else return new ArrayList<Vec3>(List.of(mid));
    }
    
    private static Vec3 getRandomRotNormal(Vec3 vec) {
        Vec3 normal = vec.normalize();
        Vec3 x = !(normal.x < 0.001 && normal.z < 0.001) ? normal.cross(new Vec3(0, 1, 0)).normalize() : normal.cross(new Vec3(1, 0, 0)).normalize();
        Vec3 z = normal.cross(x).normalize();
        float deg = rng.nextFloat(360);
        return x.scale(Math.cos(Math.toRadians(deg)))
                .add(z.scale(Math.sin(Math.toRadians(deg))));
    }
    
}
