package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.content.blocks.BlockShaking;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

@SimplyRegister
public interface BlocksRegistry {

    @RegistryName("shaking")
    BlockShaking SHAKING = new BlockShaking(BlockBehaviour.Properties.of());
}
