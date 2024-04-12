package com.qurenie.relics_thirteenflames.client;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegisterEvent;
import org.zeith.hammeranims.api.HammerAnimationsApi;
import org.zeith.hammeranims.api.animation.Animation;
import org.zeith.hammeranims.api.animation.IAnimationContainer;
import org.zeith.hammeranims.api.animation.IAnimationSource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AnimationsRegistry
{

	static final Map<ResourceLocation, IAnimationContainer> TO_REGISTER = new HashMap<>();
	
	@SubscribeEvent
	public static void registerAnimations(RegisterEvent event)
	{
		var reg = HammerAnimationsApi.animations();
		if(event.getRegistryKey().equals(reg.getRegistryKey()))
			for(var e : TO_REGISTER.entrySet())
				reg.register(e.getKey(), e.getValue());
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

	public static final IAnimationSource SUN_SPIN = register("sun_spin"); //

	@SubscribeEvent
	public static void onSetup(FMLCommonSetupEvent event)
	{
	}
}