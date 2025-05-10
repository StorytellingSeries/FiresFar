package com.qurenie.relics_thirteenflames.net;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.logging.log4j.util.Cast;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;

@MainThreaded
public class PacketSpawnParticle implements IPacket {

    ParticleOptions options;
    double spawnX;
    double spawnY;
    double spawnZ;
    double moveX;
    double moveY;
    double moveZ;

    public PacketSpawnParticle() {
    }

    public PacketSpawnParticle(ParticleOptions options, double spawnX, double spawnY, double spawnZ, double moveX, double moveY, double moveZ) {
        this.options = options;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnZ = spawnZ;
        this.moveX = moveX;
        this.moveY = moveY;
        this.moveZ = moveZ;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(BuiltInRegistries.PARTICLE_TYPE.getKey(options.getType()));
        options.getType().streamCodec().encode(buf, Cast.cast(options));
        buf.writeDouble(spawnX);
        buf.writeDouble(spawnY);
        buf.writeDouble(spawnZ);
        buf.writeDouble(moveX);
        buf.writeDouble(moveY);
        buf.writeDouble(moveZ);
    }

    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        ParticleType<?> particletype = BuiltInRegistries.PARTICLE_TYPE.get(buf.readResourceLocation());
        this.options = this.readParticle(buf, particletype);
        this.spawnX = buf.readDouble();
        this.spawnY = buf.readDouble();
        this.spawnZ = buf.readDouble();
        this.moveX = buf.readDouble();
        this.moveY = buf.readDouble();
        this.moveZ = buf.readDouble();
    }

    private <T extends ParticleOptions> T readParticle(RegistryFriendlyByteBuf pBuffer, ParticleType<T> pParticleType) {
        return pParticleType.streamCodec().decode(pBuffer);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientExecute(PacketContext ctx) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null)
            level.addParticle(options, true, spawnX, spawnY, spawnZ, moveX, moveY, moveZ);
    }

}
