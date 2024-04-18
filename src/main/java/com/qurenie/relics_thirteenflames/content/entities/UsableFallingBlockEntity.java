package com.qurenie.relics_thirteenflames.content.entities;

import com.mojang.logging.LogUtils;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import javax.annotation.Nullable;

public class UsableFallingBlockEntity extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();
    public int time;
    public boolean dropItem = true;
    private boolean cancelDrop;
    private boolean hurtEntities;
    private int fallDamageMax = 40;

    private float fallDamagePerDistance;
    @Nullable
    public CompoundTag blockData;

    private static final EntityDataAccessor<BlockState> BLOCK_STATE = SynchedEntityData.defineId(UsableFallingBlockEntity.class, EntityDataSerializers.BLOCK_STATE);

    public void setBlockState(@Nullable BlockState state) {
        this.entityData.set(BLOCK_STATE, state);
    }

    @Nullable
    public BlockState getBlockState() {
        return this.entityData.get(BLOCK_STATE);
    }

    protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(UsableFallingBlockEntity.class, EntityDataSerializers.BLOCK_POS);

    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(UsableFallingBlockEntity.class, EntityDataSerializers.INT);

    public void setLifeTime(int lifetime){
        this.getEntityData().set(LIFETIME, lifetime);
    }

    public int getLifeTime() {
        return this.getEntityData().get(LIFETIME);
    }

    public UsableFallingBlockEntity(EntityType<? extends UsableFallingBlockEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static UsableFallingBlockEntity createFalling(Level pLevel, BlockPos pos, BlockState pState) {
        UsableFallingBlockEntity ufbe = new UsableFallingBlockEntity(EntityRegistry.USABLE_FALLING, pLevel);
        ufbe.setBlockState(pState);
        ufbe.blocksBuilding = true;
        ufbe.setPos(pos.getX(), pos.getY(), pos.getZ());
        ufbe.setDeltaMovement(Vec3.ZERO);
        ufbe.xo = pos.getX();
        ufbe.yo = pos.getY();
        ufbe.zo = pos.getZ();
        ufbe.setStartPos(ufbe.blockPosition());
        return ufbe;
    }

    /**
     * Returns {@code true} if it's possible to attack this entity with an item.
     */
    @Override
    public boolean isAttackable() {
        return false;
    }

    public void setStartPos(BlockPos pStartPos) {
        this.entityData.set(DATA_START_POS, pStartPos);
    }

    public BlockPos getStartPos() {
        return this.entityData.get(DATA_START_POS);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_START_POS, BlockPos.ZERO);
        this.entityData.define(LIFETIME, 600);
        this.entityData.define(BLOCK_STATE, Blocks.AIR.defaultBlockState());
    }

    /**
     * Returns {@code true} if other Entities should be prevented from moving through this Entity.
     */
    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {
        BlockState blockState = getBlockState();
        if(blockState == null) return;
        if (blockState.isAir() || blockState.hasBlockEntity()) {
            this.discard();
        } else {
            Block block = blockState.getBlock();
            ++this.time;
            if (!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
            if (!this.level().isClientSide) {
//                if(random.nextBoolean()) ParticleHelper.spawnParticleEntity(new CircleTintData(new Color(85, 255, 0),
//                        0.1f,40, 0.91F, false), this, 1, 0.02);
                BlockPos blockpos = this.blockPosition();
                double d0 = this.getDeltaMovement().lengthSqr();

                if (!this.onGround() && this.getDeltaMovement().length() > 0) {
                    if (!this.level().isClientSide && (this.time > 100 && (blockpos.getY() <= this.level().getMinBuildHeight() || blockpos.getY() > this.level().getMaxBuildHeight()) || this.time > getLifeTime())) {
                        if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                            this.spawnAtLocation(block);
                        }
                        this.discard();
                    }
                } else {
                    BlockState blockstate = this.level().getBlockState(blockpos);
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
                    if (!blockstate.is(Blocks.MOVING_PISTON)) {
                        if (!this.cancelDrop) {
                            boolean canBeReplaced = blockstate.canBeReplaced(new DirectionalPlaceContext(this.level(), blockpos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                            boolean free = FallingBlock.isFree(this.level().getBlockState(blockpos.below()));
                            boolean canSurviveNoFree = blockState.canSurvive(this.level(), blockpos) && !free;
                            if (canBeReplaced) {
                                if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && this.level().getFluidState(blockpos).getType() == Fluids.WATER) {
                                    setBlockState(blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.TRUE));
                                    blockState = getBlockState();
                                }

                                if (this.level().setBlock(blockpos, blockState, 3)) {
                                    ((ServerLevel)this.level()).getChunkSource().chunkMap.broadcast(this, new ClientboundBlockUpdatePacket(blockpos, this.level().getBlockState(blockpos)));
                                    this.discard();

                                } else if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                    this.discard();
                                    this.spawnAtLocation(block);
                                }
                            } else {
                                this.discard();
                                if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                    this.spawnAtLocation(block);
                                }
                            }
                        } else {
                            this.discard();
                        }
                    }
                }
            }

            this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
        }
    }


    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        BlockState state = getBlockState();
        if (state != null)
            pCompound.put("BlockState", NbtUtils.writeBlockState(state));

        pCompound.putInt("Time", this.time);
        pCompound.putBoolean("DropItem", this.dropItem);
        pCompound.putInt("lifetime", getLifeTime());
        if (this.blockData != null) {
            pCompound.put("TileEntityData", this.blockData);
        }

    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        setBlockState(NbtUtils.readBlockState(this.getCommandSenderWorld().holderLookup(Registries.BLOCK), pCompound.getCompound("BlockState")));
        this.time = pCompound.getInt("Time");
        setLifeTime(pCompound.getInt("lifetime"));

        if (pCompound.contains("DropItem", 99)) {
            this.dropItem = pCompound.getBoolean("DropItem");
        }

        if (pCompound.contains("TileEntityData", 10)) {
            this.blockData = pCompound.getCompound("TileEntityData");
        }

    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }


    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
        this.setBlockState(Block.stateById(pPacket.getData()));
        this.blocksBuilding = true;
        double d0 = pPacket.getX();
        double d1 = pPacket.getY();
        double d2 = pPacket.getZ();
        this.setPos(d0, d1, d2);
        this.setStartPos(this.blockPosition());
    }
}
