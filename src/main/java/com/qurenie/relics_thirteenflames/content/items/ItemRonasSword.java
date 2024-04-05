package com.qurenie.relics_thirteenflames.content.items;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.qurenie.relics_thirteenflames.client.render.item.EmissiveItemRenderer;
import com.qurenie.relics_thirteenflames.content.effects.PoisonEffectInstance;
import com.qurenie.relics_thirteenflames.content.entities.FartCloudEntity;
import com.qurenie.relics_thirteenflames.content.entities.PoisonWaveProjectile;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.net.RhonasSweepPacket;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
            float atkspd = (float) getAbilityValue(stack, "anemia", "atkspd");
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
            .abilities(AbilitiesData.builder()
                    .ability(AbilityData.builder("spit")
                            .maxLevel(5)
                            .stat(StatData.builder("range")
                                    .initialValue(4, 4.2)
                                    .thresholdValue(4, 8)
                                    .upgradeModifier(UpgradeOperation.ADD, 0.76)
                                    .formatValue(x -> MathUtils.round(x, 2))
                                    .build()
                            )
                            .stat(StatData.builder("poisondur")
                                    .initialValue(2.0, 2.5)
                                    .thresholdValue(2.0, 4.5)
                                    .upgradeModifier(UpgradeOperation.ADD, 0.4)
                                    .formatValue(x -> MathUtils.round(x, 1))
                                    .build()
                            )
                            .stat(StatData.builder("maxstacks")
                                    .initialValue(1.0, 1.0)
                                    .thresholdValue(1.0, 6.0)
                                    .upgradeModifier(UpgradeOperation.ADD, 1)
                                    .formatValue(x -> (int) MathUtils.round(x, 1))
                                    .build()
                            )
                            .build()
                    )
                    .ability(AbilityData.builder("fart")
                            .maxLevel(3)
                            .stat(StatData.builder("radius")
                                    .initialValue(2.0, 3.5)
                                    .thresholdValue(2.0, 5.0)
                                    .upgradeModifier(UpgradeOperation.ADD, 0.5)
                                    .formatValue(x -> MathUtils.round(x, 1))
                                    .build()
                            )
                            .stat(StatData.builder("duration")
                                    .initialValue(6.0, 10.0)
                                    .thresholdValue(6.0, 20.0)
                                    .upgradeModifier(UpgradeOperation.ADD, 3.33)
                                    .formatValue(x -> MathUtils.round(x, 1))
                                    .build()
                            )
                            .stat(StatData.builder("cooldown")
                                    .initialValue(40, 30)
                                    .thresholdValue(12, 40)
                                    .upgradeModifier(UpgradeOperation.ADD, -6)
                                    .formatValue(x -> MathUtils.round(x, 1))
                                    .build()
                            )
                            .build()
                    )
                    .ability(AbilityData.builder("anemia")
                            .maxLevel(2)
                            .stat(StatData.builder("amp")
                                    .initialValue(2, 2)
                                    .thresholdValue(0, 2)
                                    .upgradeModifier(UpgradeOperation.ADD, -1.0)
                                    .formatValue(x -> (int) MathUtils.round((1 - 0.8f / (x + 1)) * 100, 0))
                                    .build()
                            )
                            .stat(StatData.builder("atkspd")
                                    .initialValue(0, 0.2)
                                    .thresholdValue(0, 1.4)
                                    .upgradeModifier(UpgradeOperation.ADD, 0.6)
                                    .formatValue(x -> MathUtils.round(4 - 2.6 + x, 2))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .leveling(new LevelingData(100, 10, 100))
            .style(StyleData.builder().borders(0xfffd75, 0xffbe00).build())
            .build();

    @Override
    public RelicData constructDefaultRelicData() {
        return null;
    }

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
        if (pPlayer.isCrouching()) {
            pLevel.playSound(null, pPlayer, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.MASTER, 1.2f, 0.1f);
            pLevel.playSound(null, pPlayer, SoundEvents.SCULK_BLOCK_BREAK, SoundSource.MASTER, 1f, 1f);
            pLevel.playSound(null, pPlayer, SoundEvents.AZALEA_FALL, SoundSource.MASTER, 1f, 0.01f);
            ItemStack sword = pPlayer.getItemInHand(pUsedHand);
            int lifetime = (int) getAbilityValue(sword, "fart", "duration") * 20;
            float radius = (float) getAbilityValue(sword, "fart", "radius");
            FartCloudEntity cloud = new FartCloudEntity(EntityRegistry.FARTCLOUD, pLevel);
            cloud.setRadius(radius);
            cloud.setLifeTime(lifetime);
            cloud.setMaxAmp((int)Math.round(getAbilityValue(sword, "spit", "maxstacks") - 1));
            cloud.setDuration((int) Math.round(getAbilityValue(sword, "spit", "poisondur") * 20));
            cloud.setOwner(pPlayer);
            cloud.setSword(pPlayer.getItemInHand(pUsedHand));
            Vec3 pos = pPlayer.getEyePosition(1).add(
                    pPlayer.getLookAngle().scale(radius + 1)
            );
            cloud.setPos(pos);
            pLevel.addFreshEntity(cloud);
            int cooldown = (int) Math.round(getAbilityValue(pPlayer.getItemInHand(pUsedHand), "fart", "cooldown") * 20);
            pPlayer.getCooldowns().addCooldown(this, cooldown);

        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemRonasSword && !event.getEntity().level().isClientSide()) {
            if(event.getEntity().getAttackStrengthScale(0.5F) > 0.9F) {
                poisonSwipe(event.getEntity(), event.getEntity().getItemInHand(InteractionHand.MAIN_HAND));
            } else{
                event.getEntity().level().playSound(null, event.getEntity(), SoundEvents.AZALEA_FALL, SoundSource.MASTER, 1f, 0.8f);
                event.getEntity().level().playSound(null, event.getEntity(), SoundEvents.SCULK_BLOCK_BREAK, SoundSource.MASTER, 0.7f, 0.7f);
                event.getEntity().level().playSound(null, event.getEntity(), SoundEvents.BLAZE_BURN, SoundSource.MASTER, 0.8f, 2.4f);
                event.getEntity().addEffect(new PoisonEffectInstance(EffectsRegistry.POISSON, 100, 0, false, true, true, event.getEntity().getItemInHand(InteractionHand.MAIN_HAND)));
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

            if ( !level.isClientSide() && (!player.hasEffect(EffectsRegistry.ANEMIA)
                    || (player.hasEffect(EffectsRegistry.ANEMIA) && player.getEffect(EffectsRegistry.ANEMIA).getDuration() < 21))) {
                int amp = (int) getAbilityValue(stack, "anemia", "amp");
                player.addEffect(new MobEffectInstance(EffectsRegistry.ANEMIA, 39, amp, true, false, true));
            }
        }
    }

    public static void poisonSwipe(LivingEntity p, ItemStack sword) {

        if(sword.getItem() instanceof ItemRonasSword relic) {
            p.level().playSound(null, p, SoundEvents.AZALEA_FALL, SoundSource.MASTER, 1f, 0.02f);
            p.level().playSound(null, p, SoundEvents.SCULK_BLOCK_BREAK, SoundSource.MASTER, 1f, 1f);
            p.level().playSound(null, p, SoundEvents.AZALEA_LEAVES_FALL, SoundSource.MASTER, 1f, 1.8f);

            double range = relic.getAbilityValue(sword, "spit", "range");

            Vec3 startVec = p.getEyePosition(1F)
                    .add(0, -0.2, 0);


            if (p.level() instanceof ServerLevel level) {

                PoisonWaveProjectile wave = new PoisonWaveProjectile(EntityRegistry.POISONWAVE, level);
                wave.setPos(startVec);
                wave.startVec = startVec;
                wave.flatluk = Vec3.directionFromRotation(0, p.getYHeadRot());
                wave.spreadAngle = 20 + (range * 1.2);
                wave.setDeltaMovement(p.getLookAngle().scale(relic.getAbilityValue(sword, "spit", "range") * 0.1 + 0.06));
                wave.setMaxRange((float) range);
                wave.setOwner(p);
                wave.setSword(sword);
                level.addFreshEntity(wave);

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

        if(event.getEntity().level().isClientSide()) return;
        int stacks = event.getEntity().hasEffect(EffectsRegistry.POISSON) ? event.getEntity().getEffect(EffectsRegistry.POISSON).getAmplifier() + 1 : 0;


        if(event.getEntity().getEffect(EffectsRegistry.POISSON) instanceof PoisonEffectInstance pei && pei.getOriginSword().getItem() instanceof ItemRonasSword relic){

            for (int i = 0; i < stacks; i++) {
                relic.addExperience(pei.getOriginSword(), rng.nextInt(3) + 1);
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
