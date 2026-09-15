package com.qurenie.relics_thirteenflames.client.gui;

import com.qurenie.api.ActivityCallEvent;
import com.qurenie.relics_thirteenflames.activity.call.ActivityCallLogic;
import com.qurenie.relics_thirteenflames.activity.call.ActivityInputHandler;
import com.qurenie.relics_thirteenflames.data.ActivityState;
import com.qurenie.relics_thirteenflames.net.ActivityCastPacket;
import it.hurts.octostudios.octolib.util.OctoColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.zeith.hammerlib.net.Network;

import java.util.*;

@EventBusSubscriber(Dist.CLIENT)
public class ActivityCallGui {

    LinkedHashMap<String, CardGuiEntity> cards = new LinkedHashMap<>();

    public static int openness = 0;
    private static boolean opening = false;
    public static final int OPEN_DELAY = 5;

    public static final ActivityCallGui INSTANCE = new ActivityCallGui();

    public void open() {
        Minecraft mc = Minecraft.getInstance();
        ActivityState.INSTANCE.cacheActivities(mc.player);

        cards.keySet().retainAll(ActivityState.getKeys());

        ActivityState.getState().entrySet().stream()
                .filter(entry -> !cards.containsKey(entry.getKey()))
                .forEach(input -> cards.put(input.getKey(),
                        new CardGuiEntity(input.getKey(), input.getValue().call().getInventoryType(), input.getValue().call().getResourceLocation(mc.player, input.getValue().stack()))));

        if (!cards.isEmpty()) {
            mc.mouseHandler.releaseMouse();

            removeSelected();
        }
    }

    public void select(CardGuiEntity card) {
        // TODO: several selected cards???
        var selected = removeSelected();
        // TODO: string set
        ActivityCallLogic.INSTANCE.selectClient(card.getId(),
                selected.isEmpty() ? null : selected.getFirst().getId());
        setBehaviour(card, CardBehaviour.SELECTED);
    }

    public CardGuiEntity removeSelection(CardGuiEntity card) {
        if (card.getBehaviour() == CardBehaviour.SELECTED)
            card.setBehaviour(CardBehaviour.IDLE);

        ActivityCallLogic.INSTANCE.removeSelected(card.getId());
        return card;
    }

    public List<CardGuiEntity> removeSelected() {
        var shuffled = shuffled();
        var list = shuffled.getOrDefault(CardBehaviour.SELECTED.getName(), List.of());
        list.forEach(this::removeSelection);
        return list;
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
        if (isOpened()) {
            for (var entry : cards.entrySet()) {
                var card = entry.getValue();
                var input = entry.getKey();

                boolean selected = card.isAlive() && card.mouseSelectedAbsolute(mx, my, false);

                if (selected && button == 0 && action == 1) {
                    return !clientCall(Minecraft.getInstance().player, input);
                }

                if (button == 1 && action == 1 && selected) {
                    select(card);

                    Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.5f);
                    return true;
                }
            }

            return true;
        }

        if (button == 1 && action == 1) {
            var shuffled = shuffled();
            if (!shuffled.containsKey(CardBehaviour.SELECTED.getName()))
                return false;

            clientCall(Minecraft.getInstance().player,
                    shuffled.get(CardBehaviour.SELECTED.getName()).getFirst().getId());
            return true;
        }

        return false;
    }

    public boolean clientCall(Player player, String id) {
        var input = ActivityState.INSTANCE.getInput(id);
        return switch (ActivityCallLogic.INSTANCE.tryCast(player, input, true)) {
            case FAILURE -> {
                ActivityCallGui.INSTANCE.fail(id);
                yield false;
            }
            case SUCCESS -> {
                Network.sendToServer(new ActivityCastPacket(input.setting().getName(), input));
                yield true;
            }
        };
    }

    private void validateOrDestroy(Player player, String input, CardGuiEntity card) {
        if (!ActivityState.INSTANCE.validate(player, input))
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

            card.setCooldown(ActivityState.INSTANCE.getCooldown(key));
            return !card.isAlive();
        });
    }

    public void onActivityCast() {
        ActivityCallGui.INSTANCE.removeSelected();
        ActivityCallGui.startHide(false);
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
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        var window = mc.getWindow();
        double rawX = mc.mouseHandler.xpos();
        double rawY = mc.mouseHandler.ypos();

        double mx = rawX * (double) window.getGuiScaledWidth()  / (double) window.getScreenWidth();
        double my = rawY * (double) window.getGuiScaledHeight() / (double) window.getScreenHeight();

        if (mc.screen == null && ActivityCallGui.INSTANCE.onMouseClick(mx, my, event.getButton(), event.getAction()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onCast(ActivityCallEvent.Post event) {
        ActivityCallGui.INSTANCE.onActivityCast();
    }

    @SubscribeEvent
    public static void tickEvent(ClientTickEvent.Pre event) {
        if (Minecraft.getInstance().player == null) return;

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
            cardGuiEntity.preRender(gui, cx, cy, pt);
        for (CardGuiEntity cardGuiEntity : cards.values()) {
            cardGuiEntity.render(gui, cx, cy, pt);
        }
    }

}
