package tech.arhan.randomswap.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import tech.arhan.randomswap.ClientOverlayHandler;

public class UpdateShowCountdownPacket {

  public UpdateShowCountdownPacket() {}

  public UpdateShowCountdownPacket(FriendlyByteBuf buf) {}

  public static void encode(UpdateShowCountdownPacket message, FriendlyByteBuf buffer) {}

  public boolean handle(Supplier<NetworkEvent.Context> context) {
    context.get().enqueueWork(() -> {
        ClientOverlayHandler.clientShowCountdownText = !ClientOverlayHandler.clientShowCountdownText;
        Component.literal("Random swap countdown text is now " + (ClientOverlayHandler.clientShowCountdownText ? "enabled" : "disabled"));
    });
    return true;
  }
}