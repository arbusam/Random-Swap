package tech.arhan.randomswap.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import tech.arhan.randomswap.ClientOverlayHandler;

public class UpdateShowCountdownPacket {
  private final boolean show;

  public UpdateShowCountdownPacket(boolean show) {
    this.show = show;
  }

  public UpdateShowCountdownPacket(FriendlyByteBuf buf) {
    this.show = buf.readBoolean();
  }

  public static void encode(UpdateShowCountdownPacket message, FriendlyByteBuf buffer) {
    buffer.writeBoolean(message.show);
  }

  public boolean handle(Supplier<NetworkEvent.Context> context) {
    context.get().enqueueWork(() -> {
        ClientOverlayHandler.clientShowCountdownText = this.show;
    });
    return true;
  }
}