package com.qurenie.relics_thirteenflames.content.items;

import com.qurenie.relics_thirteenflames.content.entities.EntitySeliasetSun;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import com.qurenie.relics_thirteenflames.net.PacketHornSounds;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.init.RelicContainerRegistry;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
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
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerlib.api.items.IColoredFoilItem;
import org.zeith.hammerlib.net.Network;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static com.qurenie.relics_thirteenflames.ThirteenFlames.SCHEDULER;
import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.ACTIVE_TICK;

public class ItemSeliasetHorn extends RelicItem implements IColoredFoilItem {
    
    public static final LootEntry PILLAGE = LootEntry.builder()
            .dimension(".*")
            .biome(".*")
            .table("[\\w]+:chests\\/[\\w_\\/]*(pillage)[\\w_\\/]*").weight(500).build();
    Random rng = new Random();
    
    public ItemSeliasetHorn(Properties props) {
        super(props);
    }
    
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, List<net.minecraft.network.chat.Component> tooltip, @NotNull TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("tooltip.relics_thirteenflames.seliaset_horn.lore").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, tooltip, isAdvanced);
    }
    
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack horn = pPlayer.getItemInHand(pUsedHand);
        pPlayer.startUsingItem(pUsedHand);
        return InteractionResultHolder.success(horn);
    }
    
    
    @Override
    public void releaseUsing(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull LivingEntity pLivingEntity, int pTimeCharged) {
        super.releaseUsing(pStack, pLevel, pLivingEntity, pTimeCharged);
        
        if (!pLevel.isClientSide() && pStack.is(this) && pLivingEntity instanceof ServerPlayer sPlayer) {
            List<ServerPlayer> players = pLevel.getEntitiesOfClass(ServerPlayer.class, new AABB(pLivingEntity.blockPosition()).inflate(20));
            for (ServerPlayer sp : players) {
                Network.sendTo(sp, new PacketHornSounds(sPlayer.getStringUUID(), sPlayer.position(), true));
            }
            
        }
        
    }
    
    @Override
    public boolean isPrimaryItemFor(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        return enchantment.is(Enchantments.FIRE_ASPECT);
    }
    
    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return enchantment.is(Enchantments.FIRE_ASPECT);
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
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity living, @NotNull ItemStack horn, int count) {
        if (living instanceof Player player) {
            this.releaseRay(player, horn);
        }
        
        
        if (level.isClientSide()) {
            int tick = this.getUseDuration(horn, living) - count;
            int segments = (int) Math.round(this.getStatValue(horn, "air_ray", "distance"));
            
            Vec3 iniPos = living.getEyePosition(1).add(0, -0.45, 0);
            if (living.isShiftKeyDown()) {
                for (int i = 1; i < segments * 2; i++) {
                    if (i < tick) {
                        double a = 360.0 * rng.nextFloat();
                        double radius = 0.2 + (i / 6.0);
                        Vec3 luk = living.getLookAngle();
                        Vec3 iStep = luk.scale(i);
                        
                        Vec3 x = !(luk.normalize().x < 0.001 && luk.normalize().z < 0.001) ? luk.normalize().cross(new Vec3(0, 1, 0)).normalize().scale(radius) : luk.normalize().cross(new Vec3(1, 0, 0)).normalize().scale(radius);
                        Vec3 z = luk.normalize().cross(x).normalize().scale(radius);
                        
                        Vec3 pos = iniPos
                                .add(x.scale(Math.cos(Math.toRadians(a))).scale(rng.nextFloat()))
                                .add(z.scale(Math.sin(Math.toRadians(a))).scale(rng.nextFloat()))
                                .add(iStep);
                        Vec3 move = iniPos.subtract(pos).normalize().scale(0.5 + (double) i / segments * rng.nextFloat());
                        
                        int fire = horn.getEnchantmentLevel(level.holderOrThrow(Enchantments.FIRE_ASPECT));
                        if (fire > 0 && rng.nextFloat() < 0.2) {
                            ParticleHelper.spawnEnginedParticle(level,
                                    ParticleTypes.FLAME, pos, move.x, move.y, move.z, 0.2f, 40, Color.WHITE, 0.5f);
                        } else if (rng.nextFloat() < 0.8f) {
                            ParticleHelper.spawnDirectedParticle(level,
                                    ParticleHelper.constructSimpleSpark(new Color(37, 36, 30), 0.08f + (float) i / segments * 0.04f, (int) Math.round(iniPos.subtract(pos).length() / move.length()), 1)
                                    , pos.x(), pos.y(), pos.z(), move.x, move.y, move.z);
                        } else {
                            ParticleHelper.spawnEnginedParticle(level,
                                    ParticleTypes.CLOUD, pos, move.x, move.y, move.z, 0.2f, (int) Math.round(iniPos.subtract(pos).length() / move.length()), Color.WHITE, 0.5f);
                        }
                    }
                }
            } else {
                for (int i = 0; i < 5; i++) {
                    
                    double a = 360.0 / 5 * i + count * 7;
                    double radius = 0.1 * rng.nextFloat();
                    Vec3 luk = living.getLookAngle();
                    Vec3 x = !(luk.normalize().x < 0.001 && luk.normalize().z < 0.001) ? luk.normalize().cross(new Vec3(0, 1, 0)).normalize().scale(radius) : luk.normalize().cross(new Vec3(1, 0, 0)).normalize().scale(radius);
                    Vec3 z = luk.normalize().cross(x).normalize().scale(radius);
                    
                    Vec3 pos = iniPos
                            .add(x.scale(Math.cos(Math.toRadians(a))).scale(rng.nextFloat()))
                            .add(z.scale(Math.sin(Math.toRadians(a))).scale(rng.nextFloat()));
                    
                    Vec3 move = luk.scale(0.5).add(pos.subtract(iniPos).normalize().scale(0.05 * rng.nextFloat()));
                    ParticleHelper.spawnDirectedParticle(level, ParticleHelper.constructSimpleSpark(new Color(37, 36, 30), 0.06f, 40 + rng.nextInt(40), 1
                            )
                            , pos.x(), pos.y(), pos.z(), move.x, move.y, move.z);
                    if (i % 5 == 0 && rng.nextBoolean()) {
                        
                        Particle particle = Minecraft.getInstance().particleEngine
                                .createParticle(ParticleTypes.CLOUD, iniPos.add(luk.scale(0.1)).x(), iniPos.add(luk.scale(0.1)).y() + 0.01, iniPos.add(luk.scale(0.1)).z(),
                                        luk.scale(0.8).x + rng.nextFloat(-0.1f, 0.1f),
                                        luk.scale(0.8).y + rng.nextFloat(-0.1f, 0.1f),
                                        luk.scale(0.8).z + rng.nextFloat(-0.1f, 0.1f));
                        particle.scale(0.3f);
                        particle.setLifetime(20);
                    }
                    
                }
                for (int i = 0; i < segments * 0.6; i++) {
                    if (i * 1.5 < tick) {
                        double a = 360.0 * rng.nextFloat();
                        double radius = 0.4 + (i / 12.0);
                        Vec3 luk = living.getLookAngle();
                        Vec3 iStep = luk.scale(i);
                        Vec3 x = !(luk.normalize().x < 0.001 && luk.normalize().z < 0.001) ? luk.normalize().cross(new Vec3(0, 1, 0)).normalize().scale(radius) : luk.normalize().cross(new Vec3(1, 0, 0)).normalize().scale(radius);
                        Vec3 z = luk.normalize().cross(x).normalize().scale(radius);
                        
                        Vec3 pos = iniPos
                                .add(x.scale(Math.cos(Math.toRadians(a))).scale(rng.nextFloat()))
                                .add(z.scale(Math.sin(Math.toRadians(a))).scale(rng.nextFloat()));
                        
                        Vec3 move = luk.scale(0.5 + (double) i / segments).add(pos.subtract(iniPos).normalize().scale(0.1 * rng.nextFloat()));
                        pos = pos.add(iStep);
                        
                        int fire = horn.getEnchantmentLevel(level.holderOrThrow(Enchantments.FIRE_ASPECT));
                        if (fire > 0 && rng.nextFloat() < 0.3) {
                            ParticleHelper.spawnEnginedParticle(level,
                                    ParticleTypes.FLAME, pos, move.x, move.y, move.z, 0.2f, 40, Color.WHITE, 0.5f);
                        } else if (rng.nextFloat() < 0.8f) {
                            ParticleHelper.spawnDirectedParticle(level,
                                    ParticleHelper.constructSimpleSpark(new Color(37, 36, 30), 0.08f + (float) i / segments * 0.08f, (int) Math.round((segments - pos.subtract(iniPos).length()) / move.length()), -1)
                                    , pos.x(), pos.y(), pos.z(), move.x, move.y, move.z);
                        } else {
                            ParticleHelper.spawnEnginedParticle(level,
                                    ParticleTypes.CLOUD, pos, move.x, move.y, move.z, 0.2f, 40, Color.WHITE, 0.5f);
                        }
                    }
                }
            }
        } else if (living instanceof ServerPlayer sPlayer) {
            List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, new AABB(living.blockPosition()).inflate(20));
            for (ServerPlayer sp : players) {
                Network.sendTo(sp, new PacketHornSounds(sPlayer.getStringUUID(), sPlayer.position(), false));
            }
        }
    }
    
    public void releaseRay(Player player, ItemStack horn) {
        Vec3 initPos = player.getEyePosition().add(0, -0.4, 0);
        double distance = this.getStatValue(horn, "air_ray", "distance");
        
        
        Vec3 endPos = initPos.add(player.getLookAngle().scale(distance / 2.0));
        if (!player.level().isClientSide) {
            List<Entity> entitiesToAffect = getAffectedEntities(player, initPos, endPos, distance, distance / 6.0);
            
            for (Entity e : entitiesToAffect) {
                if (e instanceof LivingEntity && (!e.isPushable() || e instanceof EntitySeliasetSun))
                    continue;
                
                Vec3 entityPos = e.position().add(0, e.getEyeHeight(), 0);
                Vec3 b = entityPos.subtract(initPos).add(player.getLookAngle());
                double efficiency = this.getStatValue(horn, "air_ray", "efficiency") / 20;
                
                if (e instanceof LivingEntity living && living.getMaxHealth() > 50)
                    efficiency = Mth.clamp(efficiency - (living.getMaxHealth() - 50.0) / 10.0, 0, efficiency);
                
                Vec3 speed = b.normalize().multiply(efficiency, efficiency, efficiency);
                if (player.isShiftKeyDown()) {
                    speed = speed.reverse();
                }
                
                e.setDeltaMovement(e.getDeltaMovement().add(speed));
                
                int fire = horn.getEnchantmentLevel(player.level().holderOrThrow(Enchantments.FIRE_ASPECT));
                if (fire > 0) {
                    e.setRemainingFireTicks(fire * 2 * 20);
                    ParticleHelper.spawnParticleEntity(rng.nextBoolean() ? ParticleTypes.FLAME : ParticleTypes.SMALL_FLAME, e, 3, 0.02);
                }
            }
        }
    }
    
    List<Entity> getAffectedEntities(Player player, Vec3 initPos, Vec3 endPos, double boxRadius, double dist) {
        Vec3 axis = endPos.subtract(initPos);
        return player.level().getEntitiesOfClass(Entity.class, new AABB(endPos, endPos).inflate(boxRadius), e -> {
            if (!(e instanceof LivingEntity) && !(e instanceof ItemEntity) && !(e instanceof ExperienceOrb) && !(e instanceof EntitySeliasetSun))
                return false;
            
            Vec3 ePos = e.getBoundingBox().getCenter();
            Vec3 eVec = ePos.subtract(initPos);
            double axisScalar = axis.dot(axis);
            double eScalar = eVec.dot(axis);
            Vec3 point = initPos.add(axis.scale(eScalar / axisScalar));
            return point.subtract(ePos).lengthSqr() < dist * dist && !e.equals(player) && (eVec.add(axis).length() > eVec.subtract(axis).length());
        });
    }
    
    @Override
    public int getUseDuration(@NotNull ItemStack p_41454_, @NotNull LivingEntity p_344979_) {
        return 72000;
    }
    
    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.TOOT_HORN;
    }
    
    
    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("air_ray")
                                .maxLevel(5)
                                .stat(StatData.builder("distance")
                                        .thresholdValue(8, 42)
                                        .initialValue(10, 14)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, 0.25)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build())
                                .stat(StatData.builder("efficiency")
                                        .thresholdValue(2, 5)
                                        .initialValue(2.5, 3.5)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.3)
                                        .formatValue(x -> (int) MathUtils.round(x, 0))
                                        .build())
                                .build())
                        .ability(AbilityData.builder("block")
                                .maxLevel(5)
                                .active(CastData.builder()
                                        .container(RelicContainerRegistry.INVENTORY.get())
                                        .predicate("horn_pred", PredicateType.VISIBILITY, (p, s) -> p.getMainHandItem() == s || p.getOffhandItem() == s)
                                        .type(CastType.INSTANTANEOUS)
                                        .build())
                                .stat(StatData.builder("wavesCount")
                                        .initialValue(2, 2)
                                        .upgradeModifier(UpgradeOperation.ADD, 1)
                                        .thresholdValue(2, 7)
                                        .formatValue(x -> (int) Math.round(x))
                                        .build())
                                .stat(StatData.builder("cooldown")
                                        .initialValue(60, 40)
                                        .thresholdValue(10, 60)
                                        .upgradeModifier(UpgradeOperation.ADD, -6)
                                        .formatValue(x -> (int) Math.round(x))
                                        .build())
                                .stat(StatData.builder("stunDuration")
                                        .initialValue(0.4, 0.6)
                                        .thresholdValue(0.5, 4)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.3)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingData(100, 10, 100))
                .loot(LootData.builder()
                        .entry(PILLAGE)
                        .build())
                .build();
    }
    
    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        if (ability.equals("block")) {
            int duration = (int) (this.getStatValue(stack, "block", "wavesCount") * 60);
            stack.set(ACTIVE_TICK, duration);
            this.addAbilityCooldown(stack, "block", (int) (this.getStatValue(stack, "block", "cooldown") * 20));
        }
    }
    
    @Override
    public void inventoryTick(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slot, boolean isSelected) {
        
        if (!level.isClientSide()) {
            int activeTicker = stack.getOrDefault(ACTIVE_TICK, 0);
            if (activeTicker > 0) {
                if (activeTicker-- % 60 == 0) {
                    this.releaseWave(level, entity);
                } else {
                    int tick = 60 - activeTicker % 60;
                    this.tickWave(entity, stack, tick);
                    
                }
            }
            stack.set(ACTIVE_TICK, activeTicker);
        }
        super.inventoryTick(stack, level, entity, slot, isSelected);
    }
    
    public void tickWave(Entity entity, ItemStack stack, int tick) {
        if (tick <= 30) {
            double radius = tick / 30f * 10;
            
            List<Entity> targets = new ArrayList<>(entity.level().getEntitiesOfClass(LivingEntity.class, new AABB(
                    -radius, -radius, -radius, radius, radius, radius
            ).move(entity.position()), e -> e.distanceTo(entity) <= radius));
            
            targets.addAll( entity.level().getEntitiesOfClass(ItemEntity.class, new AABB(
                    -radius, -radius, -radius, radius, radius, radius
            ).move(entity.position()), e -> e.distanceTo(entity) <= radius));
            
            targets.addAll( entity.level().getEntitiesOfClass(ExperienceOrb.class, new AABB(
                    -radius, -radius, -radius, radius, radius, radius
            ).move(entity.position()), e -> e.distanceTo(entity) <= radius));
            
            for (Entity le : targets) {
                if (le instanceof LivingEntity && !le.isPushable())
                    continue;
                
                if (Objects.equals(le.getUUID(), entity.getUUID())) continue;
                Vec3 b = le.position().subtract(entity.position());
                Vec3 sp = b.normalize().multiply(2, 2, 2).add(0, 0.5, 0);
                if (le instanceof ItemEntity || le instanceof ExperienceOrb)
                    sp = sp.scale(0.2f);
                le.setDeltaMovement(sp);
                
                if (!(le instanceof LivingEntity living))
                    return;
                
                if (!living.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && entity instanceof LivingEntity livin)
                    this.spreadRelicExperience(livin, stack, 1);
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) Math.round(this.getStatValue(stack, "block", "stunDuration") * 20), 3));
            }
        }
    }
    
    public void releaseWave(Level level, Entity entity) {
        level.playSound(null, entity.blockPosition(), SoundsRegistry.SELI_HORN_WAVE.get(), SoundSource.MASTER, 1, 1);
        releaseWaveParticles(level, entity);
    }
    
    public void releaseWaveParticles(Level level, Entity entity) {
        double angle = Math.PI * 2 / 60;
        for (float g = -0.5f; g <= 0.5; g += 0.25f) {
            for (int i = 0; i <= 60; i++) {
                double vangle = angle * i + (g * angle);
                double x = Math.sin(vangle);
                double y = Math.cos(vangle);
                
                
                float md = (1 - 2.5f / Math.abs(g)) * 0.9f;
                Vec3 dir = new Vec3(x * md, g, y * md);
                Vec3 ppos = entity.position().add(dir.scale(0.3)).add(0, 0.2, 0);
                
                Vec3 speed = dir.normalize().multiply(0.5, 0.5, 0.5);
                for (int k = 0; k <= 3; k++) {
                    Vec3 ppos1 = ppos.add(
                            rng.nextFloat() * 0.5 - 0.25,
                            rng.nextFloat() * 0.5 - 0.25,
                            rng.nextFloat() * 0.5 - 0.25
                    );
                    ParticleHelper.spawnDirectedParticle(level, ParticleTypes.CLOUD, ppos1.x, ppos1.y, ppos1.z, speed.x, speed.y, speed.z);
                }
                
            }
        }
        Vec3 ePos = entity.position();
        for (int i = 0; i < 14; i++) {
            double r = 0.540 * i + 0.3;
            int count = (int) Math.round(2 * Math.PI * r * 6);
            double r2 = r + 0.270;
            int count2 = (int) Math.round(2 * Math.PI * r2 * 6);
            final float finalI = i;
            SCHEDULER.schedule(i, () -> {
                for (int j = 0; j < count; j++) {
//                    level.addParticle(new CircleTintWithTrailData(new Color(255, 255, 255), 0.1f,0.2f,40,-1,false,true),
//                            pos.x(), pos.y(), pos.z(), sped.x(), 0, sped.z());
                    Vec3 pos = ePos.add(new Vec3(r, 0, 0).yRot((float) Math.toRadians(360.0 / count * j)));
                    Vec3 sped = pos.subtract(ePos.add(new Vec3(0, 0.3, 0))).normalize().scale(0.11);
//                    ParticleHelper.spawnDirectedParticle(level, ParticleHelper.constructSimpleSpark(new Color(42, 41, 26), 0.3f, (int) Math.round(10 + r * 2), -1),
//                            pos.x, pos.y + 0.3, pos.z, sped.x, 0, sped.z);
                    
                    if (finalI == 13) ParticleHelper.spawnDirectedParticle(level, ParticleTypes.CLOUD,
                            pos.x, pos.y, pos.z, sped.scale(2).x, 0, sped.scale(2).z);
                    
                }
                
                for (int j = 0; j < count2; j++) {
                    Vec3 pos = ePos.add(new Vec3(r2 + rng.nextDouble(0.1), 0, 0).yRot((float) Math.toRadians(360.0 / count2 * j)));
                    Vec3 sped = pos.subtract(ePos.add(new Vec3(0, 0.3, 0))).normalize().scale(0.11 * (1 - finalI / 14) + rng.nextDouble(0.03));
                    ParticleHelper.spawnDirectedParticle(level, ParticleHelper.constructSimpleSpark(new Color(63, 60, 39), 0.3f + ((finalI / 14) * 0.2f), (int) Math.round(10 + r2 * 2), 0.9f),
                            pos.x + rng.nextDouble(0.4) - 0.2, pos.y + 0.2 + rng.nextDouble(0.2), pos.z + rng.nextDouble(0.4) - 0.2, sped.x, 0, sped.z);
                    
                }
            });
            
        }
        
    }
    
    @Override
    public int getFoilColor(@NotNull ItemStack stack) {
        return /*0xFA9FEB7D*/ new Color(183, 155, 58).getRGB();
    }
    
    @OnlyIn(Dist.CLIENT)
    public static class TootSoundInstance extends AbstractTickableSoundInstance {
        
        public Vec3 originPos;
        public int unconfirmedDuration = 10;
        private float fadeDirection;
        private float fade;
        
        
        public TootSoundInstance(SoundEvent p_119658_) {
            super(p_119658_, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
            this.looping = false;
            this.delay = 0;
            this.volume = 1.0F;
            this.relative = true;
            this.originPos = null;
            this.fade = 1;
            this.fadeDirection = 0;
        }
        
        public TootSoundInstance(SoundEvent sound, Vec3 pos) {
            super(sound, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
            this.looping = false;
            this.delay = 0;
            this.volume = 1.0F;
            this.relative = true;
            this.originPos = pos;
            this.fade = 1;
            this.fadeDirection = 0;
        }
        
        @Override
        public boolean isStopped() {
            return super.isStopped() && this.fade <= 0;
        }
        
        
        public void tick() {
            if (this.fade <= 0) {
                this.stop();
            }
            if (this.unconfirmedDuration == 0 && fadeDirection > 0) this.fadeOut();
            fade = Mth.clamp(fade + fadeDirection, 0, 1);
            LocalPlayer player = Minecraft.getInstance().player;
            this.volume = (float) Mth.clamp(player == null ? 0 : 25f / player.distanceToSqr(originPos), 0.0F, 1.0F) * fade;
            this.unconfirmedDuration = Math.max(0, this.unconfirmedDuration - 1);
        }
        
        public void fadeOut() {
            this.fade = Math.min(this.fade, 1);
            this.fadeDirection = -0.1f;
            
        }
        
        public void fadeIn() {
            this.fade = Math.max(0, this.fade);
            this.fadeDirection = 0.2f;
        }
        
        public void setFade(float fade) {
            this.fade = fade;
            this.volume = fade;
        }
        
    }
    
}
