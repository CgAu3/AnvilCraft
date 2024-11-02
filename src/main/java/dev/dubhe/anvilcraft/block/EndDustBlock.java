package dev.dubhe.anvilcraft.block;

import dev.dubhe.anvilcraft.entity.FloatingBlockEntity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EndDustBlock extends Block {
    public EndDustBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(
        BlockState state,
        Level level,
        BlockPos pos,
        BlockState oldState,
        boolean movedByPiston
    ) {
        level.scheduleTick(pos, this, this.getDelayAfterPlace());
    }

    @Override
    public void tick(
        BlockState state,
        ServerLevel level,
        BlockPos pos,
        RandomSource random
    ) {
        if (!level.getFluidState(pos.above()).is(FluidTags.WATER)) return;
        if (!FallingBlock.isFree(level.getBlockState(pos.above()))) return;
        FloatingBlockEntity._float(level, pos, state);
    }

    @Override
    public void neighborChanged(
        BlockState state,
        @Nonnull Level level,
        BlockPos pos,
        Block neighborBlock,
        BlockPos neighborPos,
        boolean movedByPiston
    ) {
        if (level.getFluidState(neighborPos).is(FluidTags.WATER)) {
            level.scheduleTick(pos, this, this.getDelayAfterPlace());
        }
    }

    protected int getDelayAfterPlace() {
        return 2;
    }
}