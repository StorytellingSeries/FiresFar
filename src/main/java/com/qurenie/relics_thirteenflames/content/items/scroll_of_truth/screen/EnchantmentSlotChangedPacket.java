package com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;

@MainThreaded
public class EnchantmentSlotChangedPacket implements IPacket {
    
    private ItemStack item;
    
    public EnchantmentSlotChangedPacket(ItemStack newItem) {
        this.item = newItem;
    }
    
    
    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        IPacket.super.write(buf);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, item);
    }
    
    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        IPacket.super.read(buf);
        this.item = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
    }
    
    @Override
    public void clientExecute(PacketContext ctx) {
        IPacket.super.clientExecute(ctx);
        if (Minecraft.getInstance().screen instanceof ScrollOfTruthContainerScreen scrollScreen) {
            scrollScreen.onItemChange(item);
        }
    }
    
}
