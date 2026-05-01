package com.qurenie.api;

import it.hurts.sskirillss.relics.client.screen.description.misc.TextJustificator;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IRelicDescriptor {

    void modifyDescription(LocalPlayer player, ItemStack stack, String ability, List<TextJustificator.LineEntry> rawLines, List<MutableComponent> dynamicComponents);

}
