package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.relics_thirteenflames.init.AttachmentsRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.system.casts.handlers.HUDRenderHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// priority = PRIORITY.HIGHESTx2
@Mixin(HUDRenderHandler.CastEvents.class)
public class HUDRenderHandlerMixin {
    
    @Redirect(
            method = "onKeyPressed",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/hurts/sskirillss/relics/items/relics/base/IRelicItem;canPlayerUseAbility(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;)Z"
            ),
            remap = false
    )
    private static boolean redirect(IRelicItem instance, Player player, ItemStack stack, String ability) {
        if (player.hasData(AttachmentsRegistry.PLANESHIFT_TICK))
            return false;
        
        return instance.canPlayerUseAbility(player, stack, ability);
    }
}
