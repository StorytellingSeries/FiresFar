package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.client.particles.RotateSettings;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.qurenie.relics_thirteenflames.style.ColorScheme.AIR_COLOR;

@Getter
@Setter
public class AirVortexEntity extends Entity {

    protected static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(AirVortexEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> MAX_AGE = SynchedEntityData.defineId(AirVortexEntity.class, EntityDataSerializers.INT);

    int age = 0;
    Vec3 prevPos;

    /*
        ===========================
                OWNER
        ===========================
     */

    protected static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(AirVortexEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    protected Vec3 velocity = Vec3.ZERO;

    protected boolean vortexStarted;

    public AirVortexEntity(EntityType<?> type, Level level) {
        super(type, level);

        this.noPhysics = true;
    }

    public AirVortexEntity(Level level, Vec3 pos, Vec3 velocity, float radius, int maxAge, @Nullable Entity owner) {
        this(EntityRegistry.AIR_VORTEX_ENTITY, level);

        this.setPos(pos);

        setDeltaMovement(velocity);

        this.setRadius(radius);
        this.setMaxAge(maxAge);

        this.setOwner(owner);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 6F);
        builder.define(MAX_AGE, 20 * 15);

        builder.define(OWNER_UUID, Optional.empty());
    }

    /*
        ===========================
            OWNER ACCESS
        ===========================
     */

    public void setOwner(@Nullable Entity entity) {
        entityData.set(OWNER_UUID, entity == null ? Optional.empty() : Optional.of(entity.getUUID()));
    }

    @Nullable
    public UUID getOwnerUUID() {
        return entityData.get(OWNER_UUID).orElse(null);
    }

    @Nullable
    public Entity getOwner() {

        UUID uuid = getOwnerUUID();

        if (uuid == null) return null;

        if (!(level() instanceof ServerLevel serverLevel)) return null;

        return serverLevel.getEntity(uuid);
    }

    @Override
    public void tick() {
        super.tick();

        if (tickCount >= 1000) discard();

        if (prevPos == null) prevPos = position();

        /*
            Фаза полета шара
         */

        if (!vortexStarted) {

            this.move(MoverType.SELF, getDeltaMovement());

            if (!level().isClientSide) setDeltaMovement(getDeltaMovement().scale(0.84f));

            spawnBallParticles();

            /*
                Почти остановился —
                начинается вихрь
             */
            if (getDeltaMovement().length() < 0.00007) {
                vortexStarted = true;

                RotateSettings settings = RotateSettings.builder().center(position()).relativeAxis(new Vec3(0, 1, 0)).resizeSpeed(0.01).angleSpeed(0.2).build();

                ParticleHelper.spawnParticles(level(), ParticleHelper.constructRotatedSpark(settings, AIR_COLOR, 0.5f, 58, 0.95f),
                        position(), (int) Math.min(getRadius() / 2, 60), 1, 1, 1, 0f);
                setDeltaMovement(Vec3.ZERO);
            }

            prevPos = position();
            return;
        }

        age++;

        /*
            Основная механика вихря
         */

        if (!level().isClientSide) suckEntities();
        else spawnVortexParticles();

        if (!level().isClientSide && age >= getMaxAge()) {
            discard();
        }
    }

    protected void suckEntities() {
        double radius = getRadius();

        AABB box = getBoundingBox().inflate(radius, 4, radius);
        Entity owner = getOwner();

        List<Entity> entities = level().getEntities(this, box, entity -> entity.isAlive() && entity.isPickable() && entity != this && entity != owner || entity instanceof ItemEntity);

        Vec3 center = position().add(0, 1.0, 0);

        for (Entity entity : entities) {

            if (entity instanceof AirVortexEntity) continue;

            Vec3 delta = center.subtract(entity.position());

            double distance = Math.max(0.25, delta.length());

            if (distance > radius) continue;

            double strength = 1.0 - (distance / radius);

            /*
                Стягивание
             */
            Vec3 pull = delta.normalize().scale(0.44 * strength);

            /*
                Закрутка
             */
            Vec3 tangent = new Vec3(-delta.z, 0, delta.x).normalize().scale(0.05 * strength);

            /*
                Подъем воздуха
             */
            Vec3 lift = new Vec3(0, 0.03 * strength, 0);

            entity.setDeltaMovement(entity.getDeltaMovement().add(pull).add(tangent).add(lift));

            entity.hurtMarked = true;

            /*
                Чтобы мобов не ломало pathfinding'ом
             */
            if (entity instanceof PathfinderMob mob) {
                mob.getNavigation().stop();
            }
        }
    }

    /*
    =========================================================
                    OPTIMIZED BALL VISUALS
    =========================================================
 */

    protected void spawnBallParticles() {
        if (!(level() instanceof ServerLevel level)) return;

        Vec3 pos = position();

        Vec3 dir = getDeltaMovement().lengthSqr() < 1E-5 ? new Vec3(0, 1, 0) : getDeltaMovement().normalize();

    /*
        Основная оболочка
     */
        for (int i = 0; i < 8; i++) {

            double angle = (Math.PI * 2D / 8D) * i + tickCount * 0.18;

            Vec3 side = getPerpendicular(dir, angle);

            Vec3 spawn = pos.add(side.scale(0.55));

            RotateSettings settings = RotateSettings.builder().center(pos).relativeAxis(dir).resizeSpeed(-0.01).angleSpeed(0.22).build();

            ParticleHelper.spawnDirectedParticle(level, ParticleHelper.constructRotatedSpark(settings, AIR_COLOR, 0.4f, 38, 0.94f), spawn, getDeltaMovement().scale(0.06));
        }

    /*
        Хвост
     */

        ParticleHelper.spawnParticleLine(level, ParticleHelper.constructSmoke(AIR_COLOR, 0.4f, 20), position(), prevPos, 20, 0.03);


    /*
        Переднее сжатие
     */
        if (tickCount % 2 == 0) {

            Vec3 front = pos.add(dir.scale(0.7));

            for (int i = 0; i < 4; i++) {

                double angle = (Math.PI * 2D / 4D) * i;

                Vec3 side = getPerpendicular(dir, angle);

                Vec3 spawn = front.add(side.scale(0.16));

                RotateSettings settings = RotateSettings.builder().center(front).relativeAxis(dir).resizeSpeed(0.01).angleSpeed(0.35).build();

                ParticleHelper.spawnDirectedParticle(level, ParticleHelper.constructRotatedSpark(settings, AIR_COLOR, 0.08f, 8, 1.02f), spawn, dir.scale(0.01));
            }
        }
    }

    /*
    =========================================================
                OPTIMIZED VORTEX VISUALS
    =========================================================
 */

    protected void spawnVortexParticles() {
        Vec3 pos = position();

        int layers = 1 + (int) (getRadius() / 7);

    /*
        Спиральные слои
     */
        for (int layer = 0; layer < layers; layer++) {

            double y = (0.2 + layer * 0.1) * Math.sin(tickCount / 50f * layer + Math.PI * 1.3f * layer);

            double baseRadius = 1 + layer * 0.7 - y * y * 8;

            int particles = 5 + layer;

            for (int i = 0; i < particles; i++) {

                double angle = ((Math.PI * 2D) / particles) * i + tickCount * (0.16 + layer * 0.01);

                Vec3 offset = new Vec3(Math.cos(angle) * baseRadius, y, Math.sin(angle) * baseRadius);

                Vec3 spawn = pos.add(offset);

                RotateSettings settings = RotateSettings.builder().center(pos.add(0, y, 0)).relativeAxis(new Vec3(0, 1, 0)).resizeSpeed(-0.002).angleSpeed(0.2).build();

                ParticleHelper.spawnDirectedParticle(level(), ParticleHelper.constructRotatedSpark(settings, AIR_COLOR, 0.22f + layer * 0.08f, 34, 0.95f), spawn, new Vec3(0, 0.01, 0));
            }
        }

    /*
        Центральный поток
     */
        if (tickCount % 2 == 0) {

            for (int i = 0; i < 7; i++) {

                Vec3 spawn = pos.add(random.nextGaussian() * 0.22, 0, random.nextGaussian() * 0.22);

                ParticleHelper.spawnDirectedParticle(level(), ParticleHelper.constructSmoke(AIR_COLOR, 0.62f, 36, 0.02f), spawn, 0, random.nextGaussian() * 0.08, 0);
            }
        }

    /*
        Нижнее кольцо давления
     */
        if (tickCount % 3 == 0) {

            for (int i = 0; i < 8; i++) {

                double angle = ((Math.PI * 2D) / 8D) * i + tickCount * 0.08;

                double radius = getRadius() * 0.9;

                Vec3 spawn = pos.add(Math.cos(angle) * radius, 0.04, Math.sin(angle) * radius);

                RotateSettings settings = RotateSettings.builder().center(pos).relativeAxis(new Vec3(0, 1, 0)).resizeSpeed(-0.008).angleSpeed(0.22).build();

                ParticleHelper.spawnDirectedParticle(level(), ParticleHelper.constructRotatedSpark(settings, AIR_COLOR, 0.12f, 16, 0.985f), spawn, Vec3.ZERO);
            }
        }

    /*
        Верхние воздушные разрывы
     */
//        if (tickCount % 4 == 0) {
//
//            for (int i = 0; i < 3; i++) {
//
//                double angle = random.nextDouble() * Math.PI * 2;
//
//                double radius = random.nextDouble();
//
//                Vec3 spawn = pos.add(Math.cos(angle) * radius, 3.6, Math.sin(angle) * radius);
//
//                ParticleHelper.spawnDirectedParticle(level(), ParticleHelper.constructSmoke(AIR_COLOR, 0.16f, 18), spawn, 0, 0.06, 0);
//            }
//        }
    }

    public float getRadius() {
        return entityData.get(RADIUS);
    }

    public void setRadius(float radius) {
        entityData.set(RADIUS, radius);
    }

    public int getMaxAge() {
        return entityData.get(MAX_AGE);
    }

    public void setMaxAge(int age) {
        entityData.set(MAX_AGE, age);
    }

    protected static Vec3 getPerpendicular(Vec3 axis, double angle) {

        axis = axis.normalize();

        Vec3 helper = Math.abs(axis.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);

        Vec3 x = axis.cross(helper).normalize();

        Vec3 z = axis.cross(x).normalize();

        return x.scale(Math.cos(angle)).add(z.scale(Math.sin(angle)));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {

        setRadius(tag.getFloat("radius"));
        setMaxAge(tag.getInt("max_age"));

        vortexStarted = tag.getBoolean("vortex_started");

        velocity = new Vec3(tag.getDouble("vx"), tag.getDouble("vy"), tag.getDouble("vz"));

        age = tag.getInt("age");

        if (tag.hasUUID("owner")) {
            entityData.set(OWNER_UUID, Optional.of(tag.getUUID("owner")));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {

        tag.putFloat("radius", getRadius());
        tag.putInt("max_age", getMaxAge());
        tag.putInt("age", age);

        tag.putBoolean("vortex_started", vortexStarted);

        tag.putDouble("vx", velocity.x);
        tag.putDouble("vy", velocity.y);
        tag.putDouble("vz", velocity.z);

        UUID owner = getOwnerUUID();

        if (owner != null) {
            tag.putUUID("owner", owner);
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

}