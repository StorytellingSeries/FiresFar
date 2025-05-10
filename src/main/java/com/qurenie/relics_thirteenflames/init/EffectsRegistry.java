package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.effects.AnemiaEffect;
import com.qurenie.relics_thirteenflames.content.effects.PoissonEffect;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.zeith.hammerlib.annotations.SimplyRegister;

public class EffectsRegistry {
    
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, ThirteenFlames.MODID);
    
    public static final DeferredHolder<MobEffect, MobEffect> POISSON = EFFECTS.register("poisson", PoissonEffect::new);
    
    public static final DeferredHolder<MobEffect, MobEffect> ANEMIA = EFFECTS.register("anemia", AnemiaEffect::new);
    
}
