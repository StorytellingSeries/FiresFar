package com.qurenie.relics_thirteenflames.content.container;

import com.qurenie.relics_thirteenflames.client.screen.gloves.MontuCompositeScreen;
import com.qurenie.relics_thirteenflames.client.screen.gloves.MontuGlovesScreen;
import com.qurenie.relics_thirteenflames.content.recipes.MontuRecipeInput;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.init.MenuRegistry;
import com.qurenie.relics_thirteenflames.init.RecipeTypesRegistry;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.net.PacketContext;

public class MontuGlovesContainer extends AbstractContainerMenu {
    
    private final Level level;
    private final Player player;
    @Getter
    private MontuItemHandler handler;
    @Getter
    private final ItemStack gloves;
    
    public MontuGlovesContainer(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInv, ItemStack.STREAM_CODEC.decode(buf));
    }
    
    public MontuGlovesContainer(int containerId, Inventory playerInv, ItemStack gloves) {
        super(MenuRegistry.MONTU_SMITH_MENU.get(), containerId);
        this.level = playerInv.player.level();
        this.player = playerInv.player;
        this.gloves = gloves;
        this.handler = new MontuItemHandler(3);
        
        this.addSlot(new SlotItemHandler(handler, 0, 44, 24));
        this.addSlot(new SlotItemHandler(handler, 1, 80, 24));
        this.addSlot(new SlotItemHandler(handler, 2, 116, 24));
        
        int y = 77;
        
        for (int l = 0; l < 3; ++l) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInv, k + l * 9 + 9, 8 + k * 18, l * 18 + 51 + y));
            }
        }
        
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInv, i1, 8 + i1 * 18, 109 + y));
        }
    }
    
    private static MontuRecipeInput createRecipeInput(ItemStackHandler container) {
        return new MontuRecipeInput(container.getStackInSlot(1), container.getStackInSlot(2), container.getStackInSlot(0));
    }
    
    @Override
    public ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            ItemStack stack = itemstack1.copyWithCount(1);
            if (pIndex < this.handler.getSlots()) {
                if (!this.moveItemStackTo(stack, this.handler.getSlots(), this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                } else
                    itemstack1.shrink(1);
            } else if (!this.moveItemStackTo(stack, 0, this.handler.getSlots(), false)) {
                return ItemStack.EMPTY;
            } else
                itemstack1.shrink(1);
            
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        
        return itemstack;
    }
    
    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
    
    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        for (int i = 0; i < 3; i++) {
            ItemStack item = handler.getStackInSlot(i);
            if (!item.isEmpty()) {
                ItemEntity entity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), item.copy());
                player.level().addFreshEntity(entity);
            }
        }
    }
    
    public void omSmith() {
        var recipeHolder = level.getRecipeManager().getRecipeFor(RecipeTypesRegistry.MONTU_SMITH_TYPE.get(), createRecipeInput(handler), level);
        if (recipeHolder.isPresent()) {
            handler.setStackInSlot(0, ItemStack.EMPTY, true);
            handler.setStackInSlot(1, recipeHolder.get().value().result(), true);
            handler.setStackInSlot(2, ItemStack.EMPTY, true);
            
            player.giveExperiencePoints(recipeHolder.get().value().experience());
        }
    }
    
    protected class MontuItemHandler extends ItemStackHandler {
        
        public MontuItemHandler(int size) {
            super(size);
        }
        
        @Override
        protected int getStackLimit(int slot, @NotNull ItemStack stack) {
            return 1;
        }
        
        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            setStackInSlot(slot, stack, false);
        }
        
        public void setStackInSlot(int slot, @NotNull ItemStack stack, boolean smith) {
            ItemStack previous = getStackInSlot(slot);
            super.setStackInSlot(slot, stack);
            
            if (!smith && !previous.is(stack.getItem())) {
                var recipeHolder = level.getRecipeManager().getRecipeFor(RecipeTypesRegistry.MONTU_SMITH_TYPE.get(), createRecipeInput(this), level);
                if (recipeHolder.isPresent())
                    Network.sendTo(player, new IPacket() {
                        @Override
                        public void clientExecute(PacketContext ctx) {
                            var screen = Minecraft.getInstance().screen;
                            if (screen instanceof MontuGlovesScreen glovesScreen)
                                glovesScreen.onCraftStarted();
                        }
                    });
                
            }
        }
        
        public ItemStack center() {
            return this.getStackInSlot(1);
        }
        
        public ItemStack left() {
            return this.getStackInSlot(0);
        }
        
        public ItemStack right() {
            return this.getStackInSlot(2);
        }
    }
    
    public static class Provider implements MenuProvider {
        
        public ItemStack gloves;
        
        public Provider(ItemStack gloves) {
            if (gloves.getItem() != ItemsRegistry.MONTU_GLOVES) {
                throw new IllegalStateException("Are you dumb? You should pass a gloves item!!!");
            }
            this.gloves = gloves;
        }
        
        @Override
        public @NotNull Component getDisplayName() {
            return Component.empty();
        }
        
        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
            return new MontuGlovesContainer(pContainerId, pPlayerInventory, gloves);
        }
        
    }
    
}
