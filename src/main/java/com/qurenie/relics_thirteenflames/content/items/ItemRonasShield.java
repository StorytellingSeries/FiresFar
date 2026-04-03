package com.qurenie.relics_thirteenflames.content.items;

import com.mojang.blaze3d.platform.InputConstants;
import com.qurenie.api.IActivityContainer;
import com.qurenie.api.IExtRelicItem;
import com.qurenie.api.SettingsContainer;
import com.qurenie.relics_thirteenflames.activity.ActivitySetting;
import com.qurenie.relics_thirteenflames.activity.IActivitySetting;
import com.qurenie.relics_thirteenflames.client.bar.BarSetting;
import com.qurenie.relics_thirteenflames.client.bar.IBarSetting;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import com.qurenie.relics_thirteenflames.net.RhonasRebukePacket;
import com.qurenie.relics_thirteenflames.style.ColorScheme;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsHotkeys;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.zeith.hammerlib.api.items.IColoredFoilItem;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.net.PacketContext;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.*;

@EventBusSubscriber
public class ItemRonasShield extends ShieldItem implements IExtRelicItem, IColoredFoilItem, IRelicItem, IActivityContainer {
    
    private static final Random RNG = new Random();
    
    public ItemRonasShield(Properties properties) {
        super(properties);
    }
    
    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        ItemStack stack = event.getEntity().getItemInHand(event.getEntity().getUsedItemHand());
        if (stack.getItem() instanceof ItemRonasShield shit && event.getEntity().isUsingItem()) {
            
            float blockRate = (float) shit.getStatValue(event.getEntity(), stack, "block", "blockrate");
            float blockedDmg = event.getOriginalBlockedDamage() * blockRate;
            float hungerDmg = (float) shit.getStatValue(event.getEntity(), stack, "block", "hungerdmg");
            if (event.getEntity() instanceof Player p) {
                p.causeFoodExhaustion(blockedDmg * hungerDmg);
            }
            
            double chargeRate = shit.getStatValue(event.getEntity(), stack, "rebuke", "chargerate");
            int maxCharge = (int) shit.getStatValue(event.getEntity(), stack, "rebuke", "maxcharge");
            
            float charge = stack.getOrDefault(RHONAS_BLOCKED, 0f);
            
            int chargesToPut = stack.getOrDefault(RHONAS_CHARGES, 0);
            
            shit.addExperience(event.getEntity(), stack, (int) Math.min(blockedDmg, 15));
            
            
            charge += blockedDmg;
            
            if (charge > chargeRate) {
                chargesToPut = Math.min(chargesToPut + (int) (charge / chargeRate), maxCharge);
                charge %= (float) chargeRate;
                stack.set(RHONAS_CHARGES, chargesToPut);
            }
            
            stack.set(RHONAS_BLOCKED, charge);
            
            if (blockRate < 1)
                event.getEntity().level().playSound(null, event.getEntity().blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.9f, 1.0f + RNG.nextFloat(0.3f));
            
            event.setBlockedDamage(blockedDmg);
            event.setShieldDamage(0);
            
        }
    }
    
    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        if (event.getEntity().getUseItem().getItem() instanceof ItemRonasShield shit) {
            float blockRate = (float) shit.getStatValue(event.getEntity(), event.getEntity().getUseItem(), "block", "blockrate");
            event.setStrength(event.getStrength() * (1 - blockRate));
        }
    }
    
    @Override
    public boolean canPerformAction(@NotNull ItemStack stack, @NotNull ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_SHIELD_ACTIONS.contains(itemAbility);
    }

    @Override
    public SettingsContainer<IActivitySetting> constructActivitySettings() {
        return SettingsContainer.<IActivitySetting>builder()
                .setting(ActivitySetting.builder("rebuke")
                        .maxCooldown((s, p) -> 400)
                        .color(ColorScheme.BAR_YELLOW)
                        .build())
                .setting(ActivitySetting.builder("charge")
                        .maxCooldown((s, p) -> 400)
                        .color(ColorScheme.BAR_RED)
                        .build())
                .build();
    }

    @Override
    public SettingsContainer<IBarSetting> constructBarSettings() {
        return SettingsContainer.<IBarSetting>builder()
                .setting(BarSetting.builder()
                        .color(ColorScheme.BAR_YELLOW)
                        .maxValue((s, p) -> getStatValue(p, s, "rebuke", "maxcharge"))
                        .value((s, p) -> Double.valueOf(s.getOrDefault(ComponentRegistry.RHONAS_CHARGES, 0)))
                        .build())
                .build();
    }

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("block")
                                .initialMaxLevel(8)
                                .stat(AbilityStatTemplate.builder("blockrate")
                                        .initialValue(0.6, 0.64)
                                        .thresholdValue(0, 1)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0.05)
                                        .formatValue(x -> (int) MathUtils.round(x * 100, 0))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("speed")
                                        .initialValue(0.2, 0.2)
                                        .thresholdValue(0, 1)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0.075)
                                        .formatValue(x -> (int) MathUtils.round((1 - x) * 100, 0))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("hungerdmg")
                                        .initialValue(1.8, 1.6)
                                        .thresholdValue(0, 10)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), -0.1)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityTemplate.builder("rebuke")
                                .initialMaxLevel(2)
                                .stat(AbilityStatTemplate.builder("maxcharge")
                                        .initialValue(1, 1)
                                        .thresholdValue(1, 3)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 1)
                                        .formatValue(x -> (int) MathUtils.round(x, 0))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("chargerate")
                                        .initialValue(8.0, 7.0)
                                        .thresholdValue(1.0, 20.0)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), -1)
                                        .formatValue(x -> (int) MathUtils.round(x, 0))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("dmg")
                                        .initialValue(7, 8)
                                        .thresholdValue(0, 40)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 4)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityTemplate.builder("charge")
                                .initialMaxLevel(5)
                                .stat(AbilityStatTemplate.builder("dur")
                                        .initialValue(10, 20)
                                        .thresholdValue(1, 120)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 12)
                                        .formatValue(x -> MathUtils.round(x / 20, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("dmg")
                                        .initialValue(4, 5)
                                        .thresholdValue(0, 10)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 1)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("stunduration")
                                        .initialValue(10, 20)
                                        .thresholdValue(0, 80)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 8)
                                        .formatValue(x -> MathUtils.round(x / 20, 1))
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .leveling(LevelingTemplate.builder()
                        .maxRank(2)
                        .step(15)
                        .initialCost(100)
                        .build())
                .loot(LootTemplate.builder().entry(LootEntries.DESERT).build())
                .build();
    }

    private boolean isCtrlPressed() {
        Minecraft minecraft = Minecraft.getInstance();
        // На клиенте GLFW‑модификаторы доступны здесь
        long window = minecraft.getWindow().getWindow();
        if (window == 0) return false;

        // Проверяем, нажат ли Ctrl (или другой модификатор, если нужно)
        boolean isCtrl = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;

        return isCtrl;
    }
    
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, List<Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("tooltip.relics_thirteenflames.ronas_shield.lore").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
        
        super.appendHoverText(stack, context, tooltip, isAdvanced);
        
        tooltip.add(Component.literal(" "));
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen) {
            tooltip.add(Component.translatable("tooltip.relics.researching.info", RelicsHotkeys.RESEARCH_RELIC.getKey().getDisplayName()).withStyle(ChatFormatting.GRAY));
        }
        
        tooltip.add(Component.literal(" "));
    }
    
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }
    
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (entity instanceof LivingEntity l && stack.getOrDefault(BLOCKED, false) != (l.getUseItem() == stack))
            stack.set(BLOCKED, l.getUseItem() == stack);

    }
    
    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int slot) {
        if (living instanceof Player p && p.getFoodData().getFoodLevel() < 2) p.stopUsingItem();
        int chargingTicker = stack.getOrDefault(CHARGING_TICKER, 0);
        if (chargingTicker > 0 && living instanceof Player player) {
            
            if (living.tickCount % 3 == 0)
                player.level().playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.15f + RNG.nextFloat(0.2f), 0.05f + RNG.nextFloat(0.15f));
            
            Vec3 push = Vec3.directionFromRotation(0, player.getYHeadRot()).scale(0.6);
            player.setDeltaMovement(player.getDeltaMovement().add(push).scale(push.length()).x, player.getDeltaMovement().y, player.getDeltaMovement().add(push).scale(push.length()).z);
            
            double x, y, z;
            
            double a = 0.95;
            double b = 1.4;
            double c = 1.4;
            
            Vec3 xVec = player.getLookAngle();
            Vec3 zVec = Vec3.directionFromRotation(0, player.getYHeadRot()).cross(new Vec3(0, 1, 0));
            Vec3 yVec = xVec.cross(zVec);
            for (int t = 0; t < 360; t += 18 + RNG.nextInt(7)) {
                for (int f = 0; f < 360; f += 18 + RNG.nextInt(7)) {
                    x = a * Math.sin(t * Mth.DEG_TO_RAD) * Math.cos(f * Mth.DEG_TO_RAD) * RNG.nextFloat(0.95f, 1);
                    if (x > 0.1 && x < 0.82) {
                        y = b * Math.sin(t * Mth.DEG_TO_RAD) * Math.sin(f * Mth.DEG_TO_RAD) * RNG.nextFloat(0.95f, 1);
                        z = c * Math.cos(t * Mth.DEG_TO_RAD) * RNG.nextFloat(0.95f, 1);
                        
                        Vec3 startPos = player.getEyePosition().add(0, -0.3, 0);
                        
                        Vec3 initialMove = xVec.scale(x).add(yVec.scale(y)).add(zVec.scale(z)).subtract(xVec.scale(0.94));
                        
                        Vec3 move = initialMove.normalize().scale(Mth.clamp(0.4 * RNG.nextFloat() / initialMove.length(), 0.02, 0.27)).subtract(xVec.scale(0.41 - x * 0.5));
                        
                        ParticleHelper.spawnDirectedParticle(player.level(), ParticleHelper.constructSimpleSpark(new Color(255, RNG.nextInt(80 - 10), 0),
                                (float) (0.1f + RNG.nextFloat(0.1f) + x / 4f), 10, 0.85F), startPos.add(xVec.scale(x + 0.8)).add(yVec.scale(y)).add(zVec.scale(z)), move);
                        
                    }
                }
            }
            
            AABB aoe = player.getBoundingBox().inflate(2, 0.3, 2);
            for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, aoe, e -> !e.getUUID().equals(player.getUUID()))) {
                float damag = (float) (getStatValue(living, stack, "charge", "dmg") + (getStatValue(living, stack, "charge", "dur") - chargingTicker) / 8);
                if (target.hurt(player.damageSources().playerAttack(player), damag)) {
                    Vec3 awayctor = target.position().subtract(player.position()).subtract(player.getDeltaMovement()).normalize().scale(2);
                    target.push(awayctor.x(), awayctor.y() + 1, awayctor.z());
                    target.addEffect(new MobEffectInstance(RelicsMobEffects.STUN, (int) getStatValue(living, stack, "charge", "stunduration"), 0));
                    this.addExperience(player, stack, 1);
                }
            }
            
            if (player.tickCount % 10 == 0) stack.set(CHARGING_TICKER, Math.max(chargingTicker - 10, 0));
        }
        super.onUseTick(level, living, stack, slot);
    }
    
    @Override
    public void onStopUsing(@NotNull ItemStack stack, @NotNull LivingEntity entity, int count) {
        
        stack.set(CHARGING_TICKER, 0);
        EntityUtils.removeAttribute(entity, stack, Attributes.STEP_HEIGHT, AttributeModifier.Operation.ADD_VALUE);
        
        super.onStopUsing(stack, entity, count);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, InteractionHand pUsedHand) {
        if (player.getFoodData().getFoodLevel() < 2)
            return InteractionResultHolder.fail(player.getItemInHand(pUsedHand));
        return super.use(level, player, pUsedHand);
    }
    
    @Override
    public boolean isPrimaryItemFor(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        return enchantment.is(Enchantments.THORNS);
    }
    
    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return isPrimaryItemFor(stack, enchantment);
    }
    
    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 20;
    }
    
    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return true;
    }
    
    @Override
    public UseAnim getUseAnimation(ItemStack p_43105_) {
        return UseAnim.CUSTOM;
    }
    
    public void rebuke(Player player, ItemStack shield) {
        
        if (shield.getItem() instanceof ItemRonasShield) {
            int charges = shield.getOrDefault(RHONAS_CHARGES, 0);
            
            if (charges > 0) {
                
                
                player.level().playSound(null, player.blockPosition(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 0.66f, 3f);
                player.level().playSound(null, player.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 0.4f, 0.02f);
                player.level().playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8f, 0.3f);
                if (charges == 3)
                    player.level().playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.45f, 0.8f);
                
                double range = 2 + charges * 1.2;
                float damage = (float) this.getStatValue(player, shield, "rebuke", "dmg") * charges;
                
                AABB box = AABB.ofSize(player.getEyePosition().add(player.getLookAngle().scale(range * 0.5)), range, range / 2, range);
                
                List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e.distanceToSqr(player.position()) < range * range && !Objects.equals(e.getUUID(), player.getUUID()));
                for (LivingEntity e : targets) {
                    if (e.hurt(player.level().damageSources().playerAttack(player), damage)) {
                        Vec3 knockback = e.position().subtract(player.position()).normalize().scale(0.8 * charges);
                        e.setDeltaMovement(e.getDeltaMovement().add(knockback.add(0, 0.2, 0)));
                        if (charges == 3) e.setRemainingFireTicks(120);
                        
                        this.addExperience(player, shield, 1);
                    }
                    
                }
                
                player.swing(player.getUsedItemHand(), true);
                
                double x, y, z;
                
                double a = 0.65 + 0.05 * charges;
                double b = 0.4 + 0.1 * charges;
                double c = 1.1 + 0.2 * charges;
                
                Vec3 xVec = player.getLookAngle();
                Vec3 zVec = Vec3.directionFromRotation(0, player.getYHeadRot()).cross(new Vec3(0, 1, 0));
                Vec3 yVec = xVec.cross(zVec);
                for (int t = 0; t < 360; t += 10 - charges) {
                    for (int f = 0; f < 360; f += 10 - charges) {
                        x = a * Math.sin(t * Mth.DEG_TO_RAD) * Math.cos(f * Mth.DEG_TO_RAD) * RNG.nextFloat(0.4f, 1);
                        if (x > 0.2) {
                            y = b * Math.sin(t * Mth.DEG_TO_RAD) * Math.sin(f * Mth.DEG_TO_RAD) * RNG.nextFloat(0.4f, 1);
                            z = c * Math.cos(t * Mth.DEG_TO_RAD) * RNG.nextFloat(0.4f, 1);
                            
                            Vec3 startPos = player.getEyePosition().add(0, -0.3, 0);
                            
                            Vec3 move = xVec.scale(x * 2).add(yVec.scale(y * 0.8)).add(zVec.scale(z * 0.5)).scale((0.5 + 0.15 * charges) * RNG.nextFloat());
                            
                            ParticleHelper.spawnDirectedParticle(player.level(), ParticleHelper.constructSimpleSpark(new Color(255, RNG.nextInt(80 - 10 * charges), 0),
                                    (0.25f + RNG.nextFloat(0.3f)) * (1 + charges * 0.2f), 20 + 6 * charges, 0.85F + 0.01f * charges), startPos.add(xVec.scale(x + 0.6)).add(yVec.scale(y)).add(zVec.scale(z)), move);
                            
                            if (charges == 3 && RNG.nextFloat() < 0.12)
                                ParticleHelper.spawnDirectedParticle(player.level(), ParticleTypes.FLAME
                                        , startPos.add(xVec.scale(x + 0.6)).add(yVec.scale(y)).add(zVec.scale(z)), move.scale(1.2));
                        }
                    }
                }
                
                shield.set(RHONAS_CHARGES, 0);
            }
        }
    }
    
    @Override
    public int getFoilColor(@NotNull ItemStack stack) {
        return /*0xFA9FEB7D*/ new Color(145, 43, 29).getRGB();
    }

    @Override
    public String getConfigRoute() {
        return "relics";
    }

    @OnlyIn(Dist.CLIENT)
    @EventBusSubscriber
    public static class ClientEventHandler {
        
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onClick(InputEvent.MouseButton.Post event) {
            
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null
                    && player.getUseItem().getItem() instanceof ItemRonasShield
                    && event.getButton() == 0
                    && event.getAction() == InputConstants.PRESS
                    && player.getUseItem().getOrDefault(CHARGING_TICKER, 0) == 0) {
                
                Network.sendToServer(new RhonasRebukePacket());
            }
        }

        private static boolean isCtrl = false;

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onKeyInput(InputEvent.Key event) {

            if (event.getAction() != GLFW.GLFW_PRESS) return;

            int key = event.getKey();

            if (key != GLFW.GLFW_KEY_LEFT_CONTROL &&
                    key != GLFW.GLFW_KEY_RIGHT_CONTROL) {
                return;
            }

            if (!isCtrl) {
                isCtrl = true;

                Network.sendToServer(new IPacket() {
                    @Override
                    public void serverExecute(PacketContext ctx) {
                        Player player = ctx.getSender();
                        ItemStack stack = player.getItemInHand(player.getUsedItemHand());
                        if (stack.getItem() instanceof ItemRonasShield shield && player.isUsingItem()) {
                            stack.set(CHARGING_TICKER, (int) shield.getStatValue(player, stack, "charge", "dur"));
                            shield.setMaxCooldown(player, stack, "charge");
                            EntityUtils.applyAttribute(player, stack, Attributes.STEP_HEIGHT, 0.6F, AttributeModifier.Operation.ADD_VALUE);
                            return;
                        }
                    }
                });
            }

            if (event.getAction() == GLFW.GLFW_RELEASE &&
                    (event.getKey() == GLFW.GLFW_KEY_LEFT_CONTROL ||
                            event.getKey() == GLFW.GLFW_KEY_RIGHT_CONTROL)) {

                isCtrl = false;
            }
        }
        
    }
    
}
