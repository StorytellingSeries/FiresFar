package com.qurenie.api.event;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Getter
public class ItemHurtEvent extends Event {
    
    @Setter
    private int damageHurt;
    private final @Nullable LivingEntity owner;
    private final ServerLevel level;
    private final ItemStack stack;
    @Setter
    private Consumer<Item> onBreak;
    
    public ItemHurtEvent(ItemStack stack, int damageHurt, @Nullable LivingEntity owner, ServerLevel level, Consumer<Item> onBreak) {
        this.damageHurt = damageHurt;
        this.owner = owner;
        this.level = level;
        this.onBreak = onBreak;
        this.stack = stack;
    }
    
}
