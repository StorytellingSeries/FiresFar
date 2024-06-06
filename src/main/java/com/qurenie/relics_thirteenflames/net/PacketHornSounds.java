package com.qurenie.relics_thirteenflames.net;

import com.qurenie.relics_thirteenflames.content.items.ItemSeliasetHorn;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.Validate;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;
import oshi.util.tuples.Pair;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static com.qurenie.relics_thirteenflames.net.PacketHornSounds.TootsManager.tootMap;

@MainThreaded
public class PacketHornSounds
        implements IPacket
{
    private boolean silence;
    private String originUUID;

    private Vec3 originPos;

    public PacketHornSounds(String originUUID, Vec3 originPos, boolean silence)
    {
        this.originUUID = originUUID;
        this.originPos = originPos;
        this.silence = silence;
    }

    @Override
    public void write(FriendlyByteBuf buf)
    {
        buf.writeUtf(originUUID);
        buf.writeDouble(originPos.x);
        buf.writeDouble(originPos.y);
        buf.writeDouble(originPos.z);
        buf.writeBoolean(silence);
    }

    @Override
    public void read(FriendlyByteBuf buf)
    {
        this.originUUID = buf.readUtf();
        this.originPos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.silence = buf.readBoolean();
    }

    @OnlyIn(Dist.CLIENT)
    public static class TootsManager {
        public static HashMap<String, ItemSeliasetHorn.TootSoundInstance> tootMap = new HashMap<>();
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientExecute(PacketContext ctx)
    {


        var w = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;
        if(w != null && player != null) {
            if(tootMap.containsKey(originUUID)) {
                ItemSeliasetHorn.TootSoundInstance toot = tootMap.get(originUUID);
                if (silence)
                    toot.fadeOut();
                else if (!Minecraft.getInstance().getSoundManager().isActive(toot)) {
                    toot.originPos = originPos;
                    toot.controlledDuration = 10;
                    toot.fadeIn();
                    toot.setFade(0.02f);
                    Minecraft.getInstance().getSoundManager().play(toot);
                } else {
                    toot.controlledDuration = 10;
                    toot.originPos = originPos;
                }
            } else {
                ItemSeliasetHorn.TootSoundInstance toot = new ItemSeliasetHorn.TootSoundInstance(SoundsRegistry.SELI_HORN_BLOW.get(), originPos);
                tootMap.put(originUUID, toot);
                toot.fadeIn();
            }
        }
    }
}
