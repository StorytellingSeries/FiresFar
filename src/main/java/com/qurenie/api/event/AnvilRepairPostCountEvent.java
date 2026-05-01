package com.qurenie.api.event;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

@Getter
public class AnvilRepairPostCountEvent extends Event {

    ItemStack stack;
    Player player;
    @Setter
    int repairItemCountCost;

    public AnvilRepairPostCountEvent(ItemStack stack, Player player, int repairItemCountCost) {
        this.stack = stack;
        this.player = player;
        this.repairItemCountCost = repairItemCountCost;
    }
}
