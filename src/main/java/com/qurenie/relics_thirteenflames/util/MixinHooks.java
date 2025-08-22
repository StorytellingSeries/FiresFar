package com.qurenie.relics_thirteenflames.util;

import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import lombok.experimental.UtilityClass;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@UtilityClass
public class MixinHooks {
    
    public static boolean isEntityTravellerBoosted(Entity entity) {
        if (!(entity instanceof Player p))
            return false;
        
        float speed = 0;
        if (p.getMainHandItem().is(ItemsRegistry.TRAVELLER_SWORD))
            speed = p.getMainHandItem().getOrDefault(ComponentRegistry.SPEED, 0f);
        if (p.getOffhandItem().is(ItemsRegistry.TRAVELLER_SWORD))
            speed += p.getOffhandItem().getOrDefault(ComponentRegistry.SPEED, 0f);
        return speed > 2.5;
    }
    
}
