package com.qurenie.relics_thirteenflames.net;

import com.qurenie.relics_thirteenflames.init.AttachmentsRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;

public class AttachmentRemoveSyncPacket implements IPacket {
    
    AttachmentType<?> type;
    int entityId;
    
    public AttachmentRemoveSyncPacket(AttachmentType<?> type, int entityId) {
        this.type = type;
        this.entityId = entityId;
    }
    
    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        this.type = AttachmentsRegistry.STREAM_CODEC.decode(buf);
        this.entityId = buf.readInt();
    }
    
    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        AttachmentsRegistry.STREAM_CODEC.encode(buf, type);
        buf.writeInt(entityId);
    }
    
    @Override
    public void clientExecute(PacketContext ctx) {
        Entity e = ctx.getLevel().getEntity(entityId);
        if (e != null)
            e.removeData(type);
    }
    
}
