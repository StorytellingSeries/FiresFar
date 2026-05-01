package com.qurenie.relics_thirteenflames.client;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.zeith.hammeranims.api.HammerAnimationsApi;
import org.zeith.hammeranims.api.animation.IAnimationContainer;
import org.zeith.hammeranims.api.animation.IAnimationSource;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class AnimationsRegistry
{

	static final Map<ResourceLocation, IAnimationContainer> TO_REGISTER = new HashMap<>();
	
	@SubscribeEvent
	public static void registerAnimations(RegisterEvent event)
	{
		var reg = HammerAnimationsApi.animations();
		if(event.getRegistry().equals(reg))
			for(var e : TO_REGISTER.entrySet())
				event.register(HammerAnimationsApi.Keys.ANIMATION_CONTAINERS, e.getKey(), e::getValue);
	}
	
	public static IAnimationContainer register(String path)
	{
		return getOrLoadAnimations(path);
	}
	
	public static IAnimationSource register(String path, String variant)
	{
		return register(path).holder(variant);
	}
	
	public static IAnimationContainer getOrLoadAnimations(String path)
	{
		if(path.startsWith("animations/"))
			path = path.substring(11);
		if(path.endsWith(".json"))
			path = path.substring(0, path.length() - 5);
		
		return TO_REGISTER.computeIfAbsent(ThirteenFlames.rl(path), ___ -> IAnimationContainer.createNoSuffix());
	}

	public static final IAnimationSource SUN_SPIN = register("sun_spin");
	public static final IAnimationSource FLESH_MOVE = register("flesh_move");
	public static final IAnimationSource ZERO_SCALE = register("zero_scale");
	public static final IAnimationSource JODAH_WINGS_OPEN = register("jodah_wings_open");
	public static final IAnimationSource ATTACK_BOOK_IDLE = register("attack_book_idle");
	public static final IAnimationSource BOOK_ATTACK = register("book_attack");
	public static final IAnimationSource BOOK_OPEN = register("book_open");
	public static final IAnimationSource SCINT_CLUSTER_APPEAR = register("scint_cluster_appear");
	public static final IAnimationSource RESPAWN_BOOK_IDLE = register("respawn_book_idle");
	public static final IAnimationSource RESPAWN_BOOK_OPEN = register("respawn_book_open");
	public static final IAnimationSource RESPAWN_BOOK_RESPAWN_LAYER = register("respawn_book_respawn_layer");
	public static final IAnimationSource ADVENTURER_SWORD_BIG_RUN_PIERCE = register("adventurer_sword_big_run_pierce");
	public static final IAnimationSource ADVENTURER_SWORD_BIG_ATTACK = register("adventurer_sword_big_attack");

	public static final IAnimationSource GHOST_SMALL_ATTACK = register("rose_ghost1_attack");
	public static final IAnimationSource GHOST_SMALL_DEATH = register("rose_ghost1_death");
	public static final IAnimationSource GHOST_SMALL_IDLE = register("rose_ghost1_idle");
	public static final IAnimationSource GHOST_SMALL_WALK = register("rose_ghost1_walk");

	public static final IAnimationSource GHOST_BIG_ATTACK = register("rose_ghost2_attack");
	public static final IAnimationSource GHOST_BIG_DEATH = register("rose_ghost2_death");
	public static final IAnimationSource GHOST_BIG_IDLE = register("rose_ghost2_idle");
	public static final IAnimationSource GHOST_BIG_WALK = register("rose_ghost2_walk");


	@SubscribeEvent
	public static void onSetup(FMLCommonSetupEvent event)
	{
	}
}