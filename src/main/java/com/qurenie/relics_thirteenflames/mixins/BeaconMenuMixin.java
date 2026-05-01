package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.relics_thirteenflames.content.blocks.AurithecBeaconBlock;
import com.qurenie.relics_thirteenflames.content.container.AuritekhBeaconMenu;
import com.qurenie.relics_thirteenflames.init.BlocksRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(BeaconMenu.class)
public class BeaconMenuMixin {
    
    @Shadow
    @Final
    @Mutable
    private Container beacon;
    
    @Shadow @Final private ContainerLevelAccess access;
    
    @Inject(method = "<init>(ILnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/BeaconMenu;checkContainerDataCount(Lnet/minecraft/world/inventory/ContainerData;I)V"))
    private void onInit(CallbackInfo ci) {
        BeaconMenu menu = (BeaconMenu)(Object)this;
        
        this.beacon = new SimpleContainer(1) {
            @Override
            public boolean canPlaceItem(int index, ItemStack stack) {
                return stack.is(ItemTags.BEACON_PAYMENT_ITEMS) || stack.is(ItemsRegistry.AURITEKH_INGOT);
            }
            
            @Override
            public int getMaxStackSize() {
                return 1;
            }
            
            @Override
            public void setChanged() {
                super.setChanged();
                
                if (getItem(0).is(ItemsRegistry.AURITEKH_INGOT) && !(menu instanceof AuritekhBeaconMenu)) {
                    access.execute(((level, pos) -> {
                        level.explode(null, null, null, pos.getBottomCenter(),
                                3, false, Level.ExplosionInteraction.NONE);
                        ParticleHelper.spawnParticles(level, ParticleHelper.constructSimpleSpark(FlamesUtils.fromRGBI(239, 215, 182), 0.25f, 50, 0.95f).withGravity(1.5f),
                                pos.getBottomCenter(), 50, 0.15, 0.15, 0.15, 0.15);
                        ParticleHelper.spawnParticles(level, ParticleHelper.constructSmoke(FlamesUtils.fromRGBI(239, 215, 182), 0.8f, 70, 0f).withGravity(0.5f),
                                pos.getBottomCenter(), 25, 0.4, 0.4, 0.4, 0.04);
                        level.setBlock(pos, BlocksRegistry.BEACON.defaultBlockState(), 3);
                    }));
                    setItem(0, ItemStack.EMPTY);
                }
            }
        };
    }
    
}
