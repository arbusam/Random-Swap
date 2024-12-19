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

import net.minecraft.network.chat.Component;
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

    private int ticksUntilNextSwap = 0;

    private static final Random RANDOM = new Random();

    private boolean countdownStarted = false;

    public randomswap()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        RandomSwapCommand.register(event.getDispatcher());
    }

    public void startCountdown() {
        double randomMinutes = RANDOM.nextDouble() * (RandomSwapDataStore.getMaxTime() - RandomSwapDataStore.getMinTime()) + RandomSwapDataStore.getMinTime();

        ticksUntilNextSwap = (int) (randomMinutes * 60 * TICKS_PER_SECOND);
    }

    @SubscribeEvent
    public void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (!countdownStarted) {
            if (RandomSwapDataStore.getCountdownStarted()) {
                countdownStarted = true;
                startCountdown();
            }
            else {
                return;
            }
        }
        if (countdownStarted) {
            if (!RandomSwapDataStore.getCountdownStarted()) {
                countdownStarted = false;
                return;
            }
        }
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
            ticksUntilNextSwap--;
            LOGGER.info("Ticks until next swap: " + ticksUntilNextSwap);

            if (ticksUntilNextSwap <= 0) {
                MinecraftServer server = event.getServer();
                if (server != null && RandomSwapDataStore.getPlayers().size() >= 2) {
                    Player player1 = RandomSwapDataStore.getPlayers().get(0);
                    Player player2 = RandomSwapDataStore.getPlayers().get(1);
                    swapItems(player1, player2);
                }
                startCountdown();
            }
        }
    }

    private void swapItems(Player player1, Player player2) {
        Inventory inventory1 = player1.getInventory();
        Inventory inventory2 = player2.getInventory();

        String id1;
        int count1;
        String id2;
        int count2;
        // Reads main inventory + hotbar
        boolean notEmpty = false;
        for (int i = 0; i < inventory1.items.size(); i++) {
            ItemStack itemStack = inventory1.getItem(i);
            if (!ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString().equals("minecraft:air")) {
                notEmpty = true;
                break;
            }
        }
        if (!notEmpty) {
            return;
        }
        notEmpty = false;
        for (int i = 0; i < inventory2.items.size(); i++) {
            ItemStack itemStack = inventory2.getItem(i);
            if (!ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString().equals("minecraft:air")) {
                notEmpty = true;
                break;
            }
        }
        if (!notEmpty) {
            return;
        }
        int randomID = RANDOM.nextInt(inventory1.items.size());
        ItemStack itemStack = inventory1.getItem(randomID);
        while (ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString().equals("minecraft:air")) {
            randomID = RANDOM.nextInt(inventory1.items.size());
            itemStack = inventory1.getItem(randomID);
        }
        // Count: itemStack.getCount()
        // Item name: itemStack.getItem().getName(itemStack).getString()
        // Item ID: ForgeRegistries.ITEMS.getKey(itemStack.getItem())
        // LOGGER.info("Slot " + i + ": " + itemStack.getCount() + "x " + itemStack.getItem().getName(itemStack).getString() + " (" + ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString() + ")");
        int randomID2 = RANDOM.nextInt(inventory2.items.size());
        ItemStack itemStack2 = inventory2.getItem(randomID2);
        while (ForgeRegistries.ITEMS.getKey(itemStack2.getItem()).toString().equals("minecraft:air")) {
            randomID2 = RANDOM.nextInt(inventory2.items.size());
            itemStack2 = inventory2.getItem(randomID2);
        }

        // Swap items
        inventory1.setItem(randomID, itemStack2);
        inventory2.setItem(randomID2, itemStack);

        if (RandomSwapDataStore.getShowLostItem()) {
            player1.displayClientMessage(Component.literal("You lost: " + itemStack.getCount() + "x " + itemStack.getItem().getName(itemStack).getString()), false);
            player2.displayClientMessage(Component.literal("You lost: " + itemStack2.getCount() + "x " + itemStack2.getItem().getName(itemStack2).getString()), false);
        }

        if (RandomSwapDataStore.getShowGainedItem()) {
            player1.displayClientMessage(Component.literal("You gained: " + itemStack2.getCount() + "x " + itemStack2.getItem().getName(itemStack2).getString()), false);
            player2.displayClientMessage(Component.literal("You gained: " + itemStack.getCount() + "x " + itemStack.getItem().getName(itemStack).getString()), false);
        }
    }
}
