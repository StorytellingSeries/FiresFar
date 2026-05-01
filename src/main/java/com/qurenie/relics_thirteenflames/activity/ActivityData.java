package com.qurenie.relics_thirteenflames.activity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ActivityData(int reloadTime) {

    public int remains(int currentTime) {
        return Math.max(0, reloadTime - currentTime);
    }

    public ActivityData with(int reloadTime) {
        return new ActivityData(reloadTime);
    }

    public static final StreamCodec<ByteBuf, ActivityData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ActivityData::reloadTime,
            ActivityData::new
    );

    public static final Codec<ActivityData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("useTime").forGetter(ActivityData::reloadTime)
    ).apply(instance, ActivityData::new));
}
