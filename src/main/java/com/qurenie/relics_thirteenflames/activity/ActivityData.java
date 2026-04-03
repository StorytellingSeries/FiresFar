package com.qurenie.relics_thirteenflames.activity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ActivityData(int recharge) {

    public ActivityData ticked(int maxValue) {
        return with(Math.min(recharge + 1, maxValue));
    }

    public ActivityData with(int recharge) {
        return new ActivityData(recharge);
    }

    public static final StreamCodec<ByteBuf, ActivityData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ActivityData::recharge,
            ActivityData::new
    );

    public static final Codec<ActivityData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("recharge").forGetter(ActivityData::recharge)
    ).apply(instance, ActivityData::new));
}
