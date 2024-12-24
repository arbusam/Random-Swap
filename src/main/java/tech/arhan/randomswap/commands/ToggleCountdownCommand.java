package tech.arhan.randomswap.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import tech.arhan.randomswap.network.NetworkHandler;
import tech.arhan.randomswap.network.UpdateShowCountdownPacket;

public class ToggleCountdownCommand {
  private static final Map<UUID, Boolean> showCountdownTextMap = new HashMap<>();

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(Commands.literal("RandomSwapToggleCountdown")
      .requires(source -> source.hasPermission(0))
      .executes(context -> {
        System.out.println("Toggling countdown text");
        ServerPlayer player = context.getSource().getPlayer();
        UUID playerUUID = player.getUUID();
        boolean currentValue = showCountdownTextMap.getOrDefault(playerUUID, true);
        boolean newValue = !currentValue;
        showCountdownTextMap.put(playerUUID, newValue);

        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new UpdateShowCountdownPacket(newValue));
        return 1;
      }));
  }
}
