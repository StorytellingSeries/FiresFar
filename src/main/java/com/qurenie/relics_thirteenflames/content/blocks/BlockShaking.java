package com.qurenie.relics_thirteenflames.content.blocks;

import com.mojang.serialization.MapCodec;
import com.qurenie.relics_thirteenflames.content.tiles.TileShaking;
import com.qurenie.relics_thirteenflames.init.BlocksRegistry;
import com.qurenie.relics_thirteenflames.init.TilesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.zeith.hammeranims.api.animation.interp.DoubleInterpolation;
import org.zeith.hammeranims.api.animation.interp.InterpolatedDouble;
import org.zeith.hammeranims.api.animation.interp.Vec3Animation;
import org.zeith.hammerlib.api.blocks.INoItemBlock;
import org.zeith.hammerlib.api.forge.BlockAPI;
import org.zeith.hammerlib.api.io.IAutoNBTSerializable;
import org.zeith.hammerlib.api.io.NBTSerializable;
import org.zeith.hammerlib.api.io.serializers.INBTSerializer;
import org.zeith.hammerlib.util.java.Cast;

import java.util.HashMap;

import static net.minecraft.world.level.block.Blocks.MOVING_PISTON;

public class BlockShaking extends BaseEntityBlock implements INoItemBlock
{
    protected static final HashMap<String, ShakeBehavior> REGISTRY = new HashMap<>();

    public static final ShakeBehavior BEHAVIOR_JUMP = register(new ShakeBehavior("jump", 1F,
            new Vec3Animation(new DoubleInterpolation(
                    InterpolatedDouble.constant(0),
                    InterpolatedDouble.parse("math.sin(query.anim_time * 180)"),
                    InterpolatedDouble.constant(0)
            ))
    ));

    public static final ShakeBehavior BEHAVIOR_RAISE = register(new ShakeBehavior("raise", 1F,
            new Vec3Animation(new DoubleInterpolation(
                    InterpolatedDouble.constant(0),
                    InterpolatedDouble.parse("-math.cos(query.anim_time * 90)"),
                    InterpolatedDouble.constant(0)
            ))
    ));
    
    public static final MapCodec<BlockShaking> CODEC = simpleCodec(BlockShaking::new);

    public BlockShaking(Properties pProperties)
    {
        super(pProperties.lightLevel(state -> state.getValue(BlockStateProperties.POWER)).noOcclusion().dynamicShape());
    }
    
    @Override
    public @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    
    @Override
    public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pReader, BlockPos pPos)
    {
        var te = Cast.cast(pReader.getBlockEntity(pPos), TileShaking.class);
        if(te != null) return te.getBlock().getBlockSupportShape(pReader, pPos);
        return super.getBlockSupportShape(pState, pReader, pPos);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos)
    {
        var te = Cast.cast(pLevel.getBlockEntity(pPos), TileShaking.class);
        if(te != null)
        {
            var box = te.getBlock().getInteractionShape(pLevel, pPos);
            if(te.doPhysShift())
            {
                Vec3 offset = te.getOffset(0.5F);
                return box.move(offset.x, offset.y, offset.z);
            }
            return box;
        }
        return super.getInteractionShape(pState, pLevel, pPos);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos)
    {
        var te = Cast.cast(pLevel.getBlockEntity(pPos), TileShaking.class);
        if(te != null) return te.getBlock().getOcclusionShape(pLevel, pPos);
        return super.getOcclusionShape(pState, pLevel, pPos);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        var te = Cast.cast(pLevel.getBlockEntity(pPos), TileShaking.class);
        if(te != null)
        {
            var box = te.getBlock().getCollisionShape(pLevel, pPos, pContext);
            if(te.doPhysShift())
            {
                Vec3 offset = te.getOffset(0.5F);
                return box.move(offset.x, offset.y, offset.z);
            }
            return box;
        }
        return super.getCollisionShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public VoxelShape getVisualShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        var te = Cast.cast(pLevel.getBlockEntity(pPos), TileShaking.class);
        if(te != null)
        {
            var box = te.getBlock().getVisualShape(pLevel, pPos, pContext);
            if(box != null && te.doPhysShift())
            {
                Vec3 offset = te.getOffset(0.5F);
                return box.move(offset.x, offset.y, offset.z);
            }
            return box;
        }
        return super.getVisualShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        var te = Cast.cast(pLevel.getBlockEntity(pPos), TileShaking.class);
        if(te != null)
        {
            var box = te.getBlock().getShape(pLevel, pPos, pContext);
            if(box != null && te.doPhysShift())
            {
                Vec3 offset = te.getOffset(0.5F);
                return box.move(offset.x, offset.y, offset.z);
            }
            return box;
        }
        return super.getShape(pState, pLevel, pPos, pContext);
    }

    public static void shake(Level level, BlockPos pos, ShakeConfiguration config)
    {
        if(level.getBlockEntity(pos) != null)
            return;
        var state = level.getBlockState(pos);
        shake(state, level, pos, config);
    }

    public static void shake(BlockState state, Level level, BlockPos pos, ShakeConfiguration config)
    {
        if (state.getDestroySpeed(level, pos) == -1.0F || state.liquid())
            return;
        
        int light = level.getLightEmission(pos);
        level.setBlockAndUpdate(pos, BlocksRegistry.SHAKING.defaultBlockState()
                .setValue(BlockStateProperties.POWER, Mth.clamp(light, 0, 15)));
        var shaker = Cast.cast(level.getBlockEntity(pos), TileShaking.class);
        if(shaker != null)
        {
            shaker.setBlock(state);
            shaker.withBehavior(config);
        }
    }

    public static void stabilize(Level level, BlockPos pos)
    {
        Cast.optionally(level.getBlockEntity(pos), TileShaking.class)
                .ifPresent(TileShaking::deform);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState)
    {
        return TilesRegistry.SHAKING.create(pPos, pState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(BlockStateProperties.POWER);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType)
    {
        return BlockAPI.ticker(pLevel);
    }

    public static ShakeBehavior register(ShakeBehavior behavior)
    {
        REGISTRY.put(behavior.id(), behavior);
        return behavior;
    }

    public static ShakeBehavior getBehavior(String id)
    {
        return REGISTRY.getOrDefault(id, BEHAVIOR_JUMP);
    }
    
    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        var te = Cast.cast(level.getBlockEntity(pos), TileShaking.class);
        if(te != null) return te.getBlock().getCloneItemStack(target, level, pos, player);
        return ItemStack.EMPTY;
    }

    public record ShakeConfiguration(ShakeBehavior behavior, float speed, float intensity, boolean physicallyShift, boolean fragile)
    {
        public ShakeConfiguration(ShakeBehavior behavior, float speed, float intensity)
        {
            this(behavior, Mth.clamp(speed, 0.00001F, 1000000F), intensity, false, false);
        }

        public ShakeConfiguration(ShakeBehavior behavior, float speed, float intensity, boolean physicallyShift)
        {
            this(behavior, Mth.clamp(speed, 0.00001F, 1000000F), intensity, physicallyShift, false);
        }

        public ShakeConfiguration(ShakeBehavior behavior)
        {
            this(behavior, 1F, 1F);
        }
    }

    public record ShakeBehavior(String id,
                                float duration,
                                Vec3Animation animation) {
        public ShakeConfiguration normal()
        {
            return new ShakeConfiguration(this);
        }
        
        public void save(CompoundTag tag, HolderLookup.@NotNull Provider provider) {
            tag.putString("ShakeBehaviour", id);
        }
        
        public static ShakeBehavior create(CompoundTag tag, HolderLookup.Provider provider) {
            return REGISTRY.get(tag.getString("ShakeBehaviour"));
        }
        
    }
}
