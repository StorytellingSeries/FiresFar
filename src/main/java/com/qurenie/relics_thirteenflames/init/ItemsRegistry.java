package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.items.*;
import com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.ScrollOfTruth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import org.zeith.hammerlib.annotations.Ref;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

@SimplyRegister(creativeTabs = {@Ref(
        value = ThirteenFlames.class,
        field = "ITEM_TAB"
)})
public interface ItemsRegistry {

    @RegistryName("knef_bow")
    ItemKnefBow KNEF_BOW = new ItemKnefBow(props().rarity(Rarity.RARE).stacksTo(1));

    @RegistryName("ronas_sword")
    ItemRonasSword RONAS_SWORD = new ItemRonasSword(props().rarity(Rarity.RARE).stacksTo(1));

    @RegistryName("montu_hammer")
    ItemMontuHammer MONTU_HAMMER = new ItemMontuHammer(props().rarity(Rarity.RARE).stacksTo(1), Tiers.NETHERITE);

    @RegistryName("seliaset_horn")
    ItemSeliasetHorn SELIASET_HORN = new ItemSeliasetHorn(props().stacksTo(1).rarity(Rarity.RARE));

    @RegistryName("seliaset_sun")
    ItemSeliasetSun SELIASET_SUN = new ItemSeliasetSun(props().stacksTo(1).rarity(Rarity.RARE));

    @RegistryName("knef_rose")
    ItemKnefRose KNEF_ROSE = new ItemKnefRose(props().stacksTo(1).rarity(Rarity.RARE));

    @RegistryName("ronas_shield")
    ItemRonasShield RONAS_SHIELD = new ItemRonasShield(props().stacksTo(1).rarity(Rarity.RARE));

    @RegistryName("scroll_of_truth")
    ScrollOfTruth SCROLL_OF_TRUTH = new ScrollOfTruth(props().stacksTo(1).rarity(Rarity.RARE));

    static Item.Properties props()
    {
        return new Item.Properties();
    }
}
