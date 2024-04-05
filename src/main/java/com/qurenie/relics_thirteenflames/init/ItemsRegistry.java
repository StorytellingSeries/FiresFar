package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.items.ItemKnefBow;
import com.qurenie.relics_thirteenflames.content.items.ItemMontuHammer;
import com.qurenie.relics_thirteenflames.content.items.ItemRonasSword;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

import java.util.Set;

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
        return new Item.Properties();
    }
}
