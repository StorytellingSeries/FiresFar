package com.qurenie.relics_thirteenflames.net;

import com.qurenie.relics_thirteenflames.content.items.ItemTravellerSword;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;

public class TravellerCutPacket implements IPacket {

    private InteractionHand hand;

    public TravellerCutPacket(InteractionHand hand) {
        this.hand = hand;
    }

    public TravellerCutPacket() {
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(hand);
    }

    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        this.hand = buf.readEnum(InteractionHand.class);
    }

    @Override
    public void serverExecute(PacketContext ctx) {
        var player = ctx.getSender();
        if (player == null) return;

        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof ItemTravellerSword item) {
            item.onSwordCut(player, stack);
        }
    }
}