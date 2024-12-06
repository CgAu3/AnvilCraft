package dev.dubhe.anvilcraft.event.forge;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.hammer.IHammerChangeable;
import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import dev.dubhe.anvilcraft.client.gui.screen.inventory.AnvilHammerScreen;
import dev.dubhe.anvilcraft.init.ModBlockTags;
import dev.dubhe.anvilcraft.item.AnvilHammerItem;
import dev.dubhe.anvilcraft.network.HammerUsePack;
import dev.dubhe.anvilcraft.util.StateUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientBlockEventListener {
    /**
     * 侦听右键方块事件
     *
     * @param event 右键方块事件
     */
    @SubscribeEvent
    public static void anvilHammerUse(@NotNull PlayerInteractEvent.RightClickBlock event) {
        InteractionHand hand = event.getHand();
        if (event.getEntity().getItemInHand(hand).getItem() instanceof AnvilHammerItem) {
            if (AnvilHammerItem.ableToUseAnvilHammer(event.getLevel(), event.getPos(), event.getEntity())) {
                Block b = event.getLevel().getBlockState(event.getPos()).getBlock();
                if (b instanceof IHammerRemovable
                    && !(b instanceof IHammerChangeable)
                    && !event.getEntity().isShiftKeyDown()
                ) {
                    return;
                }
                BlockState targetBlockState = event.getLevel().getBlockState(event.getPos());
                if (event.getLevel().isClientSide()) {
                    clientHandle(event, targetBlockState, hand);
                }
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }


    private static void clientHandle(
        PlayerInteractEvent.@NotNull RightClickBlock event,
        BlockState targetBlockState,
        InteractionHand hand
    ) {
        Property<?> property = AnvilHammerItem.findChangeableProperty(targetBlockState);
        if (!event.getEntity().isShiftKeyDown()
            && AnvilHammerItem.possibleToUseEnhancedHammerChange(targetBlockState)
            && property != null
        ) {
            if ((targetBlockState.getBlock() instanceof IHammerChangeable ihc
                && ihc.checkBlockState(targetBlockState))
                || (targetBlockState.is(ModBlockTags.HAMMER_CHANGEABLE))
                && event.getEntity().getAbilities().mayBuild
            ) {
                List<BlockState> possibleStates = StateUtil.findPossibleStatesForProperty(targetBlockState, property);
                if (possibleStates.isEmpty()) {
                    new HammerUsePack(event.getPos(), hand).send();
                } else {
                    Minecraft.getInstance().setScreen(new AnvilHammerScreen(
                        event.getPos(),
                        targetBlockState,
                        property,
                        possibleStates
                    ));
                }
            }
        } else {
            new HammerUsePack(event.getPos(), hand).send();
        }
    }
}
