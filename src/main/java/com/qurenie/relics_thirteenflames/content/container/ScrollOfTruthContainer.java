package com.qurenie.relics_thirteenflames.content.container;

import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.init.MenuRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScrollOfTruthContainer extends AbstractContainerMenu {
    
    public final Container scrollContainer;
    public ItemStack scroll;
    
    public ScrollOfTruthContainer(int pContainerId, Inventory playerInv, ItemStack scroll) {
        super(MenuRegistry.SCROLL_OF_TRUTH_MENU.get(), pContainerId);
        this.scrollContainer = new SimpleContainer(1);
        
        this.scroll = scroll;
        int y = 67;
        
        this.addSlot(new Slot(scrollContainer, 0, 80, -4) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.isEnchantable() && !stack.isEnchanted();
            }
            
            @Override
            public void set(@NotNull ItemStack stack) {
                super.set(stack);
                broadcastChanges();
            }
        });
        
        for (int l = 0; l < 3; ++l) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInv, k + l * 9 + 9, 8 + k * 18, l * 18 + 51 + y));
            }
        }
        
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInv, i1, 8 + i1 * 18, 109 + y));
        }
    }
    
    public ScrollOfTruthContainer(int cid, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(cid, playerInv, ItemStack.STREAM_CODEC.decode(buf));
    }
    
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            moved = slotStack.copy();
            
            int containerSlots = 1; // наш memoryContainer
            int playerInvEnd = containerSlots + 27; // main inventory
            int hotbarEnd = playerInvEnd + 9;
            
            if (index < containerSlots) {
                // из memoryContainer -> инвентарь игрока
                if (!this.moveItemStackTo(slotStack, containerSlots, hotbarEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // из инвентаря игрока -> memoryContainer (проверка mayPlace будет учитываться)
                if (!this.moveItemStackTo(slotStack, 0, containerSlots, false)) {
                    // стандартная логика: переключение между main и hotbar
                    if (index < playerInvEnd) {
                        if (!this.moveItemStackTo(slotStack, playerInvEnd, hotbarEnd, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (index < hotbarEnd) {
                        if (!this.moveItemStackTo(slotStack, containerSlots, playerInvEnd, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        return ItemStack.EMPTY;
                    }
                }
            }
            
            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return moved;
    }
    
    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return true;
    }
    
    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        ItemStack item = scrollContainer.getItem(0);
        if (!item.isEmpty()) {
            ItemEntity entity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), item.copy());
            player.level().addFreshEntity(entity);
        }
    }
    
    public static class Provider implements MenuProvider {
        
        public ItemStack scroll;
        
        public Provider(ItemStack scroll) {
            if (scroll.getItem() != ItemsRegistry.SCROLL_OF_TRUTH) {
                throw new IllegalStateException("Are you dumb? You should pass a scroll item!!!");
            }
            this.scroll = scroll;
        }
        
        @Override
        public @NotNull Component getDisplayName() {
            return Component.empty();
        }
        
        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
            return new ScrollOfTruthContainer(pContainerId, pPlayerInventory, this.scroll);
        }
        
    }
    
}
