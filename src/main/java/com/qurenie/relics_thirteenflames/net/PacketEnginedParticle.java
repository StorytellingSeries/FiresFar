package com.qurenie.relics_thirteenflames.net;

import com.qurenie.relics_thirteenflames.mixins.ParticleAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
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
    public void write(FriendlyByteBuf buf) {
        buf.writeId(BuiltInRegistries.PARTICLE_TYPE, options.getType());
        options.writeToNetwork(buf);
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
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        ParticleType<?> particletype = buf.readById(BuiltInRegistries.PARTICLE_TYPE);
        this.options = this.readParticle(buf, particletype);
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
    }

    private <T extends ParticleOptions> T readParticle(FriendlyByteBuf pBuffer, ParticleType<T> pParticleType) {
        return pParticleType.getDeserializer().fromNetwork(pParticleType, pBuffer);
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
