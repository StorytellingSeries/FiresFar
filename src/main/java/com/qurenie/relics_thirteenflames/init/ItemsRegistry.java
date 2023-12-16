package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.entities.KnefProjectile;
import com.qurenie.relics_thirteenflames.content.items.ItemKnefBow;
import com.qurenie.relics_thirteenflames.content.items.ItemMontuHammer;
import com.qurenie.relics_thirteenflames.content.items.ItemRonasSword;
import it.hurts.sskirillss.relics.items.SolidSnowballItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

@SimplyRegister
public interface ItemsRegistry {

    @RegistryName("knef_bow")
    ItemKnefBow KNEF_BOW = new ItemKnefBow(props().rarity(Rarity.RARE).stacksTo(1));

    @RegistryName("ronas_sword")
    ItemRonasSword RONAS_SWORD = new ItemRonasSword(props().rarity(Rarity.RARE).stacksTo(1));

    @RegistryName("montu_hammer")
    ItemMontuHammer MONTU_HAMMER = new ItemMontuHammer(props().rarity(Rarity.RARE).stacksTo(1), Tiers.NETHERITE);

    static Item.Properties props()
    {
        return new Item.Properties().tab(ThirteenFlames.ITEM_TAB);
    }
}
