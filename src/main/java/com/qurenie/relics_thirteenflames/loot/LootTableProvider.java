package com.qurenie.relics_thirteenflames.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LootTableProvider extends net.minecraft.data.loot.LootTableProvider {
    
    private static final List<LootTableProvider.SubProviderEntry> SUB_PROVIDERS = List.of(
            new LootTableProvider.SubProviderEntry(EntityLootProvider::new, LootContextParamSets.ENTITY)
    );
    
    public LootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Collections.emptySet(), SUB_PROVIDERS, registries);
    }
    
}
