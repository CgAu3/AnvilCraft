package dev.dubhe.anvilcraft.event.giantanvil;

import dev.anvilcraft.lib.event.SubscribeEvent;
import dev.dubhe.anvilcraft.api.event.entity.GiantAnvilFallOnLandEvent;
import dev.dubhe.anvilcraft.api.recipe.AnvilRecipeManager;
import dev.dubhe.anvilcraft.data.recipe.anvil.AnvilCraftingContainer;
import dev.dubhe.anvilcraft.data.recipe.anvil.AnvilRecipe;
import dev.dubhe.anvilcraft.data.recipe.anvil.AnvilRecipeType;
import dev.dubhe.anvilcraft.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GiantAnvilLandingEventListener {
    private static final List<ShockBehaviorDefinition> behaviorDefs = new ArrayList<>();
    public static final Direction[] HORIZONTAL_DIRECTIONS =
            new Direction[]{Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.NORTH};
    public static final Direction[] VERTICAL_DIRECTIONS =
            new Direction[]{Direction.UP, Direction.DOWN};
    public static final Direction[][] CORNER_DIRECTIONS =
            new Direction[][]{
                    {Direction.EAST, Direction.NORTH}, {Direction.EAST, Direction.SOUTH},
                    {Direction.WEST, Direction.NORTH}, {Direction.WEST, Direction.SOUTH},
            };

    static {
        behaviorDefs.add(new ShockBehaviorDefinition.MatchAll((blockPosList, level) -> {
            for (BlockPos pos : blockPosList) {
                BlockState state = level.getBlockState(pos);
                if (state.is(BlockTags.LEAVES)
                        || state.is(BlockTags.FLOWERS)
                        || state.is(Blocks.RED_MUSHROOM)
                        || state.canBeReplaced()
                        || state.is(Blocks.BROWN_MUSHROOM)
                        || state.is(BlockTags.SNOW)
                        || state.is(BlockTags.ICE)
                ) {
                    LootParams.Builder builder = new LootParams.Builder((ServerLevel) level)
                            .withParameter(LootContextParams.ORIGIN, pos.getCenter());
                    if (state.is(BlockTags.SNOW)) {
                        builder.withParameter(LootContextParams.TOOL, Items.DIAMOND_SHOVEL.getDefaultInstance());
                    }
                    for (ItemStack drop : state.getDrops(builder)) {
                        System.out.println("drop = " + drop);
                        ItemEntity itemEntity = new ItemEntity(
                                level,
                                pos.getX() + 0.5,
                                pos.getY() + 0.5,
                                pos.getZ() + 0.5,
                                drop
                        );
                        level.addFreshEntity(itemEntity);
                        state.spawnAfterBreak((ServerLevel) level, pos, ItemStack.EMPTY, true);
                    }
                    level.destroyBlock(pos, false);
                }
                if (state.is(BlockTags.LOGS)) {
                    removeLeaves(pos, level, false);
                }
            }
        }));
        behaviorDefs.add(new ShockBehaviorDefinition.SimpleTag(
                BlockTags.WOOL,
                (blockPosList, level) -> {
                    for (BlockPos pos : blockPosList) {
                        BlockState state = level.getBlockState(pos);
                        if (state.is(BlockTags.LEAVES)
                                || state.is(BlockTags.FLOWERS)
                                || state.is(Blocks.RED_MUSHROOM)
                                || state.canBeReplaced()
                                || state.is(Blocks.BROWN_MUSHROOM)
                                || state.is(BlockTags.SNOW)
                                || state.is(BlockTags.ICE)
                        ) {
                            level.destroyBlock(pos, false);
                            ItemEntity itemEntity = new ItemEntity(
                                    level,
                                    pos.getX() + 0.5,
                                    pos.getY() + 0.5,
                                    pos.getZ() + 0.5,
                                    state.getBlock().asItem().getDefaultInstance()
                            );
                            level.addFreshEntity(itemEntity);
                        }
                        if (state.is(BlockTags.LOGS)) {
                            removeLeaves(pos, level, true);
                        }
                    }
                }
        ));
        behaviorDefs.add(new ShockBehaviorDefinition.SimpleTag(BlockTags.LOGS,
                (blockPosList, level) -> {
                    for (BlockPos pos : blockPosList) {
                        BlockState state = level.getBlockState(pos);
                        if (state.is(Blocks.SUGAR_CANE)
                                || state.is(Blocks.BAMBOO)
                                || state.is(Blocks.KELP)
                                || state.is(Blocks.CACTUS)
                        ) {
                            level.destroyBlock(pos, true);
                        }
                        if (state.is(BlockTags.LOGS)) {
                            BlockPos.breadthFirstTraversal(
                                    pos,
                                    Integer.MAX_VALUE,
                                    1024,
                                    (blockPos, blockPosConsumer) -> {
                                        for (Direction direction : Direction.values()) {
                                            blockPosConsumer.accept(blockPos.relative(direction));
                                        }
                                        for (Direction horizontal : HORIZONTAL_DIRECTIONS) {
                                            for (Direction vertical : VERTICAL_DIRECTIONS) {
                                                blockPosConsumer.accept(blockPos
                                                        .relative(horizontal)
                                                        .relative(vertical)
                                                );
                                            }
                                        }
                                        for (Direction[] corner : CORNER_DIRECTIONS) {
                                            BlockPos pos1 = blockPos;
                                            for (Direction direction : corner) {
                                                pos1 = pos1.relative(direction);
                                            }
                                            for (Direction verticalDirection : VERTICAL_DIRECTIONS) {
                                                pos1 = pos1.relative(verticalDirection);
                                                blockPosConsumer.accept(pos1);
                                            }
                                        }

                                    }, blockPos -> {
                                        if (blockPos.getY() < pos.getY()) return false;
                                        BlockState blockState = level.getBlockState(blockPos);
                                        if (blockState.is(BlockTags.LOGS)
                                                || blockState.is(BlockTags.LEAVES)
                                                || blockState.is(Blocks.MANGROVE_ROOTS)
                                                || blockState.is(Blocks.MUSHROOM_STEM)
                                                || blockState.is(Blocks.BROWN_MUSHROOM_BLOCK)
                                                || blockState.is(Blocks.RED_MUSHROOM_BLOCK)
                                                || blockState.is(BlockTags.WART_BLOCKS)
                                                || blockState.is(Blocks.SHROOMLIGHT)
                                        ) {
                                            level.destroyBlock(blockPos, true);
                                            return true;
                                        }
                                        return false;
                                    }
                            );
                        }
                    }

                })
        );
        behaviorDefs.add(new ShockBehaviorDefinition.SimpleBlock(Blocks.HAY_BLOCK,
                (blockPosList, level) -> {
                    for (BlockPos pos : blockPosList) {
                        BlockState state = level.getBlockState(pos);
                        if (state.getBlock() instanceof CropBlock cropBlock) {
                            if (cropBlock.isMaxAge(state)) {
                                level.destroyBlock(pos, true);
                                level.setBlockAndUpdate(pos, cropBlock.getStateForAge(0));
                            }
                        }
                    }
                })
        );
        behaviorDefs.add(new ShockBehaviorDefinition.SimpleBlock(Blocks.ANVIL,
                (blockPosList, level) -> {
                    for (BlockPos pos : blockPosList) {
                        AnvilCraftingContainer container = new AnvilCraftingContainer(level, pos, null);
                        Optional<AnvilRecipe> optional = AnvilRecipeManager.getAnvilRecipeList().stream()
                                .filter(recipe ->
                                        recipe.getAnvilRecipeType() == AnvilRecipeType.BLOCK_SMASH
                                                && recipe.matches(container, level)
                                ).findFirst();
                        if (optional.isPresent()) {
                            AnvilRecipe recipe = optional.get();
                            recipe.craft(container);
                        }
                    }
                })
        );

    }

    private static void removeLeaves(BlockPos pos, Level level, boolean silkTouch) {
        BlockPos.breadthFirstTraversal(
                pos,
                Integer.MAX_VALUE,
                1024,
                (blockPos, blockPosConsumer) -> {
                    for (Direction direction : Direction.values()) {
                        blockPosConsumer.accept(blockPos.relative(direction));
                    }
                }, blockPos -> {
                    if (blockPos.getY() < pos.getY()) return false;
                    BlockState blockState = level.getBlockState(blockPos);
                    if (
                            blockState.is(BlockTags.LOGS)
                                    || blockState.is(BlockTags.LEAVES)
                                    || blockState.is(Blocks.MUSHROOM_STEM)
                                    || blockState.is(Blocks.BROWN_MUSHROOM_BLOCK)
                                    || blockState.is(Blocks.RED_MUSHROOM_BLOCK)
                                    || blockState.is(BlockTags.WART_BLOCKS)
                    ) {
                        if (!blockState.is(BlockTags.LOGS)) {
                            if (!silkTouch) {
                                LootParams.Builder builder = (new LootParams.Builder((ServerLevel) level))
                                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                                        .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);
                                for (ItemStack drop : blockState.getDrops(builder)) {
                                    ItemEntity itemEntity = new ItemEntity(
                                            level,
                                            pos.getX() + 0.5,
                                            pos.getY() + 0.5,
                                            pos.getZ() + 0.5,
                                            drop
                                    );
                                    level.addFreshEntity(itemEntity);
                                }
                                level.destroyBlock(blockPos, false);
                            } else {
                                level.destroyBlock(blockPos, false);
                                ItemEntity itemEntity = new ItemEntity(
                                        level,
                                        blockPos.getX() + 0.5,
                                        blockPos.getY() + 0.5,
                                        blockPos.getZ() + 0.5,
                                        blockState.getBlock().asItem().getDefaultInstance()
                                );
                                level.addFreshEntity(itemEntity);
                            }
                        }
                        return true;
                    }
                    return false;
                }
        );
    }


    /**
     * 撼地
     */
    @SubscribeEvent
    public void onLand(@NotNull GiantAnvilFallOnLandEvent event) {
        BlockPos groundPos = event.getPos().below(2);
        if (isValidShockBaseBlock(groundPos, event.getLevel())) {
            Optional<ShockBehaviorDefinition> definitionOpt = behaviorDefs.stream()
                    .filter(it -> it.cornerMatches(groundPos, event.getLevel()))
                    .min((a, b) -> b.priority() - a.priority());
            if (definitionOpt.isEmpty())
                return;
            final ShockBehaviorDefinition def = definitionOpt.get();
            int radius = (int) Math.ceil(event.getFallDistance());
            BlockPos ground = groundPos.above();
            AABB aabb = AABB.ofSize(Vec3.atCenterOf(ground), radius * 2 + 1, 1, radius * 2 + 1);
            List<LivingEntity> e = event.getLevel().getEntitiesOfClass(LivingEntity.class, aabb);
            for (LivingEntity l : e) {
                if (l.getItemBySlot(EquipmentSlot.FEET).is(Items.AIR)) {
                    l.hurt(event.getLevel().damageSources().fall(), event.getFallDistance() * 2);
                }
            }
            ArrayList<BlockPos> posList = new ArrayList<>();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = ground.relative(Direction.Axis.X, dx)
                            .relative(Direction.Axis.Z, dz);
                    posList.add(pos);
                }
            }
            def.acceptRanges(posList, event.getLevel());
        }
    }

    private boolean isValidShockBaseBlock(BlockPos centerPos, Level level) {
        BlockState blockState = level.getBlockState(centerPos);
        if (!blockState.is(ModBlocks.HEAVY_IRON_BLOCK.get())) {
            return false;
        }
        for (Direction direction : HORIZONTAL_DIRECTIONS) {
            if (!level.getBlockState(centerPos.relative(direction)).is(ModBlocks.HEAVY_IRON_BLOCK.get()))
                return false;
        }
        return true;
    }


}
