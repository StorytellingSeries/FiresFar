package com.qurenie.relics_thirteenflames.client.bar;

import it.hurts.octostudios.octolib.util.OctoColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IBarSetting {

    double getMaxValue(ItemStack stack, Player player);

    double getValue(ItemStack stack, Player player);

    boolean isBarVisible(ItemStack stack, Player player);

    OctoColor getColor(int index);

}
