package com.qurenie.relics_thirteenflames.content.items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qurenie.api.IActivityContainer;
import com.qurenie.api.IExtRelicItem;
import com.qurenie.api.SettingsContainer;
import com.qurenie.api.event.IRenderableArmorItem;
import com.qurenie.relics_thirteenflames.activity.IActivitySetting;
import com.qurenie.relics_thirteenflames.activity.RelicActivitySetting;
import com.qurenie.relics_thirteenflames.activity.call.settings.ActivityResult;
import com.qurenie.relics_thirteenflames.activity.call.settings.InventoryType;
import com.qurenie.relics_thirteenflames.activity.call.settings.RelicsActivityCallSettings;
import com.qurenie.relics_thirteenflames.client.ClientModEvents;
import com.qurenie.relics_thirteenflames.client.particles.FeatherParticle;
import com.qurenie.relics_thirteenflames.client.render.entity.IJodahGlowed;
import com.qurenie.relics_thirteenflames.content.entities.MeteorEntity;
import com.qurenie.relics_thirteenflames.content.entities.SkintClusterEntity;
import com.qurenie.relics_thirteenflames.content.entities.SkintOrbEntity;
import com.qurenie.relics_thirteenflames.content.items.misc.MaskState;
import com.qurenie.relics_thirteenflames.content.items.misc.ScintType;
import com.qurenie.relics_thirteenflames.init.*;
import com.qurenie.relics_thirteenflames.net.MaskDarkStarPacket;
import com.qurenie.relics_thirteenflames.net.MaskSparkslipPacket;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;

import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootEntry;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.zeith.hammerlib.net.Network;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import static com.qurenie.relics_thirteenflames.style.ColorScheme.*;

public class ItemJodahMask extends ArmorItem implements IActivityContainer, IExtRelicItem, IRelicItem, IRenderableArmorItem {

    public ItemJodahMask(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    private static boolean isWalkable(Level level, Player player, Vec3 targetPos) {
        if (player.isSpectator())
            return true;

        AABB playerBox = player.getBoundingBox();
        AABB movedBox = playerBox.move(targetPos.x - player.getX(), targetPos.y - player.getY(), targetPos.z - player.getZ()).deflate(0.3);
        return level.noCollision(movedBox);
    }

    private static @NotNull HitResult getVisibleTarget(Player player, double distance) {
        HitResult result = player.pick(distance, Minecraft.getInstance().getTimer().getGameTimeDeltaTicks(), false);
        Vec3 blockHitVec = result.getLocation();

        EntityHitResult entityResult = ProjectileUtil.getEntityHitResult(
                player.level(),
                player,
                player.getEyePosition(),
                player.getEyePosition().add(player.getLookAngle().scale(distance)),
                player.getBoundingBox().inflate(2).expandTowards(player.getLookAngle().scale(distance)),
                entity -> !entity.isSpectator() && entity.isPickable()
                        && entity instanceof LivingEntity living && living.isAlive()
        );

        if (entityResult != null
                && entityResult.getEntity() instanceof LivingEntity living
                && isJodahTarget(living, player))
            return entityResult;

        return entityResult != null && (result.getType() == HitResult.Type.MISS
                || blockHitVec.distanceToSqr(player.getEyePosition()) > entityResult.getLocation().distanceToSqr(player.getEyePosition()))
                ? entityResult : result;
    }

    public static boolean isJodahActiveEye(ItemStack stack) {
        return stack.getItem() instanceof ItemJodahStaff
                && stack.getOrDefault(ComponentRegistry.JODAH_ACTIVE_TICK, 0) > 0;
    }

    public static boolean isJodahTarget(LivingEntity living, Player player) {
        return IJodahGlowed.of(living).hasJodahGlowEffect()
                && (isJodahActiveEye(player.getMainHandItem()) || isJodahActiveEye(player.getOffhandItem()));
    }

    public static boolean hasTotalDisability(LivingEntity living) {
        return living.hasEffect(EffectsRegistry.DISABILITY_EFFECT) && living.getEffect(EffectsRegistry.DISABILITY_EFFECT).getAmplifier() > 1;
    }

    @Override
    public <T extends LivingEntity> int damageItem(@NotNull ItemStack stack, int amount, @Nullable T entity, @NotNull Consumer<Item> onBroken) {
        return 0;
    }

    public static final LootEntry MASK_ENTRY = LootEntry.builder().dimension(".*").biome(".*").table("minecraft:chests/ancient_city").weight(640).build();

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("planeshift")
                                .initialMaxLevel(4)
                                .stat(AbilityStatTemplate.builder("recharge")
                                        .initialValue(1200, 800)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), -0.2)
                                        .thresholdValue(200, 1200)
                                        .formatValue(d -> MathUtils.round(d / 20, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("durability")
                                        .initialValue(0.5, 1.5)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0.5)
                                        .thresholdValue(0.5, 4)
                                        .formatValue(d -> MathUtils.round(d, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("agrochance")
                                        .initialValue(0.1, 0.2)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1.6)
                                        .thresholdValue(0, 1)
                                        .formatValue(d -> MathUtils.round(d * 100, 1))
                                        .build()
                                )
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_1")
                                        .build())
                                .rankModifier(1, "antiwarden")
                                .rankModifier(2, "friendlyfire")
                                .build()
                        )
                        .ability(AbilityTemplate.builder("sparkslip")
                                .initialMaxLevel(4)
                                .stat(AbilityStatTemplate.builder("recharge")
                                        .initialValue(900, 700)
                                        .upgradeModifier(RelicsScalingModels.EXPONENTIAL.get(), -0.2)
                                        .thresholdValue(160, 900)
                                        .formatValue(d -> MathUtils.round(d / 20, 0))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("range")
                                        .initialValue(10, 18)
                                        .upgradeModifier(RelicsScalingModels.EXPONENTIAL.get(), 0.35)
                                        .thresholdValue(4, 56)
                                        .formatValue(d -> MathUtils.round(d, 0))
                                        .build()
                                )
                                .rankModifier(2, "antispark")
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_2")
                                        .build())
                                .build()
                        )
                        .ability(AbilityTemplate.builder("dark_star")
                                .initialMaxLevel(5)
                                .stat(AbilityStatTemplate.builder("size")
                                        .initialValue(0.3, 0.8)
                                        .upgradeModifier(RelicsScalingModels.EXPONENTIAL.get(), 0.35)
                                        .thresholdValue(0.4, 3.4)
                                        .formatValue(d -> MathUtils.round(d, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("recharge")
                                        .initialValue(2400, 1600)
                                        .upgradeModifier(RelicsScalingModels.EXPONENTIAL.get(), -0.15)
                                        .thresholdValue(600, 2400)
                                        .formatValue(d -> MathUtils.round(d / 20, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("time")
                                        .initialValue(12, 22)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 2)
                                        .thresholdValue(12, 100)
                                        .formatValue(d -> MathUtils.round(d / 20, 1))
                                        .build()
                                )
                                .rankModifier(2, "target")
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_1")
                                        .build())
                                .build()
                        )
                        .ability(AbilityTemplate.builder("skint_genesis")
                                .initialMaxLevel(4)
                                .research(ResearchTemplate.builder()
                                        .star(0, 5, 7).star(1, 5, 23).star(2, 14, 12).star(3, 18, 19).star(4, 9, 25).star(5, 13, 25).star(6, 11, 16)
                                        .link(2, 5).link(5, 3).link(0, 4).link(1, 4).link(4, 5).link(5, 6)
                                        .build())
                                .stat(AbilityStatTemplate.builder("damage")
                                        .initialValue(2, 4)
                                        .upgradeModifier(RelicsScalingModels.EXPONENTIAL.get(), 0.27)
                                        .thresholdValue(2, 10)
                                        .formatValue(d -> MathUtils.round(d, 0))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("time")
                                        .initialValue(3, 5)
                                        .upgradeModifier(RelicsScalingModels.EXPONENTIAL.get(), 0.3)
                                        .thresholdValue(3, 300)
                                        .formatValue(Math::floor)
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("shard_count")
                                        .initialValue(1, 1.5)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0.5)
                                        .thresholdValue(1, 3)
                                        .formatValue(Math::floor)
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("recharge")
                                        .initialValue(3000, 2400)
                                        .upgradeModifier(RelicsScalingModels.EXPONENTIAL.get(), -0.23)
                                        .thresholdValue(800, 3000)
                                        .formatValue(d -> MathUtils.round(d / 20, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("scint_bonus")
                                        .initialValue(0.5, 1.2)
                                        .upgradeModifier(RelicsScalingModels.EXPONENTIAL.get(), 0.3)
                                        .thresholdValue(0.5, 300)
                                        .formatValue(d -> MathUtils.round(d, 1))
                                        .build()
                                )
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_3")
                                        .build())
                                .build()
                        )
                        .ability(AbilityTemplate.builder("reversal_aberration")
                                .initialMaxLevel(3)
                                .requiredPoints(2)
                                .requiredLevel(7)
                                .stat(AbilityStatTemplate.builder("skint_bonus")
                                        .initialValue(1, 2)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 1.4)
                                        .thresholdValue(1, 6)
                                        .formatValue(Math::floor)
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("recharge")
                                        .initialValue(2400, 1800)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), -360)
                                        .thresholdValue(200, 2400)
                                        .formatValue(d -> MathUtils.round(d / 20, 1))
                                        .build()
                                )
                                .rankModifier(1, "double")
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_1")
                                        .build())
                                .build()
                        )
                        .build()
                )
                .leveling(LevelingTemplate.builder()
                        .step(75)
                        .maxRank(2)
                        .initialCost(100)
                        .build())
//                .style(StyleData.builder()
//                        .beams(BeamsData.builder().startColor(-65281).endColor(255).build())
//                        .build())
                .loot(LootTemplate.builder().entry(MASK_ENTRY).build())
                .build();

    }

    @Override
    public SettingsContainer<IActivitySetting> constructActivitySettings() {
        return SettingsContainer.<IActivitySetting>builder()
                .setting(RelicActivitySetting.builderRelic("planeshift", "recharge")
                        .callSettings(RelicsActivityCallSettings.builder("planeshift")
                                .cast(ItemsRegistry.JODAH_MASK::castPlaneshift)
                                .inventoryType(InventoryType.ARMOR)
                                .visibility((p, s) -> p.getItemBySlot(EquipmentSlot.HEAD) == s)
                                .build())
                        .showBar((s, p) -> false)
                        .build())
                .setting(RelicActivitySetting.builderRelic("sparkslip", "recharge")
                        .callSettings(RelicsActivityCallSettings.builder("sparkslip")
                                .cast(ItemsRegistry.JODAH_MASK::castSparkslip)
                                .visibility((p, s) -> {
                                    MaskState state = s.getOrDefault(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL);
                                    return p.getItemBySlot(EquipmentSlot.HEAD) == s && state == MaskState.NEUTRAL;
                                })
                                .inventoryType(InventoryType.ARMOR)
                                .build())
                        .showBar((s, p) -> false)
                        .build())
                .setting(RelicActivitySetting.builderRelic("dark_star", "recharge")
                        .callSettings(RelicsActivityCallSettings.builder("dark_star")
                                .cast(ItemsRegistry.JODAH_MASK::castDarkStar)
                                .visibility((p, s) -> {
                                    MaskState state = s.getOrDefault(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL);
                                    return p.getItemBySlot(EquipmentSlot.HEAD) == s && state == MaskState.NEUTRAL;
                                })
                                .inventoryType(InventoryType.ARMOR)
                                .selectionNotify((l, s, c) ->
                                            ClientModEvents.MeteorOverlay.setActive(hasRangModifier(l, s, "dark_star", "target") && !c.isRemoved()))
                                .build())
                        .showBar((s, p) -> false)
                        .build())
                .setting(RelicActivitySetting.builderRelic("skint_genesis", "recharge")
                        .callSettings(RelicsActivityCallSettings.builder("skint_genesis")
                                .cast(ItemsRegistry.JODAH_MASK::castScintGenesis)
                                .visibility((p, s) -> {
                                    MaskState state = s.getOrDefault(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL);
                                    return p.getItemBySlot(EquipmentSlot.HEAD) == s && state != MaskState.NEUTRAL;
                                })
                                .inventoryType(InventoryType.ARMOR)
                                .build())
                        .showBar((s, p) -> false)
                        .build())
                .setting(RelicActivitySetting.builderRelic("reversal_aberration", "recharge")
                        .callSettings(RelicsActivityCallSettings.builder("reversal_aberration")
                                .cast(ItemsRegistry.JODAH_MASK::castReversalAberration)
                                .visibility((p, s) -> {
                                    MaskState state = s.getOrDefault(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL);
                                    return p.getItemBySlot(EquipmentSlot.HEAD) == s && state != MaskState.NEUTRAL;
                                })
                                .inventoryType(InventoryType.ARMOR)
                                .build())
                        .showBar((s, p) -> false)
                        .build())
                .build();
    }

    @Override
    public String getConfigRoute() {
        return "relics";
    }

    public ActivityResult castPlaneshift(LivingEntity living, ItemStack stack) {
        if (living.level().isClientSide || !(living instanceof Player player))
            return ActivityResult.SUCCESS;

        ParticleHelper.spawnParticleEntity(ParticleTypes.LARGE_SMOKE, player, 15, 0.1);
        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSmoke(PURPLE_COLOR, 1f,
                50, 0), player, 15, 0.1);


        FlamesUtils.startPlaneShift(stack, player, true);
        player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(30))
                .forEach(e -> {
                    if (e.getTarget() == player) {
                        e.setTarget(null);
                        e.getNavigation().stop();
                    }
                });

        MaskState state = stack.getOrDefault(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL);
        switch (state) {
            case SPARKLING:
                FlamesUtils.addSkint(stack, player, player, 1);
                break;
            case DUSK:
                player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10), e -> e != player)
                        .forEach(e -> {
                            DamageSource source = e.damageSources().playerAttack(player);
                            e.hurt(source, getPlayerDamage(player, player.level(), e, player.getMainHandItem(), source));
                            FlamesUtils.addAntiskint(stack, player, e, 1);
                        });
            default:
                int scints = player.getData(AttachmentsRegistry.SKINT_DATA);
                if (scints < 0)
                    break;

                ParticleHelper.spawnParticleEntity(ParticleHelper.constructHeal(GOLD_COLOR, 0.9f,
                        70, 0.98f), player, 5 * scints, 0.24);
                player.heal(scints * 2);
                addExperience(player, stack, scints);

                FlamesUtils.setSkint(player, 0, true);
        }

        List<Warden> wardens = living.level().getEntitiesOfClass(
                Warden.class,
                player.getBoundingBox().inflate(15),
                LivingEntity::isAlive
        );

        if (hasRangModifier(living, stack, "planeshift", "antiwarden"))
            for (Warden warden : wardens) {
                LivingEntity target = warden.getTarget();

                var attack = warden.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
                var roar = warden.getBrain().getMemory(MemoryModuleType.ROAR_TARGET);

                applyBury(warden);
            }

        if (hasRangModifier(living, stack, "planeshift", "friendlyfire")) {

            double chance = getStatValue(living, stack, "planeshift", "agrochance"); // N%

            List<Mob> mobs = player.level().getEntitiesOfClass(
                    Mob.class,
                    player.getBoundingBox().inflate(20),
                    e -> e.isAlive() && e.getTarget() == player
            );

            for (Mob mob : mobs) {

                if (player.getRandom().nextFloat() > chance)
                    continue;

                List<LivingEntity> nearby = player.level().getEntitiesOfClass(
                        LivingEntity.class,
                        mob.getBoundingBox().inflate(12),
                        e -> e != player
                                && e != mob
                                && e.isAlive()
                                && !(e instanceof ArmorStand)
                );

                LivingEntity nearest = nearby.stream()
                        .min(Comparator.comparingDouble(e -> e.distanceToSqr(mob)))
                        .orElse(null);

                if (nearest == null)
                    continue;

                mob.setTarget(nearest);

                if (mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                    redirectAggro(mob, nearest);
                }
            }
        }

        if (hasRangModifier(living, stack, "planeshift", "friendlyfire")) {
            // тут пиши код
            double agrochance = getStatValue(living, stack, "planeshift", "agrochance");
        }

        addExperience(player, stack, 2);
        stack.set(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL);

        return ActivityResult.SUCCESS;
    }

    public static void redirectAggro(Mob mob, LivingEntity target) {
        mob.setTarget(target);

        Brain<?> brain = mob.getBrain();

        brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        brain.setMemory(MemoryModuleType.ATTACK_TARGET, target);
    }

    private static void applyBury(Warden warden) {
        Brain<?> brain = warden.getBrain();

        // сброс агро
        brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
        brain.eraseMemory(MemoryModuleType.ROAR_TARGET);
        brain.eraseMemory(MemoryModuleType.ANGRY_AT);
        brain.eraseMemory(MemoryModuleType.NEAREST_ATTACKABLE);

        brain.eraseMemory(MemoryModuleType.DISTURBANCE_LOCATION);

        brain.eraseMemory(MemoryModuleType.DIG_COOLDOWN);
        brain.eraseMemory(MemoryModuleType.SNIFF_COOLDOWN);

        warden.getAngerManagement().getActiveEntity().ifPresent(e -> warden.getAngerManagement().clearAnger(e));

        warden.setTarget(null);
    }

    public void onPlaneshiftEnd(LivingEntity living) {
        if (living.level().isClientSide)
            return;

        if (living instanceof Mob m)
            m.setNoAi(false);

        living.removeData(AttachmentsRegistry.PLANESHIFT_TICK);
        FlamesUtils.Net.syncAttachmentRemove(living, AttachmentsRegistry.PLANESHIFT_TICK::get);

        living.setNoGravity(false);
        if (!living.hasEffect(MobEffects.INVISIBILITY))
            living.setInvisible(false);

        ParticleHelper.spawnParticleEntity(ParticleTypes.CAMPFIRE_COSY_SMOKE, living, 10, 0.1);
        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSmoke(PURPLE_COLOR, 1f,
                50, 0).withGravity(0.5f), living, 15, 0.1);
        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(PURPLE_COLOR, 0.3f,
                50, 0.98f).withGravity(-0.1f), living, 30, 0.2);
    }

    public ActivityResult castSparkslip(LivingEntity living, ItemStack stack) {
        Level level = living.level();

        if (!level.isClientSide || !(living instanceof Player player))
            return ActivityResult.FAILURE;

        double distance = getStatValue(player, stack, "sparkslip", "range");
        HitResult result = getVisibleTarget(player, distance);
        Vec3 loc = result.getLocation();

        Entity livingTarget = null;
        Vec3 target = switch (result.getType()) {
            case MISS -> {
                Vec3 maxPoint = player.getEyePosition().add(player.getLookAngle().scale(distance));
                ParticleHelper.spawnParticles(level, ParticleHelper.constructSimpleSpark(GOLD_COLOR, 0.2f,
                        40, 0.96f), maxPoint, 25, 0, 0, 0, 0.02);

                yield null;
            }
            case BLOCK -> {
                BlockHitResult hitResult = (BlockHitResult) result;
                yield switch (hitResult.getDirection()) {
                    case UP -> isWalkable(level, player, loc) ? loc : null;
                    case DOWN -> null;
                    default -> {
                        BlockPos pos = hitResult.getBlockPos();
                        double height = FlamesUtils.getBlockHeightSafety(level, pos);
                        Vec3 upper = pos.getBottomCenter().add(0, height, 0);
                        if (isWalkable(level, player, upper))
                            yield upper;

                        loc = loc.add(Vec3.atLowerCornerOf(hitResult.getDirection().getNormal()).scale(0.2));
                        pos = new BlockPos(Mth.floor(loc.x), Mth.floor(loc.y), Mth.floor(loc.z));
                        height = FlamesUtils.getBlockHeightSafety(level, pos);
                        yield isWalkable(level, player, loc) ? new Vec3(loc.x, Mth.floor(loc.y) + height, loc.z) : null;
                    }
                };
            }
            case ENTITY -> {
                livingTarget = ((EntityHitResult) result).getEntity();
                yield loc.subtract(player.getLookAngle().multiply(0.8f, 0, 0.8f));
            }
        };

        if (target == null)
            return ActivityResult.FAILURE;

        Network.sendToServer(new MaskSparkslipPacket(livingTarget, target));
        return ActivityResult.SUCCESS;
    }

    private void onScintGenesisEnd(Player player, ItemStack stack) {
        if (stack.getOrDefault(ComponentRegistry.ACTIVE_TICK, 0) > 1)
            player.setNoGravity(false);

        stack.set(ComponentRegistry.ACTIVE_TICK, 0);

        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 50, 0, false, false));
    }

    public ActivityResult castScintGenesis(LivingEntity player, ItemStack stack) {
        int scints = player.getData(AttachmentsRegistry.SKINT_DATA);

        stack.set(ComponentRegistry.ACTIVE_TICK, (int) getStatValue(player, stack, "skint_genesis", "time") * 20
                + (int) ((scints * 20) * getStatValue(player, stack, "skint_genesis", "scint_bonus")));

        if (!player.level().isClientSide)
            FlamesUtils.setSkint(player, 0, true);

        stack.set(ComponentRegistry.CLUSTERS_MASK_STATE, stack.getOrDefault(ComponentRegistry.MASK_STATE, MaskState.SPARKLING));
        stack.set(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL);
        setMaxCooldown(player, stack, "skint_genesis");

        player.addDeltaMovement(new Vec3(0, 0.5, 0));

        return ActivityResult.SUCCESS;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (level.isClientSide || !(entity instanceof Player player))
            return;

        int ticks = stack.getOrDefault(ComponentRegistry.ACTIVE_TICK, 0);
        if (ticks <= 0)
            return;


        if (player.isShiftKeyDown()) {
            onScintGenesisEnd(player, stack);

            return;
        }

        player.setDeltaMovement(player.getDeltaMovement().scale(0.97f));
        var maskState = stack.getOrDefault(ComponentRegistry.CLUSTERS_MASK_STATE, MaskState.SPARKLING);
        ParticleHelper.spawnParticles(level, ParticleHelper.constructSmoke(maskState.getColor(), 0.3f + level.random.nextFloat() * 0.7f, 60)
                        .withLightning(maskState.isSparkling()),
                player.position(), 1, 0, 0, 0, 0.05);
        player.setNoGravity(true);

        if (player.tickCount % 4 != 0 || player.getItemBySlot(getEquipmentSlot(stack)) != stack)
            return;

        final double radius = 12;
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(radius, 2, radius)
                                .expandTowards(0, -5, 0),
                        e -> e != entity && !(e instanceof SkintClusterEntity))
                .stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(player))).limit(3).toList();

        int i = 3;
        for (var e : targets) {
            BlockPos pos = e.blockPosition().below();
            if (level.random.nextBoolean() && !level.getBlockState(pos).isAir() && level.getBlockState(e.blockPosition()).isAir()) {
                double height = FlamesUtils.getBlockHeightSafety(level, pos);
                Vec3 position = new Vec3(e.position().x, pos.getY() + height, e.position().z);
                List<LivingEntity> c = level.getEntitiesOfClass(LivingEntity.class, new AABB(position.subtract(0.5, 0, 0.5), position.add(0.5, 1, 0.5)),
                        e$ -> e$ instanceof SkintClusterEntity || e$ == entity);

                if (!c.isEmpty())
                    continue;

                int count = (int) getStatValue(player, stack, "skint_genesis", "shard_count");
                float damage = (float) getStatValue(player, stack, "skint_genesis", "damage");
                int limit = (int) getStatValue(player, stack, "reversal_aberration", "skint_bonus");
                SkintClusterEntity cluster = new SkintClusterEntity(EntityRegistry.SKINT_CLUSTER, level, maskState == MaskState.SPARKLING ? ScintType.SKINT : ScintType.ANTISKINT, player, damage, count, limit);
                cluster.setPos(position);
                level.addFreshEntity(cluster);

                i--;
            }
        }

        int j = i;
        while (j-- > 0) {
            double x = (0.5 - level.random.nextDouble()) * 2 * radius;
            double z = (0.5 - level.random.nextDouble()) * 2 * Math.sqrt(radius * radius - x * x);

            for (int dy : List.of(0, -1, -2, -3, -4, -5, -6)) {
                BlockPos blockPos = player.blockPosition().offset((int) x, dy, (int) z);

                var state = level.getBlockState(blockPos);

                if ((state.isAir() || !state.getFluidState().isEmpty())
                        && level.getBlockState(blockPos.below()).isFaceSturdy(level, blockPos.below(), Direction.UP, SupportType.CENTER)) {
                    double height = level.getBlockFloorHeight(blockPos.below());
                    Vec3 position = new Vec3(blockPos.getX(), blockPos.getY() - 1 + height, blockPos.getZ());
                    List<LivingEntity> c = level.getEntitiesOfClass(LivingEntity.class, new AABB(position.subtract(0.5, 0, 0.5), position.add(0.5, 1, 0.5)),
                            e$ -> e$ instanceof SkintClusterEntity || e$ == entity);

                    if (!c.isEmpty())
                        continue;

                    int count = (int) getStatValue(player, stack, "skint_genesis", "shard_count");
                    float damage = (float) getStatValue(player, stack, "skint_genesis", "damage");
                    int limit = (int) getStatValue(player, stack, "reversal_aberration", "skint_bonus");
                    SkintClusterEntity cluster = new SkintClusterEntity(EntityRegistry.SKINT_CLUSTER, level, maskState == MaskState.SPARKLING ? ScintType.SKINT : ScintType.ANTISKINT, player, damage, count, limit);
                    cluster.setPos(position);
                    level.addFreshEntity(cluster);

                    i--;
                    break;
                }
            }
        }
    }

    public ActivityResult castDarkStar(LivingEntity player, ItemStack stack) {
        EntityHitResult entityResult = hasRangModifier(player, stack, "dark_star", "target") ? ProjectileUtil.getEntityHitResult(
                player.level(),
                player,
                player.getEyePosition(),
                player.getEyePosition().add(player.getLookAngle().scale(140)),
                player.getBoundingBox().inflate(2).expandTowards(player.getLookAngle().scale(140)),
                entity -> !entity.isSpectator() && entity.isPickable()
                        && entity instanceof LivingEntity living && living.isAlive()
        ) : null;

        Network.sendToServer(new MaskDarkStarPacket(entityResult == null ? null : entityResult.getEntity()));
        return ActivityResult.SUCCESS;
    }

    public void onDarkStarPacket(ItemStack stack, Player player, @Nullable LivingEntity target) {
        float size = (float) getStatValue(player, stack, "dark_star", "size");
        int time = (int) getStatValue(player, stack, "dark_star", "time");
        MeteorEntity meteorEntity = new MeteorEntity(player.level(), player, target, size, time);
        player.level().addFreshEntity(meteorEntity);
        stack.set(ComponentRegistry.MASK_STATE, MaskState.SPARKLING);
        setMaxCooldown(player, stack, "dark_star");
    }

    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    public ActivityResult castReversalAberration(LivingEntity living, ItemStack stack) {
        if (!(living instanceof Player player))
            return ActivityResult.FAILURE;

        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(20, 10, 20), e -> e != player && e.isAlive() && e.isPickable() && !e.isSpectator());
        if (player.level().isClientSide)
            return targets.isEmpty() ? ActivityResult.FAILURE : ActivityResult.SUCCESS;

        int limitBonus = (int) getStatValue(player, stack, "reversal_aberration", "skint_bonus");
        MaskState state = stack.getOrDefault(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL);
        int scints = player.getData(AttachmentsRegistry.SKINT_DATA);

        var random = player.getRandom();

        double r = 1;

        for (int i = 0; i < 35; i++) {
            double x = (0.5 - Math.random()) * r * 2;
            double z = Math.sqrt(r * r - x * x) * (random.nextBoolean() ? 1 : -1);
            Vec3 spawn = player.getBoundingBox().getCenter().add(x, 0, z);

            Vec3 radius = spawn.subtract(player.getBoundingBox().getCenter());
            Vec3 move = radius.normalize().yRot((float) (230f * Math.PI / 180f)).add(0, (0.5 - Math.random()) * 0.3, 0);

            ParticleHelper.spawnDirectedParticle(player.level(), ParticleHelper.constructSimpleSpark(GOLD_COLOR, (float) (0.5f + Math.random() * 0.3f), 60, 0.94f),
                    spawn.add((0.5 - Math.random()) * 0.5, (0.5 - Math.random()) * 0.5, (0.5 - Math.random()) * 0.5), move.normalize().scale(0.03 + random.nextDouble() * 0.03));
        }

        AABB aabb = player.getBoundingBox();
        Vec3 center = aabb.getCenter();
        double radius = Math.sqrt(Math.pow(aabb.getXsize() / 2, 2) + Math.pow(aabb.getZsize() / 2, 2)) * 1.5 + 0.3;
        int particleCount = 10 * Math.min(scints, 3);

        if (!targets.isEmpty())
            for (int i = 0; i < particleCount; i++) {
                Vec3 vec = new Vec3(1, 0, 0).yRot((float) (i * Math.PI * 2 / particleCount)).scale(radius).add(center);
                ParticleHelper.spawnParticles(player.level(), ParticleHelper.constructSmoke(GOLD_COLOR, (float) (0.3f + random.nextDouble() * 0.3f),
                        50, 0), vec, (int) (random.nextDouble() * 2), 0.2, 0.2, 0.2, 0.01);
            }

        for (LivingEntity e : targets) {
            int count = e.getData(AttachmentsRegistry.ANTISKINT_DATA);
            if (scints <= 0 && count <= 0)
                continue;

            if (!player.level().isClientSide) {
                for (int i = 0; i < count; i++) {
                    SkintOrbEntity orb = new SkintOrbEntity(player.level(), ScintType.SKINT, player, e, limitBonus);
                    player.level().addFreshEntity(orb);
                }
                e.setData(AttachmentsRegistry.ANTISKINT_DATA, 0);
                FlamesUtils.addAntiskint(e, scints, limitBonus);
                FlamesUtils.Net.sendSkintAttachment(e);
            }

            if (scints <= 0)
                continue;

            switch (state) {
                case DUSK -> {
                    e.addEffect(new MobEffectInstance(EffectsRegistry.DISABILITY_EFFECT, 40 * scints, 2, false, false, true));
                    ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(GRAY_COLOR, 0.5f,
                            50, 0.95f).withLightning(false).withGravity(2f), e, 5 * count, 0.1);

                }
                case SPARKLING -> {
                    DamageSource source = player.damageSources().playerAttack(player);
                    e.hurt(source, 8 * scints);
                    e.setLastHurtByPlayer(player);

                    ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(GRAY_COLOR, 0.4f,
                            50, 0.96f).withLightning(false), e, 5 * count, 0.1);
                }
            }
        }

        if (!targets.isEmpty()) {
            FlamesUtils.addSkint(player, -scints, 0);
            setMaxCooldown(player, stack, "reversal_aberration");

            if (hasRangModifier(player, stack, "reversal_aberration", "double"))
                player.addEffect(new MobEffectInstance(EffectsRegistry.DOUBLE_SCINT_EFFECT, 400, 0));
        } else {
            addCooldown(player, stack, "reversal_aberration", 60);
        }

        return ActivityResult.SUCCESS;
    }

    public void onSparkslipPacket(ItemStack stack, Player player, Vec3 spawnPos, @Nullable LivingEntity target) {
        player.teleportTo(spawnPos.x, spawnPos.y, spawnPos.z);
        boolean jodahTeleport = false;
        if (target != null) {
            if (isJodahTarget(target, player)) {
                jodahTeleport = true;
                FlamesUtils.addAntiskint(stack, player, target, 4);
            }
            target.addEffect(new MobEffectInstance(EffectsRegistry.DISABILITY_EFFECT, jodahTeleport ? 25 : 15));

            if (hasRangModifier(player, stack, "sparkslip", "antispark"))
                FlamesUtils.addAntiskint(stack, player, target, 2);
        }

        stack.set(ComponentRegistry.MASK_STATE, MaskState.DUSK);
        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(jodahTeleport ? GRAY_COLOR : GOLD_COLOR, 0.4f,
                50, 0.96f).withLightning(!jodahTeleport), player, 45, 0.1);

        if (!jodahTeleport)
            FlamesUtils.addSkint(stack, player, player, 2);
        else {
            ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(PURPLE_COLOR, 0.4f,
                    50, 0.96f), player, 25, 0.1);
            ParticleHelper.spawnParticleEntity(new FeatherParticle.Options(0.3f, 70), player, 20, 0.3);
            player.swing(player.getMainHandItem().getItem() == ItemsRegistry.JODAH_STAFF ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, true);
        }

        addExperience(player, stack, 2);
        setMaxCooldown(player, stack, "sparkslip");
    }

    private float getPlayerDamage(Player p, Level level, Entity entity, ItemStack stack, DamageSource source) {
        float f = (float) p.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (level instanceof ServerLevel serverlevel) {
            f = EnchantmentHelper.modifyDamage(serverlevel, stack, entity, source, f);
        }

        return f;
    }

    @Override
    public @NotNull EquipmentSlot getEquipmentSlot(@NotNull ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, LivingEntity livingEntity, EquipmentSlot slot, int packedLight, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public boolean cancelDefaultRenderer(LivingEntity livingEntity, EquipmentSlot slot) {
        return true;
    }

    @EventBusSubscriber
    public static class EventHandler {

        @SubscribeEvent
        public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
            if (event.getTo().getItem() == event.getFrom().getItem())
                return;

            if (event.getEntity() instanceof Player p && event.getFrom().getItem() == ItemsRegistry.JODAH_MASK && p.hasData(AttachmentsRegistry.PLANESHIFT_TICK)) {
                ItemsRegistry.JODAH_MASK.onPlaneshiftEnd(p);
                ItemsRegistry.JODAH_MASK.onScintGenesisEnd(p, event.getFrom());
            }
        }

        @SubscribeEvent
        public static void planeshiftDamage(LivingDamageEvent.Pre event) {
            if (event.getEntity().hasData(AttachmentsRegistry.PLANESHIFT_TICK))
                event.setNewDamage(0);
        }

        @SubscribeEvent
        public static void onPlayerTick(EntityTickEvent.Pre event) {
            Entity e = event.getEntity();

            if (e instanceof LivingEntity living && hasTotalDisability(living)) {
                e.setDeltaMovement(Vec3.ZERO);
                if (e instanceof Mob m) {
                    m.setTarget(null);
                    m.setNoAi(true);
                    m.getNavigation().stop();
                }
            }

            if (e.hasData(AttachmentsRegistry.PLANESHIFT_TICK)) {
                e.setDeltaMovement(Vec3.ZERO);
                e.fallDistance = 0.0F;
                e.invulnerableTime = 2;
                e.setNoGravity(true);
                e.setInvisible(true);
                if (e instanceof Mob m) {
                    m.setTarget(null);
                    m.setNoAi(true);
                    m.getNavigation().stop();
                }
                if (e instanceof Player p) {
                    ItemStack head = p.getItemBySlot(EquipmentSlot.HEAD);

                    if (head.getItem() == ItemsRegistry.JODAH_MASK && !p.isShiftKeyDown())
                        ItemsRegistry.JODAH_MASK.setMaxCooldown(p, head, "planeshift");
                    else
                        ItemsRegistry.JODAH_MASK.onPlaneshiftEnd(p);
                }
            }

        }

        @SubscribeEvent
        public static void onLivingTick(EntityTickEvent.Post event) {
            if (event.getEntity() instanceof LivingEntity living && living.tickCount % 20 == 0
                    && hasTotalDisability(living))
                ParticleHelper.spawnParticleEntity(ParticleHelper.constructSmoke(GRAY_COLOR, living.getBbWidth(),
                        50, 0.2f).withGravity(-1).withLightning(false), event.getEntity(), 1, 0.03);
        }

        @SubscribeEvent
        public static void onInteract(PlayerInteractEvent.EntityInteract event) {
            Player e = event.getEntity();
            if (e.hasData(AttachmentsRegistry.PLANESHIFT_TICK) || hasTotalDisability(e))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onInteract(PlayerInteractEvent.RightClickBlock event) {
            Player e = event.getEntity();

            if (e.hasData(AttachmentsRegistry.PLANESHIFT_TICK) || hasTotalDisability(e))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onInteract(PlayerInteractEvent.RightClickItem event) {
            Player e = event.getEntity();

            if (e.hasData(AttachmentsRegistry.PLANESHIFT_TICK) || hasTotalDisability(e))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onTargeting(LivingChangeTargetEvent event) {
            Entity e = event.getNewAboutToBeSetTarget();
            if (e == null)
                return;

            if (e.hasData(AttachmentsRegistry.PLANESHIFT_TICK))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onInteract(PlayerInteractEvent.LeftClickBlock event) {
            Player e = event.getEntity();

            if (e.hasData(AttachmentsRegistry.PLANESHIFT_TICK) || hasTotalDisability(e))
                event.setCanceled(true);
        }

    }

    @OnlyIn(Dist.CLIENT)
    @EventBusSubscriber(value = Dist.CLIENT)
    public static class ClientEventHandler {

        @SubscribeEvent
        public static void onRenderPlayer(RenderLivingEvent.Pre<?, ?> event) {
            Entity e = event.getEntity();

            if (e.hasData(AttachmentsRegistry.PLANESHIFT_TICK))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onBlockHighlight(RenderHighlightEvent.Block event) {
            Player player = Minecraft.getInstance().player;
            if (player != null && (player.hasData(AttachmentsRegistry.PLANESHIFT_TICK)
                    || hasTotalDisability(player))) {
                event.setCanceled(true);
            }

        }

        @SubscribeEvent
        public static void onMouseInput(InputEvent.InteractionKeyMappingTriggered event) {
            Player player = Minecraft.getInstance().player;
            if (player != null && (player.hasData(AttachmentsRegistry.PLANESHIFT_TICK) || hasTotalDisability(player)) && event.getKeyMapping().matchesMouse(GLFW.GLFW_KEY_ESCAPE)) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }

        }

        @SubscribeEvent
        public static void onRenderHand(RenderHandEvent event) {
            Entity e = Minecraft.getInstance().player;

            if (e != null && e.hasData(AttachmentsRegistry.PLANESHIFT_TICK))
                event.setCanceled(true);
        }

    }


}
