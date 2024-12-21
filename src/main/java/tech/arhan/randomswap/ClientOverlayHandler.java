package tech.arhan.randomswap;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = randomswap.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientOverlayHandler {
  public static int clientSecondsSinceLastSwap = 0;
  public static boolean clientShowCountdownText = true;

  @SubscribeEvent
  public static void onRenderCountdownText(RenderGuiOverlayEvent.Pre event) {
    if (!clientShowCountdownText) {
      return;
    }

    PoseStack poseStack = event.getPoseStack();
    Minecraft mc = Minecraft.getInstance();

    int x = 10;
    int y = 10;

    int totalSeconds = clientSecondsSinceLastSwap;
    
    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;

    String timeString = String.format("%02d:%02d", minutes, seconds);

    mc.font.draw(poseStack, "Time since last swap: " + timeString, x, y, 0xFFFFFF);
  }
}
