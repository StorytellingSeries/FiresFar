package com.qurenie.api;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface IExtRelicItem extends IRelicItem {

    default double getStatValue(@Nullable LivingEntity entity, ItemStack stack, String abilityName, String stat) {
        return getRelicData(entity, stack).getAbilitiesData().getAbilityData(abilityName).getStatData(stat).getValue();
    }

    default void addExperience(@Nullable LivingEntity entity, ItemStack stack, int xp) {
        getRelicData(entity, stack).getLevelingData().addExperience(xp);
    }

    default int getAbilityLevel(@Nullable LivingEntity entity, ItemStack stack, String abilityName) {
        return getRelicData(entity, stack).getAbilitiesData().getAbilityData(abilityName).getLevel();
    }

    default boolean isAbilityUnlocked(@Nullable LivingEntity entity, ItemStack stack, String abilityName) {
        return getRelicData(entity, stack).getAbilitiesData().getAbilityData(abilityName).isUnlocked();
    }

    default boolean isModEnabled(@Nullable LivingEntity entity, ItemStack stack, String abilityName, String mod) {
        return getRelicData(entity, stack).getAbilitiesData().getAbilityData(abilityName).getMode().equals(mod);
    }

    default boolean hasRangModifier(@Nullable LivingEntity entity, ItemStack stack, String abilityName, String rangModifier) {
        return getRelicData(entity, stack).getAbilitiesData().getAbilityData(abilityName).isRankModifierUnlocked(rangModifier);
    }

}
