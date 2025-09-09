package com.qurenie.relics_thirteenflames.content.items;

import com.qurenie.api.AnvilUpdatePostEvent;
import com.qurenie.api.ItemHurtEvent;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.container.MontuCompositeContainer;
import com.qurenie.relics_thirteenflames.content.container.MontuGlovesContainer;
import com.qurenie.relics_thirteenflames.content.items.base.IRenderableCurioHand;
import com.qurenie.relics_thirteenflames.content.items.models.MontuGlovesArmorLeft;
import com.qurenie.relics_thirteenflames.content.items.models.MontuGlovesArmorRight;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import it.hurts.sskirillss.relics.init.RelicContainerRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicAttributeModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicSlotModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.PredicateType;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
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
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
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

public class ItemMontuGloves extends RelicItem implements IRegisterListener, IRenderableCurioHand {
    
    public static final ResourceLocation BLOCK_INTERACTION_RANGE_ID = ThirteenFlames.rl("gloves_block_interaction_range");
    public static final ResourceLocation ENTITY_INTERACTION_RANGE_ID = ThirteenFlames.rl("gloves_entity_interaction_range");
    private static final Lazy<HumanoidModel<? extends LivingEntity>> RIGHT = Lazy.lazy(() -> new MontuGlovesArmorRight<>(Minecraft.getInstance().getEntityModels().bakeLayer(MontuGlovesArmorRight.LAYER_LOCATION)));
    private static final Lazy<HumanoidModel<? extends LivingEntity>> LEFT = Lazy.lazy(() -> new MontuGlovesArmorLeft<>(Minecraft.getInstance().getEntityModels().bakeLayer(MontuGlovesArmorLeft.LAYER_LOCATION)));
    private static final ResourceLocation TEXTURE = ThirteenFlames.rl("textures/armor/montu_gloves.png");
    
    public ItemMontuGloves(Properties properties) {
        super(properties);
    }
    
    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("gloves_range")
                                .maxLevel(5)
                                .stat(StatData.builder("range")
                                        .initialValue(1, 3)
                                        .thresholdValue(1, 8)
                                        .upgradeModifier(UpgradeOperation.ADD, 1)
                                        .formatValue(d -> MathUtils.round(d, 1))
                                        .build()
                                )
                                .stat(StatData.builder("unbreaking")
                                        .initialValue(10, 25)
                                        .thresholdValue(10, 75)
                                        .upgradeModifier(UpgradeOperation.ADD, 10)
                                        .formatValue(d -> MathUtils.round(d, 0))
                                        .build()
                                )
                                .stat(StatData.builder("block_braking")
                                        .initialValue(10, 15)
                                        .thresholdValue(10, 50)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, 0.28)
                                        .formatValue(d -> MathUtils.round(d, 0))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityData.builder("crafting")
                                .maxLevel(4)
                                .active(CastData.builder()
                                        .container(RelicContainerRegistry.CURIOS.get())
                                        .type(CastType.INSTANTANEOUS)
                                        .predicate("crafting_predicate", PredicateType.VISIBILITY,
                                                (p, s) -> getAbilityLevel(s, "crafting") > 1)
                                        .build())
                                .stat(StatData.builder("discount")
                                        .initialValue(9.99, 9.99)
                                        .thresholdValue(5, 55)
                                        .upgradeModifier(UpgradeOperation.ADD, 10)
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityData.builder("smelting")
                                .maxLevel(1)
                                .active(CastData.builder()
                                        .predicate("smelting_predicate", PredicateType.VISIBILITY,
                                                (p, s) -> getAbilityLevel(s, "smelting") > 0)
                                        .container(RelicContainerRegistry.CURIOS.get())
                                        .type(CastType.INSTANTANEOUS)
                                        .build())
                                .requiredPoints(5)
                                .requiredLevel(8)
                                .stat(StatData.builder("empty").build())
                                .build()
                        )
                        .build()
                )
                .leveling(new LevelingData(75, 15, 50))
                .loot(LootData.builder().entry(LootEntries.MINESHAFT).build())
                .build();
    }
    
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, List<Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("tooltip.relics_thirteenflames.montu_gloves.lore").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, tooltip, isAdvanced);
    }
    
    @Override
    public @Nullable RelicAttributeModifier getRelicAttributeModifiers(ItemStack stack) {
        return RelicAttributeModifier.builder()
                .attribute(new RelicAttributeModifier.Modifier(Attributes.BLOCK_INTERACTION_RANGE, (float) ((IRelicItem) stack.getItem()).getStatValue(stack, "gloves_range", "range"), AttributeModifier.Operation.ADD_VALUE))
                .attribute(new RelicAttributeModifier.Modifier(Attributes.ENTITY_INTERACTION_RANGE, (float) ((IRelicItem) stack.getItem()).getStatValue(stack, "gloves_range", "range"), AttributeModifier.Operation.ADD_VALUE))
                .attribute(new RelicAttributeModifier.Modifier(Attributes.BLOCK_BREAK_SPEED, (float) ((IRelicItem) stack.getItem()).getStatValue(stack, "gloves_range", "block_braking") / 100, AttributeModifier.Operation.ADD_VALUE))
                .build();
    }
    
    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        if (player.level().isClientSide)
            return;
        
        if (ability.equals("smelting") && getAbilityLevel(stack, "smelting") > 0) {
            player.openMenu(new MontuGlovesContainer.Provider(stack), buf -> ItemStack.STREAM_CODEC.encode(buf, stack));
            player.stopUsingItem();
            return;
        }
        
        int level = getAbilityLevel(stack, "crafting");
        if (ability.equals("crafting") && level >= 1) {
            player.openMenu(new MontuCompositeContainer.Provider(level - 1, stack), buf -> {
                ByteBufCodecs.INT.encode(buf, level - 1);
                ItemStack.STREAM_CODEC.encode(buf, stack);
            });
            player.stopUsingItem();
        }
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
                    modified = (1 - ItemsRegistry.MONTU_GLOVES.getStatValue(relic, "gloves_range", "unbreaking") / 100d) * damage;
                    double d = modified - (int) modified;
                    if (d < 1)
                        modified += owner.getRandom().nextDouble() < d ? 0 : 1;
                    addRelicExperience(relic, 3 * (damage - ((int) modified)));
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
            canEquipEvent.setEquipResult(can ? TriState.DEFAULT : TriState.FALSE);
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
    public @Nullable RelicSlotModifier getSlotModifiers(ItemStack stack) {
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
                
                double discount = ((ItemMontuGloves) it.getItem()).getStatValue(it, "crafting", "discount");
                addRelicExperience(it, (int) (menu.getCost() * 100 / (100 - discount)));
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
                
                double discount = ((ItemMontuGloves) it.getItem()).getStatValue(it, "crafting", "discount");
                int newCost = (int) (updateEvent.getCost() * (1 - discount / 100));
                updateEvent.setCost(newCost == 0 ? 1 : newCost);
                
                break;
            }
        });
    }
    
    @Override
    public HumanoidModel<? extends LivingEntity> getModel(ItemStack stack, HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? LEFT.get() : RIGHT.get();
    }
    
    @Override
    public ResourceLocation getTexture(ItemStack stack, HumanoidArm arm) {
        return TEXTURE;
    }
    
}
