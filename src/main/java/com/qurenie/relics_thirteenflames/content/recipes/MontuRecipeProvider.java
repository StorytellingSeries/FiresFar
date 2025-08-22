package com.qurenie.relics_thirteenflames.content.recipes;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class MontuRecipeProvider extends RecipeProvider {
    
    public MontuRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }
    
    @Override
    public void buildRecipes(@NotNull RecipeOutput recipeOutput) {
        build(recipeOutput, ItemsRegistry.AURITEKH_INGOT, Ingredient.of(Items.NETHER_STAR),
                Ingredient.of(Items.NETHERITE_INGOT), Ingredient.of((Blocks.EMERALD_BLOCK)), 30);
        build(recipeOutput, ItemsRegistry.AURITEKH_HOE, Ingredient.of(ItemsRegistry.AURITEKH_INGOT),
                Ingredient.of(Items.NETHERITE_HOE), Ingredient.of(ItemsRegistry.AURITEKH_INGOT), 60);
        build(recipeOutput, ItemsRegistry.AURITEKH_PICKAXE, Ingredient.of(ItemsRegistry.AURITEKH_INGOT),
                Ingredient.of(Items.NETHERITE_PICKAXE), Ingredient.of(ItemsRegistry.AURITEKH_INGOT), 60);
        build(recipeOutput, ItemsRegistry.AURITEKH_AXE, Ingredient.of(ItemsRegistry.AURITEKH_INGOT),
                Ingredient.of(Items.NETHERITE_AXE), Ingredient.of(ItemsRegistry.AURITEKH_INGOT), 60);
        build(recipeOutput, ItemsRegistry.AURITEKH_SWORD, Ingredient.of(ItemsRegistry.AURITEKH_INGOT),
                Ingredient.of(Items.NETHERITE_SWORD), Ingredient.of(ItemsRegistry.AURITEKH_INGOT), 60);
        build(recipeOutput, ItemsRegistry.AURITEKH_LEGGINGS, Ingredient.of(ItemsRegistry.AURITEKH_INGOT),
                Ingredient.of(Items.NETHERITE_LEGGINGS), Ingredient.of(ItemsRegistry.AURITEKH_INGOT), 60);
        build(recipeOutput, ItemsRegistry.AURITEKH_SHOVEL, Ingredient.of(ItemsRegistry.AURITEKH_INGOT),
                Ingredient.of(Items.NETHERITE_SHOVEL), Ingredient.of(ItemsRegistry.AURITEKH_INGOT), 60);
        build(recipeOutput, ItemsRegistry.AURITEKH_CHESTPLATE, Ingredient.of(ItemsRegistry.AURITEKH_INGOT),
                Ingredient.of(Items.NETHERITE_CHESTPLATE), Ingredient.of(ItemsRegistry.AURITEKH_INGOT), 60);
        build(recipeOutput, ItemsRegistry.AURITEKH_BOOTS, Ingredient.of(ItemsRegistry.AURITEKH_INGOT),
                Ingredient.of(Items.NETHERITE_BOOTS), Ingredient.of(ItemsRegistry.AURITEKH_INGOT), 60);
        build(recipeOutput, ItemsRegistry.AURITEKH_HELMET, Ingredient.of(ItemsRegistry.AURITEKH_INGOT),
                Ingredient.of(Items.NETHERITE_HELMET), Ingredient.of(ItemsRegistry.AURITEKH_INGOT), 60);
    }
    
    public void build(@NotNull RecipeOutput recipeOutput, ItemLike result, Ingredient left, Ingredient center, Ingredient right, int experience) {
        recipeOutput.accept(ThirteenFlames.rl(BuiltInRegistries.ITEM.getKey(result.asItem()).getPath()),
                new MontuSmithRecipe(new ItemStack(result), experience, center, right, left), null);
    }
    
    
    
}
