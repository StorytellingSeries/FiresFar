package com.qurenie.relics_thirteenflames.net;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;

@MainThreaded
public class PacketPlaySound
        implements IPacket
{
    private Vec3 pos;
    private SoundEvent sound;
    private SoundSource source;
    private float volume;
    private float pitch;

    public PacketPlaySound()
    {
    }

    public PacketPlaySound(Vec3 pos, SoundEvent pSound, SoundSource pSource, float pVolume, float pPitch)
    {
        Validate.notNull(pSound, "sound");
        this.pos = pos;
        this.sound = pSound;
        this.source = pSource;
        this.volume = pVolume;
        this.pitch = pPitch;
    }

    @Override
    public void write(FriendlyByteBuf buf)
    {
        buf.writeDouble(pos.x).writeDouble(pos.y).writeDouble(pos.z);
        buf.writeResourceLocation(this.sound.getLocation());
        buf.writeEnum(this.source);
        buf.writeFloat(this.volume);
        buf.writeFloat(this.pitch);
    }

    @Override
    public void read(FriendlyByteBuf buf)
    {
        this.pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.sound = BuiltInRegistries.SOUND_EVENT.get(buf.readResourceLocation());
        this.source = buf.readEnum(SoundSource.class);
        this.volume = buf.readFloat();
        this.pitch = buf.readFloat();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientExecute(PacketContext ctx)
    {
        var w = Minecraft.getInstance().level;
        if(w != null)
            w.playLocalSound(pos.x, pos.y, pos.z, sound, source, volume, pitch, false);
    }
}
