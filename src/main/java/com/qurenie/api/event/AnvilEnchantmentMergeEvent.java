package com.qurenie.api.event;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

@Getter
public class AnvilEnchantmentMergeEvent extends Event {

    Player player;

    ItemStack enchantmentBook;
    ItemStack result;

    @Setter
    boolean bookTaken = true;

    public AnvilEnchantmentMergeEvent(Player player, ItemStack enchantmentBook, ItemStack result) {
        this.player = player;
        this.enchantmentBook = enchantmentBook;
        this.result = result;
    }
}
