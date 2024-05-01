package com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;

@MainThreaded
public class EnchantmentSlotChangedPacket implements IPacket {

    private ItemStack item;

    public EnchantmentSlotChangedPacket(ItemStack newItem){
        this.item = newItem;
    }


    @Override
    public void write(FriendlyByteBuf buf) {
        IPacket.super.write(buf);
        buf.writeItem(item);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        IPacket.super.read(buf);
        this.item = buf.readItem();
    }

    @Override
    public void clientExecute(PacketContext ctx) {
        IPacket.super.clientExecute(ctx);
        if (Minecraft.getInstance().screen instanceof ScrollOfTruthContainerScreen scrollScreen){
            scrollScreen.onItemChange(item);
        }
    }
}
