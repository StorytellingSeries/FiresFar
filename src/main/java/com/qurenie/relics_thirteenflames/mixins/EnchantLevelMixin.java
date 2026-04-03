package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.api.event.EnchantCostEventPre;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

@Mixin(EnchantmentMenu.class)
public class EnchantLevelMixin {
    
    @ModifyArg(
            method = "lambda$slotsChanged$0",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/EventHooks;onEnchantmentLevelSet(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;IILnet/minecraft/world/item/ItemStack;I)I"),
            remap = false,
            index = 5)
    public int change(int level) {
        if (((EnchantmentMenu) (Object) this).getSlot(4).container instanceof Inventory inventory) {
            var event = new EnchantCostEventPre(inventory.player, level);
            EVENT_BUS.post(event);
            return event.getNewLevel();
        }
        
        return level;
    }
    
}
