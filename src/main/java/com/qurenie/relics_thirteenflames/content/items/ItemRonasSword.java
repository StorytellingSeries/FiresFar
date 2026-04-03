package com.qurenie.relics_thirteenflames.content.items;

import com.qurenie.api.IExtRelicItem;
import com.qurenie.relics_thirteenflames.content.effects.PoisonEffectInstance;
import com.qurenie.relics_thirteenflames.content.entities.FartCloudEntity;
import com.qurenie.relics_thirteenflames.content.entities.PoisonWaveProjectile;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.net.RhonasSweepPacket;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicAttributeModifier;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootEntry;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.api.items.IColoredFoilItem;
import org.zeith.hammerlib.net.Network;
import top.theillusivec4.curios.api.CuriosApi;

import java.awt.*;
import java.util.List;
import java.util.Random;

@EventBusSubscriber
public class ItemRonasSword extends RelicItem implements IExtRelicItem, IColoredFoilItem {

    public ItemRonasSword(Properties properties) {
        super(properties);
    }

    private static final Random RNG = new Random();


    @Override
    public @Nullable RelicAttributeModifier getRelicAttributeModifiers(LivingEntity entity, ItemStack stack) {
        float atkspd = (float) getStatValue(entity, stack, "anemia", "atkspd");

        return RelicAttributeModifier.builder()
                .attribute(new RelicAttributeModifier.Modifier(Attributes.ATTACK_DAMAGE, 3, AttributeModifier.Operation.ADD_VALUE))
                .attribute(new RelicAttributeModifier.Modifier(Attributes.ATTACK_SPEED, -2.6F + atkspd, AttributeModifier.Operation.ADD_VALUE))
                .build();
    }
    
    @Override
    public boolean canPerformAction(@NotNull ItemStack stack, @NotNull ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(itemAbility);
    }

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("spit")
                                .initialMaxLevel(5)
                                .stat(AbilityStatTemplate.builder("range")
                                        .initialValue(4, 4.2)
                                        .thresholdValue(4, 8)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0.76)
                                        .formatValue(x -> MathUtils.round(x, 2))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("poisondur")
                                        .initialValue(2.0, 2.5)
                                        .thresholdValue(2.0, 4.5)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0.4)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("maxstacks")
                                        .initialValue(1.0, 1.0)
                                        .thresholdValue(1.0, 6.0)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 1)
                                        .formatValue(x -> (int) MathUtils.round(x, 1))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityTemplate.builder("fart")
                                .initialMaxLevel(3)
                                .stat(AbilityStatTemplate.builder("radius")
                                        .initialValue(2.0, 3.5)
                                        .thresholdValue(2.0, 5.0)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0.5)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("duration")
                                        .initialValue(6.0, 10.0)
                                        .thresholdValue(6.0, 20.0)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 3.33)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("recharge")
                                        .initialValue(40, 30)
                                        .thresholdValue(12, 40)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), -6)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityTemplate.builder("anemia")
                                .initialMaxLevel(2)
                                .stat(AbilityStatTemplate.builder("amp")
                                        .initialValue(3, 2)
                                        .thresholdValue(0, 2)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), -1.0)
                                        .formatValue(x -> (int) MathUtils.round((1 - 0.8f / (x + 1)) * 100, 0))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("atkspd")
                                        .initialValue(0, 0.6)
                                        .thresholdValue(0, 1.8)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0.6)
                                        .formatValue(x -> MathUtils.round(4 - 2.6 + x, 2))
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .leveling(LevelingTemplate.builder()
                        .maxRank(1)
                        .step(100)
                        .initialCost(50)
                        .build())
                .loot(LootTemplate.builder().entry(LootEntry.builder().dimension(".*").biome("[\\w]+:.*(jungle|rainforest|tropic|wildwood|thicket|boscage|humid|bamboo)[\\w_\\/]*").table("[\\w]+:chests\\/[\\w_\\/]*[\\w]+[\\w_\\/]*").weight(500).build()).build())
                .build();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, List<net.minecraft.network.chat.Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("tooltip.relics_thirteenflames.ronas_sword.lore").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, tooltip, isAdvanced);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pPlayer.isCrouching()) {
            pLevel.playSound(null, pPlayer, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.MASTER, 1.2f, 0.1f);
            pLevel.playSound(null, pPlayer, SoundEvents.SCULK_BLOCK_BREAK, SoundSource.MASTER, 1f, 1f);
            pLevel.playSound(null, pPlayer, SoundEvents.AZALEA_FALL, SoundSource.MASTER, 1f, 0.01f);
            ItemStack sword = pPlayer.getItemInHand(pUsedHand);
            int lifetime = (int) getStatValue(pPlayer, sword, "fart", "duration") * 20;
            float radius = (float) getStatValue(pPlayer, sword, "fart", "radius");
            FartCloudEntity cloud = new FartCloudEntity(EntityRegistry.FARTCLOUD, pLevel);
            cloud.setRadius(radius);
            cloud.setLifeTime(lifetime);
            cloud.setMaxAmp((int)Math.round(getStatValue(pPlayer, sword, "spit", "maxstacks") - 1));
            cloud.setDuration((int) Math.round(getStatValue(pPlayer, sword, "spit", "poisondur") * 20));
            cloud.setOwner(pPlayer);
            cloud.setSword(pPlayer.getItemInHand(pUsedHand));
            Vec3 pos = pPlayer.getEyePosition(1).add(
                    pPlayer.getLookAngle().scale(radius + 1)
            );
            cloud.setPos(pos);
            pLevel.addFreshEntity(cloud);
            int cooldown = (int) Math.round(getStatValue(pPlayer, pPlayer.getItemInHand(pUsedHand), "fart", "recharge") * 20);
            pPlayer.getCooldowns().addCooldown(this, cooldown);

        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @SubscribeEvent
    public static void onAttack(net.neoforged.neoforge.event.entity.player.AttackEntityEvent event) {
        if (event.getEntity().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemRonasSword && !event.getEntity().level().isClientSide()) {
            if(event.getEntity().getAttackStrengthScale(0.5F) > 0.9F) {
                poisonSwipe(event.getEntity(), event.getEntity().getItemInHand(InteractionHand.MAIN_HAND));
            } else{
                event.getEntity().level().playSound(null, event.getEntity(), SoundEvents.AZALEA_FALL, SoundSource.MASTER, 1f, 0.8f);
                event.getEntity().level().playSound(null, event.getEntity(), SoundEvents.SCULK_BLOCK_BREAK, SoundSource.MASTER, 0.7f, 0.7f);
                event.getEntity().level().playSound(null, event.getEntity(), SoundEvents.BLAZE_BURN, SoundSource.MASTER, 0.8f, 2.4f);
                event.getEntity().addEffect(new PoisonEffectInstance(EffectsRegistry.POISSON, 100, 0, false, true, true, event.getEntity().getItemInHand(InteractionHand.MAIN_HAND)));
            }
        }
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        return !pPlayer.isCreative();
    }
    
    @Override
    public boolean isPrimaryItemFor(@NotNull ItemStack stack, Holder<Enchantment> enchantment) {
        Enchantment.EnchantmentDefinition definition = enchantment.value().definition();
        boolean isPrimary = definition.primaryItems().isPresent() && FlamesUtils.isWeaponEnchantment(definition.primaryItems().get());
        boolean supports = FlamesUtils.isWeaponEnchantment(definition.supportedItems());
        return isPrimary || supports && !enchantment.is(Enchantments.UNBREAKING) && !enchantment.is(Enchantments.SWEEPING_EDGE);
    }
    
    @Override
    public boolean supportsEnchantment(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        return this.isPrimaryItemFor(stack, enchantment);
    }

    @Override
    public int getEnchantmentValue(@NotNull ItemStack stack) {
        return 20;
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack pStack) {
        return true;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);
        if (entity instanceof Player player && stack.is(this)) {

            //if(player instanceof LocalPlayer lp) lp.chatSigned(String.valueOf(lp.getXRot()), null);

            if ( !level.isClientSide() && (!player.hasEffect(EffectsRegistry.ANEMIA)
                    || (player.hasEffect(EffectsRegistry.ANEMIA) && player.getEffect(EffectsRegistry.ANEMIA).getDuration() < 21))) {
                int amp = (int) getStatValue(player, stack, "anemia", "amp");
                player.addEffect(new MobEffectInstance(EffectsRegistry.ANEMIA, 39, amp, true, false, true));
            }
        }
    }

    public static void poisonSwipe(LivingEntity p, ItemStack sword) {

        if(sword.getItem() instanceof ItemRonasSword relic) {
            p.level().playSound(null, p, SoundEvents.AZALEA_FALL, SoundSource.MASTER, 1f, 0.02f);
            p.level().playSound(null, p, SoundEvents.SCULK_BLOCK_BREAK, SoundSource.MASTER, 1f, 1f);
            p.level().playSound(null, p, SoundEvents.AZALEA_LEAVES_FALL, SoundSource.MASTER, 1f, 1.8f);

            double range = relic.getStatValue(p, sword, "spit", "range");

            Vec3 startVec = p.getEyePosition(1F)
                    .add(0, -0.2, 0);
            
            if (p.level() instanceof ServerLevel level) {
                
                float glovesRangeBonus = CuriosApi.getCuriosInventory(p).map(handler -> {
                    float result = 0;
                    
                    ItemStack stack = handler.getCurios().get("hands").getStacks().getStackInSlot(0);
                    if (stack.is(ItemsRegistry.MONTU_GLOVES))
                        result += (float) ItemsRegistry.MONTU_GLOVES.getStatValue(p, stack, "gloves_range", "range") * 1.2f;
                    
                    if (handler.getCurios().get("hands").getSlots() > 1) {
                        stack = handler.getCurios().get("hands").getStacks().getStackInSlot(1);
                        if (stack.is(ItemsRegistry.MONTU_GLOVES))
                            result += (float) ItemsRegistry.MONTU_GLOVES.getStatValue(p, stack, "gloves_range", "range") * 1.2f;
                    }
                    
                    return result;
                }).orElse(0f);
                range += glovesRangeBonus;
                
                PoisonWaveProjectile wave = new PoisonWaveProjectile(EntityRegistry.POISONWAVE, level);
                wave.setPos(startVec);
                wave.startVec = startVec;
                wave.flatluk = Vec3.directionFromRotation(0, p.getYHeadRot());
                wave.spreadAngle = 20 + (range * 1.2);
                wave.setDeltaMovement(p.getLookAngle().scale(range * 0.1 + 0.06));
                wave.setMaxRange((float) range);
                wave.setOwner(p);
                wave.setSword(sword);
                level.addFreshEntity(wave);

            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {

        if(event.getEntity().level().isClientSide()) return;
        int stacks = event.getEntity().hasEffect(EffectsRegistry.POISSON) ? event.getEntity().getEffect(EffectsRegistry.POISSON).getAmplifier() + 1 : 0;


        if(event.getEntity().getEffect(EffectsRegistry.POISSON) instanceof PoisonEffectInstance pei && pei.getOriginSword().getItem() instanceof ItemRonasSword relic){

            for (int i = 0; i < stacks; i++) {
                relic.addExperience((LivingEntity) event.getSource().getEntity(), pei.getOriginSword(), RNG.nextInt(3) + 1);
            }
        }
    }

    @SubscribeEvent
    public static void onHitAir(PlayerInteractEvent.LeftClickEmpty event) {
        if(!event.getItemStack().is(ItemsRegistry.RONAS_SWORD)) return;

        Network.sendToServer(new RhonasSweepPacket(event.getItemStack()));
    }



    @Override
    public int getFoilColor(@NotNull ItemStack stack) {
        return /*0xFA9FEB7D*/ new Color(31, 110, 0).getRGB();
    }

}
