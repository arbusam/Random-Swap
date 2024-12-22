package tech.arhan.randomswap.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import tech.arhan.randomswap.RandomSwapDataStore;

public class RandomSwapCommand {
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(Commands.literal("RandomSwap")
      .requires(source -> source.hasPermission(3))
      .then(Commands.argument("playerTargetSelector", EntityArgument.players())
        .executes(context -> {
          if (RandomSwapDataStore.getCountdownStarted()) {
            context.getSource().sendSuccess(Component.literal("Random swap already started, run /RandomSwapCancel to cancel it."), true);
            return 0;
          }
          Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "playerTargetSelector");
          if (targets.size() < 2) {
            context.getSource().sendFailure(Component.literal("You must specify at least two players."));
            return 0;
          }
          RandomSwapDataStore.clearPlayers();
          RandomSwapDataStore.setPlayers(new ArrayList<>(targets));
          context.getSource().sendSuccess(Component.literal("Beginning Random Swap with: " + targets.size() + " players"), true);
          RandomSwapDataStore.setCountdownStarted(true);
          return 1;
        })
      )
      .then(Commands.argument("players", StringArgumentType.greedyString())
        .suggests((context, builder) -> {
          String remaining = builder.getRemaining().trim();
          String[] chosenPlayers = remaining.isEmpty() ? new String[0] : remaining.split("\\s+");
          String lastToken = chosenPlayers.length == 0 ? remaining : chosenPlayers[chosenPlayers.length - 1];
          String lastTokenLower = lastToken.toLowerCase();
          Set<String> alreadyChosen = new HashSet<>();
          for (String p : chosenPlayers) {
            alreadyChosen.add(p.toLowerCase());
          }
          MinecraftServer server = context.getSource().getServer();
          for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String playerName = player.getName().getString();
            if (!alreadyChosen.contains(playerName.toLowerCase()) && playerName.toLowerCase().startsWith(lastTokenLower)) {
              String prefix = remaining.endsWith(lastToken)
                ? remaining.substring(0, remaining.length() - lastToken.length())
                : remaining;
              String suggestion = prefix.isEmpty() ? playerName : (prefix.trim() + " " + playerName);
              builder.suggest(suggestion);
            }
          }
          return CompletableFuture.completedFuture(builder.build());
        })
        .executes(context -> {
          System.out.println("Test");
          if (RandomSwapDataStore.getCountdownStarted()) {
            context.getSource().sendSuccess(Component.literal("Random swap already started, run /RandomSwapCancel to cancel it."), true);
            return 0;
          }
          String playersStr = StringArgumentType.getString(context, "players").trim();
          if (playersStr.isEmpty()) {
            context.getSource().sendFailure(Component.literal("You must specify at least two players."));
            return 0;
          }
          String[] playerNames = playersStr.split("\\s+");
          if (playerNames.length < 2) {
            context.getSource().sendFailure(Component.literal("You must specify at least two players."));
            return 0;
          }
          MinecraftServer server = context.getSource().getServer();
          List<ServerPlayer> targets = new ArrayList<>();
          Set<String> usedNames = new HashSet<>();
          for (String playerName : playerNames) {
            if (!usedNames.add(playerName.toLowerCase())) {
              context.getSource().sendFailure(Component.literal("Player \"" + playerName + "\" is listed more than once."));
              return 0;
            }
            ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
            if (player == null) {
              context.getSource().sendFailure(Component.literal("Player \"" + playerName + "\" not found."));
              return 0;
            }
            targets.add(player);
          }
          RandomSwapDataStore.clearPlayers();
          for (ServerPlayer p : targets) {
            RandomSwapDataStore.addPlayer(p);
          }
          context.getSource().sendSuccess(Component.literal("Beginning Random Swap with: " + targets.size() + " players"), true);
          RandomSwapDataStore.setCountdownStarted(true);
          return 1;
        })
      )
    );

    dispatcher.register(Commands.literal("RandomSwapCancel")
      .requires(source -> source.hasPermission(3))
      .executes(context -> {
        if (RandomSwapDataStore.getCountdownStarted()) {
          RandomSwapDataStore.clearPlayers();
          RandomSwapDataStore.setCountdownStarted(false);
          context.getSource().sendSuccess(Component.literal("Random swap cancelled."), true);
          return 1;
        } else {
          context.getSource().sendSuccess(Component.literal("No random swap currently running to cancel."), true);
          return 0;
        }
      }));

    dispatcher.register(Commands.literal("RandomSwapMinTime")
      .requires(source -> source.hasPermission(3))
      .then(Commands.argument("minutes", FloatArgumentType.floatArg())
        .executes(context -> {
          float minutes = FloatArgumentType.getFloat(context, "minutes");
          RandomSwapDataStore.setMinTime(minutes);
          context.getSource().sendSuccess(
            Component.literal("Minimum swap time set to: " + minutes + " minutes"),
            true
          );
          return 1;
        })
      ));

    dispatcher.register(Commands.literal("RandomSwapMaxTime")
      .requires(source -> source.hasPermission(3))
      .then(Commands.argument("minutes", FloatArgumentType.floatArg())
        .executes(context -> {
          float minutes = FloatArgumentType.getFloat(context, "minutes");
          RandomSwapDataStore.setMaxTime(minutes);
          context.getSource().sendSuccess(
            Component.literal("Maximum swap time set to: " + minutes + " minutes"),
            true
          );
          return 1;
        })
      ));

    dispatcher.register(Commands.literal("RandomSwapShowLostItem")
      .requires(source -> source.hasPermission(3))
      .executes(context -> {
        RandomSwapDataStore.setShowLostItem(!RandomSwapDataStore.getShowLostItem());
        context.getSource().sendSuccess(
          Component.literal("Show lost item set to: " + RandomSwapDataStore.getShowLostItem()),
          true
        );
        return 1;
      }));

    dispatcher.register(Commands.literal("RandomSwapShowGainedItem")
      .requires(source -> source.hasPermission(3))
      .executes(context -> {
        RandomSwapDataStore.setShowGainedItem(!RandomSwapDataStore.getShowGainedItem());
        context.getSource().sendSuccess(
          Component.literal("Show gained item set to: " + RandomSwapDataStore.getShowGainedItem()),
          true
        );
        return 1;
      }));
  }
}
