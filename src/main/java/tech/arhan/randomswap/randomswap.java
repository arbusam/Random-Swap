package tech.arhan.randomswap;

import org.apache.commons.lang3.tuple.Pair;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import tech.arhan.randomswap.commands.RandomSwapCommand;
import tech.arhan.randomswap.commands.ToggleCountdownCommand;
import tech.arhan.randomswap.network.NetworkHandler;
import tech.arhan.randomswap.network.UpdateCountdownPacket;

import java.util.Random;

import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


@Mod(randomswap.MODID)
public class randomswap
{
    public static final String MODID = "randomswap";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final int TICKS_PER_SECOND = 20;

    private int ticksUntilNextSwap = 0;
    public static int ticksSinceLastSwap = 0;

    private static final Random RANDOM = new Random();

    private boolean countdownStarted = false;

    private boolean swapping = false;

    public randomswap()
    {
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        NetworkHandler.register();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        RandomSwapCommand.register(event.getDispatcher());
        ToggleCountdownCommand.register(event.getDispatcher());
    }

    public void startCountdown() {
        double randomMinutes = RANDOM.nextDouble() * (RandomSwapDataStore.getMaxTime() - RandomSwapDataStore.getMinTime()) + RandomSwapDataStore.getMinTime();

        ticksUntilNextSwap = (int) (randomMinutes * 60 * TICKS_PER_SECOND);
        ticksSinceLastSwap = 0;
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() != null && event.getEntity() instanceof Player && RandomSwapDataStore.getPlayers().contains(event.getEntity())) {
            RandomSwapDataStore.setCountdownStarted(false);
            for (Player player : RandomSwapDataStore.getPlayers()) {
                player.displayClientMessage(Component.literal("One of the players logged out, random swap cancelled."), false);
            }
        }
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
            ticksSinceLastSwap++;
            int secondsSinceLastSwap = ticksSinceLastSwap / TICKS_PER_SECOND;
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new UpdateCountdownPacket(secondsSinceLastSwap));

            if (ticksUntilNextSwap <= 0 && !swapping) {
                swapping = true;
                MinecraftServer server = event.getServer();
                if (server != null && RandomSwapDataStore.getPlayers().size() == 2) {
                    Player player1 = RandomSwapDataStore.getPlayers().get(0);
                    Player player2 = RandomSwapDataStore.getPlayers().get(1);
                    swapItems(player1, player2);
                }
                else if (server != null && RandomSwapDataStore.getPlayers().size() > 2) {
                    int count = RandomSwapDataStore.getPlayers().size();
                    Random random = new Random();
                    int[] randomOrder = new int[count];

                    randomOrder = generateDerangement(count, random);

                    ItemStack[] lostItemStacks = new ItemStack[count];
                    ItemStack[] gainedItemStacks = new ItemStack[count];
                    int[] randomIDs = new int[count];
                    for (int i = 0; i < count; i ++) {
                        Player player1 = RandomSwapDataStore.getPlayers().get(randomOrder[i]);
                        Pair<Integer, ItemStack> pair = getRandomItemStack(player1.getInventory());
                        lostItemStacks[i] = pair.getRight();
                        randomIDs[i] = pair.getLeft();
                        gainedItemStacks[(i+1) % count] = lostItemStacks[i];
                    }
                    for (int i = 0; i < count; i ++) {
                        Player player = RandomSwapDataStore.getPlayers().get(i);
                        player.getInventory().setItem(randomIDs[i], gainedItemStacks[i]);
                        if (RandomSwapDataStore.getShowLostItem()) {
                            player.displayClientMessage(Component.literal("You lost: " + lostItemStacks[i].getCount() + "x " + lostItemStacks[i].getItem().getName(lostItemStacks[i]).getString()), false);
                        }
                        if (RandomSwapDataStore.getShowGainedItem()) {
                            player.displayClientMessage(Component.literal("You gained: " + gainedItemStacks[i].getCount() + "x " + gainedItemStacks[i].getItem().getName(gainedItemStacks[i]).getString()), false);
                        }
                    }
                }
                startCountdown();
                swapping = false;
            }
        }
    }

    private int[] generateDerangement(int count, Random random) {
        int[] derangedOrder = new int[count];
        for (int i = 0; i < count; i++) {
            derangedOrder[i] = i;
        }

        boolean deranged = false;
        int attempts = 0;
        int maxAttempts = 1000;

        while (!deranged && attempts < maxAttempts) {
            for (int i = count - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                int temp = derangedOrder[i];
                derangedOrder[i] = derangedOrder[j];
                derangedOrder[j] = temp;
            }

            deranged = true;
            for (int i = 0; i < count; i++) {
                if (derangedOrder[i] == i) {
                    deranged = false;
                    break;
                }
            }
            attempts++;
        }

        if (attempts >= maxAttempts) {
            LOGGER.error("Failed to generate derangement after " + maxAttempts + " attempts.");
        }
        return derangedOrder;
    }

    private void swapItems(Player player1, Player player2) {
        Inventory inventory1 = player1.getInventory();
        Inventory inventory2 = player2.getInventory();

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

    private Pair<Integer, ItemStack> getRandomItemStack (Inventory inventory) {
        boolean notEmpty = false;
        for (int i = 0; i < inventory.items.size(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (!ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString().equals("minecraft:air")) {
                notEmpty = true;
                break;
            }
        }
        if (!notEmpty) {
            return Pair.of(0, ItemStack.EMPTY);
        }
        int randomID = RANDOM.nextInt(inventory.items.size());
        ItemStack itemStack = inventory.getItem(randomID);
        while (ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString().equals("minecraft:air")) {
            randomID = RANDOM.nextInt(inventory.items.size());
            itemStack = inventory.getItem(randomID);
        }
        return Pair.of(randomID, itemStack);
    }
}
