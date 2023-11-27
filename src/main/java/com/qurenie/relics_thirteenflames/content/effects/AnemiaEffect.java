package com.qurenie.relics_thirteenflames.content.effects;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import net.minecraft.client.player.Input;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
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
    public static void onMovementInput(LivingHealEvent event) {
        if (event.getEntity().hasEffect(EffectsRegistry.ANEMIA)) {
            event.setAmount(event.getAmount() * 0.5f);
        }
    }

}
