package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.content.items.ItemJodahStaff;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.style.ColorScheme;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.octostudios.octolib.util.OctoColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class JodahMarkEntity extends Entity {

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(JodahMarkEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public JodahMarkEntity(EntityType<? extends JodahMarkEntity> type, Level level) {
        super(type, level);
    }

    public JodahMarkEntity(Level level, LivingEntity owner, Vec3 position) {
        super(EntityRegistry.JODAH_MARK, level);
        setPos(position);
        setOwner(owner);
    }

    // ===== Owner =====

    public void setOwner(@Nullable LivingEntity owner) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(owner).map(LivingEntity::getUUID));
    }

    @Nullable
    public LivingEntity getOwner() {
        var uuidOpt = this.entityData.get(OWNER_UUID);

        if (uuidOpt.isPresent() && this.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(uuidOpt.get());
            if (entity instanceof LivingEntity living) {
                return living;
            }
        }

        return null;
    }

    // ===== Synced Data =====

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_UUID, Optional.empty());
    }

    // ===== Save / Load =====

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.entityData.set(OWNER_UUID, Optional.of(tag.getUUID("Owner")));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        var uuid = this.entityData.get(OWNER_UUID);
        uuid.ifPresent(value -> tag.putUUID("Owner", value));
    }

    // ===== Tick =====

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            final float radius = 0.5f;

            Vec2 point = FlamesUtils.randomCirclePoint(radius, false);
            Vec3 offset = new Vec3(point.x, 0, point.y);
            Vec3 spawn = position().add(offset);
            Vec3 move = offset.cross(new Vec3(0, 1, 0));

            if (tickCount % 2 == 0)
                return;

            ParticleHelper.spawnDirectedParticle(
                    level(),
                    ParticleHelper.constructFigure(OctoColor.WHITE, 0.15f, 20 + level().random.nextInt(10), 0.93f)
                            .withGravity(-0.3f),
                    spawn,
                    move.scale(0.04).add(0, 0, 0)
            );


            point = FlamesUtils.randomCirclePoint(radius, true);
            ParticleHelper.spawnParticles(level(),
                    ParticleHelper.constructSmoke(ColorScheme.PURPLE_COLOR, 0.4f, 40).withGravity(-0.05f),
                    position().add(point.x, 0, point.y), 1, 0f, 0f, 0f, 0.01f);
            return;
        }

        if (!(getOwner() instanceof Player player) || player.level() != this.level()) {
            this.discard();
            return;
        }

        if (!isCachedStaffValid(player)) {
            updateCache(player);

            if (cachedSlot == -1)
                discard();
        }
    }

    public void teleportToMark(LivingEntity player) {
        if (player.level() != this.level()) {
            discard();
            return;
        }

        player.teleportTo(position().x, position().y, position().z);
        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(ColorScheme.PURPLE_COLOR, 0.4f, 80, 0.97f),
                player, 25, 0.2f);
        ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(ColorScheme.PURPLE_COLOR, 0.4f, 80, 0.97f).withGravity(0.9f),
                player, 25, 0.04f);
        ParticleHelper.spawnParticleEntity(ParticleHelper.constructFigure(OctoColor.WHITE, 0.4f, 100 + level().random.nextInt(20),
                        (float) (0.95f + Math.random() * 0.02)),
                player, 20, 0.03f);
        this.discard();
    }

    private int cachedSlot = -1;
    private boolean cachedOffhand = false;

    public void updateCache(Player player) {

        // сначала проверяем offhand
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof ItemJodahStaff
                && offhand.has(ComponentRegistry.JODAH_ACTIVE_TICK)
                && offhand.get(ComponentRegistry.ENTITY_UUID) == this.uuid) {

            cachedOffhand = true;
            cachedSlot = -1;
            return;
        }

        // потом обычный инвентарь
        var inv = player.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (stack.getItem() instanceof ItemJodahStaff
                    && stack.has(ComponentRegistry.JODAH_ACTIVE_TICK)
                    && stack.get(ComponentRegistry.ENTITY_UUID) == this.uuid) {

                cachedSlot = i;
                cachedOffhand = false;
                return;
            }
        }

        cachedSlot = -1;
        cachedOffhand = false;
    }

    public boolean isCachedStaffValid(Player player) {
        if (cachedOffhand) {
            ItemStack stack = player.getOffhandItem();
            return stack.getItem() instanceof ItemJodahStaff
                    && stack.has(ComponentRegistry.JODAH_ACTIVE_TICK)
                    && stack.get(ComponentRegistry.ENTITY_UUID) == this.uuid;
        }

        if (cachedSlot < 0 || cachedSlot >= player.getInventory().getContainerSize())
            return false;

        ItemStack stack = player.getInventory().getItem(cachedSlot);

        return stack.getItem() instanceof ItemJodahStaff
                && stack.has(ComponentRegistry.JODAH_ACTIVE_TICK)
                && stack.get(ComponentRegistry.ENTITY_UUID) == this.uuid;
    }
}