package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.effects.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EffectsRegistry {
    
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, ThirteenFlames.MODID);
    
    public static final DeferredHolder<MobEffect, MobEffect> POISSON = EFFECTS.register("poisson", PoissonEffect::new);
    
    public static final DeferredHolder<MobEffect, MobEffect> ANEMIA = EFFECTS.register("anemia", AnemiaEffect::new);
    
    public static final DeferredHolder<MobEffect, MobEffect> JODAH_VISION = EFFECTS.register("jodah_vision", JodahVisionEffect::new);
    
    public static final DeferredHolder<MobEffect, MobEffect> DISABILITY_EFFECT = EFFECTS.register("diability_effect", DisabilityEffect::new);
   
    public static final DeferredHolder<MobEffect, MobEffect> SKINT_EFFECT = EFFECTS.register("scint_effect", SkintEffect::new);
    
    public static final DeferredHolder<MobEffect, MobEffect> SKINTONIT_EFFECT = EFFECTS.register("scintonit_effect", AntikintEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> DOUBLE_SCINT_EFFECT = EFFECTS.register("double_scint_effect", AntikintEffect::new);

}
