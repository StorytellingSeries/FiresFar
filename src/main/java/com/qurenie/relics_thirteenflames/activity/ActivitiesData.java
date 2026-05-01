package com.qurenie.relics_thirteenflames.activity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public record ActivitiesData(Map<String, ActivityData> activities) {

    public ActivitiesData {
        activities = Map.copyOf(activities);
    }

    public static final StreamCodec<ByteBuf, ActivitiesData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ActivityData.STREAM_CODEC),
                    ActivitiesData::activities,
                    map -> new ActivitiesData(Map.copyOf(map))
            );

    public static final Codec<ActivitiesData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(
                    Codec.STRING,
                    ActivityData.CODEC
            ).fieldOf("activities").forGetter(ActivitiesData::activities)
    ).apply(instance, ActivitiesData::new));

    public static ActivitiesData empty() {
        return new ActivitiesData(new HashMap<>());
    }

    public ActivitiesData reload(String activity) {
        ActivityData data = activities.getOrDefault(activity, new ActivityData(Integer.MIN_VALUE));

        return withActivity(activity, data.with(Integer.MIN_VALUE));
    }

    public long remains(String key, int currentTime) {
        ActivityData data = activities.get(key);

        if (data == null)
            return currentTime;

        return data.remains(currentTime);
    }

    public ActivitiesData withActivity(String key, ActivityData data) {
        HashMap<String, ActivityData> newMap = new HashMap<>(activities);
        newMap.put(key, data);
        return new ActivitiesData(newMap);
    }

    public ActivitiesData withoutActivity(String key) {
        HashMap<String, ActivityData> newMap = new HashMap<>(activities);
        newMap.remove(key);
        return new ActivitiesData(newMap);
    }

    public ActivitiesData putOrComputeIfPresent(
            String key,
            ActivityData defaultValue,
            Function<ActivityData, ActivityData> remappingFunction
    ) {
        ActivityData currentValue = activities.get(key);
        if (currentValue == null) {
            return withActivity(key, defaultValue);
        }

        ActivityData newValue = remappingFunction.apply(currentValue);
        if (newValue == currentValue) {
            return this;
        }

        HashMap<String, ActivityData> newMap = new HashMap<>(activities);
        newMap.put(key, newValue);
        return new ActivitiesData(newMap);
    }

    public ActivitiesData computeIfPresent(
            String key,
            Function<ActivityData, ActivityData> remappingFunction
    ) {
        ActivityData currentValue = activities.get(key);
        if (currentValue == null) {
            return this;
        }

        ActivityData newValue = remappingFunction.apply(currentValue);
        if (newValue == currentValue) {
            return this;
        }

        HashMap<String, ActivityData> newMap = new HashMap<>(activities);
        newMap.put(key, newValue);
        return new ActivitiesData(newMap);
    }
}
