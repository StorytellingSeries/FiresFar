package com.qurenie.relics_thirteenflames.client.base;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;

import java.util.Set;
import java.util.function.Function;

public interface ItemOverrideBranch extends Function<ItemStack, Set<BakedModel>> {
    
    static <T> ItemOverrideBranch of(ItemOverrideOption<T> option) {
        return new ItemOverrideBranch() {
            
            @Override
            public Set<BakedModel> apply(ItemStack stack) {
                return option.resolve(stack);
            }
            
            @Override
            public Set<BakedModel> getOverrides() {
                return option.overrides;
            }
        };
    }
    
    default <T> ItemOverrideBranch or(ItemOverrideOption<T> option) {
        return new ItemOverrideBranch() {
            
            @Override
            public Set<BakedModel> apply(ItemStack stack) {
                var result = ItemOverrideBranch.this.apply(stack);
                result.addAll(option.resolve(stack));
                return result;
            }
            
            @Override
            public Set<BakedModel> getOverrides() {
                var result = ItemOverrideBranch.this.getOverrides();
                result.addAll(option.overrides);
                return result;
            }
        };
    }
    
    default <T> ItemOverrideBranch and(ItemOverrideOption<T> option) {
        return new ItemOverrideBranch() {
            
            @Override
            public Set<BakedModel> apply(ItemStack stack) {
                var result = ItemOverrideBranch.this.apply(stack);
                result.retainAll(option.resolve(stack));
                return result;
            }
            
            @Override
            public Set<BakedModel> getOverrides() {
                var result = ItemOverrideBranch.this.getOverrides();
                result.retainAll(option.overrides);
                return result;
            }
        };
    }
    
    Set<BakedModel> getOverrides();
    
}
