package tech.arhan.randomswap.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;
import tech.arhan.randomswap.randomswap;
import tech.arhan.randomswap.network.NetworkHandler;
import tech.arhan.randomswap.network.UpdateShowCountdownPacket;

public class ToggleCountdownCommand {
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(Commands.literal("RandomSwapToggleCountdown")
      .requires(source -> source.hasPermission(0))
      .executes(context -> {
        randomswap.showCountdownText = !randomswap.showCountdownText;
        context.getSource().sendSuccess(Component.literal("Random swap countdown text is now " + (randomswap.showCountdownText ? "enabled" : "disabled")), false);
        NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new UpdateShowCountdownPacket(randomswap.showCountdownText));
        return 1;
      }));
  }
}
