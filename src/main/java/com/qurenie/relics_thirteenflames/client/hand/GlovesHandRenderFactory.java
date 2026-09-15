package com.qurenie.relics_thirteenflames.client.hand;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.items.models.MontuGlovesArmorLeft;
import com.qurenie.relics_thirteenflames.content.items.models.MontuGlovesArmorRight;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.util.Lazy;

public class GlovesHandRenderFactory implements RenderableHandRegistry.ModelFactory, RenderableHandRegistry.TextureFactory {

    public static final GlovesHandRenderFactory INSTANCE = new GlovesHandRenderFactory();

    private static final Lazy<HumanoidModel<? extends LivingEntity>> RIGHT = Lazy.lazy(() -> new MontuGlovesArmorRight<>(Minecraft.getInstance().getEntityModels().bakeLayer(MontuGlovesArmorRight.LAYER_LOCATION)));
    private static final Lazy<HumanoidModel<? extends LivingEntity>> RIGHT_FLAWLESS = Lazy.lazy(() -> new MontuGlovesArmorRight.Flawless<>(Minecraft.getInstance().getEntityModels().bakeLayer(MontuGlovesArmorRight.Flawless.LAYER_LOCATION)));
    private static final Lazy<HumanoidModel<? extends LivingEntity>> LEFT = Lazy.lazy(() -> new MontuGlovesArmorLeft<>(Minecraft.getInstance().getEntityModels().bakeLayer(MontuGlovesArmorLeft.LAYER_LOCATION)));
    private static final Lazy<HumanoidModel<? extends LivingEntity>> LEFT_FLAWLESS = Lazy.lazy(() -> new MontuGlovesArmorLeft.Flawless<>(Minecraft.getInstance().getEntityModels().bakeLayer(MontuGlovesArmorLeft.Flawless.LAYER_LOCATION)));
    private static final ResourceLocation TEXTURE = ThirteenFlames.rl("textures/armor/montu_gloves.png");

    @Override
    public HumanoidModel<? extends LivingEntity> getModel(Player player, ItemStack stack, HumanoidArm arm) {
        boolean flawless = ItemsRegistry.MONTU_GLOVES.getRelicData(player, stack).isFlawless();
        return flawless ? arm == HumanoidArm.LEFT ? LEFT_FLAWLESS.get() : RIGHT_FLAWLESS.get() : arm == HumanoidArm.LEFT ? LEFT.get() : RIGHT.get();
    }

    @Override
    public ResourceLocation getTexture(Player player, ItemStack stack, HumanoidArm arm) {
        boolean flawless = ItemsRegistry.MONTU_GLOVES.getRelicData(player, stack).isFlawless();
        return flawless ? getFlawlessLocation(player.tickCount / 2) : TEXTURE;
    }

    private ResourceLocation getFlawlessLocation(int tickCount) {
        return ThirteenFlames.rl(String.format("textures/armor/montu_glove_upgraded%d.png", tickCount % 6 + 1));
    }

}
