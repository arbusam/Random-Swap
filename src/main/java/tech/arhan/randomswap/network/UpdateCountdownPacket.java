package tech.arhan.randomswap.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import tech.arhan.randomswap.ClientOverlayHandler;

public class UpdateCountdownPacket {
  private final int seconds;

  public UpdateCountdownPacket(int seconds) {
    this.seconds = seconds;
  }

  public UpdateCountdownPacket(FriendlyByteBuf buf) {
    this.seconds = buf.readInt();
  }

  public static void encode(UpdateCountdownPacket message, FriendlyByteBuf buffer) {
    buffer.writeInt(message.seconds);
  }

  public boolean handle(Supplier<NetworkEvent.Context> context) {
    context.get().enqueueWork(() -> {
        ClientOverlayHandler.clientSecondsSinceLastSwap = this.seconds;
    });
    return true;
  }
}
