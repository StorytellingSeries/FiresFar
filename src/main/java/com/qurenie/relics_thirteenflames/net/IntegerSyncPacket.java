package com.qurenie.relics_thirteenflames.net;

import com.qurenie.relics_thirteenflames.init.AttachmentsRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;

import java.util.function.Supplier;

public abstract class IntegerSyncPacket implements IPacket {
    
    int data;
    int entityId;
    
    public IntegerSyncPacket(int data, int entityId) {
        this.data = data;
        this.entityId = entityId;
    }
    
    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        this.data = buf.readInt();
        this.entityId = buf.readInt();
    }
    
    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(data);
        buf.writeInt(entityId);
    }
    
    @Override
    public void clientExecute(PacketContext ctx) {
        Entity e = ctx.getLevel().getEntity(entityId);
        if (e != null)
            e.setData(getAttachment(), data);
    }
    
    abstract Supplier<AttachmentType<Integer>> getAttachment();
}
