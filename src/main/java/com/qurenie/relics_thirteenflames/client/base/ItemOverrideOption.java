package com.qurenie.relics_thirteenflames.client.base;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class ItemOverrideOption<T> {
    
    final DataComponentType<T> componentType;
    final Set<BakedModel> defaultModels;
    final List<Pair<Predicate<T>, Set<BakedModel>>> options;
    final Set<BakedModel> overrides;
    
    private ItemOverrideOption(DataComponentType<T> componentType, Set<BakedModel> defaultModel, List<Pair<Predicate<T>, Set<BakedModel>>> options, Set<BakedModel> overrides) {
        this.componentType = componentType;
        this.defaultModels = defaultModel;
        this.options = options;
        this.overrides = overrides;
    }
    
    public Set<BakedModel> resolve(ItemStack stack) {
        return !stack.has(componentType) ? defaultModels : options.stream()
                .filter(p -> p.getKey().test(stack.get(componentType)))
                .findFirst()
                .map(Pair::getValue)
                .orElse(defaultModels);
    }
    
    public static class Builder<T> {
        
        final DataComponentType<T> componentType;
        final Set<BakedModel> defaultModels;
        final List<Pair<Predicate<T>, Set<BakedModel>>> options = new ArrayList<>();
        final Set<BakedModel> overrides = new HashSet<>();
        
        public Builder(DataComponentType<T> componentType, Set<BakedModel> defaultModels) {
            this.componentType = componentType;
            this.defaultModels = defaultModels;
        }
        
        public void addOption(Predicate<T> option, ImmutableSet<BakedModel> models) {
            options.add(Pair.of(option, models));
            overrides.addAll(models);
        }
        
        public void addOption(T option, ImmutableSet<BakedModel> models) {
            options.add(Pair.of((T t) -> t.equals(option), models));
            overrides.addAll(models);
        }
        
        public void addOption(Predicate<T> option, BakedModel model) {
            options.add(Pair.of(option, Set.of(model)));
            overrides.add(model);
        }
        
        public void addOption(T option, BakedModel model) {
            options.add(Pair.of((T t) -> t.equals(option), Set.of(model)));
            overrides.add(model);
        }
        
        public ItemOverrideOption<T> build() {
            return new ItemOverrideOption<>(componentType, defaultModels, options, overrides);
        }
        
    }
    
}
