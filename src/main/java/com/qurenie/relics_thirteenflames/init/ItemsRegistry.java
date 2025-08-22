package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.items.*;
import com.qurenie.relics_thirteenflames.content.items.feather.ItemHettFeather;
import com.qurenie.relics_thirteenflames.content.items.feather.ItemHettFeatherBook;
import com.qurenie.relics_thirteenflames.content.items.misc.AuritekhTier;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootEntry;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.BeamsData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.MathUtils;
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
            .attributes(SwordItem.createAttributes(Tiers.DIAMOND, 3, -1F)));
    
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
            .attributes(PickaxeItem.createAttributes(AuritekhTier.INSTANCE, 3, -1.8f)));
    @RegistryName("auritekh_hoe")
    Item AURITEKH_HOE = new HoeItem(AuritekhTier.INSTANCE, props().fireResistant().stacksTo(1).rarity(Rarity.UNCOMMON)
            .attributes(PickaxeItem.createAttributes(AuritekhTier.INSTANCE, -3.5F, 0.6F)));
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
    Item AURITEKH_HELMET = new ArmorItem(ArmorMaterialRegistry.MONTU_SMITH_TYPE, ArmorItem.Type.HELMET, props().fireResistant().stacksTo(1).rarity(Rarity.UNCOMMON).durability(ArmorItem.Type.HELMET.getDurability(60)));
    @RegistryName("auritekh_boots")
    Item AURITEKH_BOOTS = new ArmorItem(ArmorMaterialRegistry.MONTU_SMITH_TYPE, ArmorItem.Type.BOOTS, props().fireResistant().stacksTo(1).rarity(Rarity.UNCOMMON).durability(ArmorItem.Type.BOOTS.getDurability(60)));
    @RegistryName("auritekh_chestplate")
    Item AURITEKH_CHESTPLATE = new ArmorItem(ArmorMaterialRegistry.MONTU_SMITH_TYPE, ArmorItem.Type.CHESTPLATE, props().fireResistant().stacksTo(1).rarity(Rarity.UNCOMMON).durability(ArmorItem.Type.CHESTPLATE.getDurability(60)));
    @RegistryName("auritekh_leggings")
    Item AURITEKH_LEGGINGS = new ArmorItem(ArmorMaterialRegistry.MONTU_SMITH_TYPE, ArmorItem.Type.LEGGINGS, props().fireResistant().stacksTo(1).rarity(Rarity.UNCOMMON).durability(ArmorItem.Type.LEGGINGS.getDurability(60)));
    @RegistryName("montu_smith")
    Item MONTU_SMITH = new Item(props());
    
    static Item.Properties props() {
        return new Item.Properties();
    }
    
    static RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder().ability(AbilityData.builder("mayhem").stat(StatData.builder("chance").initialValue(0.05, 0.15).upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.065).formatValue((value) -> (int) MathUtils.round(value * 100.0, 0)).build()).stat(StatData.builder("bounces").initialValue(2.0, 4.0).upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.15).formatValue((value) -> (int) MathUtils.round(value, 0)).build()).stat(StatData.builder("damage").initialValue(0.1, 0.2).upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.15).formatValue((value) -> (int) MathUtils.round(value * 100.0, 0)).build()).research(ResearchData.builder().star(0, 11, 2).star(1, 3, 19).star(2, 11, 19).star(3, 19, 19).star(4, 11, 29).link(0, 2).link(2, 1).link(2, 3).link(2, 4).build()).build()).ability(AbilityData.builder("cloning").requiredLevel(5).stat(StatData.builder("chance").initialValue(0.05, 0.1).upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1).formatValue((value) -> (int) MathUtils.round(value * 100.0, 0)).build()).research(ResearchData.builder().star(0, 12, 2).star(1, 7, 7).star(2, 17, 14).star(3, 6, 22).star(4, 11, 29).link(0, 1).link(1, 2).link(2, 3).link(3, 4).build()).build()).build())
                .style(StyleData.builder()
                        .tooltip(TooltipData.builder().borderTop(-13884368).borderBottom(-12116379).textured(true).build())
                        .beams(BeamsData.builder().startColor(-65281).endColor(255).build()).build()).leveling(LevelingData.builder().initialCost(100).maxLevel(15).step(100).sources(LevelingSourcesData.builder().source(LevelingSourceData.abilityBuilder("mayhem").initialValue(1).gem(GemShape.SQUARE, GemColor.PURPLE).build()).build()).build()).loot(LootData.builder().entry(new LootEntry[]{LootEntries.THE_END, LootEntries.END_LIKE}).build()).build();
    }
    
}
