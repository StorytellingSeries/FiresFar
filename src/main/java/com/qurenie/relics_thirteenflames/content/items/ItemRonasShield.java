package com.qurenie.relics_thirteenflames.content.items;

import com.mojang.blaze3d.platform.InputConstants;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.net.RhonasRebukePacket;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.init.CreativeTabRegistry;
import it.hurts.sskirillss.relics.init.EffectRegistry;
import it.hurts.sskirillss.relics.init.RelicContainerRegistry;
import it.hurts.sskirillss.relics.items.misc.CreativeContentConstructor;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.containers.InventoryRelicContainer;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.PredicateType;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.NBTUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.api.items.IColoredFoilItem;
import org.zeith.hammerlib.net.Network;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.*;

@EventBusSubscriber
public class ItemRonasShield extends ShieldItem implements IColoredFoilItem, IRelicItem {
    
    
    private static final Random RNG = new Random();
    
    public ItemRonasShield(Properties properties) {
        
        super(properties);
    }
    
    public void gatherCreativeTabContent(CreativeContentConstructor constructor) {
        constructor.entry((CreativeModeTab) CreativeTabRegistry.RELICS_TAB.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS, new ItemLike[]{this});
    }
    
    public String getConfigRoute() {
        return "relics";
    }
    
    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        ItemStack stack = event.getEntity().getItemInHand(event.getEntity().getUsedItemHand());
        if (stack.getItem() instanceof ItemRonasShield shit) {
            
            float blockRate = (float) shit.getStatValue(stack, "block", "blockrate");
            float blockedDmg = event.getOriginalBlockedDamage() * blockRate;
            float hungerDmg = (float) shit.getStatValue(stack, "block", "hungerdmg");
            if (event.getEntity() instanceof Player p) {
                p.causeFoodExhaustion(blockedDmg * hungerDmg);
            }
            
            double chargeRate = shit.getStatValue(stack, "rebuke", "chargerate");
            int maxCharge = (int) shit.getStatValue(stack, "rebuke", "maxcharge");
            
            float charge = stack.getOrDefault(RHONAS_BLOCKED, 0f);
            
            int chargesToPut = stack.getOrDefault(RHONAS_CHARGES, 0);
            
            shit.spreadRelicExperience(event.getEntity(), stack, (int) Math.min(blockedDmg, 15));
            
            
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
            float blockRate = (float) shit.getStatValue(event.getEntity().getUseItem(), "block", "blockrate");
            event.setStrength(event.getStrength() * (1 - blockRate));
        }
    }
    
    @Override
    public boolean canPerformAction(@NotNull ItemStack stack, @NotNull ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_SHIELD_ACTIONS.contains(itemAbility);
    }
    
    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("block")
                                .maxLevel(8)
                                .stat(StatData.builder("blockrate")
                                        .initialValue(0.6, 0.64)
                                        .thresholdValue(0, 1)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.05)
                                        .formatValue(x -> (int) MathUtils.round(x * 100, 0))
                                        .build()
                                )
                                .stat(StatData.builder("speed")
                                        .initialValue(0.2, 0.2)
                                        .thresholdValue(0, 1)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.075)
                                        .formatValue(x -> (int) MathUtils.round((1 - x) * 100, 0))
                                        .build()
                                )
                                .stat(StatData.builder("hungerdmg")
                                        .initialValue(1.8, 1.6)
                                        .thresholdValue(0, 10)
                                        .upgradeModifier(UpgradeOperation.ADD, -0.1)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityData.builder("rebuke")
                                .maxLevel(2)
                                .stat(StatData.builder("maxcharge")
                                        .initialValue(1, 1)
                                        .thresholdValue(1, 3)
                                        .upgradeModifier(UpgradeOperation.ADD, 1)
                                        .formatValue(x -> (int) MathUtils.round(x, 0))
                                        .build()
                                )
                                .stat(StatData.builder("chargerate")
                                        .initialValue(8.0, 7.0)
                                        .thresholdValue(1.0, 20.0)
                                        .upgradeModifier(UpgradeOperation.ADD, -1)
                                        .formatValue(x -> (int) MathUtils.round(x, 0))
                                        .build()
                                )
                                .stat(StatData.builder("dmg")
                                        .initialValue(7, 8)
                                        .thresholdValue(0, 40)
                                        .upgradeModifier(UpgradeOperation.ADD, 4)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityData.builder("charge")
                                .maxLevel(5)
                                .active(CastData.builder()
                                        .type(CastType.INSTANTANEOUS)
                                        .container(RelicContainerRegistry.INVENTORY.get())
                                        .predicate("chargecast", PredicateType.CAST, (p, stack) -> p.getUseItem().equals(stack))
                                        .build())
                                .stat(StatData.builder("dur")
                                        .initialValue(10, 20)
                                        .thresholdValue(1, 120)
                                        .upgradeModifier(UpgradeOperation.ADD, 12)
                                        .formatValue(x -> MathUtils.round(x / 20, 1))
                                        .build()
                                )
                                .stat(StatData.builder("dmg")
                                        .initialValue(4, 5)
                                        .thresholdValue(0, 10)
                                        .upgradeModifier(UpgradeOperation.ADD, 1)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(StatData.builder("stunduration")
                                        .initialValue(10, 20)
                                        .thresholdValue(0, 80)
                                        .upgradeModifier(UpgradeOperation.ADD, 8)
                                        .formatValue(x -> MathUtils.round(x / 20, 1))
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .leveling(new LevelingData(100, 15, 100))
                .loot(LootData.builder().entry(LootEntries.DESERT).build())
                .build();
    }
    
    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        if (ability.equals("charge")) {
            stack.set(CHARGING_TICKER, (int) getStatValue(stack, "charge", "dur"));
            addAbilityCooldown(stack, "charge", 400);
            EntityUtils.applyAttribute(player, stack, Attributes.STEP_HEIGHT, 0.6F, AttributeModifier.Operation.ADD_VALUE);
        }
        IRelicItem.super.castActiveAbility(stack, player, ability, type, stage);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable TooltipContext context, List<Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("tooltip.relics_thirteenflames.ronas_shield.lore").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
//        if (context.level() == null || !context.level().isClientSide()) return;
//
//        constructRelicTooltipBecauseIRelicItemDoesntFuckingWork(stack, tooltip);
        super.appendHoverText(stack, context, tooltip, isAdvanced);
        
    }
    
//    @OnlyIn(Dist.CLIENT)
//    private void constructRelicTooltipBecauseIRelicItemDoesntFuckingWork(ItemStack stack, List<Component> tooltip) {
//
//        Item item = stack.getItem();
//
//        if (!(item instanceof IRelicItem relic))
//            return;
//
//        tooltip.add(Component.literal(" "));
//
//        if (Screen.hasShiftDown()) {
//            RelicData relicData = relic.getRelicData();
//
//            if (relicData == null)
//                return;
//
//            Map<String, AbilityData> abilities = relicData.getAbilities().getAbilities();
//
//            tooltip.add(Component.literal("▶ ").withStyle(ChatFormatting.DARK_GREEN)
//                    .append(Component.translatable("tooltip.relics.relic.tooltip.abilities").withStyle(ChatFormatting.GREEN)));
//
//            for (Map.Entry<String, AbilityData> entry : abilities.entrySet()) {
//                String id = BuiltInRegistries.ITEM.getKey(item).getPath();
//                String name = entry.getKey();
//
//                if (!relic.isAbilityUnlocked(stack, name))
//                    continue;
//
//                tooltip.add(Component.literal("   ◆ ").withStyle(ChatFormatting.GREEN)
//                        .append(Component.translatable("tooltip.relics." + id + ".ability." + name).withStyle(ChatFormatting.YELLOW))
//                        .append(Component.literal(" - ").withStyle(ChatFormatting.WHITE))
//                        .append(Component.translatable("tooltip.relics." + id + ".ability." + name + ".description").withStyle(ChatFormatting.GRAY)));
//            }
//        } else {
//            tooltip.add(Component.translatable("tooltip.relics.relic.tooltip.shift").withStyle(ChatFormatting.GRAY));
//        }
//
//        tooltip.add(Component.literal(" "));
//    }
    
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }
    
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int p_41407_, boolean p_41408_) {
        if (entity instanceof LivingEntity l && stack.getOrDefault(BLOCKED, false) != (l.getUseItem() == stack))
            stack.set(BLOCKED, l.getUseItem() == stack);
        super.inventoryTick(stack, level, entity, p_41407_, p_41408_);
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
                        
                        ParticleHelper.spawnDirectedParticle(player.level(), ParticleUtils.constructSimpleSpark(new Color(255, RNG.nextInt(80 - 10), 0),
                                (float) (0.1f + RNG.nextFloat(0.1f) + x / 4f), 10, 0.85F), startPos.add(xVec.scale(x + 0.8)).add(yVec.scale(y)).add(zVec.scale(z)), move);
                        
                    }
                }
            }
            
            AABB aoe = player.getBoundingBox().inflate(2, 0.3, 2);
            for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, aoe, e -> !e.getUUID().equals(player.getUUID()))) {
                float damag = (float) (getStatValue(stack, "charge", "dmg") + (getStatValue(stack, "charge", "dur") - chargingTicker) / 8);
                if (target.hurt(player.damageSources().playerAttack(player), damag)) {
                    Vec3 awayctor = target.position().subtract(player.position()).subtract(player.getDeltaMovement()).normalize().scale(2);
                    target.push(awayctor.x(), awayctor.y() + 1, awayctor.z());
                    target.addEffect(new MobEffectInstance(EffectRegistry.STUN, (int) getStatValue(stack, "charge", "stunduration"), 0));
                    this.spreadRelicExperience(player, stack, 1);
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
                float damage = (float) this.getStatValue(shield, "rebuke", "dmg") * charges;
                
                AABB box = AABB.ofSize(player.getEyePosition().add(player.getLookAngle().scale(range * 0.5)), range, range / 2, range);
                
                List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e.distanceToSqr(player.position()) < range * range && !Objects.equals(e.getUUID(), player.getUUID()));
                for (LivingEntity e : targets) {
                    if (e.hurt(player.level().damageSources().playerAttack(player), damage)) {
                        Vec3 knockback = e.position().subtract(player.position()).normalize().scale(0.8 * charges);
                        e.setDeltaMovement(e.getDeltaMovement().add(knockback.add(0, 0.2, 0)));
                        if (charges == 3) e.setRemainingFireTicks(120);
                        
                        this.spreadRelicExperience(player, shield, 1);
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
                            
                            ParticleHelper.spawnDirectedParticle(player.level(), ParticleUtils.constructSimpleSpark(new Color(255, RNG.nextInt(80 - 10 * charges), 0),
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
    
    @OnlyIn(Dist.CLIENT)
    @EventBusSubscriber
    public static class ClientEventHandler {
        
        @SubscribeEvent
        public static void onClick(InputEvent.MouseButton.Post event) {
            
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && player.getUseItem().getItem() instanceof ItemRonasShield && event.getButton() == 0 && event.getAction() == InputConstants.PRESS) {
                
                Network.sendToServer(new RhonasRebukePacket());
            }
        }
        
    }
    
}
