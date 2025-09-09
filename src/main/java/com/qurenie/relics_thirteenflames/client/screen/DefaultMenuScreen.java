package com.qurenie.relics_thirteenflames.client.screen;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class DefaultMenuScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    
    protected int relX;
    protected int relY;
    
    public DefaultMenuScreen(T pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }
    
    @Override
    protected void init() {
        Window window = Minecraft.getInstance().getWindow();
        int w = window.getGuiScaledWidth();
        int h = window.getGuiScaledHeight();
        
        this.relX = w / 2 - this.getScreenWidth() / 2;
        this.relY = h / 2 - this.getScreenHeight() / 2;
        
        super.init();
    }
    
    public abstract int getScreenWidth();
    
    public abstract int getScreenHeight();
    
}
