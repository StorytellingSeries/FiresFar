package com.qurenie.relics_thirteenflames.content.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.qurenie.relics_thirteenflames.init.RecipeTypesRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.qurenie.relics_thirteenflames.init.RecipeSerializersRegistry.MONTU_SMITH_SERIALIZER;

public record MontuSmithRecipe(ItemStack result, int experience, Ingredient center, Ingredient right, Ingredient left,
                               boolean canSwap) implements Recipe<MontuRecipeInput> {

    public MontuSmithRecipe(ItemStack result, int experience, Ingredient center, Ingredient right, Ingredient left) {
        this(result, experience, center, right, left, true);
    }

    public MontuSmithRecipe swap() {
        return new MontuSmithRecipe(result, experience, center, left, right, canSwap);
    }

    @Override
    public ItemStack result() {
        return result.copy();
    }

    @Override
    public boolean matches(@NotNull MontuRecipeInput input, @NotNull Level level) {
        return center.test(input.center()) && (right.test(input.right()) && left.test(input.left())
                || canSwap && left.test(input.right()) && right.test(input.left()));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull MontuRecipeInput input, HolderLookup.@NotNull Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return result;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return MONTU_SMITH_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return RecipeTypesRegistry.MONTU_SMITH_TYPE.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MontuSmithRecipe) obj;
        return Objects.equals(this.result, that.result) &&
                this.experience == that.experience &&
                Objects.equals(this.center, that.center) &&
                Objects.equals(this.right, that.right) &&
                Objects.equals(this.left, that.left);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, experience, center, right, left);
    }

    @Override
    public @NotNull String toString() {
        return "MontuSmithRecipe[" +
                "result=" + result + ", " +
                "experience=" + experience + ", " +
                "center=" + center + ", " +
                "right=" + right + ", " +
                "left=" + left + ']';
    }


    public static class Serializer implements RecipeSerializer<MontuSmithRecipe> {

        @Override
        public @NotNull MapCodec<MontuSmithRecipe> codec() {
            return RecordCodecBuilder.mapCodec(inst -> inst.group(
                    ItemStack.CODEC.fieldOf("result").forGetter(MontuSmithRecipe::result),
                    Codec.INT.fieldOf("exp").forGetter(MontuSmithRecipe::experience),
                    Ingredient.CODEC.fieldOf("center").forGetter(MontuSmithRecipe::center),
                    Ingredient.CODEC.fieldOf("right").forGetter(MontuSmithRecipe::right),
                    Ingredient.CODEC.fieldOf("left").forGetter(MontuSmithRecipe::left),
                    Codec.BOOL.optionalFieldOf("canSwap", true).forGetter(MontuSmithRecipe::canSwap)
            ).apply(inst, MontuSmithRecipe::new));
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, MontuSmithRecipe> streamCodec() {
            return StreamCodec.of(this::toNetwork, this::fromNetwork);
        }

        private MontuSmithRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            int experience = buffer.readInt();
            Ingredient center = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            Ingredient right = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            Ingredient left = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            boolean canSwap = buffer.readBoolean();
            return new MontuSmithRecipe(result, experience, center, right, left, canSwap);
        }

        private void toNetwork(RegistryFriendlyByteBuf buffer, MontuSmithRecipe recipe) {
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeInt(recipe.experience);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.center);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.right);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.left);
            buffer.writeBoolean(recipe.canSwap);
        }

    }

}
