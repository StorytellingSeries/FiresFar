package com.qurenie.relics_thirteenflames.content.items;

import com.qurenie.api.IActivityContainer;
import com.qurenie.api.IExtRelicItem;
import com.qurenie.api.SettingsContainer;
import com.qurenie.api.event.AnvilEnchantmentMergeEvent;
import com.qurenie.api.event.AnvilRepairPostCountEvent;
import com.qurenie.api.event.AnvilUpdatePostEvent;
import com.qurenie.api.event.ItemHurtEvent;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.activity.ActivitySetting;
import com.qurenie.relics_thirteenflames.activity.IActivitySetting;
import com.qurenie.relics_thirteenflames.activity.call.settings.*;
import com.qurenie.relics_thirteenflames.content.container.MontuCompositeContainer;
import com.qurenie.relics_thirteenflames.content.container.MontuGlovesContainer;
import com.qurenie.relics_thirteenflames.content.items.base.IRenderableCurioHand;
import com.qurenie.relics_thirteenflames.content.items.models.MontuGlovesArmorLeft;
import com.qurenie.relics_thirteenflames.content.items.models.MontuGlovesArmorRight;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import it.hurts.sskirillss.relics.api.relics.RelicStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.items.misc.CreativeContentConstructor;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.WearableRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicAttributeModifier;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicSlotModifier;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;
import org.apache.logging.log4j.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.api.fml.IRegisterListener;
import org.zeith.hammerlib.util.charging.ItemChargeHelper;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioCanEquipEvent;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.List;
import java.util.stream.IntStream;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

public class ItemMontuGloves extends WearableRelicItem implements IActivityContainer, IRegisterListener, IRenderableCurioHand, IExtRelicItem {

    private static final Lazy<HumanoidModel<? extends LivingEntity>> RIGHT = Lazy.lazy(() -> new MontuGlovesArmorRight<>(Minecraft.getInstance().getEntityModels().bakeLayer(MontuGlovesArmorRight.LAYER_LOCATION)));
    private static final Lazy<HumanoidModel<? extends LivingEntity>> RIGHT_FLAWLESS = Lazy.lazy(() -> new MontuGlovesArmorRight.Flawless<>(Minecraft.getInstance().getEntityModels().bakeLayer(MontuGlovesArmorRight.Flawless.LAYER_LOCATION)));
    private static final Lazy<HumanoidModel<? extends LivingEntity>> LEFT = Lazy.lazy(() -> new MontuGlovesArmorLeft<>(Minecraft.getInstance().getEntityModels().bakeLayer(MontuGlovesArmorLeft.LAYER_LOCATION)));
    private static final Lazy<HumanoidModel<? extends LivingEntity>> LEFT_FLAWLESS = Lazy.lazy(() -> new MontuGlovesArmorLeft.Flawless<>(Minecraft.getInstance().getEntityModels().bakeLayer(MontuGlovesArmorLeft.Flawless.LAYER_LOCATION)));
    private static final ResourceLocation TEXTURE = ThirteenFlames.rl("textures/armor/montu_gloves.png");
    //TODO:
    public ItemMontuGloves(Properties properties) {
        super();
    }

    @Override
    public void gatherCreativeTabContent(CreativeContentConstructor constructor) {
    }

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("gloves_range")
                                .initialMaxLevel(5)
                                .stat(AbilityStatTemplate.builder("range")
                                        .initialValue(1, 3)
                                        .thresholdValue(1, 15)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 1)
                                        .formatValue(d -> MathUtils.round(d, 1))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("unbreaking")
                                        .initialValue(10, 25)
                                        .thresholdValue(10, 90)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 8)
                                        .formatValue(d -> MathUtils.round(d, 0))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("block_braking")
                                        .initialValue(10, 15)
                                        .thresholdValue(10, 50)
                                        .upgradeModifier(RelicsScalingModels.EXPONENTIAL.get(), 0.28)
                                        .formatValue(d -> MathUtils.round(d, 0))
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("discount")
                                        .initialValue(1, 15)
                                        .thresholdValue(1, 70)
                                        .upgradeModifier(RelicsScalingModels.EXPONENTIAL.get(), 0.28)
                                        .formatValue(d -> MathUtils.round(d, 0))
                                        .build()
                                )
                                .rankModifier(1, "discount")
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_1")
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_2")
                                        .build())
                                .build()
                        )
                        .ability(AbilityTemplate.builder("crafting")
                                .initialMaxLevel(4)
                                .stat(AbilityStatTemplate.builder("discount")
                                        .initialValue(9.99, 9.99)
                                        .thresholdValue(5, 55)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 10)
                                        .build()
                                )
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("source_1")
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 6, 12).star(1, 6, 7).star(2, 15, 12).star(3, 6, 18).star(4, 8, 16).star(5, 12, 16).star(6, 15, 18).star(7, 8, 23).star(8, 13, 23).star(9, 15, 7)
                                        .link(2, 0).link(0, 1).link(8, 7).link(7, 3).link(4, 7).link(5, 8).link(8, 6).link(1, 9).link(9, 2).link(4, 0).link(5, 2)
                                        .build())
                                .build()
                        )
                        .ability(AbilityTemplate.builder("smelting")
                                .initialMaxLevel(1)
                                .requiredPoints(5)
                                .maxLevelRankModifier(1)
                                .requiredLevel(8)
                                .stat(AbilityStatTemplate.builder("addLevel")
                                        .initialValue(0, 900)
                                        .thresholdValue(0, 10000)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 5)
                                        .formatValue(Math::round)
                                        .build()
                                )
                                .stat(AbilityStatTemplate.builder("empty").build())
                                .rankModifier(1, "upgrade")
                                .build()
                        )
                        .build()
                )
                .leveling(LevelingTemplate.builder()
                        .maxRank(1)
                        .step(50)
                        .initialCost(75)
                        .build())
                .statistic(RelicStatisticTemplate.builder().build())
                .loot(LootTemplate.builder().entry(LootEntries.MINESHAFT).build())
                .build();
    }

    @Override
    public SettingsContainer<IActivitySetting> constructActivitySettings() {
        return SettingsContainer.<IActivitySetting>builder()
                .setting(ActivitySetting.builder("smelting")
                        .maxCooldown(0)
                        .callSettings(RelicsActivityCallSettings.builder("smelting")
                                .inventoryType(InventoryType.CURIO)
                                .minVisibilityLevel(1)
                                .cast((l, s) -> {
                                    if (l.level().isClientSide)
                                        return ActivityResult.SUCCESS;

                                    if (!(l instanceof Player player))
                                        return ActivityResult.FAILURE;

                                    player.openMenu(new MontuGlovesContainer.Provider(s), buf -> ItemStack.STREAM_CODEC.encode(buf, s));
                                    player.stopUsingItem();
                                    return ActivityResult.SUCCESS;
                                })
                                .build())
                        .build())
                .setting(ActivitySetting.builder("crafting")
                        .maxCooldown(0)
                        .callSettings(RelicsActivityCallSettings.builder("crafting")
                                .inventoryType(InventoryType.CURIO)
                                .minVisibilityLevel(2)
                                .cast((l, s) -> {
                                    if (l.level().isClientSide)
                                        return ActivityResult.SUCCESS;

                                    if (!(l instanceof Player player))
                                        return ActivityResult.FAILURE;

                                    int level = getAbilityLevel(player, s, "crafting");
                                    player.openMenu(new MontuCompositeContainer.Provider(level - 1, s), buf -> {
                                        ByteBufCodecs.INT.encode(buf, level);
                                        ItemStack.STREAM_CODEC.encode(buf, s);
                                    });
                                    player.stopUsingItem();
                                    return ActivityResult.SUCCESS;
                                })
                                .build())
                        .build())
                .build();
    }
    
    @Override
    public @Nullable RelicAttributeModifier getRelicAttributeModifiers(LivingEntity livingEntity, ItemStack stack) {
        var modifiers = super.getRelicAttributeModifiers(livingEntity, stack);
        return RelicAttributeModifier.builder()
                .attributes(modifiers == null ? modifiers.getAttributes() : List.of())
                .attribute(new RelicAttributeModifier.Modifier(Attributes.BLOCK_INTERACTION_RANGE, (float) ((ItemMontuGloves) stack.getItem()).getStatValue(livingEntity, stack, "gloves_range", "range"), AttributeModifier.Operation.ADD_VALUE))
                .attribute(new RelicAttributeModifier.Modifier(Attributes.ENTITY_INTERACTION_RANGE, (float) ((ItemMontuGloves) stack.getItem()).getStatValue(livingEntity, stack, "gloves_range", "range"), AttributeModifier.Operation.ADD_VALUE))
                .attribute(new RelicAttributeModifier.Modifier(Attributes.BLOCK_BREAK_SPEED, (float) ((ItemMontuGloves) stack.getItem()).getStatValue(livingEntity, stack, "gloves_range", "block_braking") / 100, AttributeModifier.Operation.ADD_VALUE))
                .build();
    }

    @Override
    public void onPostRegistered(ResourceLocation id) {
        EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onItemHurt(ItemHurtEvent event) {
        if (event.getOwner() == null || !event.getStack().supportsEnchantment(event.getLevel().holder(Enchantments.UNBREAKING).get()))
            return;
        
        int damage = modifyItemDamage(event.getDamageHurt(), event.getOwner());
        event.setDamageHurt(damage);
    }
    
    private int modifyItemDamage(int damage, LivingEntity owner) {
        return CuriosApi.getCuriosInventory(owner).map((handler) -> {
            double modified = damage;
            IDynamicStackHandler stacks = handler.getCurios().get("hands").getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                ItemStack relic = stacks.getStackInSlot(i);
                if (relic.is(this)) {
                    modified = (1 - ItemsRegistry.MONTU_GLOVES.getStatValue(owner, relic, "gloves_range", "unbreaking") / 100d) * damage;
                    double d = modified - (int) modified;
                    if (d < 1)
                        modified += owner.getRandom().nextDouble() < d ? 0 : 1;
                    addExperience(owner, relic, 3 * (damage - ((int) modified)));
                }
                modified = damage;
            }
            return (int) modified;
        }).orElse(damage);
    }

//    @SubscribeEvent
//    public void onEnchantingLevelSet(EnchantCostEventPre updateEvent) {
//        Player player = updateEvent.getPlayer();
//        var itr = ItemChargeHelper.listPlayerInventories(player).iterator();
//        while (itr.hasNext()) {
//            var ih = itr.next();
//            for (int j = 0; j < ih.getSlots(); j++) {
//                var it = ih.getStackInSlot(j);
//                if (!it.is(this))
//                    continue;
//
//                double discount = ((ItemMontuGloves) it.getItem()).getStatValue(it, "crafting", "enchant_discount");
//                int newCost = (int) (updateEvent.getNewLevel() * (1 - discount / 100));
//                updateEvent.setNewLevel(newCost == 0 ? 1 : newCost);
//
//                break;
//            }
//        }
//    }
    
    @SubscribeEvent
    public void gloveCanEquip(CurioCanEquipEvent canEquipEvent) {
        if (canEquipEvent.getStack().is(this)) {
            boolean can = CuriosApi.getCuriosInventory(canEquipEvent.getEntity())
                    .map(handler -> handler.getCurios().get("hands"))
                    .map(handler -> {
                        var stacks = handler.getStacks();
                        return IntStream.range(0, stacks.getSlots()).allMatch(i -> canEquipEvent.getSlotContext().index() == i || stacks.getStackInSlot(i).isEmpty());
                    }).orElse(true);
            canEquipEvent.setEquipResult(can ? canEquipEvent.getEquipResult() : TriState.FALSE);
        }
    }
    
    @SubscribeEvent
    public void gloveEquip(CurioChangeEvent changeEvent) {
        if (changeEvent.getTo().is(this)) {
            CuriosApi.getCuriosInventory(changeEvent.getEntity()).ifPresent(handler -> {
                IDynamicStackHandler stacks = handler.getCurios().get("hands").getStacks();
                if (stacks.getStackInSlot(0).isEmpty()) {
                    handler.setEquippedCurio("hands", 0, changeEvent.getTo().copy());
                    changeEvent.getTo().shrink(1);
                }
//                handler.setEquippedCurio("hands", 1, ItemStack.EMPTY);
            });
        }
    }
    
    @Override
    public @Nullable RelicSlotModifier getSlotModifiers(LivingEntity livingEntity, ItemStack stack) {
        return RelicSlotModifier.builder().modifier("hands", -1).build();
    }
    
    @SubscribeEvent
    public void forgeDoneEvent(AnvilRepairEvent updateEvent) {
        Player player = updateEvent.getEntity();
        
        if (player.level().isClientSide || !(updateEvent.getEntity().containerMenu instanceof AnvilMenu menu))
            return;
        
        var itr = ItemChargeHelper.listPlayerInventories(player).iterator();
        while (itr.hasNext()) {
            var ih = itr.next();
            for (int j = 0; j < ih.getSlots(); j++) {
                var it = ih.getStackInSlot(j);
                if (!it.is(this))
                    continue;
                
                double discount = ((ItemMontuGloves) it.getItem()).getStatValue(player, it, "crafting", "discount");
                addExperience(player, it, (int) (menu.getCost() * 100 / (100 - discount)));
            }
        }
    }

    @SubscribeEvent
    public void forgeRepairEvent(AnvilRepairPostCountEvent updateEvent) {
        Player player = updateEvent.getPlayer();

        if (player.level().isClientSide || !(updateEvent.getPlayer().containerMenu instanceof AnvilMenu menu))
            return;

        var itr = ItemChargeHelper.listPlayerInventories(player).iterator();
        while (itr.hasNext()) {
            var ih = itr.next();
            for (int j = 0; j < ih.getSlots(); j++) {
                var it = ih.getStackInSlot(j);
                if (!it.is(this) || !hasRangModifier(player, it, "gloves_range", "discount"))
                    continue;

                double discount = updateEvent.getRepairItemCountCost() * ((ItemMontuGloves) it.getItem()).getStatValue(player, it, "gloves_range", "discount") / 100d;
                double remains = discount - (int) discount;
                int finalDiscount = ((int) discount) + ((Math.random() < remains) ? 1 : 0);
                updateEvent.setRepairItemCountCost(updateEvent.getRepairItemCountCost() - finalDiscount);
            }
        }
    }

    @SubscribeEvent
    public void forgeEnchantEvent(AnvilEnchantmentMergeEvent updateEvent) {
        Player player = updateEvent.getPlayer();

        if (player.level().isClientSide || !(updateEvent.getPlayer().containerMenu instanceof AnvilMenu menu))
            return;

        var itr = ItemChargeHelper.listPlayerInventories(player).iterator();
        while (itr.hasNext()) {
            var ih = itr.next();
            for (int j = 0; j < ih.getSlots(); j++) {
                var it = ih.getStackInSlot(j);
                if (!it.is(this) || !hasRangModifier(player, it, "gloves_range", "discount"))
                    continue;

                double discount = ((ItemMontuGloves) it.getItem()).getStatValue(player, it, "gloves_range", "discount") / 100d;
                if (Math.random() < discount)
                    updateEvent.setBookTaken(false);
            }
        }
    }
    
    @SubscribeEvent
    public void forgingEvent(AnvilUpdatePostEvent updateEvent) {
        Player player = updateEvent.getPlayer();
        
        CuriosApi.getCuriosInventory(updateEvent.getPlayer()).ifPresent(handler -> {
            IDynamicStackHandler stacks = handler.getCurios().get("hands").getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                var it = stacks.getStackInSlot(i);
                if (!it.is(this))
                    continue;
                
                double discount = ((ItemMontuGloves) it.getItem()).getStatValue(player, it, "crafting", "discount");
                int newCost = (int) (updateEvent.getCost() * (1 - discount / 100));
                updateEvent.setCost(newCost == 0 ? 1 : newCost);
                
                break;
            }
        });
    }
    
    @Override
    public HumanoidModel<? extends LivingEntity> getModel(Player player, ItemStack stack, HumanoidArm arm) {
        boolean flawless = getRelicData(player, stack).isFlawless();
        return flawless ? arm == HumanoidArm.LEFT ? LEFT_FLAWLESS.get() : RIGHT_FLAWLESS.get() : arm == HumanoidArm.LEFT ? LEFT.get() : RIGHT.get();
    }
    
    @Override
    public ResourceLocation getTexture(Player player, ItemStack stack, HumanoidArm arm) {
        boolean flawless = getRelicData(player, stack).isFlawless();
        return flawless ? getFlawlessLocation(player.tickCount / 2) : TEXTURE;
    }

    private ResourceLocation getFlawlessLocation(int tickCount) {
        return ThirteenFlames.rl(String.format("textures/armor/montu_glove_upgraded%d.png", tickCount % 6 + 1));
    }

}
