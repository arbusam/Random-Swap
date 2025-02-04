# Random Swap

An open source minecraft mod that randomly swaps items between player invetories at a random interval.

https://github.com/user-attachments/assets/32f96f79-41c4-40bf-9868-9852d157fef9


## Installation
1. [Download Forge](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.19.html) (mod only supports versions 1.19.x for now)
2. Download the latest version of the mod from the [releases page](https://github.com/arbusam/Random-Swap/releases).
3. Place the downloaded jar file in your mods folder.

## How to use (Commands)

### Operator Commands
- `/RandomSwap <PLAYERS>` - Starts the random swap with all the players listed.
  - `/RandomSwap Player1 Player2`
  - `/RandomSwap Player1 Player2 Player3`
  - `/RandomSwap @a` - Starts the random swap with all players.
- `/RandomSwapCancel` - Cancels the current random swap.
- `/RandomSwapMinTime <MINUTES>` - Sets the minimum amount of time between random swaps. (Default is 0 minutes)
  - `/RandomSwapMinTime 1` - Sets the minimum time to 1 minute.
  - `/RandomSwapMinTime 5.5` - Sets the minimum time to 5.5 minutes.
  - `/RandomSwapMinTime 0` - Disables the minimum time.
- `/RandomSwapMaxTime <MINUTES>` - Sets the maximum amount of time between random swaps. (Default is 10 minutes) (NOTE: Set the minimum and maximum time to be equal to have the random swap happen at a fixed interval)
  - `/RandomSwapMaxTime 1` - Sets the maximum time to 1 minute.
  - `/RandomSwapMaxTime 5.5` - Sets the maximum time to 5.5 minutes.
- `/RandomSwapShowLostItem` - Toggles showing the item that was lost during a random swap in chat. (Default is true)
- `/RandomSwapShowGainedItem` - Toggles showing the item that was gained during a random swap in chat. (Default is true)

### Player Commands
- `/RandomSwapToggleCountdown` - Toggles showing the time passed since the last swap in the top left corner (Default is true)
