package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.client.AnimationsRegistry;
import com.qurenie.relics_thirteenflames.content.entities.base.NonLivingEntity;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.zeith.hammeranims.api.animsys.AnimationSystem;
import org.zeith.hammeranims.api.tile.IAnimatedEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static com.qurenie.relics_thirteenflames.content.entities.AnimatedEntity.LAYER_ACTION;
import static com.qurenie.relics_thirteenflames.style.ColorScheme.BURN_COLOR;
import static com.qurenie.relics_thirteenflames.style.ColorScheme.CYAN_COLOR;

public class TravellerCutEntity extends NonLivingEntity implements IAnimatedEntity {

    public static final String OWNER_TAG = "owner";
    public static final String COMPLETION_TAG = "completion";
    public static final String ANGLE_X = "dir_x";
    public static final String SWEEP = "sweep";
    public static final String ANGLE_Z = "dir_z";
    public static final String STACK_TAG = "sword";
    public static final String FIRE_ASPECT_TAG = "fire_aspect";
    public static final String SPEED_TAG = "cut_speed";
    public static final int ANIM_LENGTH = 17;
    public static final int PRE_ANIM_LENGTH = 13;
    public static final int SWORD_RANGE = 5;
    private static final EntityDataAccessor<Vector3f> DIRECTION = SynchedEntityData.defineId(TravellerCutEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Float> COMPLETION = SynchedEntityData.defineId(TravellerCutEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(TravellerCutEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> FIRE_ASPECT = SynchedEntityData.defineId(TravellerCutEntity.class, EntityDataSerializers.INT);
    AnimationSystem system = AnimationSystem.create(this);
    UUID ownerUUID;
    Player owner;
    ItemStack stack;
    List<LivingEntity> targets = new ArrayList<>();
    @Setter @Getter
    boolean sweepAfterwards;

    public TravellerCutEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    public TravellerCutEntity(EntityType<? extends LivingEntity> entityType, Level level, Player owner, ItemStack stack, int fireAspect, float speed) {
        super(entityType, level);
        this.owner = owner;
        this.stack = stack;
        this.ownerUUID = owner.getUUID();
        setFireAspect(fireAspect);
        Vec3 look = owner.getLookAngle();
        setSwordDirection(new Vector3f((float) look.x, (float) look.y, (float) look.z));

        setCutSpeed(speed);

        this.setYBodyRot(owner.yHeadRot);
        this.noPhysics = true;
        system.startAnimationAt(LAYER_ACTION, AnimationsRegistry.ADVENTURER_SWORD_BIG_ATTACK
                .configure().transitionTime(0));
    }

    public static float directionToYBodyRot(Vector3f dir) {
        // atan2(-X, Z), чтобы соответствовать системе координат Minecraft
        float angleRad = (float) Math.atan2(-dir.x(), dir.z());
        return Mth.wrapDegrees((float) Math.toDegrees(angleRad - Math.PI / 2));
    }

    public static Vec3 rotateTowards(Vec3 from, Vec3 to, double angleRadians) {
        from = from.normalize();
        to = to.normalize();

        // Ось вращения — перпендикуляр к плоскости from-to
        Vec3 axis = from.cross(to).normalize();

        if (axis.lengthSqr() == 0) {
            // from и to параллельны: либо одинаковы, либо противоположны
            return to;
        }

        double cos = Math.cos(angleRadians);
        double sin = Math.sin(angleRadians);

        Vec3 term1 = from.scale(cos);
        Vec3 term2 = axis.cross(from).scale(sin);

        return term1.add(term2).normalize();
    }

    private static boolean isWalkable(Level level, Player player, Vec3 targetPos) {
        if (player.isSpectator())
            return true;

        AABB playerBox = player.getBoundingBox();
        AABB movedBox = playerBox.move(targetPos.x - player.getX(), targetPos.y - player.getY(), targetPos.z - player.getZ()).deflate(0.3);
        return level.noCollision(movedBox);
    }

    private float getPlayerDamage(Player p, Level level, Entity entity, ItemStack stack, DamageSource source) {
        float f = (float) p.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (level instanceof ServerLevel serverlevel) {
            f = EnchantmentHelper.modifyDamage(serverlevel, stack, entity, source, f);
        }
        return f;
    }

    private float getAnimLength() {
        return ANIM_LENGTH / getCutSpeed();
    }

    private float getPreAnimLength() {
        return PRE_ANIM_LENGTH / getCutSpeed();
    }

    private List<Float> getAngles() {
        float angle = (float) (Math.PI / 2.4 * getCompletion() - (Math.PI / 2 - Math.PI / 2.4));
        final float rot = 1f / (getAnimLength() - getPreAnimLength());
        final float damageRot = (float) (Math.PI / 60f);

        return Stream.iterate((float) Math.max(-Math.PI / 2f, angle - rot - damageRot), f -> f <= Math.min(Math.PI / 2f, angle + rot + damageRot), f -> f + damageRot).toList();
    }

    @Override
    public void tick() {
        system.tick();
        super.tick();
        this.yBodyRot = directionToYBodyRot(this.getSwordDirection());

        float speed = getCutSpeed();

        if (!this.level().isClientSide && owner == null) {
            discard();
            return;
        }

        Vec3 direction = new Vec3(getSwordDirection().x, 0, getSwordDirection().z);
        if (getCompletion() >= 1) {
            this.discard();
            if (level().isClientSide)
                return;

            Vec3 movePos = owner.position();
            for (float f = 0; f < SWORD_RANGE + 0.6; f += 0.5f) {
                Vec3 target = owner.position().add(direction.scale(f));
                if (!isWalkable(level(), owner, target))
                    break;
                movePos = target;
            }
            owner.teleportTo(movePos.x, movePos.y, movePos.z);
        }

        final var angles = getAngles();
        Vec3 y = new Vec3(0, 1, 0);

        if (level().isClientSide) {
            if (tickCount > getPreAnimLength() - 4)
                for (float angle : angles) {
                    Vec3 rotation = rotateTowards(y, direction, angle);
                    Vec3 end = position().add(rotation.normalize().scale(SWORD_RANGE + 0.6));
                    Vec3 start = position().add(rotation.normalize().scale(0.6));
                    if (getFireAspect() > 0) {
                        ParticleHelper.spawnParticleLine(level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, getRandom()), 0.23f, 8 + random.nextInt(2), (float) (0.6f + random.nextDouble() * 0.1)),
                                start, end, (int) (3 * speed), 0.04, 0.2);
                        ParticleHelper.spawnParticleLine(level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(BURN_COLOR, getRandom()), 0.23f, 8 + random.nextInt(2), (float) (0.6f + random.nextDouble() * 0.1)),
                                start, end, (int) (3 * speed), 0.04, 0.2);
                    } else
                        ParticleHelper.spawnParticleLine(level(), ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(CYAN_COLOR, getRandom()), 0.23f, 8 + random.nextInt(2), (float) (0.6f + random.nextDouble() * 0.1)),
                                start, end, (int) (4 * speed), 0.04, 0.2);
                }

            return;
        }

        this.setPos(owner.position().add(0, 0.5, 0).add(direction.normalize().scale(1)));
        if (tickCount % 4 == 0)
            detectTargets();

        targets.removeIf(target -> {
            boolean intersects = getAngles().stream().anyMatch(angle -> {
                Vec3 rotation = rotateTowards(y, direction, angle);

                Vec3 end = position().add(rotation.normalize().scale(SWORD_RANGE + 0.6));
                Vec3 start = position();
                return target.getBoundingBox().inflate(0.6, 0.6, 0.6).intersects(start, end);
            });
            if (!intersects)
                return false;
            target.setLastHurtByPlayer(owner);
            DamageSource source = owner.damageSources().playerAttack(owner);
            target.hurt(source, (float) (ItemsRegistry.TRAVELLER_SWORD.getStatValue(owner, stack, "swordcut", "damage") * getPlayerDamage(owner, level(), target, stack, source)));
            target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), getFireAspect() * 80));

            if (stack.getItem() == ItemsRegistry.TRAVELLER_SWORD)
                ItemsRegistry.TRAVELLER_SWORD.addExperience(owner, stack, 4);

            return true;
        });

        if (tickCount >= getPreAnimLength())
            setCompletion(getCompletion() + 1f / (getAnimLength() - getPreAnimLength()));
    }

    private void detectTargets() {
        targets = level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(SWORD_RANGE + 2, 3, SWORD_RANGE + 2), entity -> entity != owner);
    }

    private int getFireAspect() {
        return entityData.get(FIRE_ASPECT);
    }

    private void setFireAspect(int fireAspect) {
        entityData.set(FIRE_ASPECT, fireAspect);
    }

    private float getCompletion() {
        return entityData.get(COMPLETION);
    }

    private void setCompletion(float completion) {
        entityData.set(COMPLETION, completion);
    }

    private Vector3f getSwordDirection() {
        return this.entityData.get(DIRECTION);
    }

    private void setSwordDirection(Vector3f vector3f) {
        this.entityData.set(DIRECTION, vector3f);
    }

    private void bindOwner() {
        this.owner = level().getPlayerByUUID(ownerUUID);
    }

    private float getCutSpeed() {
        return entityData.get(SPEED);
    }

    private void setCutSpeed(float cutSpeed) {
        entityData.set(SPEED, cutSpeed);
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        super.remove(reason);

        if (!level().isClientSide && sweepAfterwards && owner != null && !stack.isEmpty()) {
            bindOwner();
            ItemsRegistry.TRAVELLER_SWORD.onSprintSweep(owner, stack);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DIRECTION, new Vector3f());
        builder.define(COMPLETION, 0f);
        builder.define(SPEED, 0f);
        builder.define(FIRE_ASPECT, 0);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.ownerUUID = compound.getUUID(OWNER_TAG);
        this.sweepAfterwards = compound.getBoolean(SWEEP);
        setFireAspect(compound.getInt(FIRE_ASPECT_TAG));
        setCompletion(compound.getFloat(COMPLETION_TAG));
        setSwordDirection(new Vector3f(compound.getFloat(ANGLE_X), 0, compound.getFloat(ANGLE_Z)));
        this.stack = ItemStack.parse(level().registryAccess(), Objects.requireNonNull(compound.get(STACK_TAG))).orElse(ItemStack.EMPTY);
        bindOwner();
        setSpeed(compound.getFloat(SPEED_TAG));
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putUUID(OWNER_TAG, ownerUUID);
        compound.putFloat(SPEED_TAG, getCutSpeed());
        compound.putBoolean(SWEEP, sweepAfterwards);
        compound.putInt(FIRE_ASPECT_TAG, getFireAspect());
        compound.putFloat(COMPLETION_TAG, getCompletion());
        compound.putDouble(ANGLE_X, getSwordDirection().x);
        compound.putDouble(ANGLE_Z, getSwordDirection().z);
        Tag stacktag = stack.save(level().registryAccess(), new CompoundTag());
        compound.put(STACK_TAG, stacktag);
    }

    @Override
    public void setupSystem(AnimationSystem.Builder builder) {
        FlamesUtils.setupAnimationSystem(builder);
    }

    @Override
    public AnimationSystem getAnimationSystem() {
        return system;
    }

}
