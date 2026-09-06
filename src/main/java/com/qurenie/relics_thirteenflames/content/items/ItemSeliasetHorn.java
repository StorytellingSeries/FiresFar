package com.qurenie.relics_thirteenflames.content.items;

import com.qurenie.api.IActivityContainer;
import com.qurenie.api.IExtRelicItem;
import com.qurenie.api.SettingsContainer;
import com.qurenie.relics_thirteenflames.activity.IActivitySetting;
import com.qurenie.relics_thirteenflames.activity.RelicActivitySetting;
import com.qurenie.relics_thirteenflames.activity.call.settings.ActivityResult;
import com.qurenie.relics_thirteenflames.activity.call.settings.InventoryType;
import com.qurenie.relics_thirteenflames.activity.call.settings.RelicsActivityCallSettings;
import com.qurenie.relics_thirteenflames.content.entities.AirVortexEntity;
import com.qurenie.relics_thirteenflames.content.entities.EntitySeliasetSun;
import com.qurenie.relics_thirteenflames.content.entities.WaveEntity;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import com.qurenie.relics_thirteenflames.net.EntityPacket;
import com.qurenie.relics_thirteenflames.net.PacketHornSounds;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.octostudios.octolib.util.OctoColor;
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
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootEntry;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerlib.api.items.IColoredFoilItem;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.net.PacketContext;

import java.awt.*;
import java.util.*;
import java.util.List;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.ACTIVE_TICK;

public class ItemSeliasetHorn extends RelicItem implements IExtRelicItem, IColoredFoilItem, IActivityContainer {

    public static final LootEntry PILLAGE = LootEntry.builder()
            .dimension(".*")
            .biome(".*")
            .table("[\\w]+:chests\\/[\\w_\\/]*(pillage)[\\w_\\/]*").weight(500).build();
    Random rng = new Random();

    public ItemSeliasetHorn(Properties props) {
        super(props);
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack horn = pPlayer.getItemInHand(pUsedHand);
        pPlayer.startUsingItem(pUsedHand);
        return InteractionResultHolder.pass(horn);
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
    public void gatherCreativeTabContent(CreativeContentConstructor constructor) {
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity living, @NotNull ItemStack horn, int count) {
        if (living instanceof Player player) {
            this.releaseRay(player, horn);
        }

        if (level.isClientSide()) {
            int tick = this.getUseDuration(horn, living) - count;
            int segments = (int) Math.round(this.getStatValue(living, horn, "air_ray", "distance"));

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
                                    ParticleTypes.FLAME, pos, move.x, move.y, move.z, 0.2f, 40, OctoColor.WHITE, 0.5f);
                        } else if (rng.nextFloat() < 0.8f) {
                            ParticleHelper.spawnDirectedParticle(level,
                                    ParticleHelper.constructSimpleSpark(FlamesUtils.fromRGBI(37, 36, 30), 0.08f + (float) i / segments * 0.04f, (int) Math.round(iniPos.subtract(pos).length() / move.length()), 1)
                                    , pos.x(), pos.y(), pos.z(), move.x, move.y, move.z);
                        } else {
                            ParticleHelper.spawnEnginedParticle(level,
                                    ParticleTypes.CLOUD, pos, move.x, move.y, move.z, 0.2f, (int) Math.round(iniPos.subtract(pos).length() / move.length()), OctoColor.WHITE, 0.5f);
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
                    ParticleHelper.spawnDirectedParticle(level, ParticleHelper.constructSimpleSpark(FlamesUtils.fromRGBI(37, 36, 30), 0.06f, 40 + rng.nextInt(40), 1
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
                                    ParticleTypes.FLAME, pos, move.x, move.y, move.z, 0.2f, 40, OctoColor.WHITE, 0.5f);
                        } else if (rng.nextFloat() < 0.8f) {
                            ParticleHelper.spawnDirectedParticle(level,
                                    ParticleHelper.constructSimpleSpark(FlamesUtils.fromRGBI(37, 36, 30), 0.08f + (float) i / segments * 0.08f, (int) Math.round((segments - pos.subtract(iniPos).length()) / move.length()), -1)
                                    , pos.x(), pos.y(), pos.z(), move.x, move.y, move.z);
                        } else {
                            ParticleHelper.spawnEnginedParticle(level,
                                    ParticleTypes.CLOUD, pos, move.x, move.y, move.z, 0.2f, 40, OctoColor.WHITE, 0.5f);
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
        double distance = this.getStatValue(player, horn, "air_ray", "distance");

        Vec3 look = player.getLookAngle();
        Vec3 maxEndPos = initPos.add(look.scale(distance));

        if (!player.level().isClientSide) {
            // Проверяем, упирается ли луч в блок
            BlockHitResult blockHit = player.level().clip(new ClipContext(
                    initPos,
                    maxEndPos,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    player
            ));

            // Если попали в блок — конец луча в точке столкновения, иначе на максимальной дистанции
            Vec3 endPos = blockHit.getType() == HitResult.Type.BLOCK
                    ? blockHit.getLocation()
                    : maxEndPos;

            double actualDistance = initPos.distanceTo(endPos);

            List<Entity> entitiesToAffect = getAffectedEntities(
                    player,
                    initPos,
                    endPos,
                    actualDistance,
                    actualDistance / 6.0
            );



            for (Entity e : entitiesToAffect) {

                double kResistance = hasRangModifier(player, horn , "air_ray", "imbalance")
                        ? 0 : !e.isPushable() ? 1
                        : e instanceof LivingEntity l ? l.getAttributes().getBaseValue(Attributes.KNOCKBACK_RESISTANCE) : 0;

                Vec3 entityPos = e.position().add(0, e.getEyeHeight(), 0);
                Vec3 b = entityPos.subtract(initPos).add(look);
                double efficiency = this.getStatValue(player, horn, "air_ray", "efficiency") / 20.0
                        * Math.max(0, 1 - kResistance);

                if (e instanceof LivingEntity living && living.getMaxHealth() > 50) {
                    efficiency = Mth.clamp(
                            efficiency - (Math.sqrt(living.getMaxHealth()) - 15.0) / 20.0,
                            0,
                            efficiency
                    );
                }

                efficiency = Math.max(efficiency, 0.008f);

                Vec3 speed = b.normalize().scale(efficiency);
                if (player.isShiftKeyDown()) {
                    speed = speed.reverse();
                }

                e.setDeltaMovement(e.getDeltaMovement().add(speed));

                int fire = horn.getEnchantmentLevel(player.level().holderOrThrow(Enchantments.FIRE_ASPECT));
                if (fire > 0) {
                    e.setRemainingFireTicks(fire * 2 * 20);
                    ParticleHelper.spawnParticleEntity(
                            rng.nextBoolean() ? ParticleTypes.FLAME : ParticleTypes.SMALL_FLAME,
                            e,
                            3,
                            0.02
                    );
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

    public ActivityResult castAirVortex(LivingEntity living, ItemStack stack) {
        double radius = getStatValue(living, stack, "air_vortex", "radius");
        double maxAge = getStatValue(living, stack, "air_vortex", "maxAge");

        living.playSound(SoundsRegistry.SELIASET_HORN_BALL_LAUNCH.get(), 1, 1);
        if (living.level().isClientSide) {
            return ActivityResult.SUCCESS;
        }

        AirVortexEntity vortexEntity = new AirVortexEntity(living.level(), living.getEyePosition(),
                living.getLookAngle().normalize().scale(0.8f), (float) radius, (int) maxAge, living);
        living.level().addFreshEntity(vortexEntity);

        setMaxCooldown(living, stack, "air_vortex");

        return ActivityResult.SUCCESS;
    }

    @Override
    public SettingsContainer<IActivitySetting> constructActivitySettings() {
        return SettingsContainer.<IActivitySetting>builder()
                .setting(RelicActivitySetting.builderRelic("block").build())
                .setting(RelicActivitySetting.builderRelic("air_vortex")
                        .callSettings(RelicsActivityCallSettings.builder("air_vortex")
                                .inventoryType(InventoryType.IN_HAND)
                                .cast(this::castAirVortex)
                                .build())
                        .build())
                .build();
    }

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("air_ray")
                                .initialMaxLevel(5)
                                .stat(AbilityStatTemplate.builder("distance")
                                        .thresholdValue(8, 42)
                                        .initialValue(10, 14)
                                        .targetValue(RelicsScalingModels.EXPONENTIAL.get(), 42)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("efficiency")
                                        .thresholdValue(2, 5)
                                        .initialValue(2.5, 100)
                                        .targetValue(RelicsScalingModels.EXPONENTIAL.get(), 30)
                                        .formatValue(x -> (int) MathUtils.round(x, 0))
                                        .build())
                                .rankModifier(2, "imbalance")
                                .build())
                        .ability(AbilityTemplate.builder("block")
                                .initialMaxLevel(5)
                                .stat(AbilityStatTemplate.builder("wavesCount")
                                        .initialValue(2, 2)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 7)
                                        .thresholdValue(2, 7)
                                        .formatValue(x -> (int) Math.round(x))
                                        .build())
                                .stat(AbilityStatTemplate.builder("recharge")
                                        .initialValue(1200, 800)
                                        .thresholdValue(200, 1200)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 200)
                                        .formatValue(x -> (int) Math.round(x / 20f))
                                        .build())
                                .stat(AbilityStatTemplate.builder("stunDuration")
                                        .initialValue(0.4, 0.6)
                                        .thresholdValue(0.5, 5)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 5)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_1")
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 8, 28).star(1, 3, 7).star(2, 18, 6).star(3, 19, 13).star(4, 15, 10).star(5, 9, 15).star(6, 10, 20).star(7, 8, 7).star(8, 10, 3)
                                        .link(0, 1).link(0, 3).link(3, 2).link(4, 5).link(5, 6).link(6, 0).link(7, 4).link(1, 8).link(8, 2)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("air_vortex")
                                .initialMaxLevel(3)
                                .requiredLevel(11)
                                .stat(AbilityStatTemplate.builder("recharge")
                                        .initialValue(800, 600)
                                        .thresholdValue(100, 1000)
                                        .targetValue(RelicsScalingModels.EXPONENTIAL.get(), 100)
                                        .formatValue(x -> MathUtils.round(x / 20f, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("maxAge")
                                        .initialValue(60, 80)
                                        .thresholdValue(50, 1000)
                                        .targetValue(RelicsScalingModels.EXPONENTIAL.get(), 360)
                                        .formatValue(x -> MathUtils.round(x / 20f, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("radius")
                                        .initialValue(8, 14)
                                        .thresholdValue(7, 1000)
                                        .targetValue(RelicsScalingModels.EXPONENTIAL.get(), 50)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .step(100)
                        .initialCost(100)
                        .maxRank(2)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(PILLAGE)
                        .build())
                .build();
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slot, boolean isSelected) {

        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            int activeTicker = stack.getOrDefault(ACTIVE_TICK, 0);
            if (activeTicker > 0) {
                if (activeTicker-- % 60 == 0)
                    this.releaseWave(level, living);
            }
            stack.set(ACTIVE_TICK, activeTicker);
        }

        super.inventoryTick(stack, level, entity, slot, isSelected);
    }

    public void releaseWave(Level level, LivingEntity entity) {
        WaveEntity wave = new WaveEntity(level, entity);
        level.addFreshEntity(wave);
    }

    @Override
    public int getFoilColor(@NotNull ItemStack stack) {
        return /*0xFA9FEB7D*/ new Color(183, 155, 58).getRGB();
    }

    @EventBusSubscriber
    public static class LeftClickHandler {

        @SubscribeEvent
        public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
            Network.sendToServer(new EntityPacket(event.getEntity().getId()) {

                @Override
                public void serverExecute(PacketContext ctx) {
                    if (getEntity(ctx.getLevel()) instanceof Player player)
                        handle(player);
                }
            });
        }

        @SubscribeEvent
        public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
            handle(event.getEntity());
        }

        @SubscribeEvent
        public static void onLeftClickEntity(AttackEntityEvent event) {
            handle(event.getEntity());
        }

        private static void handle(Player player) {
            ItemStack stack = player.getMainHandItem();

            if (stack.getItem() instanceof ItemSeliasetHorn item) {

                if (player.isShiftKeyDown() && item.canCast(player, stack, "block")) {

                    if (!player.level().isClientSide) {
                        int duration = (int) (item.getStatValue(player, stack, "block", "wavesCount") * 60);
                        stack.set(ACTIVE_TICK, duration);
                        item.setMaxCooldown(player, stack, "block");
                    }

                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class TootSoundInstance extends AbstractTickableSoundInstance {

        public Vec3 originPos;
        public int unconfirmedDuration = 10;
        private float fadeDirection;
        private float fade;


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
