package com.qurenie.relics_thirteenflames.client.screen.gloves;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.client.screen.DefaultMenuScreen;
import com.qurenie.relics_thirteenflames.content.container.MontuCompositeContainer;
import com.qurenie.relics_thirteenflames.net.MontuMenuTypePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.util.Cast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.net.Network;

public class MontuCompositeScreen extends DefaultMenuScreen<MontuCompositeContainer> {
    
    public static final ResourceLocation GLOVES_GUI = ThirteenFlames.rl("textures/gui/gloves/montu_gloves.png");
    private static final int GLOVES_HEIGHT = 184;
    private static final int BUTTON_HEIGHT = 16;
    private static final int BUTTON_WIDTH = 10;
    private static final int BUTTON_OFFSET = 50;
    private final Inventory inventory;
    public MontuCompositeContainer.MontuMenuType type;
    public Screen prevActiveScreen;
    public Screen activeScreen;
    public int animationOffset = 0;
    
    public MontuCompositeScreen(MontuCompositeContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.inventory = pPlayerInventory;
        this.type = MontuCompositeContainer.MontuMenuType.ANVIl;
        menu.activateType(type);
        
        this.prevActiveScreen = activeScreen;
        this.activeScreen = type.createScreen(Cast.cast(menu.getActiveMenu()), inventory, type.getContainerName());
    }
    
    public int getSwapSpeed(int absoluteX) {
        int base = 6;
        if (absoluteX <= 0 || absoluteX >= getScreenWidth()) return base;
        // H  — желаемый максимум
        int H = 40;
        double t = absoluteX / (double) getScreenWidth();      // [0..1]
        return (int) (4 * H * t * (1 - t)) + base;   // всё та же парабола, но высота — H
    }
    
    @Override
    protected void init() {
        super.init();
        
        Window window = Minecraft.getInstance().getWindow();
        int w = window.getGuiScaledWidth();
        int h = window.getGuiScaledHeight();
        
        int dy = h / 2 - BUTTON_HEIGHT / 2;
        
        if (menu.getLevel() > 1) {
            this.addRenderableWidget(new MontuButton(BUTTON_OFFSET, dy, BUTTON_WIDTH, BUTTON_HEIGHT, MontuButton.Direction.LEFT,
                    b -> {
                        if (animationOffset == 0)
                            Network.sendToServer(new MontuMenuTypePacket(this.type.previous(menu.getLevel()), MontuButton.Direction.LEFT));
                    }) {
                @Override
                protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                    if (animationOffset == 0)
                        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                }
            });
            this.addRenderableWidget(new MontuButton(w - BUTTON_WIDTH - BUTTON_OFFSET, dy, BUTTON_WIDTH, BUTTON_HEIGHT, MontuButton.Direction.RIGHT,
                    b -> {
                        if (animationOffset == 0)
                            Network.sendToServer(new MontuMenuTypePacket(this.type.next(menu.getLevel()), MontuButton.Direction.RIGHT));
                    }) {
                @Override
                protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                    if (animationOffset == 0)
                        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                }
            });
        }
        
        activeScreen.init(Minecraft.getInstance(), width, height);
        minecraft.player.containerMenu = menu;
        animationOffset = 0;
    }
    
    @Override
    protected void containerTick() {
        super.containerTick();
        if (animationOffset != 0)
            animationOffset -= (int) (Math.signum(animationOffset) * Math.min(Math.abs(animationOffset), getSwapSpeed(Math.abs(animationOffset))));
    }
    
    @Override
    protected void renderSlotHighlight(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick) {
    }
    
    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }
    
    @Override
    protected void renderSlot(@NotNull GuiGraphics guiGraphics, @NotNull Slot slot) {
    }
    
    @Override
    protected void slotClicked(@NotNull Slot slot, int slotId, int mouseButton, @NotNull ClickType type) {
    }
    
    @Override
    protected void renderSlotContents(@NotNull GuiGraphics guiGraphics, @NotNull ItemStack itemstack, @NotNull Slot slot, @Nullable String countString) {
    }
    
    public void changeType(MontuCompositeContainer.MontuMenuType type, MontuButton.Direction direction) {
        this.type = type;
        menu.activateType(type);
        
        this.prevActiveScreen = activeScreen;
        this.activeScreen = type.createScreen(Cast.cast(menu.getActiveMenu()), inventory, type.getContainerName());
        animationOffset = direction == MontuButton.Direction.RIGHT ? getScreenWidth() : -getScreenWidth();
        activeScreen.init(Minecraft.getInstance(), width, height);
        minecraft.player.containerMenu = menu;
    }
    
    @Override
    public int getScreenWidth() {
        return 356;
    }
    
    @Override
    public int getScreenHeight() {
        return 253;
    }
    
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        try {
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            
            if (activeScreen == null)
                return;
            
            int width = activeScreen.width;
            int height = activeScreen.height;
            Window window = Minecraft.getInstance().getWindow();
            int w = window.getGuiScaledWidth();
            int h = window.getGuiScaledHeight();
            
            int innerRelX = w / 2 - width / 2;
            int innerRelY = h / 2 - height / 2;
            
            PoseStack pose = guiGraphics.pose();
            if (prevActiveScreen != null && animationOffset != 0) {
                pose.pushPose();
                pose.translate(innerRelX + (Math.signum(animationOffset) * (getScreenWidth() - Math.abs(animationOffset))), innerRelY, 10);
                prevActiveScreen.render(guiGraphics, (int) (mouseX - Math.signum(animationOffset) * (getScreenWidth() - Math.abs(animationOffset))), mouseY, partialTick);
                pose.popPose();
            }
            
            pose.pushPose();
            pose.translate(innerRelX - animationOffset, innerRelY, 10);
            activeScreen.render(guiGraphics, mouseX + animationOffset, mouseY, partialTick);
            pose.popPose();
        } catch (RuntimeException e) {
            ThirteenFlames.LOGGER.error(e.getMessage(), e);
        }
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return activeScreen != null && activeScreen.mouseDragged(mouseX - animationOffset, mouseY, button, dragX, dragY)
                || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
//        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return activeScreen != null && activeScreen.mouseScrolled(mouseX - animationOffset, mouseY, scrollX, scrollY)
                || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
//        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return activeScreen != null && activeScreen.charTyped(codePoint, modifiers)
                || super.charTyped(codePoint, modifiers);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (activeScreen != null) activeScreen.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (activeScreen != null) activeScreen.keyPressed(keyCode, scanCode, modifiers);
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        try {
            boolean result = activeScreen != null && activeScreen.mouseClicked(mouseX - animationOffset, mouseY, button);
            if (hoveredSlot != null) {
                return result;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        } catch (RuntimeException e) {
            ThirteenFlames.LOGGER.error(e.getMessage(), e);
        }
//        return super.mouseClicked(mouseX, mouseY, button);
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean result = activeScreen != null && activeScreen.mouseReleased(mouseX - animationOffset, mouseY, button);
        if (hoveredSlot != null) {
            return result;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        if (activeScreen == null) return;
        PoseStack pose = guiGraphics.pose();
        
        int width = activeScreen.width;
        int height = activeScreen.height;
        Window window = Minecraft.getInstance().getWindow();
        int w = window.getGuiScaledWidth();
        int h = window.getGuiScaledHeight();
        
        int innerRelX = w / 2 - width / 2;
        int innerRelY = h / 2 - height / 2;
        
        if (prevActiveScreen != null && animationOffset != 0) {
            pose.pushPose();
            pose.translate(innerRelX + (animationOffset == 0 ? 0 : getScreenWidth() - animationOffset), innerRelY, 10);
            prevActiveScreen.renderBackground(guiGraphics, mouseX + animationOffset - getScreenWidth(), mouseY, partialTick);
            pose.popPose();
        }
        
        pose.pushPose();
        pose.translate(innerRelX + (animationOffset == 0 ? 0 : -animationOffset), innerRelY, 10);
        activeScreen.renderBackground(guiGraphics, mouseX + animationOffset, mouseY, partialTick);
        pose.popPose();
    }
    
    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(GLOVES_GUI, relX, relY + 40, this.getScreenWidth(), GLOVES_HEIGHT, 0, 0, this.getScreenWidth(), GLOVES_HEIGHT, this.getScreenWidth(), GLOVES_HEIGHT);
    }
    
}
