package dev.dubhe.anvilcraft.mixin;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.block.MagnetBlock;
import dev.dubhe.anvilcraft.init.ModBlockTags;
import dev.dubhe.anvilcraft.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.dubhe.anvilcraft.block.MagnetBlock.LIT;

@Mixin(AnvilBlock.class)
public abstract class AnvilBlockMixin extends FallingBlock {
    public AnvilBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public void tick(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, RandomSource random) {
        if (isAttracts(level.getBlockState(pos.above())) || !FallingBlock.isFree(level.getBlockState(pos.below())) || pos.getY() < level.getMinBuildHeight()) {
            return;
        }
        FallingBlockEntity fallingBlockEntity = FallingBlockEntity.fall(level, pos, state);
        this.falling(fallingBlockEntity);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        this.wasAttracted(state, level, pos);
    }

    @Unique
    private boolean isAttracts(@NotNull BlockState state) {
        return state.is(ModBlockTags.MAGNET) && !state.getValue(LIT);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        this.wasAttracted(state, level, pos);
    }

    @Unique
    private void wasAttracted(BlockState state, @NotNull Level level, @NotNull BlockPos anvil) {
        BlockPos magnet = anvil;
        if (level.getBlockState(anvil.above()).is(ModBlockTags.MAGNET)) return;
        int distance = AnvilCraft.config.magnetAttractsDistance;
        for (int i = 0; i < distance; i++) {
            magnet = magnet.above();
            BlockState state1 = level.getBlockState(magnet);
            if (!(state1.getBlock() instanceof MagnetBlock) || state1.getValue(LIT)) {
                if (state1.is(Blocks.AIR) || state1.is(Blocks.CAVE_AIR) || state1.is(Blocks.VOID_AIR)) continue;
                else return;
            }
            BlockState state2 = level.getBlockState(magnet.below());
            if (!state2.is(Blocks.AIR) && !state2.is(Blocks.CAVE_AIR) && !state2.is(Blocks.VOID_AIR)) return;
            level.setBlock(magnet.below(), state, 3);
            level.setBlock(anvil, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @Inject(method = "damage", at = @At("RETURN"), cancellable = true)
    private static void damage(@NotNull BlockState state, @NotNull CallbackInfoReturnable<BlockState> cir) {
        if (cir.getReturnValue() != null) return;
        if (state.is(ModBlocks.ROYAL_ANVIL)) cir.setReturnValue(state);
    }
}
