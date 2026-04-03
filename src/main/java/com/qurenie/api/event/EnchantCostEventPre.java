package com.qurenie.api.event;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

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
