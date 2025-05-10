package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SoundsRegistry
{
	public static final DeferredRegister<SoundEvent> SOUNDS;
	public static final DeferredHolder<SoundEvent, SoundEvent> KNEF_BOW_SHOT;
	public static final DeferredHolder<SoundEvent, SoundEvent> KNEF_BOW_RAIN;
	public static final DeferredHolder<SoundEvent, SoundEvent> KNEF_BOW_SPLASH;
	public static final DeferredHolder<SoundEvent, SoundEvent> KNEF_BOW_STORM;
	public static final DeferredHolder<SoundEvent, SoundEvent> KNEF_BOW_STORM_SHORT;

	public static final DeferredHolder<SoundEvent, SoundEvent> MONTU_SLAP;

	public static final DeferredHolder<SoundEvent, SoundEvent> SELI_HORN_BLOW;
	public static final DeferredHolder<SoundEvent, SoundEvent> SELI_HORN_BLOW_END;
	public static final DeferredHolder<SoundEvent, SoundEvent> SELI_HORN_WAVE;

	public SoundsRegistry() {
	}

	static {
		SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, "relics_thirteenflames");

		KNEF_BOW_SHOT = SOUNDS.register("knef_shot", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("relics_thirteenflames", "knef_shot")));
		KNEF_BOW_RAIN = SOUNDS.register("knef_rain", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("relics_thirteenflames", "knef_rain")));
		KNEF_BOW_SPLASH = SOUNDS.register("knef_splash", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("relics_thirteenflames", "knef_splash")));
		KNEF_BOW_STORM = SOUNDS.register("knef_storm", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("relics_thirteenflames", "knef_storm")));
		KNEF_BOW_STORM_SHORT = SOUNDS.register("knef_storm_2", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("relics_thirteenflames", "knef_storm_2")));
		MONTU_SLAP = SOUNDS.register("montu_slap", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("relics_thirteenflames", "montu_slap")));
		SELI_HORN_BLOW = SOUNDS.register("seli_horn_blow", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("seli_horn_blow")));
		SELI_HORN_BLOW_END = SOUNDS.register("seli_horn_blow_end", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("seli_horn_blow_end")));
		SELI_HORN_WAVE = SOUNDS.register("seli_horn_wave", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("seli_horn_wave")));

	}
}