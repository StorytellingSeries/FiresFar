package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.api.event.AnvilEnchantmentMergeEvent;
import com.qurenie.api.event.AnvilRepairPostCountEvent;
import com.qurenie.api.event.AnvilUpdatePostEvent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

@Mixin(AnvilMenu.class)
public class AnvilMixin {

    @Shadow
    @Final
    private DataSlot cost;

    @Shadow
    public int repairItemCountCost;

    @Inject(method = "createResult",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;get()I", ordinal = 1), remap = false)
    public void createResultEvent(CallbackInfo ci) {
        AnvilUpdatePostEvent event = new AnvilUpdatePostEvent(cost.get(), ((ItemCombinerAccessor) this).getPlayer());
        EVENT_BUS.post(event);
        cost.set(event.getCost());
    }

    @Inject(
            method = "onTake",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/Container;getItem(I)Lnet/minecraft/world/item/ItemStack;",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            ),
            remap = false
    )
    private void afterClearSlot0(Player player, ItemStack stack, CallbackInfo ci) {
//        repairItemCountCost
        AnvilRepairPostCountEvent event = new AnvilRepairPostCountEvent(stack, player, repairItemCountCost);
        EVENT_BUS.post(event);
        repairItemCountCost = event.getRepairItemCountCost();
    }

    @Redirect(
            method = "onTake",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V",
                    ordinal = 3 // ВАЖНО
            ),
            remap = false
    )
    private void redirectSetItem(Container container, int slot, ItemStack stack, Player player, ItemStack result) {
        if (slot == 1 && stack.isEmpty()) {
            AnvilEnchantmentMergeEvent event = new AnvilEnchantmentMergeEvent(player, stack, result);
            EVENT_BUS.post(event);

            if (!event.isBookTaken()) {
                return;
            }
        }

        container.setItem(slot, stack);
    }

}
