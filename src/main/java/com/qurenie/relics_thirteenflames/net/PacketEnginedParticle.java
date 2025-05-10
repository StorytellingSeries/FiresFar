package com.qurenie.relics_thirteenflames.net;

import com.qurenie.relics_thirteenflames.mixins.client.ParticleAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;

@MainThreaded
public class PacketEnginedParticle implements IPacket {

    ParticleOptions options;
    double spawnX;
    double spawnY;
    double spawnZ;
    double moveX;
    double moveY;
    double moveZ;
    float scale;
    int lifetime;
    float r;
    float g;
    float b;
    float alpha;

    public PacketEnginedParticle() {
    }

    public PacketEnginedParticle(ParticleOptions options, double spawnX, double spawnY, double spawnZ, double moveX, double moveY, double moveZ, float scale, int lifetime, float r, float g, float b, float alpha) {
        this.options = options;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnZ = spawnZ;
        this.moveX = moveX;
        this.moveY = moveY;
        this.moveZ = moveZ;
        this.scale = scale;
        this.lifetime = lifetime;
        this.r = r;
        this.g = g;
        this.b = b;
        this.alpha = alpha;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeDouble(spawnX);
        buf.writeDouble(spawnY);
        buf.writeDouble(spawnZ);
        buf.writeDouble(moveX);
        buf.writeDouble(moveY);
        buf.writeDouble(moveZ);
        buf.writeFloat(scale);
        buf.writeInt(lifetime);
        buf.writeFloat(r);
        buf.writeFloat(g);
        buf.writeFloat(b);
        buf.writeFloat(alpha);
        ParticleTypes.STREAM_CODEC.encode(buf, this.options);
    }

    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        this.spawnX = buf.readDouble();
        this.spawnY = buf.readDouble();
        this.spawnZ = buf.readDouble();
        this.moveX = buf.readDouble();
        this.moveY = buf.readDouble();
        this.moveZ = buf.readDouble();
        this.scale = buf.readFloat();
        this.lifetime = buf.readInt();
        this.r = buf.readFloat();
        this.g = buf.readFloat();
        this.b = buf.readFloat();
        this.alpha = buf.readFloat();
        this.options = ParticleTypes.STREAM_CODEC.decode(buf);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientExecute(PacketContext ctx) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            Particle particle = Minecraft.getInstance().particleEngine
                    .createParticle(options,
                            spawnX, spawnY, spawnZ, moveX, moveY, moveZ);
            if(particle != null) {
                particle.scale(scale);
                particle.setLifetime(lifetime);
                particle.setColor(r, g, b);
                ((ParticleAccessor)particle).setAlpha(alpha);
            }
        }
    }

}
