package com.qurenie.relics_thirteenflames.client.screen.scroll;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.client.screen.DefaultMenuScreen;
import com.qurenie.relics_thirteenflames.content.container.ScrollOfTruthContainer;
import com.qurenie.relics_thirteenflames.content.items.ScrollOfTruthItem;
import com.qurenie.relics_thirteenflames.content.items.misc.ScrollColorMode;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.net.EnchantPacket;
import com.qurenie.relics_thirteenflames.util.RenderTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerlib.client.utils.Scissors;
import org.zeith.hammerlib.net.Network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.SCROLL_COLOR_MODE;

public class ScrollOfTruthContainerScreen extends DefaultMenuScreen<ScrollOfTruthContainer> implements ContainerListener {
    
    private final int enchantSelectButtonsYSize = 13;
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
    private int enchantTicker = 0;
    
    public ScrollOfTruthContainerScreen(ScrollOfTruthContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.inventoryLabelY += 33;
    }
    
    @Override
    protected void init() {
        super.init();
        Window window = minecraft.getWindow();
        
        enchantButtonsWidth = 60;
        toSelectButtonsX = relX + 46;
        toEnchantButtonsX = window.getGuiScaledWidth() - relX - 46 - enchantButtonsWidth;
        enchantSelectButtonsInitY = relY + 30;
        int center = relX + this.getScreenWidth() / 2;
        addOrRemoveButton = new ScrollOfTruthButton(center - 28, relY + 60, 56, 13, Component.literal(""), (btn) -> {
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
//        this.onItemChange(this.menu.fakeHandler.getStackInSlot(0));
        
        Button lvlupbtn = new ScrollOfTruthButton(center - 42, relY + 60, 14, 13, Component.literal("▲"), (btn) -> {
            if (this.selectedEnchantButton != null) {
                EnchantmentInstance instance = this.selectedEnchantButton.getInst();
                Enchantment e = instance.enchantment.value();
                int maxAllowedLevel = 0;
                if (menu.scroll.getItem() instanceof ScrollOfTruthItem sot) {
                    maxAllowedLevel = (int) (sot.getStatValue(Minecraft.getInstance().player, menu.scroll, "enchant", "maxLevel"));
                }

                int maxLvl = Math.min(e.getMaxLevel(), maxAllowedLevel)
                        + (ItemsRegistry.SCROLL_OF_TRUTH.hasRangModifier(minecraft.player, menu.scroll, "enchant", "maxup") ? 1 : 0);
                if (instance.level < maxLvl) {
                    selectedEnchantButton.increment();
                    if (toEnchantButtons.contains(selectedEnchantButton))
                        toEnchant.put(selectedEnchantButton.getInst().enchantment, selectedEnchantButton.getInst());
                }
            }
        }) {
            @Override
            public void drawButtonText(GuiGraphics guiGraphics, int mx, int my, float pticks) {
                int shadowColor = RenderTools.DEFAULT_SHADOW_COLOR;
                int textColor = RenderTools.DEFAULT_TEXT_COLOR;
                if (!this.active) {
                    shadowColor = RenderTools.DEFAULT_TEXT_COLOR;
                    textColor = RenderTools.DEFAULT_SHADOW_COLOR;
                }
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.5f, 0f, 0);
                RenderTools.renderCenteredScaledText(guiGraphics, this.getMessage().getString(), this.getX() + this.width / 2, this.getY() + (this.height - 6) / 2, 1f,
                        shadowColor, textColor);
                guiGraphics.pose().popPose();
            }
        };
        
        Button lvldownbtn = new ScrollOfTruthButton(center + 28, relY + 60, 14, 13, Component.literal("▼"), (btn) -> {
            if (this.selectedEnchantButton != null) {
                EnchantmentInstance instance = this.selectedEnchantButton.getInst();
                if (instance.level > 1) {
                    selectedEnchantButton.decrement();
                    if (toEnchantButtons.contains(selectedEnchantButton))
                        toEnchant.put(selectedEnchantButton.getInst().enchantment, selectedEnchantButton.getInst());
                }
            }
        }) {
            @Override
            public void drawButtonText(GuiGraphics guiGraphics, int mx, int my, float pticks) {
                int shadowColor = RenderTools.DEFAULT_SHADOW_COLOR;
                int textColor = RenderTools.DEFAULT_TEXT_COLOR;
                if (!this.active) {
                    shadowColor = RenderTools.DEFAULT_TEXT_COLOR;
                    textColor = RenderTools.DEFAULT_SHADOW_COLOR;
                }
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.5f, 0f, 0);
                RenderTools.renderCenteredScaledText(guiGraphics, this.getMessage().getString(), this.getX() + this.width / 2, this.getY() + (this.height - 6) / 2, 1f,
                        shadowColor, textColor);
                guiGraphics.pose().popPose();
            }
        };
        
        Button enchantButton = new ScrollOfTruthButton(center - 42, relY + 100, 84, 13, Component.translatable("tooltip.relics_thirteenflames.scroll_of_truth.gui.enchant"), (btn) -> {
            if (EnchantPacket.mayEnchant(Minecraft.getInstance().player, menu.scroll, menu.scrollContainer.getItem(0), toEnchant.values())) {
                enchantTicker = 19;
            }
            Network.sendToServer(new EnchantPacket(this.toEnchant.values()));
        });
        this.enchantButton = enchantButton;
        this.addRenderableWidget(lvlupbtn);
        this.addRenderableWidget(lvldownbtn);
        this.addRenderableWidget(enchantButton);
        
        this.menu.addSlotListener(this);
    }
    
    @Override
    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this);
    }
    
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float p_97788_, int p_97789_, int p_97790_) {
        ResourceLocation loc = menu.scroll.getOrDefault(SCROLL_COLOR_MODE, ScrollColorMode.GREEN).gui;
        RenderSystem.setShaderTexture(0, loc);
        
        guiGraphics.blit(loc, relX, relY, this.getScreenWidth(), this.getScreenHeight(), 0, 0, this.getScreenWidth(), this.getScreenHeight(), this.getScreenWidth(), this.getScreenHeight());
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
            yOffs += 14;
        }
        yOffs = 0;
        for (var entry : toEnchant.entrySet()) {
            EnchantmentHolderButton button = createEnchantmentButton(toEnchantButtonsX, yOffs, Component.translatable("tooltip.relics_thirteenflames.scroll_of_truth.gui.removeEnch"), entry);
            this.toEnchantButtons.add(button);
            this.addWidget(button);
            yOffs += 14;
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
    public void render(@NotNull GuiGraphics guiGraphics, int mx, int my, float pTicks) {
        //guiGraphics.fill(0,0,10000,100000,0xaa000000);
        
        this.deactivateEnchantButtonsOutOfReach(this.toEnchantButtons);
        this.deactivateEnchantButtonsOutOfReach(this.toSelectButtons);
        
        super.render(guiGraphics, mx, my, pTicks);
        
        Scissors.begin(0, relY, 2000, 14 * 6 + enchantSelectButtonsInitY - relY);
        for (Button b : toSelectButtons) {
            b.render(guiGraphics, mx, my, pTicks);
        }
        for (Button b : toEnchantButtons) {
            b.render(guiGraphics, mx, my, pTicks);
        }
        Scissors.end();

        if (!this.menu.scrollContainer.getItem(0).isEmpty()) {
            Player player = Minecraft.getInstance().player;
            int cost = ScrollOfTruthItem.getFullEnchantmentCost(player, menu.scroll, this.toEnchant.values());
            int level = player == null ? 0 : player.experienceLevel;

            String text = Component.translatable("tooltip.relics_thirteenflames.scroll_of_truth.gui.levelCost").getString() + " " + cost;

            Font font = Minecraft.getInstance().font;

            // Разбиваем текст на строки с максимальной шириной 77 пикселей
            List<FormattedCharSequence> lines = font.split(Component.literal(text), 87);

            int centerX = relX + this.getScreenWidth() / 2;
            int startY = relY + 87 - (lines.size() - 1) * 5;

            int color = level >= cost ? RenderTools.DEFAULT_TEXT_COLOR : 0xdb1e10;

            for (int i = 0; i < lines.size(); i++) {
                var line = lines.get(i);

                RenderTools.renderCenteredScaledText(
                        guiGraphics,
                        line,
                        centerX,
                        startY + i * 9,
                        0.95f,
                        RenderTools.DEFAULT_SHADOW_COLOR,
                        color
                );
            }
        }

        this.renderTooltip(guiGraphics, mx, my);
        
        
        if (enchantTicker > 1 && enchantTicker <= 19) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 500);
            ResourceLocation rl = ThirteenFlames.rl("textures/gui/scroll_of_truth/enchant_anim/scroll_gui_enchant" + (21 - enchantTicker) + ".png");
            RenderSystem.setShaderTexture(0, rl);
            guiGraphics.blit(rl, relX, relY + 4, this.getScreenWidth(), this.getScreenHeight(), 0, 0, this.getScreenWidth(), this.getScreenHeight(), this.getScreenWidth(), this.getScreenHeight());
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
            this.enchantButton.active = !menu.scrollContainer.getItem(0).isEmpty() && !menu.scrollContainer.getItem(0).isEnchanted() && !this.toEnchant.isEmpty();
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
    protected void slotClicked(@NotNull Slot slot, int p_97779_, int p_97780_, @NotNull ClickType p_97781_) {
        if (slot != null && ItemStack.matches(slot.getItem(), menu.scroll)) return;
        super.slotClicked(slot, p_97779_, p_97780_, p_97781_);
    }
    
    @Override
    public boolean mouseScrolled(double mx, double my, double deltaX, double deltaY) {
        if (RenderTools.isMouseInBorders((int) mx, (int) my, toSelectButtonsX, enchantSelectButtonsInitY, toSelectButtonsX + enchantButtonsWidth,
                enchantSelectButtonsInitY + 120)) {
            this.moveSelectButtons((int) Math.round(deltaY * (enchantSelectButtonsYSize + 1)));
        } else if (RenderTools.isMouseInBorders((int) mx, (int) my, toEnchantButtonsX, enchantSelectButtonsInitY, toEnchantButtonsX + enchantButtonsWidth,
                enchantSelectButtonsInitY + 120)) {
            this.moveEnchantButtons((int) Math.round(deltaY * (enchantSelectButtonsYSize + 1)));
        }
        return super.mouseScrolled(mx, my, deltaX, deltaY);
    }
    
    private void moveSelectButtons(int amount) {
        if (amount < 0) {
            int maxSelectScroll = (enchantSelectButtonsYSize + 1) * (Math.max(toSelectButtons.size() - 5, 0));
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
            int maxEnchantScroll = ((enchantSelectButtonsYSize + 1) * (Math.max(toEnchantButtons.size() - 5, 0)));
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
        return 300;
    }
    
    @Override
    public int getScreenHeight() {
        return 234;
    }
    
    @Override
    public void slotChanged(@NotNull AbstractContainerMenu containerToSend, int dataSlotIndex, @NotNull ItemStack stack) {
        if (dataSlotIndex != 0)
            return;
        
        this.toEnchant.clear();
        this.toSelect.clear();
        if (!stack.isEmpty() && !stack.isEnchanted()) {
            var allEnchantments = Minecraft.getInstance().level
                    .registryAccess().registryOrThrow(Registries.ENCHANTMENT).asHolderIdMap();
            
            int maxWeight = 0;
            
            if (menu.scroll.getItem() instanceof ScrollOfTruthItem sot)
                maxWeight = (int) sot.getStatValue(Minecraft.getInstance().player, menu.scroll, "enchant", "maxLevel");
            
            for (Holder<Enchantment> e : allEnchantments) {
                if (stack.supportsEnchantment(e)
                        && 1f / e.value().definition().weight() * 8f <= maxWeight) {
                    
                    this.toSelect.put(e, new EnchantmentInstance(e, 1));
                }
            }
        }
        this.addOrRemoveButton.active = false;
        this.addOrRemoveButton.setMessage(Component.literal(""));
        this.reinitEnchantmentButtons();
    }
    
    @Override
    public void dataChanged(@NotNull AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
    }
    
}
