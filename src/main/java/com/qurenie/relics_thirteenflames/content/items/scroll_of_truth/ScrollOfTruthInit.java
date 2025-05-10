package com.qurenie.relics_thirteenflames.content.items.scroll_of_truth;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ScrollOfTruthInit {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES;

    public static final DeferredHolder<MenuType<?>, MenuType<ScrollOfTruthContainer>> SCROLL_OF_TRUTH_MENU;

    static {
        MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, ThirteenFlames.MODID);
        
        SCROLL_OF_TRUTH_MENU = MENU_TYPES.register("scroll_of_truth", () -> new MenuType<>((IContainerFactory<ScrollOfTruthContainer>) ScrollOfTruthContainer::new, FeatureFlags.DEFAULT_FLAGS));
    }
}
