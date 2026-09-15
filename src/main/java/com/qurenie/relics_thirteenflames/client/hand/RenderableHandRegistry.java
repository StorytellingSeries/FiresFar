package com.qurenie.relics_thirteenflames.client.hand;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@OnlyIn(Dist.CLIENT)
public class RenderableHandRegistry {

    private static final Map<Item, ModelFactory> MODEL_REGISTRY = new HashMap<>();
    private static final Map<Item, TextureFactory> TEXTURE_REGISTRY = new HashMap<>();

    public static void register(Item item, ModelFactory modelGetter, TextureFactory resourceLocation) {
        MODEL_REGISTRY.put(item, modelGetter);
        TEXTURE_REGISTRY.put(item, resourceLocation);
    }

    public static boolean has(Item item) {
        return MODEL_REGISTRY.containsKey(item);
    }

    public static void calculateIfPresent(Player player, ItemStack item, HumanoidArm arm, BiConsumer<HumanoidModel<?>, ResourceLocation> ifPresent) {
        Optional.ofNullable(MODEL_REGISTRY.get(item.getItem())).ifPresent(model ->
                ifPresent.accept(model.getModel(player, item, arm), TEXTURE_REGISTRY.get(item.getItem()).getTexture(player, item, arm)));
    }

    public static HumanoidModel<?> getModel(Player player, ItemStack stack, HumanoidArm arm) {
        return MODEL_REGISTRY.get(stack.getItem()).getModel(player, stack, arm);
    }

    public static ResourceLocation getTexture(Player player, ItemStack stack, HumanoidArm arm) {
        return TEXTURE_REGISTRY.get(stack.getItem()).getTexture(player, stack, arm);
    }

    public interface ModelFactory {

        HumanoidModel<?> getModel(Player player, ItemStack stack, HumanoidArm arm);

    }

    public interface TextureFactory {

        ResourceLocation getTexture(Player player, ItemStack stack, HumanoidArm arm);

    }

}
