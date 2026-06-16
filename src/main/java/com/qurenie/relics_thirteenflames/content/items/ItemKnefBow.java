package com.qurenie.relics_thirteenflames.content.items;

import com.qurenie.api.IActivityContainer;
import com.qurenie.api.IBarContainer;
import com.qurenie.api.IExtRelicItem;
import com.qurenie.api.SettingsContainer;
import com.qurenie.relics_thirteenflames.activity.ActivitySetting;
import com.qurenie.relics_thirteenflames.activity.IActivitySetting;
import com.qurenie.relics_thirteenflames.activity.RelicActivitySetting;
import com.qurenie.relics_thirteenflames.client.bar.BarSetting;
import com.qurenie.relics_thirteenflames.client.bar.IBarSetting;
import com.qurenie.relics_thirteenflames.content.entities.KnefProjCarrier;
import com.qurenie.relics_thirteenflames.content.entities.KnefProjectile;
import com.qurenie.relics_thirteenflames.content.entities.KnefProjectileSpecial;
import com.qurenie.relics_thirteenflames.content.entities.KnefStormcaller;
import com.qurenie.relics_thirteenflames.init.*;
import com.qurenie.relics_thirteenflames.style.ColorScheme;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.items.misc.CreativeContentConstructor;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerlib.api.items.IColoredFoilItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.awt.*;
import java.util.Collections;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.*;

public class ItemKnefBow extends RelicItem implements IExtRelicItem, IColoredFoilItem, IActivityContainer, IBarContainer {

    public ItemKnefBow(Properties properties) {

        super(properties);
    }

    RandomSource random = RandomSource.create();

    @Override
    public void gatherCreativeTabContent(CreativeContentConstructor constructor) {
    }

    @Override
    public SettingsContainer<IBarSetting> constructBarSettings() {
        return SettingsContainer.<IBarSetting>builder()
                .setting(BarSetting.builder()
                        .color(ColorScheme.BAR_BLUE)
                        .maxValue((stack, player) -> getStatValue(player, stack, "swim", "charge"))
                        .value((s, p) -> Double.valueOf(s.getOrDefault(ComponentRegistry.ACTIVE_TICK, 0)))
                        .inverse(true)
                        .visibility((s, p) -> s.getOrDefault(ComponentRegistry.ACTIVE_TICK, 0) > 0)
                        .build())
                .build();
    }

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("shot")
                                .initialMaxLevel(10)
                                .stat(AbilityStatTemplate.builder("rays")
                                        .initialValue(3, 3)
                                        .thresholdValue(3, 40)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(),  40)
                                        .formatValue(x -> (int) MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("dmg")
                                        .initialValue(3, 4)
                                        .thresholdValue(3, 20)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 20)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("drain")
                                        .initialValue(0.35, 0.2)
                                        .thresholdValue(0.1, 0.35)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 0.1)
                                        .formatValue(x -> MathUtils.round(x * 100, 1))
                                        .build()
                                )
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_1")
                                        .build())
                                .rankModifier(2, "bounce")
                                .build()
                        )
                        .ability(AbilityTemplate.builder("swim")
                                .initialMaxLevel(5)
                                .modes("on", "off")
                                .research(ResearchTemplate.builder()
                                        .star(0, 3, 8).star(1, 12, 4).star(2, 18, 8).star(3, 10, 13).star(4, 6, 17).star(5, 15, 17).star(6, 18, 14).star(7, 9, 23).star(8, 11, 26).star(9, 3, 13).star(10, 8, 10).star(11, 11, 8)
                                        .link(0, 1).link(1, 2).link(2, 3).link(3, 4).link(5, 6).link(5, 4).link(5, 7).link(7, 8).link(9, 4).link(0, 10).link(10, 11)
                                        .build())
                                .stat(AbilityStatTemplate.builder("speed")
                                        .initialValue(4, 6)
                                        .thresholdValue(4, 15)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 15)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("dmg")
                                        .initialValue(6, 8)
                                        .thresholdValue(6, 20)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 20)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("charge")
                                        .initialValue(30, 50)
                                        .thresholdValue(40, 200)
                                        .targetValue(RelicsScalingModels.EXPONENTIAL.get(), 200)
                                        .formatValue(x -> MathUtils.round(x / 20, 1))
                                        .build())
                                .rankModifier(1, "upgrade")
                                .build()
                        )
                        .ability(AbilityTemplate.builder("storm")
                                .requiredLevel(10)
                                .initialMaxLevel(5)
                                .stat(AbilityStatTemplate.builder("radius")
                                        .initialValue(4.0, 5.0)
                                        .thresholdValue(4, 20)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 20)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("dur")
                                        .initialValue(11, 16)
                                        .thresholdValue(11, 32)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 32)
                                        .formatValue(x -> (int) MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("dmg")
                                        .initialValue(6, 8)
                                        .thresholdValue(6, 20)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 20)
                                        .formatValue(x -> (int) MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("heal")
                                        .initialValue(2, 3)
                                        .thresholdValue(2, 10)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 10)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_2")
                                        .build())
                                .build()
                        )
                        .build()
                )
                .leveling(LevelingTemplate.builder()
                        .maxRank(2)
                        .step(100)
                        .initialCost(100)
                        .build())
                .loot(LootTemplate.builder().entry(LootEntries.AQUATIC).build())
                .build();
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean canContinueUsing(@NotNull ItemStack oldStack, @NotNull ItemStack newStack) {
        return true;
    }

    @Override
    public SettingsContainer<IActivitySetting> constructActivitySettings() {
        return SettingsContainer.<IActivitySetting>builder()
                .setting(ActivitySetting.builder("storm")
                        .maxCooldown(1200)
                        .build())
                .build();
    }
    //private boolean isSurging = false;

    @Override
    public void releaseUsing(@NotNull ItemStack pStack, @NotNull Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        if (!(pLivingEntity instanceof Player) || (isModEnabled(pLivingEntity, pStack, "swim", "on") && pLivingEntity.isInWaterOrRain()))
            return;

        int delta = this.getUseDuration(pStack, pLivingEntity) - pTimeCharged;
        if (hasGloves(pLivingEntity))
            delta *= 2;

        float baseDmg = (float) getStatValue(pLivingEntity, pStack, "shot", "dmg");
        boolean isShitting = pLivingEntity.isShiftKeyDown();
        if (!isShitting || !isAbilityUnlocked(pLivingEntity, pStack, "storm") || !canCast(pLivingEntity, pStack, "storm")) {
            if (delta > 19) {
                if (!pLevel.isClientSide()) {
                    float fl = random.nextFloat();
                    pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), SoundsRegistry.KNEF_BOW_SHOT.get(), SoundSource.MASTER, 0.5f, 1.8f - fl * 0.15f);
                    pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), SoundsRegistry.KNEF_BOW_SHOT.get(), SoundSource.MASTER, 0.5f, 0.6f);
                }
                int count = (int) getStatValue(pLivingEntity, pStack, "shot", "rays");

                Vec3 pos = pLivingEntity.getEyePosition(1f).add(pLivingEntity.getLookAngle().scale(1.6))
                        .add(pLivingEntity.getLookAngle()
                                .cross((pLivingEntity.getLookAngle().x < 0.001 && pLivingEntity.getLookAngle().z < 0.001) ?
                                        Vec3.directionFromRotation(0, pLivingEntity.getYHeadRot()).scale(pLivingEntity.getLookAngle().y > 0 ? -1 : 1).normalize() :
                                        new Vec3(0, 1, 0)
                                ).normalize().scale(0.2)
                        )
                        .add(0, -0.13, 0).subtract(pLivingEntity.getLookAngle().scale(1.4));
                KnefProjCarrier carrier = new KnefProjCarrier(EntityRegistry.KNEF_PROJECTILE_CARRIER, pLevel)
                        .setRays(
                                KnefProjectile.makeList(count, pLevel, pLivingEntity, pos, pLivingEntity.getLookAngle().scale(0.3), baseDmg,
                                        pStack.getEnchantmentLevel(pLevel.holderOrThrow(Enchantments.POWER)), pStack)
                        );
                carrier.setPos(pos);
                carrier.setOwner(pLivingEntity);
                carrier.setOwnerUUID(pLivingEntity.getStringUUID());
                carrier.shootFromRotation(pLivingEntity, pLivingEntity.getXRot(), pLivingEntity.getYRot(), 0.75f, 1f, 0);
                pLevel.addFreshEntity(carrier);
                for (KnefProjectile proj : carrier.rays) pLevel.addFreshEntity(proj);
            } else if (delta > 5) {
                if (!pLevel.isClientSide()) {
                    float fl = random.nextFloat();
                    pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), SoundsRegistry.KNEF_BOW_SHOT.get(), SoundSource.MASTER, 0.5f, 1.75f + fl * 0.1f);
                }
                Vec3 pos = pLivingEntity.getEyePosition(1f).add(pLivingEntity.getLookAngle().scale(1.6))
                        .add(pLivingEntity.getLookAngle()
                                .cross((pLivingEntity.getLookAngle().x < 0.001 && pLivingEntity.getLookAngle().z < 0.001) ?
                                        Vec3.directionFromRotation(0, pLivingEntity.getYHeadRot()).scale(pLivingEntity.getLookAngle().y > 0 ? -1 : 1).normalize() :
                                        new Vec3(0, 1, 0)
                                ).normalize().scale(0.2)
                        )
                        .add(0, -0.13, 0).subtract(pLivingEntity.getLookAngle().scale(1.4));
                KnefProjectile proj = new KnefProjectile(EntityRegistry.KNEF_PROJECTILE, pLevel);
                proj.setPos(pos);
                proj.setOwner(pLivingEntity);
                proj.setOwnerUUID(pLivingEntity.getStringUUID());
                proj.setBaseDmg(baseDmg);
                proj.setPowerEnch(pStack.getEnchantmentLevel(pLevel.holderOrThrow(Enchantments.POWER)));
                proj.setBow(pStack);
                proj.shootFromRotation(pLivingEntity, pLivingEntity.getXRot(), pLivingEntity.getYRot(), 0.75f, 1f, 0);
                proj.setBounce(ItemsRegistry.KNEF_BOW.hasRangModifier(pLivingEntity, pStack, "shot", "bounce"));
                proj.setFree(true);
                pLevel.addFreshEntity(proj);
            }
        } else if (delta > 19 && canCast(pLivingEntity, pStack, "storm")/* && !pLevel.isClientSide()*/) {
            pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), SoundsRegistry.KNEF_BOW_SHOT.get(), SoundSource.MASTER, 0.7f, 0.6f);
            pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), SoundsRegistry.KNEF_BOW_SHOT.get(), SoundSource.MASTER, 0.6f, 0.3f);

            KnefStormcaller stormcaller = new KnefStormcaller(EntityRegistry.KNEF_STORMCALLER, pLevel);
            Vec3 pos = pLivingEntity.getEyePosition(1f).add(pLivingEntity.getLookAngle().scale(1.6))
                    .add(pLivingEntity.getLookAngle()
                            .cross((pLivingEntity.getLookAngle().x < 0.001 && pLivingEntity.getLookAngle().z < 0.001) ?
                                    Vec3.directionFromRotation(0, pLivingEntity.getYHeadRot()).scale(pLivingEntity.getLookAngle().y > 0 ? -1 : 1).normalize() :
                                    new Vec3(0, 1, 0)
                            ).normalize().scale(0.2)
                    )
                    .add(0, -0.13, 0).subtract(pLivingEntity.getLookAngle().scale(1.4));
            stormcaller.setPos(pos);
            stormcaller.setOwner(pLivingEntity);
            stormcaller.setOwnerUUID(pLivingEntity.getStringUUID());
            stormcaller.shotPos = pos;
            stormcaller.prevPos = pos;
            stormcaller.setBow(pStack);
            stormcaller.setRays(
                    KnefProjectileSpecial.makeList(6, pLevel, pLivingEntity, pos, pLivingEntity.getLookAngle().scale(0.3))
            );
            stormcaller.shootFromRotation(pLivingEntity, pLivingEntity.getXRot(), pLivingEntity.getYRot(), 0.75f, 2.5f, 0);
            for (KnefProjectileSpecial proj : stormcaller.rays) pLevel.addFreshEntity(proj);
            pLevel.addFreshEntity(stormcaller);
            setMaxCooldown(pLivingEntity, pStack, "storm");
        } else if (delta > 5) {
            if (!pLevel.isClientSide()) {
                float fl = random.nextFloat();
                pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), SoundsRegistry.KNEF_BOW_SHOT.get(), SoundSource.MASTER, 0.5f, 1.75f + fl * 0.1f);
            }
            Vec3 pos = pLivingEntity.getEyePosition(1f).add(pLivingEntity.getLookAngle().scale(1.6))
                    .add(pLivingEntity.getLookAngle()
                            .cross((pLivingEntity.getLookAngle().x < 0.001 && pLivingEntity.getLookAngle().z < 0.001) ?
                                    Vec3.directionFromRotation(0, pLivingEntity.getYHeadRot()).scale(pLivingEntity.getLookAngle().y > 0 ? -1 : 1).normalize() :
                                    new Vec3(0, 1, 0)
                            ).normalize().scale(0.2)
                    )
                    .add(0, -0.13, 0).subtract(pLivingEntity.getLookAngle().scale(1.4));
            KnefProjectile proj = new KnefProjectile(EntityRegistry.KNEF_PROJECTILE, pLevel);
            proj.setPos(pos);
            proj.setOwner(pLivingEntity);
            proj.setOwnerUUID(pLivingEntity.getStringUUID());
            proj.setBaseDmg(baseDmg);
            proj.setBounce(ItemsRegistry.KNEF_BOW.hasRangModifier(pLivingEntity, pStack, "shot", "bounce"));
            proj.setPowerEnch(pStack.getEnchantmentLevel(pLevel.holderOrThrow(Enchantments.POWER)));
            proj.setBow(pStack);
            proj.setFree(true);
            proj.shootFromRotation(pLivingEntity, pLivingEntity.getXRot(), pLivingEntity.getYRot(), 0.75f, 1f, 0);
            pLevel.addFreshEntity(proj);
        }
        pStack.set(SHIFTING, false);
    }

    private static boolean hasGloves(Entity entity) {
        return entity instanceof LivingEntity living && CuriosApi.getCuriosInventory(living).map(handler -> {
            var stacks = handler.getCurios().get("hands").getStacks();
            return stacks.getStackInSlot(0).is(ItemsRegistry.MONTU_GLOVES) || stacks.getStackInSlot(0).is(ItemsRegistry.MONTU_GLOVES);
        }).orElse(false);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slot, boolean isSelected) {
        boolean hasGloves = hasGloves(entity);
        if (!level.isClientSide() && entity instanceof LivingEntity l
                && stack.getOrDefault(PULL, 0f) != (l.getUseItem() == stack ? (float) (stack.getUseDuration(l) - l.getUseItemRemainingTicks()) / (hasGloves ? 10.0f : 20.0F) : 0))
            stack.set(PULL, l.getUseItem() == stack ? (float) (stack.getUseDuration(l) - l.getUseItemRemainingTicks()) / (hasGloves ? 10.0f : 20.0F) : 0);

        int cooldown = stack.getOrDefault(COOLDOWN, 0);

        int activeTick = stack.getOrDefault(ACTIVE_TICK, 0);
        if (cooldown == 0) {
            stack.set(ACTIVE_TICK, Math.max(activeTick - 3, 0));
        } else
            stack.set(COOLDOWN, cooldown - 1);

        super.inventoryTick(stack, level, entity, slot, isSelected);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack pStack, @NotNull LivingEntity entity) {
        return 72000;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }


    @Override
    public void onUseTick(@NotNull Level level, final @NotNull LivingEntity living, @NotNull ItemStack stack, int count) {

        if (isModEnabled(living, stack, "swim", "on") && living.isInWaterOrRain() && living instanceof Player p) {

            int activeTick = stack.getOrDefault(ACTIVE_TICK, 0);
            var maxCharge = getStatValue(living, stack, "swim", "charge");

            if (activeTick >= maxCharge)
                return;

            stack.set(ACTIVE_TICK, activeTick + 1);
            stack.set(COOLDOWN, 50);

            if (!p.isCreative()) {
                if (living.getHealth() > 1)
                    living.hurt(DamageSourceRegistry.SUCC, living.getMaxHealth() * (float) getStatValue(living, stack, "shot", "drain") * 0.02f);
                else living.kill();
            }

            living.hurtTime = 0;
            living.hurtDuration = 0;


            living.setSwimming(false);
            Vec3 luk = p.getLookAngle();
            Vec3 motion = living.getDeltaMovement();
            double speedd = getStatValue(living, stack, "swim", "speed") / 5;

            AABB aoe = living.getBoundingBox().inflate(2);
            for (LivingEntity target : living.level().getEntitiesOfClass(LivingEntity.class, aoe, e -> !e.getUUID().equals(living.getUUID()))) {
                target.hurt(p.damageSources().playerAttack(p), (float) getStatValue(living, stack, "swim", "dmg"));
                Vec3 awayctor = target.position().subtract(living.position()).subtract(motion);
                target.push(awayctor.x() * 1 / awayctor.length(), awayctor.y() * 1 / awayctor.length(), awayctor.z() * 1 / awayctor.length());
            }

            var aabb = aoe.inflate(10);
            var entities = living.level().getEntitiesOfClass(
                    LivingEntity.class,
                    aabb,
                    e -> !e.getUUID().equals(living.getUUID())
            );
            Collections.shuffle(entities);

            if (ItemsRegistry.KNEF_BOW.hasRangModifier(living, stack, "swim", "upgrade")
                && living.tickCount % 7 == 0)
                entities.stream().limit(Math.max(3, entities.size())).forEach(target -> {
                    // upgrade modifier

                    long gameTime = level.getGameTime();
                    long lastShot = target.getPersistentData().getLong("knef_swim_proj");

                    // anti-spam
                    if (gameTime - lastShot >= 8) {

                        target.getPersistentData().putLong("knef_swim_proj", gameTime);

                        Vec3 from = living.getEyePosition();
                        Vec3 to = target.getBoundingBox().getCenter();

                        Vec3 dir = to.subtract(from).normalize();

                        KnefProjectile proj = new KnefProjectile(
                                EntityRegistry.KNEF_PROJECTILE,
                                level
                        );

                        proj.setPos(
                                from.x + dir.x * 1.2,
                                from.y + dir.y * 1.2,
                                from.z + dir.z * 1.2
                        );

                        proj.setOwner(living);
                        proj.setOwnerUUID(living.getStringUUID());

                        proj.setBaseDmg(
                                (float) getStatValue(living, stack, "shot", "dmg")
                        );

                        proj.setPowerEnch(
                                stack.getEnchantmentLevel(
                                        level.holderOrThrow(Enchantments.POWER)
                                )
                        );

                        proj.setBow(stack);

                        proj.setBounce(
                                ItemsRegistry.KNEF_BOW.hasRangModifier(
                                        living,
                                        stack,
                                        "shot",
                                        "bounce"
                                )
                        );

                        proj.setFree(true);

                        proj.shoot(
                                dir.x,
                                dir.y,
                                dir.z,
                                1.6f,
                                0f
                        );

                        level.addFreshEntity(proj);
                    }

                });

            p.setDeltaMovement(0, 0, 0);
            p.push(luk.x() * speedd, luk.y() * speedd, luk.z() * speedd);
            p.startAutoSpinAttack(2, (float) getStatValue(living, stack, "swim", "dmg"), stack);
            p.fallDistance = 0;
            for (int i = 0; i < 12; i++) {

                double a = 360.0 / 12 * i - p.tickCount * 10.0;
                double radius = 0.7 + Math.sin(Math.toRadians(p.tickCount * 20.0) - 90) * 0.44;

                if (i % 2 == 0) {
                    radius += 1.4;
                }

                Vec3 x = !(motion.normalize().x < 0.001 && motion.normalize().z < 0.001) ? motion.normalize().cross(new Vec3(0, 1, 0)).normalize().scale(radius) : motion.normalize().cross(new Vec3(1, 0, 0)).normalize().scale(radius);
                Vec3 z = motion.normalize().cross(x).normalize().scale(radius);

                Vec3 pos = living.getPosition(1F)
                        .add(x.scale(Math.cos(Math.toRadians(a))))
                        .add(z.scale(Math.sin(Math.toRadians(a))));

                if (i % 2 == 0) {
                    pos = pos.add(luk.scale(3.4));
                    if (i % 4 == 0) pos = pos.subtract(luk.scale(0.8));
                }
                pos = pos.add(luk.scale(-0.4));
                ParticleHelper.spawnDirectedParticle(living.level(), ParticleHelper.constructSimpleSpark(FlamesUtils.fromRGBI(0, (int) (174 + Math.sin(p.tickCount / 6.0) * 30), (int) (105 - Math.sin(count / 6.0) * 20)), 0.35f, 60, 0.92f),
                        pos.x(), pos.y(), pos.z(), 0, 0, 0);
            }
        } else if (living instanceof Player p) {
            boolean hasGloves = hasGloves(living);
            if (this.getUseDuration(stack, p) - count < (hasGloves ? 19 : 9)) {
                if (!p.isCreative()) {
                    if (living.getHealth() > 1) {
                        boolean isShitting = stack.getOrDefault(SHIFTING, false);
                        living.hurt(DamageSourceRegistry.SUCC, living.getMaxHealth() * (float) getStatValue(living, stack, "shot", "drain") * (isShitting ? 0.1f : 0.05f) * (hasGloves ? 1.5f : 1));
                    } else living.kill();
                }
                living.hurtTime = 0;
                living.hurtDuration = 0;
            }

        }
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player pPlayer, @NotNull InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        itemstack.set(SHIFTING, pPlayer.isShiftKeyDown());
        pPlayer.startUsingItem(pHand);
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public boolean isPrimaryItemFor(@NotNull ItemStack stack, Holder<Enchantment> enchantment) {
        return enchantment.is(Enchantments.POWER);
    }

    @Override
    public boolean supportsEnchantment(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
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
    public int getFoilColor(@NotNull ItemStack stack) {
        return /*0xFA9FEB7D*/ new Color(0, 133, 108).getRGB();
    }

    @EventBusSubscriber
    public static class EventHandler {

        @SubscribeEvent
        public static void onItemUseEvent(LivingEntityUseItemEvent.Tick event) {
            if (event.getItem().is(ItemsRegistry.KNEF_BOW)
                    && event.getEntity().isInWaterOrRain() &&
                    event.getEntity() instanceof Player player &&
                    ItemsRegistry.KNEF_BOW.isModEnabled(player, event.getItem(), "swim", "on")) {
                event.setDuration(event.getItem().getUseDuration(event.getEntity()));
            }
        }

    }

}
