package com.qurenie.relics_thirteenflames.activity.call.settings;

import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface SlotReference {

    void encode(FriendlyByteBuf buf);

    void decode(FriendlyByteBuf buf);

    ItemStack getStack(Player player);

    boolean correctReference(Player player, ItemStack stack);

    default boolean verifyReference(Player player, ItemStack stack) {
        ItemStack that = getStack(player);
        return FlamesUtils.getOrCreateUUID(that).equals(FlamesUtils.getOrCreateUUID(stack));
    }

}
