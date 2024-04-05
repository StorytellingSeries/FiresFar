package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.effects.AnemiaEffect;
import com.qurenie.relics_thirteenflames.content.effects.PoissonEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

@SimplyRegister
public interface EffectsRegistry {

    @RegistryName("poisson")
    MobEffect POISSON = new PoissonEffect();

    @RegistryName("anemia")
    MobEffect ANEMIA = new AnemiaEffect();

}
