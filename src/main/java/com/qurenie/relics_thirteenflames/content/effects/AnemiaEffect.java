package com.qurenie.relics_thirteenflames.content.effects;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;

@Mod.EventBusSubscriber(modid = ThirteenFlames.MODID)
public class AnemiaEffect extends MobEffect {

    public AnemiaEffect() {

        super(MobEffectCategory.HARMFUL, new Color(93, 0, 0).getRGB());
    }


    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity().hasEffect(EffectsRegistry.ANEMIA)) {
            int amp = event.getEntity().getEffect(EffectsRegistry.ANEMIA).getAmplifier() + 1;
            event.setAmount(event.getAmount() * (0.8f / amp));
        }
    }

}
