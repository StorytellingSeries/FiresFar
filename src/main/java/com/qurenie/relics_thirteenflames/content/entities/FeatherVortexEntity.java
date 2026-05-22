package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.client.AnimationsRegistry;
import com.qurenie.relics_thirteenflames.content.entities.base.NonLivingEntity;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import lombok.Getter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammeranims.api.animation.LoopMode;
import org.zeith.hammeranims.api.animsys.AnimationSystem;
import org.zeith.hammeranims.api.tile.IAnimatedEntity;

import java.util.ArrayList;
import java.util.List;

import static com.qurenie.relics_thirteenflames.content.entities.AnimatedEntity.LAYER_ACTION;
import static com.qurenie.relics_thirteenflames.style.ColorScheme.HETT_COLOR;
import static com.qurenie.relics_thirteenflames.util.FlamesUtils.setupAnimationSystem;

@Getter
public class FeatherVortexEntity extends NonLivingEntity implements IAnimatedEntity {

    private final AnimationSystem animationSystem = AnimationSystem.create(this);

    private int lvl;

    private boolean upgraded;
    private int maxSize;
    private int repairCount;
    private @Nullable ItemStack prevBook;

    public FeatherVortexEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
        this.lvl = 1;
        this.upgraded = false;
        this.maxSize = 1;
        this.repairCount = 0;
        this.prevBook = null;
        setNoGravity(true);
        this.noPhysics = true;
    }

    public FeatherVortexEntity(
            LivingEntity target,
            Level level,
            int lvl,
            int maxSize
    ) {
        this(target, level, lvl, false, maxSize, 0, null);
    }

    public FeatherVortexEntity(
            LivingEntity target,
            Level level,
            int lvl,
            boolean upgraded,
            int maxSize,
            int repairCount,
            @Nullable ItemStack prevBook
    ) {
        this(EntityRegistry.FEATHER_VORTEX_ENTITY, level);
        this.setPos(target.position());
        target.startRiding(this);

        this.lvl = lvl;
        this.upgraded = upgraded;
        this.maxSize = maxSize;
        this.repairCount = repairCount;
        this.prevBook = prevBook == null ? null : prevBook.copy();

        animationSystem.startAnimationAt("ANIMATION_1", AnimationsRegistry.ATTACK_BOOK_IDLE);
        animationSystem.startAnimationAt(LAYER_ACTION, AnimationsRegistry.BOOK_OPEN.configure()
                .speed(0.7f)
                .startTime(0.5f)
                .loopMode(LoopMode.ONCE)
                .next(AnimationsRegistry.BOOK_ATTACK.configure().loopMode(LoopMode.ONCE)));
    }

    private ItemStack buildResultBook(EntityType<?> newType) {
        ItemStack result;

        if (!upgraded) {
            result = ItemsRegistry.HETT_FEATHER_BOOK.getDefaultInstance();
            result.set(ComponentRegistry.SIZE, maxSize);
            result.set(ComponentRegistry.TARGET_TYPES, List.of(newType));
            result.set(ComponentRegistry.LEVEL, lvl);
            return result;
        }

        if (prevBook == null) {
            result = ItemsRegistry.HETT_FEATHER_BOOK.getDefaultInstance();
            result.set(ComponentRegistry.SIZE, maxSize);
            result.set(ComponentRegistry.TARGET_TYPES, List.of(newType));
            result.set(ComponentRegistry.LEVEL, lvl);
            return result;
        }

        result = prevBook.copy();

        List<EntityType<?>> types = new ArrayList<>(result.getOrDefault(ComponentRegistry.TARGET_TYPES, List.of()));

        if (types.contains(newType)) {
            int damage = result.getDamageValue();
            result.setDamageValue(Math.max(0, damage - repairCount));
            return result;
        }

        int size = result.getOrDefault(ComponentRegistry.SIZE, 1);

        if (types.size() >= size) {
            types.addFirst(newType);
            types.removeLast();
        } else {
            types.addFirst(newType);
        }

        result.set(ComponentRegistry.TARGET_TYPES, types);
        return result;
    }

    @Override
    public void tick() {
        animationSystem.tick();
        super.tick();

        var currentAnim = animationSystem.getLayer(LAYER_ACTION).currentAnimation;
        if (currentAnim == null) {
            discard();
            return;
        }

        if (level().isClientSide) {
            for (int i = 0; i < 5; i++) {
                double y = 0.5 + random.nextDouble() * 1.5;
                Vec3 center = new Vec3(getX(), getY() + y, getZ());

                for (int j = 0; j < 3; j++) {
                    double r = y / 1.5 * 0.7;

                    double x = random.nextGaussian() * r;
                    double z = Math.sqrt(r * r - x * x) * (random.nextBoolean() ? 1 : -1);
                    Vec3 spawn = center.add(x, 0, z);

                    Vec3 radius = spawn.subtract(center);
                    Vec3 move = radius.normalize().yRot((float) (230f * Math.PI / 180f)).add(0, 0.02f, 0);
                    ParticleHelper.spawnDirectedParticle(
                            level(),
                            ParticleHelper.constructSimpleSpark(HETT_COLOR, 0.13f, 20, 0.91f),
                            spawn,
                            move.normalize().scale(0.03 * y)
                    );
                }
            }
        } else if (
                currentAnim.config.animation.getLocation().equals(AnimationsRegistry.BOOK_ATTACK.getLocation())
                        && FlamesUtils.getCompletion(animationSystem, animationSystem.getLayer(LAYER_ACTION)) >= 0.6
        ) {
            if (this.getPassengers().isEmpty() && prevBook != null) {
                ItemEntity item = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), prevBook);
                this.level().addFreshEntity(item);
            }

            for (Entity e : this.getPassengers()) {
                ItemStack stack = buildResultBook(e.getType());

                ItemEntity item = new ItemEntity(e.level(), e.getX(), e.getY(), e.getZ(), stack);
                e.level().addFreshEntity(item);

                ParticleHelper.spawnParticleEntity(ParticleTypes.CAMPFIRE_COSY_SMOKE, e, 20, 0.05);
                ParticleHelper.spawnParticleEntity(
                        ParticleHelper.constructSmoke(HETT_COLOR, (e.getBbHeight() + e.getBbWidth()) / 2, 60, 0).withLightning(false),
                        e,
                        20,
                        0.03
                );

                e.discard();
            }

            discard();
        }
    }

    @Override
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    protected void positionRider(@NotNull Entity passenger, @NotNull MoveFunction callback) {
        Vec3 target = position().add(new Vec3(0, 2.5 + passenger.getBbHeight(), 0)
                .scale(Mth.sin((tickCount) / 6f))).add(0, 1.5 + passenger.getBbHeight(), 0);
        Vec3 delta = target.subtract(passenger.position()).normalize().scale(0.08);
        callback.accept(passenger, passenger.getX() + delta.x, passenger.getY() + delta.y, passenger.getZ() + delta.z);
        passenger.setYBodyRot((20 * tickCount) % 360);
        passenger.setYHeadRot((20 * tickCount) % 360);
        passenger.setYRot((20 * tickCount) % 360);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        this.lvl = compound.getInt("lvl");

        this.upgraded = compound.getBoolean("upgraded");
        this.maxSize = compound.getInt("maxSize");
        this.repairCount = compound.getInt("repairCount");

        if (compound.contains("prevBook")) {
            this.prevBook = ItemStack.parseOptional(
                    level().registryAccess(),
                    compound.getCompound("prevBook")
            );
        } else {
            this.prevBook = null;
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putInt("lvl", lvl);

        compound.putBoolean("upgraded", upgraded);
        compound.putInt("maxSize", maxSize);
        compound.putInt("repairCount", repairCount);

        if (prevBook != null) {
            compound.put("prevBook", prevBook.save(level().registryAccess()));
        }
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean isInWall() {
        return false;
    }

    @Override
    public void setupSystem(AnimationSystem.Builder builder) {
        setupAnimationSystem(builder);
    }
}