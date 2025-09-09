package com.qurenie.relics_thirteenflames.content.items.misc;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;

public enum JodahTier implements StringRepresentable {
    S(-1, 16, 6, Thief.of(2, 8, 24), OneThousandEyes.of(2, 2.0f)),
    A(7, 8, 3, Thief.of(1, 6, 16), OneThousandEyes.of(2, 1.6f)),
    B(6, 4, 1, Thief.of(1, 4, 11), OneThousandEyes.of(1, 1.35f)),
    C(5, 2, 0.5f, Thief.of(0.5f, 3, 7), OneThousandEyes.of(1, 1.15f)),
    D(5, 0, 0, Thief.of(0.5f, 2, 4), OneThousandEyes.of(1, 1.0f));
    
    public static final Codec<JodahTier> CODEC = StringRepresentable.fromEnum(JodahTier::values);
    public static final IntFunction<JodahTier> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, JodahTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
    public final int hitCount;
    public final int xpSuck;
    public final float damageIncrease;
    public final Thief thief;
    public final OneThousandEyes oneThousandEyes;
    
    JodahTier(int hitCount, int xpSuck, float damageIncrease, Thief thief, OneThousandEyes oneThousandEyes) {
        this.hitCount = hitCount;
        this.xpSuck = xpSuck;
        this.damageIncrease = damageIncrease;
        this.thief = thief;
        this.oneThousandEyes = oneThousandEyes;
    }
    
    @Override
    public @NotNull String getSerializedName() {
        return this.name();
    }
    
    public JodahTier previous() {
        return this == D ? D : JodahTier.values()[ordinal() + 1];
    }
    
    public JodahTier next() {
        return this == S ? S : JodahTier.values()[ordinal() - 1];
    }
    
    public record Thief(float health, int xp, int radius) {
        
        public static Thief of(float health, int xp, int radius) {
            return new Thief(health, xp, radius);
        }
        
    }
    
    public record OneThousandEyes(int targetingCount, float damageMultiplier) {
        
        public static OneThousandEyes of(int targetingCount, float damageMultiplier) {
            return new OneThousandEyes(targetingCount, damageMultiplier);
        }
        
    }
}
