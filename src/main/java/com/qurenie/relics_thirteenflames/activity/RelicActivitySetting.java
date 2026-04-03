package com.qurenie.relics_thirteenflames.activity;

import com.qurenie.relics_thirteenflames.activity.call.settings.IActivityCallSettings;
import it.hurts.octostudios.octolib.util.OctoColor;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import lombok.Builder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class RelicActivitySetting extends ActivitySetting {

    RelicActivitySetting(OctoColor color, String name, BiFunction<ItemStack, LivingEntity, Integer> maxCooldown,
                         BiPredicate<ItemStack, LivingEntity> castCondition,
                         BiPredicate<ItemStack, LivingEntity> showBar,
                         @Nullable IActivityCallSettings callSettings) {
        super(color, name, maxCooldown, castCondition, showBar, callSettings);
    }

    public static RelicActivitySettingBuilder builderRelic(String ability, String stat) {
        return new RelicActivitySettingBuilder()
                .name(ability)
                .maxCooldown((s, p) -> {
                    if (!(s.getItem() instanceof IRelicItem item))
                        throw new IllegalArgumentException("Relic items must be of type IRelicItem");

                    return (int) item.getRelicData(p, s).getAbilitiesData().getAbilityData(ability).getStatData(stat).getValue();
                });
    }

    public static RelicActivitySettingBuilder builderRelic(String ability) {
        return builderRelic(ability, "recharge");
    }

    public static class RelicActivitySettingBuilder {

        @Nullable
        private OctoColor color;
        @Nullable
        IActivityCallSettings callSettings;
        private int startingCastLevel = -1;
        private String name = "unspecified";
        private BiFunction<ItemStack, LivingEntity, Integer> maxCooldown = (s, p) -> 0;
        private BiPredicate<ItemStack, LivingEntity> castCondition = (s, p) -> true;
        private BiPredicate<ItemStack, LivingEntity> showBar = (s, p) -> true;

        public RelicActivitySettingBuilder color(@Nullable OctoColor color) {
            this.color = color;
            return this;
        }

        public RelicActivitySettingBuilder name(String name) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("name cannot be null or empty");
            }
            this.name = name;
            return this;
        }

        public RelicActivitySettingBuilder startingCastLevel(int level) {
            if (level < 0) {
                throw new IllegalArgumentException("level cannnot be negative");
            }
            this.startingCastLevel = level;
            return this;
        }

        public RelicActivitySettingBuilder maxCooldown(int maxCooldown) {
            if (maxCooldown < 0) {
                throw new IllegalArgumentException("maxCooldown cannot be negative");
            }
            this.maxCooldown = (_1, _2) -> maxCooldown;
            return this;
        }

        public RelicActivitySettingBuilder maxCooldown(BiFunction<ItemStack, LivingEntity, Integer> maxCooldown) {
            if (maxCooldown == null) {
                throw new IllegalArgumentException("maxCooldown cannot be null");
            }
            this.maxCooldown = maxCooldown;
            return this;
        }

        public RelicActivitySettingBuilder callSettings(@Nullable IActivityCallSettings callSettings) {
            this.callSettings = callSettings;
            return this;
        }

        public RelicActivitySettingBuilder castCondition(BiPredicate<ItemStack, LivingEntity> castCondition) {
            if (castCondition == null) {
                throw new IllegalArgumentException("castCondition cannot be null");
            }
            this.castCondition = castCondition;
            return this;
        }

        public RelicActivitySettingBuilder showBar(BiPredicate<ItemStack, LivingEntity> predicate) {
            if (showBar == null) {
                throw new IllegalArgumentException("castCondition cannot be null");
            }
            this.showBar = predicate;
            return this;
        }

        public RelicActivitySetting build() {
            return new RelicActivitySetting(color, name, maxCooldown, castCondition.and((s, p) -> {
                if (!(s.getItem() instanceof IRelicItem))
                    throw new IllegalArgumentException("Relic items must be of type IRelicItem");

                var abilityData = ((IRelicItem) s.getItem()).getRelicData(p, s)
                        .getAbilitiesData()
                        .getAbilityData(name);

                if (abilityData == null)
                    throw new IllegalArgumentException("Relic has not " + name + " ability.");

                return abilityData.isUnlocked() && abilityData.getLevel() >= startingCastLevel;
            }), showBar, callSettings);
        }
    }
}
