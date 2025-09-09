package com.qurenie.relics_thirteenflames.client.particles.misc;

import com.mojang.serialization.Codec;
import com.qurenie.relics_thirteenflames.content.items.misc.JodahTier;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum RotationType  {
    
    PLANE,
    SIDE_RANDOM,
    TOTAL_RANDOM;
    
    public static final Codec<RotationType> ORDINAL_CODEC = Codec.INT.xmap(
            i -> RotationType.values()[i],
            Enum::ordinal
    );
    
    public static final IntFunction<RotationType> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, RotationType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
    
    
}
