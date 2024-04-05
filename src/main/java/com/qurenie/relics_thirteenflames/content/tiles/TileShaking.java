package com.qurenie.relics_thirteenflames.content.tiles;

import com.qurenie.relics_thirteenflames.content.blocks.BlockShaking;
import com.qurenie.relics_thirteenflames.net.PacketPlaySound;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.zeith.hammeranims.api.animation.interp.Query;
import org.zeith.hammerlib.api.io.NBTSerializable;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.net.properties.PropertyBlockState;
import org.zeith.hammerlib.net.properties.PropertyBool;
import org.zeith.hammerlib.net.properties.PropertyFloat;
import org.zeith.hammerlib.net.properties.PropertyString;
import org.zeith.hammerlib.tiles.TileSyncableTickable;
import org.zeith.hammerlib.util.java.DirectStorage;

public class TileShaking
        extends TileSyncableTickable
{
    @NBTSerializable("State")
    protected BlockState _state = Blocks.AIR.defaultBlockState();

    @NBTSerializable("Speed")
    protected float _speed = 1F;

    @NBTSerializable("Intensity")
    protected float _intensity = 1F;

    @NBTSerializable("PhysShift")
    protected boolean _physicallyShift;

    @NBTSerializable("Fragile")
    protected boolean _fragile;

    @NBTSerializable("Behavior")
    protected BlockShaking.ShakeBehavior _behavior = BlockShaking.BEHAVIOR_JUMP;

    // -- Properties for sync --
    protected final PropertyFloat speed = new PropertyFloat(DirectStorage.create(__ -> _speed = __, () -> _speed));
    protected final PropertyFloat intensity = new PropertyFloat(DirectStorage.create(__ -> _intensity = __, () -> _intensity));
    protected final PropertyBlockState state = new PropertyBlockState(DirectStorage.create(nv -> _state = nv == null ? Blocks.AIR.defaultBlockState() : nv, () -> _state));
    protected final PropertyBool physicallyShift = new PropertyBool(DirectStorage.create(nv -> _physicallyShift = nv, () -> _physicallyShift));
    protected final PropertyBool fragile = new PropertyBool(DirectStorage.create(nv -> _fragile = nv, () -> _fragile));
    protected final PropertyString behavior = new PropertyString(DirectStorage.create(__ -> _behavior = BlockShaking.getBehavior(__), () -> _behavior.id()));
    // -------------------------

    public TileShaking(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        dispatcher.registerProperty("state", this.state);
        dispatcher.registerProperty("behavior", behavior);
        dispatcher.registerProperty("speed", speed);
        dispatcher.registerProperty("intensity", intensity);
    }

    public void withBehavior(BlockShaking.ShakeConfiguration behavior)
    {
        this.behavior.set(behavior.behavior().id());
        this.speed.setFloat(behavior.speed());
        this.intensity.setFloat(behavior.intensity());
        this.physicallyShift.setBool(behavior.physicallyShift());
        this.fragile.setBool(behavior.fragile());
    }

    public final Query query = new Query();

    @Override
    public void update()
    {
        Vec3 prevOffset = getOffset(0F);
        VoxelShape prev = getBlock().getShape(level, worldPosition);

        query.anim_duration = query.anim_length = _behavior.duration();
        query.anim_time = ticksExisted * 0.05F * _speed;

        Vec3 curOffset = getOffset(1F);

        if(prev != null && !prev.isEmpty() && doPhysShift())
        {
            var prevAABB = prev.bounds().move(prevOffset).move(worldPosition);
            var shifted = curOffset.subtract(prevOffset);
            var findBounds = prevAABB.move(shifted);
            for(Entity ent : level.getEntitiesOfClass(Entity.class, findBounds))
                ent.move(MoverType.SHULKER_BOX, shifted);
        }

        if(_fragile && !level.isClientSide)
        {
            var prevAABB = prev.bounds().move(prevOffset).move(worldPosition);
            var shifted = curOffset.subtract(prevOffset);
            var findBounds = prevAABB.move(shifted).inflate(0.15);
            for(var ent : level.getEntitiesOfClass(Player.class, findBounds))
            {
                ent.hurt(ent.damageSources().inWall(), ent.getMaxHealth() * 0.9F);
                deform();
                return;
            }
        }

        if(query.anim_time >= query.anim_duration)
        {
            deform();
            return;
        }
    }

    public Vec3 getOffset(float partialTicks)
    {
        query.anim_time = Math.min((ticksExisted + partialTicks) * 0.05F * _speed, _behavior.duration());
        return _behavior.animation().get(query).scale(_intensity);
    }

    public void setBlock(BlockState state)
    {
        this.state.set(state);
    }

    public BlockState getBlock()
    {
        return state.get();
    }

    public void deform()
    {
        BlockPos dst = worldPosition;

        Vec3 vec3 = Vec3.atCenterOf(dst).add(getOffset(1F));
        if(_physicallyShift) dst = new BlockPos((int) vec3.x, (int) vec3.y, (int) vec3.z);

        // if fragile, we don't place the block back
        if(_fragile)
        {
            if(level instanceof ServerLevel sl)
            {
                BlockState st = getBlock();

                VoxelShape prev = st.getShape(level, worldPosition);
                if(prev.isEmpty()) prev = Block.box(0, 0, 0, 16, 16, 16);

                var prevAABB = prev.bounds().move(getOffset(1F)).move(worldPosition);

                var fx = new BlockParticleOption(
                        ParticleTypes.BLOCK,
                        st
                );

                ParticleHelper.spawnParticleAABB(level, fx, prevAABB, 500, 0.01);
                var sound = st.getBlock().getSoundType(st, level, worldPosition, null);
//                ScriptUses.playSoundAt(prevAABB.getCenter(), sound.getBreakSound(), SoundSource.BLOCKS, sound.getVolume(), sound.getPitch() * 0.8F)
//                        .run();
                Network.sendToAll(new PacketPlaySound(prevAABB.getCenter(), sound.getBreakSound(), SoundSource.BLOCKS, sound.getVolume(), sound.getPitch() * 0.8F));
                //level.playSound(null, new BlockPos(prevAABB.getCenter()), sound.getBreakSound(), SoundSource.BLOCKS, sound.getVolume(), sound.getPitch() * 0.8F);
            }

            level.removeBlock(worldPosition, true);
        } else
            level.setBlockAndUpdate(dst, _state != null ? _state : Blocks.AIR.defaultBlockState());

        if(!dst.equals(worldPosition))
            level.removeBlock(worldPosition, true);
    }

    public boolean doPhysShift()
    {
        return _physicallyShift;
    }

    @Override
    public AABB getRenderBoundingBox()
    {
        BlockState st = getBlock();

        VoxelShape prev = st.getShape(level, worldPosition);
        if(prev.isEmpty()) prev = Block.box(0, 0, 0, 16, 16, 16);

        return prev.bounds().move(getOffset(1F)).move(worldPosition);
    }
}
