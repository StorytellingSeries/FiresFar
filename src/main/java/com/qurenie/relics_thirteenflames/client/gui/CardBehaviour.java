package com.qurenie.relics_thirteenflames.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.qurenie.relics_thirteenflames.data.ActivityState.OPEN_DELAY;

public abstract class CardBehaviour {

    public static final CardBehaviour IDLE = new CardBehaviour("idle", SizeModifier.TARGET, SizeModifier.OPENNESS, SizeModifier.MOUSE_SELECT) {

        @Override
        void tickTargets(CardGuiEntity card, List<CardGuiEntity> group, int index) {
            var mc = Minecraft.getInstance();

            float p = Math.min(OPEN_DELAY, ActivityCallGui.openness) / (float) OPEN_DELAY;
            float percent = p * p;
            int radius = (int) (Math.sqrt(group.size() - 1) * 22 * percent);

            double angle = (2 * Math.PI * index) / group.size() - Math.PI / 2 + percent * Math.PI / 6
                    + ((mc.player.tickCount) % 1500f) / 1500f * Math.PI * 2;

            float x = (float) (Math.cos(angle) * radius);
            float y = (float) (Math.sin(angle) * radius);

            card.setTarget(new CardTarget(new CardPosition(x, y), 1f));
        }

    };

    public static final CardBehaviour DYING = new CardBehaviour("dying", SizeModifier.TARGET) {

        @Override
        void tickTargets(CardGuiEntity card, List<CardGuiEntity> group, int index) {
        }

        @Override
        void onSelect(CardGuiEntity card, List<CardGuiEntity> other) {
            super.onSelect(card, other);

            card.setTarget(new CardTarget(card.position, 0f));
        }

        @Override
        boolean isAlive(CardGuiEntity card) {
            return card.getSize() > 0;
        }

        @Override
        boolean unconnected(CardGuiEntity card) {
            return true;
        }
    };

    public static final CardBehaviour SELECTED = new CardBehaviour("selected", SizeModifier.TARGET) {

        @Override
        void tickTargets(CardGuiEntity card, List<CardGuiEntity> group, int index) {
        }

        @Override
        void onSelect(CardGuiEntity card, List<CardGuiEntity> other) {
            super.onSelect(card, other);

            card.setTarget(new CardTarget(new CardPosition(0, -80), 1.6f));
            for (var c : other)
                c.setBehaviour(IDLE);
        }

        @Override
        void postRender(CardGuiEntity card, GuiGraphics gui, float partialTicks) {
            super.postRender(card, gui, partialTicks);

            RenderSystem.enableBlend();

            int time = Minecraft.getInstance().player.tickCount;
            int i = (time / 2) % 13;

            if (i < 4)
                i = 0;
            else if (i < 6)
                i = i - 3;
            else if (i < 9)
                i = 3;
            else
                i = i - 6;

            gui.blit(ThirteenFlames.rl("textures/gui/ability/frame_selection_active.png"), -12, -17, 0, 33 * Mth.clamp(i, 0, 6) + (i > 2 ? 2 : 0), 24, 33, 24, 233);

            RenderSystem.disableBlend();
        }
    };

    @Getter
    private final Set<SizeModifier> modifiers = new HashSet<>();
    @Getter
    private final String name;

    public CardBehaviour(String name, SizeModifier... modifiers) {
        this.name = name;
        this.modifiers.addAll(Arrays.stream(modifiers).toList());
    }

    abstract void tickTargets(CardGuiEntity card, List<CardGuiEntity> group, int index);

    void onSelect(CardGuiEntity card, List<CardGuiEntity> other) {
    }

    boolean isAlive(CardGuiEntity card) {
        return true;
    }

    boolean unconnected(CardGuiEntity card) {
        return false;
    }

    void postRender(CardGuiEntity card, GuiGraphics gui, float partialTicks) {
    }
}
