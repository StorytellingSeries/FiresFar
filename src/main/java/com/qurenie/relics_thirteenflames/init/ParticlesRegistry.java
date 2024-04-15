package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.client.particles.CircleTintData;
import com.qurenie.relics_thirteenflames.client.particles.CircleTintFactory;
import com.qurenie.relics_thirteenflames.client.particles.DeathFlameParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = ThirteenFlames.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ParticlesRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ThirteenFlames.MODID);;
    public static final RegistryObject<ParticleType<CircleTintData>> CIRCLE_TINT = PARTICLES.register("circle_tint", CircleTintFactory.CircleTintType::new);
    public static final RegistryObject<SimpleParticleType> DEATH_FLAME_PARTICLE = PARTICLES.register("death_flame_particle", () -> new SimpleParticleType(true));

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerParticles(RegisterParticleProvidersEvent event){
        event.registerSpriteSet(ParticlesRegistry.CIRCLE_TINT.get(), CircleTintFactory::new);
        event.registerSpriteSet(ParticlesRegistry.DEATH_FLAME_PARTICLE.get(), DeathFlameParticle.Factory::new);
    }
}
