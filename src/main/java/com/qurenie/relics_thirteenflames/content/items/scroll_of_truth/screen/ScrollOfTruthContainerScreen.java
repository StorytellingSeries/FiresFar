package com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.client.screen.DefaultMenuScreen;
import com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.EnchantPacket;
import com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.ScrollColorMode;
import com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.ScrollOfTruth;
import com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.ScrollOfTruthContainer;
import com.qurenie.relics_thirteenflames.util.RenderTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerlib.net.Network;

import java.util.*;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.SCROLL_COLOR_MODE;

public class ScrollOfTruthContainerScreen extends DefaultMenuScreen<ScrollOfTruthContainer> {
    
    
    public static final ResourceLocation SCROLL_GUI = ThirteenFlames.rl("textures/hud/scroll_of_truth/scroll_wip.png");
    
    
    public HashMap<Holder<Enchantment>, EnchantmentInstance> toSelect = new HashMap<>();
    public HashMap<Holder<Enchantment>, EnchantmentInstance> toEnchant = new HashMap<>();
    public List<EnchantmentHolderButton> toSelectButtons = new ArrayList<>();
    public List<EnchantmentHolderButton> toEnchantButtons = new ArrayList<>();
    private EnchantmentHolderButton selectedEnchantButton = null;
    private Button addOrRemoveButton = null;
    private Button enchantButton = null;
    private int selectScroll = 0;
    private int enchantScroll = 0;
    
    private int toSelectButtonsX = 100;
    
    private int toEnchantButtonsX = 100;
    private int enchantSelectButtonsInitY = 100;
    private int enchantButtonsWidth = 40;
    
    private int enchantSelectButtonsYSize = 20;
    
    private int enchantTicker = 0;
    
    public ScrollOfTruthContainerScreen(ScrollOfTruthContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.inventoryLabelY += 33;
    }
    
    
    @Override
    protected void init() {
        super.init();
        enchantButtonsWidth = 60;
        toSelectButtonsX = relX + 40;
        toEnchantButtonsX = relX + 204;
        enchantSelectButtonsInitY = relY + 20;
        int center = relX + this.getScreenWidth() / 2;
        addOrRemoveButton = new ScrollOfTruthButton(center - 30, relY + 60, 60, 20, Component.literal(""), (btn) -> {
            if (this.selectedEnchantButton != null) {
                this.addOrRemoveButton.active = false;
                this.addOrRemoveButton.setMessage(Component.literal(""));
                EnchantmentInstance inst = this.selectedEnchantButton.getInst();
                if (toSelectButtons.contains(this.selectedEnchantButton)) {
                    this.toSelect.remove(inst.enchantment);
                    this.toEnchant.put(inst.enchantment, inst);
                } else {
                    this.toSelect.put(inst.enchantment, inst);
                    this.toEnchant.remove(inst.enchantment);
                }
                this.selectedEnchantButton = null;
                this.reinitEnchantmentButtons();
            }
        });
        addOrRemoveButton.active = false;
        this.addRenderableWidget(addOrRemoveButton);
        this.onItemChange(this.menu.fakeHandler.getStackInSlot(0));
        
        Button lvlupbtn = new ScrollOfTruthButton(center - 44, relY + 60, 14, 20, Component.literal("▲"), (btn) -> {
            if (this.selectedEnchantButton != null) {
                EnchantmentInstance instance = this.selectedEnchantButton.getInst();
                Enchantment e = instance.enchantment.value();
                int maxAllowedLevel = 0;
                if (menu.scroll.getItem() instanceof ScrollOfTruth sot) {
                    maxAllowedLevel = (int) (e.getMaxLevel() * sot.getAbilityLevel(menu.scroll, "enchant") / 10.0) + 1;
                }
                if (instance.level < e.getMaxLevel() && instance.level < maxAllowedLevel)
//TODO: MaxLevel check
                    selectedEnchantButton.increment();
            }
        });
        
        Button lvldownbtn = new ScrollOfTruthButton(center + 30, relY + 60, 14, 20, Component.literal("▼"), (btn) -> {
            if (this.selectedEnchantButton != null) {
                EnchantmentInstance instance = this.selectedEnchantButton.getInst();
                if (instance.level > 1)
                    selectedEnchantButton.decrement();
            }
        });
        Button enchantButton = new ScrollOfTruthButton(center - 42, relY + 100, 82, 20, Component.translatable("tooltip.relics_thirteenflames.scroll_of_truth.gui.enchant"), (btn) -> {
            if (EnchantPacket.mayEnchant(Minecraft.getInstance().player, menu.scroll, menu.fakeHandler.getStackInSlot(0), toEnchant.values())) {
                enchantTicker = 19;
            }
            Network.sendToServer(new EnchantPacket(this.toEnchant.values()));
        });
        this.enchantButton = enchantButton;
        this.addRenderableWidget(lvlupbtn);
        this.addRenderableWidget(lvldownbtn);
        this.addRenderableWidget(enchantButton);
    }
    
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float p_97788_, int p_97789_, int p_97790_) {
        ResourceLocation loc = menu.scroll.getOrDefault(SCROLL_COLOR_MODE, ScrollColorMode.GREEN).gui;
        RenderSystem.setShaderTexture(0, loc);
        
        guiGraphics.blit(loc, relX, relY, this.getScreenWidth(), this.getScreenHeight(), 0, 0, this.getScreenWidth(), this.getScreenHeight(), this.getScreenWidth(), this.getScreenHeight());
    }
    
    public void onItemChange(ItemStack itemInSlot) {
        this.toEnchant.clear();
        this.toSelect.clear();
        if (!itemInSlot.isEmpty() && !itemInSlot.isEnchanted()) {
            var allEnchantments = Minecraft.getInstance().level
                    .registryAccess().registryOrThrow(Registries.ENCHANTMENT).asHolderIdMap();
            
            for (Holder<Enchantment> e : allEnchantments) {
                if (itemInSlot.supportsEnchantment(e)
                        && menu.scroll.getItem() instanceof ScrollOfTruth sot
                        && 0.5 / e.value().definition().weight() <= sot.getAbilityLevel(menu.scroll, "enchant") * 0.375) {
                    
                    this.toSelect.put(e, new EnchantmentInstance(e, 1));
                    
                }
            }
        }
        this.addOrRemoveButton.active = false;
        this.addOrRemoveButton.setMessage(Component.literal(""));
        this.reinitEnchantmentButtons();
    }
    
    
    public void reinitEnchantmentButtons() {
        this.enchantScroll = 0;
        this.selectScroll = 0;
        for (Button b : toSelectButtons) {
            this.removeWidget(b);
        }
        for (Button b : toEnchantButtons) {
            this.removeWidget(b);
        }
        this.toSelectButtons.clear();
        this.toEnchantButtons.clear();
        
        
        int yOffs = 0;
        for (var entry : toSelect.entrySet()) {
            boolean nonCompatibleFlag = false;
            for (Holder<Enchantment> ench : toEnchant.keySet()) {
                if (ench.value().exclusiveSet().contains(entry.getKey())) nonCompatibleFlag = true;
            }
            if (nonCompatibleFlag) continue;
            EnchantmentHolderButton button = createEnchantmentButton(toSelectButtonsX, yOffs, Component.translatable("tooltip.relics_thirteenflames.scroll_of_truth.gui.addEnch"), entry);
            this.toSelectButtons.add(button);
            this.addWidget(button);
            yOffs += 20;
        }
        yOffs = 0;
        for (var entry : toEnchant.entrySet()) {
            EnchantmentHolderButton button = createEnchantmentButton(toEnchantButtonsX, yOffs, Component.translatable("tooltip.relics_thirteenflames.scroll_of_truth.gui.removeEnch"), entry);
            this.toEnchantButtons.add(button);
            this.addWidget(button);
            yOffs += 20;
        }
    }
    
    @NotNull
    private EnchantmentHolderButton createEnchantmentButton(int toEnchantButtonsX, int yOffs, Component text, Map.Entry<Holder<Enchantment>, EnchantmentInstance> entry) {
        EnchantmentHolderButton button = new EnchantmentHolderButton(toEnchantButtonsX, enchantSelectButtonsInitY + yOffs, enchantButtonsWidth, enchantSelectButtonsYSize, Component.empty(), (btn) -> {
            for (EnchantmentHolderButton h : toSelectButtons) {
                h.active = true;
            }
            for (EnchantmentHolderButton h : toEnchantButtons) {
                h.active = true;
            }
            btn.active = false;
            this.selectedEnchantButton = (EnchantmentHolderButton) btn;
            this.addOrRemoveButton.active = true;
            this.addOrRemoveButton.setMessage(text);
        });
        button.setInst(entry.getValue());
        return button;
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mx, int my, float pTicks) {
        //guiGraphics.fill(0,0,10000,100000,0xaa000000);
//        this.renderBackground(guiGraphics, mx, my, pTicks);
        
        this.deactivateEnchantButtonsOutOfReach(this.toEnchantButtons);
        this.deactivateEnchantButtonsOutOfReach(this.toSelectButtons);
        
        super.render(guiGraphics, mx, my, pTicks);
        if (!this.menu.fakeHandler.getStackInSlot(0).isEmpty()) {
            int cost = ScrollOfTruth.getFullEnchantmentCost(menu.scroll, this.toEnchant.values());
            RenderTools.renderCenteredScaledText(guiGraphics, Component.translatable("tooltip.relics_thirteenflames.scroll_of_truth.gui.levelCost").getString() + " " + cost, relX + this.getScreenWidth() / 2, relY + 87, 0.95f);
        }
        this.renderTooltip(guiGraphics, mx, my);
        
        
        //Scissors.begin(0,relY + 20,2000,100);
        for (Button b : toSelectButtons) {
            b.render(guiGraphics, mx, my, pTicks);
        }
        for (Button b : toEnchantButtons) {
            b.render(guiGraphics, mx, my, pTicks);
        }
        //Scissors.end();
        
        
        if (enchantTicker > 1 && enchantTicker <= 19) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 500);
            ResourceLocation rl = ThirteenFlames.rl("textures/hud/scroll_of_truth/enchant_anim/scroll_gui_enchant" + (21 - enchantTicker) + ".png");
            RenderSystem.setShaderTexture(0, rl);
            guiGraphics.blit(rl, relX, relY, this.getScreenWidth(), this.getScreenHeight(), 0, 0, this.getScreenWidth(), this.getScreenHeight(), this.getScreenWidth(), this.getScreenHeight());
            guiGraphics.pose().popPose();
        }
    }
    
    @Override
    protected void containerTick() {
        
        super.containerTick();
        if (enchantTicker > 0) {
            enchantTicker--;
        }
        if (enchantButton != null) {
            if (menu.fakeHandler.getStackInSlot(0).isEmpty() || menu.fakeHandler.getStackInSlot(0).isEnchanted() || this.toEnchant.isEmpty()) {
                this.enchantButton.active = false;
            } else {
                this.enchantButton.active = true;
            }
            
        }
    }
    
    private void deactivateEnchantButtonsOutOfReach(List<EnchantmentHolderButton> buttons) {
        for (Button b : buttons) {
            int y = b.getY() - this.enchantSelectButtonsInitY;
            if (y > 99 || y < 0) {
                b.active = false;
                b.visible = false;
            } else {
                b.visible = true;
                if (b != this.selectedEnchantButton) {
                    b.active = true;
                }
            }
        }
    }
    
    @Override
    protected void slotClicked(Slot slot, int p_97779_, int p_97780_, @NotNull ClickType p_97781_) {
        // SLOT CAN BE NULL... somehow
        if (slot == null || slot.getItem().equals(menu.scroll)) return;
        super.slotClicked(slot, p_97779_, p_97780_, p_97781_);
    }
    
    @Override
    public boolean mouseScrolled(double mx, double my, double deltaX, double deltaY) {
        if (RenderTools.isMouseInBorders((int) mx, (int) my, toSelectButtonsX, enchantSelectButtonsInitY, toSelectButtonsX + enchantButtonsWidth,
                enchantSelectButtonsInitY + 120)) {
            this.moveSelectButtons((int) Math.round(deltaY * enchantSelectButtonsYSize));
        } else if (RenderTools.isMouseInBorders((int) mx, (int) my, toEnchantButtonsX, enchantSelectButtonsInitY, toEnchantButtonsX + enchantButtonsWidth,
                enchantSelectButtonsInitY + 120)) {
            this.moveEnchantButtons((int) Math.round(deltaY * enchantSelectButtonsYSize));
        }
        return super.mouseScrolled(mx, my, deltaX, deltaY);
    }
    
    private void moveSelectButtons(int amount) {
        if (amount < 0) {
            int maxSelectScroll = enchantSelectButtonsYSize * (Math.max(toSelectButtons.size() - 5, 0));
            if (this.selectScroll > -maxSelectScroll) this.selectScroll += amount;
            else amount = 0;
        } else {
            int moveAmount = Math.max(this.selectScroll, -amount);
            this.selectScroll -= moveAmount;
            amount = -moveAmount;
        }
        for (Button b : toSelectButtons) {
            b.setY(b.getY() + amount);
        }
    }
    
    private void moveEnchantButtons(int amount) {
        if (amount < 0) {
            int maxEnchantScroll = enchantSelectButtonsYSize * (Math.max(toEnchantButtons.size() - 5, 0));
            if (this.enchantScroll > -maxEnchantScroll) this.enchantScroll += amount;
            else amount = 0;
        } else {
            int moveAmount = Math.max(this.enchantScroll, -amount);
            this.enchantScroll -= moveAmount;
            amount = -moveAmount;
        }
        for (Button b : toEnchantButtons) {
            b.setY(b.getY() + amount);
        }
    }
    
    @Override
    public int getScreenWidth() {
        return 303;
    }
    
    @Override
    public int getScreenHeight() {
        return 229;
    }
    
}
