package com.forgeessentials.economy.commands;

import com.forgeessentials.core.commands.ForgeEssentialsCommandBase;
import com.forgeessentials.util.OutputHandler;
import com.forgeessentials.util.UserIdent;
import com.forgeessentials.api.APIRegistry;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.permissions.PermissionsManager.RegisteredPermValue;

import java.util.Arrays;
import java.util.List;

public class CommandSellCommand extends ForgeEssentialsCommandBase {
    @Override
    public String getCommandName()
    {
        return "sellcommand";
    }

    @Override
    public List<String> getCommandAliases()
    {
        return Arrays.asList("sc", "scmd");
    }

    /*
     * Expected structure: "/sellcommand <player> <item> [amount] [meta] <command [args]>"
     */
    @Override
    public void processCommandConsole(ICommandSender sender, String[] args)
    {
        // System.out.print(sender);
        if (args.length >= 4)
        {
            String playerName = args[0];

            EntityPlayerMP player = UserIdent.getPlayerByMatchOrUsername(sender, playerName);
            if (player != null)
            {
                boolean found = false, hasMeta = false;

                String itemName = args[1];
                int amount = Integer.parseInt(args[2]);
                int meta = -1;
                if (args.length >= 5)
                {
                    hasMeta = true;
                    meta = Integer.parseInt(args[3]);
                }

                // get item
                Item offeredItem = null;
                try {
                    offeredItem = CommandBase.getItemByText(sender, itemName);
                }
                catch(NumberFormatException e) {
                    offeredItem = (Item)GameData.getItemRegistry().getObject(itemName);
                }

                if (offeredItem != null)
                {
                    ItemStack target = new ItemStack(offeredItem, amount, meta);
                    String targetName = target.getUnlocalizedName();

                    // Loop though inventory and find a stack big enough to support the sell command
                    for (int slot = 0; slot < player.inventory.mainInventory.length; slot++)
                    {
                        ItemStack is = player.inventory.mainInventory[slot];
                        if (is != null)
                        {
                            if (is.getUnlocalizedName().equalsIgnoreCase(targetName))
                            {
                                if (meta == -1 || meta == is.getItemDamage())
                                {
                                    if (is.stackSize >= amount)
                                    {
                                        player.inventory.decrStackSize(slot, amount);
                                        found = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (found)
                    {
                        // Do command

                        StringBuilder cmd = new StringBuilder(args.toString().length());
                        if (hasMeta)
                        {
                            for (int i = 4; i < args.length; i++)
                            {
                                cmd.append(args[i]);
                                cmd.append(" ");
                            }
                        }
                        else
                        {
                            for (int i = 3; i < args.length; i++)
                            {
                                cmd.append(args[i]);
                                cmd.append(" ");
                            }
                        }
                        MinecraftServer.getServer().getCommandManager().executeCommand(sender, cmd.toString());
                        OutputHandler.chatConfirmation(player, String.format("That cost you %d x %s. Your balance is %s.",
                                amount, target.getDisplayName(), APIRegistry.wallet.getMoneyString(player.getPersistentID())));
                    }
                    else
                    {
                        //this should be removed
                        OutputHandler.chatError(player, "You don't have the requested item in your inventory!");
                    }
                }
                else
                {
                    OutputHandler.chatError(sender, String.format("Item %s was not found.", itemName));
                }
            }
            else
            {
                //this should be removed
                OutputHandler.chatError(sender, String.format("Player %s does not exist, or is not online.", args[0]));
            }
        }
        else
        {
            OutputHandler.chatError(sender, "Improper syntax. Please try this instead: <player> <['amount'x]item[:'meta']> <command [args]>");
        }
    }

    @Override
    public boolean canConsoleUseCommand()
    {
        return true;
    }

    @Override
    public String getPermissionNode()
    {
        return null;
    }

    @Override
    public boolean canPlayerUseCommand(EntityPlayer player)
    {
        return false;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {

        return "/sellcommand <player> <['amount'x]item[:'meta']> <command [args]>";
    }

    @Override
    public RegisteredPermValue getDefaultPermission()
    {

        return null;
    }

}
