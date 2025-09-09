package com.qurenie.api;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

@Getter
@Setter
public class AnvilUpdatePostEvent extends Event {
    
    private int cost;
    private final Player player;
    
    public AnvilUpdatePostEvent(int cost, Player player) {
        this.cost = cost;
        this.player = player;
    }
    
    
    
}
