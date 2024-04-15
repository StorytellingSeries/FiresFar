package com.qurenie.relics_thirteenflames.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

public class DeathFlameParticle extends TextureSheetParticle {

    public DeathFlameParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
        super(level, x, y, z, x, y, z);
        this.quadSize = 0.2f;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.x = x;
        this.y = y;
        this.z = z;
        this.lifetime = 30;
    }

    @Override
    public void tick() {
        super.tick();
        this.quadSize = Mth.clamp((1 - (age / (float) lifetime)) * 0.2f, 0f, 0.2f);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }


    @Override
    protected int getLightColor(float pPartialTick) {
        return super.getLightColor(pPartialTick);
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSetl;

        public Factory(SpriteSet sprite) {
            this.spriteSetl = sprite;
        }


        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double xv, double yv, double zv) {
            DeathFlameParticle particle = new DeathFlameParticle(world, x, y, z, xv, yv, zv);

            particle.pickSprite(this.spriteSetl);
            return particle;
        }
    }
}