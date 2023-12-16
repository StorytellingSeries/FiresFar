package com.qurenie.relics_thirteenflames.content.items;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.qurenie.relics_thirteenflames.client.render.item.EmissiveItemRenderer;
import com.qurenie.relics_thirteenflames.content.effects.PoisonEffectInstance;
import com.qurenie.relics_thirteenflames.content.entities.FartCloudEntity;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.net.RhonasSweepPacket;
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
import it.hurts.sskirillss.relics.items.relics.base.utils.LevelingUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.Scheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.api.items.IColoredFoilItem;
import org.zeith.hammerlib.net.Network;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod.EventBusSubscriber
public class ItemRonasSword extends RelicItem implements IColoredFoilItem {



    public ItemRonasSword(Properties properties) {

        super(properties);
    }

    private static Random rng = new Random();
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        if (slot == EquipmentSlot.MAINHAND) {
            float atkspd = (float)AbilityUtils.getAbilityValue(stack, "anemia", "atkspd");
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "DMG modifier", 3, AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -2.6F + atkspd, AttributeModifier.Operation.ADDITION));
        }
        return builder.build();
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
        return net.minecraftforge.common.ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction);
    }


    protected final RelicData data = RelicData.builder()
            .abilityData(RelicAbilityData.builder()
                    .ability("spit", RelicAbilityEntry.builder()
                            .maxLevel(5)
                            .stat("range", RelicAbilityStat.builder()
                                    .initialValue(4, 4.2)
                                    .thresholdValue(4, 8)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 0.76)
                                    .formatValue(x -> MathUtils.round(x, 2))
                                    .build()
                            )
                            .stat("poisondur", RelicAbilityStat.builder()
                                    .initialValue(2.0, 2.5)
                                    .thresholdValue(2.0, 4.5)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 0.4)
                                    .formatValue(x -> MathUtils.round(x, 1))
                                    .build()
                            )
                            .stat("maxstacks", RelicAbilityStat.builder()
                                    .initialValue(1.0, 1.0)
                                    .thresholdValue(1.0, 6.0)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 1)
                                    .formatValue(x -> (int) MathUtils.round(x, 1))
                                    .build()
                            )
                            .build()
                    )
                    .ability("fart", RelicAbilityEntry.builder()
                            .maxLevel(3)
                            .stat("radius", RelicAbilityStat.builder()
                                    .initialValue(2.0, 3.5)
                                    .thresholdValue(2.0, 5.0)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 0.5)
                                    .formatValue(x -> MathUtils.round(x, 1))
                                    .build()
                            )
                            .stat("duration", RelicAbilityStat.builder()
                                    .initialValue(6.0, 10.0)
                                    .thresholdValue(6.0, 20.0)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 3.33)
                                    .formatValue(x -> MathUtils.round(x, 1))
                                    .build()
                            )
                            .stat("cooldown", RelicAbilityStat.builder()
                                    .initialValue(40, 30)
                                    .thresholdValue(12, 40)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, -6)
                                    .formatValue(x -> MathUtils.round(x, 1))
                                    .build()
                            )
                            .build()
                    )
                    .ability("anemia", RelicAbilityEntry.builder()
                            .maxLevel(2)
                            .stat("amp", RelicAbilityStat.builder()
                                    .initialValue(2, 2)
                                    .thresholdValue(0, 2)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, -1.0)
                                    .formatValue(x -> (int) MathUtils.round((1 - 0.8f / (x + 1)) * 100, 0))
                                    .build()
                            )
                            .stat("atkspd", RelicAbilityStat.builder()
                                    .initialValue(0, 0.2)
                                    .thresholdValue(0, 1.4)
                                    .upgradeModifier(RelicAbilityStat.Operation.ADD, 0.6)
                                    .formatValue(x -> MathUtils.round(4 - 2.6 + x, 2))
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<net.minecraft.network.chat.Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("tooltip.relics_thirteenflames.ronas_sword.lore").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, level, tooltip, isAdvanced);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pPlayer.isCrouching() /*&& !AbilityUtils.isAbilityOnCooldown(pPlayer.getItemInHand(pUsedHand), "fart")*/) {
            pLevel.playSound(null, pPlayer, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.MASTER, 1.2f, 0.1f);
            pLevel.playSound(null, pPlayer, SoundEvents.SCULK_BLOCK_BREAK, SoundSource.MASTER, 1f, 1f);
            pLevel.playSound(null, pPlayer, SoundEvents.AZALEA_FALL, SoundSource.MASTER, 1f, 0.01f);
            ItemStack sword = pPlayer.getItemInHand(pUsedHand);
            int lifetime = (int) AbilityUtils.getAbilityValue(sword, "fart", "duration") * 20;
            float radius = (float)AbilityUtils.getAbilityValue(sword, "fart", "radius");
            FartCloudEntity cloud = new FartCloudEntity(EntityRegistry.FARTCLOUD, pLevel);
            cloud.setRadius(radius);
            cloud.setLifeTime(lifetime);
            cloud.setMaxAmp((int)Math.round(AbilityUtils.getAbilityValue(sword, "spit", "maxstacks") - 1));
            cloud.setDuration((int) Math.round(AbilityUtils.getAbilityValue(sword, "spit", "poisondur") * 20));
            cloud.setOwner(pPlayer);
            cloud.setSword(pPlayer.getItemInHand(pUsedHand));
            Vec3 pos = pPlayer.getEyePosition(1).add(
                    pPlayer.getLookAngle().scale(radius + 1)
            );
            cloud.setPos(pos);
            pLevel.addFreshEntity(cloud);
            int cooldown = (int) Math.round(AbilityUtils.getAbilityValue(pPlayer.getItemInHand(pUsedHand), "fart", "cooldown") * 20);
            pPlayer.getCooldowns().addCooldown(this, cooldown);
            //AbilityUtils.addAbilityCooldown(pPlayer.getItemInHand(pUsedHand), "fart", (int) AbilityUtils.getAbilityValue(sword, "fart", "cooldown") * 20);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemRonasSword && !event.getEntity().getLevel().isClientSide()) {
            if(event.getEntity().getAttackStrengthScale(0.5F) > 0.9F) {
                poisonSwipe(event.getEntity(), event.getEntity().getItemInHand(InteractionHand.MAIN_HAND));
            } else{
                event.getEntity().addEffect(new PoisonEffectInstance(EffectsRegistry.POISSON, 100, 0, false, true, false, event.getEntity().getItemInHand(InteractionHand.MAIN_HAND)));
            }
        }
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        return !pPlayer.isCreative();
    }


    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.category == EnchantmentCategory.WEAPON && enchantment != Enchantments.UNBREAKING;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 20;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);
        if (entity instanceof Player player && stack.is(this)) {

            //if(player instanceof LocalPlayer lp) lp.chatSigned(String.valueOf(lp.getXRot()), null);

            if ((!player.hasEffect(EffectsRegistry.ANEMIA)
                    || (player.hasEffect(EffectsRegistry.ANEMIA) && player.getEffect(EffectsRegistry.ANEMIA).getDuration() < 20))) {
                int amp = (int) AbilityUtils.getAbilityValue(stack, "anemia", "amp");
                player.addEffect(new MobEffectInstance(EffectsRegistry.ANEMIA, 39, amp, true, false, true));
            }
        }
    }

    public static void poisonSwipe(LivingEntity p, ItemStack sword) {


        p.level.playSound(null, p, SoundEvents.AZALEA_FALL, SoundSource.MASTER, 1f, 0.02f);
        p.level.playSound(null, p, SoundEvents.SCULK_BLOCK_BREAK, SoundSource.MASTER, 1f, 1f);
        p.level.playSound(null, p, SoundEvents.AZALEA_LEAVES_FALL, SoundSource.MASTER, 1f, 1.8f);
        double spreadAngle = 20 + (AbilityUtils.getAbilityValue(sword, "spit", "range") * 1.8);
        double range = AbilityUtils.getAbilityValue(sword, "spit", "range");
        int maxAmp = (int) Math.round(AbilityUtils.getAbilityValue(sword, "spit", "maxstacks") - 1);

        Vec3 startVec = p.getEyePosition(1F)
                .add(0, -0.2, 0);
        Vec3 luk = Vec3.directionFromRotation(0, p.getYHeadRot());
        Vec3 down = p.getLookAngle().subtract(luk);




        if (p.level instanceof ServerLevel level) {
            for (int i = 0; i < range * 1.8; i++) {
                int dark = RandomSource.create().nextInt(80);
                int yellowness = RandomSource.create().nextInt(80);
                int finalI = i;
                //PoisonWaveEntity wave = new PoisonWaveEntity(EntityRegistry.POISON_WAVE, p.getLevel());

                Scheduler.schedule(i, () -> {
                    for (int j = 0; j < range * 4 + 1; j++) {
                        Vec3 vec = startVec.add(luk
                                .yRot((float) Math.toRadians(-spreadAngle + j * (spreadAngle * 2 / range / 4)))
                                .add(down)
                                .normalize()
                                .scale(0.7 + finalI / 1.8/* * (range / 10.0) * 2 */)
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
                //wave.setPos(startVec);
                //p.getLevel().addFreshEntity(wave);
            }
        }

        AABB eBox = new AABB(
                startVec.add(p.getLookAngle()
                        .scale(range * 0.6)),
                startVec.add(p.getLookAngle()
                        .scale(range * 0.6))
        ).inflate(range * 0.3);
        HashSet<LivingEntity> entitySet = new HashSet<>(p.level.getEntitiesOfClass(LivingEntity.class, eBox, e -> !(e.equals(p))));
        eBox = new AABB(
                startVec.add(p.getLookAngle()
                        .scale(range * 0.2)),
                startVec.add(p.getLookAngle()
                        .scale(range * 0.2))
        ).inflate(range * 0.1);
        entitySet.addAll(p.level.getEntitiesOfClass(LivingEntity.class, eBox, e -> !(e.equals(p))));

        int duration = (int) Math.round(AbilityUtils.getAbilityValue(sword, "spit", "poisondur") * 20);
        for (LivingEntity e : entitySet) {
            e.hurt(DamageSource.mobAttack(p), 1);
            if (e.hasEffect(EffectsRegistry.POISSON)) {
                int appliedAmplifier = e.getEffect(EffectsRegistry.POISSON).getAmplifier() + 1;
                if (appliedAmplifier <= maxAmp) {
                    e.addEffect(new PoisonEffectInstance(EffectsRegistry.POISSON, duration + appliedAmplifier * 20, appliedAmplifier, false, true, false, sword));
                    if(rng.nextFloat() < 0.25f) LevelingUtils.addExperience(sword, 1);
                }
                else {
                    e.addEffect(new PoisonEffectInstance(EffectsRegistry.POISSON, duration + maxAmp * 20, maxAmp, false, true, false, sword));
                }
            } else {
                e.addEffect(new PoisonEffectInstance(EffectsRegistry.POISSON, duration, 0, false, true, false, sword));
                if(rng.nextFloat() < 0.25f) LevelingUtils.addExperience(sword, 1);
            }
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

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {

        if(event.getEntity().getLevel().isClientSide()) return;
        int stacks = event.getEntity().hasEffect(EffectsRegistry.POISSON) ? event.getEntity().getEffect(EffectsRegistry.POISSON).getAmplifier() + 1 : 0;


        if(event.getEntity().getEffect(EffectsRegistry.POISSON) instanceof PoisonEffectInstance pei && pei.getOriginSword().is(ItemsRegistry.RONAS_SWORD)){

            for (int i = 0; i < stacks; i++) {
                LevelingUtils.addExperience(pei.getOriginSword(), rng.nextInt(3) + 1);
            }
        }
    }

    @SubscribeEvent
    public static void onHitAir(PlayerInteractEvent.LeftClickEmpty event) {
        if(!event.getItemStack().is(ItemsRegistry.RONAS_SWORD)) return;

        Network.sendToServer(new RhonasSweepPacket(event.getItemStack()));
    }



    @Override
    public int getFoilColor(@NotNull ItemStack stack) {
        return /*0xFA9FEB7D*/ new Color(31, 110, 0).getRGB();
    }

}
