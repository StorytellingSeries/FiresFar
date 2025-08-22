package com.qurenie.relics_thirteenflames.content.tiles;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.qurenie.relics_thirteenflames.content.container.AuritekhBeaconMenu;
import com.qurenie.relics_thirteenflames.init.TilesRegistry;
import lombok.Getter;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.LockCode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AurithecBeaconBlockEntity extends BlockEntity implements MenuProvider, Nameable {
    
    /**
     * A list of effects that beacons can apply.
     */
    public static final List<List<Holder<MobEffect>>> BEACON_EFFECTS = List.of(
            List.of(MobEffects.MOVEMENT_SPEED, MobEffects.DIG_SPEED),
            List.of(MobEffects.DAMAGE_RESISTANCE, MobEffects.JUMP),
            List.of(MobEffects.DAMAGE_BOOST),
            List.of(MobEffects.REGENERATION)
    );
    private static final Set<Holder<MobEffect>> VALID_EFFECTS = BEACON_EFFECTS.stream().flatMap(Collection::stream).collect(Collectors.toSet());
    private static final Component DEFAULT_NAME = Component.translatable("container.beacon");
    /**
     * A list of beam segments for this beacon.
     */
    List<AurithecBeaconBlockEntity.BeaconBeamSection> beamSections = Lists.newArrayList();
    private List<AurithecBeaconBlockEntity.BeaconBeamSection> checkingBeamSections = Lists.newArrayList();
    /**
     * The number of levels of this beacon's pyramid.
     */
    int levels;
    private int lastCheckY;
    /**
     * The primary effect given by this beacon.
     */
    @Nullable
    Holder<MobEffect> primaryPower;
    /**
     * The secondary effect given by this beacon.
     */
    @Nullable
    Holder<MobEffect> secondaryPower;
    /**
     * The custom name for this beacon.
     */
    @Nullable
    private Component name;
    private LockCode lockKey = LockCode.NO_LOCK;
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int p_58711_) {
            return switch (p_58711_) {
                case 0 -> AurithecBeaconBlockEntity.this.levels;
                case 1 -> BeaconMenu.encodeEffect(AurithecBeaconBlockEntity.this.primaryPower);
                case 2 -> BeaconMenu.encodeEffect(AurithecBeaconBlockEntity.this.secondaryPower);
                default -> 0;
            };
        }
        
        @Override
        public void set(int p_58713_, int p_58714_) {
            switch (p_58713_) {
                case 0:
                    AurithecBeaconBlockEntity.this.levels = p_58714_;
                    break;
                case 1:
                    if (!AurithecBeaconBlockEntity.this.level.isClientSide && !AurithecBeaconBlockEntity.this.beamSections.isEmpty()) {
                        AurithecBeaconBlockEntity.playSound(AurithecBeaconBlockEntity.this.level, AurithecBeaconBlockEntity.this.worldPosition, SoundEvents.BEACON_POWER_SELECT);
                    }
                    
                    AurithecBeaconBlockEntity.this.primaryPower = AurithecBeaconBlockEntity.filterEffect(BeaconMenu.decodeEffect(p_58714_));
                    break;
                case 2:
                    AurithecBeaconBlockEntity.this.secondaryPower = AurithecBeaconBlockEntity.filterEffect(BeaconMenu.decodeEffect(p_58714_));
            }
        }
        
        @Override
        public int getCount() {
            return 3;
        }
    };
    
    @Nullable
    static Holder<MobEffect> filterEffect(@Nullable Holder<MobEffect> effect) {
        return VALID_EFFECTS.contains(effect) ? effect : null;
    }
    
    public AurithecBeaconBlockEntity(BlockPos pos, BlockState blockState) {
        super(TilesRegistry.AURITHEC_BEACON, pos, blockState);
    }
    
    public static void tick(Level level, BlockPos pos, BlockState state, AurithecBeaconBlockEntity blockEntity) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        BlockPos blockpos;
        if (blockEntity.lastCheckY < j) {
            blockpos = pos;
            blockEntity.checkingBeamSections = Lists.newArrayList();
            blockEntity.lastCheckY = pos.getY() - 1;
        } else {
            blockpos = new BlockPos(i, blockEntity.lastCheckY + 1, k);
        }
        
        AurithecBeaconBlockEntity.BeaconBeamSection beaconblockentity$beaconbeamsection = blockEntity.checkingBeamSections.isEmpty()
                ? null
                : blockEntity.checkingBeamSections.get(blockEntity.checkingBeamSections.size() - 1);
        int l = level.getHeight(Heightmap.Types.WORLD_SURFACE, i, k);
        
        for (int i1 = 0; i1 < 10 && blockpos.getY() <= l; i1++) {
            BlockState blockstate = level.getBlockState(blockpos);
            Integer j1 = blockstate.getBeaconColorMultiplier(level, blockpos, pos);
            if (j1 != null) {
                if (blockEntity.checkingBeamSections.size() <= 1) {
                    beaconblockentity$beaconbeamsection = new AurithecBeaconBlockEntity.BeaconBeamSection(j1);
                    blockEntity.checkingBeamSections.add(beaconblockentity$beaconbeamsection);
                } else if (beaconblockentity$beaconbeamsection != null) {
                    if (j1 == beaconblockentity$beaconbeamsection.color) {
                        beaconblockentity$beaconbeamsection.increaseHeight();
                    } else {
                        beaconblockentity$beaconbeamsection = new AurithecBeaconBlockEntity.BeaconBeamSection(
                                FastColor.ARGB32.average(beaconblockentity$beaconbeamsection.color, j1)
                        );
                        blockEntity.checkingBeamSections.add(beaconblockentity$beaconbeamsection);
                    }
                }
            } else {
                if (beaconblockentity$beaconbeamsection == null || blockstate.getLightBlock(level, blockpos) >= 15 && !blockstate.is(Blocks.BEDROCK)) {
                    blockEntity.checkingBeamSections.clear();
                    blockEntity.lastCheckY = l;
                    break;
                }
                
                beaconblockentity$beaconbeamsection.increaseHeight();
            }
            
            blockpos = blockpos.above();
            blockEntity.lastCheckY++;
        }
        
        int k1 = blockEntity.levels;
        if (level.getGameTime() % 80L == 0L) {
            if (!blockEntity.beamSections.isEmpty()) {
                blockEntity.levels = updateBase(level, i, j, k);
            }
            
            if (blockEntity.levels > 0 && !blockEntity.beamSections.isEmpty()) {
                applyEffects(level, pos, blockEntity.levels, blockEntity.primaryPower, blockEntity.secondaryPower);
                playSound(level, pos, SoundEvents.BEACON_AMBIENT);
            }
        }
        
        if (blockEntity.lastCheckY >= l) {
            blockEntity.lastCheckY = level.getMinBuildHeight() - 1;
            boolean flag = k1 > 0;
            blockEntity.beamSections = blockEntity.checkingBeamSections;
            if (!level.isClientSide) {
                boolean flag1 = blockEntity.levels > 0;
                if (!flag && flag1) {
                    playSound(level, pos, SoundEvents.BEACON_ACTIVATE);
                    
                    for (ServerPlayer serverplayer : level.getEntitiesOfClass(
                            ServerPlayer.class, new AABB(i, j, k, i, j - 4, k).inflate(10.0, 5.0, 10.0)
                    )) {
                        CriteriaTriggers.CONSTRUCT_BEACON.trigger(serverplayer, blockEntity.levels);
                    }
                } else if (flag && !flag1) {
                    playSound(level, pos, SoundEvents.BEACON_DEACTIVATE);
                }
            }
        }
    }
    
    private static int updateBase(Level level, int x, int y, int z) {
        int i = 0;
        
        for (int j = 1; j <= 4; i = j++) {
            int k = y - j;
            if (k < level.getMinBuildHeight()) {
                break;
            }
            
            boolean flag = true;
            
            for (int l = x - j; l <= x + j && flag; l++) {
                for (int i1 = z - j; i1 <= z + j; i1++) {
                    if (!level.getBlockState(new BlockPos(l, k, i1)).is(BlockTags.BEACON_BASE_BLOCKS)) {
                        flag = false;
                        break;
                    }
                }
            }
            
            if (!flag) {
                break;
            }
        }
        
        return i;
    }
    
    @Override
    public void setRemoved() {
        playSound(this.level, this.worldPosition, SoundEvents.BEACON_DEACTIVATE);
        super.setRemoved();
    }
    
    private static void applyEffects(
            Level level, BlockPos pos, int beaconLevel, @Nullable Holder<MobEffect> primaryEffect, @Nullable Holder<MobEffect> secondaryEffect
    ) {
        if (!level.isClientSide && primaryEffect != null) {
            double d0 = beaconLevel * 10 + 10;
            int i = 2;
            if (beaconLevel >= 4 && Objects.equals(primaryEffect, secondaryEffect)) {
                i = 3;
            }
            
            int j = (9 + beaconLevel * 2) * 20;
            AABB aabb = new AABB(pos).inflate(d0).expandTowards(0.0, level.getHeight(), 0.0);
            List<Player> list = level.getEntitiesOfClass(Player.class, aabb);
            
            for (Player player : list) {
                player.addEffect(new MobEffectInstance(primaryEffect, j, i, true, true));
            }
            
            if (beaconLevel >= 4 && !Objects.equals(primaryEffect, secondaryEffect) && secondaryEffect != null) {
                for (Player player1 : list) {
                    player1.addEffect(new MobEffectInstance(secondaryEffect, j, 2, true, true));
                }
            }
        }
    }
    
    public static void playSound(Level level, BlockPos pos, SoundEvent sound) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
    
    public @NotNull List<AurithecBeaconBlockEntity.BeaconBeamSection> getBeamSections() {
        return this.levels == 0 ? ImmutableList.of() : this.beamSections;
    }
    
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
    
    private static void storeEffect(CompoundTag tag, String key, @Nullable Holder<MobEffect> effect) {
        if (effect != null) {
            effect.unwrapKey().ifPresent(p_316401_ -> tag.putString(key, p_316401_.location().toString()));
        }
    }
    
    @Nullable
    private static Holder<MobEffect> loadEffect(CompoundTag tag, String key) {
        if (tag.contains(key, 8)) {
            ResourceLocation resourcelocation = ResourceLocation.tryParse(tag.getString(key));
            return resourcelocation == null ? null : BuiltInRegistries.MOB_EFFECT.getHolder(resourcelocation).map(AurithecBeaconBlockEntity::filterEffect).orElse(null);
        } else {
            return null;
        }
    }
    
    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.primaryPower = loadEffect(tag, "primary_effect");
        this.secondaryPower = loadEffect(tag, "secondary_effect");
        if (tag.contains("CustomName", 8)) {
            this.name = parseCustomNameSafe(tag.getString("CustomName"), registries);
        }
        
        this.lockKey = LockCode.fromTag(tag);
    }
    
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        storeEffect(tag, "primary_effect", this.primaryPower);
        storeEffect(tag, "secondary_effect", this.secondaryPower);
        tag.putInt("Levels", this.levels);
        if (this.name != null) {
            tag.putString("CustomName", Component.Serializer.toJson(this.name, registries));
        }
        
        this.lockKey.addToTag(tag);
    }
    
    /**
     * Sets the custom name for this beacon.
     */
    public void setCustomName(@Nullable Component name) {
        this.name = name;
    }
    
    @Nullable
    @Override
    public Component getCustomName() {
        return this.name;
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return BaseContainerBlockEntity.canUnlock(player, this.lockKey, this.getDisplayName())
                ? new AuritekhBeaconMenu(containerId, playerInventory, this.dataAccess, ContainerLevelAccess.create(this.level, this.getBlockPos()))
                : null;
    }
    
    @Override
    public @NotNull Component getDisplayName() {
        return this.getName();
    }
    
    @Override
    public @NotNull Component getName() {
        return this.name != null ? this.name : DEFAULT_NAME;
    }
    
    @Override
    protected void applyImplicitComponents(BlockEntity.@NotNull DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        this.name = componentInput.get(DataComponents.CUSTOM_NAME);
        this.lockKey = componentInput.getOrDefault(DataComponents.LOCK, LockCode.NO_LOCK);
    }
    
    @Override
    protected void collectImplicitComponents(DataComponentMap.@NotNull Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.CUSTOM_NAME, this.name);
        if (!this.lockKey.equals(LockCode.NO_LOCK)) {
            components.set(DataComponents.LOCK, this.lockKey);
        }
    }
    
    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove("CustomName");
        tag.remove("Lock");
    }
    
    @Override
    public void setLevel(@NotNull Level level) {
        super.setLevel(level);
        this.lastCheckY = level.getMinBuildHeight() - 1;
    }
    
    @Getter
    public static class BeaconBeamSection {
        /**
         * The colors of this section of a beacon beam, in RGB float format.
         */
        final int color;
        private int height;
        
        public BeaconBeamSection(int color) {
            this.color = color;
            this.height = 1;
        }
        
        protected void increaseHeight() {
            this.height++;
        }
        
    }
    
}
