package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.style.ColorScheme;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.octostudios.octolib.util.OctoColor;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerlib.util.charging.ItemChargeHelper;

import static com.qurenie.relics_thirteenflames.style.ColorScheme.HETT_COLOR;
import static com.qurenie.relics_thirteenflames.style.ColorScheme.SOUL_COLOR;

@Getter
public class BookOrbEntity extends Entity {

    public static final String TARGET_TAG = "target";
    public static final String BOOK_TAG = "book";
    public static final String LEVEL_TAG = "level";
    public static final String UPGRADED_TAG = "upgraded";
    public static final String MAX_SIZE_TAG = "maxSize";
    public static final String REPAIR_COUNT_TAG = "repairCount";

    private int targetID;
    private Entity target;
    private Vec3 prevPos;
    private @NotNull ItemStack book = ItemStack.EMPTY;
    private int lvl;
    private boolean upgraded;
    private int maxSize;
    private int repairCount;

    public BookOrbEntity(EntityType<?> entityScintType, Level level) {
        super(entityScintType, level);
    }

    public BookOrbEntity(
            Level level,
            LivingEntity target,
            Player player,
            @NotNull ItemStack book,
            int lvl,
            boolean upgraded,
            int maxSize,
            int repairCount
    ) {
        super(EntityRegistry.BOOK_ORB, level);

        this.target = target;
        this.targetID = target.getId();

        this.book = book;

        this.lvl = lvl;
        this.upgraded = upgraded;
        this.maxSize = maxSize;
        this.repairCount = repairCount;

        setPos(player.getBoundingBox().getCenter());

        this.setYRot((float) (Math.random() * Math.PI * 2));

        setDeltaMovement(
                new Vec3(
                        getRandom().nextGaussian(),
                        getRandom().nextDouble(),
                        getRandom().nextGaussian()
                ).normalize().scale(0.4)
        );
    }

    @Override
    public void tick() {
        if (prevPos == null) prevPos = this.position();

        if (level().isClientSide) {
            ParticleHelper.spawnParticleLine(level(), ParticleHelper.constructSimpleSpark(ColorScheme.HETT_COLOR, 0.16f,
                    3, 0.5f), position(), prevPos, 3, () -> new Vec3(level().random.nextGaussian() * 0.003, 0.02 + level().random.nextGaussian() * 0.003, level().random.nextGaussian() * 0.003), 0);

            ParticleHelper.spawnParticleLine(level(), ParticleHelper.constructSmoke(ColorScheme.HETT_COLOR, 0.1f, 8, 0),
                    position(), prevPos, 2, () -> new Vec3(level().random.nextGaussian() * 0.002, 0.013 + level().random.nextGaussian() * 0.002, level().random.nextGaussian() * 0.002), 0);

            prevPos = this.position();
            return;
        }

        if (target == null || book == ItemStack.EMPTY) {
            remove(RemovalReason.DISCARDED);
            return;
        }

        super.tick();

        prevPos = this.position();
        if (level().isClientSide)
            return;

        if (target.getBoundingBox().inflate(0.3).contains(this.position())) {
            FeatherVortexEntity featherVortexEntity = new FeatherVortexEntity(
                    (LivingEntity) target,
                    level(),
                    lvl,
                    upgraded,
                    maxSize,
                    repairCount,
                    book
            );

            level().addFreshEntity(featherVortexEntity);

            discard();
        }

        final double maxSpeed = 0.5;
        Vec3 target = this.target.getBoundingBox().getCenter();
        Vec3 acceleration = target.subtract(this.position()).normalize().scale(0.1);
        Vec3 movement = this.getDeltaMovement().scale(0.95).add(acceleration);
        if (movement.lengthSqr() > maxSpeed * maxSpeed)
            movement = movement.normalize().scale(maxSpeed);
        setDeltaMovement(movement);
        this.moveTo(position().x + getDeltaMovement().x, position().y + getDeltaMovement().y, position().z + getDeltaMovement().z);
        if (this.tickCount > 200)
            this.discard();
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        super.remove(reason);
        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(HETT_COLOR, 0.34f,
                25, 0.95f).withGravity(0.4f), this, 15, 0.07);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compound) {
        this.targetID = compound.getInt(TARGET_TAG);
        this.target = level().getEntity(targetID);

        this.book = ItemStack.parse(
                registryAccess(),
                compound.getCompound(BOOK_TAG)
        ).orElse(ItemStack.EMPTY);

        this.lvl = compound.getInt(LEVEL_TAG);
        this.upgraded = compound.getBoolean(UPGRADED_TAG);
        this.maxSize = compound.getInt(MAX_SIZE_TAG);
        this.repairCount = compound.getInt(REPAIR_COUNT_TAG);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compound) {
        compound.putInt(TARGET_TAG, targetID);

        compound.put(BOOK_TAG, book.save(registryAccess(), new CompoundTag()));

        compound.putInt(LEVEL_TAG, lvl);
        compound.putBoolean(UPGRADED_TAG, upgraded);
        compound.putInt(MAX_SIZE_TAG, maxSize);
        compound.putInt(REPAIR_COUNT_TAG, repairCount);
    }
    @Override
    public boolean isAlive() {
        return false;
    }

}
