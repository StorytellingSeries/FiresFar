package com.qurenie.relics_thirteenflames.net;

import com.qurenie.relics_thirteenflames.init.AttachmentsRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;

public class SkintDataAttachmentPacket implements IPacket {
    
    int skintCharges;
    int antiskintCharges;
    int entityId;
    
    public SkintDataAttachmentPacket(Entity entity) {
        this.skintCharges = entity.getData(AttachmentsRegistry.SKINT_DATA);
        this.entityId = entity.getId();
        this.antiskintCharges = entity.getData(AttachmentsRegistry.ANTISKINT_DATA);
    }
    
    public SkintDataAttachmentPacket(int skintCharges, int antiskintCharges, int entityId) {
        this.skintCharges = skintCharges;
        this.entityId = entityId;
        this.antiskintCharges = antiskintCharges;
    }
    
    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(skintCharges);
        buf.writeInt(antiskintCharges);
        buf.writeInt(entityId);
    }
    
    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        this.skintCharges = buf.readInt();
        this.antiskintCharges = buf.readInt();
        this.entityId = buf.readInt();
    }
    
    @Override
    public void clientExecute(PacketContext ctx) {
        Entity e = ctx.getLevel().getEntity(entityId);
        if (e != null) {
            e.setData(AttachmentsRegistry.SKINT_DATA.get(), skintCharges);
            e.setData(AttachmentsRegistry.ANTISKINT_DATA.get(), antiskintCharges);
        }
    }
    
}
