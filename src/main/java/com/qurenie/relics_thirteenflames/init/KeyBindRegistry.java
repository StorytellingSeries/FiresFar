package com.qurenie.relics_thirteenflames.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public interface KeyBindRegistry {

    String CATEGORY = "key.category.relics_thirteenflames";

    KeyMapping ACTIVITY_KEY = new KeyMapping(
            "key.relics_thirteenflames.activity",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K, // клавиша по умолчанию
            CATEGORY
    );

    static void register(RegisterKeyMappingsEvent event) {
        event.register(ACTIVITY_KEY);
    }

}
