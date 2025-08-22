package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public interface ArmorMaterialRegistry {
    
    DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(BuiltInRegistries.ARMOR_MATERIAL, ThirteenFlames.MODID);
    
    DeferredHolder<ArmorMaterial, ArmorMaterial> MONTU_SMITH_TYPE = register("auritekh", Util.make(new EnumMap<>(ArmorItem.Type.class), p_323379_ -> {
        p_323379_.put(ArmorItem.Type.BOOTS, 5);
        p_323379_.put(ArmorItem.Type.LEGGINGS, 8);
        p_323379_.put(ArmorItem.Type.CHESTPLATE, 11);
        p_323379_.put(ArmorItem.Type.HELMET, 5);
        p_323379_.put(ArmorItem.Type.BODY, 15);
    }), 22, SoundEvents.ARMOR_EQUIP_NETHERITE, 6.0F, 0.3F, () -> Ingredient.of(ItemsRegistry.AURITEKH_INGOT));
    
    private static DeferredHolder<ArmorMaterial, ArmorMaterial> register(
            String name,
            EnumMap<ArmorItem.Type, Integer> defense,
            int enchantmentValue,
            Holder<SoundEvent> equipSound,
            float toughness,
            float knockbackResistance,
            Supplier<Ingredient> repairIngridient
    ) {
        List<ArmorMaterial.Layer> list = List.of(new ArmorMaterial.Layer(ThirteenFlames.rl(name)));
        EnumMap<ArmorItem.Type, Integer> enummap = new EnumMap<>(ArmorItem.Type.class);
        
        for (ArmorItem.Type armoritem$type : ArmorItem.Type.values()) {
            enummap.put(armoritem$type, defense.get(armoritem$type));
        }
        
        return ARMOR_MATERIALS.register(name, () -> new ArmorMaterial(enummap, enchantmentValue, equipSound, repairIngridient, list, toughness, knockbackResistance));
    }
    
    static void register(IEventBus bus) {
        ARMOR_MATERIALS.register(bus);
    }
    
}
