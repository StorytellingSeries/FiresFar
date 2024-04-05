package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.client.render.tile.TESRShaking;
import com.qurenie.relics_thirteenflames.content.tiles.TileShaking;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;
import org.zeith.hammerlib.annotations.client.TileRenderer;

import static org.zeith.hammerlib.api.forge.BlockAPI.createBlockEntityType;

@SimplyRegister
public interface TilesRegistry {

    @RegistryName("shaking")
    @TileRenderer(TESRShaking.class)
    BlockEntityType<TileShaking> SHAKING = createBlockEntityType(TileShaking::new, BlocksRegistry.SHAKING);
}
