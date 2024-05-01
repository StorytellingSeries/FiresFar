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
    GREEN(new Color(124,237,150), ()->MobEffects.LUCK, ThirteenFlames.rl("textures/hud/scroll_of_truth/scroll_gui_green.png"),0),
    RED(new Color(234,112,130), ()->MobEffects.DAMAGE_BOOST, ThirteenFlames.rl("textures/hud/scroll_of_truth/scroll_gui_red.png"),1),
    GRAY(new Color(230,236,237), ()->MobEffects.MOVEMENT_SPEED, ThirteenFlames.rl("textures/hud/scroll_of_truth/scroll_gui_gray.png"),2),
    YELLOW(new Color(255,218,126), ()->MobEffects.DIG_SPEED, ThirteenFlames.rl("textures/hud/scroll_of_truth/scroll_gui_yellow.png"),3),
    BLUE(new Color(109,191,241), ()->MobEffects.JUMP, ThirteenFlames.rl("textures/hud/scroll_of_truth/scroll_gui_blue.png"),4),
    CYAN(new Color(126,235,211), ()->MobEffects.DOLPHINS_GRACE, ThirteenFlames.rl("textures/hud/scroll_of_truth/scroll_gui_cyan.png"),5),
    DARK_GRAY(new Color(130,137,154), ()->MobEffects.DAMAGE_RESISTANCE, ThirteenFlames.rl("textures/hud/scroll_of_truth/scroll_gui_gray.png"),6)
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
