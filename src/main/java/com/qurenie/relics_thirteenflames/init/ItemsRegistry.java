package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.items.*;
import com.qurenie.relics_thirteenflames.content.items.feather.ItemHettFeather;
import com.qurenie.relics_thirteenflames.content.items.feather.ItemHettFeatherBook;
import com.qurenie.relics_thirteenflames.content.items.misc.AuritekhTier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import org.zeith.hammerlib.annotations.Ref;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

import static com.qurenie.relics_thirteenflames.content.items.ItemTravellerSword.TRAVELLER_STEP_HEIGHT;

@SimplyRegister(creativeTabs = {@Ref(
        value = ThirteenFlames.class,
        field = "ITEM_TAB"
)})
public interface ItemsRegistry {
    
    @RegistryName("knef_bow")
    ItemKnefBow KNEF_BOW = new ItemKnefBow(props().rarity(Rarity.RARE).stacksTo(1));

    @RegistryName("traveller_sword")
    ItemTravellerSword TRAVELLER_SWORD = new ItemTravellerSword(Tiers.DIAMOND, props().rarity(Rarity.RARE).attributes(SwordItem.createAttributes(Tiers.DIAMOND, 3, -2.4F)
            .withModifierAdded(Attributes.STEP_HEIGHT, new AttributeModifier(TRAVELLER_STEP_HEIGHT, 1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HAND)).stacksTo(1));
    
    @RegistryName("jodah_staff")
    ItemJodahStaff JODAH_STAFF = new ItemJodahStaff(Tiers.IRON, props().rarity(Rarity.RARE).stacksTo(1)
            .attributes(SwordItem.createAttributes(Tiers.DIAMOND, 3, -1.8F)));
    
    @RegistryName("jodah_mask")
    ItemJodahMask JODAH_MASK = new ItemJodahMask(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, props().rarity(Rarity.RARE).stacksTo(1));
    
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
    ScrollOfTruthItem SCROLL_OF_TRUTH = new ScrollOfTruthItem(props().stacksTo(1).rarity(Rarity.RARE));
    
    @RegistryName("hett_feather_book")
    ItemHettFeatherBook HETT_FEATHER_BOOK = new ItemHettFeatherBook(props().stacksTo(1).rarity(Rarity.UNCOMMON).durability(50).setNoRepair());
    
    @RegistryName("hett_feather")
    ItemHettFeather HETT_FEATHER = new ItemHettFeather(props().stacksTo(1).rarity(Rarity.RARE).setNoRepair());
    
    @RegistryName("montu_gloves")
    ItemMontuGloves MONTU_GLOVES = new ItemMontuGloves(props().stacksTo(1).rarity(Rarity.RARE).setNoRepair());
    
    @RegistryName("auritekh_ingot")
    Item AURITEKH_INGOT = new Item(props().rarity(Rarity.UNCOMMON).fireResistant());
    
    @RegistryName("auritekh_sword")
    Item AURITEKH_SWORD = new SwordItem(AuritekhTier.INSTANCE, props().fireResistant().stacksTo(1).rarity(Rarity.UNCOMMON)
            .attributes(SwordItem.createAttributes(AuritekhTier.INSTANCE, 3, -1.8f)));
   
    @RegistryName("auritekh_hoe")
    Item AURITEKH_HOE = new HoeItem(AuritekhTier.INSTANCE, props().fireResistant().stacksTo(1).rarity(Rarity.UNCOMMON)
            .attributes(HoeItem.createAttributes(AuritekhTier.INSTANCE, -3.5F, 0.6F)));
    
    @RegistryName("auritekh_shovel")
    Item AURITEKH_SHOVEL = new ShovelItem(AuritekhTier.INSTANCE, props().fireResistant().stacksTo(1).rarity(Rarity.UNCOMMON)
            .attributes(ShovelItem.createAttributes(AuritekhTier.INSTANCE, 1.5F, -2.4F)));
   
    @RegistryName("auritekh_pickaxe")
    Item AURITEKH_PICKAXE = new PickaxeItem(AuritekhTier.INSTANCE, props().fireResistant().stacksTo(1).rarity(Rarity.UNCOMMON)
            .attributes(PickaxeItem.createAttributes(AuritekhTier.INSTANCE, 1.0F, -2.2F)));
   
    @RegistryName("auritekh_axe")
    Item AURITEKH_AXE = new AxeItem(AuritekhTier.INSTANCE, props().fireResistant().stacksTo(1).rarity(Rarity.UNCOMMON)
            .attributes(AxeItem.createAttributes(AuritekhTier.INSTANCE, 5.0F, -2.4F)));
   
    @RegistryName("auritekh_helmet")
    Item AURITEKH_HELMET = new AuritekhArmor(ArmorMaterialRegistry.MONTU_SMITH_TYPE, ArmorItem.Type.HELMET, props().fireResistant().stacksTo(1).rarity(Rarity.UNCOMMON));
   
    @RegistryName("auritekh_boots")
    Item AURITEKH_BOOTS = new AuritekhArmor(ArmorMaterialRegistry.MONTU_SMITH_TYPE, ArmorItem.Type.BOOTS, props().fireResistant().stacksTo(1).rarity(Rarity.UNCOMMON));
    
    @RegistryName("auritekh_chestplate")
    Item AURITEKH_CHESTPLATE = new AuritekhArmor(ArmorMaterialRegistry.MONTU_SMITH_TYPE, ArmorItem.Type.CHESTPLATE, props().fireResistant().stacksTo(1).rarity(Rarity.UNCOMMON));
   
    @RegistryName("auritekh_leggings")
    Item AURITEKH_LEGGINGS = new AuritekhArmor(ArmorMaterialRegistry.MONTU_SMITH_TYPE, ArmorItem.Type.LEGGINGS, props().fireResistant().stacksTo(1).rarity(Rarity.UNCOMMON));
    
    @RegistryName("auritekh_elytra")
    Item AURITEKH_ELYTRA = new AuritekhElytraItem(ArmorMaterialRegistry.MONTU_SMITH_TYPE, ArmorItem.Type.CHESTPLATE, props().fireResistant().stacksTo(1).rarity(Rarity.EPIC).durability(20000));
    
    static Item.Properties props() {
        return new Item.Properties();
    }
    
}
