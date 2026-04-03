package com.qurenie.api;

import com.qurenie.relics_thirteenflames.activity.ActivitiesData;
import com.qurenie.relics_thirteenflames.activity.ActivityData;
import com.qurenie.relics_thirteenflames.activity.IActivitySetting;
import com.qurenie.relics_thirteenflames.client.bar.BarSetting;
import com.qurenie.relics_thirteenflames.client.bar.IBarSetting;
import com.qurenie.relics_thirteenflames.data.TempData;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public interface IActivityContainer extends IBarContainer {

    SettingsContainer<IActivitySetting> constructActivitySettings();

    default NamedSettingsContainer<IActivitySetting> getActivitySettings() {
        return TempData.ACTIVITY_TEMPLATE.computeIfAbsent(
                this,
                key -> constructActivitySettings().named(IActivitySetting::getName)
        );
    }

    default int getCooldown(LivingEntity livingEntity, ItemStack stack, String activity) {
        var settings = getActivitySettings().getValues().get(activity);
        if (settings == null)
            throw new IllegalArgumentException("Activity " + activity + " does not exist");

        var activities = stack.getOrDefault(ComponentRegistry.ACTIVITIES, ActivitiesData.empty());
        var maxCooldown = settings.getMaxCooldown(livingEntity, stack);
        return Optional.ofNullable(activities.activities().get(activity)).map(a -> maxCooldown - a.recharge()).orElse(0);
    }

    default void addCooldown(LivingEntity livingEntity, ItemStack stack, String activity, int cooldown) {
        var settings = getActivitySettings().getValues().get(activity);
        if (settings == null)
            throw new IllegalArgumentException("Activity " + activity + " does not exist");

        var activities = stack.getOrDefault(ComponentRegistry.ACTIVITIES, ActivitiesData.empty());
        var maxCooldown = settings.getMaxCooldown(livingEntity, stack);
        stack.set(ComponentRegistry.ACTIVITIES, activities.computeIfPresent(activity, a -> a.with(Math.max(0, maxCooldown - cooldown))));
    }

    default void setMaxCooldown(LivingEntity livingEntity, ItemStack stack, String activity) {
        var settings = getActivitySettings().getValues().get(activity);
        if (settings == null)
            throw new IllegalArgumentException("Activity " + activity + " does not exist");

        var activities = stack.getOrDefault(ComponentRegistry.ACTIVITIES, ActivitiesData.empty());

        stack.set(ComponentRegistry.ACTIVITIES, activities.putOrComputeIfPresent(activity, new ActivityData(0), a -> a.with(0)));
    }

    default boolean canCast(LivingEntity living, ItemStack stack, String activity) {
        var settings = getActivitySettings().getValues().get(activity);
        if (settings == null)
            throw new IllegalArgumentException("Activity " + activity + " does not exist");

        return getActivitySettings().getValues().get(activity).castCondition(living, stack)
                && getCooldown(living, stack, activity) == 0;
    }

    default void tick(ItemStack stack, Player player) {
        if (!stack.has(ComponentRegistry.ACTIVITIES))
            return;

        var activities = stack.get(ComponentRegistry.ACTIVITIES);
        var settings = getActivitySettings();

        for (var entry : settings.getValues().entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();

            activities = activities.tick(key, value.getMaxCooldown(player, stack));
        }

        stack.set(ComponentRegistry.ACTIVITIES, activities);
    }

    default SettingsContainer<IBarSetting> constructActivityBarSettings() {
        var barSettings = constructBarSettings();

        var builder = SettingsContainer.<IBarSetting>builder();
        for (var entry : getActivitySettings().getValues().entrySet()) {

            var activity = entry.getValue();
            var key = entry.getKey();
            builder.setting(BarSetting.builder()
                    .color(activity.getColor())
                    .visibility((s, p) -> {
                        if (!s.has(ComponentRegistry.ACTIVITIES))
                            return false;

                        var activities = s.get(ComponentRegistry.ACTIVITIES);
                        return activities.activities().containsKey(key) && activity.showBar(p, s);
                    })
                    .value((s, p) -> {
                        var activities = s.get(ComponentRegistry.ACTIVITIES);
                        return (double) activities.activities().get(key).recharge();
                    })
                    .maxValue((s, p) -> (double) activity.getMaxCooldown(p, s))
                    .build());
        }

        return barSettings.merge(builder.build());
    }

    @Override
    default SettingsContainer<IBarSetting> constructBarSettings() {
        return SettingsContainer.<IBarSetting>builder().build();
    }

    @Override
    default SettingsContainer<IBarSetting> getBarSettings() {
        return TempData.BAR_TEMPLATES.computeIfAbsent(
                this,
                key -> constructActivityBarSettings()
        );
    }
}
