package com.qurenie.relics_thirteenflames.content.effects;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

public class PoisonEffectInstance extends MobEffectInstance {

    private ItemStack originSword;

    public ItemStack getOriginSword(){
        return this.originSword;
    }

    public void setOriginSword(ItemStack sword){
        this.originSword = sword;
    }

    public PoisonEffectInstance(Holder<MobEffect> pEffect, int pDuration, int pAmplifier, boolean pAmbient, boolean pVisible, boolean pShowIcon) {
        super(pEffect, pDuration, pAmplifier, pAmbient, pVisible, pShowIcon);
    }

    public PoisonEffectInstance(Holder<MobEffect> pEffect, int pDuration, int pAmplifier, boolean pAmbient, boolean pVisible, boolean pShowIcon, ItemStack sword) {
        super(pEffect, pDuration, pAmplifier, pAmbient, pVisible, pShowIcon);
        this.originSword = sword;
    }






}
