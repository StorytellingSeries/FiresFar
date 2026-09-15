package com.qurenie.api;

import com.qurenie.relics_thirteenflames.activity.IActivitySetting;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.apache.http.concurrent.Cancellable;

@Getter
public abstract class ActivityCallEvent extends Event {

    Player player;
    ItemStack stack;
    IActivitySetting setting;

    public ActivityCallEvent(Player player, ItemStack stack, IActivitySetting setting) {
        this.player = player;
        this.stack = stack;
        this.setting = setting;
    }

    public static class Post extends ActivityCallEvent {

        @Getter
        boolean clientSide;

        public Post(Player player, ItemStack stack, IActivitySetting setting, boolean clientSide) {
            super(player, stack, setting);
            this.clientSide = clientSide;
        }
    }

    public static class Cast extends ActivityCallEvent implements ICancellableEvent {

        @Getter
        boolean canCast;

        @Getter
        boolean clientSide;

        public Cast(Player player, ItemStack stack, IActivitySetting setting, boolean clientSide, boolean canCast) {
            super(player, stack, setting);
            this.canCast = canCast;
            this.clientSide = clientSide;
        }
    }

    public static class Visible extends ActivityCallEvent {

        @Getter
        @Setter
        boolean isVisible;

        public Visible(Player player, ItemStack stack, IActivitySetting setting, boolean isVisible) {
            super(player, stack, setting);
            this.isVisible = isVisible;
        }
    }
}
