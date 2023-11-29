package com.qurenie.relics_thirteenflames.content.effects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PoisonEffectInstance extends MobEffectInstance {

    private ItemStack originSword;

    public ItemStack getOriginSword(){
        return this.originSword;
    }

    public void setLivingSource(ItemStack sword){
        this.originSword = sword;
    }

    public PoisonEffectInstance(MobEffect pEffect, int pDuration, int pAmplifier, boolean pAmbient, boolean pVisible, boolean pShowIcon) {
        super(pEffect, pDuration, pAmplifier, pAmbient, pVisible, pShowIcon);
    }

    public PoisonEffectInstance(MobEffect pEffect, int pDuration, int pAmplifier, boolean pAmbient, boolean pVisible, boolean pShowIcon, ItemStack sword) {
        super(pEffect, pDuration, pAmplifier, pAmbient, pVisible, pShowIcon);
        this.originSword = sword;
    }






}
