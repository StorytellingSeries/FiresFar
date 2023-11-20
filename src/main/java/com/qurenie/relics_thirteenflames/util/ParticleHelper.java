package com.qurenie.relics_thirteenflames.util;

import com.qurenie.relics_thirteenflames.net.PacketSpawnParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.zeith.hammerlib.net.Network;


public class ParticleHelper {

    public static void spawnParticleEntity(ParticleOptions particleOptions, Entity entity, int count, double maxSpeed) {
        spawnParticleAABB(entity.level, particleOptions, entity.getBoundingBox(), count, maxSpeed);
    }

    public static void spawnParticleAABB(Level level, ParticleOptions particleOptions, AABB box, int count, double maxSpeed) {
        Vec3 center = box.getCenter();
        double deltaX = box.getXsize() / 2d;
        double deltaY = box.getYsize() / 2d;
        double deltaZ = box.getZsize() / 2d;

        spawnParticles(level, particleOptions, center.x, center.y, center.z, count, deltaX, deltaY, deltaZ, maxSpeed);
    }


    public static void spawnParticleLine(Level level, ParticleOptions particle, Vec3 start, Vec3 end, int particleCount, double speed) {
        Vec3 delta = end.subtract(start);
        Vec3 dir = delta.normalize();
        double len = delta.length();

        for (int i = 0; i < particleCount; ++i) {
            double progress = i * len / particleCount;
            spawnParticles(level, particle, start.x + dir.x * progress, start.y + dir.y * progress, start.z + dir.z * progress,
                    1, 0, 0, 0, speed);
        }
    }

    public static void spawnDirectedParticle(Level level, ParticleOptions options, double x, double y, double z, double moveX, double moveY, double moveZ) {
        if (level.isClientSide)
            level.addParticle(options, true, x, y, z, moveX, moveY, moveZ);
        else
            Network.sendToAll(new PacketSpawnParticle(options, x, y, z, moveX, moveY, moveZ));
    }

    public static void spawnParticles(Level level, ParticleOptions options, double x, double y, double z, int count, double dx, double dy, double dz, double maxSpeed) {
        if (level.isClientSide)
            if (count == 0) {
                double d0 = maxSpeed * dx;
                double d2 = maxSpeed * dy;
                double d4 = maxSpeed * dz;
                level.addParticle(options, true, x, y, z, d0, d2, d4);
            } else {
                for (int i = 0; i < count; ++i) {
                    double d1 = level.random.nextGaussian() * dx;
                    double d3 = level.random.nextGaussian() * dy;
                    double d5 = level.random.nextGaussian() * dz;
                    double d6 = level.random.nextGaussian() * maxSpeed;
                    double d7 = level.random.nextGaussian() * maxSpeed;
                    double d8 = level.random.nextGaussian() * maxSpeed;
                    level.addParticle(options, true, x + d1, y + d3, z + d5, d6, d7, d8);
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


}
