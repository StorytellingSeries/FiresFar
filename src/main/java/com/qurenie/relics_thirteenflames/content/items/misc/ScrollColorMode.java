package com.qurenie.relics_thirteenflames.content.items.misc;

import com.mojang.serialization.Codec;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Locale;
import java.util.function.IntFunction;

public enum ScrollColorMode implements StringRepresentable {
    GREEN(new Color(117, 241, 104), MobEffects.LUCK, ThirteenFlames.rl("textures/gui/scroll_of_truth/scroll_gui_green.png"),0),
    RED(new Color(246, 50, 74), MobEffects.DAMAGE_BOOST, ThirteenFlames.rl("textures/gui/scroll_of_truth/scroll_gui_red.png"),1),
    GRAY(new Color(221, 228, 239), MobEffects.MOVEMENT_SPEED, ThirteenFlames.rl("textures/gui/scroll_of_truth/scroll_gui_white.png"),2),
    YELLOW(new Color(255, 211, 77), MobEffects.DIG_SPEED, ThirteenFlames.rl("textures/gui/scroll_of_truth/scroll_gui_yellow.png"),3),
    BLUE(new Color(65, 164, 255), MobEffects.JUMP, ThirteenFlames.rl("textures/gui/scroll_of_truth/scroll_gui_blue.png"),4),
    CYAN(new Color(44, 255, 206), MobEffects.DOLPHINS_GRACE, ThirteenFlames.rl("textures/gui/scroll_of_truth/scroll_gui_cyan.png"),5),
    DARK_GRAY(new Color(63, 72, 80), MobEffects.DAMAGE_RESISTANCE, ThirteenFlames.rl("textures/gui/scroll_of_truth/scroll_gui_gray.png"),6)
    ;
    
    public static final Codec<ScrollColorMode> CODEC = StringRepresentable.fromEnum(ScrollColorMode::values);
    public static final IntFunction<ScrollColorMode> BY_ID = ByIdMap.continuous(p_335877_ -> p_335877_.id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, ScrollColorMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, p_335484_ -> p_335484_.id);
    
    public final Color color;
    public final Holder<MobEffect> effect;
    public final ResourceLocation gui;
    public final int id;
    
    ScrollColorMode(Color color, Holder<MobEffect> effect, ResourceLocation loc, int id){
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
        return ScrollColorMode.valueOf(id);
    }
    
    @Override
    public @NotNull String getSerializedName() {
        return this.name();
    }
}
