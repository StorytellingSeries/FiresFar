package com.qurenie.relics_thirteenflames.content.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Sets;
import com.qurenie.api.IActivityContainer;
import com.qurenie.api.IExtRelicItem;
import com.qurenie.api.SettingsContainer;
import com.qurenie.api.event.MeleeAttackCheckEvent;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.activity.IActivitySetting;
import com.qurenie.relics_thirteenflames.activity.RelicActivitySetting;
import com.qurenie.relics_thirteenflames.content.entities.TravellerCutEntity;
import com.qurenie.relics_thirteenflames.content.entities.TravellerSweepEntity;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import com.qurenie.relics_thirteenflames.net.TravellerCutPacket;
import com.qurenie.relics_thirteenflames.net.TravellerSweepPacket;
import com.qurenie.relics_thirteenflames.style.ColorScheme;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.MixinHooks;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootEntry;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.MathUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.api.fml.IRegisterListener;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.net.PacketContext;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.*;
import static com.qurenie.relics_thirteenflames.style.ColorScheme.BURN_COLOR;
import static com.qurenie.relics_thirteenflames.style.ColorScheme.CYAN_COLOR;
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

public class ItemTravellerSword extends SwordItem implements IExtRelicItem, IRelicItem, IRegisterListener, IActivityContainer {

    public static final ResourceLocation TRAVELLER_STEP_HEIGHT = ThirteenFlames.rl("traveller_step_height");
    public static final ResourceLocation TRAVELLER_MOVEMENT_SPEED = ThirteenFlames.rl("traveller_movement_speed");
    private static final float MAX_CHARGE = 5f;
    private static final int DASH_TICKS = 3;

    public static final LootEntry RUINED_PORTAL = LootEntry.builder().dimension(".*").biome(".*").table("minecraft:chests/ruined_portal").weight(900).build();

    public ItemTravellerSword(Tier tier, Properties properties) {
        super(tier, properties);
    }

    private static void applySpeedModifier(Player p, float charge) {
        p.getAttributes().addTransientAttributeModifiers(ImmutableMultimap.<Holder<Attribute>, AttributeModifier>builder()
                .put(Attributes.MOVEMENT_SPEED, new AttributeModifier(TRAVELLER_MOVEMENT_SPEED, charge / MAX_CHARGE * 2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))
                .build());
    }

    @Nullable
    private static Vec3 projectToSurface(@NotNull Level level, double x, double y, double z, int limit) {
        int bx = Mth.floor(x);
        int by = Mth.floor(y);
        int bz = Mth.floor(z);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(bx, by, bz);
        do {
            if (level.getBlockState(pos).isFaceSturdy(level, pos, Direction.UP, SupportType.CENTER))
                return new Vec3(x, pos.getY() + level.getBlockFloorHeight(pos), z);
            pos.move(Direction.DOWN, 1);
        } while (limit-- > 0);
        return null;
    }

    private static boolean isWalkable(Level level, Player player, Vec3 targetPos) {
        if (player.isSpectator())
            return true;

        AABB playerBox = player.getBoundingBox();
        AABB movedBox = playerBox.move(targetPos.x - player.getX(), targetPos.y - player.getY(), targetPos.z - player.getZ()).deflate(0.2);
        return level.noCollision(movedBox);
    }

    @Nullable
    public static TravellerDashPointer createPointer(Level level, Player player, Vec3 start, Vec3 end, boolean inAir) {
        Vec3 direction = end.subtract(start);
        double length = direction.length();
        Vec3 norm = direction.normalize();

        Set<Vec3> rawPoints = new TreeSet<>(Comparator.comparing(start::distanceToSqr));
        ArrayList<Vec3> collected = new ArrayList<>();

        for (Axis axis : List.of(Axis.X, Axis.Z)) {
            double startCoord = axis == Axis.X ? start.x : start.z;
            double endCoord = axis == Axis.X ? end.x : end.z;

            int min = (int) Math.floor(Math.min(startCoord, endCoord));
            int max = (int) Math.ceil(Math.max(startCoord, endCoord));

            // --- 1. Границы блоков (как было) ---
            for (double coord = min; coord <= max; coord += 1) {
                addIntersectionPoint(start, direction, norm, length, axis, coord, rawPoints);
            }

            // --- 2. Середины блоков (новое) ---
            for (double coord = min + 0.5; coord <= max + 0.5; coord += 1) {
                addIntersectionPoint(start, direction, norm, length, axis, coord, rawPoints);
            }
        }

        if (rawPoints.isEmpty())
            return null;

        for (Vec3 rawPoint : rawPoints) {
            if (!isWalkable(level, player, rawPoint)) break;

            Vec3 projected = projectToSurface(level, rawPoint.x, rawPoint.y, rawPoint.z, inAir ? 2 : 3);

            if (projected != null)
                collected.add(projected);
            else if (inAir)
                collected.add(rawPoint);
        }

        if (collected.isEmpty())
            return null;

        return new TravellerDashPointer(collected.getLast(), collected);
    }

    private static void addIntersectionPoint(Vec3 start,
                                             Vec3 direction,
                                             Vec3 norm,
                                             double length,
                                             Axis axis,
                                             double coord,
                                             Set<Vec3> rawPoints) {

        double t;

        if (axis == Axis.X) {
            if (direction.x == 0) return;
            t = (coord - start.x) / direction.x;
        } else {
            if (direction.z == 0) return;
            t = (coord - start.z) / direction.z;
        }

        if (t < 0 || t > 1) return;

        Vec3 rawPoint = start.add(norm.scale(length * t));
        rawPoints.add(rawPoint);
    }

    @Override
    public <T extends LivingEntity> int damageItem(@NotNull ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        return 0;
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
    public SettingsContainer<IActivitySetting> constructActivitySettings() {
        return SettingsContainer.<IActivitySetting>builder()
                .setting(RelicActivitySetting.builderRelic("dash", "recharge")
                        .color(ColorScheme.BAR_BLUE).build())
                .setting(RelicActivitySetting.builderRelic("swordcut", "recharge")
                        .color(CYAN_COLOR).build())
                .build();
    }

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("travelers_stride")
                                .initialMaxLevel(4)
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_2")
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_4")
                                        .build())
                                .stat(AbilityStatTemplate.builder("charge")
                                        .initialValue(1, 2)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 5)
                                        .thresholdValue(1, 5)
                                        .formatValue(d -> MathUtils.round(5d / d, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("stride_damage")
                                        .initialValue(10, 15)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 40)
                                        .thresholdValue(10, 40)
                                        .formatValue(d -> MathUtils.round(d, 0))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("sweep_damage_multi")
                                        .initialValue(0.5, 1)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 4)
                                        .thresholdValue(0.5, 4)
                                        .formatValue(d -> MathUtils.round(d * 100, 0))
                                        .build()
                                )
                                .rankModifier(2, "circle_sword")
                                .build()
                        )
                        .ability(AbilityTemplate.builder("dash")
                                .initialMaxLevel(5)
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_1")
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_3")
                                        .build())
                                .stat(AbilityStatTemplate.builder("range")
                                        .initialValue(4, 7)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 12)
                                        .thresholdValue(4, 12)
                                        .formatValue(d -> MathUtils.round(d, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("damage_boost")
                                        .initialValue(0.5, 1)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 7)
                                        .thresholdValue(0.5, 7)
                                        .formatValue(d -> MathUtils.round(d * 100, 0))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("recharge")
                                        .initialValue(400, 300)
                                        .thresholdValue(100, 400)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 100)
                                        .formatValue(d -> MathUtils.round(d / 20f, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("speed_boost")
                                        .initialValue(1.1, 1.4)
                                        .thresholdValue(1, 2.25)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 2.25)
                                        .formatValue(d -> MathUtils.round(d * 100 - 100, 2))
                                        .build()
                                )
                                .research(ResearchTemplate.builder()
                                        .star(0, 11, 23).star(1, 4, 20).star(2, 18, 21).star(3, 11, 9).star(4, 7, 16).star(5, 18, 13)
                                        .link(1, 0).link(0, 4).link(0, 3).link(0, 2).link(0, 5)
                                        .build())
                                .rankModifier(1, "overrun")
                                .build()
                        )
                        .ability(AbilityTemplate.builder("swordcut")
                                .requiredPoints(2)
                                .requiredLevel(9)
                                .initialMaxLevel(2)
                                .stat(AbilityStatTemplate.builder("damage")
                                        .initialValue(1.5, 2)
                                        .thresholdValue(1.5, 4)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 3)
                                        .formatValue(d -> MathUtils.round(d * 100, 0))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("recharge")
                                        .initialValue(220, 180)
                                        .thresholdValue(140, 220)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 140)
                                        .formatValue(d -> MathUtils.round(d / 20, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("speed")
                                        .initialValue(0.1, 0.25)
                                        .thresholdValue(0.1, 220)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 2.5)
                                        .formatValue(d -> MathUtils.round(d * 100, 1))
                                        .build()
                                )
                                .rankModifier(1, "speed")
                                .rankModifier(2, "sweep")
                                .build()
                        )
                        .build()
                )
                .leveling(LevelingTemplate.builder()
                        .step(100)
                        .initialCost(100)
                        .maxRank(2)
                        .build())
//                .leveling(LevelingData.builder().initialCost(100).initialMaxLevel(13).step(100)
//                        .sources(LevelingSourcesData.builder()
//                                .source(LevelingSourceData.genericBuilder("travelers_stride_run").initialValue(1).gem(GemShape.SQUARE, GemColor.CYAN).build())
//                                .source(LevelingSourceData.genericBuilder("travelers_stride_attack").initialValue(2).gem(GemShape.SQUARE, GemColor.CYAN).build())
//                                .source(LevelingSourceData.abilityBuilder("dash").initialValue(6).gem(GemShape.SQUARE, GemColor.CYAN).build())
//                                .source(LevelingSourceData.genericBuilder("dash_attack").initialValue(1).gem(GemShape.SQUARE, GemColor.CYAN).build())
//                                .build())
//                        .build())
                .loot(LootTemplate.builder().entry(RUINED_PORTAL).build())
                .build();
    }

    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (canCast(player, stack, "dash")) {
            setMaxCooldown(player, stack, "dash");
            stack.set(TRAVELLER_ACTIVE_TICK, DASH_TICKS);

            double range = ItemsRegistry.TRAVELLER_SWORD.getStatValue(player, stack, "dash", "range");
            Vec3 lookAngle = player.getLookAngle();
            double ly = lookAngle.y;
            double l1 = Math.sqrt(1 - ly * ly * 0.95);
            double scale = 1 / l1;
            boolean onSurface = !player.isFallFlying() && !player.isInFluidType() && !player.level().getBlockState(player.blockPosition().below()).isAir();
            TravellerDashPointer pointer = createPointer(player.level(), player, player.getEyePosition(),
                    player.getEyePosition().add(player.getLookAngle().scale(scale * range)), !onSurface);

            if (pointer == null)
                return super.use(level, player, usedHand);

            int fireAspect = stack.getEnchantmentLevel(player.level().holder(Enchantments.FIRE_ASPECT).get());
            player.playSound(SoundsRegistry.ADVENTURER_SWORD_DASH.get(), 1, 1);
            if (player.level().isClientSide) {
                Vec3 last = player.position();
                for (Vec3 pos : pointer) {
                    if (pos.y == last.y)
                        spawnDashParticles(player.level(), pos, last, true, fireAspect > 0);
                    else {
                        spawnDashParticles(player.level(), last, new Vec3(last.x, last.y, last.z), false, fireAspect > 0);
                        spawnDashParticles(player.level(), new Vec3(last.x, last.y, last.z), pos, true, fireAspect > 0);
                    }
                    last = pos;
                }

            }
            stack.set(DIRECTION, pointer.getEnd().subtract(player.position()));

            var end = pointer.getEnd();
            AABB movedBox = player.getBoundingBox().move(
                    end.x - player.getX(),
                    end.y - player.getY(),
                    end.z - player.getZ()
            );

            double maxY = Double.NEGATIVE_INFINITY;

            for (VoxelShape shape : level.getBlockCollisions(player, movedBox)) {
                for (AABB aabb : shape.toAabbs()) {
                    if (aabb.maxY > maxY) {
                        maxY = aabb.maxY;
                    }
                }
            }
            double finalY = (maxY == Double.NEGATIVE_INFINITY) ? end.y : maxY;

            player.moveTo(end.x, finalY, end.z);

            player.swing(stack == player.getItemInHand(InteractionHand.MAIN_HAND) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);

            if (player.level().isClientSide)
                return super.use(level, player, usedHand);

            if (hasRangModifier(player, stack, "dash", "overrun")) {
                float charge = stack.getOrDefault(SPEED, 0f);
                stack.set(SPEED, charge * (float) getStatValue(player, stack, "dash", "speed_boost"));
            }

            Vec3 last = player.position();
            for (Vec3 pos : pointer) {
                AABB aabb = new AABB(pos, last).inflate(1.5, 1.5, 1.5).expandTowards(0, 4, 0);
                for (LivingEntity living : player.level().getEntitiesOfClass(LivingEntity.class, aabb, l -> l != player)) {
                    living.setLastHurtByPlayer(player);
                    DamageSource source = player.damageSources().playerAttack(player);
                    living.hurt(source, (float) this.getStatValue(player, stack, "dash", "damage_boost") * getPlayerDamage(player, player.level(), living, stack, source) * 0.75f);

                    if (fireAspect > 0)
                        living.setRemainingFireTicks(Math.max(living.getRemainingFireTicks(), 60 * fireAspect));

                    if (stack.getItem() == ItemsRegistry.TRAVELLER_SWORD)
                        addExperience(player, stack, 3);
                }
            }

            addExperience(player, stack, 8);
        }

        return super.use(level, player, usedHand);
    }

    private void spawnDashParticles(Level level, Vec3 point, Vec3 lastPoint, boolean upParticles, boolean onFire) {
        double rate = 0.1;

        double distance = point.distanceTo(lastPoint);
        int count = (int) (distance / rate);

        if (!onFire)
            ParticleHelper.spawnParticleLine(level, ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, level.random), 0.43f, 70, 0.95f),
                    point, lastPoint, count, 0, 0.2);
        else {
            ParticleHelper.spawnParticleLine(level, ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(BURN_COLOR, level.random), 0.43f, 70, 0.95f),
                    point, lastPoint, (count / 2) + 1, 0, 0.2);
            ParticleHelper.spawnParticleLine(level, ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, level.random), 0.43f, 70, 0.95f),
                    point, lastPoint, (count / 2) + 1, 0, 0.2);
        }

        point = point.add(0, 0.1, 0);
        lastPoint = lastPoint.add(0, 0.1, 0);
        if (upParticles) {
            if (!onFire)
                ParticleHelper.spawnParticleLine(level, ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, level.random), 0.43f, 60 + level.random.nextInt() * 30, 0.95f),
                        point, lastPoint, count * 2, new Vec3(0, 0.03, 0), 0.1);
            else {
                ParticleHelper.spawnParticleLine(level, ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, level.random), 0.43f, 60 + level.random.nextInt() * 30, 0.95f),
                        point, lastPoint, (count / 2) + 1, new Vec3(0, 0.03, 0), 0.1);
                ParticleHelper.spawnParticleLine(level, ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(BURN_COLOR, level.random), 0.43f, 60 + level.random.nextInt() * 30, 0.95f),
                        point, lastPoint, (count / 2) + 1, new Vec3(0, 0.03, 0), 0.1);
            }
        }
    }

    @Override
    public void onPostRegistered(ResourceLocation id) {
        EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void meleeAttackCheck(MeleeAttackCheckEvent event) {
        if (event.getTarget() instanceof Player p && MixinHooks.isEntityTravellerBoosted(p))
            event.setCanceled(true);
    }

    @Override
    public boolean isDamageable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(entity instanceof LivingEntity living) || ((living.getMainHandItem() != stack) && (living.getOffhandItem() != stack)))
            return;

        int activeTick = stack.getOrDefault(ACTIVE_TICK, 0);
        if (activeTick > 0)
            stack.set(ACTIVE_TICK, activeTick - 1);

        int dashTick = stack.getOrDefault(TRAVELLER_ACTIVE_TICK, 0);
        if (dashTick > 0 && entity instanceof Player player && !level.isClientSide) {
            stack.set(TRAVELLER_ACTIVE_TICK, dashTick - 1);

            float d = 1f / DASH_TICKS;
            float completion = 1 - d * dashTick;

            double range = getStatValue(player, stack, "dash", "range") / 2;
            Vec3 delta = stack.getOrDefault(DIRECTION, Vec3.ZERO).normalize().scale(range * 1.5);
            Vec3 eyePos = player.getEyePosition().subtract(0, 0.5, 0);

            boolean right = player.getItemInHand(InteractionHand.MAIN_HAND) == stack == (player.getMainArm() == HumanoidArm.RIGHT);
            Vec3 start = stack.getOrDefault(LAST_POS, eyePos.subtract(delta.scale(0.5).yRot((float) (right ? Math.PI / 10 : -Math.PI / 10))));

            double rot = right ? Math.PI / 9 * completion : -Math.PI / 9 * completion;
            Vec3 end = start.add(delta.scale(d).yRot((float) rot));
            stack.set(LAST_POS, end);

            int fireAspect = stack.getEnchantmentLevel(player.level().holder(Enchantments.FIRE_ASPECT).get());
            double size = 0.53f * (completion + d) + range * 0.04f;
            if (fireAspect == 0) {
                double spread = 0.1 * (completion + d) + range * 0.01;
                ParticleHelper.spawnParticleLine(player.level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, player.level().random), (float) size, 20, 0.8f),
                        start, end, (int) (22 + range * 0.35), new Vec3(0, 0, 0), spread);
                if (dashTick == 1)
                    ParticleHelper.spawnParticleLine(player.level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, player.level().random), (float) (0.33f * (completion + d) + range * 0.007f), 40, 0.86f).withGravity(1.5f),
                            start, end, (int) (15 + range * 0.25), 0.06, spread);
            } else {
                double spread = 0.1 * (completion + d) + range * 0.03;
                ParticleHelper.spawnParticleLine(player.level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(BURN_COLOR, player.level().random), (float) size, 20, 0.8f),
                        start, end, (int) (12 + range * 0.2), new Vec3(0, 0, 0), spread);
                ParticleHelper.spawnParticleLine(player.level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, player.level().random), (float) size, 20, 0.8f),
                        start, end, (int) (12 + range * 0.2), new Vec3(0, 0, 0), spread);
            }

            Vec3 sweep = end.subtract(delta.scale(0.5).yRot(-(float) rot * 1.5f));
            if (dashTick == 1) {
                stack.remove(LAST_POS);
                stack.remove(DIRECTION);

                ParticleHelper.spawnParticleLine(player.level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, player.level().random), (float) (0.53f + range * 0.01f), 20, 0.8f),
                        end, sweep, (33) + 1, new Vec3(0, 0, 0), 0.15 + range * 0.01f);
                ParticleHelper.spawnParticleLine(player.level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, player.level().random), (float) (0.33f + range * 0.02f), 55, 0.84f).withGravity(1.5f),
                        end, sweep, (int) ((15) + 1 + range * 2), 0.06 + range * 0.005f, 0.15 + range * 0.01f);
                ParticleHelper.spawnParticles(level, ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, player.level().random), (float) (0.63f + range * 0.03f), 55, 0.89f).withGravity(1.5f),
                        end, (int) (20 + range * 3), 0.15, 0.15, 0.15, 0.06 + range * 0.005f);
            }

            Set<LivingEntity> targets = Sets.newHashSet(level.getEntitiesOfClass(LivingEntity.class, new AABB(sweep, end).inflate((0.5f * completion + range * 0.06) * 1.3).expandTowards(delta.normalize().scale(0.6 + range * 0.05)), l ->
                    l.isAlive() && l != entity && !l.isSpectator() && l.isPickable()));

            if (dashTick == 1) {
                targets.addAll(level.getEntitiesOfClass(LivingEntity.class, new AABB(sweep, end).inflate((1.3 + range * 0.1) * 1.5).expandTowards(delta.normalize().scale(0.6 + range * 0.05)), l ->
                        l.isAlive() && l != entity && !l.isSpectator() && l.isPickable()));
                targets.addAll(level.getEntitiesOfClass(LivingEntity.class, new AABB(sweep, end).inflate((0.7f + range * 0.07) * 1.3).expandTowards(delta.normalize().scale(0.6 + range * 0.05)), l ->
                        l.isAlive() && l != entity && !l.isSpectator() && l.isPickable()));
            }
            for (var target : targets) {
                DamageSource source = player.damageSources().playerAttack(player);
                target.hurt(source, (float) this.getStatValue(player, stack, "dash", "damage_boost") * getPlayerDamage(player, player.level(), living, stack, source));

                if (fireAspect > 0)
                    target.setRemainingFireTicks(Math.max(living.getRemainingFireTicks(), 60 * fireAspect));

            }
        }

        if (entity instanceof Player p) {
            float charge = stack.getOrDefault(ComponentRegistry.SPEED, 0f);
            if (p.isSprinting()) {
                double chargeAdd = this.getStatValue(living, stack, "travelers_stride", "charge");
                int fireAspect = stack.getEnchantmentLevel(level.holder(Enchantments.FIRE_ASPECT).get());

                if (charge > MAX_CHARGE) {
                    charge -= (float) Math.min(charge - MAX_CHARGE, 0.08);

                    if (!level.isClientSide()) {
                        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, p.level().random), (float) (0.19f +Math.random() * 0.12), 55, 0.89f).withGravity(1.5f), p,
                                1, 0.03f);;

                        if (fireAspect == 0)
                            ParticleHelper.spawnParticles(level, ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, level.random), 0.43f, 45, 0.93f),
                                    p.position(), 3, 0.2, 0.2, 0.2, 0);
                        else {
                            ParticleHelper.spawnParticles(level, ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(BURN_COLOR, level.random), 0.43f, 45, 0.93f),
                                    p.position(), 3, 0.2, 0.2, 0.2, 0);
                            ParticleHelper.spawnParticles(level, ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, level.random), 0.43f, 45, 0.93f),
                                    p.position(), 3, 0.2, 0.2, 0.2, 0);
                        }
                    }
                } else {
                    charge = (float) Math.min(charge + chargeAdd / 20f, MAX_CHARGE);
                }
                stack.set(ComponentRegistry.SPEED, charge);
                if (p.tickCount % 40 == 0)
                    this.addExperience(living, stack, 1);
                DamageSource damagesource = p.damageSources().playerAttack(p);
                applySpeedModifier(p, charge);
                if (charge > 2) {

                    AABB aoe = living.getBoundingBox().inflate(0.5);
                    for (LivingEntity target : living.level().getEntitiesOfClass(LivingEntity.class, aoe, e -> !e.getUUID().equals(living.getUUID()))) {
                        target.hurt(damagesource, (float) (getPlayerDamage(p, level, entity, stack, damagesource) *
                                this.getStatValue(living, stack, "travelers_stride", "stride_damage") / 100
                                * (charge > MAX_CHARGE ? 6 : 1)));
                        p.setLastHurtMob(entity);
                        target.setRemainingFireTicks(Math.max(entity.getRemainingFireTicks(), 30 * fireAspect));
                        this.addExperience(living, stack, 2);
                        Vec3 awayctor = target.position().subtract(living.position()).subtract(p.getDeltaMovement());

                        if (target.isPushable())
                            target.push(awayctor.x() * 0.2 / awayctor.length(), awayctor.y() * 0.2 / awayctor.length(), awayctor.z() * 0.2 / awayctor.length());
                    }
                }
            } else {
                stack.set(ComponentRegistry.SPEED, 0f);
                applySpeedModifier(p, 0);
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

    public void onSwordCut(Player player, ItemStack stack) {
        if (player.level().isClientSide)
            return;

        float speed = hasRangModifier(player, stack, "swordcut", "speed")
                ? 1 + (float) getStatValue(player, stack, "swordcut", "speed") : 1;
        int fireAspect = stack.getEnchantmentLevel(player.level().holder(Enchantments.FIRE_ASPECT).get());
        TravellerCutEntity cutEnt = new TravellerCutEntity(EntityRegistry.TRAVELLER_CUT, player.level(),
                player, stack, fireAspect, speed);
        cutEnt.setPos(player.position());
        cutEnt.setSweepAfterwards(hasRangModifier(player, stack, "swordcut", "sweep"));
        player.level().addFreshEntity(cutEnt);
        setMaxCooldown(player, stack, "swordcut");
    }


    public void onSprintSweep(Player player, ItemStack stack) {
        player.setSprinting(false);

        player.setDeltaMovement(player.getDeltaMovement().multiply(0, 1, 0));
        stack.set(ComponentRegistry.SPEED, 0f);

        if (player.level().isClientSide)
            return;

        int fireAspect = stack.getEnchantmentLevel(player.level().holder(Enchantments.FIRE_ASPECT).get());
        applySpeedModifier(player, 0);
        TravellerSweepEntity sweepEntity = new TravellerSweepEntity(EntityRegistry.TRAVELLER_SWEEP, player.level(),
                player, stack, ((ItemTravellerSword) stack.getItem()).getStatValue(player, stack, "travelers_stride", "sweep_damage_multi"), fireAspect,
                hasRangModifier(player, stack, "travelers_stride", "circle_sword"));
        sweepEntity.setPos(player.position());
        player.level().addFreshEntity(sweepEntity);
    }

    @SubscribeEvent
    public void onAttributeChange(ItemAttributeModifierEvent event) {
        if (event.getItemStack().is(this)) {
            float charge = event.getItemStack().getOrDefault(ComponentRegistry.SPEED, 0f);
            event.addModifier(Attributes.MOVEMENT_SPEED, new AttributeModifier(TRAVELLER_MOVEMENT_SPEED, charge / MAX_CHARGE * 2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.HAND);
        }
    }

    @SubscribeEvent
    public void onItemClickClient(PlayerInteractEvent.LeftClickEmpty click) {
        if (!click.getItemStack().is(this))
            return;

        float charge = click.getItemStack().getOrDefault(ComponentRegistry.SPEED, 0f);
        if (charge >= MAX_CHARGE) {
            Network.sendToServer(new TravellerSweepPacket(click.getItemStack()));
            onSprintSweep(click.getEntity(), click.getItemStack());
            click.getEntity().setSprinting(false);
            click.getEntity().startUsingItem(click.getHand());
            click.getEntity().setDeltaMovement(click.getEntity().getDeltaMovement().multiply(0, 1, 0));
        }

        if (canCast(click.getEntity(), click.getItemStack(), "swordcut") && click.getEntity().isShiftKeyDown()) {
            setMaxCooldown(click.getEntity(), click.getItemStack(), "swordcut");
            Network.sendToServer(new TravellerCutPacket(click.getHand()));
        }
    }

    public boolean isSprintSweepReady(ItemStack stack) {
        float charge = stack.getOrDefault(ComponentRegistry.SPEED, 0f);
        return charge >= MAX_CHARGE;
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        ItemStack stack = event.getItemStack();

        if (!stack.is(this) || event.getLevel().isClientSide)
            return;

        if (isSprintSweepReady(stack)) {
            onSprintSweep(event.getEntity(), stack);
            Network.sendTo(event.getEntity(), new IPacket() {
                @Override
                public void clientExecute(PacketContext ctx) {
                    IPacket.super.clientExecute(ctx);
                    if (Minecraft.getInstance().player != null)
                        Minecraft.getInstance().player.setSprinting(false);
                }
            });
        }

        if (canCast(event.getEntity(), stack, "swordcut") && event.getEntity().isShiftKeyDown()) {
            onSwordCut(event.getEntity(), event.getItemStack());
            setMaxCooldown(event.getEntity(), stack, "swordcut");
        }
    }

    @Override
    public void postHurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);

        if (!stack.is(this) || !(attacker instanceof Player p))
            return;

        float charge = stack.getOrDefault(ComponentRegistry.SPEED, 0f);
        if (charge >= MAX_CHARGE)
            onSprintSweep(p, stack);

        int activeTick = stack.getOrDefault(ComponentRegistry.ACTIVE_TICK, 0);
        if (activeTick > 0 && !p.getCooldowns().isOnCooldown(this))
            onSwordCut(p, stack);
    }

    @Override
    public String getConfigRoute() {
        return "relics";
    }

    private enum Axis {
        X, Z
    }

    @Getter
    @AllArgsConstructor
    public static class TravellerDashPointer implements Iterable<Vec3> {

        private @NotNull Vec3 end;

        private Collection<Vec3> path;

        @Override
        public @NotNull Iterator<Vec3> iterator() {
            return path.iterator();
        }

    }

}
