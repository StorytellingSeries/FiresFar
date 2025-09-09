package com.qurenie.relics_thirteenflames.content.items;

import com.mojang.blaze3d.platform.MacosUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.qurenie.api.IRenderableArmorItem;
import com.qurenie.relics_thirteenflames.client.particles.FeatherParticle;
import com.qurenie.relics_thirteenflames.client.render.entity.IJodahGlowed;
import com.qurenie.relics_thirteenflames.content.entities.MeteorEntity;
import com.qurenie.relics_thirteenflames.content.entities.SkintClusterEntity;
import com.qurenie.relics_thirteenflames.content.entities.SkintOrbEntity;
import com.qurenie.relics_thirteenflames.content.items.misc.MaskState;
import com.qurenie.relics_thirteenflames.init.*;
import com.qurenie.relics_thirteenflames.net.MaskDarkStarPacket;
import com.qurenie.relics_thirteenflames.net.MaskSparkslipPacket;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.init.RelicContainerRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.PredicateType;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootEntry;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.style.BeamsData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
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
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.zeith.hammerlib.net.Network;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class ItemJodahMask extends ArmorItem implements IRelicItem, IRenderableArmorItem {
    
    public static final Color GOLD_COLOR = new Color(200, 150, 20);
    public static final Color GRAY_COLOR = new Color(50, 50, 50);
    public static final Color PURPLE_COLOR = new Color(100, 20, 150);
    
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
                || blockHitVec.distanceToSqr(player.getEyePosition()) < entityResult.getLocation().distanceToSqr(player.getEyePosition()))
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
    
    public static final LootEntry MASK_ENTRY = LootEntry.builder().dimension(".*").biome(".*").table("minecraft:chests/ruined_portal").weight(640).build();
    
    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("planeshift")
                                .maxLevel(4)
                                .active(CastData.builder()
                                        .predicate("planeshift_predicate", PredicateType.VISIBILITY, (p, s) -> {
                                            MaskState state = s.getOrDefault(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL);
                                            return p.getItemBySlot(EquipmentSlot.HEAD) == s;
                                        })
                                        .container(RelicContainerRegistry.INVENTORY.get())
                                        .type(CastType.INSTANTANEOUS)
                                        .build())
                                .stat(StatData.builder("cooldown")
                                        .initialValue(60, 40)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, -0.2)
                                        .thresholdValue(10, 60)
                                        .formatValue(d -> MathUtils.round(d, 1))
                                        .build()
                                )
                                .stat(StatData.builder("durability")
                                        .initialValue(0.5, 1.5)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.5)
                                        .thresholdValue(0.5, 4)
                                        .formatValue(d -> MathUtils.round(d, 1))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityData.builder("sparkslip")
                                .maxLevel(4)
                                .active(CastData.builder()
                                        .predicate("sparkslip_predicate", PredicateType.VISIBILITY, (p, s) -> {
                                            MaskState state = s.getOrDefault(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL);
                                            return p.getItemBySlot(EquipmentSlot.HEAD) == s && state == MaskState.NEUTRAL;
                                        })
                                        .container(RelicContainerRegistry.INVENTORY.get())
                                        .type(CastType.INSTANTANEOUS)
                                        .build())
                                .stat(StatData.builder("cooldown")
                                        .initialValue(45, 35)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, -0.2)
                                        .thresholdValue(8, 45)
                                        .formatValue(d -> MathUtils.round(d, 0))
                                        .build()
                                )
                                .stat(StatData.builder("range")
                                        .initialValue(10, 18)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, 0.35)
                                        .thresholdValue(4, 56)
                                        .formatValue(d -> MathUtils.round(d, 0))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityData.builder("dark_star")
                                .maxLevel(5)
                                .active(CastData.builder()
                                        .container(RelicContainerRegistry.INVENTORY.get())
                                        .type(CastType.INSTANTANEOUS)
                                        .predicate("dark_star_predicate", PredicateType.VISIBILITY, (p, s) -> {
                                            MaskState state = s.getOrDefault(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL);
                                            return p.getItemBySlot(EquipmentSlot.HEAD) == s && state == MaskState.NEUTRAL;
                                        })
                                        .build())
                                .stat(StatData.builder("size")
                                        .initialValue(0.3, 0.8)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, 0.35)
                                        .thresholdValue(0.4, 3.4)
                                        .formatValue(d -> MathUtils.round(d, 1))
                                        .build()
                                )
                                .stat(StatData.builder("cooldown")
                                        .initialValue(120, 80)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, -0.15)
                                        .thresholdValue(30, 120)
                                        .formatValue(d -> MathUtils.round(d, 1))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityData.builder("skint_genesis")
                                .active(CastData.builder()
                                        .container(RelicContainerRegistry.INVENTORY.get())
                                        .type(CastType.INSTANTANEOUS)
                                        .predicate("skint_genesis_predicate", PredicateType.VISIBILITY, (p, s) -> {
                                            MaskState state = s.getOrDefault(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL);
                                            return p.getItemBySlot(EquipmentSlot.HEAD) == s && state != MaskState.NEUTRAL;
                                        })
                                        .build())
                                .maxLevel(4)
                                .stat(StatData.builder("damage")
                                        .initialValue(2, 4)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, 0.27)
                                        .thresholdValue(2, 10)
                                        .formatValue(d -> MathUtils.round(d, 0))
                                        .build()
                                )
                                .stat(StatData.builder("clusters")
                                        .initialValue(3, 5)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, 0.27)
                                        .thresholdValue(3, 13)
                                        .formatValue(Math::floor)
                                        .build()
                                )
                                .stat(StatData.builder("shard_count")
                                        .initialValue(1, 1.5)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.5)
                                        .thresholdValue(1, 3)
                                        .formatValue(Math::floor)
                                        .build()
                                )
                                .stat(StatData.builder("cooldown")
                                        .initialValue(150, 120)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, -0.23)
                                        .thresholdValue(40, 150)
                                        .formatValue(d -> MathUtils.round(d, 1))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityData.builder("reversal_aberration")
                                .maxLevel(3)
                                .requiredPoints(2)
                                .requiredLevel(7)
                                .active(CastData.builder()
                                        .predicate("reversal_aberration_predicate", PredicateType.VISIBILITY, (p, s) -> {
                                            MaskState state = s.getOrDefault(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL);
                                            return p.getItemBySlot(EquipmentSlot.HEAD) == s && state != MaskState.NEUTRAL;
                                        })
                                        .container(RelicContainerRegistry.INVENTORY.get())
                                        .type(CastType.INSTANTANEOUS)
                                        .build())
                                .stat(StatData.builder("skint_bonus")
                                        .initialValue(1, 2)
                                        .upgradeModifier(UpgradeOperation.ADD, 1.4)
                                        .thresholdValue(1, 6)
                                        .formatValue(Math::floor)
                                        .build()
                                )
                                .stat(StatData.builder("cooldown")
                                        .initialValue(120, 90)
                                        .upgradeModifier(UpgradeOperation.ADD, -23)
                                        .thresholdValue(20, 120)
                                        .formatValue(d -> MathUtils.round(d, 1))
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .style(StyleData.builder()
                        .beams(BeamsData.builder().startColor(-65281).endColor(255).build())
                        .build())
                .leveling(new LevelingData(100, 16, 75))
                .loot(LootData.builder().entry(MASK_ENTRY).build())
                .build();
        
    }
    
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, List<net.minecraft.network.chat.Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("tooltip.relics_thirteenflames.jodah_mask.lore").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, tooltip, isAdvanced);
    }
    
    @Override
    public String getConfigRoute() {
        return "relics";
    }
    
    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        switch (ability) {
            case "planeshift" -> castPlaneshift(stack, player);
            case "sparkslip" -> castSparkslip(stack, player);
            case "dark_star" -> castDarkStar(stack, player);
            case "skint_genesis" -> castScintGenesis(stack, player);
            case "reversal_aberration" -> castReversalAberration(stack, player);
        }
    }
    
    public void castPlaneshift(ItemStack stack, Player player) {
        if (player.level().isClientSide)
            return;
        
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
                FlamesUtils.addSkint(stack, player, 1);
                break;
            case DUSK:
                player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10), e -> e != player)
                        .forEach(e -> {
                            DamageSource source = e.damageSources().playerAttack(player);
                            e.hurt(source, getPlayerDamage(player, player.level(), e, player.getMainHandItem(), source));
                            FlamesUtils.addAntiskint(stack, e, 1);
                        });
            default:
                int scints = player.getData(AttachmentsRegistry.SKINT_DATA);
                if (scints < 0)
                    break;
                
                ParticleHelper.spawnParticleEntity(ParticleHelper.constructHeal(GOLD_COLOR, 0.9f,
                        70, 0.98f), player, 5 * scints, 0.24);
                player.heal(scints * 2);
                addRelicExperience(stack, scints);
                
                FlamesUtils.addSkint(player, -scints, 0);
        }
        
        addRelicExperience(stack, 2);
        stack.set(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL);
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
    
    public void castSparkslip(ItemStack stack, Player player) {
        Level level = player.level();
        
        if (!level.isClientSide)
            return;
        
        double distance = getStatValue(stack, "sparkslip", "range");
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
            return;
        
        Network.sendToServer(new MaskSparkslipPacket(livingTarget, target));
    }
    
    public void castScintGenesis(ItemStack stack, Player player) {
        int scints = player.getData(AttachmentsRegistry.SKINT_DATA);
        
        stack.set(ComponentRegistry.SKINT_GENESIS_COUNT, (int) getStatValue(stack, "skint_genesis", "clusters") + scints * 2);
        stack.set(ComponentRegistry.ACTIVE_TICK, 200);
        
        if (!player.level().isClientSide)
            FlamesUtils.addSkint(player, -scints, 0);
        
        stack.set(ComponentRegistry.CLUSTERS_MASK_STATE, stack.getOrDefault(ComponentRegistry.MASK_STATE, MaskState.SPARKLING));
        stack.set(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL);
        addAbilityCooldown(stack, "skint_genesis", (int) (getStatValue(stack, "skint_genesis", "cooldown") * 20));
    }
    
    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        
        if (level.isClientSide || !(entity instanceof Player player))
            return;
        
        int clusters = stack.getOrDefault(ComponentRegistry.SKINT_GENESIS_COUNT, 0);
        int ticks = stack.getOrDefault(ComponentRegistry.ACTIVE_TICK, 0);
        if (clusters <= 0) {
            if (ticks > 0)
                stack.set(ComponentRegistry.ACTIVE_TICK, 0);
            
            return;
        }
        
        if (ticks == 0) {
            stack.set(ComponentRegistry.SKINT_GENESIS_COUNT, 0);
            return;
        }
        
        if (player.tickCount % 3 != 0 || player.getItemBySlot(getEquipmentSlot(stack)) != stack)
            return;
        
        final double radius = 12;
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(radius, 2, radius),
                        e -> e != entity && !(e instanceof SkintClusterEntity))
                .stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(player))).limit(3).toList();
        
        int i = Math.min(clusters, 3);
        for (var e : targets) {
            BlockPos pos = e.blockPosition().below();
            if (level.random.nextBoolean() && !level.getBlockState(pos).isAir() && level.getBlockState(e.blockPosition()).isAir()) {
                double height = FlamesUtils.getBlockHeightSafety(level, pos);
                Vec3 position = new Vec3(e.position().x, pos.getY() + height, e.position().z);
                List<LivingEntity> c = level.getEntitiesOfClass(LivingEntity.class, new AABB(position.subtract(0.5, 0, 0.5), position.add(0.5, 1, 0.5)),
                        e$ -> e$ instanceof SkintClusterEntity || e$ == entity);
                
                if (!c.isEmpty())
                    continue;
                
                int count = (int) getStatValue(stack, "skint_genesis", "shard_count");
                float damage = (float) getStatValue(stack, "skint_genesis", "damage");
                int limit = (int) getStatValue(stack, "reversal_aberration", "skint_bonus");
                SkintClusterEntity cluster = new SkintClusterEntity(EntityRegistry.SKINT_CLUSTER, level, stack.get(ComponentRegistry.CLUSTERS_MASK_STATE) == MaskState.SPARKLING ? SkintClusterEntity.Type.SKINT : SkintClusterEntity.Type.ANTISKINT, player, damage, count, limit);
                cluster.setPos(position);
                level.addFreshEntity(cluster);
                
                i--;
            }
        }
        
        int j = i;
        while (j-- > 0) {
            double x = (0.5 - level.random.nextDouble()) * 2 * radius;
            double z = (0.5 - level.random.nextDouble()) * 2 * Math.sqrt(radius * radius - x * x);
            
            for (int dy : List.of(0, 1, -1, -2)) {
                Vec3 blockPosVec = player.position().add(x, dy, z);
                BlockPos blockPos = new BlockPos((int) blockPosVec.x, (int) blockPosVec.y, (int) blockPosVec.z);
                
                if (level.getBlockState(blockPos).isAir() && !level.getBlockState(blockPos.below()).isAir()) {
                    double height = level.getBlockFloorHeight(blockPos.below());
                    Vec3 position = new Vec3(blockPosVec.x, blockPosVec.y - 1 + height, blockPosVec.z);
                    List<LivingEntity> c = level.getEntitiesOfClass(LivingEntity.class, new AABB(position.subtract(0.5, 0, 0.5), position.add(0.5, 1, 0.5)),
                            e$ -> e$ instanceof SkintClusterEntity || e$ == entity);
                    if (!c.isEmpty())
                        continue;
                    
                    int count = (int) getStatValue(stack, "skint_genesis", "shard_count");
                    float damage = (float) getStatValue(stack, "skint_genesis", "damage");
                    int limit = (int) getStatValue(stack, "reversal_aberration", "skint_bonus");
                    SkintClusterEntity cluster = new SkintClusterEntity(EntityRegistry.SKINT_CLUSTER, level, stack.get(ComponentRegistry.CLUSTERS_MASK_STATE) == MaskState.SPARKLING ? SkintClusterEntity.Type.SKINT : SkintClusterEntity.Type.ANTISKINT, player, damage, count, limit);
                    cluster.setPos(position);
                    level.addFreshEntity(cluster);
                    
                    i--;
                    break;
                }
            }
        }
        
        stack.set(ComponentRegistry.SKINT_GENESIS_COUNT, clusters - 3 + i);
    }
    
    public void castDarkStar(ItemStack stack, Player player) {
        EntityHitResult entityResult = ProjectileUtil.getEntityHitResult(
                player.level(),
                player,
                player.getEyePosition(),
                player.getEyePosition().add(player.getLookAngle().scale(100)),
                player.getBoundingBox().inflate(2).expandTowards(player.getLookAngle().scale(100)),
                entity -> !entity.isSpectator() && entity.isPickable()
                        && entity instanceof LivingEntity living && living.isAlive()
        );
        
        Network.sendToServer(new MaskDarkStarPacket(entityResult == null ? null : entityResult.getEntity()));
    }
    
    public void onDarkStarPacket(ItemStack stack, Player player, @Nullable LivingEntity target) {
        float size = (float) getStatValue(stack, "dark_star", "size");
        MeteorEntity meteorEntity = new MeteorEntity(player.level(), player, target, size);
        player.level().addFreshEntity(meteorEntity);
        stack.set(ComponentRegistry.MASK_STATE, MaskState.SPARKLING);
        addAbilityCooldown(stack, "dark_star", (int) (getStatValue(stack, "dark_star", "cooldown") * 20));
    }
    
    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }
    
    public void castReversalAberration(ItemStack stack, Player player) {
        if (player.level().isClientSide)
            return;
        
        int limitBonus = (int) getStatValue(stack, "reversal_aberration", "skint_bonus");
        MaskState state = stack.getOrDefault(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL);
        
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(20, 10, 20), e -> e != player && e.isAlive() && e.isPickable() && !e.isSpectator());
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
                    SkintOrbEntity orb = new SkintOrbEntity(player.level(), SkintOrbEntity.Type.SKINT, player, e, limitBonus);
                    player.level().addFreshEntity(orb);
                }
                e.setData(AttachmentsRegistry.ANTISKINT_DATA, 0);
                FlamesUtils.addAntiskint(e, scints, limitBonus);
                FlamesUtils.Net.startTrackingSkintAttachments(e);
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
            addAbilityCooldown(stack, "reversal_aberration", (int) (getStatValue(stack, "reversal_aberration", "cooldown") * 20));
        } else {
            addAbilityCooldown(stack, "reversal_aberration", 60);
        }
    }
    
    public void onSparkslipPacket(ItemStack stack, Player player, Vec3 spawnPos, @Nullable LivingEntity target) {
        player.teleportTo(spawnPos.x, spawnPos.y, spawnPos.z);
        boolean jodahTeleport = false;
        if (target != null) {
            if (isJodahTarget(target, player)) {
                jodahTeleport = true;
                FlamesUtils.addAntiskint(stack, target, 4);
            }
            target.addEffect(new MobEffectInstance(EffectsRegistry.DISABILITY_EFFECT, jodahTeleport ? 25 : 15));
        }
        
        stack.set(ComponentRegistry.MASK_STATE, MaskState.DUSK);
        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(jodahTeleport ? GRAY_COLOR : GOLD_COLOR, 0.4f,
                50, 0.96f).withLightning(!jodahTeleport), player, 45, 0.1);
        
        if (!jodahTeleport)
            FlamesUtils.addSkint(stack, player, 2);
        else {
            ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(PURPLE_COLOR, 0.4f,
                    50, 0.96f), player, 25, 0.1);
            ParticleHelper.spawnParticleEntity(new FeatherParticle.Options(0.3f, 70), player, 20, 0.3);
            player.swing(player.getMainHandItem().getItem() == ItemsRegistry.JODAH_STAFF ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, true);
        }
        
        addRelicExperience(stack, 2);
        addAbilityCooldown(stack, "sparkslip", (int) (getStatValue(stack, "sparkslip", "cooldown") * 20));
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
            
            if (event.getEntity() instanceof Player p && event.getFrom().getItem() == ItemsRegistry.JODAH_MASK && p.hasData(AttachmentsRegistry.PLANESHIFT_TICK))
                ItemsRegistry.JODAH_MASK.onPlaneshiftEnd(p);
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
                        ItemsRegistry.JODAH_MASK.setAbilityCooldown(head, "planeshift", 20 * (int) ItemsRegistry.JODAH_MASK.getStatValue(head, "planeshift", "cooldown"));
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
