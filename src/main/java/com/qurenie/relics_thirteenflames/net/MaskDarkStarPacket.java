package com.qurenie.relics_thirteenflames.net;

import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;

public class MaskDarkStarPacket implements IPacket {
    
    boolean hasTarget;
    int entityId;
    
    public MaskDarkStarPacket(@Nullable Entity target) {
        if (target != null) {
            hasTarget = true;
            this.entityId = target.getId();
        }
    }
    
    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        if (buf.readBoolean())
            this.entityId = buf.readInt();
    }
    
    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(hasTarget);
        if (hasTarget)
            buf.writeInt(entityId);
    }
    
    @Override
    public void serverExecute(PacketContext ctx) {
        Entity target = hasTarget ? ctx.getLevel().getEntity(entityId) : null;
        if (!(target instanceof LivingEntity))
            target = null;
        
        ItemStack stack = ctx.getSender().getItemBySlot(EquipmentSlot.HEAD);
        if (stack.is(ItemsRegistry.JODAH_MASK))
            ItemsRegistry.JODAH_MASK.onDarkStarPacket(stack, ctx.getSender(), (LivingEntity) target);
    }
    
}
