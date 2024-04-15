package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.content.entities.data.EntityDataSerializerHL;
import com.qurenie.relics_thirteenflames.content.items.ItemKnefRose;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

@SimplyRegister
public interface EntityDataSerializersTF
{
	@RegistryName("knef_rose_stats")
	EntityDataSerializerHL<ItemKnefRose.RoseStats> KNEF_ROSE_STATS = EntityDataSerializerHL.simpleCopyable(
			(buf, val) -> buf.writeNbt(val.serializeNBT()),
			buf -> new ItemKnefRose.RoseStats(buf.readNbt()),
			stats -> new ItemKnefRose.RoseStats(stats.serializeNBT())
	);
}