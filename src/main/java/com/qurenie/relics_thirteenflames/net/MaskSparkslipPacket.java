package com.qurenie.relics_thirteenflames.net;

import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;

public class MaskSparkslipPacket implements IPacket {
    
    boolean hasTarget;
    int entityId;
    Vec3 position;
    
    public MaskSparkslipPacket(@Nullable Entity target, Vec3 position) {
        if (target != null) {
            hasTarget = true;
            this.entityId = target.getId();
        }
        this.position = position;
    }
    
    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        this.hasTarget = buf.readBoolean();
        this.entityId = buf.readInt();
        this.position = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }
    
    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(hasTarget);
        buf.writeInt(entityId);
        buf.writeDouble(position.x);
        buf.writeDouble(position.y);
        buf.writeDouble(position.z);
    }
    
    @Override
    public void serverExecute(PacketContext ctx) {
        Entity target = hasTarget ? ctx.getLevel().getEntity(entityId) : null;
        if (!(target instanceof LivingEntity))
            target = null;
        
        ItemStack stack = ctx.getSender().getItemBySlot(EquipmentSlot.HEAD);
        if (stack.is(ItemsRegistry.JODAH_MASK))
            ItemsRegistry.JODAH_MASK.onSparkslipPacket(stack, ctx.getSender(), position, (LivingEntity) target);
    }
    
}
