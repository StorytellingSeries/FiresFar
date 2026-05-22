package com.qurenie.relics_thirteenflames.content.blocks;

import com.qurenie.relics_thirteenflames.style.ColorScheme;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import it.hurts.octostudios.octolib.util.OctoColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NitorBlock extends Block {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<NitorColor> COLOR = EnumProperty.create("color", NitorColor.class);

    public NitorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable PushReaction getPistonPushReaction(@NotNull BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    protected void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (!level.isClientSide) {
            // Планируем тик через 15-20 секунд (300-400 тиков)
            int delay = 300 + level.random.nextInt(100);
            level.scheduleTick(pos, this, delay);
        }
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        super.animateTick(state, level, pos, random);

        OctoColor color = state.getValue(COLOR).color;

        ParticleHelper.spawnParticles(level,
                ParticleHelper.constructSimpleSpark(FlamesUtils.spreadColor(color, level.getRandom()), 0.3f, 40, 0.95f).withGravity(-0.05f),
                pos.getCenter(), 2, 0.1, 0.1, 0.1, 0.005);

        ParticleHelper.spawnParticles(level,
                ParticleHelper.constructSmoke(FlamesUtils.spreadColor(color, level.getRandom()), 0.3f, 60, 0f).withGravity(-0.023f),
                pos.getCenter(), 2, 0.1, 0.1, 0.1, 0.0012);
    }

    @Override
    public void tick(@NotNull BlockState state, ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        level.removeBlock(pos, false);
    }

    @Override
    protected void spawnDestroyParticles(@NotNull Level level, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state) {
        OctoColor color = state.getValue(COLOR).color;

        ParticleHelper.spawnParticles(level,
                ParticleHelper.constructSmoke(FlamesUtils.spreadColor(color, level.getRandom()), 0.5f, 60, 0f).withGravity(-0.1f),
                pos.getCenter(), 10, 0.2, 0.2, 0.2, 0.01);
    }


    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.box(0.4, 0.4, 0.4, 0.8, 0.8, 0.8);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return state.getFluidState().isEmpty();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, COLOR);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected float getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected @NotNull BlockState updateShape(
            BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos
    ) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    public enum NitorColor implements StringRepresentable {
        FIRE (ColorScheme.BURN_COLOR),
        PURPLE (ColorScheme.PURPLE_COLOR),;

        final OctoColor color;

        NitorColor(OctoColor color) {
            this.color = color;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase();
        }
    }

}
