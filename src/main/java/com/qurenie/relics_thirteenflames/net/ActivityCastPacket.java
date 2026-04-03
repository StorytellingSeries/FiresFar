package com.qurenie.relics_thirteenflames.net;

import com.qurenie.api.IActivityContainer;
import com.qurenie.relics_thirteenflames.activity.call.ActivityCallLogic;
import com.qurenie.relics_thirteenflames.activity.call.CallInput;
import com.qurenie.relics_thirteenflames.activity.call.settings.InventoryType;
import com.qurenie.relics_thirteenflames.activity.call.settings.SlotReference;
import com.qurenie.relics_thirteenflames.content.items.ItemTravellerSword;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;

public class ActivityCastPacket implements IPacket {

    String name;
    CallInput input;
    SlotReference reference;

    public ActivityCastPacket(String name, CallInput input){
        this.name = name;
        this.input = input;
    }
    
    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeEnum(input.call().getInventoryType());
        input.reference().encode(buf);
    }
    
    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        this.name = buf.readUtf();
        var reference = buf.readEnum(InventoryType.class).constructor.getReference();
        reference.decode(buf);
        this.reference = reference;
    }
    
    @Override
    public void serverExecute(PacketContext ctx) {
        ItemStack stack = reference.getStack(ctx.getSender());
        if (stack.getItem() instanceof IActivityContainer container) {
            var setting = container.getActivitySettings().get(name);

            if (setting != null && setting.getCallSettings() != null) {
                ActivityCallLogic.INSTANCE.serverCall(ctx.getSender(), new CallInput(
                        stack,
                        setting,
                        setting.getCallSettings(),
                        reference
                ));
            }
        }
    }
    
}
