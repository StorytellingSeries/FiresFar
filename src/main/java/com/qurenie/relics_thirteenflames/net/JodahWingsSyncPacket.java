package com.qurenie.relics_thirteenflames.net;

import com.qurenie.relics_thirteenflames.init.AttachmentsRegistry;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.function.Supplier;

public class JodahWingsSyncPacket extends IntegerSyncPacket{
    
    public JodahWingsSyncPacket(int data, int entityId) {
        super(data, entityId);
    }
    
    @Override
    Supplier<AttachmentType<Integer>> getAttachment() {
        return AttachmentsRegistry.WINGS_LAYER_DATA;
    }
    
}
