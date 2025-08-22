package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.container.AuritekhBeaconMenu;
import com.qurenie.relics_thirteenflames.content.container.MontuCompositeContainer;
import com.qurenie.relics_thirteenflames.content.container.MontuGlovesContainer;
import com.qurenie.relics_thirteenflames.content.container.ScrollOfTruthContainer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MenuRegistry {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES;

    public static final DeferredHolder<MenuType<?>, MenuType<ScrollOfTruthContainer>> SCROLL_OF_TRUTH_MENU;
    public static final DeferredHolder<MenuType<?>, MenuType<MontuGlovesContainer>> MONTU_SMITH_MENU;
    public static final DeferredHolder<MenuType<?>, MenuType<MontuCompositeContainer>> MONTU_COMPOSIT_MENU;
    public static final DeferredHolder<MenuType<?>, MenuType<AuritekhBeaconMenu>> AURITEKH_BEACON_MENU;

    static {
        MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, ThirteenFlames.MODID);
        
        SCROLL_OF_TRUTH_MENU = MENU_TYPES.register("scroll_of_truth", () -> new MenuType<>((IContainerFactory<ScrollOfTruthContainer>) ScrollOfTruthContainer::new, FeatureFlags.DEFAULT_FLAGS));
        MONTU_SMITH_MENU = MENU_TYPES.register("montu_smith", () -> new MenuType<>((IContainerFactory<MontuGlovesContainer>) (MontuGlovesContainer::new), FeatureFlags.DEFAULT_FLAGS));
        AURITEKH_BEACON_MENU = MENU_TYPES.register("auritekh_beacon", () -> new MenuType<>((IContainerFactory<AuritekhBeaconMenu>) (AuritekhBeaconMenu::new), FeatureFlags.DEFAULT_FLAGS));
        MONTU_COMPOSIT_MENU = MENU_TYPES.register("montu_composit", () -> new MenuType<>((IContainerFactory<MontuCompositeContainer>) (MontuCompositeContainer::new), FeatureFlags.DEFAULT_FLAGS));
    }
}
