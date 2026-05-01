package com.qurenie.relics_thirteenflames.client.bar;

import it.hurts.octostudios.octolib.util.OctoColor;
import lombok.Builder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

@Builder
public class BarSetting implements IBarSetting {

    BiFunction<ItemStack, Player, Double> maxValue;
    @Builder.Default
    BiFunction<ItemStack, Player, Double> value = (i, player) -> 0d;
    @Builder.Default
    BiFunction<ItemStack, Player, Boolean> visibility = (i, player) -> true;
    @Builder.Default
    @Nullable
    OctoColor color = null;
    @Builder.Default
    boolean inverse = false;

    BarSetting(BiFunction<ItemStack, Player, Double> maxValue, BiFunction<ItemStack, Player, Double> value, BiFunction<ItemStack, Player, Boolean> visibility, @Nullable OctoColor color, boolean inverse) {
        this.maxValue = maxValue;
        this.value = value;
        this.visibility = visibility;
        this.color = color;
        this.inverse = inverse;
    }

    @Override
    public boolean inverse() {
        return inverse;
    }

    @Override
    public double getMaxValue(ItemStack stack, Player player) {
        return maxValue.apply(stack, player);
    }

    @Override
    public double getValue(ItemStack stack, Player player) {
        return value.apply(stack, player);
    }

    @Override
    public boolean isBarVisible(ItemStack stack, Player player) {
        return visibility.apply(stack, player);
    }

    @Override
    @NotNull
    public OctoColor getColor(int index) {
        return color == null ? getBaseColor(index) : color;
    }

    public OctoColor getBaseColor(int index) {
        return switch (index % 3) {
            case 0 -> new OctoColor(0xff337dff);
            case 1 -> new OctoColor(0xffFF2B2B);
            case 2 -> new OctoColor(0xff228B22);
            default -> throw new IllegalStateException();
        };
    }
}
