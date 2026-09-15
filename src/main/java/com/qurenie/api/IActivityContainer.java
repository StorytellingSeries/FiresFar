package com.qurenie.api;

import com.qurenie.relics_thirteenflames.activity.ActivitiesData;
import com.qurenie.relics_thirteenflames.activity.ActivityData;
import com.qurenie.relics_thirteenflames.activity.IActivitySetting;
import com.qurenie.relics_thirteenflames.activity.call.settings.RelicsActivityCallSettings;
import com.qurenie.relics_thirteenflames.client.bar.BarSetting;
import com.qurenie.relics_thirteenflames.client.bar.IBarSetting;
import com.qurenie.relics_thirteenflames.data.TempData;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import com.qurenie.relics_thirteenflames.init.KeyBindRegistry;
import it.hurts.sskirillss.relics.client.screen.description.misc.TextJustificator;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public interface IActivityContainer extends IBarContainer, IRelicDescriptor {

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

        int currentTime = (int) livingEntity.level().getGameTime();
        var activities = stack.getOrDefault(ComponentRegistry.ACTIVITIES, ActivitiesData.empty());

        return Optional.ofNullable(activities.activities().get(activity)).map(a -> a.remains(currentTime)).orElse(0);
    }

    default void addCooldown(LivingEntity livingEntity, ItemStack stack, String activity, int cooldown) {
        var settings = getActivitySettings().getValues().get(activity);
        if (settings == null)
            throw new IllegalArgumentException("Activity " + activity + " does not exist");

        int currentTime = (int) livingEntity.level().getGameTime();
        var activities = stack.getOrDefault(ComponentRegistry.ACTIVITIES, ActivitiesData.empty());

        int reloadTime = cooldown + currentTime;

        stack.set(ComponentRegistry.ACTIVITIES, activities.computeIfPresent(activity, a -> a.with(Math.max(0, reloadTime))));
    }

    default void setMaxCooldown(LivingEntity livingEntity, ItemStack stack, String activity) {
        var settings = getActivitySettings().getValues().get(activity);
        if (settings == null)
            throw new IllegalArgumentException("Activity " + activity + " does not exist");

        int currentTime = (int) livingEntity.level().getGameTime();
        var activities = stack.getOrDefault(ComponentRegistry.ACTIVITIES, ActivitiesData.empty());

        int maxCooldown = settings.getMaxCooldown(livingEntity, stack);
        int reloadTime = maxCooldown + currentTime;

        stack.set(ComponentRegistry.ACTIVITIES, activities.putOrComputeIfPresent(activity, new ActivityData(reloadTime), a -> a.with(reloadTime)));
    }

    default boolean canCast(LivingEntity living, ItemStack stack, String activity) {
        var settings = getActivitySettings().getValues().get(activity);
        if (settings == null)
            throw new IllegalArgumentException("Activity " + activity + " does not exist");

        return getActivitySettings().getValues().get(activity).castCondition(living, stack)
                && getCooldown(living, stack, activity) == 0;
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
                        int currentTime = (int) p.level().getGameTime();

                        var a = activities.activities().get(key);
                        return (double) a.remains(currentTime);
                    })
                    .maxValue((s, p) -> (double) activity.getMaxCooldown(p, s))
                    .inverse(true)
                    .build());
        }

        return builder.build().merge(barSettings);
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

    @Override
    default void modifyDescription(Player player, ItemStack stack, String ability, List<TextJustificator.LineEntry> rawLines, List<MutableComponent> dynamicComponents) {
        if (stack.getItem() instanceof IActivityContainer container) {
            var activity = container.getActivitySettings().get(ability);
            if (activity == null || !(activity.getCallSettings() instanceof RelicsActivityCallSettings))
                return;

            Minecraft mc = Minecraft.getInstance();
            float delta = mc.getTimer().getGameTimeDeltaTicks();

            float hue = ((mc.level.getGameTime() + delta) % 400f) / 400f;
            int rgb = Color.HSBtoRGB(hue, 1.0f, 0.7f);

            var button = KeyBindRegistry.ACTIVITY_KEY.getKey().getDisplayName().copy()
                    .withStyle(Style.EMPTY.withColor(rgb).withBold(true));
            dynamicComponents.add(button);

            var description = Component.translatable("thirteen_flames.relics.call.keybind.description", "%" + dynamicComponents.size() + "$s");
            rawLines.addFirst(new TextJustificator.LineEntry(description, false));
        }
    }
}
