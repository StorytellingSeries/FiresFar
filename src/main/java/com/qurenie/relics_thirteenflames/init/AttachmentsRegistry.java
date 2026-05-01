package com.qurenie.relics_thirteenflames.init;

import com.mojang.serialization.Codec;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class AttachmentsRegistry {
    
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ThirteenFlames.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Float>> JODAH_SHEILD = ATTACHMENT_TYPES.register(
            "jodah_shield", () -> AttachmentType.builder(() -> 0f).serialize(Codec.FLOAT).build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> SKINT_DATA = ATTACHMENT_TYPES.register(
            "skint", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build()
    );
    
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> ANTISKINT_DATA = ATTACHMENT_TYPES.register(
            "antiskint", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build()
    );
    
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> PLANESHIFT_TICK = ATTACHMENT_TYPES.register(
            "planeshift", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build()
    );
    
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> WINGS_LAYER_DATA = ATTACHMENT_TYPES.register(
            "wings_layer", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build()
    );
    
    public static final StreamCodec<ByteBuf, AttachmentType<?>> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(NeoForgeRegistries.ATTACHMENT_TYPES::get, NeoForgeRegistries.ATTACHMENT_TYPES::getKey);

}
