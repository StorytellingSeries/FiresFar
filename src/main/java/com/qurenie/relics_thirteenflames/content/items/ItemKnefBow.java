package com.qurenie.relics_thirteenflames.content.items;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMultimap;
import com.qurenie.relics_thirteenflames.client.render.item.KnefBowItemRenderer;
import com.qurenie.relics_thirteenflames.content.entities.KnefProjCarrier;
import com.qurenie.relics_thirteenflames.content.entities.KnefProjectile;
import com.qurenie.relics_thirteenflames.content.entities.KnefProjectileSpecial;
import com.qurenie.relics_thirteenflames.content.entities.KnefStormcaller;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import it.hurts.sskirillss.relics.client.particles.circle.CircleTintData;
import it.hurts.sskirillss.relics.client.tooltip.base.RelicStyleData;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.base.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityEntry;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityStat;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicLevelingData;
import it.hurts.sskirillss.relics.items.relics.base.utils.AbilityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.api.items.IColoredFoilItem;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraftforge.common.ForgeMod.WATER_TYPE;

@Mod.EventBusSubscriber
public class ItemKnefBow extends RelicItem implements IColoredFoilItem {


    boolean isShitting = false;

    public ItemKnefBow(Properties properties) {

        super(properties);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
    }

    RandomSource random = RandomSource.create();

    protected final RelicData data = RelicData.builder()
            .abilityData(RelicAbilityData.builder()
                    .ability("shot", RelicAbilityEntry.builder()
                            .maxLevel(10)
                            .stat("rays", RelicAbilityStat.builder()
                                    .initialValue(3, 3)
                                    .thresholdValue(3, 23)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 2)
                                    .formatValue(x -> (int) MathUtils.round(x, 1))
                                    .build()
                            )
                            .stat("dmg", RelicAbilityStat.builder()
                                    .initialValue(3, 4)
                                    .thresholdValue(3, 5)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 0.1)
                                    .formatValue(x -> MathUtils.round(x, 1))
                                    .build()
                            )
                            .stat("drain", RelicAbilityStat.builder()
                                    .initialValue(0.25, 0.2)
                                    .thresholdValue(0.05, 0.25)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, -0.015)
                                    .formatValue(x -> MathUtils.round(x * 100, 1))
                                    .build()
                            )
                            .build()
                    )
                    .ability("swim", RelicAbilityEntry.builder()
                            .maxLevel(5)
                            .stat("speed", RelicAbilityStat.builder()
                                    .initialValue(4, 6)
                                    .thresholdValue(4, 11)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 1)
                                    .formatValue(x -> MathUtils.round(x, 1))
                                    .build()
                            )
                            .stat("dmg", RelicAbilityStat.builder()
                                    .initialValue(6, 8)
                                    .thresholdValue(6, 20)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 2.4)
                                    .formatValue(x -> MathUtils.round(x, 1))
                                    .build()
                            )
                            .build()
                    )
                    .ability("storm", RelicAbilityEntry.builder()
                            .requiredLevel(10)
                            .maxLevel(5)
                            .stat("radius", RelicAbilityStat.builder()
                                    .initialValue(4.0, 5.0)
                                    .thresholdValue(4, 20)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 3.0)
                                    .formatValue(x -> MathUtils.round(x, 1))
                                    .build()
                            )
                            .stat("dur", RelicAbilityStat.builder()
                                    .initialValue(11, 16)
                                    .thresholdValue(11, 32)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 4.0)
                                    .formatValue(x -> (int) MathUtils.round(x, 1))
                                    .build()
                            )
                            .stat("dmg", RelicAbilityStat.builder()
                                    .initialValue(6, 8)
                                    .thresholdValue(6, 13)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 1)
                                    .formatValue(x -> (int) MathUtils.round(x, 1))
                                    .build()
                            )
                            .stat("heal", RelicAbilityStat.builder()
                                    .initialValue(2, 3)
                                    .thresholdValue(2, 10)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 1.4)
                                    .formatValue(x -> MathUtils.round(x, 1))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .levelingData(new RelicLevelingData(100, 18, 100))
            .styleData(RelicStyleData.builder().borders("#fffd75", "#ffbe00").build())
            .build();

    @Override
    public RelicData getRelicData() {
        return data;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltip, isAdvanced);
        tooltip.add(Component.literal("One of the \"Flames\", legendary artifacts scattered across the world.\nCreated by Knephmtyti, goddess of Death").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
    }

    @Override
    public UseAnim getUseAnimation(@NotNull ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return true;
    }

    private boolean isSurging = false;

    @Override
    public void releaseUsing(@NotNull ItemStack pStack, @NotNull Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        if (!pLivingEntity.isEyeInFluidType(WATER_TYPE.get()) && pLivingEntity instanceof Player p) {

            isSurging = false;

            if (!isShitting || !AbilityUtils.canUseAbility(pStack, "storm") || AbilityUtils.isAbilityOnCooldown(pStack, "storm")) {
                if (this.getUseDuration(pStack) - pTimeCharged > 19) {
                    if (!pLevel.isClientSide()) {
                        float fl = random.nextFloat();
                        pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), SoundsRegistry.KNEF_BOW_SHOT.get(), SoundSource.MASTER, 0.5f, 1.8f - fl * 0.15f);
                        pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), SoundsRegistry.KNEF_BOW_SHOT.get(), SoundSource.MASTER, 0.5f, 0.6f);
                    }
                    int count = (int) AbilityUtils.getAbilityValue(pStack, "shot", "rays");
                    Vec3 pos = pLivingEntity.getEyePosition(1f).add(pLivingEntity.getLookAngle().scale(0.3))
                            .add(pLivingEntity.getLookAngle()
                                    .cross( (pLivingEntity.getLookAngle().x < 0.001 && pLivingEntity.getLookAngle().z < 0.001) ?
                                            Vec3.directionFromRotation(0, pLivingEntity.getYHeadRot()).scale(pLivingEntity.getLookAngle().y > 0 ? -1 : 1).normalize() :
                                            new Vec3(0,1,0)
                                    ).normalize().scale(0.2)
                            )
                            .add(0, -0.13, 0).subtract(pLivingEntity.getLookAngle().scale(1.4));
                    KnefProjCarrier carrier = new KnefProjCarrier(EntityRegistry.KNEF_PROJECTILE_CARRIER, pLevel)
                            .setRays(
                                    KnefProjectile.makeList(count, pLevel, pLivingEntity, pos, pLivingEntity.getLookAngle().scale(0.3), pStack.getEnchantmentLevel(Enchantments.POWER_ARROWS), pStack)
                            );
                    carrier.setPos(pos);
                    carrier.setOwner(pLivingEntity);
                    carrier.shootFromRotation(pLivingEntity, pLivingEntity.getXRot(), pLivingEntity.getYRot(), 0.75f, 1f, 0);
                    pLevel.addFreshEntity(carrier);
                    for (KnefProjectile proj : carrier.rays) pLevel.addFreshEntity(proj);
                } else if (this.getUseDuration(pStack) - pTimeCharged > 5) {
                    if (!pLevel.isClientSide()) {
                        float fl = random.nextFloat();
                        pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), SoundsRegistry.KNEF_BOW_SHOT.get(), SoundSource.MASTER, 0.5f, 1.75f + fl * 0.1f);
                    }
                    Vec3 pos = pLivingEntity.getEyePosition(1f).add(pLivingEntity.getLookAngle().scale(0.3))
                            .add(pLivingEntity.getLookAngle()
                                    .cross( (pLivingEntity.getLookAngle().x < 0.001 && pLivingEntity.getLookAngle().z < 0.001) ?
                                            Vec3.directionFromRotation(0, pLivingEntity.getYHeadRot()).scale(pLivingEntity.getLookAngle().y > 0 ? -1 : 1).normalize() :
                                            new Vec3(0,1,0)
                                    ).normalize().scale(0.2)
                            )
                            .add(0, -0.13, 0).subtract(pLivingEntity.getLookAngle().scale(1.4));
                    KnefProjectile proj = new KnefProjectile(EntityRegistry.KNEF_PROJECTILE, pLevel);
                    proj.setPos(pos);
                    proj.setOwner(pLivingEntity);
                    proj.setPowerEnch(pStack.getEnchantmentLevel(Enchantments.POWER_ARROWS));
                    proj.setBow(pStack);
                    proj.shootFromRotation(pLivingEntity, pLivingEntity.getXRot(), pLivingEntity.getYRot(), 0.75f, 1f, 0);
                    pLevel.addFreshEntity(proj);
                }
            } else if (this.getUseDuration(pStack) - pTimeCharged > 19 && !AbilityUtils.isAbilityOnCooldown(pStack, "storm")/* && !pLevel.isClientSide()*/) {
                pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), SoundsRegistry.KNEF_BOW_SHOT.get(), SoundSource.MASTER, 0.7f, 0.6f);
                pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), SoundsRegistry.KNEF_BOW_SHOT.get(), SoundSource.MASTER, 0.6f, 0.3f);

                KnefStormcaller stormcaller = new KnefStormcaller(EntityRegistry.KNEF_STORMCALLER, pLevel);
                Vec3 pos = pLivingEntity.getEyePosition(1f).add(pLivingEntity.getLookAngle().scale(0.3))
                        .add(pLivingEntity.getLookAngle()
                                .cross( (pLivingEntity.getLookAngle().x < 0.001 && pLivingEntity.getLookAngle().z < 0.001) ?
                                        Vec3.directionFromRotation(0, pLivingEntity.getYHeadRot()).scale(pLivingEntity.getLookAngle().y > 0 ? -1 : 1).normalize() :
                                        new Vec3(0,1,0)
                                ).normalize().scale(0.2)
                        )
                        .add(0, -0.13, 0).subtract(pLivingEntity.getLookAngle().scale(1.4));
                stormcaller.setPos(pos);
                stormcaller.setOwner(pLivingEntity);
                stormcaller.shotPos = pos;
                stormcaller.setBow(pStack);
                stormcaller.setRays(
                        KnefProjectileSpecial.makeList(6, pLevel, pLivingEntity, pos, pLivingEntity.getLookAngle().scale(0.3))
                );
                stormcaller.shootFromRotation(pLivingEntity, pLivingEntity.getXRot(), pLivingEntity.getYRot(), 0.75f, 2.5f, 0);
                for (KnefProjectileSpecial proj : stormcaller.rays) pLevel.addFreshEntity(proj);
                pLevel.addFreshEntity(stormcaller);
                AbilityUtils.addAbilityCooldown(pStack, "storm", 600);
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event)
    {
        if(event.getEntity() instanceof Player p
                && p.isUsingItem()
                && p.getUseItem().getItem() instanceof ItemKnefBow bow
                && bow.isSurging
                && event.getSource() == DamageSource.FALL){
            event.setCanceled(true);
        }
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
        if ( (player.isEyeInFluidType(WATER_TYPE.get()) || isSurging) && player instanceof Player p) {

            if (!p.isCreative()) {
                if (player.getHealth() > 1)
                    player.setHealth(player.getHealth() - player.getMaxHealth() * (float)AbilityUtils.getAbilityValue(stack, "shot", "drain") * 0.02f);
                else player.kill();
            }
            player.hurtTime = 0;
            player.hurtDuration = 0;

            isSurging = player.isInWaterOrRain();
            player.setSwimming(false);
            Vec3 luk = p.getLookAngle();
            Vec3 motion = player.getDeltaMovement();
            double spid = AbilityUtils.getAbilityValue(stack, "swim", "speed") / 5;

            AABB aoe = player.getBoundingBox().inflate(2);
            for (LivingEntity target : player.getLevel().getEntitiesOfClass(LivingEntity.class, aoe, e -> !(e.equals(player) || e instanceof LocalPlayer))) {
                target.hurt(DamageSource.playerAttack(p), (float) AbilityUtils.getAbilityValue(stack, "swim", "dmg"));
                Vec3 awayctor = target.position().subtract(player.position()).subtract(motion);
                target.push(awayctor.x() * 1 / awayctor.length(), awayctor.y() * 1 / awayctor.length(), awayctor.z() * 1 / awayctor.length());
            }

            p.setDeltaMovement(0,0,0);
            p.push(luk.x() * spid, luk.y() * spid, luk.z() * spid);
            p.startAutoSpinAttack(2);
            for (int i = 0; i < 12; i++) {

                double a = 360.0 / 12 * i - count * 10.0;
                double radius = 0.7 + Math.sin(Math.toRadians(count * 20.0) - 90) * 0.44;

                if (i % 2 == 0) {
                    radius += 1.4;
                }

                Vec3 x = !( motion.normalize().x < 0.001 && motion.normalize().z < 0.001 ) ? motion.normalize().cross(new Vec3(0, 1, 0)).normalize().scale(radius) : motion.normalize().cross(new Vec3(1, 0, 0)).normalize().scale(radius);
                Vec3 z = motion.normalize().cross(x).normalize().scale(radius);

                Vec3 pos = player.getPosition(1F)
                        .add(x.scale(Math.cos(Math.toRadians(a))))
                        .add(z.scale(Math.sin(Math.toRadians(a))))
                        //.subtract(motion.scale((double) i / rays.size() * 2))
                        ;
                if (i % 2 == 0) {
                    pos = pos.add(luk.scale(3.4));
                    if (i % 4 == 0) pos = pos.subtract(luk.scale(0.8));
                }
                pos = pos.add(luk.scale(-0.4));
                player.getLevel().addParticle(new CircleTintData(new Color(0, (int) (140 + Math.sin(count / 6.0) * 100), (int) (215 - Math.sin(count / 6.0) * 40)), 0.35f, 60, 0.92f, false),
                        pos.x(), pos.y(), pos.z(), 0, 0, 0);
            }
        } else if(player instanceof Player p){
            if (this.getUseDuration(stack) - count < 19) {
                if (!p.isCreative()) {
                    if (player.getHealth() > 1)
                        player.setHealth(player.getHealth() - player.getMaxHealth() * (float)AbilityUtils.getAbilityValue(stack, "shot", "drain") * (isShitting ? 0.1f : 0.05f));
                    else player.kill();
                }
                player.hurtTime = 0;
                player.hurtDuration = 0;
            }

            if (!player.isCrouching() && isShitting) {
                isShitting = false;
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        pPlayer.startUsingItem(pHand);
        isShitting = pPlayer.isCrouching();
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            final Supplier<KnefBowItemRenderer> renderer = Suppliers.memoize(KnefBowItemRenderer::new);

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer.get();
            }
        });
    }


    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.category == EnchantmentCategory.BOW;
    }

    @Override
    public int getFoilColor(@NotNull ItemStack stack) {
        return /*0xFA9FEB7D*/ new Color(0, 26, 75).getRGB(); //хекс коды люблю невероятно
    }

}
