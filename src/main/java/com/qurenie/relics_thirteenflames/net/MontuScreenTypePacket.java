package com.qurenie.relics_thirteenflames.net;

import com.qurenie.relics_thirteenflames.client.screen.gloves.MontuButton;
import com.qurenie.relics_thirteenflames.client.screen.gloves.MontuCompositeScreen;
import com.qurenie.relics_thirteenflames.content.container.MontuCompositeContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;

public class MontuScreenTypePacket implements IPacket {
    
    MontuCompositeContainer.MontuMenuType type;
    MontuButton.Direction direction;
    
    public MontuScreenTypePacket(MontuCompositeContainer.MontuMenuType type, MontuButton.Direction direction) {
        this.type = type;
        this.direction = direction;
    }
    
    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        IPacket.super.write(buf);
        buf.writeEnum(type);
        buf.writeEnum(direction);
    }
    
    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        IPacket.super.read(buf);
        this.type = buf.readEnum(MontuCompositeContainer.MontuMenuType.class);
        this.direction = buf.readEnum(MontuButton.Direction.class);
    }
    
    @Override
    public void clientExecute(PacketContext ctx) {
        IPacket.super.clientExecute(ctx);
        if (Minecraft.getInstance().screen instanceof MontuCompositeScreen screen) {
            screen.changeType(type, direction);
        }
    }
    
}
