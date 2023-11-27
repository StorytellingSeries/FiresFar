package com.qurenie.relics_thirteenflames.content.items;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.qurenie.relics_thirteenflames.client.render.item.EmissiveItemRenderer;
import com.qurenie.relics_thirteenflames.content.entities.FartCloudEntity;
import com.qurenie.relics_thirteenflames.content.entities.PoisonWaveEntity;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import it.hurts.sskirillss.relics.client.particles.circle.CircleTintData;
import it.hurts.sskirillss.relics.client.particles.spark.SparkTintData;
import it.hurts.sskirillss.relics.client.tooltip.base.RelicStyleData;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.base.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityEntry;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityStat;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicLevelingData;
import it.hurts.sskirillss.relics.items.relics.base.utils.AbilityUtils;
import it.hurts.sskirillss.relics.items.relics.base.utils.QualityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerlib.api.items.IColoredFoilItem;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod.EventBusSubscriber
public class ItemRonasSword extends RelicItem implements IColoredFoilItem {



    public ItemRonasSword(Properties properties) {

        super(properties);
    }

//    @Override
//    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot)
//    {
//        return slot == EquipmentSlot.MAINHAND
//                ? this.defaultModifiers
//                : super.getDefaultAttributeModifiers(slot);
//    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
        return net.minecraftforge.common.ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction);
    }


    protected final RelicData data = RelicData.builder()
            .abilityData(RelicAbilityData.builder()
                    .ability("spit", RelicAbilityEntry.builder()
                            .maxLevel(5)
                            .stat("range", RelicAbilityStat.builder()
                                    .initialValue(2, 2.5)
                                    .thresholdValue(2, 4)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 0.3)
                                    .formatValue(x -> (float) MathUtils.round(x, 2))
                                    .build()
                            )
                            .stat("poisondur", RelicAbilityStat.builder()
                                    .initialValue(2.0, 5.0)
                                    .thresholdValue(2.0, 15.0)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 2.0)
                                    .formatValue(x -> (int) MathUtils.round(x, 1))
                                    .build()
                            )
                            .stat("poisonstrength", RelicAbilityStat.builder()
                                    .initialValue(1.0, 3.0)
                                    .thresholdValue(1.0, 8.0)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 1)
                                    .formatValue(x -> (int) MathUtils.round(x, 1))
                                    .build()
                            )
                            .build()
                    )
                    .ability("fart", RelicAbilityEntry.builder()
                            .maxLevel(5)
                            .stat("radius", RelicAbilityStat.builder()
                                    .initialValue(1.0, 2.0)
                                    .thresholdValue(1.0, 4.0)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 0.4)
                                    .formatValue(x -> (float) MathUtils.round(x, 2))
                                    .build()
                            )
                            .stat("duration", RelicAbilityStat.builder()
                                    .initialValue(4.0, 10.0)
                                    .thresholdValue(4.0, 25.0)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 3.0)
                                    .formatValue(x -> (int) MathUtils.round(x, 1))
                                    .build()
                            )
                            .stat("cooldown", RelicAbilityStat.builder()
                                    .initialValue(30, 40)
                                    .thresholdValue(5, 40)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, -5)
                                    .formatValue(x -> (int) MathUtils.round(x, 1))
                                    .build()
                            )
                            .build()
                    )
                    .ability("anemia", RelicAbilityEntry.builder()
                            .maxLevel(0)
                            .stat("yes", RelicAbilityStat.builder()
                                    .initialValue(0.0, 5.0)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 1.0)
                                    .formatValue(x -> (int) MathUtils.round(x, 1))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .levelingData(new RelicLevelingData(100, 10, 100))
            .styleData(RelicStyleData.builder().borders("#fffd75", "#ffbe00").build())
            .build();

    @Override
    public RelicData getRelicData() {
        return data;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pPlayer.isCrouching() && !AbilityUtils.isAbilityOnCooldown(pPlayer.getItemInHand(pUsedHand), "fart") && !pLevel.isClientSide()) {
            pLevel.playSound(null, pPlayer, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.MASTER, 1.2f, 0.1f);
            pLevel.playSound(null, pPlayer, SoundEvents.SCULK_BLOCK_BREAK, SoundSource.MASTER, 1f, 1f);
            pLevel.playSound(null, pPlayer, SoundEvents.AZALEA_FALL, SoundSource.MASTER, 1f, 0.01f);
            ItemStack sword = pPlayer.getItemInHand(pUsedHand);
            int lifetime = (int) AbilityUtils.getAbilityValue(sword, "fart", "duration") * 20;
            float radius = (float)AbilityUtils.getAbilityValue(sword, "fart", "radius");
            FartCloudEntity cloud = new FartCloudEntity(EntityRegistry.FARTCLOUD, pLevel);
            cloud.setRadius(radius);
            cloud.setLifeTime(lifetime);
            Vec3 pos = pPlayer.getEyePosition(1).add(
                    pPlayer.getLookAngle().scale(radius + 1)
            );
            cloud.setPos(pos);
            pLevel.addFreshEntity(cloud);
            AbilityUtils.addAbilityCooldown(pPlayer.getItemInHand(pUsedHand), "fart", (int) AbilityUtils.getAbilityValue(sword, "fart", "cooldown") * 20);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemRonasSword
                && event.getEntity().getAttackStrengthScale(0.5F) > 0.9F
                && !event.getEntity().getLevel().isClientSide())
            poisonSwipe(event.getEntity(), event.getEntity().getItemInHand(InteractionHand.MAIN_HAND));

    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        return !pPlayer.isCreative();
    }

//    static int tick = 0;
//    @SubscribeEvent
//    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
//        if(event.phase == TickEvent.Phase.START || event.side == LogicalSide.CLIENT) return;
//        tick++;
//        ScriptUtils.sendMessageToPlayers(String.valueOf(tick));
//        ((ServerLevel)event.player.getLevel()).sendParticles(new CircleTintData(new Color(85, 255, 0), 0.25F,
//                        40, 0.94F, false, true, new ScatterController()),
//                event.player.getX(), event.player.getY(), event.player.getZ(), 4, 0.015, 0.015, 0.015, 0.02);
//    }


    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        if (slot == EquipmentSlot.MAINHAND) {
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Worship DMG modifier", 4, AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -2.4F, AttributeModifier.Operation.ADDITION));
        }
        return builder.build();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.category == EnchantmentCategory.WEAPON;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);
        if (entity instanceof Player player && stack.is(this) && (!player.hasEffect(EffectsRegistry.ANEMIA)
                || (player.hasEffect(EffectsRegistry.ANEMIA) && player.getEffect(EffectsRegistry.ANEMIA).getDuration() < 20))
                && QualityUtils.getAbilityQuality(stack, "anemia") < 10) {
            player.addEffect(new MobEffectInstance(EffectsRegistry.ANEMIA, 39, 0, true, false, true));
        }


    }

    public static void poisonSwipe(LivingEntity p, ItemStack sword) {
        if (!(p.level instanceof ServerLevel level)) return;

        p.level.playSound(null, p, SoundEvents.AZALEA_FALL, SoundSource.MASTER, 1f, 0.02f);
        p.level.playSound(null, p, SoundEvents.SCULK_BLOCK_BREAK, SoundSource.MASTER, 1f, 1f);
        p.level.playSound(null, p, SoundEvents.AZALEA_LEAVES_FALL, SoundSource.MASTER, 1f, 1.8f);
        double spreadAngle = 20 + (AbilityUtils.getAbilityValue(sword, "spit", "range") * 4);
        double range = AbilityUtils.getAbilityValue(sword, "spit", "range");
        int maxAmp = 5 + (int) Math.round(AbilityUtils.getAbilityValue(sword, "spit", "poisonstrength") - 1);

        Vec3 startVec = p.getEyePosition(1F)
                .add(0, -0.2, 0);
        Vec3 luk = p.getLookAngle();

        AABB eBox = new AABB(
                startVec.add(luk
                        .scale(0.7 * range * 0.5)),
                startVec.add(luk
                        .scale(0.7 + range * 0.5))
        ).inflate(range * 0.5, range * 0.5, range * 0.5);

        for (int i = 0; i < range * 3; i++) {
            int dark = RandomSource.create().nextInt(80);
            int yellowness = RandomSource.create().nextInt(80);
            int finalI = i;
            PoisonWaveEntity wave = new PoisonWaveEntity(EntityRegistry.POISON_WAVE, p.getLevel());
            wave.addTask(i + 1, () -> {
                for (int j = 0; j < range * 5 + 1; j++) {
                    Vec3 vec = startVec.add(luk
                            .scale(0.7 + finalI * (range / 10.0) * 2)
                            .yRot((float) Math.toRadians(-spreadAngle + j * (spreadAngle * 2 / range / 5)))
                    );
                    level.sendParticles(new CircleTintData(new Color(85 - dark + yellowness, 255 - dark - RandomSource.create().nextInt(100), 0),
                                    (float) (0.2F + 0.025f * range), 20, 0.83F, false),
                            vec.x, vec.y, vec.z, 1, 0.018 * range, 0.018 * range, 0.018 * range, 0.005 + finalI * 0.008);
                    if (j % 3 == 0)
                        level.sendParticles(new SparkTintData(new Color(85 - RandomSource.create().nextInt(80), 255 - RandomSource.create().nextInt(100), 0),
                                        (float) (0.2F + 0.025f * range), 20),
                                vec.x, vec.y, vec.z, 1, 0.018 * range, 0.018 * range, 0.018 * range, 0.005 + finalI * 0.008);
                }
            });
            wave.setPos(startVec);
            p.getLevel().addFreshEntity(wave);
        }
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, eBox, e -> !(e.equals(p)));
        int duration = (int) Math.round(AbilityUtils.getAbilityValue(sword, "spit", "poisondur") * 20);
        int amplifier = (int) Math.round(AbilityUtils.getAbilityValue(sword, "spit", "poisonstrength") - 1);
        for (LivingEntity e : entities) {
            e.hurt(DamageSource.mobAttack(p), 1);
            if (e.hasEffect(EffectsRegistry.POISSON)) {
                if (e.getEffect(EffectsRegistry.POISSON).getAmplifier() + amplifier + 1 < maxAmp) {
                    e.addEffect(new MobEffectInstance(EffectsRegistry.POISSON, duration, e.getEffect(EffectsRegistry.POISSON).getAmplifier() + amplifier + 1, false, true, false));
                }
                if (e.getEffect(EffectsRegistry.POISSON).getAmplifier() + amplifier + 1 >= maxAmp) {
                    e.addEffect(new MobEffectInstance(EffectsRegistry.POISSON, duration, maxAmp, false, true, false));
                }
            } else e.addEffect(new MobEffectInstance(EffectsRegistry.POISSON, duration, amplifier, false, true, false));
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
        return /*0xFA9FEB7D*/ new Color(31, 110, 0).getRGB(); //хекс коды люблю невероятно
    }

}
