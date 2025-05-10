package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.client.particles.DeathFlameParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = ThirteenFlames.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ParticlesRegistry {
    
    public static final DeferredRegister<ParticleType<?>> PARTICLES;
    
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DEATH_FLAME_PARTICLE;
    
    static {
        PARTICLES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, "relics_thirteenflames");
        
        DEATH_FLAME_PARTICLE = PARTICLES.register("death_flame_particle", () -> new SimpleParticleType(true));
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerParticles(RegisterParticleProvidersEvent event){
        event.registerSpriteSet(ParticlesRegistry.DEATH_FLAME_PARTICLE.get(), DeathFlameParticle.Factory::new);
    }
}
