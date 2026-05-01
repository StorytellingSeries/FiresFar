package com.qurenie.relics_thirteenflames.content.effects;

import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.octostudios.octolib.util.OctoColor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import java.awt.*;

public class JodahVisionEffect extends MobEffect {

    private static final OctoColor PURPLE_COLOR = new OctoColor(160 / 255f, 20 / 255f, 140 / 255f, 1f);
    
    public JodahVisionEffect() {
        super(MobEffectCategory.HARMFUL, PURPLE_COLOR.getARGB(), ParticleHelper.constructEye(PURPLE_COLOR, 0.15f, 40, 0.98f));
    }
    
}
