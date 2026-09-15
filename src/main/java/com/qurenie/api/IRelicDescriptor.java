package com.qurenie.api;

import it.hurts.sskirillss.relics.client.screen.description.misc.TextJustificator;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IRelicDescriptor {

    void modifyDescription(Player player, ItemStack stack, String ability, List<TextJustificator.LineEntry> rawLines, List<MutableComponent> dynamicComponents);

}
