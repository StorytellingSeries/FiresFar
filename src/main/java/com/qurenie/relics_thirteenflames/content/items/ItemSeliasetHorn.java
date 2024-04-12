package com.qurenie.relics_thirteenflames.content.items;

import com.google.common.base.Suppliers;
import com.qurenie.relics_thirteenflames.client.particles.CircleTintData;
import com.qurenie.relics_thirteenflames.client.render.item.EmissiveItemRenderer;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.init.EffectRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastSource;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.Scheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.api.items.IColoredFoilItem;
import org.zeith.hammerlib.net.Network;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemSeliasetHorn extends RelicItem implements IColoredFoilItem {
    public ItemSeliasetHorn(Properties props){
        super(props);
    }

    Random rng = new Random();

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<net.minecraft.network.chat.Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("tooltip.relics_thirteenflames.seliaset_horn.lore").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, level, tooltip, isAdvanced);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!pLevel.isClientSide){
            ItemStack horn = pPlayer.getItemInHand(pUsedHand);
            pPlayer.startUsingItem(pUsedHand);
            return InteractionResultHolder.success(horn);
        }
        return super.use(pLevel,pPlayer,pUsedHand);
    }

    public static SimpleSoundInstance ssi = SimpleSoundInstance.forUI(SoundsRegistry.SELI_HORN_BLOW.get(), 1, 1);
    public static SimpleSoundInstance ssiStop = SimpleSoundInstance.forUI(SoundsRegistry.SELI_HORN_BLOW_END.get(), 1f, 0.8f);
    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        super.releaseUsing(pStack, pLevel, pLivingEntity, pTimeCharged);

        if(!pLevel.isClientSide()) {
            Minecraft.getInstance().getSoundManager().play(ssiStop);
            Scheduler.schedule(6, () -> Minecraft.getInstance().getSoundManager().stop(ssi));
        }

    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack horn,  int count) {
        if (living instanceof Player player){
            this.releaseRay(level, player, horn);
        }


        if(!Minecraft.getInstance().getSoundManager().isActive(ssi) && !level.isClientSide()) Minecraft.getInstance().getSoundManager().play(ssi);


        if(level.isClientSide()) {
            int tick = this.getUseDuration(horn) - count;
            int segments = (int) Math.round(this.getAbilityValue(horn, "air_ray", "distance"));

            if (living.isShiftKeyDown()) {
                for (int i = 1; i < segments * 2; i++) {
                    if (i < tick) {
                        double a = 360.0 * rng.nextFloat();
                        double radius = 0.2 + (i / 6.0);
                        Vec3 luk = living.getLookAngle();
                        Vec3 iStep = luk.scale(i);
                        Vec3 iniPos = living.getEyePosition(1).add(0, -0.25, 0);
                        Vec3 x = !(luk.normalize().x < 0.001 && luk.normalize().z < 0.001) ? luk.normalize().cross(new Vec3(0, 1, 0)).normalize().scale(radius) : luk.normalize().cross(new Vec3(1, 0, 0)).normalize().scale(radius);
                        Vec3 z = luk.normalize().cross(x).normalize().scale(radius);

                        Vec3 pos = iniPos
                                .add(x.scale(Math.cos(Math.toRadians(a))).scale(rng.nextFloat()))
                                .add(z.scale(Math.sin(Math.toRadians(a))).scale(rng.nextFloat()))
                                .add(iStep);
                        Vec3 move = iniPos.subtract(pos).normalize().scale(0.5 + (double) i / segments * rng.nextFloat());

                        if(rng.nextFloat() < 0.8f) {
                            ParticleHelper.spawnDirectedParticle(level,
                                    new CircleTintData(new Color(37, 36, 30), 0.08f + (float) i / segments * 0.04f, 0, (int) Math.round(iniPos.subtract(pos).length() / move.length()), -1, false)
                                    , pos.x(), pos.y(), pos.z(), move.x, move.y, move.z);
                        } else {
                            ParticleHelper.spawnEnginedParticle(level,
                                    ParticleTypes.CLOUD, pos, move.x, move.y, move.z, 0.2f, (int) Math.round(iniPos.subtract(pos).length()  / move.length()), Color.WHITE, 0.5f);
                        }
                    }
                }
            } else {
                for (int i = 0; i < 5; i++) {

                    double a = 360.0 / 5 * i + count * 7;
                    double radius = 0.1 * rng.nextFloat();
                    Vec3 luk = living.getLookAngle();
                    Vec3 iniPos = living.getEyePosition(1).add(0, -0.25, 0);
                    Vec3 x = !(luk.normalize().x < 0.001 && luk.normalize().z < 0.001) ? luk.normalize().cross(new Vec3(0, 1, 0)).normalize().scale(radius) : luk.normalize().cross(new Vec3(1, 0, 0)).normalize().scale(radius);
                    Vec3 z = luk.normalize().cross(x).normalize().scale(radius);

                    Vec3 pos = iniPos
                            .add(x.scale(Math.cos(Math.toRadians(a))).scale(rng.nextFloat()))
                            .add(z.scale(Math.sin(Math.toRadians(a))).scale(rng.nextFloat()));

                    Vec3 move = luk.scale(0.5).add(pos.subtract(iniPos).normalize().scale(0.05 * rng.nextFloat()));
                    ParticleHelper.spawnDirectedParticle(level, new CircleTintData(new Color(37, 36, 30), 0.06f, 0, 40 + rng.nextInt(40), -1, false
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
                        Vec3 iniPos = living.getEyePosition(1).add(0, -0.25, 0);
                        Vec3 x = !(luk.normalize().x < 0.001 && luk.normalize().z < 0.001) ? luk.normalize().cross(new Vec3(0, 1, 0)).normalize().scale(radius) : luk.normalize().cross(new Vec3(1, 0, 0)).normalize().scale(radius);
                        Vec3 z = luk.normalize().cross(x).normalize().scale(radius);

                        Vec3 pos = iniPos
                                .add(x.scale(Math.cos(Math.toRadians(a))).scale(rng.nextFloat()))
                                .add(z.scale(Math.sin(Math.toRadians(a))).scale(rng.nextFloat()));

                        Vec3 move = luk.scale(0.5 + (double) i / segments).add(pos.subtract(iniPos).normalize().scale(0.1 * rng.nextFloat()));
                        pos = pos.add(iStep);
                        if(rng.nextFloat() < 0.8f) {
                            ParticleHelper.spawnDirectedParticle(level,
                                    new CircleTintData(new Color(37, 36, 30), 0.08f + (float) i / segments * 0.08f, 2, (int) Math.round((segments - pos.subtract(iniPos).length()) / move.length()), -1, false)
                                    , pos.x(), pos.y(), pos.z(), move.x, move.y, move.z);
                        } else {
                            ParticleHelper.spawnEnginedParticle(level,
                                    ParticleTypes.CLOUD, pos, move.x, move.y, move.z, 0.2f, 40, Color.WHITE, 0.5f);
                        }
                    }
                }
            }
        }
    }

    public void releaseRay(Level level, Player player, ItemStack horn){
        Vec3 initPos = player.position().add(0,player.getEyeHeight(),0);
        double distance = this.getAbilityValue(horn, "air_ray", "distance");


        Vec3 endPos = initPos.add(player.getLookAngle().scale(distance / 2.0));
        if (!level.isClientSide) {
            List<LivingEntity> entitiesToAffect = getAffectedEntities(level, initPos, endPos, distance, distance / 6.0);


            for (LivingEntity e : entitiesToAffect) {
                Vec3 entityPos = e.position().add(0, e.getEyeHeight(), 0);
                Vec3 b = entityPos.subtract(initPos).add(player.getLookAngle());
                double efficiency = this.getAbilityValue(horn, "air_ray", "efficiency") / 20;
                Vec3 speed = b.normalize().multiply(efficiency, efficiency, efficiency);
                if (player.isShiftKeyDown()) {
                    speed = speed.reverse();
                }

                e.setDeltaMovement(e.getDeltaMovement().add(speed));
            }
        }
    }

    List<LivingEntity> getAffectedEntities(Level level, Vec3 initPos, Vec3 endPos, double boxRadius, double dist){
        Vec3 axis = endPos.subtract(initPos);
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(endPos, endPos).inflate(boxRadius), e -> {
            Vec3 ePos = e.getBoundingBox().getCenter();
            Vec3 eVec = ePos.subtract(initPos);
            double axisScalar = axis.x * axis.x + axis.y * axis.y + axis.z * axis.z;
            double eScalar = eVec.x * axis.x + eVec.y * axis.y + eVec.z * axis.z;
            Vec3 point = initPos.add(axis.scale( eScalar / axisScalar ));
            return point.subtract(ePos).lengthSqr() < dist * dist;
        });
    }


    @Override
    public int getUseDuration(ItemStack pStack) {
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
                                        .thresholdValue(8, 24)
                                        .initialValue(10, 14)
                                        .upgradeModifier(UpgradeOperation.ADD,2)
                                        .formatValue(x-> MathUtils.round(x,1))
                                        .build())
                                .stat(StatData.builder("efficiency")
                                        .thresholdValue(2, 5)
                                        .initialValue(3, 3.5)
                                        .upgradeModifier(UpgradeOperation.ADD,0.3)
                                        .formatValue(x-> MathUtils.round(x,1))
                                        .build())
                                .build())
                        .ability(AbilityData.builder("block")
                                .maxLevel(5)
                                .active(CastData.builder()
                                        .source(CastSource.INVENTORY)
                                        .type(CastType.INSTANTANEOUS)
                                        .build())
                                .stat(StatData.builder("wavesCount")
                                        .initialValue(2,2)
                                        .upgradeModifier(UpgradeOperation.ADD,1)
                                        .thresholdValue(2,7)
                                        .formatValue(x-> (int)Math.round(x))
                                        .build())
                                .stat(StatData.builder("cooldown")
                                        .initialValue(60, 40)
                                        .thresholdValue(10,60)
                                        .upgradeModifier(UpgradeOperation.ADD,-6)
                                        .formatValue(x-> (int)Math.round(x))
                                        .build())
                                .stat(StatData.builder("stunDuration")
                                        .initialValue(0.5,1.5)
                                        .thresholdValue(0.5,4)
                                        .upgradeModifier(UpgradeOperation.ADD,0.5)
                                        .formatValue(x-> MathUtils.round(x,1))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingData(100, 10, 100))

                .build();
    }

    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        if (ability.equals("block")) {
            int duration = (int) (this.getAbilityValue(stack, "block", "wavesCount") * 60);
            stack.getOrCreateTag().putInt("activetick", duration);
            this.addAbilityCooldown(stack, "block", (int) (this.getAbilityValue(stack, "block", "cooldown") * 20));
        }
        super.castActiveAbility(stack, player, ability, type, stage);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {

        int activeTicker = stack.getOrCreateTag().getInt("activetick");
        if (activeTicker > 0){
            if (activeTicker-- % 60 == 0) {
                this.releaseWave(level, entity);
            }else{
                int tick = 60 - activeTicker % 60;
                this.tickWave(entity, stack, tick);

            }
        }
        stack.getOrCreateTag().putInt("activetick", activeTicker);
        super.inventoryTick(stack, level, entity, slot, isSelected);
    }

    public void tickWave(Entity entity, ItemStack stack, int tick){
        if (tick <= 30) {
            double radius = tick / 30f * 10;
            List<LivingEntity> toThrowOut = entity.level().getEntitiesOfClass(LivingEntity.class, new AABB(
                    -radius, -radius, -radius, radius, radius, radius
            ).move(entity.position()), e -> e.distanceTo(entity) <= radius);
            for (LivingEntity le : toThrowOut) {
                if (Objects.equals(le.getUUID(), entity.getUUID())) continue;
                Vec3 b = le.position().subtract(entity.position());
                Vec3 sp = b.normalize().multiply(2,2,2).add(0,0.5,0);
                le.setDeltaMovement(sp);
                le.addEffect(new MobEffectInstance(EffectRegistry.STUN.get(),(int)Math.round(this.getAbilityValue(stack,"block","stunDuration")*20),0));
            }
        }
    }

    public void releaseWave(Level level, Entity entity){
        if(!level.isClientSide()) Minecraft.getInstance().getSoundManager().playDelayed(new SimpleSoundInstance(SoundsRegistry.SELI_HORN_WAVE.get(), SoundSource.MASTER, 1, 1, entity.level().getRandom(), entity.blockPosition()), 2);
        releaseWaveParticles(level, entity);
    }

    public void releaseWaveParticles(Level level, Entity entity){
        double angle = Math.PI * 2 / 72;
        for (float g = -0.5f; g <= 0.5;g += 0.25f){
            for (int i = 0; i <= 72; i++){
                double vangle = angle*i + (g * angle);
                double x = Math.sin(vangle);
                double y = Math.cos(vangle);


                float md = (1 - 2.5f / Math.abs(g)) * 0.9f;
                Vec3 dir = new Vec3(x*md,g,y*md);
                Vec3 ppos = entity.position().add(dir.multiply(0.3,0.3,0.3)).add(0,0.2,0);

                Vec3 speed = dir.normalize().multiply(0.5,0.5,0.5);
                for (int k = 0; k <= 3;k++){
                    Vec3 ppos1 = ppos .add(
                            rng.nextFloat() * 0.5 - 0.25,
                            rng.nextFloat() * 0.5 - 0.25,
                            rng.nextFloat() * 0.5 - 0.25
                    );
                    ParticleHelper.spawnDirectedParticle(level, ParticleTypes.CLOUD, ppos1.x, ppos1.y, ppos1.z, speed.x, speed.y, speed.z);
                }

            }
        }
        Vec3 ePos = entity.position();
        for(int i = 0; i < 14; i++){
            double r = 0.512 * i + 0.3;
            int count = (int) Math.round(2 * Math.PI * r * 10);
            double r2 = r + 0.256;
            int count2 = (int) Math.round(2 * Math.PI * r2 * 10);
            int finalI = i;
            Scheduler.schedule(i , () -> {
                for(int j = 0; j < count; j++){
//                    level.addParticle(new CircleTintWithTrailData(new Color(255, 255, 255), 0.1f,0.2f,40,-1,false,true),
//                            pos.x(), pos.y(), pos.z(), sped.x(), 0, sped.z());
                    Vec3 pos = ePos.add(new Vec3(r, 0, 0).yRot((float) Math.toRadians(360.0 / count * j)));
                    Vec3 sped = pos.subtract(ePos.add(new Vec3(0, 0.3, 0))).normalize().scale(0.11);
                    ParticleHelper.spawnDirectedParticle(level, new CircleTintData(new Color(42, 41, 26), 0.2f, 0, (int) Math.round(10 + r * 3), -1, false),
                            pos.x, pos.y + 0.3, pos.z, sped.x, 0, sped.z);
                    if(finalI == 13) ParticleHelper.spawnDirectedParticle(level, ParticleTypes.CLOUD,
                            pos.x, pos.y, pos.z, sped.scale(2).x, 0, sped.scale(2).z);

                }

                for (int j = 0; j < count2; j++) {
                    Vec3 pos = ePos.add(new Vec3(r2, 0, 0).yRot((float) Math.toRadians(360.0 / count2 * j)));
                    Vec3 sped = pos.subtract(ePos.add(new Vec3(0, 0.3, 0))).normalize().scale(0.11);
                    ParticleHelper.spawnDirectedParticle(level, new CircleTintData(new Color(42, 41, 26), 0.2f, 0, (int) Math.round(10 + r2 * 3), -1, false),
                            pos.x, pos.y + 0.3, pos.z, sped.x, 0, sped.z);

                }
            });

        }

    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            final Supplier<EmissiveItemRenderer> renderer = Suppliers.memoize(EmissiveItemRenderer::new);

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer.get();
            }
        });
    }

    @Override
    public int getFoilColor(@NotNull ItemStack stack) {
        return /*0xFA9FEB7D*/ new Color(183, 155, 58).getRGB();
    }
}
