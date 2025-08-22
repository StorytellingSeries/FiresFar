package com.qurenie.relics_thirteenflames.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.net.IPacket;

public class EntityPacket implements IPacket {
    
    protected int entityID;
    
    public EntityPacket(int entityID) {
        this.entityID = entityID;
    }
    
    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(entityID);
    }
    
    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        this.entityID = buf.readInt();
    }
    
    public @Nullable Entity getEntity(Level level) {
        return level.getEntity(entityID);
    }
    
}
