package com.qurenie.relics_thirteenflames.net;

import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.MONTU_AOE;

@MainThreaded
public class HammerAOEChangePacket implements IPacket {

    private int delta;
    private int maxAOE;
    public HammerAOEChangePacket(int delta, int maxAOE){
        this.delta = delta;
        this.maxAOE = maxAOE;
    }


    @Override
    public void write(FriendlyByteBuf buf) {
        IPacket.super.write(buf);
        buf.writeInt(delta);
        buf.writeInt(maxAOE);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        IPacket.super.read(buf);
        this.delta = buf.readInt();
        this.maxAOE = buf.readInt();
    }

    @Override
    public void serverExecute(PacketContext ctx) {
        IPacket.super.serverExecute(ctx);
        ServerPlayer sender = ctx.getSender();
        ItemStack item = sender.getMainHandItem();
        if (item.is(ItemsRegistry.MONTU_HAMMER)){
            int aoe = item.getComponents().getOrDefault(MONTU_AOE.get(), 0);
            int newAOE;
            if (aoe + delta >= 0) {
                newAOE = (aoe + delta) % (maxAOE + 1);
            }else{
                newAOE = maxAOE;
            }
            item.set(MONTU_AOE.get(), newAOE);
        }
    }
}
