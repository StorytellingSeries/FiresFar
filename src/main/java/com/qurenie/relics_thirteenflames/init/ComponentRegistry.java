package com.qurenie.relics_thirteenflames.init;

import com.mojang.serialization.Codec;
import com.qurenie.relics_thirteenflames.content.entities.EntitySeliasetSun;
import com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.ScrollColorMode;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;
import org.zeith.hammerlib.api.registrars.Registrar;

@SimplyRegister
public class ComponentRegistry {
    
    @RegistryName("nether_ticker")
    public static final Registrar<DataComponentType<Integer>> NETHER_TICKER = Registrar.dataComponentType(DataComponentType.<Integer>builder()
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
    
    @RegistryName("bines")
    public static final Registrar<DataComponentType<Integer>> BONES = Registrar.dataComponentType(DataComponentType.<Integer>builder()
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
    
    
}
