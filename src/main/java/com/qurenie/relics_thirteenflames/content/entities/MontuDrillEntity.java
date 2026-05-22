package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.client.particles.RotateSettings;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.qurenie.relics_thirteenflames.style.ColorScheme.MONTU_GREEN;

@Getter
@Setter
public class MontuDrillEntity extends Entity {

    /*
        ============================================
                    DATA
        ============================================
     */

    protected static final EntityDataAccessor<Float> CAPACITY = SynchedEntityData.defineId(MontuDrillEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> FORTUNE = SynchedEntityData.defineId(MontuDrillEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Boolean> SILK_TOUCH = SynchedEntityData.defineId(MontuDrillEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Float> STRENGTH = SynchedEntityData.defineId(MontuDrillEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(MontuDrillEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    /*
        ============================================
     */

    protected Vec3 velocity = Vec3.ZERO;

    protected float passiveDrain = 0.45f;

    public MontuDrillEntity(EntityType<?> type, Level level) {
        super(type, level);

        noPhysics = true;
    }

    public MontuDrillEntity(Level level, Vec3 pos, Vec3 velocity, float capacity, int fortune, boolean silkTouch, float strength, @Nullable Entity owner) {
        this(EntityRegistry.MONTU_DRILL_ENTITY, level);

        setPos(pos);

        this.velocity = velocity;
        setDeltaMovement(velocity);

        setCapacity(capacity);
        setFortune(fortune);
        setSilkTouch(silkTouch);
        setStrength(strength);

        setOwner(owner);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

        builder.define(CAPACITY, 100F);
        builder.define(FORTUNE, 0);
        builder.define(SILK_TOUCH, false);
        builder.define(STRENGTH, 8F);

        builder.define(OWNER, Optional.empty());
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide)
            spawnDrillParticles();

        if (!level().isClientSide && getCapacity() <= 0) {
            destroyDrill();
            return;
        }

        move(MoverType.SELF, velocity);

        if (!level().isClientSide) {
            setDeltaMovement(velocity);

            setCapacity(getCapacity() - passiveDrain);

            destroyBlocks();

            damageEntities();
        }

    }

    /*
        ============================================
                    BLOCK BREAKING
        ============================================
     */

    protected void destroyBlocks() {

        if (!(level() instanceof ServerLevel level)) return;

        Vec3 dir = velocity.normalize();

        AABB box = getBoundingBox().inflate(0.9);

        BlockPos.betweenClosedStream(box).forEach(pos -> {

            BlockState state = level.getBlockState(pos);

            if (state.isAir()) return;

            if (!canMine(state)) return;

            float hardness = state.getDestroySpeed(level, pos);

            if (hardness < 0) return;

            drainCapacity(Math.max(0.3f, hardness));

            ItemStack tool = createTool();

            var owner = getOwner();
            if (owner instanceof Player player)
                state.getBlock().playerDestroy(level, player, pos, state, null, tool);
            level.destroyBlock(pos, false, owner);

            level.destroyBlock(pos, true, this, 512);

        });
    }

    protected boolean canMine(BlockState state) {

        return state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    protected ItemStack createTool() {

        ItemStack stack = new ItemStack(Items.NETHERITE_PICKAXE);

        if (getFortune() > 0) {
            stack.enchant(level().registryAccess().holderOrThrow(Enchantments.FORTUNE), getFortune());
        }

        if (isSilkTouch()) {
            stack.enchant(level().registryAccess().holderOrThrow(Enchantments.SILK_TOUCH), 1);
        }

        return stack;
    }

    /*
        ============================================
                    ENTITY DAMAGE
        ============================================
     */

    protected void damageEntities() {

        List<Entity> entities = level().getEntities(this, getBoundingBox().inflate(1.2), entity -> entity.isAlive() && entity != getOwner() && entity != this);

        if (entities.isEmpty()) return;

        drainCapacity(passiveDrain * 3F);
        setDeltaMovement(getDeltaMovement().scale(0.4));

        for (Entity entity : entities) {

            if (!(entity instanceof LivingEntity living)) continue;

            if (tickCount % 20 == 0) {

                DamageSource source = damageSources().mobProjectile(this, getOwner() instanceof LivingEntity livingOwner ? livingOwner : null);

                living.hurt(source, getStrength());

                level().playSound(null, blockPosition(), SoundEvents.ANVIL_HIT, getSoundSource(), 0.8F, 1.5F);
            }

            Vec3 push = velocity.normalize().scale(0.12);

            entity.setDeltaMovement(entity.getDeltaMovement().add(push));
        }
    }

    /*
        ============================================
                    PARTICLES
        ============================================
     */

    protected void spawnDrillParticles() {

        Vec3 pos = position();

        Vec3 dir = getDeltaMovement().normalize().scale(0.3);
        Vec3 particleSpeed = getDeltaMovement().subtract(getDeltaMovement().normalize().scale(0.08f));

        /*
            Вращающаяся спираль
         */

        double angleSpeed = 1.2;
        double maxI = 4;

        for (int i = 0; i < maxI; i++) {

            double progress = i / maxI;

            double angle = angleSpeed * i / maxI;

            Vec3 up = Math.abs(dir.y) > 0.99
                    ? new Vec3(1, 0, 0)
                    : new Vec3(0, 1, 0);

            Vec3 side = dir.cross(up).normalize().scale(0.01);

            Vec3 center = pos.subtract(dir.scale(progress));

            Vec3 spawn = center.add(side);

            RotateSettings settings = RotateSettings.builder().center(center).relativeAxis(dir).resizeSpeed(0.06).angleSpeed(angleSpeed).angle0(-angle).build();

            ParticleHelper.spawnDirectedParticle(level(), ParticleHelper.constructRotatedSpark(settings, MONTU_GREEN, 0.2f, 20, 1.05f),
                    spawn, particleSpeed);

        }

        /*
            Наконечник
         */

        Vec3 front = pos.add(dir.scale(0.9));

        for (int i = 0; i < 4; i++) {

            double angle = ((Math.PI * 2D) / 4D) * i + tickCount;

            Vec3 side = getPerpendicular(dir, angle);

            Vec3 spawn = front.add(side.scale(0.12));

            RotateSettings settings = RotateSettings.builder().center(front).relativeAxis(dir).resizeSpeed(-0.015).angleSpeed(1.3).build();

            ParticleHelper.spawnDirectedParticle(level(), ParticleHelper.constructRotatedSpark(settings, MONTU_GREEN, 0.12f, 10, 0.98f), spawn, particleSpeed);
        }

        /*
            Воздушный след
         */

        if (tickCount % 2 == 0) {

            Vec3 back = pos.subtract(dir.scale(0.8));

            ParticleHelper.spawnParticles(level(), ParticleHelper.constructSmoke(MONTU_GREEN, 0.2f, 20), back.x, back.y, back.z, 1, 0.08, 0.08, 0.08, 0.002);
        }
    }

    /*
        ============================================
                    HELPERS
        ============================================
     */

    protected void destroyDrill() {

        if (level() instanceof ServerLevel level) {

            for (int i = 0; i < 24; i++) {

                Vec3 v = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize().scale(0.08 + random.nextDouble() * 0.12);

                ParticleHelper.spawnDirectedParticle(level, ParticleHelper.constructSmoke(MONTU_GREEN, 0.25f, 30), position(), v);
            }
        }

        discard();
    }

    protected void drainCapacity(float amount) {
        setCapacity(Math.max(0, getCapacity() - amount));
    }

    protected static Vec3 getPerpendicular(Vec3 axis, double angle) {

        axis = axis.normalize();

        Vec3 helper = Math.abs(axis.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);

        Vec3 x = axis.cross(helper).normalize();

        Vec3 z = axis.cross(x).normalize();

        return x.scale(Math.cos(angle)).add(z.scale(Math.sin(angle)));
    }

    /*
        ============================================
                    OWNER
        ============================================
     */

    public void setOwner(@Nullable Entity entity) {

        entityData.set(OWNER, entity == null ? Optional.empty() : Optional.of(entity.getUUID()));
    }

    @Nullable
    public Entity getOwner() {

        Optional<UUID> uuid = entityData.get(OWNER);

        if (uuid.isEmpty()) return null;

        if (!(level() instanceof ServerLevel serverLevel)) return null;

        return serverLevel.getEntity(uuid.get());
    }

    /*
        ============================================
                    GETTERS
        ============================================
     */

    public float getCapacity() {
        return entityData.get(CAPACITY);
    }

    public void setCapacity(float value) {
        entityData.set(CAPACITY, value);
    }

    public int getFortune() {
        return entityData.get(FORTUNE);
    }

    public void setFortune(int value) {
        entityData.set(FORTUNE, value);
    }

    public boolean isSilkTouch() {
        return entityData.get(SILK_TOUCH);
    }

    public void setSilkTouch(boolean value) {
        entityData.set(SILK_TOUCH, value);
    }

    public float getStrength() {
        return entityData.get(STRENGTH);
    }

    public void setStrength(float value) {
        entityData.set(STRENGTH, value);
    }

    /*
        ============================================
                    SAVE DATA
        ============================================
     */

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {

        setCapacity(tag.getFloat("capacity"));
        setFortune(tag.getInt("fortune"));
        setSilkTouch(tag.getBoolean("silk_touch"));
        setStrength(tag.getFloat("strength"));

        velocity = new Vec3(tag.getDouble("vx"), tag.getDouble("vy"), tag.getDouble("vz"));

        if (tag.hasUUID("owner")) {

            entityData.set(OWNER, Optional.of(tag.getUUID("owner")));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {

        tag.putFloat("capacity", getCapacity());

        tag.putInt("fortune", getFortune());

        tag.putBoolean("silk_touch", isSilkTouch());

        tag.putFloat("strength", getStrength());

        tag.putDouble("vx", velocity.x);
        tag.putDouble("vy", velocity.y);
        tag.putDouble("vz", velocity.z);

        Entity owner = getOwner();

        if (owner != null) {
            tag.putUUID("owner", owner.getUUID());
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }
}