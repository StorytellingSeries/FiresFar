package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.client.particles.CircleTintData;
import com.qurenie.relics_thirteenflames.client.particles.CircleTintFactory;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ParticlesRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ThirteenFlames.MODID);;
    public static final RegistryObject<ParticleType<CircleTintData>> CIRCLE_TINT = PARTICLES.register("circle_tint", CircleTintFactory.CircleTintType::new);

}
