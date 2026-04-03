package com.qurenie.relics_thirteenflames.activity.call.settings;

import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;


public enum InventoryType {

    INVENTORY(InventorySlotReference::new),
    ARMOR(ArmorSlotReference::new),
    IN_HAND(InventorySlotReference::new),
    CURIO(CurioSlotReference::new);

    public final ReferenceConstructor constructor;

    InventoryType(ReferenceConstructor constructor) {
        this.constructor = constructor;
    }

    public interface ReferenceConstructor {

        SlotReference getReference();

    }

    public static class CurioSlotReference implements SlotReference {

        String slot;
        int order;

        public CurioSlotReference(String slot, int order) {
            this.slot = slot;
            this.order = order;
        }

        private CurioSlotReference() {
        }

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeUtf(slot);
            buf.writeInt(order);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            slot = buf.readUtf();
            order = buf.readInt();
        }

        @Override
        public ItemStack getStack(Player player) {
            return CuriosApi.getCuriosInventory(player).map((handler) -> {
                IDynamicStackHandler stacks = handler.getCurios().get(slot).getStacks();
                return stacks.getStackInSlot(order);
            }).orElse(ItemStack.EMPTY);
        }

        @Override
        public boolean correctReference(Player player, ItemStack stack) {
            return CuriosApi.getCuriosInventory(player).map((handler) -> {
                for (var entry : handler.getCurios().entrySet()) {
                    var slot = entry.getKey();
                    var stacks = entry.getValue().getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        if (FlamesUtils.sameUUID(stack, stacks.getStackInSlot(i))) {
                            this.slot = slot;
                            this.order = i;
                            return true;
                        }
                    }
                }
                return false;
            }).orElse(false);
        }
    }

    public static class ArmorSlotReference extends InventorySlotReference {
        public ArmorSlotReference(int slot) {
            super(slot);
        }

        private ArmorSlotReference() {
        }

        @Override
        public ItemStack getStack(Player player) {
            return player.getInventory().getArmor(slot);
        }

        @Override
        public boolean correctReference(Player player, ItemStack stack) {
            for (int i = 0; i < 4; i++) {
                if (FlamesUtils.sameUUID(stack, player.getInventory().getArmor(i))) {
                    this.slot = i;
                    return true;
                }
            }

            return false;
        }
    }

    public static class InventorySlotReference implements SlotReference
    {

        protected int slot;

        private InventorySlotReference() {}

        public InventorySlotReference(int slot) {
            this.slot = slot;
        }

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeInt(slot);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            slot = buf.readInt();
        }

        @Override
        public ItemStack getStack(Player player) {
            return player.getInventory().getItem(slot);
        }

        @Override
        public boolean correctReference(Player player, ItemStack stack) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (FlamesUtils.sameUUID(stack, player.getInventory().getItem(i))) {
                    this.slot = i;
                    return true;
                }
            }

            return false;
        }
    }

}
