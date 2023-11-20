package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.entities.KnefProjectile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class SoundsRegistry
{
	private static final DeferredRegister<SoundEvent> SOUNDS;
	public static final RegistryObject<SoundEvent> KNEF_BOW_SHOT;
	public static final RegistryObject<SoundEvent> KNEF_BOW_RAIN;
	public static final RegistryObject<SoundEvent> KNEF_BOW_SPLASH;
	public static final RegistryObject<SoundEvent> KNEF_BOW_STORM;
	public static final RegistryObject<SoundEvent> KNEF_BOW_STORM_SHORT;

	public SoundsRegistry() {
	}

	public static void registerSounds() {
		SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	static {
		SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "relics_thirteenflames");

		KNEF_BOW_SHOT = SOUNDS.register("knef_shot", () -> {
			return new SoundEvent(new ResourceLocation("relics_thirteenflames", "knef_shot"));
		});
		KNEF_BOW_RAIN = SOUNDS.register("knef_rain", () -> {
			return new SoundEvent(new ResourceLocation("relics_thirteenflames", "knef_rain"));
		});
		KNEF_BOW_SPLASH = SOUNDS.register("knef_splash", () -> {
			return new SoundEvent(new ResourceLocation("relics_thirteenflames", "knef_splash"));
		});
		KNEF_BOW_STORM = SOUNDS.register("knef_storm", () -> {
			return new SoundEvent(new ResourceLocation("relics_thirteenflames", "knef_storm"));
		});
		KNEF_BOW_STORM_SHORT = SOUNDS.register("knef_storm_2", () -> {
			return new SoundEvent(new ResourceLocation("relics_thirteenflames", "knef_storm_2"));
		});

	}
}