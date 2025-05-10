package com.qurenie.relics_thirteenflames.content.items.scroll_of_truth;

import com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.screen.EnchantmentSlotChangedPacket;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.net.PacketContext;

import java.util.Collection;
import java.util.stream.Stream;

@MainThreaded
public class EnchantPacket implements IPacket {
    
    private Collection<EnchantmentInstance> data;
    
    public EnchantPacket(Collection<EnchantmentInstance> instances) {
        this.data = instances;
    }
    
    public static boolean mayEnchant(Player player, ItemStack scroll, ItemStack item, Collection<EnchantmentInstance> instances) {
        int lvlCost = ScrollOfTruth.getFullEnchantmentCost(scroll, instances);
        if (player.experienceLevel >= lvlCost) {
            if (!item.isEmpty() && !item.isEnchanted()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(data.size());
        data.forEach(inst -> writeInst(buf, inst));
    }
    
    private void writeInst(RegistryFriendlyByteBuf buf, EnchantmentInstance inst) {
        Enchantment.STREAM_CODEC.encode(buf, inst.enchantment);
        buf.writeInt(inst.level);
    }
    
    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        this.data = Stream.generate(() -> readInst(buf)).limit(buf.readInt()).toList();
    }
    
    private EnchantmentInstance readInst(RegistryFriendlyByteBuf buf) {
        return new EnchantmentInstance(Enchantment.STREAM_CODEC.decode(buf), buf.readInt());
    }
    
    @Override
    public void serverExecute(PacketContext ctx) {
        IPacket.super.serverExecute(ctx);
        
        ServerPlayer player = ctx.getSender();
        if (player.containerMenu instanceof ScrollOfTruthContainer scrollOfTruthContainer) {
            int lvlCost = ScrollOfTruth.getFullEnchantmentCost(scrollOfTruthContainer.scroll, data);
            if (player.experienceLevel >= lvlCost) {
                ItemStack item = scrollOfTruthContainer.fakeHandler.getStackInSlot(0);
                if (!item.isEmpty() && !item.isEnchanted()) {
                    for (EnchantmentInstance inst : data)
                        item.enchant(inst.enchantment, inst.level);
                    
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.AMBIENT, 1f, 1f);
                    player.giveExperienceLevels(-lvlCost);
                    Network.sendTo(player, new EnchantmentSlotChangedPacket(item.copy()));
                }
            }
        }
    }
    
}
