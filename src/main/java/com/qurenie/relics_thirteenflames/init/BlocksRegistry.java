package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.blocks.AurithecBeaconBlock;
import com.qurenie.relics_thirteenflames.content.blocks.BlockShaking;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.zeith.hammerlib.annotations.Ref;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

@SimplyRegister(creativeTabs = @Ref(ThirteenFlames.class))
public interface BlocksRegistry {
    
    @RegistryName("shaking")
    BlockShaking SHAKING = new BlockShaking(BlockBehaviour.Properties.of());
    
    @RegistryName("auritekh_beacon")
    AurithecBeaconBlock BEACON = new AurithecBeaconBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEACON));
    
}
