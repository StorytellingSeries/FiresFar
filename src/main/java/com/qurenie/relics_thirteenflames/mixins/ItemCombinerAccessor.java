package com.qurenie.relics_thirteenflames.mixins;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ItemCombinerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemCombinerMenu.class)
public interface ItemCombinerAccessor {
    
    @Accessor(value = "player", remap = false)
    Player getPlayer();
    
}
