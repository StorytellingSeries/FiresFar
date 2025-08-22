package com.qurenie.relics_thirteenflames.client.render.item;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.items.ItemJodahStaff;
import com.qurenie.relics_thirteenflames.content.items.misc.JodahTier;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JodahStaffItemRenderer extends EmissiveItemRenderer {
    
    @Override
    protected @Nullable RenderType getRenderType(@NotNull ItemStack stack, int index) {
        if (!(stack.getItem() instanceof ItemJodahStaff staff))
            return null;
        
        JodahTier tier = stack.getOrDefault(ComponentRegistry.JODAH_TIER, JodahTier.D);
        boolean active = staff.isAbilityTicking(stack, "health_theft");
        return RenderType.entityTranslucentCull(
                ThirteenFlames.rl(String.format("textures/item/jodah_staff_rank%d%s%s.png",
                        JodahTier.values().length - tier.ordinal(),
                        active ? "_purple" : "",
                        index > 1 ? "_emissive" : "")));
    }
    
}
