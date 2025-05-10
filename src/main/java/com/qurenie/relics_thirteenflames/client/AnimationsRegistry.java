package com.qurenie.relics_thirteenflames.client;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
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

	@SubscribeEvent
	public static void onSetup(FMLCommonSetupEvent event)
	{
	}
}