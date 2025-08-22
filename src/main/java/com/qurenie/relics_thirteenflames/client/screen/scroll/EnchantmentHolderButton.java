package com.qurenie.relics_thirteenflames.client.screen.scroll;

import com.qurenie.relics_thirteenflames.util.RenderTools;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

@Getter
public class EnchantmentHolderButton extends ScrollOfTruthButton {

    private EnchantmentInstance inst;
    
    public EnchantmentHolderButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress);
    }

    public void setInst(EnchantmentInstance inst) {
        this.inst = inst;
        this.setMessage(Enchantment.getFullname(inst.enchantment, inst.level));
    }
    
    public void decrement() {
        setInst(new EnchantmentInstance(inst.enchantment, inst.level - 1));
    }
    
    public void increment() {
        setInst(new EnchantmentInstance(inst.enchantment, inst.level + 1));
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
            if (inst.enchantment.value().getMaxLevel() == 1){
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
                StringBuilder newName = new StringBuilder();
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

        if(this.isHovered) guiGraphics.renderTooltip(Minecraft.getInstance().font, Enchantment.getFullname(inst.enchantment, inst.level), mouseX, mouseY);
    }
}
