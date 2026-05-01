package com.qurenie.jei;

import com.qurenie.relics_thirteenflames.client.screen.gloves.MontuCompositeScreen;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class MontuGuiHandler implements IGuiContainerHandler<MontuCompositeScreen> {

    @Override
    public @NotNull List<Rect2i> getGuiExtraAreas(@NotNull MontuCompositeScreen containerScreen) {
        return List.of(
                new Rect2i(containerScreen.getGuiLeft(), containerScreen.getGuiTop(),
                        240, containerScreen.height)
        );
    }

}
