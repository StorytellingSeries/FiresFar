package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.octostudios.octolib.util.OctoColor;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerlib.util.charging.ItemChargeHelper;

import static com.qurenie.relics_thirteenflames.style.ColorScheme.SOUL_COLOR;

@Getter
public class SoulOrbEntity extends Entity {

    public static final String TARGET_TAG = "target";
    public static final String VALUE_TAG = "value";

    private int targetID;
    private Entity target;
    private Vec3 prevPos;
    private int value;

    public SoulOrbEntity(EntityType<?> entityScintType, Level level) {
        super(entityScintType, level);
    }

    public SoulOrbEntity(Level level, Player target, double x, double y, double z, int value) {
        super(EntityRegistry.SOUL_ORB, level);
        this.target = target;
        this.targetID = target.getId();
        setPos(new Vec3(x, y, z));
        this.value = value;
        this.setYRot((float) (Math.random() * Math.PI * 2));
        setDeltaMovement(new Vec3(getRandom().nextGaussian(), getRandom().nextDouble(), getRandom().nextGaussian()).normalize().scale(0.4));
    }

    public SoulOrbEntity(Level level, Player to, BlockPos from, int value) {
        super(EntityRegistry.SOUL_ORB, level);
        this.target = to;
        this.targetID = to.getId();
        setPos(from.getCenter().add(0, 0.5, 0));
        this.value = value;
        this.setYRot((float) (Math.random() * Math.PI * 2));
        setDeltaMovement(new Vec3(getRandom().nextGaussian(), getRandom().nextDouble(), getRandom().nextGaussian()).normalize().scale(0.4));
    }

    @Override
    public void tick() {
        if (!level().isClientSide && target == null) {
            remove(RemovalReason.DISCARDED);
            return;
        }
        if (prevPos == null) prevPos = this.position();

        super.tick();

        if (level().isClientSide) {
            ParticleHelper.spawnParticleLine(level(), ParticleHelper.constructSimpleSpark(OctoColor.WHITE, 0.16f,
                    3, 0.5f), position(), prevPos, 3, () -> new Vec3(level().random.nextGaussian() * 0.003, 0.02 + level().random.nextGaussian() * 0.003, level().random.nextGaussian() * 0.003), 0);

            ParticleHelper.spawnParticleLine(level(), ParticleHelper.constructSmoke(SOUL_COLOR, 0.1f, 8, 0),
                    position(), prevPos, 2, () -> new Vec3(level().random.nextGaussian() * 0.002, 0.013 + level().random.nextGaussian() * 0.002, level().random.nextGaussian() * 0.002), 0);
        }

        prevPos = this.position();
        if (level().isClientSide)
            return;

        if (target.getBoundingBox().inflate(0.3).contains(this.position())
                && target instanceof Player player) {
            this.kill();
            var itr = ItemChargeHelper.listPlayerInventories(player).iterator();
            while (itr.hasNext()) {
                var ih = itr.next();
                for (int j = 0; j < ih.getSlots(); j++) {
                    var it = ih.getStackInSlot(j);
                    if (it.is(ItemsRegistry.KNEF_ROSE)) {
                        ItemsRegistry.KNEF_ROSE.addSouls(player, it, value);
                    }
                }
            }
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
        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(SOUL_COLOR, 0.34f,
                25, 0.95f).withGravity(0.4f), this, 15, 0.07);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compound) {
        this.targetID = compound.getInt(TARGET_TAG);
        this.target = level().getEntity(targetID);
        this.value = compound.getInt(VALUE_TAG);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compound) {
        compound.putInt(TARGET_TAG, targetID);
        compound.putInt(VALUE_TAG, value);
    }

    @Override
    public boolean isAlive() {
        return false;
    }

}
