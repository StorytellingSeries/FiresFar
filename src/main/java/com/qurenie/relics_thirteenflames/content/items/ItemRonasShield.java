package com.qurenie.relics_thirteenflames.content.items;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.platform.InputConstants;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.client.render.item.EmissiveItemRenderer;
import com.qurenie.relics_thirteenflames.client.render.item.RonasShieldItemRenderer;
import com.qurenie.relics_thirteenflames.content.effects.PoisonEffectInstance;
import com.qurenie.relics_thirteenflames.content.entities.FartCloudEntity;
import com.qurenie.relics_thirteenflames.content.entities.PoisonWaveProjectile;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.net.RhonasRebukePacket;
import com.qurenie.relics_thirteenflames.net.RhonasSweepPacket;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.api.items.IColoredFoilItem;
import org.zeith.hammerlib.net.Network;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod.EventBusSubscriber
public class ItemRonasShield extends ShieldItem implements IColoredFoilItem, IRelicItem {



    public ItemRonasShield(Properties properties) {

        super(properties);
    }

    private static Random rng = new Random();


    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
        return ToolActions.DEFAULT_SHIELD_ACTIONS.contains(toolAction);
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
                                        .formatValue(x -> MathUtils.round(x * 100, 0))
                                        .build()
                                )
                                .stat(StatData.builder("speed")
                                        .initialValue(0.2, 0.2)
                                        .thresholdValue(0, 1)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.075)
                                        .formatValue(x -> MathUtils.round((1 - x) * 100, 0))
                                        .build()
                                )
                                .stat(StatData.builder("hungerdmg")
                                        .initialValue(1.8, 1.6)
                                        .thresholdValue(0, 10)
                                        .upgradeModifier(UpgradeOperation.ADD, -0.1)
                                        .formatValue(x -> (int) MathUtils.round(x, 1))
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
                                        .formatValue(x -> MathUtils.round(x, 0))
                                        .build()
                                )
                                .stat(StatData.builder("chargerate")
                                        .initialValue(8.0, 7.0)
                                        .thresholdValue(1.0, 20.0)
                                        .upgradeModifier(UpgradeOperation.ADD, -1)
                                        .formatValue(x -> MathUtils.round(x, 0))
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
                                .stat(StatData.builder("dur")
                                        .initialValue(30, 40)
                                        .thresholdValue(1, 120)
                                        .upgradeModifier(UpgradeOperation.ADD, 16)
                                        .formatValue(x -> (int) MathUtils.round(x / 20, 1))
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
                                        .initialValue(20, 20)
                                        .thresholdValue(0, 60)
                                        .upgradeModifier(UpgradeOperation.ADD, 8)
                                        .formatValue(x -> (int) MathUtils.round(x / 20, 1))
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .leveling(new LevelingData(100, 15, 100))
                .build();
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("tooltip.relics_thirteenflames.ronas_sword.lore").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, level, tooltip, isAdvanced);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int slot) {
        if(living instanceof Player p && p.getFoodData().getFoodLevel() < 2) p.stopUsingItem();

        super.onUseTick(level, living, stack, slot);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand pUsedHand) {
        if(player.getFoodData().getFoodLevel() < 2) return InteractionResultHolder.fail(player.getItemInHand(pUsedHand));
        return super.use(level, player, pUsedHand);
    }


    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.ALL_DAMAGE_PROTECTION;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 20;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);

//        Minecraft.getInstance().player.sendSystemMessage(Component.literal(String.valueOf(stack.getOrCreateTag().getInt("charges"))));
//        Minecraft.getInstance().player.sendSystemMessage(Component.literal("-  " + String.valueOf(stack.getOrCreateTag().getFloat("blockeddmg"))));

    }

    @Override
    public UseAnim getUseAnimation(ItemStack p_43105_) {
        return UseAnim.CUSTOM;
    }

    @SubscribeEvent
    public static void onShieldBlock(ShieldBlockEvent event){
        if(event.getEntity().getUseItem().getItem() instanceof ItemRonasShield shit){
            ItemStack stack = event.getEntity().getUseItem();
            float blockRate = (float) shit.getAbilityValue(stack, "block", "blockrate");
            float blockedDmg = event.getOriginalBlockedDamage() * blockRate;
            float hungerDmg = (float) shit.getAbilityValue(stack, "block", "hungerdmg");
            if(event.getEntity() instanceof Player p) {
                p.causeFoodExhaustion(blockedDmg * hungerDmg);
            }

            double chargeRate = shit.getAbilityValue(stack, "rebuke", "chargerate");
            int maxCharge = (int) shit.getAbilityValue(stack, "rebuke", "maxcharge");

            float charge = stack.getOrCreateTag().getFloat("blockeddmg");

            int chargesToPut = stack.getOrCreateTag().getInt("charges");


            charge += blockedDmg;

            if(charge > chargeRate) {
                chargesToPut = Math.min(chargesToPut + (int) (charge / chargeRate), maxCharge);
                charge %= (float) chargeRate;
                stack.getOrCreateTag().putInt("charges", chargesToPut);
            }

            stack.getOrCreateTag().putFloat("blockeddmg", charge);

            event.setBlockedDamage(blockedDmg);
            event.setShieldTakesDamage(false);

        }
    }

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event){
        if(event.getEntity().getUseItem().getItem() instanceof ItemRonasShield shit){
            float blockRate = (float) shit.getAbilityValue(event.getEntity().getUseItem(), "block", "blockrate");
            event.setStrength(event.getStrength() * (1 - blockRate));
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Mod.EventBusSubscriber(modid = ThirteenFlames.MODID,bus = Mod.EventBusSubscriber.Bus.FORGE,value = Dist.CLIENT)
    public static class ClientEventHandler{
        @SubscribeEvent
        public static void onClick(InputEvent.MouseButton.Post event) {

            LocalPlayer player = Minecraft.getInstance().player;
            if(player != null && player.getUseItem().getItem() instanceof ItemRonasShield && event.getButton() == 0 && event.getAction() == InputConstants.PRESS) {

                Network.sendToServer(new RhonasRebukePacket());
            }
        }
    }

    public void rebuke(Player player, ItemStack shield){

        if(shield.getItem() instanceof ItemRonasShield shit) {
            int charges = shield.getOrCreateTag().getInt("charges");

            if (charges > 0) {

                double range = 2 + charges * 1.2;
                float damage = (float) shit.getAbilityValue(shield, "rebuke", "dmg");

                AABB box = AABB.ofSize(player.getEyePosition().add(player.getLookAngle().scale(range * 0.5)), range, range / 2, range);

                List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e.distanceToSqr(player.position()) < range * range && !Objects.equals(e.getUUID(), player.getUUID()));
                for (LivingEntity e : targets) {
                    e.hurt(player.level().damageSources().playerAttack(player), damage);
                    Vec3 knockback = e.position().subtract(player.position()).normalize().scale(0.8 * charges);
                    e.setDeltaMovement(e.getDeltaMovement().add(knockback.add(0, 0.2, 0)));
                    if(charges == 3) e.setSecondsOnFire(6);
                }

                player.swing(player.getUsedItemHand());

                double x, y, z;

                double a = 0.65 + 0.05 * charges;
                double b = 0.4 + 0.1 * charges;
                double c = 1.1 + 0.2 * charges;

                Vec3 xVec = player.getLookAngle();
                Vec3 zVec = Vec3.directionFromRotation(0, player.getYHeadRot()).cross(new Vec3(0, 1, 0));
                Vec3 yVec = xVec.cross(zVec);
                for (int t = 0; t < 360; t += 10 - charges) {
                    for (int f = 0; f < 360; f += 10 - charges) {
                        x = a * Math.sin(t * Mth.DEG_TO_RAD) * Math.cos(f * Mth.DEG_TO_RAD) * rng.nextFloat(0.4f, 1);
                        if (x > 0.2) {
                            y = b * Math.sin(t * Mth.DEG_TO_RAD) * Math.sin(f * Mth.DEG_TO_RAD) * rng.nextFloat(0.4f, 1);
                            z = c * Math.cos(t * Mth.DEG_TO_RAD) * rng.nextFloat(0.4f, 1);

                            Vec3 startPos = player.getEyePosition().add(0, -0.3, 0);

                            Vec3 move = xVec.scale(x * 2).add(yVec.scale(y * 0.8)).add(zVec.scale(z * 0.5)).scale((0.5 + 0.15 * charges) * rng.nextFloat());

                            ParticleHelper.spawnDirectedParticle(player.level(), ParticleUtils.constructSimpleSpark(new Color(255, rng.nextInt(80 - 10 * charges), 0),
                                    (0.25f + rng.nextFloat(0.3f)) * (1 + charges * 0.2f), 20 + 6 * charges, 0.85F + 0.01f * charges), startPos.add(xVec.scale(x + 0.6)).add(yVec.scale(y)).add(zVec.scale(z)), move);

                            if(charges == 3 && rng.nextFloat() < 0.12) ParticleHelper.spawnDirectedParticle(player.level(), ParticleTypes.FLAME
                                    , startPos.add(xVec.scale(x + 0.6)).add(yVec.scale(y)).add(zVec.scale(z)), move.scale(1.2));
                        }
                    }
                }


                shield.getOrCreateTag().putInt("charges", 0);
            }
        }
    }


    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            final Supplier<RonasShieldItemRenderer> renderer = Suppliers.memoize(RonasShieldItemRenderer::new);

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer()
            {
                return renderer.get();
            }

            public static final HumanoidModel.ArmPose ARM_POSE_LEFT = HumanoidModel.ArmPose.create("thirteenflames_shield_pose_left", true, (model, entity, arm) ->
            {
                var lArm = model.leftArm;

                lArm.xRot = lArm.xRot * 0.1F - 0.9424779F;
                lArm.yRot = ((float)Math.PI / 6F);

            });

            public static final HumanoidModel.ArmPose ARM_POSE_RIGHT = HumanoidModel.ArmPose.create("thirteenflames_shield_pose_right", true, (model, entity, arm) ->
            {

                var rArm = model.rightArm;

                rArm.xRot = rArm.xRot * 0.1F - 0.9424779F;
                rArm.yRot = (-(float)Math.PI / 6F);

            });

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack)
            {
                return entityLiving.getUseItem().is(ItemsRegistry.RONAS_SHIELD) ? entityLiving.getUsedItemHand().equals(InteractionHand.OFF_HAND) ? ARM_POSE_LEFT : ARM_POSE_RIGHT : HumanoidModel.ArmPose.ITEM;
            }
        });
    }




    @Override
    public int getFoilColor(@NotNull ItemStack stack) {
        return /*0xFA9FEB7D*/ new Color(180, 16, 0).getRGB();
    }

}
