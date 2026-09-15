package com.qurenie.relics_thirteenflames.activity.call;

import com.qurenie.api.ActivityCallEvent;
import com.qurenie.relics_thirteenflames.activity.call.settings.ActivityResult;
import com.qurenie.relics_thirteenflames.activity.call.settings.SelectionContext;
import com.qurenie.relics_thirteenflames.data.ActivityState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

public class ActivityCallLogic {

    public static final ActivityCallLogic INSTANCE = new ActivityCallLogic();

    public void selectClient(String id, @Nullable String prevOne) {
        var call = ActivityState.getState().get(id);
        var prevCall = Optional.ofNullable(prevOne).map(s -> ActivityState.getState().get(s)).orElse(null);
        SelectionContext selectionContext = new SelectionContext(false, prevCall);
        call.call().selectionNotify(Minecraft.getInstance().player, call.stack(), selectionContext);
    }

    public void removeSelected(String id) {
        var call = ActivityState.getState().get(id);
        SelectionContext selectionContext = new SelectionContext(true, null);
        call.call().selectionNotify(Minecraft.getInstance().player, call.stack(), selectionContext);
    }

    public boolean serverCall(Player player, CallInput id) {
        return tryCast(player, id, false) != ActivityResult.FAILURE;
    }

    public ActivityResult tryCast(Player player, CallInput input, boolean clientSide) {
        var stack = input.stack();
        ActivityCallEvent.Cast castEvent = new ActivityCallEvent.Cast(player, input.stack(), input.setting(), clientSide,
                input.container().canCast(player, stack, input.setting().getName()));
        EVENT_BUS.post(castEvent);

        ActivityResult result = (castEvent.isCanCast() && !castEvent.isCanceled())
                ? input.call().cast(player, stack)
                : ActivityResult.FAILURE;

        if (result == ActivityResult.SUCCESS)
            EVENT_BUS.post(new ActivityCallEvent.Post(player, input.stack(), input.setting(), clientSide));

        return result;
    }

}
