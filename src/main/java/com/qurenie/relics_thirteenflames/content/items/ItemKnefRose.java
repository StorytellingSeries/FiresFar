package com.qurenie.relics_thirteenflames.content.items;

import com.qurenie.relics_thirteenflames.content.entities.LivingFleshEntity;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ParticlesRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.sskirillss.relics.api.events.common.ContainerSlotClickEvent;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.NBTUtils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.HammerLib;
import org.zeith.hammerlib.api.fml.IRegisterListener;
import org.zeith.hammerlib.api.io.IAutoNBTSerializable;
import org.zeith.hammerlib.api.io.NBTSerializable;
import org.zeith.hammerlib.api.items.IColoredFoilItem;
import org.zeith.hammerlib.util.charging.ItemChargeHelper;

import java.util.List;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.*;
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

public class ItemKnefRose
        extends RelicItem
        implements IColoredFoilItem, IRegisterListener {
    
    public ItemKnefRose(Properties properties) {
        super(properties);
    }
    
    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("undeath")
                                .maxLevel(5)
                                .stat(StatData.builder("max_bones")
                                        .initialValue(7, 10)
                                        .thresholdValue(7, 20)
                                        .upgradeModifier(UpgradeOperation.ADD, 2)
                                        .formatValue(x -> MathUtils.round(x, 0))
                                        .build()
                                )
                                .stat(StatData.builder("damage_taken")
                                        .initialValue(0.75, 1)
                                        .thresholdValue(0.75, 2.0)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.2)
                                        .formatValue(x -> MathUtils.round(x, 2))
                                        .build()
                                )
                                .stat(StatData.builder("deterioration_rate")
                                        .initialValue(800, 1200)
                                        .thresholdValue(1, 24000)
                                        .upgradeModifier(UpgradeOperation.ADD, 240)
                                        .formatValue(x -> MathUtils.round(1200.0 / x, 2))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityData.builder("living_rot")
                                .maxLevel(10)
                                .stat(StatData.builder("chance")
                                        .initialValue(5.0, 5.0)
                                        .thresholdValue(5.0, 30.0)
                                        .upgradeModifier(UpgradeOperation.ADD, 2.5)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(StatData.builder("hp_rate")
                                        .initialValue(0.2, 0.3)
                                        .thresholdValue(0.2, 1)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.05)
                                        .formatValue(x -> MathUtils.round(x * 100, 0))
                                        .build()
                                )
                                .build()
                        )
                        .ability(AbilityData.builder("rot_split")
                                .requiredLevel(10)
                                .maxLevel(5)
                                .stat(StatData.builder("chance")
                                        .initialValue(10.0, 12.5)
                                        .thresholdValue(10.0, 30.0)
                                        .upgradeModifier(UpgradeOperation.ADD, 2.5)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(StatData.builder("split_size")
                                        .initialValue(30.0, 35.0)
                                        .thresholdValue(30.0, 50.0)
                                        .upgradeModifier(UpgradeOperation.ADD, 2.5)
                                        .formatValue(x -> MathUtils.round(x, 1))
                                        .build()
                                )
                                .stat(StatData.builder("max_splits")
                                        .initialValue(2.0, 2.0)
                                        .thresholdValue(2.0, 8.0)
                                        .upgradeModifier(UpgradeOperation.ADD, 2.0)
                                        .formatValue(Double::intValue)
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .leveling(new LevelingData(100, 20, 150))
                .loot(LootData.builder().entry(LootEntries.THE_NETHER).build())
                .build();
    }
    
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, List<Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("tooltip.relics_thirteenflames.knef_rose.lore").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
        int bones = getBones(stack);
        if (bones > 0)
            tooltip.add(Component.literal("Костей: ").append(Integer.toUnsignedString(bones))
                    .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, isAdvanced);
    }
    
    @Override
    public int getFoilColor(@NotNull ItemStack stack) {
        return 0xCCA1CE | FULL_ALPHA;
    }
    
    @Override
    public void onPostRegistered(ResourceLocation id) {
        EVENT_BUS.register(this);
    }
    
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        if (!level.isClientSide()) {
            if (entity.level().dimension().equals(Level.NETHER)) {
                int netherTicker = stack.getOrDefault(NETHER_TICKER, 0);
                if (netherTicker++ >= 1200) {
                    netherTicker = 0;
                    this.addBones(stack, 1);
                }
                stack.set(NETHER_TICKER, netherTicker);
            } else {
                int deterioTicker = stack.getOrDefault(DETERIORATION_TICKER, 0);
                if (this.getBones(stack) > 0 && deterioTicker++ >= this.getStatValue(stack, "undeath", "deterioration_rate")) {
                    deterioTicker = 0;
                    this.takeBones(stack, 1, false);
                }
                stack.set(DETERIORATION_TICKER, deterioTicker);
            }
        }
        super.inventoryTick(stack, level, entity, slot, isSelected);
    }
    
    @SubscribeEvent
    public void slotClick(ContainerSlotClickEvent e) {
        if (e.getSlotStack().is(this) && e.getHeldStack().is(Tags.Items.BONES)) {
            var stack = e.getSlotStack();
            int bones = e.getHeldStack().getCount();
            
            // Limit to 1 bone per right click; No limit on left click.
            if (e.getAction() == ClickAction.SECONDARY) bones = Math.min(bones, 1);
            bones = Math.min(bones, getMaxBones(stack) - getBones(stack));
            addBones(stack, bones);
            e.getHeldStack().shrink(bones);
            
            e.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public void livingDeath(LivingDeathEvent e) {
        if (e.getSource().getEntity() instanceof LivingFleshEntity flesch) {
            flesch.lifetime += (int) (e.getEntity().getMaxHealth() * 10);
        }
        
        
        if (!(e.getEntity() instanceof Enemy)) return;
        if (!(e.getSource().getEntity() instanceof ServerPlayer sp)) return;
        
        var itr = ItemChargeHelper.listPlayerInventories(sp).iterator();
        while (itr.hasNext()) {
            var ih = itr.next();
            for (int j = 0; j < ih.getSlots(); j++) {
                var it = ih.getStackInSlot(j);
                if (it.is(this)) {
                    var spawnChance = this.getStatValue(it, "living_rot", "chance") / 100;
                    
                    if (sp.getRandom().nextFloat() < spawnChance) {
                        LivingFleshEntity ent = new LivingFleshEntity(EntityRegistry.LIVING_FLESH, sp.level())
                                .initPrimary(e.getEntity(), new RoseStats(it));
                        ent.moveTo(e.getEntity().position());
                        ent.setOwnerUUID(sp.getStringUUID());
                        ent.lifetime = (int) (e.getEntity().getMaxHealth() * 15);
                        
                        HammerLib.PROXY.queueTask(sp.level(), 15, () -> sp.level().addFreshEntity(ent));
                        return;
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public void playerHurt(LivingDamageEvent.Pre e) {
        if (e.getEntity() instanceof Player pl) {
            var itr = ItemChargeHelper.listPlayerInventories(pl).iterator();
            while (itr.hasNext()) {
                var ih = itr.next();
                for (int j = 0; j < ih.getSlots(); j++) {
                    var it = ih.getStackInSlot(j);
                    if (it.is(this) && getBones(it) > 0) {
                        
                        float newAmount = reduceDamage(pl, it, e.getNewDamage());
                        if (newAmount == e.getNewDamage()) continue;
                        
                        e.setNewDamage(newAmount);
                        ParticleHelper.spawnParticleEntity(ParticlesRegistry.DEATH_FLAME_PARTICLE.get(), pl, 16, 0.05);
                        
                        if (e.getNewDamage() <= 0F) {
                            e.setNewDamage(0F);
                            return;
                        }
                    }
                }
            }
        }
    }
    
    public float reduceDamage(LivingEntity pl, ItemStack stack, float damage) {
        var damageReductionPerBone = this.getStatValue(stack, "undeath", "damage_taken");
        
        // Do not deduce bones if the damage is negligible
        if (damage < damageReductionPerBone) return damage;
        
        int neededBonesToNegateAllDamage = (int) Math.ceil(damage / damageReductionPerBone);
        int bonesTaken = takeBones(stack, neededBonesToNegateAllDamage, false);
        
        if (bonesTaken > 0) {//noinspection lossy-conversions
            damage -= bonesTaken * damageReductionPerBone;
            spreadRelicExperience(pl, stack, bonesTaken);
        }
        
        return Math.max(0F, damage);
    }
    
    public void addBones(ItemStack stack, int bones) {
        setBones(stack, getBones(stack) + bones);
    }
    
    public int takeBones(ItemStack stack, int bones, boolean simulate) {
        int avail = getBones(stack);
        bones = Math.min(bones, avail);
        if (!simulate) setBones(stack, avail - bones);
        return bones;
    }
    
    public void setBones(ItemStack stack, int bones) {
        bones = Math.max(bones, 0);
        bones = Math.min(bones, getMaxBones(stack));
        
        if (bones == 0)
            stack.remove(BONES);
        else
            stack.set(BONES, bones);
    }
    
    public int getBones(ItemStack stack) {
        return stack.getOrDefault(BONES, 0);
    }
    
    public int getMaxBones(ItemStack stack) {
        return (int) MathUtils.round(this.getStatValue(stack, "undeath", "max_bones"), 0);
    }
    
    @Getter
    public static class RoseStats
            implements IAutoNBTSerializable {
        
        public static final StreamCodec<RegistryFriendlyByteBuf, RoseStats> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public RoseStats decode(RegistryFriendlyByteBuf buf) {
                return new RoseStats(buf.readFloat(), buf.readFloat(), buf.readInt(), buf.readInt(), buf.readFloat(),
                        ItemStack.STREAM_CODEC.decode(buf));
            }
            
            @Override
            public void encode(RegistryFriendlyByteBuf buf, RoseStats stats) {
                buf.writeFloat(stats.splitChance);
                buf.writeFloat(stats.splitScale);
                buf.writeInt(stats.maxSplits);
                buf.writeInt(stats.counter);
                buf.writeFloat(stats.hpRate);
                ItemStack.STREAM_CODEC.encode(buf, stats.rose);
            }
        };
        
        @NBTSerializable
        public float splitChance = 10F;
        
        @NBTSerializable
        public float splitScale = 30F;
        
        @NBTSerializable
        public int maxSplits = 2;
        
        @NBTSerializable
        public int counter;
        
        @NBTSerializable
        public float hpRate = 0.3F;
        
        @NBTSerializable
        public ItemStack rose;
        
        private RoseStats(float splitChance, float splitScale, int maxSplits, int counter, float hpRate, ItemStack rose) {
            this.splitChance = splitChance;
            this.splitScale = splitScale;
            this.maxSplits = maxSplits;
            this.counter = counter;
            this.hpRate = hpRate;
            this.rose = rose;
        }
        
        public RoseStats(ItemStack roseStack) {
            if (roseStack.isEmpty() || !(roseStack.getItem() instanceof IRelicItem relic)) return;
            splitChance = relic.isAbilityUnlocked(roseStack, "rot_split") ? (float) relic.getStatValue(roseStack, "rot_split", "chance") : 0;
            splitScale = relic.isAbilityUnlocked(roseStack, "rot_split") ? (float) relic.getStatValue(roseStack, "rot_split", "split_size") : 0;
            maxSplits = relic.isAbilityUnlocked(roseStack, "rot_split") ? (int) relic.getStatValue(roseStack, "rot_split", "max_splits") : 0;
            hpRate = (float) relic.getStatValue(roseStack, "living_rot", "hp_rate");
            rose = roseStack;
        }
        
        public RoseStats(HolderLookup.Provider lookup, CompoundTag nbt) {
            deserializeNBT(lookup, nbt);
        }
        
        public int generateSplits(RandomSource src) {
            return 2 + (maxSplits > 2 ? src.nextInt(maxSplits - 1) : 0);
        }
        
        public RoseStats split() {
            RoseStats roseStats = new RoseStats(splitChance, splitScale, maxSplits, counter, hpRate, rose.copy());
            roseStats.counter++;
            return roseStats;
        }
        
    }
    
}