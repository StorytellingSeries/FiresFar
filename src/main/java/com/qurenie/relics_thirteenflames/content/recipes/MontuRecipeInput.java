package com.qurenie.relics_thirteenflames.content.recipes;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

public record MontuRecipeInput(ItemStack center, ItemStack right, ItemStack left) implements RecipeInput {
    
    @Override
    public @NotNull ItemStack getItem(int p_346205_) {
        return switch (p_346205_) {
            case 0 -> this.center;
            case 1 -> this.right;
            case 2 -> this.left;
            default -> throw new IllegalArgumentException("Recipe does not contain slot " + p_346205_);
        };
    }
    
    @Override
    public int size() {
        return 3;
    }
    
    @Override
    public boolean isEmpty() {
        return this.center.isEmpty() && this.right.isEmpty() && this.left.isEmpty();
    }
    
}
