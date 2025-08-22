package com.qurenie.relics_thirteenflames.content.entities.base;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NonLivingEntity extends LivingEntity {
    
    protected NonLivingEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }
    
    @Override
    public boolean isAlive() {
        return false;
    }
    
    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        return true;
    }
    
    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }
    
    @Override
    public boolean isAttackable() {
        return false;
    }
    
    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return false; // полностью игнорирует любой урон
    }
    
    @Override
    public boolean isPickable() {
        return false;
    }
    
    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return List.of();
    }
    
    @Override
    public ItemStack getItemBySlot(@NotNull EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
    }
    
    @Override
    protected void doPush(@NotNull Entity other) {
        // ничего
    }
    
    @Override
    public void push(double x, double y, double z) {
        // ничего
    }
    
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }
    
    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }
    
}
