package com.qurenie.relics_thirteenflames.content.items;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.InputConstants;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.items.base.IArmor;
import com.qurenie.relics_thirteenflames.init.ArmorMaterialRegistry;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.net.EntityPacket;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.zeith.hammerlib.api.fml.IRegisterListener;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.net.PacketContext;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

public class AuritekhElytraItem extends ElytraItem implements IRegisterListener, IArmor {
    
    protected final ArmorItem.Type type;
    protected final Holder<ArmorMaterial> material;
    private final Supplier<ItemAttributeModifiers> defaultModifiers;
    
    private static final ResourceLocation ARMOR_TEXTURE = ThirteenFlames.rl("textures/armor/auritekh_elytra_armor.png");
    
    public AuritekhElytraItem(Holder<ArmorMaterial> material, ArmorItem.Type type, Properties properties) {
        super(properties);
        this.material = material;
        this.type = type;
        
        this.defaultModifiers = Suppliers.memoize(
                () -> {
                    int i = (int) (material.value().getDefense(type) * 0.65f);
                    float f = material.value().toughness() * 0.65f;
                    ItemAttributeModifiers.Builder itemattributemodifiers$builder = ItemAttributeModifiers.builder();
                    EquipmentSlotGroup equipmentslotgroup = EquipmentSlotGroup.bySlot(type.getSlot());
                    ResourceLocation resourcelocation = ResourceLocation.withDefaultNamespace("armor." + type.getName());
                    itemattributemodifiers$builder.add(
                            Attributes.ARMOR, new AttributeModifier(resourcelocation, i, AttributeModifier.Operation.ADD_VALUE), equipmentslotgroup
                    );
                    itemattributemodifiers$builder.add(
                            Attributes.ARMOR_TOUGHNESS, new AttributeModifier(resourcelocation, f, AttributeModifier.Operation.ADD_VALUE), equipmentslotgroup
                    );
                    float f1 = material.value().knockbackResistance() * 0.75f;
                    if (f1 > 0.0F) {
                        itemattributemodifiers$builder.add(
                                Attributes.KNOCKBACK_RESISTANCE,
                                new AttributeModifier(resourcelocation, f1, AttributeModifier.Operation.ADD_VALUE),
                                equipmentslotgroup
                        );
                    }
                    
                    return itemattributemodifiers$builder.build();
                }
        );
    }
    
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, List<net.minecraft.network.chat.Component> tooltip, @NotNull TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("tooltip.relics_thirteenflames.auritekh_elytra.ability").withStyle(ChatFormatting.DARK_AQUA).withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, tooltip, isAdvanced);
    }
    
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        
        int c = stack.getOrDefault(ComponentRegistry.ACTIVE_TICK, 0);
        if (c > 0)
            stack.set(ComponentRegistry.ACTIVE_TICK, c - 1);
    }
    
    @Override
    public boolean supportsEnchantment(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        if (enchantment.is(Enchantments.UNBREAKING) || enchantment.is(Enchantments.MENDING))
            return false;
        return super.supportsEnchantment(stack, enchantment);
    }
    
    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        LocalPlayer player = mc.player;
        
        // Проверяем, летит ли на элитрах
        ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
        
        if (player.isFallFlying() && stack.is(this) && Minecraft.getInstance().screen == null) {
            long window = mc.getWindow().getWindow();
            if (InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL)) {
                Network.sendToServer(new EntityPacket(player.getId()) {
                    
                    @Override
                    public void serverExecute(PacketContext ctx) {
                        super.serverExecute(ctx);
                        
                        if (!(getEntity(ctx.getLevel()) instanceof ServerPlayer p))
                            return;
                        
                        ItemStack stack = p.getItemBySlot(EquipmentSlot.CHEST);
                        if (!stack.is(ItemsRegistry.AURITEKH_ELYTRA))
                            return;
                        
                        int cooldown = stack.getOrDefault(ComponentRegistry.ACTIVE_TICK, 0);
                        if (cooldown > 0)
                            return;
                        
                        Vec3 look = p.getLookAngle();
                        Vec3 boost = look.scale(0.8);
                        
                        ParticleHelper.spawnParticles(p.level(), ParticleHelper.constructSimpleSpark(new Color(239, 215, 182), 0.4f, 60, 0.97f),
                                p.getBoundingBox().getCenter(), 20, 0.15, 0.15, 0.15, 0.02);
                        ParticleHelper.spawnParticles(p.level(), ParticleHelper.constructSimpleSpark(new Color(4,136,247), 0.35f, 50, 0.95f).withGravity(1.4f),
                                p.getBoundingBox().getCenter(), 30, 0.1, 0.1, 0.1, 0.4);
                        ParticleHelper.spawnParticles(p.level(), ParticleHelper.constructSmoke(new Color(4,136,247), 1f, 70, 0f),
                                p.getBoundingBox().getCenter(), 40, 0.1, 0.1, 0.1, 0.03);
                        
                        p.setDeltaMovement(p.getDeltaMovement().add(boost));
                        p.connection.send(new ClientboundSetEntityMotionPacket(p));
                        stack.set(ComponentRegistry.ACTIVE_TICK, 70);
                    }
                });
                
            }
        }
    }
    
    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers() {
        return this.defaultModifiers.get();
    }
    
    @Override
    public int getEnchantmentValue() {
        return this.material.value().enchantmentValue();
    }
    
    @Override
    public boolean elytraFlightTick(@NotNull ItemStack stack, LivingEntity entity, int flightTicks) {
        if (!entity.level().isClientSide) {
            int nextFlightTick = flightTicks + 1;
            if (nextFlightTick % 10 == 0) {
                entity.gameEvent(net.minecraft.world.level.gameevent.GameEvent.ELYTRA_GLIDE);
            }
        }
        
//        entity.setDeltaMovement(entity.getDeltaMovement().add(entity.getLookAngle().scale(0.02)));
        return true;
    }
    
    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
    
    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        return 0;
    }
    
    @Override
    public @Nullable ResourceLocation getArmorTexture(@NotNull ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return ARMOR_TEXTURE;
    }
    
    @Override
    public void onPostRegistered(ResourceLocation id) {
        EVENT_BUS.register(this);
    }
    
    @Override
    public @NotNull Holder<ArmorMaterial> getMaterial() {
        return ArmorMaterialRegistry.MONTU_SMITH_TYPE;
    }
    
    @Override
    public ArmorItem.@NotNull Type getType() {
        return ArmorItem.Type.CHESTPLATE;
    }
    
    @Override
    public @NotNull Item self() {
        return this;
    }
    
}
