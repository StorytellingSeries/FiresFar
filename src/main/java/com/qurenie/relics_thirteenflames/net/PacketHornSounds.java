package com.qurenie.relics_thirteenflames.net;

import com.qurenie.relics_thirteenflames.content.items.ItemSeliasetHorn;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.Validate;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;

import static com.qurenie.relics_thirteenflames.net.PacketHornSounds.Toots.toot;
import static com.qurenie.relics_thirteenflames.net.PacketHornSounds.Toots.tootStop;

@MainThreaded
public class PacketHornSounds
        implements IPacket
{
    private boolean silence;

    public PacketHornSounds(boolean silence)
    {
        this.silence = silence;
    }

    @Override
    public void write(FriendlyByteBuf buf)
    {
        buf.writeBoolean(silence);
    }

    @Override
    public void read(FriendlyByteBuf buf)
    {
        this.silence = buf.readBoolean();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Toots {
        public static ItemSeliasetHorn.TootSoundInstance toot = new ItemSeliasetHorn.TootSoundInstance(SoundsRegistry.SELI_HORN_BLOW.get());
        public static ItemSeliasetHorn.TootSoundInstance tootStop = new ItemSeliasetHorn.TootSoundInstance(SoundsRegistry.SELI_HORN_BLOW_END.get());
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientExecute(PacketContext ctx)
    {


        var w = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;
        if(w != null && player != null) {
            if (silence)
                toot.fadeOut();
            else if (!Minecraft.getInstance().getSoundManager().isActive(toot)) {
                toot.originPos = player.position();
                toot.fadeIn();
                toot.setFade(0.02f);
                Minecraft.getInstance().getSoundManager().play(toot);
            } else toot.originPos = player.position();
        }
    }
}
