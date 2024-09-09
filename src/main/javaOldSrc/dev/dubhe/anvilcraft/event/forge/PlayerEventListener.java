package dev.dubhe.anvilcraft.event.forge;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.event.entity.PlayerEvent;
import dev.dubhe.anvilcraft.api.recipe.AnvilRecipeManager;
import dev.dubhe.anvilcraft.event.TooltipEventListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;


@Mod.EventBusSubscriber(modid = AnvilCraft.MOD_ID)
public class PlayerEventListener {
    /**
     * @param event 玩家交互实体事件
     */
    @SubscribeEvent
    public static void useEntity(@NotNull PlayerInteractEvent.EntityInteract event) {
        PlayerEvent.UseEntity playerEvent = new PlayerEvent.UseEntity(
            event.getEntity(), event.getTarget(), event.getHand(), event.getLevel()
        );
        AnvilCraft.EVENT_BUS.post(playerEvent);
        InteractionResult result = playerEvent.getResult();
        if (result != InteractionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void itemTooltip(@NotNull ItemTooltipEvent event) {
        TooltipEventListener.addTooltip(event.getItemStack(), event.getToolTip());
    }

    /**
     * 服务器玩家登陆事件
     */
    @SubscribeEvent
    public static void layerLoggedInEvent(
            @NotNull net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event
    ) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
            PacketDistributor.sendToPlayer(serverPlayer, new ClientRecipeManagerSyncPack(AnvilRecipeManager.getAnvilRecipeList()));
    }
}