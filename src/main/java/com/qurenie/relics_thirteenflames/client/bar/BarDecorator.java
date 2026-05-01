package com.qurenie.relics_thirteenflames.client.bar;

import com.qurenie.api.IBarContainer;
import it.hurts.octostudios.octolib.util.OctoColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import org.jetbrains.annotations.NotNull;

public class BarDecorator implements IItemDecorator {

    public static final BarDecorator INSTANCE = new BarDecorator();

    @Override
    public boolean render(@NotNull GuiGraphics guiGraphics, @NotNull Font font, @NotNull ItemStack stack, int x, int y) {
        if (!(stack.getItem() instanceof IBarContainer container)) return false;

        boolean isLast = !stack.isBarVisible();
        int j = x + 2;
        int k = y + 13 - (!isLast ? 1 : 0);

        int i = 0;
        for (IBarSetting setting : container.getBarSettings()) {
            OctoColor color = setting.getColor(i++);

            if (!setting.isBarVisible(stack, Minecraft.getInstance().player))
                continue;

            int l = getBarWidth(stack, Minecraft.getInstance().player, setting);

            guiGraphics.fill(RenderType.guiOverlay(), j, k, j + 13, k + (isLast ? 2 : 1), -16777216);
            guiGraphics.fill(RenderType.guiOverlay(), j, k, j + l, k + 1, color.getARGB() | 0xFF000000);

            k -= 1;
            isLast = false;
        }

        return true;
    }

    private int getBarWidth(ItemStack stack, Player player, IBarSetting setting) {
        double maxValue = setting.getMaxValue(stack, player);
        double value = setting.getValue(stack, player);

        if (setting.inverse())
            value = maxValue - value;

        return (int) (13 * value / maxValue);
    }

}
