package com.qurenie.jei;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.recipes.MontuSmithRecipe;
import com.qurenie.relics_thirteenflames.init.RecipeTypesRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JeiPlugin
public class FlamesJeiPlugin implements IModPlugin {
    
    private static final ResourceLocation ID = ThirteenFlames.rl("jei_plugin");
    
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ID;
    }
    
    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        
        registration.addRecipeCategories(new MontuGlovesRecipeCategory(guiHelper));
    }
    
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Minecraft mc = Minecraft.getInstance();
        RecipeManager recipeManager = mc.level.getRecipeManager();
        
        List<MontuSmithRecipe> recipes = recipeManager
                .getAllRecipesFor(RecipeTypesRegistry.MONTU_SMITH_TYPE.get())
                .stream()
                .map(RecipeHolder::value) // Распаковываем холдер
                .flatMap(r -> Stream.of(r, r.swap()))
                .collect(Collectors.toList());
        
        registration.addRecipes(JEITypeRegistry.MONTU_SMITH_TYPE, recipes);
    }
    
}
