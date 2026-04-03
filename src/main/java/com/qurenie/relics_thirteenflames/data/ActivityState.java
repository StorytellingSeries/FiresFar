package com.qurenie.relics_thirteenflames.data;

import com.qurenie.relics_thirteenflames.activity.call.ActivityCallLogic;
import com.qurenie.relics_thirteenflames.activity.call.ActivityInputHandler;
import com.qurenie.relics_thirteenflames.activity.call.CallInput;
import com.qurenie.relics_thirteenflames.client.gui.ActivityCallGui;
import it.hurts.sskirillss.relics.api.events.utility.ContainerSlotClickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@EventBusSubscriber(value = Dist.CLIENT)
public class ActivityState {

    private static final LinkedHashMap<String, CallInput> CACHE = new LinkedHashMap<>();
    public static final int OPEN_DELAY = 5;

    public static void cache(LinkedHashMap<String, CallInput> map) {
        CACHE.clear();
        CACHE.putAll(map);
    }

    public static Collection<CallInput> getInputs() {
        return CACHE.values();
    }

    public static Set<String> getKeys() {
        return CACHE.keySet();
    }

    public static LinkedHashMap<String, CallInput> getState() {
        return CACHE;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            CACHE.clear();
        }
    }

}
