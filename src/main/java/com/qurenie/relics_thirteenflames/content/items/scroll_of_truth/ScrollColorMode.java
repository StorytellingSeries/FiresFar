package com.qurenie.relics_thirteenflames.content.items.scroll_of_truth;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;
import java.util.Locale;
import java.util.function.Supplier;

public enum ScrollColorMode {
    GREEN(new Color(117, 241, 104), ()->MobEffects.LUCK, ThirteenFlames.rl("textures/hud/scroll_of_truth/scroll_gui_green.png"),0),
    RED(new Color(246, 50, 74), ()->MobEffects.DAMAGE_BOOST, ThirteenFlames.rl("textures/hud/scroll_of_truth/scroll_gui_red.png"),1),
    GRAY(new Color(221, 228, 239), ()->MobEffects.MOVEMENT_SPEED, ThirteenFlames.rl("textures/hud/scroll_of_truth/scroll_gui_white.png"),2),
    YELLOW(new Color(255, 211, 77), ()->MobEffects.DIG_SPEED, ThirteenFlames.rl("textures/hud/scroll_of_truth/scroll_gui_yellow.png"),3),
    BLUE(new Color(65, 164, 255), ()->MobEffects.JUMP, ThirteenFlames.rl("textures/hud/scroll_of_truth/scroll_gui_blue.png"),4),
    CYAN(new Color(44, 255, 206), ()->MobEffects.DOLPHINS_GRACE, ThirteenFlames.rl("textures/hud/scroll_of_truth/scroll_gui_cyan.png"),5),
    DARK_GRAY(new Color(63, 72, 80), ()->MobEffects.DAMAGE_RESISTANCE, ThirteenFlames.rl("textures/hud/scroll_of_truth/scroll_gui_gray.png"),6)
    ;

    public Color color;
    public Supplier<MobEffect> effect;
    public ResourceLocation gui;
    public int id;
    ScrollColorMode(Color color, Supplier<MobEffect> effect, ResourceLocation loc, int id){
        this.color = color;
        this.effect = effect;
        this.gui = loc;
        this.id = id;
    }

    public void toTag(CompoundTag tag){
        tag.putString("scrollColorMode",this.name().toUpperCase(Locale.ROOT));
    }

    public static ScrollColorMode fromTag(CompoundTag tag){
        if (!tag.contains("scrollColorMode")){
            GREEN.toTag(tag);
        }
        String id = tag.getString("scrollColorMode");
        ScrollColorMode mode =  ScrollColorMode.valueOf(id);
        return mode;
    }

    public static MobEffect safeGetEffect(ResourceLocation effectName){
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectName);
        if (effect == null) return MobEffects.JUMP;
        return effect;
    }
}
