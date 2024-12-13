package tech.arhan.randomswap.commands;

import java.util.Collection;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import tech.arhan.randomswap.RandomSwapDataStore;

public class RandomSwapCommand {
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(Commands.literal("RandomSwap")
      .then(Commands.argument("players", EntityArgument.players())
        .executes(context -> {
          Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
          
          RandomSwapDataStore.clearPlayers();

          for (ServerPlayer player : players) {
            RandomSwapDataStore.addPlayer(player);
          }

          context.getSource().sendSuccess(
            Component.literal("Beginning Random Swap with: " + players.size() + " players"),
            true
          );
          return 1;
        })
      )
    );
  }
}
