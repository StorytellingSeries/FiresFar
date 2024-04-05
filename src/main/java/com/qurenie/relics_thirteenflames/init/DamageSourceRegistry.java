package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ThirteenFlames.MODID,bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DamageSourceRegistry {

    public static final ResourceKey<DamageType> SUCC_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(ThirteenFlames.MODID,"succ"));
    public static DamageSource SUCC;

    @SubscribeEvent
    public static void initiateDamageSources(ServerStartedEvent event){
        initializeDamageSources(event.getServer().registryAccess());
    }
    public static void initializeDamageSources(RegistryAccess access){
        Registry<DamageType> types = access.registryOrThrow(Registries.DAMAGE_TYPE);
        SUCC = new DamageSource(types.getHolderOrThrow(SUCC_TYPE));

    }

}
