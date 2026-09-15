package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.api.IRelicDescriptor;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.misc.TextJustificator;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.AbilityDescriptionContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.DescriptionContainerWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbilityDescriptionContainerWidget.class)
public abstract class DescriptionMixin extends DescriptionContainerWidget {

    public DescriptionMixin(DescriptionScreen screen) {
        super(screen);
    }

    @ModifyArgs(
            method = "constructDescriptionData",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lit/hurts/sskirillss/relics/client/screen/description/relic/widgets/AbilityDescriptionContainerWidget$DescriptionData;<init>(Ljava/util/List;Ljava/util/List;)V"
            )
    )
    private void relics_thirteenflames$modifyDescriptionData(Args args) {
        List<TextJustificator.LineEntry> rawLines = new ArrayList<>(args.get(0));
        List<MutableComponent> dynamicComponents = new ArrayList<>(args.get(1));

        Minecraft mc = Minecraft.getInstance();

        Player player = mc.player;
        AbilityDescriptionScreen screen = (AbilityDescriptionScreen) getScreen();
        ItemStack stack = screen.getStack();

        if (stack.getItem() instanceof IRelicDescriptor descriptor)
            descriptor.modifyDescription(player, stack, screen.getSelectedAbility(), rawLines, dynamicComponents);

        args.set(0, rawLines);
        args.set(1, dynamicComponents);
    }
}