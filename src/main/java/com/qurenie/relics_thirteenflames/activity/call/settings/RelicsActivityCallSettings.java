package com.qurenie.relics_thirteenflames.activity.call.settings;

import com.qurenie.relics_thirteenflames.activity.call.CallInput;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@Builder
@AllArgsConstructor
public class RelicsActivityCallSettings implements IActivityCallSettings {

    String ability;
    @Builder.Default
    InventoryType inventoryType = InventoryType.INVENTORY;
    @Builder.Default
    BiPredicate<LivingEntity, ItemStack> visibility = null;
    BiFunction<LivingEntity, ItemStack, ActivityResult> cast;
    @Builder.Default
    TriConsumer<LivingEntity, ItemStack, SelectionContext> selectionNotify = (e, s, context) -> {};


    public static RelicsActivityCallSettingsBuilder builder(String ability) {
        RelicsActivityCallSettingsBuilder b = new RelicsActivityCallSettingsBuilder();
        b.visibility$value = (p, s) -> {
            if (!(s.getItem() instanceof IRelicItem))
                throw new IllegalArgumentException("Relic items must be of type IRelicItem");

            var abilityData = ((IRelicItem) s.getItem()).getRelicData(p, s)
                    .getAbilitiesData()
                    .getAbilityData(ability);

            if (abilityData == null)
                throw new IllegalArgumentException("Relic has not " + ability + " ability.");

            return abilityData.isUnlocked();
        };
        b.visibility$set = true;
        return b.ability(ability);
    }

    @Override
    public ResourceLocation getResourceLocation(LivingEntity entity, ItemStack itemStack) {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID,
                String.format("textures/abilities/%s/%s.png", BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getPath(), ability));
    }

    @Override
    public boolean isVisible(LivingEntity entity, ItemStack itemStack) {
        return visibility.test(entity, itemStack);
    }

    @Override
    public ActivityResult cast(LivingEntity entity, ItemStack itemStack) {
        return cast.apply(entity, itemStack);
    }

    @Override
    public InventoryType getInventoryType() {
        return inventoryType;
    }

    @Override
    public void selectionNotify(LivingEntity entity, ItemStack itemStack, SelectionContext context) {
        selectionNotify.accept(entity, itemStack, context);
    }

    public static class RelicsActivityCallSettingsBuilder {

        public RelicsActivityCallSettingsBuilder minVisibilityLevel(int level) {
            if (!visibility$set) {
                visibility$value = predicateMinLvl(level);
                visibility$set = true;
            } else
                visibility$value = visibility$value.and(predicateMinLvl(level));

            return this;
        }

        public RelicsActivityCallSettingsBuilder visibility(BiPredicate<LivingEntity, ItemStack> visibility) {
            if (!visibility$set) {
                visibility$value = visibility;
                visibility$set = true;
            } else
                visibility$value = visibility$value.and(visibility);

            return this;
        }

        private BiPredicate<LivingEntity, ItemStack> predicateMinLvl(int level) {
            return (p, s) -> {
                if (!(s.getItem() instanceof IRelicItem))
                    throw new IllegalArgumentException("Relic items must be of type IRelicItem");

                if (level < 0)
                    return true;

                var abilityData = ((IRelicItem) s.getItem()).getRelicData(p, s)
                        .getAbilitiesData()
                        .getAbilityData(ability);

                if (abilityData == null)
                    throw new IllegalArgumentException("Relic has not " + ability + " ability.");

                return abilityData.isUnlocked() && abilityData.getLevel() >= level;
            };
        }

    }
}
