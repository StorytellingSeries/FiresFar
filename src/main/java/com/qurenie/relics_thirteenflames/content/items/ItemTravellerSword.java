package com.qurenie.relics_thirteenflames.content.items;

import com.google.common.collect.ImmutableMultimap;
import com.qurenie.api.MeleeAttackCheckEvent;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.entities.TravellerCutEntity;
import com.qurenie.relics_thirteenflames.content.entities.TravellerSweepEntity;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.net.TravellerCutPacket;
import com.qurenie.relics_thirteenflames.net.TravellerSweepPacket;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.MixinHooks;
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
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

public class ItemTravellerSword extends SwordItem implements IRelicItem, IRegisterListener {
    
    public static final ResourceLocation TRAVELLER_STEP_HEIGHT = ThirteenFlames.rl("traveller_step_height");
    public static final ResourceLocation TRAVELLER_MOVEMENT_SPEED = ThirteenFlames.rl("traveller_movement_speed");
    private static final float MAX_CHARGE = 5f;
    private static final int DASH_TICKS = 3;
    
    private static final Color COLOR = new Color(30, 170, 170);
    private static final Color BURN_COLOR = new Color(230, 90, 20);
    
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
        AABB movedBox = playerBox.move(targetPos.x - player.getX(), targetPos.y - player.getY(), targetPos.z - player.getZ()).deflate(0.3);
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
            
            for (double coord = min; coord <= max; coord += 1) {
                double t;
                if (axis == Axis.X) {
                    if (direction.x == 0) continue;
                    t = (coord - start.x) / direction.x;
                } else {
                    if (direction.z == 0) continue;
                    t = (coord - start.z) / direction.z;
                }
                
                if (t < 0 || t > 1) continue;
                
                Vec3 rawPoint = start.add(norm.scale(length * t));
                rawPoints.add(rawPoint);
            }
        }
        
        if (rawPoints.isEmpty())
            return null;
        
        for (Vec3 rawPoint : rawPoints) {
            if (!isWalkable(level, player, rawPoint)) break;
            
            Vec3 projected = projectToSurface(level, rawPoint.x, rawPoint.y, rawPoint.z, inAir ? 1 : 3);
            
            if (projected != null)
                collected.add(projected);
            else if (inAir)
                collected.add(rawPoint);
        }
        
        if (collected.isEmpty())
            return null;
        
        return new TravellerDashPointer(collected.getLast(), collected);
    }
    
    @Override
    public <T extends LivingEntity> int damageItem(@NotNull ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        return 0;
    }
    
    @Override
    public boolean isPrimaryItemFor(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        return enchantment != Enchantments.UNBREAKING && enchantment.value().definition()
                .primaryItems().flatMap(HolderSet::unwrapKey).map(tag -> tag == ItemTags.SWORD_ENCHANTABLE)
                .orElse(false) || enchantment.value().definition()
                .supportedItems().unwrapKey().map(tag -> tag == ItemTags.WEAPON_ENCHANTABLE)
                .orElse(false);
    }
    
    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("travelers_stride")
                                .maxLevel(4)
                                .stat(StatData.builder("charge")
                                        .initialValue(1, 3)
                                        .upgradeModifier(UpgradeOperation.ADD, 2)
                                        .thresholdValue(1, 10)
                                        .build()
                                )
                                .stat(StatData.builder("stride_damage")
                                        .initialValue(10, 15)
                                        .upgradeModifier(UpgradeOperation.ADD, 4)
                                        .thresholdValue(10, 30)
                                        .build()
                                )
                                .stat(StatData.builder("sweep_damage_multi")
                                        .initialValue(0.5, 1)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.5)
                                        .thresholdValue(0.5, 3)
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityData.builder("dash")
                                .maxLevel(5)
                                .active(CastData.builder()
                                        .container(RelicContainerRegistry.INVENTORY.get())
                                        .type(CastType.INSTANTANEOUS)
                                        .predicate("dash_predicate", PredicateType.VISIBILITY, (p, s) -> p.getMainHandItem() == s || p.getOffhandItem() == s)
                                        .build())
                                .stat(StatData.builder("range")
                                        .initialValue(4, 7)
                                        .upgradeModifier(UpgradeOperation.ADD, 1)
                                        .thresholdValue(4, 12)
                                        .build()
                                )
                                .stat(StatData.builder("damage_boost")
                                        .initialValue(0.5, 1)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.6)
                                        .thresholdValue(0.5, 6)
                                        .build()
                                )
                                .stat(StatData.builder("cooldown")
                                        .initialValue(15, 10)
                                        .thresholdValue(3, 15)
                                        .upgradeModifier(UpgradeOperation.ADD, -1.5)
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityData.builder("swordcut")
                                .requiredPoints(2)
                                .requiredLevel(9)
                                .maxLevel(2)
                                .active(CastData.builder()
                                        .container(RelicContainerRegistry.INVENTORY.get())
                                        .type(CastType.INSTANTANEOUS)
                                        .predicate("swordcut_predicate", PredicateType.VISIBILITY,
                                                (p, s) -> this.getAbilityLevel(s, "swordcut") > 1 && p.getMainHandItem() == s || p.getOffhandItem() == s)
                                        .build())
                                .build()
                        )
                        .build()
                )
                .leveling(new LevelingData(100, 11, 100))
                .loot(LootData.builder().entry(LootEntries.MINESHAFT).entry(LootEntries.END_LIKE).build())
                .build();
    }
    
    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        if (ability.equals("dash")) {
            this.addAbilityCooldown(stack, "dash", 20 * (int) this.getStatValue(stack, "dash", "cooldown"));
            stack.set(TRAVELLER_ACTIVE_TICK, DASH_TICKS);
            
            double range = ItemsRegistry.TRAVELLER_SWORD.getStatValue(stack, "dash", "range");
            Vec3 lookAngle = player.getLookAngle();
            double scale = 1 / lookAngle.multiply(1, 0, 1).length();
            boolean onSurface = !player.isFallFlying() && !player.isInFluidType() && !player.level().getBlockState(player.blockPosition().below()).isAir();
            TravellerDashPointer pointer = createPointer(player.level(), player, player.getEyePosition(),
                    player.getEyePosition().add(player.getLookAngle().scale(scale * range)), !onSurface);
            
            if (pointer == null)
                return;
            
            int fireAspect = stack.getEnchantmentLevel(player.level().holder(Enchantments.FIRE_ASPECT).get());
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
            player.moveTo(pointer.getEnd());
            player.swing(stack == player.getItemInHand(InteractionHand.MAIN_HAND) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            
            if (player.level().isClientSide)
                return;
            
            Vec3 last = player.position();
            for (Vec3 pos : pointer) {
                AABB aabb = new AABB(pos, last).inflate(1.5, 1.5, 1.5).expandTowards(0, 4, 0);
                for (LivingEntity living : player.level().getEntitiesOfClass(LivingEntity.class, aabb, l -> l != player)) {
                    living.setLastHurtByPlayer(player);
                    DamageSource source = player.damageSources().playerAttack(player);
                    living.hurt(source, (float) this.getStatValue(stack, "dash", "damage_boost") * getPlayerDamage(player, player.level(), living, stack, source) * 0.75f);
                    
                    if (fireAspect > 0)
                        living.setRemainingFireTicks(Math.max(living.getRemainingFireTicks(), 60 * fireAspect));
                    
                    if (stack.getItem() == ItemsRegistry.TRAVELLER_SWORD)
                        ItemsRegistry.TRAVELLER_SWORD.addRelicExperience(stack, 5);
                }
            }

//            if (!player.level().isClientSide) {
//                TravellerAfterdashEntity afterdash = new TravellerAfterdashEntity(
//                        EntityRegistry.TRAVELLER_AFTERDASH,
//                        player.level(),
//                        player,
//                        fireAspect,
//                        (int) (this.getStatValue(stack, "dash", "range")),
//                        (float) this.getStatValue(stack, "dash", "damage_boost") * 8
//                );
//                player.level().addFreshEntity(afterdash);
//            }
            
            ItemsRegistry.TRAVELLER_SWORD.addRelicExperience(stack, 10);
        } else if (ability.equals("swordcut")) {
            stack.set(ACTIVE_TICK, 400);
            addAbilityCooldown(stack, "swordcut", 1000);
        }
    }
    
    private void spawnDashParticles(Level level, Vec3 point, Vec3 lastPoint, boolean upParticles, boolean onFire) {
        double rate = 0.1;
        
        double distance = point.distanceTo(lastPoint);
        int count = (int) (distance / rate);
        
        if (!onFire)
            ParticleHelper.spawnParticleLine(level, ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(COLOR, level.random), 0.43f, 70, 0.95f),
                    point, lastPoint, count, 0, 0.2);
        else {
            ParticleHelper.spawnParticleLine(level, ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(BURN_COLOR, level.random), 0.43f, 70, 0.95f),
                    point, lastPoint, (count / 2) + 1, 0, 0.2);
            ParticleHelper.spawnParticleLine(level, ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(COLOR, level.random), 0.43f, 70, 0.95f),
                    point, lastPoint, (count / 2) + 1, 0, 0.2);
        }
        
        point = point.add(0, 0.1, 0);
        lastPoint = lastPoint.add(0, 0.1, 0);
        if (upParticles) {
            if (!onFire)
                ParticleHelper.spawnParticleLine(level, ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(COLOR, level.random), 0.43f, 60 + level.random.nextInt() * 30, 0.95f),
                        point, lastPoint, count * 2, new Vec3(0, 0.03, 0), 0.1);
            else {
                ParticleHelper.spawnParticleLine(level, ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(COLOR, level.random), 0.43f, 60 + level.random.nextInt() * 30, 0.95f),
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
            
            double range = getStatValue(stack, "dash", "range") / 2;
            Vec3 delta = stack.getOrDefault(DIRECTION, Vec3.ZERO).normalize().scale(range * 1.5);
            Vec3 eyePos = player.getEyePosition().subtract(0, 0.5, 0);
            
            boolean right = player.getItemInHand(InteractionHand.MAIN_HAND) == stack == (player.getMainArm() == HumanoidArm.RIGHT);
            Vec3 start = stack.getOrDefault(LAST_POS, eyePos.subtract(delta.scale(0.5).yRot((float) (right ? Math.PI / 10 : -Math.PI / 10))));
            
            double rot = right ? Math.PI / 9 * completion : -Math.PI / 9 * completion;
            Vec3 end = start.add(delta.scale(d).yRot((float) rot));
            stack.set(LAST_POS, end);
            
            int fireAspect = stack.getEnchantmentLevel(player.level().holder(Enchantments.FIRE_ASPECT).get());
            if (fireAspect == 0) {
                ParticleHelper.spawnParticleLine(player.level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(COLOR, player.level().random), 0.53f * (completion + d), 20, 0.8f),
                        start, end, 22, new Vec3(0, 0, 0), 0.1 * (completion + d));
                if (dashTick == 1)
                    ParticleHelper.spawnParticleLine(player.level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(COLOR, player.level().random), 0.33f * (completion + d), 40, 0.86f).withGravity(1.5f),
                            start, end, 15, 0.06, 0.1 * (completion + d));
            } else {
                ParticleHelper.spawnParticleLine(player.level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(BURN_COLOR, player.level().random), 0.53f * (completion + d), 20, 0.8f),
                        start, end, (11) + 1, new Vec3(0, 0, 0), 0.1 * (completion + d));
                ParticleHelper.spawnParticleLine(player.level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(COLOR, player.level().random), 0.53f * (completion + d), 20, 0.8f),
                        start, end, (11) + 1, new Vec3(0, 0, 0), 0.1 * (completion + d));
            }
            
            if (dashTick == 1) {
                stack.remove(LAST_POS);
                stack.remove(DIRECTION);
                
                ParticleHelper.spawnParticleLine(player.level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(COLOR, player.level().random), 0.53f, 20, 0.8f),
                        end, end.subtract(delta.scale(0.5).yRot(-(float) rot * 1.5f)), (33) + 1, new Vec3(0, 0, 0), 0.15);
                ParticleHelper.spawnParticleLine(player.level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(COLOR, player.level().random), 0.33f, 40, 0.84f).withGravity(1.5f),
                        end, end.subtract(delta.scale(0.5).yRot(-(float) rot * 1.5f)), (22) + 1, 0.06, 0.15);
                ParticleHelper.spawnParticles(level, ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(COLOR, player.level().random), 0.63f, 40, 0.89f).withGravity(1.5f),
                        end, 30, 0.15, 0.15, 0.15, 0.06);
            }
            
            var targets = level.getEntitiesOfClass(LivingEntity.class, new AABB(start, end).inflate(0.6f * completion), l ->
                    l.isAlive() && l != entity && !l.isSpectator() && l.isPickable());
            for (var target : targets) {
                DamageSource source = player.damageSources().playerAttack(player);
                target.hurt(source, (float) this.getStatValue(stack, "dash", "damage_boost") * getPlayerDamage(player, player.level(), living, stack, source));
                
                if (fireAspect > 0)
                    target.setRemainingFireTicks(Math.max(living.getRemainingFireTicks(), 60 * fireAspect));
                
            }
        }
        
        if (entity instanceof Player p) {
            float charge = stack.getOrDefault(ComponentRegistry.SPEED, 0f);
            if (p.isSprinting()) {
                charge = (float) Math.min(charge + this.getStatValue(stack, "travelers_stride", "charge") / 20f, MAX_CHARGE);
                stack.set(ComponentRegistry.SPEED, charge);
                if (p.tickCount % 40 == 0)
                    this.addRelicExperience(stack, 1);
                DamageSource damagesource = p.damageSources().playerAttack(p);
                applySpeedModifier(p, charge);
                if (charge > 2) {
                    int fireAspect = stack.getEnchantmentLevel(level.holder(Enchantments.FIRE_ASPECT).get());
                    
                    AABB aoe = living.getBoundingBox().inflate(0.5);
                    for (LivingEntity target : living.level().getEntitiesOfClass(LivingEntity.class, aoe, e -> !e.getUUID().equals(living.getUUID()))) {
                        target.hurt(damagesource, (float) (getPlayerDamage(p, level, entity, stack, damagesource) *
                                this.getStatValue(stack, "travelers_stride", "stride_damage") / 100));
                        p.setLastHurtMob(entity);
                        target.setRemainingFireTicks(Math.max(entity.getRemainingFireTicks(), 30 * fireAspect));
                        this.addRelicExperience(stack, 2);
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
        if (player.level().isClientSide || player.getCooldowns().getCooldownPercent(this, 0f) > 0)
            return;
        
        int fireAspect = stack.getEnchantmentLevel(player.level().holder(Enchantments.FIRE_ASPECT).get());
        TravellerCutEntity cutEnt = new TravellerCutEntity(EntityRegistry.TRAVELLER_CUT, player.level(),
                player, stack, fireAspect);
        cutEnt.setPos(player.position());
        player.level().addFreshEntity(cutEnt);
        player.getCooldowns().addCooldown(this, 10);
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
                player, stack, ((IRelicItem) stack.getItem()).getStatValue(stack, "travelers_stride", "sweep_damage_multi"), fireAspect);
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
            click.getEntity().setSprinting(false);
            click.getEntity().startUsingItem(click.getHand());
            click.getEntity().setDeltaMovement(click.getEntity().getDeltaMovement().multiply(0, 1, 0));
        } else {
            int activeTick = click.getItemStack().getOrDefault(ComponentRegistry.ACTIVE_TICK, 0);
            if (activeTick > 0 && !click.getEntity().getCooldowns().isOnCooldown(this))
                Network.sendToServer(new TravellerCutPacket(click.getItemStack()));
        }
    }
    
    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        ItemStack stack = event.getItemStack();
        
        if (!stack.is(this) || event.getLevel().isClientSide)
            return;
        
        float charge = stack.getOrDefault(ComponentRegistry.SPEED, 0f);
        if (charge >= MAX_CHARGE) {
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
        
        int activeTick = event.getItemStack().getOrDefault(ComponentRegistry.ACTIVE_TICK, 0);
        if (activeTick > 0 && !event.getEntity().getCooldowns().isOnCooldown(this))
            onSwordCut(event.getEntity(), event.getItemStack());
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
