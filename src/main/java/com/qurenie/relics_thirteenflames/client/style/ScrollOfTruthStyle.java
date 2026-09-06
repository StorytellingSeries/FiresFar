package com.qurenie.relics_thirteenflames.client.style;

import com.qurenie.relics_thirteenflames.content.items.feather.ItemHettFeather;
import com.qurenie.relics_thirteenflames.content.items.misc.ScrollColorMode;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import it.hurts.sskirillss.relics.client.style.base.RelicStyle;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ScrollOfTruthStyle extends RelicStyle {

    @Override
    public @Nullable ResourceLocation getTooltipFrameTexture(LivingEntity entity, ItemStack stack) {
        ScrollColorMode mode = stack.getOrDefault(ComponentRegistry.SCROLL_COLOR_MODE, ScrollColorMode.GREEN);
        String itemName = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return ResourceLocation.fromNamespaceAndPath("relics", "textures/gui/tooltip/frame/" + itemName
                + "/scroll_tooltip_" + mode.name().toLowerCase() + ".png");
    }

}
