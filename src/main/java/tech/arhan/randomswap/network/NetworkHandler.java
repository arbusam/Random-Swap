package tech.arhan.randomswap.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import tech.arhan.randomswap.randomswap;

public class NetworkHandler {
  public static final String PROTOCOL_VERSION = "1";
  public static SimpleChannel INSTANCE;

  public static void register() {
    INSTANCE = NetworkRegistry.newSimpleChannel(
      new ResourceLocation(randomswap.MODID, "network"),
      () -> PROTOCOL_VERSION,
      PROTOCOL_VERSION::equals,
      PROTOCOL_VERSION::equals);
    int id = 0;
    INSTANCE.registerMessage(id++, UpdateCountdownPacket.class, UpdateCountdownPacket::encode, UpdateCountdownPacket::new, UpdateCountdownPacket::handle);
    INSTANCE.registerMessage(id++, UpdateShowCountdownPacket.class, UpdateShowCountdownPacket::encode, UpdateShowCountdownPacket::new, UpdateShowCountdownPacket::handle);
  }
}
