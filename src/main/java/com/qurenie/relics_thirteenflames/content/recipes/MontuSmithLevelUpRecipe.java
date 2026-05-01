package com.qurenie.relics_thirteenflames.content.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.qurenie.relics_thirteenflames.init.RecipeTypesRegistry;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.qurenie.relics_thirteenflames.init.RecipeSerializersRegistry.MONTU_SMITH_LEVEL_UP_SERIALIZER;
import static com.qurenie.relics_thirteenflames.init.RecipeSerializersRegistry.MONTU_SMITH_SERIALIZER;

public record MontuSmithLevelUpRecipe(Ingredient right, Ingredient left, boolean canSwap)
        implements Recipe<MontuRecipeInput> {

    public MontuSmithLevelUpRecipe(Ingredient right, Ingredient left) {
        this(right, left, true);
    }

    public MontuSmithLevelUpRecipe swap() {
        return new MontuSmithLevelUpRecipe(left, right, canSwap);
    }

    @Override
    public boolean matches(@NotNull MontuRecipeInput input, @NotNull Level level) {
        return (input.center().getItem() instanceof IRelicItem) && (right.test(input.right()) && left.test(input.left())
                || canSwap && left.test(input.right()) && right.test(input.left()));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull MontuRecipeInput input, HolderLookup.@NotNull Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return MONTU_SMITH_LEVEL_UP_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return RecipeTypesRegistry.MONTU_SMITH_LEVEL_UP_TYPE.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MontuSmithLevelUpRecipe) obj;
        return Objects.equals(this.right, that.right) &&
                Objects.equals(this.left, that.left);
    }

    @Override
    public int hashCode() {
        return Objects.hash(right, left);
    }

    public static class Serializer implements RecipeSerializer<MontuSmithLevelUpRecipe> {

        @Override
        public @NotNull MapCodec<MontuSmithLevelUpRecipe> codec() {
            return RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Ingredient.CODEC.fieldOf("right").forGetter(MontuSmithLevelUpRecipe::right),
                    Ingredient.CODEC.fieldOf("left").forGetter(MontuSmithLevelUpRecipe::left),
                    Codec.BOOL.optionalFieldOf("canSwap", true).forGetter(MontuSmithLevelUpRecipe::canSwap)
            ).apply(inst, MontuSmithLevelUpRecipe::new));
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, MontuSmithLevelUpRecipe> streamCodec() {
            return StreamCodec.of(this::toNetwork, this::fromNetwork);
        }

        private MontuSmithLevelUpRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            Ingredient right = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            Ingredient left = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            boolean canSwap = buffer.readBoolean();
            return new MontuSmithLevelUpRecipe(right, left, canSwap);
        }

        private void toNetwork(RegistryFriendlyByteBuf buffer, MontuSmithLevelUpRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.right);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.left);
            buffer.writeBoolean(recipe.canSwap);
        }

    }
}
