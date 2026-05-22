package com.qurenie.relics_thirteenflames.content.effects;

import com.qurenie.api.JodahMaskEvent;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import com.qurenie.relics_thirteenflames.style.ColorScheme;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.awt.*;

@EventBusSubscriber
public class DoubleScintEffect extends MobEffect {

    protected DoubleScintEffect() {
        super(MobEffectCategory.BENEFICIAL, ColorScheme.GOLD_COLOR.getARGB());
    }

    @SubscribeEvent
    public static void addScint(JodahMaskEvent.AddScint event) {
        if (!(event.getLiving() instanceof LivingEntity living) || !living.hasEffect(EffectsRegistry.DOUBLE_SCINT_EFFECT))
            return;

        event.setValue(event.getValue() * (living.getEffect(EffectsRegistry.DOUBLE_SCINT_EFFECT).getAmplifier() + 2));
    }

}
