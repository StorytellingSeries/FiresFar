package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.api.MeleeAttackCheckEvent;
import com.qurenie.relics_thirteenflames.client.screen.gloves.MontuCompositeScreen;
import com.qurenie.relics_thirteenflames.client.screen.gloves.MontuGlovesScreen;
import com.qurenie.relics_thirteenflames.content.container.MontuCompositeContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

@Mixin(Screen.class)
public class MenuScreenMixin {
    
    @Shadow @Nullable protected Minecraft minecraft;
    
    @Inject(remap = false, method = "renderTransparentBackground", cancellable = true, at = @At(value = "HEAD"))
    public void check(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (minecraft == null)
            return;
        
        if (((Object) this) instanceof AbstractContainerScreen
                && minecraft.screen instanceof MontuCompositeScreen
                && !(((Object) this) instanceof MontuCompositeScreen))
            ci.cancel();
            
    }
    
}
