package com.qurenie.relics_thirteenflames.content.effects;

import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import java.awt.*;

public class JodahVisionEffect extends MobEffect {
    
    private static final Color PURPLE_COLOR = new Color(160, 20, 140);
    
    public JodahVisionEffect() {
        super(MobEffectCategory.HARMFUL, PURPLE_COLOR.getRGB(), ParticleHelper.constructEye(PURPLE_COLOR, 0.15f, 40, 0.98f));
    }
    
}
