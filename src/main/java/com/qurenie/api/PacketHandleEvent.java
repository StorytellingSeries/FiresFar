package com.qurenie.api;

import lombok.Getter;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

@Getter
public class PacketHandleEvent extends Event implements ICancellableEvent {
    
    private final PacketListener processor;
    private final Packet<?> packet;
    
    public PacketHandleEvent(PacketListener processor, Packet<?> packet) {
        this.processor = processor;
        this.packet = packet;
    }
    
}
