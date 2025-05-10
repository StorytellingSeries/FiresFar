package com.qurenie.relics_thirteenflames.net;

import com.qurenie.relics_thirteenflames.content.effects.PoisonEffectInstance;
import com.qurenie.relics_thirteenflames.content.items.ItemRonasSword;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;

@MainThreaded
public class RhonasSweepPacket implements IPacket {

    private ItemStack sword;
    public RhonasSweepPacket(ItemStack sword){
        this.sword = sword;
    }


    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        IPacket.super.write(buf);
        ItemStack.STREAM_CODEC.encode(buf, sword);
    }

    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        IPacket.super.read(buf);
        this.sword = ItemStack.STREAM_CODEC.decode(buf);
    }

    @Override
    public void serverExecute(PacketContext ctx) {
        IPacket.super.serverExecute(ctx);
        ServerPlayer sender = ctx.getSender();
        if(!sword.is(ItemsRegistry.RONAS_SWORD)) return;
        if(sender.getAttackStrengthScale(0.5F) > 0.9F){
            ItemRonasSword.poisonSwipe(sender, sword);
        } else {
            sender.level().playSound(null, sender, SoundEvents.AZALEA_FALL, SoundSource.MASTER, 1f, 0.8f);
            sender.level().playSound(null, sender, SoundEvents.SCULK_BLOCK_BREAK, SoundSource.MASTER, 0.7f, 0.7f);
            sender.level().playSound(null, sender, SoundEvents.BLAZE_BURN, SoundSource.MASTER, 0.8f, 2.4f);
            sender.addEffect(new PoisonEffectInstance(EffectsRegistry.POISSON, 100, 0, false, true, true, sword));
        }
    }
}
