package com.qurenie.api.event;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

@Getter
public class BabySpawnCountEvent extends Event {
    
    private final Mob parentA;
    private final Mob parentB;
    private final Player causalPlayer;
    @Setter
    private int count;
    
    public BabySpawnCountEvent(Mob parentA, Mob parentB) {
        //causedByPlayer calculated here to simplify the patch.
        Player causalPlayer = null;
        if (parentA instanceof Animal) {
            causalPlayer = ((Animal) parentA).getLoveCause();
        }
        
        if (causalPlayer == null && parentB instanceof Animal) {
            causalPlayer = ((Animal) parentB).getLoveCause();
        }
        
        this.parentA = parentA;
        this.parentB = parentB;
        this.causalPlayer = causalPlayer;
    }
    
}
