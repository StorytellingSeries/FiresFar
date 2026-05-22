package com.qurenie.relics_thirteenflames.content.items.feather;

import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.qurenie.relics_thirteenflames.content.items.feather.ItemHettFeather.BOOK_ACTIVE_MAX_LEVEL;
import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.*;

@EventBusSubscriber
public class ItemHettFeatherBook extends Item implements ICurioItem {
    
    public ItemHettFeatherBook(Properties properties) {
        super(properties);
    }
    
    @Override
    public <T extends LivingEntity> int damageItem(@NotNull ItemStack stack, int amount, @Nullable T entity, @NotNull Consumer<Item> onBroken) {
        return BOOK_ACTIVE_MAX_LEVEL / stack.getOrDefault(ComponentRegistry.LEVEL, 1) * amount;
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void damageEvent(LivingDamageEvent.Pre event) {
        Entity e = event.getSource().getEntity();
        if (e == null)
            return;
        if (event.getEntity() instanceof LivingEntity living)
            CuriosApi.getCuriosInventory(living).ifPresent((handler) -> {
                IDynamicStackHandler stacks = handler.getCurios().get("charm").getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (stack.getItem() instanceof ItemHettFeatherBook book) {
                        if (book.isCorrectEntityType(stack, e.getType())) {
                            int level = stack.getOrDefault(ComponentRegistry.LEVEL, 1);
                            event.setNewDamage(event.getNewDamage() * (1 - 0.5f * level / BOOK_ACTIVE_MAX_LEVEL));
                            
                            if (living.level() instanceof ServerLevel serverLevel)
                                stack.hurtAndBreak(1, serverLevel, living, item -> {});
                        }
                    }
                }
            });
        
        if (!(event.getSource().getEntity() instanceof LivingEntity living))
            return;
        
        CuriosApi.getCuriosInventory(living).ifPresent((handler) -> {
            IDynamicStackHandler stacks = handler.getCurios().get("charm").getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                ItemStack stack = stacks.getStackInSlot(0);
                if (stack.getItem() instanceof ItemHettFeatherBook book) {
                    if (book.isCorrectEntityType(stack, event.getEntity().getType())) {
                        int level = stack.getOrDefault(ComponentRegistry.LEVEL, 1);
                        event.setNewDamage(event.getNewDamage() +
                                event.getNewDamage() * 0.75f * level / BOOK_ACTIVE_MAX_LEVEL);
                        
                        if (living.level() instanceof ServerLevel serverLevel)
                            stack.hurtAndBreak(1, serverLevel, living, item -> {});
                    }
                }
            }
        });
    }

    private static final String[] ROMAN_NUMERALS = {
            "", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX",
            "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX",
            "XX", "XXI", "XXII", "XXIII", "XXIV", "XXV", "XXVI", "XXVII", "XXVIII", "XXIX",
            "XXX", "XXXI", "XXXII", "XXXIII", "XXXIV", "XXXV", "XXXVI", "XXXVII", "XXXVIII", "XXXIX",
            "XL", "XLI", "XLII", "XLIII", "XLIV", "XLV", "XLVI", "XLVII", "XLVIII", "XLIX",
            "L", "LI", "LII", "LIII", "LIV", "LV", "LVI", "LVII", "LVIII", "LIX",
            "LX", "LXI", "LXII", "LXIII", "LXIV", "LXV", "LXVI", "LXVII", "LXVIII", "LXIX",
            "LXX", "LXXI", "LXXII", "LXXIII", "LXXIV", "LXXV", "LXXVI", "LXXVII", "LXXVIII", "LXXIX",
            "LXXX", "LXXXI", "LXXXII", "LXXXIII", "LXXXIV", "LXXXV", "LXXXVI", "LXXXVII", "LXXXVIII", "LXXXIX",
            "XC", "XCI", "XCII", "XCIII", "XCIV", "XCV", "XCVI", "XCVII", "XCVIII", "XCIX",
            "C"
    };

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        int level = stack.getOrDefault(ComponentRegistry.LEVEL, 1);

        String numeral = level >= 0 && level <= 100
                ? ROMAN_NUMERALS[level]
                : Integer.toString(level);

        MutableComponent component =
                MutableComponent.create(super.getName(stack).getContents());

        component.append(" ").append(numeral);

        return component;
    }
    
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        List<EntityType<?>> types = stack.getOrDefault(TARGET_TYPES, List.of());
        int level = stack.getOrDefault(SIZE, 1);
        int j = 0;
        for (int i = 0; i < Math.min(level, types.size()); i++) {
            var entityType = types.get(i);
            tooltipComponents.add(MutableComponent.create(entityType.getDescription().getContents()).withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
            j++;
        }
        for (int i = j; i < level; i++) {
            tooltipComponents.add(Component.literal("(???)").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
    
    @Override
    public int getLootingLevel(SlotContext slotContext, @Nullable LootContext lootContext, ItemStack stack) {
        if (lootContext == null)
            return ICurioItem.super.getLootingLevel(slotContext, null, stack);
        
        Entity target = lootContext.getParam(LootContextParams.THIS_ENTITY);
        if (stack.getItem() instanceof ItemHettFeatherBook book && book.isCorrectEntityType(stack, target.getType()))
            return stack.getOrDefault(ComponentRegistry.LEVEL, 1);
        
        return ICurioItem.super.getLootingLevel(slotContext, lootContext, stack);
    }
    
    public boolean isCorrectEntityType(ItemStack stack, EntityType<?> type) {
        List<EntityType<?>> types = stack.getOrDefault(TARGET_TYPES, List.of());
        return types.contains(type);
    }
    
}
