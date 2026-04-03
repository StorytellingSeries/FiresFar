package com.qurenie.relics_thirteenflames.content.items.feather;

import com.qurenie.api.IActivityContainer;
import com.qurenie.api.IExtRelicItem;
import com.qurenie.api.SettingsContainer;
import com.qurenie.relics_thirteenflames.activity.ActivitySetting;
import com.qurenie.relics_thirteenflames.activity.IActivitySetting;
import com.qurenie.relics_thirteenflames.content.entities.FeatherVortexEntity;
import com.qurenie.relics_thirteenflames.content.entities.RespawnBookEntity;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.style.ColorScheme;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootEntry;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerlib.api.fml.IRegisterListener;
import org.zeith.hammerlib.util.charging.ItemChargeHelper;

import java.awt.*;
import java.util.List;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.ENTITY_UUID;
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

public class ItemHettFeather extends RelicItem implements IExtRelicItem, IRegisterListener, IActivityContainer {

    public static final LootEntry STRONGHOLD = LootEntry.builder().dimension(".*").biome(".*").table("[\\w]+:chests\\/[\\w_\\/]*(stronghold)[\\w_\\/]*").weight(850).build();
    static final int BOOK_ACTIVE_MAX_LEVEL = 3;

    public ItemHettFeather(Properties properties) {
        super(properties);
    }

    @Override
    public void onPostRegistered(ResourceLocation id) {
        EVENT_BUS.register(this);
    }

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("lifegiving_knowledge")
                                .initialMaxLevel(5)
                                .stat(AbilityStatTemplate.builder("hp_value")
                                        .initialValue(5, 15)
                                        .thresholdValue(0, 65)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 10)
                                        .formatValue(d -> MathUtils.round(d / 100, 2))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityTemplate.builder("book_slap")
                                .initialMaxLevel(3)
                                .stat(AbilityStatTemplate.builder("level")
                                        .initialValue(1, 1.75)
                                        .thresholdValue(0, 3)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0.5)
                                        .formatValue(Math::floor)
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("max_health")
                                        .initialValue(1, 6)
                                        .thresholdValue(1, 15)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 3)
                                        .formatValue(d -> MathUtils.round(d, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("recharge")
                                        .initialValue(30, 30)
                                        .thresholdValue(30, 30)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0)
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityTemplate.builder("savepoint")
                                .requiredLevel(8)
                                .initialMaxLevel(3)
                                .stat(AbilityStatTemplate.builder("radius")
                                        .initialValue(12, 20)
                                        .thresholdValue(15, 60)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 15)
                                        .formatValue(d -> MathUtils.round(d, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("xp_consume")
                                        .initialValue(100, 90)
                                        .thresholdValue(30, 100)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), -20)
                                        .formatValue(d -> MathUtils.round(d, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("hp_consume")
                                        .initialValue(100, 90)
                                        .thresholdValue(50, 100)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), -15)
                                        .formatValue(d -> MathUtils.round(d, 1))
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(100)
                        .maxRank(2)
                        .build())
                .loot(LootTemplate.builder().entry(STRONGHOLD).build())
                .build();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, List<net.minecraft.network.chat.Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("tooltip.relics_thirteenflames.hett_feather.lore").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, tooltip, isAdvanced);
    }

    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getPlayer() == null || !ctx.getPlayer().isShiftKeyDown())
            return super.useOn(ctx);

        var level = ctx.getLevel();
        ItemStack feather = ctx.getItemInHand();

        var itr = ItemChargeHelper.listPlayerInventories(ctx.getPlayer()).iterator();
        while (itr.hasNext()) {
            var ih = itr.next();
            for (int j = 0; j < ih.getSlots(); j++) {
                var it = ih.getStackInSlot(j);
                if (!it.is(Items.BOOK))
                    continue;

                if (!ctx.getPlayer().isCreative())
                    it.shrink(1);

                if (level.isClientSide)
                    return InteractionResult.SUCCESS;

                if (feather.has(ENTITY_UUID)) {
                    Entity last = ((ServerLevel) level).getEntity(feather.get(ENTITY_UUID));

                    if (last instanceof RespawnBookEntity respawnBook && !respawnBook.isDeadOrDying() && respawnBook.getDeathTick() < 0)
                        respawnBook.close();
                }

                ((ItemHettFeather) feather.getItem()).addExperience(ctx.getPlayer(), feather, 10);

                var position = ctx.getClickLocation();
                RespawnBookEntity respawnBook = new RespawnBookEntity(ctx.getPlayer(), ctx.getItemInHand(), position.x, position.y, position.z);
                ctx.getLevel().addFreshEntity(respawnBook);
                ctx.getPlayer().getCooldowns().addCooldown(this, 1200);
                ctx.getItemInHand().set(ENTITY_UUID, respawnBook.getUUID());

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (canCast(player, stack, "book_slap")) {
            if (!player.level().isClientSide) {
                player.swing(player.getItemInHand(InteractionHand.MAIN_HAND) == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
                Vec3 eyePos = player.getEyePosition();
                Vec3 lookVec = player.getLookAngle();
                Vec3 reachVec = eyePos.add(lookVec.scale(8));
                AABB aabb = player.getBoundingBox().expandTowards(lookVec.scale(8)).inflate(1.0D);

                EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                        player.level(), player, eyePos, reachVec, aabb,
                        entity -> !entity.isSpectator() && entity.isPickable() && entity != player
                );

                if (hitResult == null)
                    return super.use(level, player, usedHand);

                if (!(hitResult.getEntity() instanceof LivingEntity living) || living instanceof Player)
                    return InteractionResultHolder.fail(stack);

                if (living.getHealth() > ItemsRegistry.HETT_FEATHER.getStatValue(player, stack, "book_slap", "max_health"))
                    return InteractionResultHolder.fail(stack);

                if (living instanceof RespawnBookEntity respawnBook) {
                    ParticleHelper.spawnParticleEntity(ParticleTypes.CAMPFIRE_COSY_SMOKE, respawnBook, 20, 0.05);
//                ParticleHelper.spawnParticleEntity(ParticleHelper.constructSimpleSpark(new Color(239, 215, 182), 0.2f, 60, 0.97f), e, 20, 0.05);
                    ParticleHelper.spawnParticleEntity(ParticleHelper.constructSmoke(new Color(239, 215, 182), (respawnBook.getBbHeight() + respawnBook.getBbWidth()) / 2, 60, 0).withLightning(false), respawnBook, 20, 0.03);
                    ItemEntity item = new ItemEntity(respawnBook.level(), respawnBook.getX(), respawnBook.getY(), respawnBook.getZ(), new ItemStack(Items.BOOK));
                    respawnBook.level().addFreshEntity(item);
                    respawnBook.discard();
                    return super.use(level, player, usedHand);
                }

                FeatherVortexEntity vortexEntity = new FeatherVortexEntity(living, player.level(), (int) ItemsRegistry.HETT_FEATHER.getStatValue(player, stack, "book_slap", "level"));
                player.level().addFreshEntity(vortexEntity);

                ((ItemHettFeather) stack.getItem()).addExperience(living, stack, 10);

                this.setMaxCooldown(living, stack, "book_slap");
            }

            return InteractionResultHolder.success(stack);
        }

        return super.use(level, player, usedHand);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void damageEvent(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        ExperienceOrb orb = event.getOrb();

        var itr = ItemChargeHelper.listPlayerInventories(player).iterator();
        while (itr.hasNext()) {
            var ih = itr.next();
            for (int j = 0; j < ih.getSlots(); j++) {
                var it = ih.getStackInSlot(j);
                if (!it.is(this))
                    continue;

                float delta = player.getMaxHealth() - player.getHealth();
                if (delta > 0) {
                    int perXp = (int) ((ItemHettFeather) it.getItem()).getStatValue(player, it, "lifegiving_knowledge", "hp_value");
                    player.heal((float) orb.value * perXp / 100f);

                    ((ItemHettFeather) it.getItem()).addExperience(player, it, Math.min((int) (orb.value * perXp / 100f), 1));
                }

                int level = ((ItemHettFeather) it.getItem()).getAbilityLevel(player, it, "lifegiving_knowledge");
                if (level >= 5)
                    player.getFoodData().eat(1, 0.1f);

                break;
            }
        }

    }


    @Override
    public SettingsContainer<IActivitySetting> constructActivitySettings() {
        return SettingsContainer.<IActivitySetting>builder()
                .setting(ActivitySetting.builder("book_slap")
                        .maxCooldown(600)
                        .color(ColorScheme.BAR_YELLOW)
                        .build())
                .build();
    }
}
