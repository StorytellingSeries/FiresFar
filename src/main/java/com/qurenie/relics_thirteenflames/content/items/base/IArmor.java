package com.qurenie.relics_thirteenflames.content.items.base;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public interface IArmor {
    
    @NotNull Holder<ArmorMaterial> getMaterial();
    
    @NotNull ArmorItem.Type getType();
    
    @NotNull Item self();
    
}
