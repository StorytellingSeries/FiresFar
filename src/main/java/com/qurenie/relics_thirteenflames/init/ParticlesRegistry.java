package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.client.particles.CircleTintParticle;
import com.qurenie.relics_thirteenflames.client.particles.ColoredRelicParticle;
import com.qurenie.relics_thirteenflames.client.particles.DeathFlameParticle;
import com.qurenie.relics_thirteenflames.client.particles.FeatherParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.zeith.hammerlib.core.adapter.recipe.SmeltingRecipeBuilder;
import org.zeith.hammerlib.util.mcf.RecipeRegistrationContext;

@EventBusSubscriber(modid = ThirteenFlames.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ParticlesRegistry {
    
    public static final DeferredRegister<ParticleType<?>> PARTICLES;
    
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DEATH_FLAME_PARTICLE;
    public static final DeferredHolder<ParticleType<?>, ColoredRelicParticle.Type> COLORED_RELIC_PARTICLE;
    public static final DeferredHolder<ParticleType<?>, ColoredRelicParticle.Type> COLORED_HEAL;
    public static final DeferredHolder<ParticleType<?>, ColoredRelicParticle.Type> COLORED_EYE;
    public static final DeferredHolder<ParticleType<?>, ColoredRelicParticle.Type> COLORED_SMOKE;
    public static final DeferredHolder<ParticleType<?>, CircleTintParticle.Type> CIRCLE_TINT_PARTICLE;
    public static final DeferredHolder<ParticleType<?>, FeatherParticle.Type> JODAH_FEATHER;
    public static final DeferredHolder<ParticleType<?>, FeatherParticle.Type> HETT_FEATHER;
    
    static {
        PARTICLES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, "relics_thirteenflames");
        DEATH_FLAME_PARTICLE = PARTICLES.register("death_flame_particle", () -> new SimpleParticleType(true));
        COLORED_RELIC_PARTICLE = PARTICLES.register("colored_particle", ColoredRelicParticle.Type::new);
        COLORED_EYE = PARTICLES.register("colored_eye",  ColoredRelicParticle.Type::new);
        COLORED_HEAL = PARTICLES.register("colored_heal",  ColoredRelicParticle.Type::new);
        COLORED_SMOKE = PARTICLES.register("colored_smoke",  ColoredRelicParticle.Type::new);
        CIRCLE_TINT_PARTICLE = PARTICLES.register("circle_tint", CircleTintParticle.Type::new);
        JODAH_FEATHER = PARTICLES.register("jodah_feather", FeatherParticle.Type::new);
        HETT_FEATHER = PARTICLES.register("hett_feather", FeatherParticle.Type::new);
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerParticles(RegisterParticleProvidersEvent event){
        event.registerSpriteSet(ParticlesRegistry.DEATH_FLAME_PARTICLE.get(), DeathFlameParticle.Factory::new);
        event.registerSpriteSet(ParticlesRegistry.COLORED_RELIC_PARTICLE.get(), ColoredRelicParticle.Factory::new);
        event.registerSpriteSet(ParticlesRegistry.COLORED_HEAL.get(), ColoredRelicParticle.Factory::new);
        event.registerSpriteSet(ParticlesRegistry.COLORED_EYE.get(), ColoredRelicParticle.SpellFactory::new);
        event.registerSpriteSet(ParticlesRegistry.COLORED_SMOKE.get(), ColoredRelicParticle.DisappearingParticleFactory::new);
        event.registerSpriteSet(ParticlesRegistry.CIRCLE_TINT_PARTICLE.get(), CircleTintParticle.Factory::new);
        event.registerSpriteSet(ParticlesRegistry.JODAH_FEATHER.get(), FeatherParticle.Factory::new);
        event.registerSpriteSet(ParticlesRegistry.HETT_FEATHER.get(), FeatherParticle.Factory::new);
    }
    
}
