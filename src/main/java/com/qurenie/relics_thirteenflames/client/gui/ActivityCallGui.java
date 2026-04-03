package com.qurenie.relics_thirteenflames.client.gui;

import com.qurenie.relics_thirteenflames.activity.call.ActivityCallLogic;
import com.qurenie.relics_thirteenflames.activity.call.ActivityInputHandler;
import com.qurenie.relics_thirteenflames.data.ActivityState;
import it.hurts.octostudios.octolib.util.OctoColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.*;

@EventBusSubscriber
public class ActivityCallGui {

    LinkedHashMap<String, CardGuiEntity> cards = new LinkedHashMap<>();

    public static int openness = 0;
    private static boolean opening = false;
    public static final int OPEN_DELAY = 5;

    public static final ActivityCallGui INSTANCE = new ActivityCallGui();

    public void open() {
        Minecraft mc = Minecraft.getInstance();
        ActivityCallLogic.INSTANCE.cacheActivities(mc.player);

        cards.keySet().retainAll(ActivityState.getKeys());

        ActivityState.getState().entrySet().stream()
                .filter(entry -> !cards.containsKey(entry.getKey()))
                .forEach(input -> cards.put(input.getKey(),
                        new CardGuiEntity(input.getKey(), input.getValue().call().getResourceLocation(mc.player, input.getValue().stack()))));

        mc.mouseHandler.releaseMouse();

        removeSelected();
    }

    public void removeSelected() {
        var shuffled = shuffled();
        if (shuffled.containsKey(CardBehaviour.SELECTED.getName()))
            for (CardGuiEntity card : shuffled().get(CardBehaviour.SELECTED.getName())) {
                setBehaviour(card, CardBehaviour.IDLE);
            }
    }

    public void hide() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.screen == null)
            mc.mouseHandler.grabMouse();
    }

    public void fail(String input) {
        var card = cards.get(input);
        card.setShake(10);

        card.setTempColor(new OctoColor(0xFFDD0809));
    }

    void setBehaviour(CardGuiEntity newOne, CardBehaviour behaviour) {
        List<CardGuiEntity> group = shuffled().getOrDefault(behaviour.getName(), List.of());

        behaviour.onSelect(newOne, group);
        newOne.setBehaviour(behaviour);
    }

    public boolean onMouseClick(double mx, double my, int button, int action) {
        if (isOpened())
            for (var entry : cards.entrySet()) {
                var card = entry.getValue();
                var input = entry.getKey();

                boolean selected = card.mouseSelectedAbsolute(mx, my, false);

                if (selected && button == 0 && action == 1) {
                    ActivityCallLogic.INSTANCE.clientCall(Minecraft.getInstance().player, input);

                    return true;
                }

                if (button == 1 && action == 1 && selected) {
                    setBehaviour(card, CardBehaviour.SELECTED);

                    return true;
                }
            }

        if (button == 1 && action == 1) {
            var shuffled = shuffled();
            if (!shuffled.containsKey(CardBehaviour.SELECTED.getName()))
                return false;

            ActivityCallLogic.INSTANCE.clientCall(Minecraft.getInstance().player,
                    shuffled.get(CardBehaviour.SELECTED.getName()).getFirst().getId());
            return true;
        }

        return false;
    }

    private void validateOrDestroy(Player player, String input, CardGuiEntity card) {
        if (!ActivityCallLogic.INSTANCE.validate(player, input))
            setBehaviour(card, CardBehaviour.DYING);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || INSTANCE.cards.isEmpty()) return;

        var gui = event.getGuiGraphics();

        int cx = mc.getWindow().getGuiScaledWidth() / 2;
        int cy = mc.getWindow().getGuiScaledHeight() / 2;

        INSTANCE.renderCards(gui, cx, cy, event.getPartialTick().getGameTimeDeltaTicks());
    }

    private HashMap<String, List<CardGuiEntity>> shuffled() {
        return cards.values().stream().reduce(new HashMap<>(),
                (map, card) -> {
                    String behaviour = card.getBehaviour().getName();

                    map.computeIfAbsent(behaviour, ignored -> new ArrayList<>());
                    map.get(behaviour).add(card);

                    return map;
                },
                (m1, m2) -> {
                    m1.keySet().forEach(k -> {
                        m1.merge(k, m2.get(k), (v1, v2) -> {
                            v1.addAll(v2);
                            return v1;
                        });
                    });
                    return m1;
                });
    }

    public void tick() {
        for (var entry : shuffled().entrySet()) {
            var cards = entry.getValue();
            for (int i = 0; i < cards.size(); i++) {
                var card = cards.get(i);

                card.tickBehaviour(cards, i);
                card.tick();
            }
        }

        syncData();
    }

    private void syncData() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        cards.entrySet().removeIf(entry -> {
            var card = entry.getValue();
            var key = entry.getKey();

            validateOrDestroy(player, key, card);
            if (card.unconnected())
                return !card.isAlive();

            card.setCooldown(ActivityCallLogic.INSTANCE.getCooldown(key));
            return !card.isAlive();
        });
    }

    public static boolean isOpened() {
        return openness > 0 || opening;
    }

    public static void startHide(boolean force) {
        opening = false;

        if (force) {
            openness = 0;
        }
    }

    private static boolean wasHolding = false;

    @SubscribeEvent
    public static void tickEvent(ClientTickEvent.Pre event) {
        if (!ActivityCallGui.INSTANCE.cards.isEmpty())
            ActivityCallGui.INSTANCE.tick();

        if (opening) {
            openness = Math.min(openness + 1, OPEN_DELAY);
        } else if (openness > 0) {
            openness--;

            if (openness == 0)
                ActivityCallGui.INSTANCE.hide();
        }

        boolean holding = ActivityInputHandler.isHolding();

        if (holding && !wasHolding) {
            ActivityCallGui.INSTANCE.open();
            opening = true;
        }

        if (!holding && wasHolding)
            startHide(false);

        wasHolding = holding;
    }


    private void renderCards(GuiGraphics gui, int cx, int cy, float pt) {
        for (CardGuiEntity cardGuiEntity : cards.values())
            cardGuiEntity.render(gui, cx, cy, pt);
    }

}
