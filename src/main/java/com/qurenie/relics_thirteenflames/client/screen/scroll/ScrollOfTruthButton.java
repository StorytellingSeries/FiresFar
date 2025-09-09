package com.qurenie.relics_thirteenflames.client.screen.scroll;

import com.mojang.blaze3d.systems.RenderSystem;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.util.RenderTools;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class ScrollOfTruthButton extends Button implements IHoverableWidget {
    
    
    public static final ResourceLocation BUTTONS_LOCATION = ThirteenFlames.rl("textures/gui/scroll_of_truth/scroll_buttons.png");
    
    public ScrollOfTruthButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, Supplier::get);
    }
    
    
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BUTTONS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        guiGraphics.blit(BUTTONS_LOCATION, this.getX(), this.getY(), 0, i * 13, this.width / 2, this.height, 134, 39);
        guiGraphics.blit(BUTTONS_LOCATION, this.getX() + (int) Math.ceil(this.width / 2f), this.getY(), (float) (134 - Math.ceil(this.width / 2f)), (float) (i * 13), (int) Math.ceil(this.width / 2f), this.height, 134, 39);
        //this.renderBg(pPoseStack, minecraft, pMouseX, pMouseY);
        this.drawButtonText(guiGraphics, pMouseX, pMouseY, pPartialTick);
        if (this.isHovered()) {
            this.onHovered(guiGraphics, pMouseX, pMouseY);
        }
    }
    
    protected int getYImage(boolean pIsHovered) {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (pIsHovered) {
            i = 2;
        }
        
        return i;
    }
    
    public void drawButtonText(GuiGraphics guiGraphics, int mx, int my, float pticks) {
        int shadowColor = RenderTools.DEFAULT_SHADOW_COLOR;
        int textColor = RenderTools.DEFAULT_TEXT_COLOR;
        if (!this.active) {
            shadowColor = RenderTools.DEFAULT_TEXT_COLOR;
            textColor = RenderTools.DEFAULT_SHADOW_COLOR;
        }
        RenderTools.renderCenteredScaledText(guiGraphics, this.getMessage().getString(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 1f,
                shadowColor, textColor);
    }
    
    @Override
    public void onHovered(GuiGraphics guiGraphics, int i, int i1) {
    
    }
    
}
