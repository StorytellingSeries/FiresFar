package com.qurenie.api;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.enchanting.EnchantmentLevelSetEvent;

@Getter
public class EnchantCostEventPre extends Event {

    Player player;
    int originalLevel;
    @Setter
    int newLevel;
    
    public EnchantCostEventPre(Player player, int originalLevel) {
        this.player = player;
        this.originalLevel = originalLevel;
        this.newLevel = originalLevel;
    }
    
}
