package com.qurenie.relics_thirteenflames.net;

import com.qurenie.relics_thirteenflames.content.items.ItemRonasShield;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;

@MainThreaded
public class RhonasRebukePacket implements IPacket {

    public RhonasRebukePacket(){
    }
    
    @Override
    public void write(FriendlyByteBuf buf) {
        IPacket.super.write(buf);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        IPacket.super.read(buf);
    }

    @Override
    public void serverExecute(PacketContext ctx) {
        IPacket.super.serverExecute(ctx);
        ServerPlayer sender = ctx.getSender();
        var used = sender.getItemInHand(sender.getUsedItemHand());
        if(!(used.getItem() instanceof ItemRonasShield shit)) return;
        shit.rebuke(sender, used);
    }
}
