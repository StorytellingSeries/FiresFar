package com.qurenie.relics_thirteenflames.content.entities;

import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.util.LayersList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.phys.Vec3;
import org.zeith.hammeranims.api.animation.IAnimationSource;
import org.zeith.hammeranims.api.animation.interp.BlendMode;
import org.zeith.hammeranims.api.animsys.AnimationSystem;
import org.zeith.hammeranims.api.animsys.CommonLayerNames;
import org.zeith.hammeranims.api.animsys.ConfiguredAnimation;
import org.zeith.hammeranims.api.animsys.layer.AnimationLayer;
import org.zeith.hammeranims.api.tile.IAnimatedEntity;
import org.zeith.hammeranims.core.init.DefaultsHA;
import org.zeith.hammerlib.api.io.NBTSerializationHelper;

public class AnimatedEntity
        extends PathfinderMob implements IAnimatedEntity {
    
    public static final String LAYER_WALKING = CommonLayerNames.LEGS;
    public static final String LAYER_ACTION = CommonLayerNames.ACTION;
    private static final ResourceLocation MAX_HEALTH_LOC = ResourceLocation.fromNamespaceAndPath(ThirteenFlames.MODID, "max_health_attribute");
    protected final AnimationSystem animationSystem = AnimationSystem.create(this);
    public boolean canDie;
    public boolean rotateModel180 = false;
    public boolean allowRotate = false;
    public float xPassengerOffset = 0;
    public float yPassengerOffset = 0;
    public float zPassengerOffset = 0;
    
    public AnimatedEntity(EntityType<? extends AnimatedEntity> type, Level world) {
        super(type, world);
    }
    
    @Override
    public void tick() {
        animationSystem.tick();
        super.tick();
    }
    
    @Override
    public float getWalkTargetValue(BlockPos pPos, LevelReader pLevel) {
        float value = super.getWalkTargetValue(pPos, pLevel);
        
        if (pLevel.getBlockState(pPos).getBlock() instanceof BushBlock)
            value -= 2;
        if (pLevel.getBlockState(pPos).getFluidState().isEmpty())
            value += 3;
        return value;
    }
    
    @Override
    public void setupSystem(AnimationSystem.Builder builder) {
        LayersList list = new LayersList();
        
        list.addLast(LAYER_ACTION, BlendMode.OVERRIDE, 1.0F);
        list.addLast(LAYER_WALKING, BlendMode.ADD, 1.0F);
        
        int xtraLayerCount = 5;
        
        var t = getType();
        
        for (int i = 0; i < xtraLayerCount; i++) {
            list.addLast("ANIMATION_" + i, BlendMode.ADD, 1.0F);
        }
        
        addLayers(list);
        
        builder.addLayers(list.getLayers().toArray(AnimationLayer.Builder[]::new));
        builder.autoSync(true);
    }
    
    public ConfiguredAnimation getWalkingAnimation() {
        return DefaultsHA.NULL_ANIMATION.configure();
    }
    
    public ConfiguredAnimation getIdleAnimation() {
        return DefaultsHA.NULL_ANIMATION.configure();
    }
    
    public void addLayers(LayersList layers) {
    }
    
    public void startAnimation(String layer, ConfiguredAnimation cfg) {
        animationSystem.startAnimationAt(layer, cfg);
    }
    
    public void stopAnimation(String layer, float transitionTime) {
        startAnimation(layer, DefaultsHA.NULL_ANIM.configure()
                .transitionTime(transitionTime));
    }
    
    public final void startAnimation(String layer, IAnimationSource animation) {
        startAnimation(layer, animation.configure().transitionTime(0.3F));
    }
    
    public final void stopAnimation(String layer) {
        stopAnimation(layer, 2F);
    }
    
    public Vec3 getPassengerOffset() {
        return new Vec3(xPassengerOffset, yPassengerOffset, zPassengerOffset);
    }
    
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.goalSelector.tick();
        setYRot(getYHeadRot());
    }
    
    @Override
    public void checkDespawn() {
        this.noActionTime = 0;
    }
    
    public void setMaxHealth(float maxHealth) {
        final String name = "ST Max Health";
        AttributeInstance instance = getAttributes().getInstance(Attributes.MAX_HEALTH);
        instance.removeModifier(MAX_HEALTH_LOC);
        float before = this.getMaxHealth();
        instance.addPermanentModifier(new AttributeModifier(MAX_HEALTH_LOC,
                maxHealth - before,
                AttributeModifier.Operation.ADD_VALUE));
        this.setHealth(maxHealth);
        this.heal(maxHealth);
    }
    
    public void addMaxHealth(String modifierName, int health) {
        this.getAttributes().getInstance(Attributes.MAX_HEALTH)
                .addPermanentModifier(new AttributeModifier(MAX_HEALTH_LOC, health, AttributeModifier.Operation.ADD_VALUE));
        this.setHealth(this.getHealth());
    }
    
    @Override
    public AnimationSystem getAnimationSystem() {
        return animationSystem;
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        animationSystem.deserializeNBT(null, pCompound.getCompound("Animations"));
        super.readAdditionalSaveData(pCompound);
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.put("Animations", animationSystem.serializeNBT(registryAccess()));
        super.addAdditionalSaveData(pCompound);
    }
    
}
