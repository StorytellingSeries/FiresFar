package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.api.ItemHurtEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    
    @ModifyVariable(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;processDurabilityChange(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;I)I"),
            argsOnly = true, remap = false)
    public int handleDamage(int damageHurt, int p_220158_, ServerLevel level, @Nullable LivingEntity entity, Consumer<Item> action) {
        ItemHurtEvent event = new ItemHurtEvent((ItemStack) (Object) this, damageHurt, entity, level, action);
        EVENT_BUS.post(event);
        return event.getDamageHurt();
    }
    
}
