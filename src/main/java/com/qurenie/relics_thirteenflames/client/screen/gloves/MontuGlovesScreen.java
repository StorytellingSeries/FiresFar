package com.qurenie.relics_thirteenflames.client.screen.gloves;

import com.mojang.blaze3d.systems.RenderSystem;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.client.screen.DefaultMenuScreen;
import com.qurenie.relics_thirteenflames.content.container.MontuGlovesContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.net.PacketContext;

public class MontuGlovesScreen extends DefaultMenuScreen<MontuGlovesContainer> {
    
    public static final ResourceLocation GLOVES_GUI = ThirteenFlames.rl("textures/gui/gloves/montu_gui.png");
    
    public static final int ANIMATION_LENGTH = 25;
    int smithTicker;
    
    public MontuGlovesScreen(MontuGlovesContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }
    
    public void onCraftStarted() {
        smithTicker = ANIMATION_LENGTH;
        
    }
    
    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
    }
    
    @Override
    protected void containerTick() {
        super.containerTick();
        
        if (smithTicker > 0) {
            smithTicker--;
            if (smithTicker == 5) {
                Network.sendToServer(new IPacket() {
                    @Override
                    public void serverExecute(PacketContext ctx) {
                        ServerPlayer serverPlayer = ctx.getSender();
                        
                        if (serverPlayer.containerMenu instanceof MontuGlovesContainer container)
                            container.omSmith();
                    }
                });
            }
        }
    }
    
    @Override
    protected void slotClicked(@NotNull Slot slot, int slotId, int mouseButton, @NotNull ClickType type) {
        // SLOT ITEM CAN BE NULL... somehow
        if (slot == null || ItemStack.matches(slot.getItem(), menu.getGloves())) return;
        super.slotClicked(slot, slotId, mouseButton, type);
    }
    
    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
//            RenderSystem.setShaderTexture(0, smithTicker == 0 ? SCROLL_GUI : ThirteenFlames.rl(String.format("textures/gui/gloves/small_hammering%d.png", 26 - smithTicker)));
        guiGraphics.blit(GLOVES_GUI, relX, relY, this.getScreenWidth(), this.getScreenHeight(), 0, 0, this.getScreenWidth(), this.getScreenHeight(), this.getScreenWidth(), this.getScreenHeight());
        if (smithTicker > 0) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 1000);
            RenderSystem.enableBlend();
            guiGraphics.blit(ThirteenFlames.rl(String.format("textures/gui/gloves/small_hammering_only%d.png", 26 - smithTicker)), relX, relY, this.getScreenWidth(), this.getScreenHeight(), 0, 0, this.getScreenWidth(), this.getScreenHeight(), this.getScreenWidth(), this.getScreenHeight());
//            ThirteenFlames.rl(String.format("textures/gui/gloves/small_hammering_only%d.png", 26 - smithTicker));
            RenderSystem.defaultBlendFunc();
            guiGraphics.pose().popPose();
        }
    }
    
    @Override
    public int getScreenWidth() {
        return 356;
    }
    
    @Override
    public int getScreenHeight() {
        return 253;
    }
    
    
}
