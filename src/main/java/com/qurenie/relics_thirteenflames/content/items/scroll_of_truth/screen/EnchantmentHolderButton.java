package com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qurenie.relics_thirteenflames.util.RenderTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class EnchantmentHolderButton extends ScrollOfTruthButton {

    private ScrollOfTruthContainerScreen.EnchantmentInstance enchantment;
    public EnchantmentHolderButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress);
    }



    public void setEnchantment(ScrollOfTruthContainerScreen.EnchantmentInstance enchantment) {
        this.enchantment = enchantment;
        this.setMessage(enchantment.enchantment.getFullname(enchantment.lvl));
    }

    public ScrollOfTruthContainerScreen.EnchantmentInstance getEnchantment() {
        return enchantment;
    }

    @Override
    public void drawButtonText(GuiGraphics guiGraphics, int mx, int my, float pticks) {
        int shadowColor = RenderTools.DEFAULT_SHADOW_COLOR;
        int textColor = RenderTools.DEFAULT_TEXT_COLOR;
        if (!this.active){
            shadowColor = RenderTools.DEFAULT_TEXT_COLOR;
            textColor = RenderTools.DEFAULT_SHADOW_COLOR;
        }
        String text = this.getMessage().getString();
        Font f = Minecraft.getInstance().font;
        float scale = 0.8f;
        int maxWidth = 55;
        if (f.width(text) * scale >= maxWidth) {
            if (enchantment.enchantment.getMaxLevel() == 1){
                int lastSpace = text.lastIndexOf(' ');
                if (lastSpace == -1){
                    lastSpace = text.length() - 1;
                }
                String name = text.substring(0,lastSpace);
                StringBuilder newName = new StringBuilder();
                int ch = 0;
                while (f.width(newName.toString()) * scale <= maxWidth && ch < name.length()){
                    newName.append(name.charAt(ch));
                    ch++;
                }
                newName.append("...");
                RenderTools.renderCenteredScaledText(guiGraphics, newName.toString(), this.getX() + this.width / 2, (int) (this.getY() + (this.height - 6 * scale) / 2), scale,
                        shadowColor, textColor);
            }else{
                int lastSpace = text.lastIndexOf(' ');
                String name = text.substring(0,lastSpace);
                String lvl = text.substring(lastSpace + 1);
                StringBuilder newName = new StringBuilder("");
                int ch = 0;
                while (f.width(newName.toString()) <= (40 - f.width(lvl)) && ch < name.length()){
                    newName.append(name.charAt(ch));
                    ch++;
                }
                newName.append("...").append(lvl);
                RenderTools.renderCenteredScaledText(guiGraphics, newName.toString(), this.getX() + this.width / 2, (int) (this.getY() + (this.height - 6 * scale) / 2), scale,
                        shadowColor, textColor);
            }
        } else {
            RenderTools.renderCenteredScaledText(guiGraphics, text, this.getX() + this.width / 2, (int) (this.getY() + (this.height - 6 * scale) / 2), scale,
                    shadowColor, textColor);
        }
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.onHovered(guiGraphics, mouseX, mouseY);

        if(this.isHovered) guiGraphics.renderTooltip(Minecraft.getInstance().font, getEnchantment().enchantment.getFullname(getEnchantment().lvl), mouseX, mouseY);
    }
}
