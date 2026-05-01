package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.recipes.MontuSmithLevelUpRecipe;
import com.qurenie.relics_thirteenflames.content.recipes.MontuSmithRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface RecipeTypesRegistry {
    
    DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, ThirteenFlames.MODID);
    
    DeferredHolder<RecipeType<?>, RecipeType<MontuSmithRecipe>> MONTU_SMITH_TYPE = register("montu_smith");
    DeferredHolder<RecipeType<?>, RecipeType<MontuSmithLevelUpRecipe>> MONTU_SMITH_LEVEL_UP_TYPE = register("montu_smith_level_up");

    static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> register(final String identifier) {
        return RECIPE_TYPES.register(identifier, () -> new RecipeType<>() {
            @Override
            public String toString() {
                return identifier;
            }
        });
    }
    
    static void register(IEventBus bus) {
        RECIPE_TYPES.register(bus);
    }
    
}
