package com.qurenie.relics_thirteenflames.client.screen.gloves;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class MontuButton extends Button implements IHoverableWidget {
    
    public Direction direction;
    
    protected MontuButton(int x, int y, int width, int height, Direction direction, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, Supplier::get);
        this.direction = direction;
    }
    
    @Override
    public void onHovered(GuiGraphics guiGraphics, int i, int i1) {
    }
    
    private ResourceLocation getTexture(boolean isActive) {
        return ThirteenFlames.rl(String.format("textures/gui/gloves/montu_button_%s%s.png",
                direction == Direction.LEFT ? "left" : "right",
                isActive ? "_active" : ""));
    }
    
    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack stack = guiGraphics.pose();
        
        stack.pushPose();
        stack.translate(0, 0, 20);
        if (this.isHovered()) {
            this.onHovered(guiGraphics, mouseX, mouseY);
            guiGraphics.blit(getTexture(true), this.getX(), this.getY(), this.width, this.height, 0, 0, this.width, this.height, this.width, this.height);
        } else {
            guiGraphics.blit(getTexture(false), this.getX(), this.getY(), this.width, this.height, 0, 0, this.width, this.height, this.width, this.height);
        }
        stack.popPose();
    }
    
    public enum Direction {
        LEFT,
        RIGHT;
    }
}
