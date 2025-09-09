package com.qurenie.jei;

import com.qurenie.relics_thirteenflames.content.recipes.MontuSmithRecipe;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class MontuGlovesRecipeCategory implements IRecipeCategory<MontuSmithRecipe> {
    
    private final IDrawable icon;
    
    public MontuGlovesRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemsRegistry.MONTU_GLOVES));
    }
    
    @Override
    public int getWidth() {
        return 100;
    }
    
    @Override
    public int getHeight() {
        return 44;
    }
    
    @Override
    public @NotNull RecipeType<MontuSmithRecipe> getRecipeType() {
        return JEITypeRegistry.MONTU_SMITH_TYPE;
    }
    
    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.thirteenflames.montu_smith");
    }
    
    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }
    
    @Override
    public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder, @NotNull MontuSmithRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addRecipePlusSign().setPosition(26, 5);
        builder.addRecipePlusSign().setPosition(63, 5);
        IRecipeCategory.super.createRecipeExtras(builder, recipe, focuses);
    }
    
    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull MontuSmithRecipe recipe, @NotNull IFocusGroup focuses) {
        List<ItemStack> right = Arrays.asList(recipe.right().getItems());
        List<ItemStack> left = Arrays.asList(recipe.left().getItems());
        
        builder.addSlot(RecipeIngredientRole.INPUT, 6, 4)
                .addIngredients(VanillaTypes.ITEM_STACK, left)
                .setStandardSlotBackground();
        
        builder.addSlot(RecipeIngredientRole.INPUT, 43, 4)
                .addIngredients(recipe.center())
                .setStandardSlotBackground();
        
        builder.addSlot(RecipeIngredientRole.INPUT, 80, 4)
                .addIngredients(VanillaTypes.ITEM_STACK, right)
                .setStandardSlotBackground();
        
        // Выходной слот (под центральным входом)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 43, 26)
                .addItemStack(recipe.result())
                .setStandardSlotBackground();
    }
}
