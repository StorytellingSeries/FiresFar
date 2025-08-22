package com.qurenie.relics_thirteenflames.loot;

import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class EntityLootProvider extends EntityLootSubProvider {
    
    public EntityLootProvider(HolderLookup.Provider registries) {
        super(FeatureFlags.REGISTRY.allFlags(), registries);
    }
    
    @Override
    public void generate() {
        add(EntityRegistry.LIVING_FLESH, LootTable.lootTable());
    }
    
    @Override
    protected @NotNull Stream<EntityType<?>> getKnownEntityTypes() {
        return Stream.of(EntityRegistry.LIVING_FLESH);
    }
    
}
