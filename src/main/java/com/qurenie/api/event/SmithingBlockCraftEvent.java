package com.qurenie.api.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class SmithingBlockCraftEvent extends PlayerEvent {
    
    final ItemStack crafted;
    
    public SmithingBlockCraftEvent(Player player, ItemStack crafted) {
        super(player);
        this.crafted = crafted;
    }
    
}
