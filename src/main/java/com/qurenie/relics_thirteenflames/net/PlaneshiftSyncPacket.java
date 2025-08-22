package com.qurenie.relics_thirteenflames.net;

import com.qurenie.relics_thirteenflames.init.AttachmentsRegistry;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.function.Supplier;

public class PlaneshiftSyncPacket extends IntegerSyncPacket {
    
    public PlaneshiftSyncPacket(int data, int entityId) {
        super(data, entityId);
    }
    
    @Override
    Supplier<AttachmentType<Integer>> getAttachment() {
        return AttachmentsRegistry.PLANESHIFT_TICK;
    }
    
}
