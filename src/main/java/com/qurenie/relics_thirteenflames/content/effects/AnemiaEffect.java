package com.qurenie.relics_thirteenflames.content.effects;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.awt.*;

@EventBusSubscriber(modid = ThirteenFlames.MODID)
public class AnemiaEffect extends MobEffect {

    public AnemiaEffect() {
        super(MobEffectCategory.HARMFUL, new Color(93, 0, 0).getRGB());
    }

    @SubscribeEvent
    public static void onLivingHeal(net.neoforged.neoforge.event.entity.living.LivingHealEvent event) {
        if (event.getEntity().hasEffect(EffectsRegistry.ANEMIA)) {
            int amp = event.getEntity().getEffect(EffectsRegistry.ANEMIA).getAmplifier() + 1;
            event.setAmount((float) (event.getAmount() * 0.8 / amp));
        }
    }

}
