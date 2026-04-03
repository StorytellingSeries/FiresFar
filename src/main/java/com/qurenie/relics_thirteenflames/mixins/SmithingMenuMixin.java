package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.api.event.SmithingBlockCraftEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

@Mixin(SmithingMenu.class)
public class SmithingMenuMixin {
    
    @Inject(method = "onTake", at = @At("TAIL"), remap = false)
    public void isNoAi(Player player, ItemStack stack, CallbackInfo ci) {
        EVENT_BUS.post(new SmithingBlockCraftEvent(player, stack));
    }
    
}
