package com.qurenie.relics_thirteenflames.activity.call;

import com.qurenie.relics_thirteenflames.init.KeyBindRegistry;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber
public class ActivityInputHandler {

    @Getter
    private static boolean isHolding = false;


    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean currentlyDown = KeyBindRegistry.ACTIVITY_KEY.isDown();

        if (currentlyDown && !isHolding) {
            isHolding = true;
        }

        if (!currentlyDown && isHolding) {
            isHolding = false;
        }
    }

}
