package com.qurenie.relics_thirteenflames.activity.call;

import com.qurenie.api.IActivityContainer;
import com.qurenie.relics_thirteenflames.activity.call.settings.ActivityResult;
import com.qurenie.relics_thirteenflames.activity.call.settings.InventoryType;
import com.qurenie.relics_thirteenflames.client.gui.ActivityCallGui;
import com.qurenie.relics_thirteenflames.data.ActivityState;
import com.qurenie.relics_thirteenflames.net.ActivityCastPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.zeith.hammerlib.net.Network;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ActivityCallLogic {

    public static final ActivityCallLogic INSTANCE = new ActivityCallLogic();

    private CallInput getInput(String id) {
        return ActivityState.getState().get(id);
    }

    public int getCooldown(String id) {
        Minecraft mc = Minecraft.getInstance();

        var input = getInput(id);
        return input.container().getCooldown(mc.player, input.reference().getStack(mc.player), input.setting().getName());
    }

    public void clientCall(Player player, String id) {
        var input = getInput(id);
        switch (tryCast(player, input)) {
            case FAILURE -> ActivityCallGui.INSTANCE.fail(id);
            case SUCCESS -> {
                ActivityCallGui.INSTANCE.removeSelected();
                ActivityCallGui.startHide(false);
            }
        }
        Network.sendToServer(new ActivityCastPacket(input.setting().getName(), input));
    }

    public void serverCall(Player player, CallInput id) {
        tryCast(player, id);
    }

    private ActivityResult tryCast(Player player, CallInput input) {
        var stack = input.stack();
        if (input.container().canCast(player, stack, input.setting().getName()))
            return input.call().cast(player, stack);

        return ActivityResult.FAILURE;
    }

    public boolean validate(Player player, String id) {
        return getInput(id).validateAndCorrectReference(player);
    }

    public void cacheActivities(Player player) {
        LinkedHashMap<String, CallInput> map = new LinkedHashMap<>();
        ActivityCallLogic.INSTANCE.getValidItems(player).stream()
                .filter(c -> c.call().isVisible(player, c.stack()))
                .forEach(c -> map.put(c.getId(), c));
        ActivityState.cache(map);
    }

    public List<CallInput> getValidItems(Player player) {
        List<CallInput> result = new ArrayList<>();

        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack stack = player.getInventory().armor.get(i);

            if (!stack.isEmpty() && stack.getItem() instanceof IActivityContainer container) {
                for (var settings : container.getActivitySettings().getValues().values()) {
                    var call = settings.getCallSettings();

                    if (call != null && (call.getInventoryType() == InventoryType.ARMOR
                            || call.getInventoryType() == InventoryType.INVENTORY))
                        result.add(new CallInput(stack, settings, call, new InventoryType.ArmorSlotReference(i)));
                }
            }
        }

        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);

            if (!stack.isEmpty() && stack.getItem() instanceof IActivityContainer container) {
                for (var settings : container.getActivitySettings().getValues().values()) {
                    var call = settings.getCallSettings();

                    if (call != null && checkInventoryType(stack, player, call.getInventoryType()))
                        result.add(new CallInput(stack, settings, call, new InventoryType.InventorySlotReference(i)));
                }

            }
        }

        CuriosApi.getCuriosInventory(player).ifPresent((handler) -> {
            for (var entry : handler.getCurios().entrySet()) {
                var stacks = entry.getValue().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof IActivityContainer container) {
                        for (var settings : container.getActivitySettings().getValues().values()) {
                            var call = settings.getCallSettings();

                            if (call != null && call.getInventoryType() == InventoryType.CURIO)
                                result.add(new CallInput(stack, settings, call, new InventoryType.CurioSlotReference(entry.getKey(), i)));
                        }

                    }
                }
            }
        });

        return result;
    }

    private static boolean checkInventoryType(ItemStack stack, Player player, InventoryType type) {
        return switch (type) {
            case INVENTORY -> true;
            case IN_HAND -> player.getMainHandItem() == stack || player.getOffhandItem() == stack;
            default -> false;
        };
    }

}
