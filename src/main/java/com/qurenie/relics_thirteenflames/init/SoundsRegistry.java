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

	// --- Existing sounds ---
	public static final DeferredHolder<SoundEvent, SoundEvent> KNEF_BOW_SHOT;
	public static final DeferredHolder<SoundEvent, SoundEvent> KNEF_BOW_RAIN;
	public static final DeferredHolder<SoundEvent, SoundEvent> KNEF_BOW_SPLASH;
	public static final DeferredHolder<SoundEvent, SoundEvent> KNEF_BOW_STORM;
	public static final DeferredHolder<SoundEvent, SoundEvent> KNEF_BOW_STORM_SHORT;

	public static final DeferredHolder<SoundEvent, SoundEvent> MONTU_SLAP;

	public static final DeferredHolder<SoundEvent, SoundEvent> SELI_HORN_BLOW;
	public static final DeferredHolder<SoundEvent, SoundEvent> SELI_HORN_BLOW_END;
	public static final DeferredHolder<SoundEvent, SoundEvent> SELI_HORN_WAVE;

	// --- New sounds (from 13flames_sfx archive) ---
	public static final DeferredHolder<SoundEvent, SoundEvent> ADVENTURER_SWORD_DASH;
	public static final DeferredHolder<SoundEvent, SoundEvent> ADVENTURER_SWORD_HIT;
	public static final DeferredHolder<SoundEvent, SoundEvent> ADVENTURER_SWORD_SPIN_HIT;
	public static final DeferredHolder<SoundEvent, SoundEvent> ADVENTURER_SWORD_UPDOWN_HIT;
	public static final DeferredHolder<SoundEvent, SoundEvent> ARROW_REFLECT;
	public static final DeferredHolder<SoundEvent, SoundEvent> BOOK_APPEAR;
	public static final DeferredHolder<SoundEvent, SoundEvent> BOOK_ATTACK;
	public static final DeferredHolder<SoundEvent, SoundEvent> BOOK_CLOSE;
	public static final DeferredHolder<SoundEvent, SoundEvent> BOOK_MOB_CAPTURE;
	public static final DeferredHolder<SoundEvent, SoundEvent> BOOK_RESPAWN;
	public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_ABILITY_CAST;
	public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_APPEAR;
	public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_DISSAPPEAR;
	public static final DeferredHolder<SoundEvent, SoundEvent> INTERWORLD_TELEPORT_IN;
	public static final DeferredHolder<SoundEvent, SoundEvent> INTERWORLD_TELEPORT_OUT;
	public static final DeferredHolder<SoundEvent, SoundEvent> JODAH_STAFF_RANK_UP;
	public static final DeferredHolder<SoundEvent, SoundEvent> JODAH_STAFF_TELEPORT;
	public static final DeferredHolder<SoundEvent, SoundEvent> JODAH_WINGS_APPEAR;
	public static final DeferredHolder<SoundEvent, SoundEvent> KNEFMTITI_ROSE_GHOST_SPAWN;
	public static final DeferredHolder<SoundEvent, SoundEvent> KNEFMTITI_ROSE_ORB;
	public static final DeferredHolder<SoundEvent, SoundEvent> KNEFMTITI_ROSE_SOUL_SAND;
	public static final DeferredHolder<SoundEvent, SoundEvent> LIFE_ORB_GET;
	public static final DeferredHolder<SoundEvent, SoundEvent> LIFE_STEALING;
	public static final DeferredHolder<SoundEvent, SoundEvent> METEORITE_FLY;
	public static final DeferredHolder<SoundEvent, SoundEvent> MONTU_HAMMER_DRILL;
	public static final DeferredHolder<SoundEvent, SoundEvent> MONTU_HAMMER_GUI;
	public static final DeferredHolder<SoundEvent, SoundEvent> REVERSAL_ABERRATION;
	public static final DeferredHolder<SoundEvent, SoundEvent> RONAS_SHIELD_BLOCK;
	public static final DeferredHolder<SoundEvent, SoundEvent> SELIASET_HORN_BALL_EXPLODE;
	public static final DeferredHolder<SoundEvent, SoundEvent> SELIASET_HORN_BALL_LAUNCH;
	public static final DeferredHolder<SoundEvent, SoundEvent> SELIASET_HORN_WIND;
	public static final DeferredHolder<SoundEvent, SoundEvent> SELIASET_SUN_FLAME_BREAK;
	public static final DeferredHolder<SoundEvent, SoundEvent> SELIASET_SUN_FLAME_SPAWN;
	public static final DeferredHolder<SoundEvent, SoundEvent> SPARK_TELEPORT;
	public static final DeferredHolder<SoundEvent, SoundEvent> KNEFMTITI_ROSE_GHOST_HIT;
	public static final DeferredHolder<SoundEvent, SoundEvent> JODAH_STAFF_CAST_CIRCLE;
	public static final DeferredHolder<SoundEvent, SoundEvent> ADVENTURER_SWORD_UPDOWN_HIT_FAST;


	public SoundsRegistry() {
	}

	static {
		SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, "relics_thirteenflames");

		// --- Existing sounds ---
		KNEF_BOW_SHOT = SOUNDS.register("knef_shot", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("relics_thirteenflames", "knef_shot")));
		KNEF_BOW_RAIN = SOUNDS.register("knef_rain", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("relics_thirteenflames", "knef_rain")));
		KNEF_BOW_SPLASH = SOUNDS.register("knef_splash", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("relics_thirteenflames", "knef_splash")));
		KNEF_BOW_STORM = SOUNDS.register("knef_storm", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("relics_thirteenflames", "knef_storm")));
		KNEF_BOW_STORM_SHORT = SOUNDS.register("knef_storm_2", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("relics_thirteenflames", "knef_storm_2")));
		MONTU_SLAP = SOUNDS.register("montu_slap", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("relics_thirteenflames", "montu_slap")));
		SELI_HORN_BLOW = SOUNDS.register("seli_horn_blow", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("seli_horn_blow")));
		SELI_HORN_BLOW_END = SOUNDS.register("seli_horn_blow_end", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("seli_horn_blow_end")));
		SELI_HORN_WAVE = SOUNDS.register("seli_horn_wave", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("seli_horn_wave")));

		// --- New sounds (from 13flames_sfx archive) ---
		ADVENTURER_SWORD_DASH = SOUNDS.register("adventurer_sword_dash", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("adventurer_sword_dash")));
		ADVENTURER_SWORD_HIT = SOUNDS.register("adventurer_sword_hit", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("adventurer_sword_hit")));
		ADVENTURER_SWORD_SPIN_HIT = SOUNDS.register("adventurer_sword_spin_hit", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("adventurer_sword_spin_hit")));
		ADVENTURER_SWORD_UPDOWN_HIT = SOUNDS.register("adventurer_sword_updown_hit", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("adventurer_sword_updown_hit")));
		ARROW_REFLECT = SOUNDS.register("arrow_reflect", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("arrow_reflect")));
		BOOK_APPEAR = SOUNDS.register("book_appear", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("book_appear")));
		BOOK_ATTACK = SOUNDS.register("book_attack", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("book_attack")));
		BOOK_CLOSE = SOUNDS.register("book_close", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("book_close")));
		BOOK_MOB_CAPTURE = SOUNDS.register("book_mob_capture", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("book_mob_capture")));
		BOOK_RESPAWN = SOUNDS.register("book_respawn", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("book_respawn")));
		CRYSTAL_ABILITY_CAST = SOUNDS.register("crystal_ability_cast", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("crystal_ability_cast")));
		CRYSTAL_APPEAR = SOUNDS.register("crystal_appear", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("crystal_appear")));
		CRYSTAL_DISSAPPEAR = SOUNDS.register("crystal_dissappear", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("crystal_dissappear")));
		INTERWORLD_TELEPORT_IN = SOUNDS.register("interworld_teleport_in", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("interworld_teleport_in")));
		INTERWORLD_TELEPORT_OUT = SOUNDS.register("interworld_teleport_out", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("interworld_teleport_out")));
		JODAH_STAFF_RANK_UP = SOUNDS.register("jodah_staff_rank_up", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("jodah_staff_rank_up")));
		JODAH_STAFF_TELEPORT = SOUNDS.register("jodah_staff_teleport", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("jodah_staff_teleport")));
		JODAH_WINGS_APPEAR = SOUNDS.register("jodah_wings_appear", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("jodah_wings_appear")));
		KNEFMTITI_ROSE_GHOST_SPAWN = SOUNDS.register("knefmtiti_rose_ghost_spawn", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("knefmtiti_rose_ghost_spawn")));
		KNEFMTITI_ROSE_ORB = SOUNDS.register("knefmtiti_rose_orb", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("knefmtiti_rose_orb")));
		KNEFMTITI_ROSE_SOUL_SAND = SOUNDS.register("knefmtiti_rose_soul_sand", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("knefmtiti_rose_soul_sand")));
		LIFE_ORB_GET = SOUNDS.register("life_orb_get", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("life_orb_get")));
		LIFE_STEALING = SOUNDS.register("life_stealing", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("life_stealing")));
		METEORITE_FLY = SOUNDS.register("meteorite_fly", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("meteorite_fly")));
		MONTU_HAMMER_DRILL = SOUNDS.register("montu_hammer_drill", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("montu_hammer_drill")));
		MONTU_HAMMER_GUI = SOUNDS.register("montu_hammer_gui", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("montu_hammer_gui")));
		REVERSAL_ABERRATION = SOUNDS.register("reversal_aberration", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("reversal_aberration")));
		RONAS_SHIELD_BLOCK = SOUNDS.register("ronas_shield_block", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("ronas_shield_block")));
		SELIASET_HORN_BALL_EXPLODE = SOUNDS.register("seliaset_horn_ball_explode", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("seliaset_horn_ball_explode")));
		SELIASET_HORN_BALL_LAUNCH = SOUNDS.register("seliaset_horn_ball_launch", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("seliaset_horn_ball_launch")));
		SELIASET_HORN_WIND = SOUNDS.register("seliaset_horn_wind", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("seliaset_horn_wind")));
		SELIASET_SUN_FLAME_BREAK = SOUNDS.register("seliaset_sun_flame_break", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("seliaset_sun_flame_break")));
		SELIASET_SUN_FLAME_SPAWN = SOUNDS.register("seliaset_sun_flame_spawn", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("seliaset_sun_flame_spawn")));
		JODAH_STAFF_CAST_CIRCLE = SOUNDS.register("jodah_staff_cast_circle", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("jodah_staff_cast_circle")));
		KNEFMTITI_ROSE_GHOST_HIT = SOUNDS.register("knefmtiti_rose_ghost_hit", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("knefmtiti_rose_ghost_hit")));
		ADVENTURER_SWORD_UPDOWN_HIT_FAST = SOUNDS.register("adventurer_sword_updown_hit_fast", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("adventurer_sword_updown_hit_fast")));
		SPARK_TELEPORT = SOUNDS.register("spark_teleport", () -> SoundEvent.createVariableRangeEvent(ThirteenFlames.rl("spark_teleport")));
	}
}
