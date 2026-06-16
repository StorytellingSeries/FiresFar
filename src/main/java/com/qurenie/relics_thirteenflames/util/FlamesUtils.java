package com.qurenie.relics_thirteenflames.util;

import com.qurenie.api.JodahMaskEvent;
import com.qurenie.relics_thirteenflames.client.render.misc.JodahWingsLayer;
import com.qurenie.relics_thirteenflames.init.AttachmentsRegistry;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.net.*;
import it.hurts.octostudios.octolib.util.OctoColor;
import lombok.experimental.UtilityClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammeranims.api.animation.interp.BlendMode;
import org.zeith.hammeranims.api.animsys.AnimationSystem;
import org.zeith.hammeranims.api.animsys.layer.ActiveAnimation;
import org.zeith.hammeranims.api.animsys.layer.AnimationLayer;
import org.zeith.hammerlib.net.Network;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.qurenie.relics_thirteenflames.content.entities.AnimatedEntity.LAYER_ACTION;
import static com.qurenie.relics_thirteenflames.content.entities.AnimatedEntity.LAYER_WALKING;
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;


@UtilityClass
public class FlamesUtils {

    public static void setupAnimationSystem(AnimationSystem.Builder builder) {
        LayersList list = new LayersList();

        list.addLast(LAYER_ACTION, BlendMode.OVERRIDE, 1.0F);
        list.addLast(LAYER_WALKING, BlendMode.ADD, 1.0F);

        int xtraLayerCount = 5;

        for (int i = 0; i < xtraLayerCount; i++) {
            list.addLast("ANIMATION_" + i, BlendMode.ADD, 1.0F);
        }

        builder.addLayers(list.getLayers().toArray(AnimationLayer.Builder[]::new));
        builder.autoSync(true);
    }

    public static final UUID UUID_EMPTY = UUID.randomUUID();

    public static void addModifier(ItemStack stack, Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
        var default$ = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, stack.getItem().getDefaultAttributeModifiers(stack));
        default$.withModifierAdded(attribute, modifier, slot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, default$);
    }

    @NotNull
    public static UUID getOrCreateUUID(ItemStack stack) {
        if (!stack.has(ComponentRegistry.UNIQUE_UUID))
            stack.set(ComponentRegistry.UNIQUE_UUID, UUID.randomUUID());

        return stack.getOrDefault(ComponentRegistry.UNIQUE_UUID, UUID_EMPTY);
    }

    public boolean sameUUID(ItemStack stack1, ItemStack stack2) {
        return getOrCreateUUID(stack1).equals(getOrCreateUUID(stack2));
    }

    public static void gainJodahShield(LivingEntity player, float shield) {
        AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);

        if (attr != null) {
            double maxHp = attr.getValue();
            float maxShield = (float) (maxHp * 2);

            float existingShield = player.getData(AttachmentsRegistry.JODAH_SHEILD);
            player.setData(AttachmentsRegistry.JODAH_SHEILD, Mth.clamp(existingShield + shield, 0, maxShield));
        }

        Net.sendShieldAttachment(player);
    }

    public static Vec2 randomCirclePoint(float radius, boolean full) {
        float angle = (float) (Math.PI * 2 * Math.random());
        float x = Mth.cos(angle);
        float y = Mth.sin(angle);
        return new Vec2(x, y).scale(full ? (float) (Math.random() * radius) : radius);
    }

    public static double getBlockHeightSafety(Level level, BlockPos pos) {
        double height = level.getBlockFloorHeight(pos);
        if (Double.isInfinite(height))
            return 0;
        return height;
    }

    public static boolean isSoulBlock(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.SOUL_SAND);
    }

    public static float getCompletion(AnimationSystem system, @Nullable AnimationLayer layer) {
        if (layer == null)
            return 0;
        ActiveAnimation activeAnimation = layer.getCurrentAnimation();
        if (activeAnimation == null)
            return 0;
        return (float) ((system.getTime(0) - activeAnimation.activationTime) * (double) activeAnimation.config.speed);
    }

    public OctoColor spreadColor(OctoColor color, RandomSource random) {
        return spreadColor(color, random, 30 / 255f);
    }

    public OctoColor spreadColor(OctoColor color, RandomSource random, float range) {
        return new OctoColor((float) Math.clamp(color.r() - range * 0.5 + random.nextFloat() * range, 0, 1f),
                (float) Math.clamp(color.g() - range * 0.5 + random.nextFloat() * range, 0, 1f),
                (float) Math.clamp(color.b() - range * 0.5 + random.nextFloat() * range, 0, 1f), 1);
    }

    public static void tickAttachment(Entity entity, Supplier<AttachmentType<Integer>> type) {
        tickAttachment(entity, type, e -> {
        });
    }

    public static void tickAttachment(Entity entity, Supplier<AttachmentType<Integer>> type, Consumer<Entity> afterwards) {
        if (entity.hasData(type)) {
            int activeTicks = entity.getData(type);
            if (activeTicks == 0) {
                entity.removeData(type);
                afterwards.accept(entity);
            } else
                entity.setData(type, activeTicks - 1);
        }
    }

    public static void startPlaneShift(ItemStack stack, Player player, boolean sync) {
        player.setData(AttachmentsRegistry.PLANESHIFT_TICK, 20 * (int) ItemsRegistry.JODAH_MASK.getStatValue(player, stack, "planeshift", "durability"));

        if (sync)
            Net.syncPlaneshift(player);
    }

    public static void startJodahWings(Player player, boolean sync) {
        player.setData(AttachmentsRegistry.WINGS_LAYER_DATA, JodahWingsLayer.ANIMATION_LENGTH);

        if (sync)
            Net.sendWingsAttachment(player);
    }

    public static OctoColor fromRGBI(int r, int g, int b) {
        return new OctoColor(r / 255f, g / 255f, b / 255f, 1f);
    }

    public static int getRGBA(OctoColor color) {
        int a = (color.getARGB() >>> 24) & 0xFF;
        return ((color.getARGB() & 0xFFFFFF) << 8) | a;
    }

    public static OctoColor withAlpha(OctoColor color, float alpha) {
        return new OctoColor(color.r(), color.b(), color.g(), alpha);
    }

    public static int getRGB(OctoColor color) {
        return (color.getARGB() & 0xFFFFFF);
    }

    public static void addAntiskint(Entity entity, int count, int maxBonus) {
        if (count == 0)
            return;

        int max = 5 + maxBonus;

        var event = new JodahMaskEvent.AddAntiscint(entity, count, max);
        EVENT_BUS.post(event);

        max = event.getMaxValue();
        count = event.getValue();

        if (!entity.hasData(AttachmentsRegistry.ANTISKINT_DATA))
            entity.setData(AttachmentsRegistry.ANTISKINT_DATA, 0);

        int prev = entity.getData(AttachmentsRegistry.ANTISKINT_DATA);
        int skint = Mth.clamp(count + prev, 0, Math.max(count, max));
        entity.setData(AttachmentsRegistry.ANTISKINT_DATA, skint);

        if (entity instanceof LivingEntity living) {
            if (count > 0)
                living.forceAddEffect(new MobEffectInstance(EffectsRegistry.SKINTONIT_EFFECT, 1200, count - 1, false, false, true), null);
            else
                living.removeEffect(EffectsRegistry.SKINTONIT_EFFECT);
        }

        if (skint != prev)
            Net.sendSkintAttachment(entity);
    }

    public static void addAntiskint(@Nullable ItemStack stack, LivingEntity owner, Entity entity, int count) {
        addAntiskint(entity, count, stack == null || !ItemsRegistry.JODAH_MASK.isAbilityUnlocked(owner, stack, "reversal_aberration") ? 0 : (int) ItemsRegistry.JODAH_MASK.getStatValue(owner, stack, "reversal_aberration", "skint_bonus"));
    }

    public static void addSkint(Entity entity, int count, int maxBonus) {
        if (count == 0)
            return;

        int max = 5 + maxBonus;

        var event = new JodahMaskEvent.AddScint(entity, count, max);
        EVENT_BUS.post(event);

        max = event.getMaxValue();
        count = event.getValue();

        if (!entity.hasData(AttachmentsRegistry.SKINT_DATA))
            entity.setData(AttachmentsRegistry.SKINT_DATA, 0);

        int prev = entity.getData(AttachmentsRegistry.SKINT_DATA);
        int skint = Mth.clamp(count + prev, 0, Math.max(count, max));
        entity.setData(AttachmentsRegistry.SKINT_DATA, skint);

        if (entity instanceof LivingEntity living) {
            if (skint > 0)
                living.forceAddEffect(new MobEffectInstance(EffectsRegistry.SKINT_EFFECT, 1200, skint - 1, false, false, true), null);
            else
                living.removeEffect(EffectsRegistry.SKINT_EFFECT);
        }

        if (skint != prev)
            Net.sendSkintAttachment(entity);
    }

    public static void addSkint(@Nullable ItemStack stack, LivingEntity owner, Entity entity, int count) {
        addSkint(entity, count, stack == null ? 0 : (int) ItemsRegistry.JODAH_MASK.getStatValue(owner, stack, "reversal_aberration", "skint_bonus"));
    }

    public static void setAntikint(Entity entity, int count, boolean addEffect) {
        if (!entity.hasData(AttachmentsRegistry.ANTISKINT_DATA) && count != 0)
            entity.setData(AttachmentsRegistry.ANTISKINT_DATA, 0);

        entity.setData(AttachmentsRegistry.ANTISKINT_DATA, count);

        if (addEffect && entity instanceof LivingEntity living) {
            if (count > 0)
                living.forceAddEffect(new MobEffectInstance(EffectsRegistry.SKINTONIT_EFFECT, 1200, count - 1, false, false, true), null);
            else
                living.removeEffect(EffectsRegistry.SKINTONIT_EFFECT);
        }

        Net.sendSkintAttachment(entity);
    }

    public static void setSkint(Entity entity, int count, boolean addEffect) {
        if (!entity.hasData(AttachmentsRegistry.SKINT_DATA) && count != 0)
            entity.setData(AttachmentsRegistry.SKINT_DATA, 0);

        entity.setData(AttachmentsRegistry.SKINT_DATA, count);

        if (addEffect && entity instanceof LivingEntity living) {
            if (count > 0)
                living.forceAddEffect(new MobEffectInstance(EffectsRegistry.SKINT_EFFECT, 1200, count - 1, false, false, true), null);
            else
                living.removeEffect(EffectsRegistry.SKINT_EFFECT);
        }

        Net.sendSkintAttachment(entity);
    }

    public static boolean isWeaponOrMiningEnchantment(HolderSet<Item> holders) {
        return holders.unwrapKey().map(tag -> tag == ItemTags.SWORD_ENCHANTABLE || tag == ItemTags.MINING_ENCHANTABLE ||
                tag == ItemTags.MINING_LOOT_ENCHANTABLE).orElse(false);
    }

    public static boolean isMiningEnchantment(HolderSet<Item> holders) {
        return holders.unwrapKey().map(tag -> tag == ItemTags.MINING_ENCHANTABLE ||
                tag == ItemTags.MINING_LOOT_ENCHANTABLE).orElse(false);
    }

    public static boolean isWeaponEnchantment(HolderSet<Item> holders) {
        return holders.unwrapKey().map(tag -> tag == ItemTags.SWORD_ENCHANTABLE).orElse(false);
    }

    public static class Net {

        public static void sendShieldAttachment(Entity entity) {
            Network.sendToTrackingAndSelf(entity, new JodahShieldSyncPacket(
                    entity.getData(AttachmentsRegistry.JODAH_SHEILD),
                    entity.getId()
            ));
        }

        public static void sendSkintAttachment(Entity entity) {
            Network.sendToTrackingAndSelf(entity, new SkintDataAttachmentPacket(
                    entity.getData(AttachmentsRegistry.SKINT_DATA),
                    entity.getData(AttachmentsRegistry.ANTISKINT_DATA),
                    entity.getId()
            ));
        }

        public static void sendWingsAttachment(Entity entity) {
            Network.sendToTrackingAndSelf(entity, new JodahWingsSyncPacket(
                    entity.getData(AttachmentsRegistry.WINGS_LAYER_DATA),
                    entity.getId()
            ));
        }

        public static void syncPlaneshift(Entity entity) {
            Network.sendToTrackingAndSelf(entity, new PlaneshiftSyncPacket(
                    entity.getData(AttachmentsRegistry.PLANESHIFT_TICK),
                    entity.getId()
            ));
        }

        public static void syncAttachmentRemove(Entity entity, Supplier<AttachmentType<?>> type) {
            Network.sendToTrackingAndSelf(entity, new AttachmentRemoveSyncPacket(type.get(), entity.getId()));
        }

    }

}
