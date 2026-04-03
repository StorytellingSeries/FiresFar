package com.qurenie.relics_thirteenflames.activity.call;

import com.qurenie.api.IActivityContainer;
import com.qurenie.relics_thirteenflames.activity.IActivitySetting;
import com.qurenie.relics_thirteenflames.activity.call.settings.IActivityCallSettings;
import com.qurenie.relics_thirteenflames.activity.call.settings.SlotReference;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;


public record CallInput(ItemStack stack, IActivitySetting setting, IActivityCallSettings call, SlotReference reference) {

    public IActivityContainer container() {
        return (IActivityContainer) stack.getItem();
    }

    public String getId() {
        return FlamesUtils.getOrCreateUUID(stack) + ":" + setting.getName();
    }

    public boolean validateAndCorrectReference(Player player) {
        boolean valid = reference.verifyReference(player, stack);
        if (valid)
            return true;

        return reference.correctReference(player, stack);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CallInput other)) return false;

        return getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
