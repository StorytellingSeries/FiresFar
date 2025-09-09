package com.qurenie.relics_thirteenflames.content.items;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;

public class AuritekhArmor extends ArmorItem {
    
    public AuritekhArmor(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }
    
    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return true;
    }
    
    @Override
    public boolean supportsEnchantment(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        if (enchantment.is(Enchantments.UNBREAKING) || enchantment.is(Enchantments.MENDING))
            return false;
        return super.supportsEnchantment(stack, enchantment);
    }
    
}
