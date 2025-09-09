package com.qurenie.relics_thirteenflames.content.items;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.client.particles.FeatherParticle;
import com.qurenie.relics_thirteenflames.client.render.entity.IJodahGlowed;
import com.qurenie.relics_thirteenflames.client.render.misc.JodahStaffRenderUtil;
import com.qurenie.relics_thirteenflames.content.entities.JodahHealEntity;
import com.qurenie.relics_thirteenflames.content.items.misc.JodahTier;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
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
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.api.fml.IRegisterListener;
import org.zeith.hammerlib.util.charging.ItemChargeHelper;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.*;
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

public class ItemJodahStaff extends SwordItem implements IRelicItem, IRegisterListener {
    
    public static final int DURATION = 72000;
    private static final Color PURPLE_COLOR = new Color(160, 20, 140);
    
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
    
    @Override
    public void onPostRegistered(ResourceLocation id) {
        EVENT_BUS.register(this);
    }
    
    @Override
    public boolean isDamageable(@NotNull ItemStack stack) {
        return false;
    }
    
    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("ranging")
                                .maxLevel(4)
                                .stat(StatData.builder("increasing")
                                        .initialValue(0, 1)
                                        .upgradeModifier(UpgradeOperation.ADD, 1)
                                        .thresholdValue(0, 4)
                                        .build()
                                )
                                .stat(StatData.builder("damage_modifier")
                                        .initialValue(1, 1.2)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, 0.2)
                                        .thresholdValue(1, 2.5)
                                        .formatValue(d -> MathUtils.round(d * 100, 0))
                                        .build()
                                )
                                .stat(StatData.builder("xp_consume")
                                        .initialValue(1, 1.15)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, 0.15)
                                        .thresholdValue(1, 3)
                                        .formatValue(d -> MathUtils.round(100 - 100 / d, 0))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityData.builder("health_theft")
                                .requiredPoints(1)
                                .maxLevel(3)
                                .active(CastData.builder()
                                        .container(RelicContainerRegistry.INVENTORY.get())
                                        .type(CastType.INSTANTANEOUS)
                                        .predicate("health_theft_predicate", PredicateType.VISIBILITY, (p, s) -> p.getMainHandItem() == s || p.getOffhandItem() == s)
                                        .build())
                                .stat(StatData.builder("cooldown")
                                        .initialValue(80, 70)
                                        .thresholdValue(40, 80)
                                        .upgradeModifier(UpgradeOperation.ADD, -10)
                                        .formatValue(d -> MathUtils.round(d, 0))
                                        .build()
                                )
                                .stat(StatData.builder("entity_count")
                                        .initialValue(1, 1.5)
                                        .thresholdValue(1, 3)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.5)
                                        .formatValue(d -> MathUtils.round(Math.floor(d), 0))
                                        .build()
                                )
                                .stat(StatData.builder("xp_modifier")
                                        .initialValue(1, 1.15)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, 0.15)
                                        .thresholdValue(1, 2)
                                        .formatValue(d -> MathUtils.round((d - 1) * 100, 0))
                                        .build()
                                )
                                .stat(StatData.builder("durability")
                                        .initialValue(4, 8)
                                        .thresholdValue(4, 21)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, 0.5)
                                        .formatValue(d -> MathUtils.round(d, 1))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityData.builder("one_thousand_eyes")
                                .requiredPoints(2)
                                .requiredLevel(7)
                                .maxLevel(1)
                                .stat(StatData.builder("empty").build())
                                .active(CastData.builder()
                                        .predicate("one_thousand_eyes_predicate", PredicateType.VISIBILITY, (p, s) ->
                                                getAbilityLevel(s, "one_thousand_eyes") > 1)
                                        .container(RelicContainerRegistry.INVENTORY.get())
                                        .type(CastType.INSTANTANEOUS)
                                        .predicate("one_thousand_eyes_predicate", PredicateType.VISIBILITY, (p, s) -> p.getMainHandItem() == s || p.getOffhandItem() == s)
                                        .build())
                                .build()
                        )
                        .build()
                )
                .leveling(new LevelingData(100, 10, 200))
                .loot(LootData.builder().entry(END_LIKE).build())
                .build();
    }
    
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, List<net.minecraft.network.chat.Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("tooltip.relics_thirteenflames.jodah_staff.lore").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, tooltip, isAdvanced);
    }
    
    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        if (ability.equals("health_theft")) {
            stack.set(ACTIVE_TICK, 20 * (int) getStatValue(stack, "health_theft", "durability"));
            addAbilityCooldown(stack, "health_theft", 20 * (int) getStatValue(stack, "health_theft", "cooldown"));
        }
        if (ability.equals("one_thousand_eyes") && getAbilityLevel(stack, "one_thousand_eyes") > 0) {
            if (!player.level().isClientSide)
                player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(40), e -> e != player)
                        .forEach(e -> e.addEffect(new MobEffectInstance(EffectsRegistry.JODAH_VISION, 500,
                                stack.getOrDefault(ComponentRegistry.JODAH_TIER, JodahTier.D).oneThousandEyes.targetingCount() - 1)));
            stack.set(JODAH_ACTIVE_TICK, 500);
            addAbilityCooldown(stack, "one_thousand_eyes", 1200);
            if (!player.level().isClientSide)
                FlamesUtils.startJodahWings(player, true);
//            player.setData(AttachmentsRegistry.WINGS_LAYER_DATA, JodahWingsLayer.ANIMATION_LENGTH);
        }
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
                .limit((int) getStatValue(stack, "health_theft", "entity_count"))
                .toList();
        
        for (LivingEntity target : targets) {
            int xp = (int) (tier.thief.xp() * getStatValue(stack, "health_theft", "xp_modifier"));
            if (target instanceof Player targetPlayer) {
                xp = Math.min(targetPlayer.totalExperience, xp);
                targetPlayer.giveExperiencePoints(-xp);
            }
            addRelicExperience(stack, 2);
            level.addFreshEntity(new JodahHealEntity(EntityRegistry.JODAH_HEAL, level, Math.min(tier.thief.health(), target.getHealth()), xp, living, target));
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
                
                ParticleHelper.spawnParticles(player.level(), ParticleHelper.constructFigure(Color.GRAY, 0.12f,
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
            int bonus = (int) getStatValue(stack, "ranging", "increasing");
            int level = stack.getOrDefault(ComponentRegistry.LEVEL, 0) + 1;
            
            if (level + bonus >= tier.hitCount) {
                stack.set(ComponentRegistry.JODAH_TIER, tier.next());
                stack.set(ComponentRegistry.LEVEL, 0);
            } else {
                stack.set(ComponentRegistry.LEVEL, level);
            }
            
            float fineReduce = (int) getStatValue(stack, "ranging", "xp_consume");
            if ((attacker instanceof Player p)) {
                int reduce = Math.min(p.totalExperience, (int) (tier.xpSuck / fineReduce));
                p.giveExperiencePoints(-reduce);
                addRelicExperience(stack, (int) (tier.xpSuck * (reduce / Math.floor(tier.xpSuck / fineReduce))));
            }
        }
    }
    
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (level.isClientSide)
            return super.use(level, player, usedHand);
        
        ItemStack stack = player.getItemInHand(usedHand);
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
                        && living.isAlive() &&
                        (isWalkable(level, player, living.position()) || isWalkable(level, player, living.position().subtract(0, 1, 0)))
        );
        
        if (entityHit != null) {
            LivingEntity living = (LivingEntity) entityHit.getEntity();
            
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
            player.teleportTo(living.position().x,
                    isWalkable(player.level(), player, living.position()) ? living.position().y : living.position().y - 1, living.position().z);
            ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(PURPLE_COLOR, 0.4f,
                    50, 0.96f), player, 40, 0.2);
            ParticleHelper.spawnParticleEntity(ParticleHelper.constructFigure(Color.GRAY, 0.21f,
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
            
            addRelicExperience(stack, 3);
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
                
                JodahTier tier = it.getOrDefault(ComponentRegistry.JODAH_TIER, JodahTier.D);
                it.set(ComponentRegistry.JODAH_TIER, tier.previous().previous());
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
        float modifier = (int) getStatValue(damageSource.getWeaponItem(), "ranging", "damage_modifier");
        float fineReduce = (int) getStatValue(damageSource.getWeaponItem(), "ranging", "xp_consume");
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
                                (isWalkable(player.level(), player, living.position()) || isWalkable(player.level(), player, living.position().subtract(0, 1, 0)))
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
