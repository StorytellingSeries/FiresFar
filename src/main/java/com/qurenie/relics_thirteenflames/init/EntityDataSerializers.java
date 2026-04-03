package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.items.ItemKnefRose;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS;

public class EntityDataSerializers {
    
    public static final DeferredRegister<EntityDataSerializer<?>> DATA_SERIALIZERS = DeferredRegister.create(ENTITY_DATA_SERIALIZERS, ThirteenFlames.MODID);
    
    public static final Supplier<EntityDataSerializer<ItemKnefRose.RoseStats>> ROSE_STATS;
    
    static {
        ROSE_STATS = DATA_SERIALIZERS.register("knef_rose_stats", () -> EntityDataSerializer.forValueType(ItemKnefRose.RoseStats.STREAM_CODEC));
    }
}
