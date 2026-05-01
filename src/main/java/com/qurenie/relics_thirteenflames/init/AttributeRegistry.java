package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.entities.EntitySeliasetSun;
import com.qurenie.relics_thirteenflames.content.entities.LivingFleshEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = ThirteenFlames.MODID)
public class AttributeRegistry {
    
    @SubscribeEvent
    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.SELIASET_SUN, EntitySeliasetSun.createLivingAttributes().build());
        event.put(EntityRegistry.LIVING_FLESH, LivingFleshEntity.createMobAttributes().build());
        event.put(EntityRegistry.SMALL_GHOST, LivingFleshEntity.createMobAttributes()
                .add(Attributes.FLYING_SPEED, 1.4).build());
        event.put(EntityRegistry.BIG_GHOST, LivingFleshEntity.createMobAttributes()
                .add(Attributes.FLYING_SPEED, 1).build());
        event.put(EntityRegistry.RESPAWN_BOOK, PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 20).build());
        event.put(EntityRegistry.FEATHER_VORTEX_ENTITY, LivingEntity.createLivingAttributes().build());
        event.put(EntityRegistry.SKINT_CLUSTER, LivingEntity.createLivingAttributes().build());
        event.put(EntityRegistry.TRAVELLER_CUT, LivingEntity.createLivingAttributes().build());
        event.put(EntityRegistry.TRAVELLER_SWEEP, LivingEntity.createLivingAttributes().build());
    }
    
}
