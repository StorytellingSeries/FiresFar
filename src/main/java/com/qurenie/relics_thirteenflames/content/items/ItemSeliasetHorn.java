//package com.qurenie.relics_thirteenflames.content.items;
//
//import com.qurenie.relics_thirteenflames.util.ParticleHelper;
//import it.hurts.sskirillss.relics.client.particles.circle.CircleTintData;
//import it.hurts.sskirillss.relics.client.tooltip.base.RelicStyleData;
//import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
//import it.hurts.sskirillss.relics.items.relics.base.data.base.RelicData;
//import it.hurts.sskirillss.relics.items.relics.base.data.cast.AbilityCastType;
//import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityData;
//import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityEntry;
//import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityStat;
//import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicLevelingData;
//import it.hurts.sskirillss.relics.items.relics.base.utils.AbilityUtils;
//import it.hurts.sskirillss.relics.utils.MathUtils;
//import it.hurts.sskirillss.relics.utils.Scheduler;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.particle.Particle;
//import net.minecraft.client.resources.sounds.SimpleSoundInstance;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.particles.ParticleTypes;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.InteractionResult;
//import net.minecraft.world.InteractionResultHolder;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.UseAnim;
//import net.minecraft.world.item.context.UseOnContext;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.phys.Vec3;
//import org.jetbrains.annotations.Nullable;
//
//import java.awt.*;
//import java.util.List;
//import java.util.Random;
//
//public class ItemSeliasetHorn extends RelicItem {
//    public ItemSeliasetHorn(Properties props){
//        super(props);
//    }
//
//    Random rng = new Random();
//
//    @Override
//    public InteractionResult useOn(UseOnContext ctx) {
//        ItemStack item = ctx.getItemInHand();
//        Player player = ctx.getPlayer();
//        Level level = ctx.level();
//        Vec3 loc = ctx.getClickLocation();
//        if (!level.isClientSide){
//            SeliasetHornEntity entity = SeliasetHornEntity.createHorn(level,loc,item.copy());
//            level.addFreshEntity(entity);
//            item.setCount(0);
//            if(player != null) player.setItemInHand(ctx.getHand(),ItemStack.EMPTY);
//            return InteractionResult.CONSUME;
//        }
//        return super.useOn(ctx);
//    }
//
//    @Override
//    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
//        if (!pLevel.isClientSide){
//            ItemStack horn = pPlayer.getItemInHand(pUsedHand);
//            pPlayer.startUsingItem(pUsedHand);
//            return InteractionResultHolder.success(horn);
//        }
//        return super.use(pLevel,pPlayer,pUsedHand);
//    }
//
//    public static SimpleSoundInstance ssi = SimpleSoundInstance.forUI(SoundsST.SELI_HORN_BLOW, 1, 1);
//    public static SimpleSoundInstance ssiStop = SimpleSoundInstance.forUI(SoundsST.SELI_HORN_BLOW_END, 1f, 0.8f);
//    @Override
//    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
//        super.releaseUsing(pStack, pLevel, pLivingEntity, pTimeCharged);
//
//        if(!pLevel.isClientSide()) {
//            Minecraft.getInstance().getSoundManager().play(ssiStop);
//            Scheduler.schedule(6, () -> Minecraft.getInstance().getSoundManager().stop(ssi));
//        }
//
//    }
//
//    @Override
//    public void onUsingTick(ItemStack horn, LivingEntity living, int count) {
//        if (living instanceof Player player){
//            this.releaseRay(player.level,player,horn);
//        }
//
//
//        if(!Minecraft.getInstance().getSoundManager().isActive(ssi) && !living.level.isClientSide()) Minecraft.getInstance().getSoundManager().play(ssi);
//
//
//        if(living.isCrouching()){
//            for (int i = 0; i < 15; i++){
//
//                double a = 360.0 / 15 * i - count * 7;
//                double radius = 1.25;
//                Vec3 luk = living.getLookAngle();
//                Vec3 iniPos = living.getEyePosition(1).add(luk.scale(12)).add(0, -0.25, 0);
//                Vec3 x = !luk.normalize().equals(new Vec3(0, 1, 0)) ? luk.normalize().cross(new Vec3(0,1,0)).normalize().scale(radius) : luk.normalize().cross(new Vec3(1,0,0)).normalize().scale(radius);
//                Vec3 z = luk.normalize().cross(x).normalize().scale(radius);
//
//                Vec3 pos = iniPos
//                        .add(x.scale(Math.cos(Math.toRadians(a))))
//                        .add(z.scale(Math.sin(Math.toRadians(a))))
//                        ;
//
//                Vec3 move = luk.scale(0.9).add(pos.subtract(iniPos).normalize().scale(0.09));
//                if (living.level.isClientSide)
//                    living.level.addParticle(new CircleTintData(new Color(37, 36, 30), 0.06f, 16, 1, false)
//                            , pos.x(), pos.y(), pos.z(), -move.x, -move.y, -move.z
//                    );
//                if(i % 5 == 0 && living.level.random.nextBoolean()) {
//                    pos = iniPos
//                            .add(x.scale(Math.cos(Math.toRadians(a))).scale(living.level.random.nextFloat()))
//                            .add(z.scale(Math.sin(Math.toRadians(a))).scale(living.level.random.nextFloat()))
//                    ;
//                    move = pos.subtract(living.getEyePosition(1).add(0, -0.3, 0)).normalize().scale(0.9);
//                    Particle particle = Minecraft.getInstance().particleEngine
//                            .createParticle(ParticleTypes.CLOUD,
//                                    pos.x(), pos.y(), pos.z(), -move.x, -move.y, -move.z);
//                    particle.scale(0.3f);
//                    particle.setLifetime(20);
//                }
//            }
//        }
//        else {
//            for (int i = 0; i < 15; i++){
//
//                double a = 360.0 / 15 * i + count * 7;
//                double radius = 0.1;
//                Vec3 luk = living.getLookAngle();
//                Vec3 iniPos = living.getEyePosition(1).add(0, -0.25, 0);
//                Vec3 x = !luk.normalize().equals(new Vec3(0, 1, 0)) ? luk.normalize().cross(new Vec3(0,1,0)).normalize().scale(radius) : luk.normalize().cross(new Vec3(1,0,0)).normalize().scale(radius);
//                Vec3 z = luk.normalize().cross(x).normalize().scale(radius);
//
//                Vec3 pos = iniPos
//                        .add(x.scale(Math.cos(Math.toRadians(a))))
//                        .add(z.scale(Math.sin(Math.toRadians(a))))
//                        ;
//
//                Vec3 move = luk.scale(0.5).add(pos.subtract(iniPos).normalize().scale(0.05));
//                if (living.level.isClientSide)
//                    living.level.addParticle(new CircleTintData(new Color(37, 36, 30), 0.06f, 40 + living.level.random.nextInt(40), -1, false
//                            )
//                            , pos.x(), pos.y(), pos.z(), move.x, move.y, move .z
//                    );
//                if(i % 5 == 0 && living.level.random.nextBoolean() && living.level.isClientSide) {
//
//                    Particle particle = Minecraft.getInstance().particleEngine
//                            .createParticle(ParticleTypes.CLOUD,iniPos.add(luk.scale(0.1)).x(), iniPos.add(luk.scale(0.1)).y() + 0.01, iniPos.add(luk.scale(0.1)).z(),
//                                    luk.scale(0.8).x + rng.nextFloat(-0.1f, 0.1f),
//                                    luk.scale(0.8).y + rng.nextFloat(-0.1f, 0.1f),
//                                    luk.scale(0.8).z + rng.nextFloat(-0.1f, 0.1f));
//                    particle.scale(0.3f);
//                    particle.setLifetime(20);
//                }
//
//            }
//        }
//    }
//
//    public void releaseRay(Level level, Player player, ItemStack horn){
//        Vec3 initPos = player.position().add(0,player.getEyeHeight(),0);
//        double distance = AbilityUtils.getAbilityValue(horn,"air_ray","distance");
//
//
//        Vec3 endPos = initPos.add(player.getLookAngle().multiply(distance,distance,distance));
//        if (!level.isClientSide){
//            List<Entity> entitiesToAffect = Util.getEntitiesOnWay(player,initPos,endPos,(entity)->true,distance*distance);
//
//
//            for (Entity e : entitiesToAffect){
//                Vec3 entityPos = e.position().add(0,e.getEyeHeight(),0);
//                Vec3 b = entityPos.subtract(initPos);
//                double efficiency = AbilityUtils.getAbilityValue(horn,"air_ray","efficiency") / 20;
//                Vec3 speed = b.normalize().multiply(efficiency,efficiency,efficiency);
//                if (player.isCrouching()){
//                    speed = speed.reverse();
//                }
//
//                e.setDeltaMovement(speed);
//            }
//        }
//    }
//
//
//    @Override
//    public int getUseDuration(ItemStack pStack) {
//        return 72000;
//    }
//
//    @Override
//    public UseAnim getUseAnimation(ItemStack pStack) {
//        return UseAnim.TOOT_HORN;
//    }
//
//
//    public final RelicData SELIASET_HORN_DATA = RelicData.builder()
//            .abilityData(RelicAbilityData.builder()
//                    .ability("air_ray", RelicAbilityEntry.builder()
//                            .maxLevel(10)
//                            .stat("distance", RelicAbilityStat.builder()
//                                    .thresholdValue(10,64)
//                                    .initialValue(10,20)
//                                    .upgradeModifier(RelicAbilityStat.Operation.ADD,5)
//                                    .formatValue(x-> MathUtils.round(x,1))
//                                    .build())
//                            .stat("efficiency", RelicAbilityStat.builder()
//                                    .thresholdValue(2,6)
//                                    .initialValue(2,3.5)
//                                    .upgradeModifier(RelicAbilityStat.Operation.ADD,0.25)
//                                    .formatValue(x-> MathUtils.round(x,1))
//                                    .build())
//                            .build())
//                    .ability("block", RelicAbilityEntry.builder()
//                            .stat("wavesCount", RelicAbilityStat.builder()
//                                    .initialValue(2,2)
//                                    .upgradeModifier(RelicAbilityStat.Operation.ADD,1)
//                                    .thresholdValue(2,6)
//                                    .formatValue(x-> (int)Math.round(x))
//                                    .build())
//                            .stat("cooldown", RelicAbilityStat.builder()
//                                    .initialValue(40,60)
//                                    .thresholdValue(10,60)
//                                    .upgradeModifier(RelicAbilityStat.Operation.ADD,-3)
//                                    .formatValue(x-> (int)Math.round(x))
//                                    .build())
//                            .stat("stunDuration", RelicAbilityStat.builder()
//                                    .initialValue(0.5,1.5)
//                                    .thresholdValue(0.5,4)
//                                    .upgradeModifier(RelicAbilityStat.Operation.ADD,0.25)
//                                    .formatValue(x-> MathUtils.round(x,1))
//                                    .build())
//                            .build())
//                    .build())
//            .levelingData(new RelicLevelingData(100, 10, 100))
//            .styleData(RelicStyleData.builder().borders("#ffe54f", "#ffd900").build())
//
//            .build();
//
//    @Override
//    public @Nullable RelicData getRelicData() {
//        return SELIASET_HORN_DATA;
//    }
//
//}
