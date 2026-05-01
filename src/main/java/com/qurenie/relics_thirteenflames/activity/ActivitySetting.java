package com.qurenie.relics_thirteenflames.activity;

import com.qurenie.relics_thirteenflames.activity.call.settings.IActivityCallSettings;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import it.hurts.octostudios.octolib.util.OctoColor;
import lombok.Builder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@Builder
public class ActivitySetting implements IActivitySetting {

    @Nullable
    protected OctoColor color;
    protected String name;
    protected BiFunction<ItemStack, LivingEntity, Integer> maxCooldown;
    @Builder.Default
    protected BiPredicate<ItemStack, LivingEntity> castCondition = (s, l) -> true;
    protected BiPredicate<ItemStack, LivingEntity> showBar;
    @Nullable
    @Builder.Default
    IActivityCallSettings callSettings = null;

    public ActivitySetting(String name, BiFunction<ItemStack, LivingEntity, Integer> maxCooldown, BiPredicate<ItemStack, LivingEntity> castCondition, BiPredicate<ItemStack, LivingEntity> showBar) {
        this.name = name;
        this.maxCooldown = maxCooldown;
        this.castCondition = castCondition;
        this.showBar = showBar == null ? this::defaultBarPredicate : showBar.and(this::defaultBarPredicate);
    }

    public ActivitySetting(@Nullable OctoColor color, String name,
                           BiFunction<ItemStack, LivingEntity, Integer> maxCooldown,
                           BiPredicate<ItemStack, LivingEntity> castCondition,
                           BiPredicate<ItemStack, LivingEntity> showBar,
                           @Nullable IActivityCallSettings callSettings) {
        this.name = name;
        this.maxCooldown = maxCooldown;
        this.castCondition = castCondition;
        this.color = color;
        this.callSettings = callSettings;
        this.showBar = showBar == null ? this::defaultBarPredicate : showBar.and(this::defaultBarPredicate);
    }

    protected boolean defaultBarPredicate(ItemStack s, LivingEntity l) {
        if (!s.has(ComponentRegistry.ACTIVITIES))
            return false;

        var activities = s.get(ComponentRegistry.ACTIVITIES);
        if (!activities.activities().containsKey(name))
            return false;

        int currentTime = (int) l.level().getGameTime();
        var a = activities.activities().get(name);

        return a.remains(currentTime) > 0;
    }

    @Override
    public int getMaxCooldown(LivingEntity player, ItemStack stack) {
        return maxCooldown.apply(stack, player);
    }

    @Override
    public boolean showBar(LivingEntity player, ItemStack stack) {
        return showBar.test(stack, player);
    }

    @Override
    public boolean castCondition(LivingEntity player, ItemStack stack) {
        return castCondition.test(stack, player);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @Nullable OctoColor getColor() {
        return color;
    }

    @Override
    public @Nullable IActivityCallSettings getCallSettings() {
        return callSettings;
    }

    public static ActivitySettingBuilder builder(String name) {
        ActivitySettingBuilder builder = new ActivitySettingBuilder();
        builder.name(name);
        return builder;
    }

    public static class ActivitySettingBuilder {

        public ActivitySettingBuilder maxCooldown(int cooldown) {
            this.maxCooldown((s, p) -> cooldown);
            return this;
        }

        public ActivitySettingBuilder maxCooldown(BiFunction<ItemStack, LivingEntity, Integer> maxCooldown) {
            this.maxCooldown = maxCooldown;
            return this;
        }

    }
}
