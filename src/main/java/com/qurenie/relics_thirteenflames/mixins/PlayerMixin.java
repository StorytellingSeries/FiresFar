package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.ScrollOfTruth;
import com.qurenie.relics_thirteenflames.init.DamageSourceRegistry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;


@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntityMixin {

    @Inject(method = "hurt", at = @At(value = "HEAD"), cancellable = true, remap = false)
    public void hurt(DamageSource pSource, float pAmount, CallbackInfoReturnable<Boolean> cir) {
        if(Objects.equals(pSource, DamageSourceRegistry.SUCC)){
            super.hurt(pSource, pAmount, cir);
            cir.cancel();
        }
    }

    @Inject(method = "onEnchantmentPerformed", at = @At("HEAD"), remap = false)
    public void onEnchantmentPerformed(ItemStack pEnchantedItem, int pLevelCost, CallbackInfo ci) {
        Player deez = ((Player)(Object)this);
        if(deez == null) return;
        for(ItemStack stack : deez.getInventory().items) {
            if(stack.getItem() instanceof  ScrollOfTruth scroll) {
                scroll.spreadRelicExperience(deez, stack, pLevelCost);
            }
        }
    }

}
