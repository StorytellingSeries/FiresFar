package com.qurenie.relics_thirteenflames.content.items.scroll_of_truth;

import com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.screen.EnchantmentSlotChangedPacket;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.net.Network;

public class ScrollOfTruthContainer extends AbstractContainerMenu {

    public ItemStackHandler fakeHandler;
    public ItemStack scroll;

    public ScrollOfTruthContainer(int pContainerId,Inventory playerInv,ItemStack scroll) {
        super(ScrollOfTruthInit.SCROLL_OF_TRUTH_MENU.get(), pContainerId);
        this.fakeHandler = new ItemStackHandler(1);
        this.scroll = scroll;
        int y = 65;

        this.addSlot(new SlotItemHandler(fakeHandler, 0, 81, -6){

            @Override
            public void set(@NotNull ItemStack stack) {
                super.set(stack);
                if (!playerInv.player.level().isClientSide() && playerInv.player instanceof ServerPlayer sP){
                    Network.send(PacketDistributor.PLAYER.with(() -> sP),new EnchantmentSlotChangedPacket(stack));
                }
            }

            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.isEnchantable() && !stack.isEnchanted();
            }
        });


        for(int l = 0; l < 3; ++l) {
            for(int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInv, k + l * 9 + 9, 8 + k * 18 + 1, l * 18 + 51 + y));
            }
        }

        for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInv, i1, 8 + i1 * 18 + 1, 109 + y));
        }

    }


    public ScrollOfTruthContainer(int cid, Inventory playerInv,FriendlyByteBuf buf){
        this(cid,playerInv,buf.readItem());
    }


    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
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
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        ItemStack item = fakeHandler.getStackInSlot(0);
        if (!item.isEmpty()){
            ItemEntity entity = new ItemEntity(player.level(),player.getX(),player.getY(),player.getZ(),item.copy());
            player.level().addFreshEntity(entity);
        }
    }

    public static class Provider implements MenuProvider{

        public ItemStack scroll;

        public Provider(ItemStack scroll){
            if (scroll.getItem() != ItemsRegistry.SCROLL_OF_TRUTH){
                throw new IllegalStateException("Are you dumb? You should pass a scroll item!!!");
            }
            this.scroll = scroll;
        }

        @Override
        public Component getDisplayName() {
            return Component.empty();
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
            return new ScrollOfTruthContainer(pContainerId,pPlayerInventory,this.scroll);
        }
    }
}
