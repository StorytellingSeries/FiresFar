package com.qurenie.relics_thirteenflames.content.effects;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.awt.*;

public class PoissonEffect extends MobEffect {

    public PoissonEffect() {

        super(MobEffectCategory.HARMFUL, new Color(0, 93, 2).getRGB());
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        super.applyEffectTick(pLivingEntity, pAmplifier);
        int invulTime = pLivingEntity.invulnerableTime;
        pLivingEntity.hurt(pLivingEntity.level().damageSources().magic(), 1.0f + pAmplifier * 0.2f);
        pLivingEntity.invulnerableTime = invulTime;
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {

        int j = 20 - pAmplifier * 3;
        if (j > 0) {
            return pDuration % j == 0;
        } else {
            return true;
        }
    }

}
