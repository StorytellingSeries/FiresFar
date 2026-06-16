package com.qurenie.relics_thirteenflames.content.items;

import com.qurenie.api.IActivityContainer;
import com.qurenie.api.IExtRelicItem;
import com.qurenie.api.SettingsContainer;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.activity.ActivitySetting;
import com.qurenie.relics_thirteenflames.activity.IActivitySetting;
import com.qurenie.relics_thirteenflames.activity.RelicActivitySetting;
import com.qurenie.relics_thirteenflames.activity.call.settings.ActivityCallSettings;
import com.qurenie.relics_thirteenflames.activity.call.settings.ActivityResult;
import com.qurenie.relics_thirteenflames.activity.call.settings.InventoryType;
import com.qurenie.relics_thirteenflames.activity.call.settings.RelicsActivityCallSettings;
import com.qurenie.relics_thirteenflames.client.particles.FeatherParticle;
import com.qurenie.relics_thirteenflames.client.render.entity.IJodahGlowed;
import com.qurenie.relics_thirteenflames.client.render.misc.JodahStaffRenderUtil;
import com.qurenie.relics_thirteenflames.content.entities.JodahHealEntity;
import com.qurenie.relics_thirteenflames.content.entities.JodahMarkEntity;
import com.qurenie.relics_thirteenflames.content.items.misc.JodahTier;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.style.ColorScheme;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.octostudios.octolib.util.OctoColor;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootEntry;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.api.fml.IRegisterListener;
import org.zeith.hammerlib.util.charging.ItemChargeHelper;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.*;
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

public class ItemJodahStaff extends SwordItem implements IActivityContainer, IExtRelicItem, IRelicItem, IRegisterListener {

    public static final int DURATION = 72000;
    private static final OctoColor PURPLE_COLOR = FlamesUtils.fromRGBI(160, 20, 140);

    public static final LootEntry END_LIKE = LootEntry.builder().dimension(".*").biome(".*").table("[\\w]+:chests\\/[\\w_\\/]*(end)[\\w_\\/]*").weight(800).build();

    public ItemJodahStaff(Tier tier, Properties properties) {
        super(tier, properties);
    }

    private static boolean isWalkable(Level level, Player player, Vec3 targetPos) {
        if (player.isSpectator())
            return true;

        AABB playerBox = player.getBoundingBox();
        AABB movedBox = playerBox.move(targetPos.x - player.getX(), targetPos.y - player.getY(), targetPos.z - player.getZ()).deflate(0.3);
        return level.noCollision(movedBox);
    }

    @Nullable
    private static Vec3 getWalkableNearTarget(Level level, Player player, LivingEntity entity) {
        if (player.isSpectator())
            return entity.position();

        if (isWalkable(player.level(), player, entity.position()))
            return entity.position();

        if (isWalkable(player.level(), player, entity.position().subtract(0, 1, 0)))
            return entity.position().subtract(0, 1, 0);

        Vec3 inFrontOf = player.getLookAngle().multiply(1, 0, 0).normalize();
        if (isWalkable(player.level(), player, entity.position().subtract(inFrontOf)))
            return entity.position().subtract(inFrontOf);

        if (isWalkable(player.level(), player, entity.position().add(0, 1, 0)))
            return entity.position().add(0, 1, 0);

        return null;
    }

    @Override
    public void onPostRegistered(ResourceLocation id) {
        EVENT_BUS.register(this);
    }

    @Override
    public boolean isDamageable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder().abilities(AbilitiesTemplate.builder()
                                .ability(AbilityTemplate.builder("ranging")
                                        .initialMaxLevel(4)
                                        .stat(AbilityStatTemplate.builder("increasing")
                                                .initialValue(0, 1)
                                                .targetValue(RelicsScalingModels.ADDITIVE.get(), 5)
                                                .thresholdValue(0, 4)
                                                .build()
                                        )
                                        .stat(AbilityStatTemplate.builder("damage_modifier")
                                                .initialValue(1, 1.2)
                                                .targetValue(RelicsScalingModels.EXPONENTIAL.get(), 4)
                                                .thresholdValue(1, 4)
                                                .formatValue(d -> MathUtils.round(d * 100, 0))
                                                .build()
                                        )
                                        .stat(AbilityStatTemplate.builder("xp_consume")
                                                .initialValue(1, 1.15)
                                                .targetValue(RelicsScalingModels.EXPONENTIAL.get(), 10)
                                                .thresholdValue(1, 10)
                                                .formatValue(d -> MathUtils.round(100 - 100 / d, 0))
                                                .build()
                                        )
                                        .rankModifier(1, "mercy")
                                        .experienceSources(ExperienceSourcesTemplate.builder()
                                                .source("source_1")
                                                .build())
                                        .build()
                                )
                                .ability(AbilityTemplate.builder("health_theft")
                                                .requiredPoints(1)
                                                .initialMaxLevel(3)
                                                .research(ResearchTemplate.builder()
                                                        .star(0, 3, 11).star(1, 6, 15).star(2, 9, 11).star(3, 16, 8).star(4, 13, 9).star(5, 14, 12).star(6, 17, 11).star(7, 10, 23).star(8, 10, 17).star(9, 8, 6).star(10, 16, 16)
                                                        .link(0, 1).link(1, 2).link(4, 5).link(5, 6).link(6, 3).link(4, 3).link(1, 8).link(8, 7).link(8, 5).link(0, 9).link(10, 7)
                                                        .build())
//                                .active(CastData.builder()
//                                        .container(RelicContainerRegistry.INVENTORY.get())
//                                        .type(CastType.INSTANTANEOUS)
//                                        .predicate("health_theft_predicate", PredicateType.VISIBILITY, (p, s) -> p.getMainHandItem() == s || p.getOffhandItem() == s)
//                                        .build())
                                                .stat(AbilityStatTemplate.builder("recharge")
                                                        .initialValue(1600, 1400)
                                                        .thresholdValue(500, 1600)
                                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 500)
                                                        .formatValue(d -> MathUtils.round(d / 20, 1))
                                                        .build()
                                                )
                                                .stat(AbilityStatTemplate.builder("entity_count")
                                                        .initialValue(1, 1.5)
                                                        .thresholdValue(1, 5)
                                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 5)
                                                        .formatValue(d -> MathUtils.round(Math.floor(d), 0))
                                                        .build()
                                                )
                                                .stat(AbilityStatTemplate.builder("xp_modifier")
                                                        .initialValue(1, 1.15)
                                                        .targetValue(RelicsScalingModels.EXPONENTIAL.get(), 4)
                                                        .thresholdValue(1, 4)
                                                        .formatValue(d -> MathUtils.round((d - 1) * 100, 0))
                                                        .build()
                                                )
                                                .stat(AbilityStatTemplate.builder("durability")
                                                        .initialValue(4, 8)
                                                        .thresholdValue(4, 30)
                                                        .targetValue(RelicsScalingModels.EXPONENTIAL.get(), 30)
                                                        .formatValue(d -> MathUtils.round(d, 1))
                                                        .build()
                                                )
                                                .rankModifier(2, "shield")
                                                .experienceSources(ExperienceSourcesTemplate.builder()
                                                        .source("source_2")
                                                        .build())
                                                .build()
                                )
                                .ability(AbilityTemplate.builder("one_thousand_eyes")
                                        .requiredPoints(2)
                                        .requiredLevel(7)
                                        .initialMaxLevel(1)
                                        .experienceSources(ExperienceSourcesTemplate.builder()
                                                .source("source_3")
                                                .build())
                                        .stat(AbilityStatTemplate.builder("time_add")
                                                .initialValue(3, 10)
                                                .thresholdValue(3, 20)
                                                .targetValue(RelicsScalingModels.ADDITIVE.get(), 20)
                                                .formatValue(d -> MathUtils.round(d, 1))
                                                .build()
                                        )
                                        .stat(AbilityStatTemplate.builder("recharge")
                                                .initialValue(2700, 2200)
                                                .thresholdValue(0, 2400)
                                                .targetValue(RelicsScalingModels.ADDITIVE.get(), 300)
                                                .formatValue(d -> MathUtils.round(d / 20, 1))
                                                .build()
                                        )
                                        .rankModifier(2, "mark")
                                        .rankModifier(3, "infinity")
                                        .build()
                                )
                                .build()
                )
                .leveling(LevelingTemplate.builder()
                        .maxRank(3)
                        .initialCost(100)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder().entry(END_LIKE).build())
                .build();
    }

    @Override
    public SettingsContainer<IActivitySetting> constructActivitySettings() {
        return SettingsContainer.<IActivitySetting>builder()
                .setting(ActivitySetting.builder("mark")
                        .color(ColorScheme.PURPLE_COLOR)
                        .maxCooldown(0)
                        .callSettings(ActivityCallSettings.builder()
                                .inventoryType(InventoryType.IN_HAND)
                                .visibility((l, s) -> s.getOrDefault(JODAH_ACTIVE_TICK, 0) > 0
                                        && hasRangModifier(l, s, "one_thousand_eyes", "mark"))
                                .cast(this::spawnMark)
                                .resourceLocation((l, s) -> ThirteenFlames.rl("textures/gui/abilities/jodah_mark.png"))
                                .build())
                        .build())
                .setting(RelicActivitySetting.builderRelic("health_theft", "recharge")
                        .color(ColorScheme.BAR_RED)
                        .build())
                .setting(RelicActivitySetting.builderRelic("one_thousand_eyes")
                        .color(ColorScheme.BAR_PURPLE)
                        .maxCooldown(1200)
                        .callSettings(RelicsActivityCallSettings.builder("one_thousand_eyes")
                                .inventoryType(InventoryType.IN_HAND)
                                .minVisibilityLevel(1)
                                .cast((living, stack) -> {
                                    if (!living.level().isClientSide)
                                        living.level().getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(40), e -> e != living && e.isPickable() && !e.isDeadOrDying())
                                                .forEach(e -> e.addEffect(new MobEffectInstance(EffectsRegistry.JODAH_VISION, getJodahVisionDuration(living, stack),
                                                        stack.getOrDefault(ComponentRegistry.JODAH_TIER, JodahTier.D).oneThousandEyes.targetingCount() - 1)));
                                    stack.set(JODAH_ACTIVE_TICK, getJodahVisionDuration(living, stack));
                                    setMaxCooldown(living, stack, "one_thousand_eyes");

                                    if (!living.level().isClientSide() && living instanceof Player player)
                                        FlamesUtils.startJodahWings(player, true);

                                    return ActivityResult.SUCCESS;
                                })
                                .build())
                        .build())
                .build();
    }

    private int getJodahVisionDuration(LivingEntity living, ItemStack stack) {
        return 500 + (hasRangModifier(living, stack, "one_thousand_eyes", "mark")
                ? (int) getStatValue(living, stack, "one_thousand_eyes", "time_add") * 20 : 0);
    }

    public ActivityResult spawnMark(LivingEntity living, ItemStack stack) {
        var level = living.level();

        if (!level.noCollision(living)) {
            return ActivityResult.FAILURE;
        }

        JodahMarkEntity mark = new JodahMarkEntity(living.level(), living, living.position());
        living.level().addFreshEntity(mark);
        stack.set(ENTITY_UUID, mark.getUUID());

        return ActivityResult.SUCCESS;
    }

    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (level.isClientSide || !(entity instanceof Player living))
            return;

        int activeTick = stack.getOrDefault(ACTIVE_TICK, 0);
        if (activeTick > 0) {
            stack.set(ACTIVE_TICK, activeTick - 1);
            if (entity.tickCount % 20 == 0 && (living.getMainHandItem() == stack || living.getOffhandItem() == stack))
                useThiefPower(stack, level, living);
        }

        activeTick = stack.getOrDefault(JODAH_ACTIVE_TICK, 0);
        var e = Optional.ofNullable(stack.get(ENTITY_UUID)).map(((ServerLevel) level)::getEntity).orElse(null);

        if (activeTick == 1 && e instanceof JodahMarkEntity mark)
            mark.teleportToMark(living);

        if (activeTick > 0) {
            stack.set(JODAH_ACTIVE_TICK, activeTick - 1);
        }
    }

    @Override
    public <T extends LivingEntity> int damageItem(@NotNull ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        return 0;
    }

    private void useThiefPower(@NotNull ItemStack stack, @NotNull Level level, Player living) {
        JodahTier tier = stack.getOrDefault(ComponentRegistry.JODAH_TIER, JodahTier.D);
        AABB aabb = living.getBoundingBox().inflate(tier.thief.radius());
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, aabb, t -> t != living
                        && t.isAlive() && !t.isSpectator() && t.isPickable())
                .stream()
                .filter(l -> l.distanceToSqr(living.getBoundingBox().getCenter()) <= tier.thief.radius() * tier.thief.radius())
                .sorted(Comparator.comparingDouble(l -> l.distanceToSqr(living)))
                .limit((int) getStatValue(living, stack, "health_theft", "entity_count"))
                .toList();

        for (LivingEntity target : targets) {
            int xp = (int) (tier.thief.xp() * getStatValue(living, stack, "health_theft", "xp_modifier"));
            if (target instanceof Player targetPlayer) {
                xp = Math.min(targetPlayer.totalExperience, xp);
                targetPlayer.giveExperiencePoints(-xp);
            }
            addExperience(living, stack, 2);
            var heal = new JodahHealEntity(EntityRegistry.JODAH_HEAL, level, Math.min(tier.thief.health(), target.getHealth()), xp, living, target);
            heal.setGainShield(hasRangModifier(living, stack, "health_theft", "shield"));
            level.addFreshEntity(heal);
            target.hurt(level.damageSources().wither(), tier.thief.health());
        }
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        MutableComponent component = MutableComponent.create(super.getName(stack).getContents());
        JodahTier tier = stack.getOrDefault(ComponentRegistry.JODAH_TIER, JodahTier.D);
        component.append(" [").append(tier.name()).append("]");
        return component;
    }

    @Override
    public boolean onLeftClickEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull Entity entity) {
        boolean result = super.onLeftClickEntity(stack, player, entity);
        if (player.level().isClientSide) {
            HitResult hitResult = Minecraft.getInstance().hitResult;

            if (hitResult != null) {
                JodahTier tier = stack.getOrDefault(ComponentRegistry.JODAH_TIER, JodahTier.D);
                int xp = player.isCreative() ? tier.xpSuck : Math.min(tier.xpSuck, player.totalExperience);
                if (xp == 0)
                    return result;

                int pxp = xp - xp / 2;

                ParticleHelper.spawnParticles(player.level(), ParticleHelper.constructFigure(new OctoColor(0xFFAAAAAA), 0.12f,
                        30, 0.96f).withGravity(1f), hitResult.getLocation(), pxp, 0.05, 0.05, 0.05, 0.03);
                ParticleHelper.spawnParticles(player.level(), ParticleHelper.constructSimpleSpark(PURPLE_COLOR, 0.3f,
                        30, 0.96f).withGravity(1f), hitResult.getLocation(), xp - pxp, 0.05, 0.05, 0.05, 0.03);
            }
        }

        return result;
    }

    @Override
    public void postHurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);

        if (stack.is(this)) {
            JodahTier tier = stack.getOrDefault(ComponentRegistry.JODAH_TIER, JodahTier.D);
            int bonus = (int) getStatValue(attacker, stack, "ranging", "increasing");
            int level = stack.getOrDefault(ComponentRegistry.LEVEL, 0) + 1;

            if (level + bonus >= tier.hitCount) {
                stack.set(ComponentRegistry.JODAH_TIER, tier.next());
                stack.set(ComponentRegistry.LEVEL, 0);
            } else {
                stack.set(ComponentRegistry.LEVEL, level);
            }

            float fineReduce = (int) getStatValue(attacker, stack, "ranging", "xp_consume");
            if ((attacker instanceof Player p)) {
                int reduce = Math.min(p.totalExperience, (int) (tier.xpSuck / fineReduce));
                p.giveExperiencePoints(-reduce);
                addExperience(attacker, stack, (int) (tier.xpSuck * (reduce / Math.floor(tier.xpSuck / fineReduce))));
            }
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (level.isClientSide)
            return super.use(level, player, usedHand);

        ItemStack stack = player.getItemInHand(usedHand);
        if (player.isShiftKeyDown() && canCast(player, stack, "health_theft")) {
            stack.set(ACTIVE_TICK, 20 * (int) getStatValue(player, stack, "health_theft", "durability"));
            setMaxCooldown(player, stack, "health_theft");
        }

        int activeTick = stack.getOrDefault(JODAH_ACTIVE_TICK, 0);

        if (activeTick <= 0)
            return super.use(level, player, usedHand);

        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookVector = player.getLookAngle();
        Vec3 endVec = eyePosition.add(lookVector.scale(60));

        AABB box = player.getBoundingBox().expandTowards(lookVector.scale(60)).inflate(1.0);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level,
                player,
                eyePosition,
                endVec,
                box,
                entity -> !entity.isSpectator() && entity.isPickable()
                        && entity instanceof LivingEntity living && IJodahGlowed.of(living).hasJodahGlowEffect()
                        && living.isAlive()
        );

        if (entityHit != null) {
            LivingEntity living = (LivingEntity) entityHit.getEntity();
            var walkablePos = getWalkableNearTarget(level, player, living);

            if (walkablePos == null)
                return InteractionResultHolder.consume(stack);

            var effect = living.getEffect(EffectsRegistry.JODAH_VISION);
            if (effect == null)
                return InteractionResultHolder.consume(stack);

            int amplifier = effect.getAmplifier();
            living.removeEffect(EffectsRegistry.JODAH_VISION);

            if (amplifier > 0)
                living.addEffect(new MobEffectInstance(EffectsRegistry.JODAH_VISION, effect.getDuration(), amplifier - 1));

            JodahTier tier = stack.getOrDefault(JODAH_TIER, JodahTier.D);

            DamageSource damagesource = player.damageSources().playerAttack(player);
            float damage = getPlayerDamage(player, level, living, stack, damagesource) * tier.oneThousandEyes.damageMultiplier();
            living.hurt(damagesource, damage);

            living.addEffect(new MobEffectInstance(EffectsRegistry.DISABILITY_EFFECT, 20, 1, false, false));
            player.teleportTo(walkablePos.x, walkablePos.y, walkablePos.z);
            ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(PURPLE_COLOR, 0.4f,
                    50, 0.96f), player, 40, 0.2);
            ParticleHelper.spawnParticleEntity(ParticleHelper.constructFigure(new OctoColor(0xFFAAAAAA), 0.21f,
                    50, 0.96f), player, 15, 0.12);
            ParticleHelper.spawnParticleEntity(new FeatherParticle.Options(0.3f, 70), player, 20, 0.3);

            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(0.9),
                    e -> e != living && e != player);
            for (var target : targets) {
                Vec3 pushMovement = target.position().subtract(player.position()).multiply(1, 0, 1)
                        .normalize().scale(0.3f).add(0, 0.2f, 0);

                if (target.isPushable())
                    target.push(pushMovement.x, pushMovement.y, pushMovement.z);
                target.addEffect(new MobEffectInstance(EffectsRegistry.DISABILITY_EFFECT, 20, 1, false, false));
                target.hurt(damagesource, 5 * tier.oneThousandEyes.damageMultiplier());
            }

            if (hasRangModifier(player, stack, "one_thousand_eyes", "infinity")
                    && tier == JodahTier.A || tier == JodahTier.S) {
                living.level().getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(17), e -> e != player && !e.isDeadOrDying() && e.isPickable())
                        .forEach(e -> e.addEffect(new MobEffectInstance(EffectsRegistry.JODAH_VISION, activeTick,
                                tier.oneThousandEyes.targetingCount() - 1)));
            }
            addExperience(player, stack, 3);
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return DURATION;
    }

    @SubscribeEvent
    public void playerHurt(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Player p))
            return;

        var itr = ItemChargeHelper.listPlayerInventories(p).iterator();

        while (itr.hasNext()) {
            var ih = itr.next();
            for (int j = 0; j < ih.getSlots(); j++) {
                var it = ih.getStackInSlot(j);
                if (!it.is(this))
                    continue;

                boolean hasRang = hasRangModifier(p, it, "ranging", "mercy");

                JodahTier tier = it.getOrDefault(ComponentRegistry.JODAH_TIER, JodahTier.D);
                it.set(ComponentRegistry.JODAH_TIER, hasRang ? tier.previous() : tier.previous().previous());
                it.set(ComponentRegistry.LEVEL, 0);
            }
        }
    }

    private float getPlayerDamage(Player p, Level level, Entity entity, ItemStack stack, DamageSource source) {
        float f = (float) p.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (level instanceof ServerLevel serverlevel) {
            f = EnchantmentHelper.modifyDamage(serverlevel, stack, entity, source, f);
        }

        return f;
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
    public float getAttackDamageBonus(@NotNull Entity target, float damage, @NotNull DamageSource damageSource) {
        float damageBonus = super.getAttackDamageBonus(target, damage, damageSource);
        if (damageSource.getWeaponItem() == null || !(damageSource.getEntity() instanceof Player p))
            return damageBonus;

        JodahTier tier = damageSource.getWeaponItem().getOrDefault(ComponentRegistry.JODAH_TIER, JodahTier.D);
        float modifier = (int) getStatValue(p, damageSource.getWeaponItem(), "ranging", "damage_modifier");
        float fineReduce = (int) getStatValue(p, damageSource.getWeaponItem(), "ranging", "xp_consume");
        if (!(p.totalExperience >= tier.xpSuck / fineReduce))
            return damageBonus;
        return damageBonus + modifier * tier.damageIncrease;
    }

    @Override
    public String getConfigRoute() {
        return "relics";
    }

    @EventBusSubscriber(value = Dist.CLIENT)
    public static class JodahStaffClientEvents {

        @Nullable
        private static LivingEntity target;
        private static boolean hasTicked;

        @SubscribeEvent
        public static void renderEvent(ClientTickEvent.Pre event) {
            Player player = Minecraft.getInstance().player;

            if (player == null || !player.getMainHandItem().is(ItemsRegistry.JODAH_STAFF)
                    && !player.getOffhandItem().is(ItemsRegistry.JODAH_STAFF)) {
                target = null;
                return;
            }

            Vec3 eyePosition = player.getEyePosition();
            Vec3 lookVector = player.getLookAngle();
            Vec3 endVec = eyePosition.add(lookVector.scale(60));

            AABB box = player.getBoundingBox().expandTowards(lookVector.scale(60)).inflate(1.0);

            if (player.tickCount % 3 == 0 && !hasTicked) {
                hasTicked = true;
                EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                        player.level(),
                        player,
                        eyePosition,
                        endVec,
                        box,
                        entity -> !entity.isSpectator() && entity.isPickable()
                                && entity instanceof LivingEntity living && IJodahGlowed.of(living).hasJodahGlowEffect()
                                && living.isAlive() &&
                                (getWalkableNearTarget(player.level(), player, living) != null)
                );

                if (entityHit != null) {
                    LivingEntity living = (LivingEntity) entityHit.getEntity();

                    var effect = living.getEffect(EffectsRegistry.JODAH_VISION);
                    if (effect != null)
                        target = living;
                } else
                    target = null;
            } else
                hasTicked = false;
        }

        @SubscribeEvent
        public static void renderEvent(RenderLivingEvent.Post<? extends LivingEntity, ?> event) {
            Player player = Minecraft.getInstance().player;

            if (player == null)
                return;

            if (!player.getMainHandItem().is(ItemsRegistry.JODAH_STAFF)
                    && !player.getOffhandItem().is(ItemsRegistry.JODAH_STAFF)
                    || event.getEntity() != target)
                return;

            AABB aabb = event.getEntity().getBoundingBox();
            Vec3 renderPosition = new Vec3(0, aabb.getYsize() * 1.25 + 0.3, 0);

            JodahStaffRenderUtil.renderEye(event.getMultiBufferSource(), event.getPoseStack(), (float) renderPosition.x, (float) renderPosition.y, (float) renderPosition.z, Minecraft.getInstance().gameRenderer.getMainCamera());
        }

    }

}
