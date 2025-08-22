package com.qurenie.relics_thirteenflames.content.items.misc;

import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class AuritekhTier implements Tier {
    
    public static final AuritekhTier INSTANCE = new AuritekhTier();
    
    private AuritekhTier() {
    }
    
    @Override
    public int getUses() {
        return 8098;
    }
    
    @Override
    public float getSpeed() {
        return 15f;
    }
    
    @Override
    public float getAttackDamageBonus() {
        return 7.0f;
    }
    
    @Override
    public @NotNull TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
    }
    
    @Override
    public int getEnchantmentValue() {
        return 22;
    }
    
    @Override
    public @NotNull Ingredient getRepairIngredient() {
        return Ingredient.of(ItemsRegistry.AURITEKH_INGOT);
    }
    
}
