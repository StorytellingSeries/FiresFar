package com.qurenie.api;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

@Getter
public class JodahMaskEvent extends Event {

    Entity living;

    public JodahMaskEvent(Entity living) {
        this.living = living;
    }

    public static class AddScint extends JodahMaskEvent {

        @Setter
        @Getter
        int value;

        @Setter
        @Getter
        int maxValue;

        public AddScint(Entity living, int value, int maxValue) {
            super(living);
            this.value = value;
            this.maxValue = maxValue;
        }
    }

    public static class AddAntiscint extends JodahMaskEvent {

        @Setter
        @Getter
        int value;

        @Setter
        @Getter
        int maxValue;

        public AddAntiscint(Entity living, int value, int maxValue) {
            super(living);
            this.value = value;
            this.maxValue = maxValue;
        }
    }
}
