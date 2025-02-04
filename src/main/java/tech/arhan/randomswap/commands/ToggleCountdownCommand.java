package tech.arhan.randomswap.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.network.PacketDistributor;
import tech.arhan.randomswap.network.NetworkHandler;
import tech.arhan.randomswap.network.UpdateShowCountdownPacket;

public class ToggleCountdownCommand {
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(Commands.literal("RandomSwapToggleCountdown")
      .requires(source -> source.hasPermission(0))
      .executes(context -> {
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> context.getSource().getPlayer()), new UpdateShowCountdownPacket());
        return 1;
      }));
  }
}
