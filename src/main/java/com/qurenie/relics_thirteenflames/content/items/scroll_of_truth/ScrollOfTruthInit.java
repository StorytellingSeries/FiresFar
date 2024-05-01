package com.qurenie.relics_thirteenflames.content.items.scroll_of_truth;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ScrollOfTruthInit {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ThirteenFlames.MODID);

    public static final RegistryObject<MenuType<ScrollOfTruthContainer>> SCROLL_OF_TRUTH_MENU = MENU_TYPES.register("scroll_of_truth",() -> IForgeMenuType.create(ScrollOfTruthContainer::new));

}
