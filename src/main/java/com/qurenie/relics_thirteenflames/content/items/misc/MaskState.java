package com.qurenie.relics_thirteenflames.content.items.misc;

import com.mojang.serialization.Codec;
import com.qurenie.relics_thirteenflames.style.ColorScheme;
import io.netty.buffer.ByteBuf;
import it.hurts.octostudios.octolib.util.OctoColor;
import lombok.Getter;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;

@Getter
public enum MaskState implements StringRepresentable {
    NEUTRAL("", 0xFFf5d3ff, ColorScheme.PURPLE_COLOR, true),
    SPARKLING("_scint", 0xFFfffee1, ColorScheme.GOLD_COLOR, true),
    DUSK("_scintonit", 0xFFe2dbe7, ColorScheme.GRAY_COLOR, false);
    
    private final String texturePostfix;
    private final int eyesColor;
    private final OctoColor color;
    private final boolean sparkling;
    
    public static final Codec<MaskState> CODEC = StringRepresentable.fromEnum(MaskState::values);
    public static final IntFunction<MaskState> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, MaskState> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
    
    MaskState(String texturePostfix, int eyesColor, OctoColor color, boolean sparkling) {
        this.texturePostfix = texturePostfix;
        this.eyesColor = eyesColor;
        this.color = color;
        this.sparkling = sparkling;
    }
    
    @Override
    public @NotNull String getSerializedName() {
        return name();
    }
}
