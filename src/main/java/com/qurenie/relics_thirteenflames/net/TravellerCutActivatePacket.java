package com.qurenie.relics_thirteenflames.net;

import com.qurenie.relics_thirteenflames.content.items.ItemTravellerSword;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.ACTIVE_TICK;

public class TravellerCutActivatePacket implements IPacket {

    private ItemStack sword;

    public TravellerCutActivatePacket(ItemStack sword){
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
            sword.set(ACTIVE_TICK, 400);
    }

}
