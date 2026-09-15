package com.qurenie.relics_thirteenflames.data;

import com.qurenie.api.ActivityCallEvent;
import com.qurenie.api.IActivityContainer;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.activity.call.CallInput;
import com.qurenie.relics_thirteenflames.activity.call.settings.InventoryType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;


@EventBusSubscriber(modid = ThirteenFlames.MODID, value = Dist.CLIENT)
public class ActivityState {

    private static final LinkedHashMap<String, CallInput> CACHE = new LinkedHashMap<>();
    public static final int OPEN_DELAY = 5;

    // Only Client Side??
    public static final ActivityState INSTANCE =  new ActivityState();

    public static void cache(LinkedHashMap<String, CallInput> map) {
        CACHE.clear();
        CACHE.putAll(map);
    }

    public int getCooldown(String id) {
        Minecraft mc = Minecraft.getInstance();

        var input = getInput(id);
        return input.container().getCooldown(mc.player, input.reference().getStack(mc.player), input.setting().getName());
    }

    public static void cache(String id, CallInput input) {
        CACHE.put(id, input);
    }

    public static Collection<CallInput> getInputs() {
        return CACHE.values();
    }

    public static Set<String> getKeys() {
        return CACHE.keySet();
    }

    public static LinkedHashMap<String, CallInput> getState() {
        return CACHE;
    }

    public CallInput getInput(String id) {
        return ActivityState.getState().get(id);
    }

    public void cacheActivities(Player player) {
        LinkedHashMap<String, CallInput> map = new LinkedHashMap<>();
        getValidItems(player).stream()
                .filter(c -> isVisible(c, player))
                .forEach(c -> map.put(c.getId(), c));
        ActivityState.cache(map);
    }

    public boolean validate(Player player, String id) {
        var input = getInput(id);
        return input.validateAndCorrectReference(player) && isVisible(input, player);
    }

    private boolean isVisible(CallInput input, Player player) {
        ActivityCallEvent.Visible event = new ActivityCallEvent.Visible(player, input.stack(), input.setting(), input.call().isVisible(player, input.stack()));
        EVENT_BUS.post(event);
        return event.isVisible();
    }

    private static boolean checkInventoryType(ItemStack stack, Player player, InventoryType type) {
        return switch (type) {
            case INVENTORY -> true;
            case IN_HAND -> player.getMainHandItem() == stack || player.getOffhandItem() == stack;
            default -> false;
        };
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

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            CACHE.clear();
        }
    }

}
