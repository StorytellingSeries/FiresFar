package com.qurenie.relics_thirteenflames.content.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
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
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;
import org.apache.logging.log4j.util.Lazy;
import org.zeith.hammerlib.api.fml.IRegisterListener;
import org.zeith.hammerlib.util.charging.ItemChargeHelper;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

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
                                        .build()
                                )
                                .stat(StatData.builder("unbreaking")
                                        .initialValue(10, 25)
                                        .thresholdValue(10, 75)
                                        .upgradeModifier(UpgradeOperation.ADD, 10)
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityData.builder("crafting")
                                .maxLevel(5)
                                .active(CastData.builder()
                                        .container(RelicContainerRegistry.CURIOS.get())
                                        .type(CastType.INSTANTANEOUS)
                                        .predicate("crafting_predicate", PredicateType.VISIBILITY,
                                                (p, s) -> getAbilityLevel(s, "crafting") > 1)
                                        .build())
                                .stat(StatData.builder("forge_discount")
                                        .initialValue(5, 15)
                                        .thresholdValue(5, 55)
                                        .upgradeModifier(UpgradeOperation.ADD, 8)
                                        .build()
                                )
                                .stat(StatData.builder("enchant_discount")
                                        .initialValue(5, 15)
                                        .thresholdValue(5, 65)
                                        .upgradeModifier(UpgradeOperation.ADD, 10)
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityData.builder("smelting")
                                .maxLevel(2)
                                .active(CastData.builder()
                                        .predicate("smelting_predicate", PredicateType.VISIBILITY,
                                                (p, s) -> getAbilityLevel(s, "smelting") > 1)
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
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        return ImmutableMultimap.<Holder<Attribute>, AttributeModifier>builder()
                .put(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(BLOCK_INTERACTION_RANGE_ID, ((IRelicItem) stack.getItem()).getStatValue(stack, "gloves_range", "range"), AttributeModifier.Operation.ADD_VALUE))
                .put(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(ENTITY_INTERACTION_RANGE_ID, ((IRelicItem) stack.getItem()).getStatValue(stack, "gloves_range", "range"), AttributeModifier.Operation.ADD_VALUE))
                .build();
    }
    
    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        if (player.level().isClientSide)
            return;
        
        if (ability.equals("smelting") && getAbilityLevel(stack, "smelting") > 1) {
            player.openMenu(new MontuGlovesContainer.Provider(stack), buf -> ItemStack.STREAM_CODEC.encode(buf, stack));
            player.stopUsingItem();
            return;
        }
        
        int level = getAbilityLevel(stack, "crafting");
        if (ability.equals("crafting") && level >= 2) {
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
        
        int damage = modifyItemDamage(event.getDamageHurt(), event.getOwner(), 1);
        damage = modifyItemDamage(damage, event.getOwner(), 0);
        event.setDamageHurt(damage);
    }
    
    private int modifyItemDamage(int damage, LivingEntity owner, int slot) {
        return CuriosApi.getCuriosInventory(owner).map((handler) -> {
            IDynamicStackHandler stacks = handler.getCurios().get("hands").getStacks();
            ItemStack relic = stacks.getStackInSlot(slot);
            if (relic.is(this)) {
                double modified = (1 - ItemsRegistry.MONTU_GLOVES.getStatValue(relic, "gloves_range", "unbreaking") / 100d) * damage;
                double d = modified - (int) modified;
                if (d < 1)
                    modified += owner.getRandom().nextDouble() < d ? 0 : 1;
                addRelicExperience(relic, 5 * (damage - ((int) modified)));
                return (int) modified;
            }
            return damage;
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
    public void forgeDoneEvent(AnvilRepairEvent updateEvent)
    {
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
                
                double discount = ((ItemMontuGloves) it.getItem()).getStatValue(it, "crafting", "forge_discount");
                addRelicExperience(it, (int) (discount * menu.getCost()));
            }
        }
    }
    
    @SubscribeEvent
    public void forgingEvent(AnvilUpdateEvent updateEvent) {
        Player player = updateEvent.getPlayer();
        
        var itr = ItemChargeHelper.listPlayerInventories(player).iterator();
        while (itr.hasNext()) {
            var ih = itr.next();
            for (int j = 0; j < ih.getSlots(); j++) {
                var it = ih.getStackInSlot(j);
                if (!it.is(this))
                    continue;
                
                double discount = ((ItemMontuGloves) it.getItem()).getStatValue(it, "crafting", "forge_discount");
                int newCost = (int) (updateEvent.getCost() * (1 - discount / 100));
                updateEvent.setCost(newCost == 0 ? 1 : newCost);
                
                break;
            }
        }
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
