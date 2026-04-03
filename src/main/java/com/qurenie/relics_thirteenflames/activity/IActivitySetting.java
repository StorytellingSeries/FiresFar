package com.qurenie.relics_thirteenflames.activity;

import com.qurenie.relics_thirteenflames.activity.call.settings.IActivityCallSettings;
import it.hurts.octostudios.octolib.util.OctoColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface IActivitySetting {

    int getMaxCooldown(LivingEntity player, ItemStack stack);

    boolean castCondition(LivingEntity player, ItemStack stack);

    String getName();

    boolean showBar(LivingEntity player, ItemStack stack);

    @Nullable
    OctoColor getColor();

    @Nullable
    IActivityCallSettings getCallSettings();

}
