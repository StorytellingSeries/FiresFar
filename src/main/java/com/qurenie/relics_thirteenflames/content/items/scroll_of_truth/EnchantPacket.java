package com.qurenie.relics_thirteenflames.content.items.scroll_of_truth;

import com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.screen.EnchantmentSlotChangedPacket;
import com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.screen.ScrollOfTruthContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.net.PacketContext;

import java.util.Collection;

@MainThreaded
public class EnchantPacket implements IPacket {

    private EnchantData data;

    public EnchantPacket(ItemStack scroll, Collection<ScrollOfTruthContainerScreen.EnchantmentInstance> instances){
        this.data = new EnchantData(instances);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        IPacket.super.write(buf);
        buf.writeNbt(data.serializeNBT());
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        IPacket.super.read(buf);
        data = new EnchantData();
        data.deserializeNBT(buf.readNbt());
    }


    @Override
    public void serverExecute(PacketContext ctx) {
        IPacket.super.serverExecute(ctx);
        ServerPlayer player = ctx.getSender();
        if (player.containerMenu instanceof ScrollOfTruthContainer scrollOfTruthContainer) {
            int lvlCost = ScrollOfTruth.getFullEnchantmentCost(scrollOfTruthContainer.scroll, this.data.getEnchantmentInstances());
            if (player.experienceLevel >= lvlCost) {
                ItemStack item = scrollOfTruthContainer.fakeHandler.getStackInSlot(0);
                if (!item.isEmpty() && !item.isEnchanted()) {
                    for (ScrollOfTruthContainerScreen.EnchantmentInstance inst : this.data.getEnchantmentInstances()) {
                        item.enchant(inst.enchantment, inst.lvl);
                    }
                    player.level().playSound(null,player.getX(),player.getY(),player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.AMBIENT,1f,1f);
                    player.giveExperienceLevels(-lvlCost);
                    Network.sendTo(player,new EnchantmentSlotChangedPacket(item.copy()));
                }
            }
        }
    }

    public static boolean mayEnchant(Player player, ItemStack scroll, ItemStack item, Collection<ScrollOfTruthContainerScreen.EnchantmentInstance> instances){
        int lvlCost = ScrollOfTruth.getFullEnchantmentCost(scroll, instances);
        if (player.experienceLevel >= lvlCost){
            if (!item.isEmpty() && !item.isEnchanted()) {
                return true;
            }
        }
        return false;
    }
}
