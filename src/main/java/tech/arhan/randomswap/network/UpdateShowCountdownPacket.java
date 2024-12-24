package tech.arhan.randomswap.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import tech.arhan.randomswap.ClientOverlayHandler;

public class UpdateShowCountdownPacket {
  private final boolean showCountdownText;

  public UpdateShowCountdownPacket(boolean showCountdownText) {
    this.showCountdownText = showCountdownText;
  }

  public UpdateShowCountdownPacket(FriendlyByteBuf buf) {
    this.showCountdownText = buf.readBoolean();
  }

  public static void encode(UpdateShowCountdownPacket message, FriendlyByteBuf buffer) {
    buffer.writeBoolean(message.showCountdownText);
  }

  public boolean handle(Supplier<NetworkEvent.Context> context) {
    context.get().enqueueWork(() -> {
        ClientOverlayHandler.clientShowCountdownText = this.showCountdownText;
        Component.literal("Random swap countdown text is now " + (ClientOverlayHandler.clientShowCountdownText ? "enabled" : "disabled"));
    });
    return true;
  }
}