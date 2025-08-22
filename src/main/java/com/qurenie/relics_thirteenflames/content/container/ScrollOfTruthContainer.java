package com.qurenie.relics_thirteenflames.content.container;

import com.qurenie.relics_thirteenflames.init.MenuRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScrollOfTruthContainer extends AbstractContainerMenu {
    
    public ItemStackHandler fakeHandler;
    public ItemStack scroll;
    
    public ScrollOfTruthContainer(int pContainerId, Inventory playerInv, ItemStack scroll) {
        super(MenuRegistry.SCROLL_OF_TRUTH_MENU.get(), pContainerId);
        this.fakeHandler = new ItemStackHandler(1);
        this.scroll = scroll;
        int y = 67;
        
        this.addSlot(new SlotItemHandler(fakeHandler, 0, 80, -4) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.isEnchantable() && !stack.isEnchanted();
            }
            
            @Override
            public void set(ItemStack stack) {
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
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (pIndex < this.fakeHandler.getSlots()) {
                if (!this.moveItemStackTo(itemstack1, this.fakeHandler.getSlots(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, this.fakeHandler.getSlots(), false)) {
                return ItemStack.EMPTY;
            }
            
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        
        return itemstack;
    }
    
    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return true;
    }
    
    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        ItemStack item = fakeHandler.getStackInSlot(0);
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
