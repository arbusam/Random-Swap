package tech.arhan.randomswap;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.player.Player;

public class RandomSwapDataStore {
  private static final List<Player> players = new ArrayList<Player>();

  public static void addPlayer(Player player) {
    players.add(player);
  }

  public static List<Player> getPlayers() {
    return players;
  }

  public static void clearPlayers() {
    players.clear();
  }
}
