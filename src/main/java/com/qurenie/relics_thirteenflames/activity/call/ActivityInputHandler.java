package com.qurenie.relics_thirteenflames.activity.call;

import com.qurenie.relics_thirteenflames.client.gui.ActivityCallGui;
import com.qurenie.relics_thirteenflames.data.ActivityState;
import com.qurenie.relics_thirteenflames.init.KeyBindRegistry;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;

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

    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        var window = mc.getWindow();
        double rawX = mc.mouseHandler.xpos();
        double rawY = mc.mouseHandler.ypos();

        double mx = rawX * (double) window.getGuiScaledWidth()  / (double) window.getScreenWidth();
        double my = rawY * (double) window.getGuiScaledHeight() / (double) window.getScreenHeight();

        if (mc.screen == null && ActivityCallGui.INSTANCE.onMouseClick(mx, my, event.getButton(), event.getAction()))
            event.setCanceled(true);
    }

}
