package com.qurenie.relics_thirteenflames.content.items;

import com.qurenie.api.IExtRelicItem;
import com.qurenie.relics_thirteenflames.content.entities.GhostBigEntity;
import com.qurenie.relics_thirteenflames.content.entities.GhostSmallEntity;
import com.qurenie.relics_thirteenflames.content.entities.SoulOrbEntity;
import com.qurenie.relics_thirteenflames.content.entities.SoulSpawnCarrierEntity;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import com.qurenie.relics_thirteenflames.net.PacketPlaySound;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.api.events.utility.ContainerSlotClickEvent;
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
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.HammerLib;
import org.zeith.hammerlib.api.fml.IRegisterListener;
import org.zeith.hammerlib.api.io.IAutoNBTSerializable;
import org.zeith.hammerlib.api.io.NBTSerializable;
import org.zeith.hammerlib.api.items.IColoredFoilItem;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.util.charging.ItemChargeHelper;

import java.util.*;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.*;
import static com.qurenie.relics_thirteenflames.style.ColorScheme.SOUL_COLOR;
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

public class ItemKnefRose
        extends RelicItem
        implements IExtRelicItem, IColoredFoilItem, IRegisterListener {

    public ItemKnefRose(Properties properties) {
        super(properties);
    }

    @Override
    public void gatherCreativeTabContent(CreativeContentConstructor constructor) {
    }

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("undeath")
                                .initialMaxLevel(5)
                                .stat(AbilityStatTemplate.builder("max_bones")
                                        .initialValue(15, 30)
                                        .thresholdValue(15, 1000)
                                        .targetValue(RelicsScalingModels.EXPONENTIAL.get(), 1000)
                                        .formatValue(x -> MathUtils.round(x, 0))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("per_bone")
                                        .initialValue(1, 2)
                                        .thresholdValue(1, 20)
                                        .targetValue(RelicsScalingModels.EXPONENTIAL.get(), 15)
                                        .formatValue(x -> MathUtils.round(x, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("per_block")
                                        .initialValue(0.6, 1)
                                        .thresholdValue(1, 20)
                                        .targetValue(RelicsScalingModels.EXPONENTIAL.get(), 7)
                                        .formatValue(x -> MathUtils.round(x, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("max_health")
                                        .initialValue(4, 6)
                                        .thresholdValue(4, 1000)
                                        .targetValue(RelicsScalingModels.EXPONENTIAL.get(), 150)
                                        .formatValue(x -> MathUtils.round(x, 0))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("damage_bonus")
                                        .initialValue(0.01, 0.03)
                                        .thresholdValue(0, 1000)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 0.05)
                                        .formatValue(x -> MathUtils.round(x, 2))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("lifetime_bonus")
                                        .initialValue(0.01, 0.03)
                                        .thresholdValue(0, 1000)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 0.05)
                                        .formatValue(x -> MathUtils.round(x, 2))
                                        .build()
                                )
                                .rankModifier(1, "bone_bonus")
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_1")
                                        .build())
                                .build()
                        )
                        .ability(AbilityTemplate.builder("living_rot")
                                .initialMaxLevel(10)
                                .stat(AbilityStatTemplate.builder("chance")
                                        .initialValue(2.5, 7.5)
                                        .thresholdValue(2.5, 80)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 80)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("hp_rate")
                                        .initialValue(0.2, 0.35)
                                        .thresholdValue(0.2, 1)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 1)
                                        .formatValue(x -> MathUtils.round(x * 100, 0))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("big_chance")
                                        .initialValue(20, 30)
                                        .thresholdValue(20, 60)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 60)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("new_one_chance")
                                        .initialValue(20, 30)
                                        .thresholdValue(0, 100)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 100)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_1")
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 8, 5).star(1, 10, 10).star(2, 15, 13).star(3, 18, 5).star(4, 15, 19).star(5, 13, 24).star(6, 19, 25).star(7, 8, 21).star(8, 2, 22).star(9, 5, 15).star(10, 4, 11).star(11, 3, 5).star(12, 6, 26)
                                        .link(11, 10).link(10, 1).link(1, 0).link(1, 3).link(1, 2).link(2, 4).link(4, 5).link(5, 6).link(5, 7).link(7, 9).link(9, 10).link(9, 8).link(12, 7)
                                        .build())
                                .rankModifier(1, "homing_ghost")
                                .rankModifier(2, "new_one")
                                .build()
                        )
                        .ability(AbilityTemplate.builder("rot_split")
                                .requiredLevel(10)
                                .initialMaxLevel(5)
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_1")
                                        .build())
                                .stat(AbilityStatTemplate.builder("chance")
                                        .initialValue(10.0, 15)
                                        .thresholdValue(10.0, 40.0)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 40)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("split_size")
                                        .initialValue(25.0, 35.0)
                                        .thresholdValue(30.0, 55.0)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 55)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("max_splits")
                                        .initialValue(1.0, 3.0)
                                        .thresholdValue(2.0, 8.0)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 8)
                                        .formatValue(Double::intValue)
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .leveling(LevelingTemplate.builder()
                        .maxRank(2)
                        .step(100)
                        .initialCost(100)
                        .build())
                .loot(LootTemplate.builder().entry(LootEntries.THE_NETHER).build())
                .build();
    }

    @EventBusSubscriber
    public static class LeftClickHandler {

        @SubscribeEvent
        public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
            var player = event.getEntity();
            var level = player.level();
            ItemStack stack = player.getMainHandItem();

            if (stack.getItem() instanceof ItemKnefRose item && player.isShiftKeyDown()
                    && !player.getCooldowns().isOnCooldown(ItemsRegistry.KNEF_ROSE)) {
                if (level.isClientSide) {
                    event.setCanceled(true);
                    return;
                }
                int max = (int) item.getStatValue(player, stack, "undeath", "max_bones");
                double perOnce = item.getStatValue(player, stack, "undeath", "per_block");

                int has = stack.getOrDefault(SOULS, 0);
                int need = max - has;

                BlockPos origin = event.getPos();

                int radius = 6;
                int total = 0;

                Set<BlockPos> visited = new HashSet<>();
                Queue<BlockPos> queue = new ArrayDeque<>();

                queue.add(origin);
                visited.add(origin);

                boolean used = false;
                while (!queue.isEmpty() && total < need) {
                    BlockPos current = queue.poll();

                    // проверка радиуса (манхэттен или обычная дистанция — выбери)
                    if (current.distManhattan(origin) > radius)
                        continue;

                    if (!FlamesUtils.isSoulBlock(level, current))
                        continue;

                    double remainder = perOnce - (int) perOnce;
                    int souls = (int) perOnce + (Math.random() < remainder ? 1 : 0);
                    int value = Math.min(souls, need - total);

                    if (value > 0) {
                        SoulOrbEntity soul = new SoulOrbEntity(level, player, current, souls);
                        level.addFreshEntity(soul);

                        used = true;
                    }

                    level.setBlock(current, Blocks.SOUL_SOIL.defaultBlockState(), 3);
                    ParticleHelper.spawnParticleOutbox(level, ParticleTypes.SOUL_FIRE_FLAME, current, 3, 0.005);
                    ParticleHelper.spawnParticleOutbox(level, ParticleHelper.constructSmoke(SOUL_COLOR, 0.6f, 40, 0f), current, 3, 0.005);

                    total += value;

                    // добавляем соседей (6 направлений)
                    for (var dir : Direction.values()) {
                        BlockPos next = current.relative(dir);

                        if (!visited.contains(next)) {
                            visited.add(next);
                            queue.add(next);
                        }
                    }
                }

                if (used) {
                    player.getCooldowns().addCooldown(ItemsRegistry.KNEF_ROSE, SOULSAND_COOLDOWN_TICKS);
                    level.playSound(null, origin, SoundsRegistry.KNEFMTITI_ROSE_SOUL_SAND.get(),  SoundSource.NEUTRAL, 1, 1);
                }

                event.setCanceled(true);
            }
        }
    }

    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 8;
    private static final int SOULSAND_COOLDOWN_TICKS = 20 * 20;

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        var stack = player.getItemInHand(usedHand);

        if (!player.isShiftKeyDown()) return super.use(level, player, usedHand);

        if (level.isClientSide) return InteractionResultHolder.success(stack);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.pass(stack);

        int souls = this.getSouls(stack);
        if (souls <= 0) return InteractionResultHolder.fail(stack);

        int maxHp = Math.max(1, Math.round(this.getMaxMobHP(player, stack)));
        BlockPos origin = player.blockPosition();

        var server = (ServerLevel) level;
        RandomSource random = player.getRandom();

        List<SpawnChoice> pool = collectSpawnPool(server, origin, maxHp);

        if (pool.isEmpty()) {
            pool = trashPool();
        }

        List<SpawnChoice> chosen = buildSpawnPlan(pool, souls, random);

        if (chosen.isEmpty()) return InteractionResultHolder.fail(stack);

        setSouls(player, stack, 0);
        player.getCooldowns().addCooldown(this, DEFAULT_COOLDOWN_TICKS);

        Vec3 look = player.getLookAngle();

        for (SpawnChoice choice : chosen) {
            Vec3 start = player.position()
                    .add(0, player.getEyeHeight() * 0.6, 0);

            // случайное отклонение (конус)
            Vec3 spread = new Vec3(
                    (0.5 - Math.random()) * 3,
                    0.1 + Math.random() * 0.3,
                    (0.5 - Math.random()) * 3
            );

            look = new Vec3(look.x, Math.max(look.y, 0), look.z);
            Vec3 dir = look.add(spread).normalize();

            Vec3 motion = dir.scale(0.6 + random.nextDouble() * 0.4);

            String mobId = BuiltInRegistries.ENTITY_TYPE.getKey(choice.type()).toString();
            boolean noGravity = isFlyingMob(choice.type());

            SoulSpawnCarrierEntity carrier = new SoulSpawnCarrierEntity(
                    server,
                    player,
                    start,
                    motion,
                    mobId,
                    choice.cost(),
                    noGravity
            );

            Network.sendToTrackingAndSelf(player,  new PacketPlaySound(player.position(),
                    SoundsRegistry.KNEFMTITI_ROSE_GHOST_SPAWN.get(), SoundSource.PLAYERS, 0.7f, (float) (0.75f + player.getRandom().nextGaussian() * 0.5)));
            server.addFreshEntity(carrier);

        }

        return super.use(level, player, usedHand);
    }

    private List<SpawnChoice> collectSpawnPool(ServerLevel level, BlockPos pos, int maxHp) {
        List<SpawnChoice> out = new ArrayList<>();

        // ---------- BIOME SPAWNS ----------

        Biome biome = level.getBiome(pos).value();
        MobSpawnSettings settings = biome.getMobSettings();

        for (MobCategory category : MobCategory.values()) {
            addFromList(level, settings.getMobs(category), maxHp, out);
        }

        // ---------- STRUCTURE OVERRIDES ----------

        var structureManager = level.structureManager();

        for (var structure : structureManager.startsForStructure(
                new ChunkPos(pos),
                s -> true
        )) {
            if (!structure.getBoundingBox().isInside(pos))
                continue;

            var overrides =
                    structure.getStructure().spawnOverrides();

            for (var entry : overrides.entrySet()) {
                StructureSpawnOverride override = entry.getValue();

                addFromList(
                        level,
                        override.spawns(),
                        maxHp,
                        out
                );
            }
        }

        // ---------- FALLBACK ----------

        out.addAll(trashPool());

        return out;
    }

    private void addFromList(Level level,
                             WeightedRandomList<MobSpawnSettings.SpawnerData> list,
                             int maxHp,
                             List<SpawnChoice> out) {
        if (list == null || list.isEmpty()) return;

        for (var data : list.unwrap()) {
            EntityType<?> rawType = data.type;
            if (!(rawType.create(level) instanceof Mob mob)) continue;

            float hp = mob.getMaxHealth();
            if (hp > maxHp) continue;

            int weight = Math.max(1, data.getWeight().asInt()); // если у тебя accessor другой, замени здесь
            int cost = Math.max(1, Math.round(hp));

            @SuppressWarnings("unchecked")
            EntityType<? extends Mob> mobType = (EntityType<? extends Mob>) rawType;

            out.add(new SpawnChoice(mobType, weight, cost, false));
        }
    }

    private List<SpawnChoice> trashPool() {
        List<SpawnChoice> out = new ArrayList<>();

        out.add(new SpawnChoice(EntityType.BAT, 20, 3, true));
        out.add(new SpawnChoice(EntityType.SILVERFISH, 12, 4, true));
        out.add(new SpawnChoice(EntityType.ENDERMITE, 8, 5, true));

        return out;
    }

    private List<SpawnChoice> buildSpawnPlan(List<SpawnChoice> pool, int bones, RandomSource random) {
        List<SpawnChoice> result = new ArrayList<>();
        Object2IntArrayMap<EntityType<?>> pickedTypes = new Object2IntArrayMap<>();
        int remaining = bones;

        pool.forEach(c -> pickedTypes.put(c.type, 0));
        while (remaining > 0) {
            List<SpawnChoice> affordable = new ArrayList<>();
            for (SpawnChoice c : pool) {
                if (c.trash() || c.cost() <= remaining) {
                    affordable.add(c);
                }
            }

            SpawnChoice picked = pickWeighted(affordable, random, pickedTypes);
            if (picked == null) break;

            int cost = Math.min(picked.cost(), remaining);
            if (cost <= 0) break;

            result.add(new SpawnChoice(picked.type(), picked.weight(), cost, picked.trash()));
            pickedTypes.compute(picked.type, (t, i) -> i + 1);

            remaining -= cost;
        }

        return result;
    }

    private static boolean isFlyingMob(EntityType<? extends Mob> type) {
        return FlyingMob.class.isAssignableFrom(type.getBaseClass())
                || type == EntityType.BAT
                || type == EntityType.BLAZE
                || type == EntityType.GHAST
                || type == EntityType.ALLAY;
    }

    private SpawnChoice pickWeighted(List<SpawnChoice> pool, RandomSource random, Function<EntityType<?>, Integer> pickedTypes) {
        float total = 0;
        for (var c : pool) total += Math.max(1, c.weight() * (float) Math.pow(0.76f, pickedTypes.apply(c.type)));
        if (total <= 0) return null;

        float roll = random.nextFloat() * total;
        for (var c : pool) {
            roll -= Math.max(1, c.weight() * (float) Math.pow(0.76f, pickedTypes.apply(c.type)));
            if (roll < 0) return c;
        }

        return pool.getLast();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag isAdvanced) {
        int bones = getSouls(stack);
        if (bones > 0)
            tooltip.add(Component.translatable("tooltip.relics_thirteenflames.knef_rose").append(Integer.toUnsignedString(bones))
                    .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, isAdvanced);
    }

    @Override
    public int getFoilColor(@NotNull ItemStack stack) {
        return 0xCCA1CE | FULL_ALPHA;
    }

    @Override
    public void onPostRegistered(ResourceLocation id) {
        EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void slotClick(ContainerSlotClickEvent e) {
        if (e.getSlotStack().is(this) && e.getHeldStack().is(Tags.Items.BONES)) {
            var stack = e.getSlotStack();
            int bones = e.getHeldStack().getCount();

            int perBone = (int) getStatValue(e.getEntity(), stack, "undeath", "per_bone");
            // Limit to 1 bone per right click; No limit on left click.
            if (e.getAction() == ClickAction.SECONDARY) bones = Math.min(bones, 1);

            int toAdd = Math.min(bones * perBone, getMaxSouls(e.getEntity(), stack) - getSouls(stack));

            addSouls(e.getEntity(), stack, toAdd);
            e.getHeldStack().shrink((int) Math.ceil(toAdd / (float) perBone));

            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void livingDeath(LivingDeathEvent e) {
        if (e.getSource().getEntity() instanceof GhostSmallEntity flesch) {
            flesch.lifetime += (int) (e.getEntity().getMaxHealth() * 6);
        }

        if (e.getSource().getEntity() instanceof GhostBigEntity flesch) {
            flesch.lifetime += (int) (e.getEntity().getMaxHealth() * 6);
        }

        if (!(e.getEntity() instanceof Enemy) && !(e.getEntity() instanceof Animal)) return;
        if (!(e.getSource().getEntity() instanceof ServerPlayer sp)) return;

        var itr = ItemChargeHelper.listPlayerInventories(sp).iterator();
        while (itr.hasNext()) {
            var ih = itr.next();
            for (int j = 0; j < ih.getSlots(); j++) {
                var it = ih.getStackInSlot(j);
                if (it.is(this) && isAbilityUnlocked(sp, it, "living_rot")) {
                    var spawnChance = this.getStatValue(sp, it, "living_rot", "chance") / 100;

                    var causing = e.getSource().getDirectEntity();
                    if ((causing instanceof GhostSmallEntity || causing instanceof GhostBigEntity)) {
                        double newOneChanceModifier = hasRangModifier(sp, it, "living_rot", "new_one")
                                ? getStatValue(sp, it, "living_rot", "new_one_chance") : 0;
                        spawnChance *= newOneChanceModifier;
                    }

                    if (sp.getRandom().nextFloat() < spawnChance) {
                        LivingEntity ent = createLiving(sp.level(), sp, e.getEntity(), it);

                        Network.sendToArea((ServerLevel) sp.level(), null, e.getEntity().position(), 20, new PacketPlaySound(e.getEntity().position(),
                                SoundsRegistry.KNEFMTITI_ROSE_GHOST_SPAWN.get(), SoundSource.HOSTILE, 1.5f, 1f));
                        HammerLib.PROXY.queueTask(sp.level(), 15, () -> sp.level().addFreshEntity(ent));
                        return;
                    }
                }
            }
        }
    }

    private LivingEntity createLiving(Level level, Player player, LivingEntity living, ItemStack stack) {
        var mutationChance = this.getStatValue(player, stack, "living_rot", "big_chance") / 100;

        if (living.getMaxHealth() > 15 && player.getRandom().nextFloat() <= mutationChance) {
            GhostBigEntity ent = new GhostBigEntity(EntityRegistry.BIG_GHOST, level)
                    .initPrimary(living, new RoseStats(player, stack));
            ent.moveTo(living.position());
            ent.setOwnerUUID(player.getStringUUID());
            ent.lifetime = (int) (40 + living.getMaxHealth() * 25);

            ent.setExplosiveBall(hasRangModifier(player, stack, "living_rot", "homing_ghost"));

            if (hasRangModifier(player, stack, "undeath", "bone_bonus")) {
                double damageBonus = getStatValue(player, stack, "undeath", "damage_bonus");
                double lifetimebonus = getStatValue(player, stack, "undeath", "lifetime_bonus");

                ent.upgradeWithSouls(stack.getOrDefault(SOULS, 0), damageBonus, lifetimebonus);
            }

            return ent;
        }

        GhostSmallEntity ent = new GhostSmallEntity(EntityRegistry.SMALL_GHOST, level)
                .initPrimary(living, new RoseStats(player, stack));
        ent.moveTo(living.position());
        ent.setOwnerUUID(player.getStringUUID());
        ent.lifetime = (int) (40 + living.getMaxHealth() * 15);

        if (hasRangModifier(player, stack, "undeath", "bone_bonus")) {
            double damageBonus = getStatValue(player, stack, "undeath", "damage_bonus");
            double lifetimebonus = getStatValue(player, stack, "undeath", "lifetime_bonus");

            ent.upgradeWithSouls(stack.getOrDefault(SOULS, 0), damageBonus, lifetimebonus);
        }

        return ent;
    }

    public void addSouls(LivingEntity livingEntity, ItemStack stack, int bones) {
        setSouls(livingEntity, stack, getSouls(stack) + bones);
    }

    public int takeSouls(LivingEntity livingEntity, ItemStack stack, int bones, boolean simulate) {
        int avail = getSouls(stack);
        bones = Math.min(bones, avail);
        if (!simulate) setSouls(livingEntity, stack, avail - bones);
        return bones;
    }

    public void setSouls(LivingEntity livingEntity, ItemStack stack, int bones) {
        bones = Math.max(bones, 0);
        bones = Math.min(bones, getMaxSouls(livingEntity, stack));

        if (bones == 0)
            stack.remove(SOULS);
        else
            stack.set(SOULS, bones);
    }

    public float getMaxMobHP(LivingEntity livingEntity, ItemStack stack) {
        return (float) this.getStatValue(livingEntity, stack, "undeath", "max_health");
    }

    public int getSouls(ItemStack stack) {
        return stack.getOrDefault(SOULS, 0);
    }

    public int getMaxSouls(LivingEntity livingEntity, ItemStack stack) {
        return (int) MathUtils.round(this.getStatValue(livingEntity, stack, "undeath", "max_bones"), 0);
    }

    @Getter
    public static class RoseStats
            implements IAutoNBTSerializable {

        public static final StreamCodec<RegistryFriendlyByteBuf, RoseStats> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public RoseStats decode(RegistryFriendlyByteBuf buf) {
                return new RoseStats(buf.readFloat(), buf.readFloat(), buf.readInt(), buf.readInt(), buf.readFloat(),
                        ItemStack.STREAM_CODEC.decode(buf));
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, RoseStats stats) {
                buf.writeFloat(stats.splitChance);
                buf.writeFloat(stats.splitScale);
                buf.writeInt(stats.maxSplits);
                buf.writeInt(stats.counter);
                buf.writeFloat(stats.hpRate);
                ItemStack.STREAM_CODEC.encode(buf, stats.rose);
            }
        };

        @NBTSerializable
        public float splitChance = 10F;

        @NBTSerializable
        public float splitScale = 30F;

        @NBTSerializable
        public int maxSplits = 2;

        @NBTSerializable
        public int counter;

        @NBTSerializable
        public float hpRate = 0.3F;

        @NBTSerializable
        public ItemStack rose;

        private RoseStats(float splitChance, float splitScale, int maxSplits, int counter, float hpRate, ItemStack rose) {
            this.splitChance = splitChance;
            this.splitScale = splitScale;
            this.maxSplits = maxSplits;
            this.counter = counter;
            this.hpRate = hpRate;
            this.rose = rose;
        }

        public RoseStats(@Nullable LivingEntity livingEntity, ItemStack roseStack) {
            if (livingEntity == null || roseStack.isEmpty() || !(roseStack.getItem() instanceof ItemKnefRose relic))
                return;
            splitChance = relic.isAbilityUnlocked(livingEntity, roseStack, "rot_split") ? (float) relic.getStatValue(livingEntity, roseStack, "rot_split", "chance") : 0;
            splitScale = relic.isAbilityUnlocked(livingEntity, roseStack, "rot_split") ? (float) relic.getStatValue(livingEntity, roseStack, "rot_split", "split_size") : 0;
            maxSplits = relic.isAbilityUnlocked(livingEntity, roseStack, "rot_split") ? (int) relic.getStatValue(livingEntity, roseStack, "rot_split", "max_splits") : 0;
            hpRate = (float) relic.getStatValue(livingEntity, roseStack, "living_rot", "hp_rate");
            rose = roseStack;
        }

        public RoseStats(HolderLookup.Provider lookup, CompoundTag nbt) {
            deserializeNBT(lookup, nbt);
        }

        public int generateSplits(RandomSource src) {
            return 2 + (maxSplits > 2 ? src.nextInt(maxSplits - 1) : 0);
        }

        public RoseStats split() {
            RoseStats roseStats = new RoseStats(splitChance, splitScale, maxSplits, counter, hpRate, rose.copy());
            roseStats.counter++;
            return roseStats;
        }

    }

    private record SpawnChoice(
            EntityType<? extends Mob> type,
            int weight,
            int cost,
            boolean trash
    ) {
    }

}