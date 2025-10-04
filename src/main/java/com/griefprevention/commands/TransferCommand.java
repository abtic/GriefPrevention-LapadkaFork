package com.griefprevention.commands;

import me.ryanhamshire.GriefPrevention.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TransferCommand extends CommandHandler {

    public TransferCommand(GriefPrevention plugin) {
        super(plugin, "transferclaim");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (args.length != 1) {
            player.sendMessage("Usage: /transferclaim <player>");
            return true;
        }

        // get claim at player’s location
        Claim claim = plugin.dataStore.getClaimAt(player.getLocation(), true, null);
        if (claim == null) {
            player.sendMessage("You are not standing in a claim.");
            return true;
        }

        // check ownership
        if (!claim.getOwnerID().equals(player.getUniqueId())) {
            player.sendMessage("You don’t own this claim.");
            return true;
        }

        // find target player
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("That player is not online.");
            return true;
        }

        if (!claim.contains(target.getLocation(), true, false)) {
            player.sendMessage("The target player must be standing inside this claim.");
            return true;
        }
        // transfer ownership
        claim.ownerID = target.getUniqueId();
        // get player data
        PlayerData giverData = plugin.dataStore.getPlayerData(player.getUniqueId());
        PlayerData takerData = plugin.dataStore.getPlayerData(target.getUniqueId());
        
        // remove from giver’s list
        giverData.getClaims().remove(claim);

        // add to taker’s list
        takerData.getClaims().add(claim);
          
        int area = claim.getArea();
        //takerData.setAccruedClaimBlocks(takerData.getRemainingClaimBlocks() + area);
        giverData.setAccruedClaimBlocks(giverData.getRemainingClaimBlocks() - area);
        
        // update claim owner
        plugin.dataStore.saveClaim(claim);
        plugin.dataStore.savePlayerData(player.getUniqueId(), giverData);
        plugin.dataStore.savePlayerData(target.getUniqueId(), takerData);

        player.sendMessage("Claim transferred to " + target.getName());
        target.sendMessage("You are now the owner of this claim!");

        return true;
    }
}
