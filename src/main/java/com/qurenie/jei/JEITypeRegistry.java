package com.qurenie.jei;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.recipes.MontuSmithRecipe;
import mezz.jei.api.recipe.RecipeType;

public interface JEITypeRegistry {
    
    RecipeType<MontuSmithRecipe> MONTU_SMITH_TYPE = RecipeType.create(ThirteenFlames.MODID, "smith_recipe", MontuSmithRecipe.class);

}
