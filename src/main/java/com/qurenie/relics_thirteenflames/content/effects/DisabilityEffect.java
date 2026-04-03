package com.qurenie.relics_thirteenflames.content.effects;


import com.qurenie.api.event.MeleeAttackCheckEvent;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.awt.*;

@EventBusSubscriber
public class DisabilityEffect extends MobEffect {
    
    public DisabilityEffect() {
        super(MobEffectCategory.HARMFUL, new Color(100, 80, 10).getRGB());
    }
    
    @SubscribeEvent
    public static void onMobMeleeAttack(MeleeAttackCheckEvent event) {
        if (event.getEntity().hasEffect(EffectsRegistry.DISABILITY_EFFECT))
            event.setCanceled(true);
    }
    
}
