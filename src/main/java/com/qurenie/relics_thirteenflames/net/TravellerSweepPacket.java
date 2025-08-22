package com.qurenie.relics_thirteenflames.net;

import com.qurenie.relics_thirteenflames.content.items.ItemTravellerSword;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;

public class TravellerSweepPacket implements IPacket {
    
    private ItemStack sword;
    
    public TravellerSweepPacket(ItemStack sword){
        this.sword = sword;
    }
    
    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        ItemStack.STREAM_CODEC.encode(buf, sword);
    }
    
    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        this.sword = ItemStack.STREAM_CODEC.decode(buf);
    }
    
    @Override
    public void serverExecute(PacketContext ctx) {
        if (sword.getItem() instanceof ItemTravellerSword item)
            item.onSprintSweep(ctx.getSender(), sword);
    }
    
}
