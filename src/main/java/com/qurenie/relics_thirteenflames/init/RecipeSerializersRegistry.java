package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.recipes.MontuSmithLevelUpRecipe;
import com.qurenie.relics_thirteenflames.content.recipes.MontuSmithRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface RecipeSerializersRegistry {
    
    DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, ThirteenFlames.MODID);
    
    DeferredHolder<RecipeSerializer<?>, MontuSmithRecipe.Serializer> MONTU_SMITH_SERIALIZER = RECIPE_SERIALIZERS.register("montu_smith", MontuSmithRecipe.Serializer::new);
    DeferredHolder<RecipeSerializer<?>, MontuSmithLevelUpRecipe.Serializer> MONTU_SMITH_LEVEL_UP_SERIALIZER = RECIPE_SERIALIZERS.register("montu_smith_level_up", MontuSmithLevelUpRecipe.Serializer::new);

    static void register(IEventBus bus) {
        RECIPE_SERIALIZERS.register(bus);
    }
    
}
