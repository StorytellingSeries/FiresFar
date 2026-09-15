package com.qurenie.relics_thirteenflames.init;

import com.mojang.serialization.Codec;
import com.qurenie.relics_thirteenflames.activity.ActivitiesData;
import com.qurenie.relics_thirteenflames.content.entities.EntitySeliasetSun;
import com.qurenie.relics_thirteenflames.content.items.misc.JodahTier;
import com.qurenie.relics_thirteenflames.content.items.misc.MaskState;
import com.qurenie.relics_thirteenflames.content.items.misc.ScrollColorMode;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;
import org.zeith.hammerlib.api.registrars.Registrar;

import java.util.List;
import java.util.UUID;

@SimplyRegister
public class ComponentRegistry {

    @RegistryName("activities")
    public static final Registrar<DataComponentType<ActivitiesData>> ACTIVITIES = Registrar.dataComponentType(DataComponentType.<ActivitiesData>builder()
            .persistent(ActivitiesData.CODEC)
            .networkSynchronized(ActivitiesData.STREAM_CODEC).cacheEncoding());

    @RegistryName("nether_ticker")
    public static final Registrar<DataComponentType<Integer>> NETHER_TICKER = Registrar.dataComponentType(DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT).cacheEncoding());

    @RegistryName("entity_uuid")
    public static final Registrar<DataComponentType<UUID>> ENTITY_UUID = Registrar.dataComponentType(DataComponentType.<UUID>builder()
            .persistent(UUIDUtil.CODEC)
            .networkSynchronized(UUIDUtil.STREAM_CODEC).cacheEncoding());

    @RegistryName("activity_uuid")
    public static final Registrar<DataComponentType<UUID>> UNIQUE_UUID = Registrar.dataComponentType(DataComponentType.<UUID>builder()
            .persistent(UUIDUtil.CODEC)
            .networkSynchronized(UUIDUtil.STREAM_CODEC).cacheEncoding());

    @RegistryName("target_type")
    public static final Registrar<DataComponentType<String>> TARGET_TYPE = Registrar.dataComponentType(DataComponentType.<String>builder()
            .persistent(Codec.STRING)
            .networkSynchronized(ByteBufCodecs.STRING_UTF8).cacheEncoding());

    @SuppressWarnings({"unchecked", "rawtypes"})
    @RegistryName("target_types")
    public static final Registrar<DataComponentType<List<EntityType<?>>>> TARGET_TYPES =
            Registrar.dataComponentType(DataComponentType.<List<EntityType<?>>>builder()
                    .persistent(BuiltInRegistries.ENTITY_TYPE.byNameCodec().listOf())
                    .networkSynchronized(ByteBufCodecs.list().apply((StreamCodec) ByteBufCodecs.registry(Registries.ENTITY_TYPE)))
                    .cacheEncoding()
            );

    @RegistryName("level")
    public static final Registrar<DataComponentType<Integer>> LEVEL = Registrar.dataComponentType(DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT).cacheEncoding());

    @RegistryName("size")
    public static final Registrar<DataComponentType<Integer>> SIZE = Registrar.dataComponentType(DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT).cacheEncoding());

    @RegistryName("montu_aoe")
    public static final Registrar<DataComponentType<Integer>> MONTU_AOE = Registrar.dataComponentType(DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT).cacheEncoding());

    @RegistryName("blocks_mined")
    public static final Registrar<DataComponentType<Integer>> BLOCKS_MINED = Registrar.dataComponentType(DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT).cacheEncoding());

    @RegistryName("active_tick")
    public static final Registrar<DataComponentType<Integer>> ACTIVE_TICK = Registrar.dataComponentType(DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT).cacheEncoding());

    @RegistryName("cooldown")
    public static final Registrar<DataComponentType<Integer>> COOLDOWN = Registrar.dataComponentType(DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT).cacheEncoding());

    @RegistryName("traveller_active_tick")
    public static final Registrar<DataComponentType<Integer>> TRAVELLER_ACTIVE_TICK = Registrar.dataComponentType(DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT).cacheEncoding());

    @RegistryName("direction")
    public static final Registrar<DataComponentType<Vec3>> DIRECTION = Registrar.dataComponentType(DataComponentType.<Vec3>builder()
            .persistent(Vec3.CODEC)
            .cacheEncoding());

    @RegistryName("last_pos")
    public static final Registrar<DataComponentType<Vec3>> LAST_POS = Registrar.dataComponentType(DataComponentType.<Vec3>builder()
            .persistent(Vec3.CODEC)
            .cacheEncoding());

    @RegistryName("jodah_active_tick")
    public static final Registrar<DataComponentType<Integer>> JODAH_ACTIVE_TICK = Registrar.dataComponentType(DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT).cacheEncoding());

    @RegistryName("bines")
    public static final Registrar<DataComponentType<Integer>> SOULS = Registrar.dataComponentType(DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT).cacheEncoding());

    @RegistryName("charge")
    public static final Registrar<DataComponentType<Integer>> CHARGE = Registrar.dataComponentType(DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT).cacheEncoding());

    @RegistryName("rhonas_charged")
    public static final Registrar<DataComponentType<Integer>> RHONAS_CHARGES = Registrar.dataComponentType(DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT).cacheEncoding());

    @RegistryName("rhonas_blocked_damage")
    public static final Registrar<DataComponentType<Float>> RHONAS_BLOCKED = Registrar.dataComponentType(DataComponentType.<Float>builder()
            .persistent(Codec.FLOAT)
            .networkSynchronized(ByteBufCodecs.FLOAT).cacheEncoding());

    @RegistryName("seliaset_item_data")
    public static final Registrar<DataComponentType<ItemStack>> SELIASET_ITEM_DATA = Registrar.dataComponentType(DataComponentType.<ItemStack>builder()
            .persistent(ItemStack.CODEC)
            .networkSynchronized(ItemStack.STREAM_CODEC).cacheEncoding());

    @RegistryName("seliaset_sun_data")
    public static final Registrar<DataComponentType<EntitySeliasetSun.SeliasetSunData>> SELIASET_SUN_DATA = Registrar.dataComponentType(DataComponentType.<EntitySeliasetSun.SeliasetSunData>builder()
            .persistent(EntitySeliasetSun.SeliasetSunData.CODEC)
            .networkSynchronized(EntitySeliasetSun.SeliasetSunData.STREAM_CODEC).cacheEncoding());

    @RegistryName("charging_ticker")
    public static final Registrar<DataComponentType<Integer>> CHARGING_TICKER = Registrar.dataComponentType(DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT).cacheEncoding());

    @RegistryName("speed")
    public static final Registrar<DataComponentType<Float>> SPEED = Registrar.dataComponentType(DataComponentType.<Float>builder()
            .persistent(Codec.FLOAT)
            .networkSynchronized(ByteBufCodecs.FLOAT).cacheEncoding());

    @RegistryName("deterioration_ticker")
    public static final Registrar<DataComponentType<Integer>> DETERIORATION_TICKER = Registrar.dataComponentType(DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT).cacheEncoding());

    @RegistryName("scroll_xp_counter")
    public static final Registrar<DataComponentType<Integer>> SCROLL_XP_COUNTER = Registrar.dataComponentType(DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT).cacheEncoding());

    @RegistryName("rhonas_blcked")
    public static final Registrar<DataComponentType<Boolean>> BLOCKED = Registrar.dataComponentType(DataComponentType.<Boolean>builder()
            .persistent(Codec.BOOL)
            .networkSynchronized(ByteBufCodecs.BOOL).cacheEncoding());

    @RegistryName("teleporting")
    public static final Registrar<DataComponentType<Boolean>> TELEPORTING = Registrar.dataComponentType(DataComponentType.<Boolean>builder()
            .persistent(Codec.BOOL)
            .networkSynchronized(ByteBufCodecs.BOOL).cacheEncoding());

    @RegistryName("shifting")
    public static final Registrar<DataComponentType<Boolean>> SHIFTING = Registrar.dataComponentType(DataComponentType.<Boolean>builder()
            .persistent(Codec.BOOL)
            .networkSynchronized(ByteBufCodecs.BOOL).cacheEncoding());

    @RegistryName("pull")
    public static final Registrar<DataComponentType<Float>> PULL = Registrar.dataComponentType(DataComponentType.<Float>builder()
            .persistent(Codec.FLOAT)
            .networkSynchronized(ByteBufCodecs.FLOAT).cacheEncoding());

    @RegistryName("scroll_color_mode")
    public static final Registrar<DataComponentType<ScrollColorMode>> SCROLL_COLOR_MODE = Registrar.dataComponentType(DataComponentType.<ScrollColorMode>builder()
            .persistent(ScrollColorMode.CODEC).networkSynchronized(ScrollColorMode.STREAM_CODEC).cacheEncoding());

    @RegistryName("jodah_tier")
    public static final Registrar<DataComponentType<JodahTier>> JODAH_TIER = Registrar.dataComponentType(DataComponentType.<JodahTier>builder()
            .persistent(JodahTier.CODEC).networkSynchronized(JodahTier.STREAM_CODEC).cacheEncoding());

    @RegistryName("cluster_mask_state")
    public static final Registrar<DataComponentType<MaskState>> CLUSTERS_MASK_STATE = Registrar.dataComponentType(DataComponentType.<MaskState>builder()
            .persistent(MaskState.CODEC).networkSynchronized(MaskState.STREAM_CODEC).cacheEncoding());

    @RegistryName("mask_state")
    public static final Registrar<DataComponentType<MaskState>> MASK_STATE = Registrar.dataComponentType(DataComponentType.<MaskState>builder()
            .persistent(MaskState.CODEC).networkSynchronized(MaskState.STREAM_CODEC).cacheEncoding());

    @RegistryName("mask_state_f")
    public static final Registrar<DataComponentType<Float>> MASK_STATE_F = Registrar.dataComponentType(DataComponentType.<Float>builder()
            .persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT).cacheEncoding());

    @RegistryName("skint_charges")
    public static final Registrar<DataComponentType<Integer>> SKINT_CHARGES = Registrar.dataComponentType(DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT).cacheEncoding());

    @RegistryName("antiskint_charges")
    public static final Registrar<DataComponentType<Integer>> ANTISKINT_CHARGES = Registrar.dataComponentType(DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT).cacheEncoding());

}
