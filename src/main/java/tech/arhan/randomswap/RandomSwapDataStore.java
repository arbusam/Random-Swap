package tech.arhan.randomswap;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.player.Player;

public class RandomSwapDataStore {
  private static final List<Player> players = new ArrayList<Player>();
  private static float minTime = 0;
  private static float maxTime = 10;
  private static boolean showLostItem = true;
  private static boolean showGainedItem = true;
  private static boolean countdownStarted = false;

  public static void addPlayer(Player player) {
    players.add(player);
  }

  public static List<Player> getPlayers() {
    return players;
  }

  public static void clearPlayers() {
    players.clear();
  }

  public static void setMinTime(float minutes) {
    minTime = minutes;
  }

  public static float getMinTime() {
    return minTime;
  }

  public static void setMaxTime(float minutes) {
    maxTime = minutes;
  }

  public static float getMaxTime() {
    return maxTime;
  }

  public static void setShowLostItem(boolean show) {
    showLostItem = show;
  }

  public static boolean getShowLostItem() {
    return showLostItem;
  }

  public static void setShowGainedItem(boolean show) {
    showGainedItem = show;
  }

  public static boolean getShowGainedItem() {
    return showGainedItem;
  }

  public static void setCountdownStarted(boolean started) {
    countdownStarted = started;
  }

  public static boolean getCountdownStarted() {
    return countdownStarted;
  }
}
