package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.api.PacketHandleEvent;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

@Mixin(PacketUtils.class)
public class THISISSOBADMIXIN {
    
    @Inject(remap = false, method = "lambda$ensureRunningOnSameThread$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/Packet;handle(Lnet/minecraft/network/PacketListener;)V"), cancellable = true)
    private static void onPacketHandle(PacketListener processor, Packet packet, CallbackInfo ci) {
        PacketHandleEvent event = new PacketHandleEvent(processor, packet);
        if (EVENT_BUS.post(event).isCanceled())
            ci.cancel();
    }
    
}
