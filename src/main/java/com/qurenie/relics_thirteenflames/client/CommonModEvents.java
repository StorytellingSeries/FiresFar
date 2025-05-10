package com.qurenie.relics_thirteenflames.client;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.entities.EntitySeliasetSun;
import com.qurenie.relics_thirteenflames.content.entities.LivingFleshEntity;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;


@EventBusSubscriber(modid = ThirteenFlames.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CommonModEvents {


    @SubscribeEvent
    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.SELIASET_SUN, EntitySeliasetSun.createLivingAttributes().build());
        event.put(EntityRegistry.LIVING_FLESH, LivingFleshEntity.createMobAttributes().build());
    }

}