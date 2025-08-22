package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.api.EntityIgnoreExplosionEvent;
import com.qurenie.relics_thirteenflames.init.AttachmentsRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

@Mixin(Explosion.class)
public class ExplosionMixin {
    
    @Redirect(
            method = "explode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;ignoreExplosion(Lnet/minecraft/world/level/Explosion;)Z"
            ),
            remap = false
    )
    private static boolean redirect(Entity instance, Explosion explosion) {
        EntityIgnoreExplosionEvent explosionEvent = new EntityIgnoreExplosionEvent(instance, explosion, instance.ignoreExplosion(explosion));
        EVENT_BUS.post(explosionEvent);
        return explosionEvent.isShouldIgnore();
    }
    
}
