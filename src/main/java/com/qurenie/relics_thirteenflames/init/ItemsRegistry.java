package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.entities.KnefProjectile;
import com.qurenie.relics_thirteenflames.content.items.ItemKnefBow;
import it.hurts.sskirillss.relics.items.SolidSnowballItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

@SimplyRegister
public interface ItemsRegistry {
//    private static final DeferredRegister<Item> ITEMS;
//    public static final RegistryObject<Item> KNEF_BOW;

    @RegistryName("knef_bow")
    ItemKnefBow KNEF_BOW = new ItemKnefBow(props().rarity(Rarity.RARE).stacksTo(1));


//    public static void registerItems() {
//        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
//    }
//
//    static {
//        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "relics_thirteenflames");
//
//        KNEF_BOW = ITEMS.register("knef_bow", () -> new ItemKnefBow(props().rarity(Rarity.RARE)));
//    }

    static Item.Properties props()
    {
        return new Item.Properties().tab(ThirteenFlames.ITEM_TAB);
    }
}
