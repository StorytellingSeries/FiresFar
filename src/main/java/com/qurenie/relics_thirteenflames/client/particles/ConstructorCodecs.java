package com.qurenie.relics_thirteenflames.client.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.client.particles.BasicColoredParticle;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ConstructorCodecs {
    
    public static final MapCodec<BasicColoredParticle.Constructor> CONSTRUCTOR = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.INT.fieldOf("color").forGetter(c -> c.getColor().getRGB()),
                    Codec.FLOAT.fieldOf("diameter").forGetter(BasicColoredParticle.Constructor::getDiameter),
                    Codec.INT.fieldOf("lifetime").forGetter(BasicColoredParticle.Constructor::getLifetime),
                    Codec.FLOAT.fieldOf("roll").forGetter(BasicColoredParticle.Constructor::getRoll),
                    Codec.FLOAT.fieldOf("scaleModifier").forGetter(BasicColoredParticle.Constructor::getScaleModifier),
                    Codec.BOOL.fieldOf("visibleThroughWalls").forGetter(BasicColoredParticle.Constructor::isVisibleThroughWalls)
            ).apply(instance, (c, d, l, r, s, v) ->
                    BasicColoredParticle.Constructor.builder().color(c).diameter(d).lifetime(l).roll(r).scaleModifier(s).visibleThroughWalls(v).build()));
    
    public static final StreamCodec<ByteBuf, BasicColoredParticle.Constructor> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, c -> c.getColor().getRGB(),
            ByteBufCodecs.FLOAT, BasicColoredParticle.Constructor::getDiameter,
            ByteBufCodecs.INT, BasicColoredParticle.Constructor::getLifetime,
            ByteBufCodecs.FLOAT, BasicColoredParticle.Constructor::getRoll,
            ByteBufCodecs.FLOAT, BasicColoredParticle.Constructor::getScaleModifier,
            ByteBufCodecs.BOOL, BasicColoredParticle.Constructor::isVisibleThroughWalls,
            (c, d, l, r, s, v) ->
                    BasicColoredParticle.Constructor.builder().color(c).diameter(d).lifetime(l).roll(r).scaleModifier(s).visibleThroughWalls(v).build()
    );
    
}
