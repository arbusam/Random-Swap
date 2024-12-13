package tech.arhan.randomswap;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tech.arhan.randomswap.commands.RandomSwapCommand;

import java.util.Random;

import org.slf4j.Logger;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


@Mod(randomswap.MODID)
public class randomswap
{
    public static final String MODID = "randomswap";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int TICKS_PER_SECOND = 20;

    private static final int MIN_INTERVAL_MINUTES = 1;
    private static final int MAX_INTERVAL_MINUTES = 1;

    private int ticksUntilNextSwap = 0;

    private static final Random RANDOM = new Random();

    public randomswap()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        RandomSwapCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // TODO: Make this only run when the command is run
        double randomMinutes = RANDOM.nextDouble() * (MAX_INTERVAL_MINUTES - MIN_INTERVAL_MINUTES) + MIN_INTERVAL_MINUTES;

        ticksUntilNextSwap = (int) (randomMinutes * 60 * TICKS_PER_SECOND);
    }

    @SubscribeEvent
    public void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
            ticksUntilNextSwap--;
            LOGGER.info("Ticks until next swap: " + ticksUntilNextSwap);

            if (ticksUntilNextSwap <= 0) {
                MinecraftServer server = event.getServer();
                if (server != null) {
                    Player player1 = RandomSwapDataStore.getPlayers().get(0);
                    Player player2 = RandomSwapDataStore.getPlayers().get(1);
                    swapItems(player1, player2);
                }
            }
        }
    }

    private void swapItems(Player player1, Player player2) {
        Inventory inventory1 = player1.getInventory();
            
        // Reads main inventory + hotbar
        for (int i = 0; i < inventory1.items.size(); i++) {
            ItemStack itemStack = inventory1.getItem(i);
            if (!itemStack.isEmpty()) {
                // Count: itemStack.getCount()
                // Item name: itemStack.getItem().getName(itemStack).getString()
                // Item ID: ForgeRegistries.ITEMS.getKey(itemStack.getItem())
                LOGGER.info("Slot " + i + ": " + itemStack.getCount() + "x " + itemStack.getItem().getName(itemStack).getString() + " (" + ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString() + ")");
            }
        }
    }
}
