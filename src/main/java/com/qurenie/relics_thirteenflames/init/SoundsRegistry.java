package com.qurenie.relics_thirteenflames.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber
public class SoundsRegistry
{
	private static final DeferredRegister<SoundEvent> SOUNDS;
	public static final RegistryObject<SoundEvent> KNEF_BOW_SHOT;
	public static final RegistryObject<SoundEvent> KNEF_BOW_RAIN;
	public static final RegistryObject<SoundEvent> KNEF_BOW_SPLASH;
	public static final RegistryObject<SoundEvent> KNEF_BOW_STORM;
	public static final RegistryObject<SoundEvent> KNEF_BOW_STORM_SHORT;
	public static final RegistryObject<SoundEvent> MONTU_SLAP;

	public SoundsRegistry() {
	}

	public static void registerSounds() {
		SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	static {
		SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "relics_thirteenflames");

		KNEF_BOW_SHOT = SOUNDS.register("knef_shot", () -> {
			return SoundEvent.createVariableRangeEvent(new ResourceLocation("relics_thirteenflames", "knef_shot"));
		});
		KNEF_BOW_RAIN = SOUNDS.register("knef_rain", () -> {
			return SoundEvent.createVariableRangeEvent(new ResourceLocation("relics_thirteenflames", "knef_rain"));
		});
		KNEF_BOW_SPLASH = SOUNDS.register("knef_splash", () -> {
			return SoundEvent.createVariableRangeEvent(new ResourceLocation("relics_thirteenflames", "knef_splash"));
		});
		KNEF_BOW_STORM = SOUNDS.register("knef_storm", () -> {
			return SoundEvent.createVariableRangeEvent(new ResourceLocation("relics_thirteenflames", "knef_storm"));
		});
		KNEF_BOW_STORM_SHORT = SOUNDS.register("knef_storm_2", () -> {
			return SoundEvent.createVariableRangeEvent(new ResourceLocation("relics_thirteenflames", "knef_storm_2"));
		});
		MONTU_SLAP = SOUNDS.register("montu_slap", () -> {
			return SoundEvent.createVariableRangeEvent(new ResourceLocation("relics_thirteenflames", "montu_slap"));
		});
	}
}