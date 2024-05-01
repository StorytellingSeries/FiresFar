package com.qurenie.relics_thirteenflames.content.items.scroll_of_truth;

import com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.screen.ScrollOfTruthContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.enchantment.Enchantments;
import org.zeith.hammerlib.api.io.IAutoNBTSerializable;

import java.util.ArrayList;
import java.util.Collection;

public class EnchantData implements IAutoNBTSerializable {

    private Collection<ScrollOfTruthContainerScreen.EnchantmentInstance> enchantmentInstances;

    public EnchantData(Collection<ScrollOfTruthContainerScreen.EnchantmentInstance> enchantments){
        this.enchantmentInstances = enchantments;
    }
    public EnchantData(){}

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = IAutoNBTSerializable.super.serializeNBT();
        int i = 0;
        for (ScrollOfTruthContainerScreen.EnchantmentInstance inst : enchantmentInstances){
            CompoundTag s = inst.serializeNBT();
            tag.put("ench" + i,s);
            i++;
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        IAutoNBTSerializable.super.deserializeNBT(nbt);
        enchantmentInstances = new ArrayList<>();
        int i = 0;
        while (nbt.contains("ench" + i)){
            CompoundTag tag = nbt.getCompound("ench" + i++);
            ScrollOfTruthContainerScreen.EnchantmentInstance inst = new ScrollOfTruthContainerScreen.EnchantmentInstance(Enchantments.AQUA_AFFINITY,1);
            inst.deserializeNBT(tag);
            enchantmentInstances.add(inst);
        }
    }

    public Collection<ScrollOfTruthContainerScreen.EnchantmentInstance> getEnchantmentInstances() {
        return enchantmentInstances;
    }
}
