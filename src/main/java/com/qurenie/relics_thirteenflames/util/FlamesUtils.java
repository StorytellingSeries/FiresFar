package com.qurenie.relics_thirteenflames.util;

import com.qurenie.relics_thirteenflames.client.render.misc.JodahWingsLayer;
import com.qurenie.relics_thirteenflames.init.AttachmentsRegistry;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.net.AttachmentRemoveSyncPacket;
import com.qurenie.relics_thirteenflames.net.JodahWingsSyncPacket;
import com.qurenie.relics_thirteenflames.net.PlaneshiftSyncPacket;
import com.qurenie.relics_thirteenflames.net.SkintDataAttachmentPacket;
import lombok.experimental.UtilityClass;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammeranims.api.animation.interp.BlendMode;
import org.zeith.hammeranims.api.animsys.AnimationSystem;
import org.zeith.hammeranims.api.animsys.layer.ActiveAnimation;
import org.zeith.hammeranims.api.animsys.layer.AnimationLayer;
import org.zeith.hammerlib.net.Network;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.qurenie.relics_thirteenflames.content.entities.AnimatedEntity.LAYER_ACTION;
import static com.qurenie.relics_thirteenflames.content.entities.AnimatedEntity.LAYER_WALKING;


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
    
    public double getBlockHeightSafety(Level level, BlockPos pos) {
        double height = level.getBlockFloorHeight(pos);
        if (Double.isInfinite(height))
            return 0;
        return height;
    }
    
    public float getCompletion(AnimationSystem system, @Nullable AnimationLayer layer) {
        if (layer == null)
            return 0;
        ActiveAnimation activeAnimation = layer.getCurrentAnimation();
        if (activeAnimation == null)
            return 0;
        return (float) ((system.getTime(0) - activeAnimation.activationTime) * (double) activeAnimation.config.speed);
    }
    
    public Color spreadColor(Color color, RandomSource random) {
        return spreadColor(color, random, 30);
    }
    
    public Color spreadColor(Color color, RandomSource random, int range) {
        return new Color((int) Math.clamp(0, color.getRed() - range * 0.5 + random.nextInt(range), 255),
                (int) Math.clamp(0, color.getGreen() - range * 0.5 + random.nextInt(range), 255),
                (int) Math.clamp(0, color.getBlue() - range * 0.5 + random.nextInt(range), 255));
    }
    
    public static void tickAttachment(Entity entity, Supplier<AttachmentType<Integer>> type) {
        tickAttachment(entity, type, e -> {});
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
        player.setData(AttachmentsRegistry.PLANESHIFT_TICK, 20 * (int) ItemsRegistry.JODAH_MASK.getStatValue(stack, "planeshift", "durability"));
        
        if (sync)
            Net.startTrackingPlaneshift(player);
    }
    
    public static void startJodahWings(Player player, boolean sync) {
        player.setData(AttachmentsRegistry.WINGS_LAYER_DATA, JodahWingsLayer.ANIMATION_LENGTH);
        
        if (sync)
            Net.startTrackingWingsAttachments(player);
    }
    
    public static void addAntiskint(Entity entity, int count, int maxBonus) {
        int max = 5 + maxBonus;
        
        if (!entity.hasData(AttachmentsRegistry.ANTISKINT_DATA))
            entity.setData(AttachmentsRegistry.ANTISKINT_DATA, 0);
        
        int prev = entity.getData(AttachmentsRegistry.ANTISKINT_DATA);
        int skint = Mth.clamp(count + prev, 0, Math.max(count, max));
        entity.setData(AttachmentsRegistry.ANTISKINT_DATA, skint);
        
        if (entity instanceof LivingEntity living) {
            living.removeEffect(EffectsRegistry.SKINTONIT_EFFECT);
            if (skint > 0)
                living.addEffect(new MobEffectInstance(EffectsRegistry.SKINTONIT_EFFECT, 1200, skint - 1, false, false));
        }
        
        if (skint != prev)
            Net.startTrackingSkintAttachments(entity);
    }
    
    public static void addAntiskint(@Nullable ItemStack stack, Entity entity, int count) {
        addAntiskint(entity, count, stack == null || !ItemsRegistry.JODAH_MASK.isAbilityUnlocked(stack, "reversal_aberration") ? 0 : (int) ItemsRegistry.JODAH_MASK.getStatValue(stack, "reversal_aberration", "skint_bonus"));
    }
    
    public static void addSkint(Entity entity, int count, int maxBonus) {
        int max = 5 + maxBonus;
        
        if (!entity.hasData(AttachmentsRegistry.SKINT_DATA))
            entity.setData(AttachmentsRegistry.SKINT_DATA, 0);
        
        int prev = entity.getData(AttachmentsRegistry.SKINT_DATA);
        int skint = Mth.clamp(count + prev, 0, Math.max(count, max));
        entity.setData(AttachmentsRegistry.SKINT_DATA, skint);
        
        if (entity instanceof LivingEntity living) {
            living.removeEffect(EffectsRegistry.SKINT_EFFECT);
            if (skint > 0)
                living.addEffect(new MobEffectInstance(EffectsRegistry.SKINT_EFFECT, 1200, skint - 1, false, false));
        }
        
        if (skint != prev)
            Net.startTrackingSkintAttachments(entity);
    }
    
    public static void addSkint(@Nullable ItemStack stack, Entity entity, int count) {
        addSkint(entity, count, stack == null ? 0 : (int) ItemsRegistry.JODAH_MASK.getStatValue(stack, "reversal_aberration", "skint_bonus"));
    }
    
    public static class Net {
        
        public static void startTrackingSkintAttachments(Entity entity) {
            Network.sendToTrackingAndSelf(entity, new SkintDataAttachmentPacket(
                    entity.getData(AttachmentsRegistry.SKINT_DATA),
                    entity.getData(AttachmentsRegistry.ANTISKINT_DATA),
                    entity.getId()
            ));
        }
        
        public static void startTrackingWingsAttachments(Entity entity) {
            Network.sendToTrackingAndSelf(entity, new JodahWingsSyncPacket(
                    entity.getData(AttachmentsRegistry.WINGS_LAYER_DATA),
                    entity.getId()
            ));
        }
        
        public static void startTrackingPlaneshift(Entity entity) {
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
