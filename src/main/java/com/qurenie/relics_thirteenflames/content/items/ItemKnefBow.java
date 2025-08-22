package com.qurenie.relics_thirteenflames.content.items;

import com.qurenie.relics_thirteenflames.content.entities.KnefProjCarrier;
import com.qurenie.relics_thirteenflames.content.entities.KnefProjectile;
import com.qurenie.relics_thirteenflames.content.entities.KnefProjectileSpecial;
import com.qurenie.relics_thirteenflames.content.entities.KnefStormcaller;
import com.qurenie.relics_thirteenflames.init.DamageSourceRegistry;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.init.RelicContainerRegistry;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.api.items.IColoredFoilItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.awt.*;
import java.util.List;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.PULL;
import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.SHIFTING;

public class ItemKnefBow extends RelicItem implements IColoredFoilItem {


    public ItemKnefBow(Properties properties) {
    
        super(properties);
    }

    RandomSource random = RandomSource.create();

    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("shot")
                                .maxLevel(10)
                                .stat(StatData.builder("rays")
                                        .initialValue(3, 3)
                                        .thresholdValue(3, 23)
                                        .upgradeModifier(UpgradeOperation.ADD, 2)
                                        .formatValue(x -> (int) MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(StatData.builder("dmg")
                                        .initialValue(3, 4)
                                        .thresholdValue(3, 5)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.1)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(StatData.builder("drain")
                                        .initialValue(0.25, 0.2)
                                        .thresholdValue(0.05, 0.25)
                                        .upgradeModifier(UpgradeOperation.ADD, -0.015)
                                        .formatValue(x -> MathUtils.round(x * 100, 1))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityData.builder("swim")
                                .maxLevel(5)
                                .active(CastData.builder()
                                        .container(RelicContainerRegistry.INVENTORY.get())
                                        .type(CastType.TOGGLEABLE)
                                        .build())
                                .stat(StatData.builder("speed")
                                        .initialValue(4, 6)
                                        .thresholdValue(4, 11)
                                        .upgradeModifier(UpgradeOperation.ADD, 1)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(StatData.builder("dmg")
                                        .initialValue(6, 8)
                                        .thresholdValue(6, 20)
                                        .upgradeModifier(UpgradeOperation.ADD, 2.4)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityData.builder("storm")
                                .requiredLevel(10)
                                .maxLevel(5)
                                .stat(StatData.builder("radius")
                                        .initialValue(4.0, 5.0)
                                        .thresholdValue(4, 20)
                                        .upgradeModifier(UpgradeOperation.ADD, 3.0)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(StatData.builder("dur")
                                        .initialValue(11, 16)
                                        .thresholdValue(11, 32)
                                        .upgradeModifier(UpgradeOperation.ADD, 4.0)
                                        .formatValue(x -> (int) MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(StatData.builder("dmg")
                                        .initialValue(6, 8)
                                        .thresholdValue(6, 13)
                                        .upgradeModifier(UpgradeOperation.ADD, 1)
                                        .formatValue(x -> (int) MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(StatData.builder("heal")
                                        .initialValue(2, 3)
                                        .thresholdValue(2, 10)
                                        .upgradeModifier(UpgradeOperation.ADD, 1.4)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .leveling(new LevelingData(100, 20, 100))
                .loot(LootData.builder().entry(LootEntries.AQUATIC).build())
                .build();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable TooltipContext context, List<Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("tooltip.relics_thirteenflames.knef_bow.lore").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, tooltip, isAdvanced);
    }

    @Override
    public UseAnim getUseAnimation(@NotNull ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return true;
    }

    //private boolean isSurging = false;

    @Override
    public void releaseUsing(@NotNull ItemStack pStack, @NotNull Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        if(!(pLivingEntity instanceof Player)|| (isAbilityTicking(pStack, "swim") && pLivingEntity.isInWaterOrRain())) return;

        int delta = this.getUseDuration(pStack, pLivingEntity) - pTimeCharged;
        if (hasGloves(pLivingEntity))
            delta *= 2;
        
        float baseDmg = (float) getStatValue(pStack, "shot", "dmg");
        boolean isShitting = pLivingEntity.isShiftKeyDown();
        if (!isShitting || !isAbilityUnlocked(pStack, "storm") || isAbilityOnCooldown(pStack, "storm")) {
            if (delta > 19) {
                if (!pLevel.isClientSide()) {
                    float fl = random.nextFloat();
                    pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), SoundsRegistry.KNEF_BOW_SHOT.get(), SoundSource.MASTER, 0.5f, 1.8f - fl * 0.15f);
                    pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), SoundsRegistry.KNEF_BOW_SHOT.get(), SoundSource.MASTER, 0.5f, 0.6f);
                }
                int count = (int) getStatValue(pStack, "shot", "rays");

                Vec3 pos = pLivingEntity.getEyePosition(1f).add(pLivingEntity.getLookAngle().scale(1.6))
                        .add(pLivingEntity.getLookAngle()
                                .cross( (pLivingEntity.getLookAngle().x < 0.001 && pLivingEntity.getLookAngle().z < 0.001) ?
                                        Vec3.directionFromRotation(0, pLivingEntity.getYHeadRot()).scale(pLivingEntity.getLookAngle().y > 0 ? -1 : 1).normalize() :
                                        new Vec3(0,1,0)
                                ).normalize().scale(0.2)
                        )
                        .add(0, -0.13, 0).subtract(pLivingEntity.getLookAngle().scale(1.4));
                KnefProjCarrier carrier = new KnefProjCarrier(EntityRegistry.KNEF_PROJECTILE_CARRIER, pLevel)
                        .setRays(
                                KnefProjectile.makeList(count, pLevel, pLivingEntity, pos, pLivingEntity.getLookAngle().scale(0.3), baseDmg,
                                        pStack.getEnchantmentLevel(pLevel.holderOrThrow(Enchantments.POWER)), pStack)
                        );
                carrier.setPos(pos);
                carrier.setOwner(pLivingEntity);
                carrier.setOwnerUUID(pLivingEntity.getStringUUID());
                carrier.shootFromRotation(pLivingEntity, pLivingEntity.getXRot(), pLivingEntity.getYRot(), 0.75f, 1f, 0);
                pLevel.addFreshEntity(carrier);
                for (KnefProjectile proj : carrier.rays) pLevel.addFreshEntity(proj);
            } else if (delta > 5) {
                if (!pLevel.isClientSide()) {
                    float fl = random.nextFloat();
                    pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), SoundsRegistry.KNEF_BOW_SHOT.get(), SoundSource.MASTER, 0.5f, 1.75f + fl * 0.1f);
                }
                Vec3 pos = pLivingEntity.getEyePosition(1f).add(pLivingEntity.getLookAngle().scale(1.6))
                        .add(pLivingEntity.getLookAngle()
                                .cross( (pLivingEntity.getLookAngle().x < 0.001 && pLivingEntity.getLookAngle().z < 0.001) ?
                                        Vec3.directionFromRotation(0, pLivingEntity.getYHeadRot()).scale(pLivingEntity.getLookAngle().y > 0 ? -1 : 1).normalize() :
                                        new Vec3(0,1,0)
                                ).normalize().scale(0.2)
                        )
                        .add(0, -0.13, 0).subtract(pLivingEntity.getLookAngle().scale(1.4));
                KnefProjectile proj = new KnefProjectile(EntityRegistry.KNEF_PROJECTILE, pLevel);
                proj.setPos(pos);
                proj.setOwner(pLivingEntity);
                proj.setOwnerUUID(pLivingEntity.getStringUUID());
                proj.setBaseDmg(baseDmg);
                proj.setPowerEnch(pStack.getEnchantmentLevel(pLevel.holderOrThrow(Enchantments.POWER)));
                proj.setBow(pStack);
                proj.shootFromRotation(pLivingEntity, pLivingEntity.getXRot(), pLivingEntity.getYRot(), 0.75f, 1f, 0);
                pLevel.addFreshEntity(proj);
            }
        } else if (delta > 19 && !isAbilityOnCooldown(pStack, "storm")/* && !pLevel.isClientSide()*/) {
            pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), SoundsRegistry.KNEF_BOW_SHOT.get(), SoundSource.MASTER, 0.7f, 0.6f);
            pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), SoundsRegistry.KNEF_BOW_SHOT.get(), SoundSource.MASTER, 0.6f, 0.3f);

            KnefStormcaller stormcaller = new KnefStormcaller(EntityRegistry.KNEF_STORMCALLER, pLevel);
            Vec3 pos = pLivingEntity.getEyePosition(1f).add(pLivingEntity.getLookAngle().scale(1.6))
                    .add(pLivingEntity.getLookAngle()
                            .cross( (pLivingEntity.getLookAngle().x < 0.001 && pLivingEntity.getLookAngle().z < 0.001) ?
                                    Vec3.directionFromRotation(0, pLivingEntity.getYHeadRot()).scale(pLivingEntity.getLookAngle().y > 0 ? -1 : 1).normalize() :
                                    new Vec3(0,1,0)
                            ).normalize().scale(0.2)
                    )
                    .add(0, -0.13, 0).subtract(pLivingEntity.getLookAngle().scale(1.4));
            stormcaller.setPos(pos);
            stormcaller.setOwner(pLivingEntity);
            stormcaller.setOwnerUUID(pLivingEntity.getStringUUID());
            stormcaller.shotPos = pos;
            stormcaller.prevPos = pos;
            stormcaller.setBow(pStack);
            stormcaller.setRays(
                    KnefProjectileSpecial.makeList(6, pLevel, pLivingEntity, pos, pLivingEntity.getLookAngle().scale(0.3))
            );
            stormcaller.shootFromRotation(pLivingEntity, pLivingEntity.getXRot(), pLivingEntity.getYRot(), 0.75f, 2.5f, 0);
            for (KnefProjectileSpecial proj : stormcaller.rays) pLevel.addFreshEntity(proj);
            pLevel.addFreshEntity(stormcaller);
            addAbilityCooldown(pStack, "storm", 600);
        } else if (delta > 5) {
            if (!pLevel.isClientSide()) {
                float fl = random.nextFloat();
                pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), SoundsRegistry.KNEF_BOW_SHOT.get(), SoundSource.MASTER, 0.5f, 1.75f + fl * 0.1f);
            }
            Vec3 pos = pLivingEntity.getEyePosition(1f).add(pLivingEntity.getLookAngle().scale(1.6))
                    .add(pLivingEntity.getLookAngle()
                            .cross( (pLivingEntity.getLookAngle().x < 0.001 && pLivingEntity.getLookAngle().z < 0.001) ?
                                    Vec3.directionFromRotation(0, pLivingEntity.getYHeadRot()).scale(pLivingEntity.getLookAngle().y > 0 ? -1 : 1).normalize() :
                                    new Vec3(0,1,0)
                            ).normalize().scale(0.2)
                    )
                    .add(0, -0.13, 0).subtract(pLivingEntity.getLookAngle().scale(1.4));
            KnefProjectile proj = new KnefProjectile(EntityRegistry.KNEF_PROJECTILE, pLevel);
            proj.setPos(pos);
            proj.setOwner(pLivingEntity);
            proj.setOwnerUUID(pLivingEntity.getStringUUID());
            proj.setBaseDmg(baseDmg);
            
            proj.setPowerEnch(pStack.getEnchantmentLevel(pLevel.holderOrThrow(Enchantments.POWER)));
            proj.setBow(pStack);
            proj.setFree(false);
            proj.shootFromRotation(pLivingEntity, pLivingEntity.getXRot(), pLivingEntity.getYRot(), 0.75f, 1f, 0);
            pLevel.addFreshEntity(proj);
        }
        pStack.set(SHIFTING, false);
    }
    
    private static boolean hasGloves(Entity entity) {
        return entity instanceof LivingEntity living && CuriosApi.getCuriosInventory(living).map(handler -> {
            var stacks = handler.getCurios().get("hands").getStacks();
            return stacks.getStackInSlot(0).is(ItemsRegistry.MONTU_GLOVES) || stacks.getStackInSlot(0).is(ItemsRegistry.MONTU_GLOVES);
        }).orElse(false);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        boolean hasGloves = hasGloves(entity);
        if(!level.isClientSide() && entity instanceof LivingEntity l
        && stack.getOrDefault(PULL, 0f) != (l.getUseItem() == stack ? (float) (stack.getUseDuration(l) - l.getUseItemRemainingTicks()) / (hasGloves ? 10.0f : 20.0F) : 0))
            stack.set( PULL, l.getUseItem() == stack ? (float) (stack.getUseDuration(l) - l.getUseItemRemainingTicks()) / (hasGloves ? 10.0f : 20.0F) : 0);
        super.inventoryTick(stack, level, entity, slot, isSelected);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack pStack, @NotNull LivingEntity entity) {
        return 72000;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }


    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int count) {
        if ( isAbilityTicking(stack, "swim") && living.isInWaterOrRain() && living instanceof Player p) {

            if (!p.isCreative()) {
                if (living.getHealth() > 1)
                    living.hurt(DamageSourceRegistry.SUCC, living.getMaxHealth() * (float) getStatValue(stack, "shot", "drain") * 0.02f);
                else living.kill();
            }
            living.hurtTime = 0;
            living.hurtDuration = 0;


            living.setSwimming(false);
            Vec3 luk = p.getLookAngle();
            Vec3 motion = living.getDeltaMovement();
            double spid = getStatValue(stack, "swim", "speed") / 5;

            AABB aoe = living.getBoundingBox().inflate(2);
            for (LivingEntity target : living.level().getEntitiesOfClass(LivingEntity.class, aoe, e -> !e.getUUID().equals(living.getUUID()))) {
                target.hurt(p.damageSources().playerAttack(p), (float) getStatValue(stack, "swim", "dmg"));
                Vec3 awayctor = target.position().subtract(living.position()).subtract(motion);
                target.push(awayctor.x() * 1 / awayctor.length(), awayctor.y() * 1 / awayctor.length(), awayctor.z() * 1 / awayctor.length());
            }

            p.setDeltaMovement(0,0,0);
            p.push(luk.x() * spid, luk.y() * spid, luk.z() * spid);
            p.startAutoSpinAttack(2, (float) getStatValue(stack, "swim", "dmg") ,stack);
            p.fallDistance = 0;
            for (int i = 0; i < 12; i++) {

                double a = 360.0 / 12 * i - count * 10.0;
                double radius = 0.7 + Math.sin(Math.toRadians(count * 20.0) - 90) * 0.44;

                if (i % 2 == 0) {
                    radius += 1.4;
                }

                Vec3 x = !( motion.normalize().x < 0.001 && motion.normalize().z < 0.001 ) ? motion.normalize().cross(new Vec3(0, 1, 0)).normalize().scale(radius) : motion.normalize().cross(new Vec3(1, 0, 0)).normalize().scale(radius);
                Vec3 z = motion.normalize().cross(x).normalize().scale(radius);

                Vec3 pos = living.getPosition(1F)
                        .add(x.scale(Math.cos(Math.toRadians(a))))
                        .add(z.scale(Math.sin(Math.toRadians(a))));

                if (i % 2 == 0) {
                    pos = pos.add(luk.scale(3.4));
                    if (i % 4 == 0) pos = pos.subtract(luk.scale(0.8));
                }
                pos = pos.add(luk.scale(-0.4));
                ParticleHelper.spawnDirectedParticle(living.level(), ParticleHelper.constructSimpleSpark(new Color(0, (int) (174 + Math.sin(count / 6.0) * 30), (int) (105 - Math.sin(count / 6.0) * 20)), 0.35f, 60, 0.92f),
                        pos.x(), pos.y(), pos.z(), 0, 0, 0);
            }
        } else if(living instanceof Player p){
            boolean hasGloves = hasGloves(living);
            if (this.getUseDuration(stack, p) - count < (hasGloves ? 19 : 9)) {
                if (!p.isCreative()) {
                    if (living.getHealth() > 1) {
                        boolean isShitting = stack.getOrDefault(SHIFTING, false);
                        living.hurt(DamageSourceRegistry.SUCC, living.getMaxHealth() * (float) getStatValue(stack, "shot", "drain") * (isShitting ? 0.1f : 0.05f) * (hasGloves ? 1.5f : 1));
                    }
                    else living.kill();
                }
                living.hurtTime = 0;
                living.hurtDuration = 0;
            }

        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        itemstack.set(SHIFTING, pPlayer.isShiftKeyDown());
        pPlayer.startUsingItem(pHand);
        return InteractionResultHolder.consume(itemstack);
    }
    
    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return enchantment.is(Enchantments.POWER);
    }
    
    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return isPrimaryItemFor(stack, enchantment);
    }
    
    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 20;
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return true;
    }

    @Override
    public int getFoilColor(@NotNull ItemStack stack) {
        return /*0xFA9FEB7D*/ new Color(0, 133, 108).getRGB(); //хекс коды люблю невероятно
    }
    
    @EventBusSubscriber
    public static class EventHandler {
        
        @SubscribeEvent
        public static void onItemUseEvent(LivingEntityUseItemEvent.Tick event) {
            if (event.getItem().is(ItemsRegistry.KNEF_BOW)
                    && ItemsRegistry.KNEF_BOW.isAbilityTicking(event.getItem(), "swim")
                    && event.getEntity().isInWaterOrRain() &&
                    event.getEntity() instanceof Player) {
                event.setDuration(event.getItem().getUseDuration(event.getEntity()));
            }
        }
        
    }

}
