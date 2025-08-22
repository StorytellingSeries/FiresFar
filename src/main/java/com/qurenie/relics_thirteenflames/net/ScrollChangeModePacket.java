package com.qurenie.relics_thirteenflames.net;

import com.qurenie.relics_thirteenflames.content.items.misc.ScrollColorMode;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.SCROLL_COLOR_MODE;

@MainThreaded
public class ScrollChangeModePacket implements IPacket {

    private int delta;
    public ScrollChangeModePacket(int delta){
        this.delta = delta;
    }


    @Override
    public void write(FriendlyByteBuf buf) {
        IPacket.super.write(buf);
        buf.writeInt(delta);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        IPacket.super.read(buf);
        this.delta = buf.readInt();
    }

    @Override
    public void serverExecute(PacketContext ctx) {
        IPacket.super.serverExecute(ctx);
        ServerPlayer sender = ctx.getSender();
        ItemStack item = sender.getMainHandItem();
        if (item.is(ItemsRegistry.SCROLL_OF_TRUTH)){
            ScrollColorMode colorMode = item.getOrDefault(SCROLL_COLOR_MODE, ScrollColorMode.GRAY);
            ScrollColorMode newMode;
            if (colorMode.id + delta > -1) {
                newMode = ScrollColorMode.values()[(colorMode.id + delta) % ScrollColorMode.values().length];
            }else{
                newMode = ScrollColorMode.values()[ScrollColorMode.values().length - 1];
            }
            var effect = ctx.getSender().getEffect(colorMode.effect);
            if (effect != null && effect.endsWithin(26))
                ctx.getSender().removeEffect(colorMode.effect);
                
            item.set(SCROLL_COLOR_MODE, newMode);
        }
    }
}
