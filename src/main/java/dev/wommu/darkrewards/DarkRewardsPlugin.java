package dev.wommu.darkrewards;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class DarkRewardsPlugin extends JavaPlugin implements Listener {

    private Inventory claimInventory;
    private ItemStack claimItem;
    private String permission;
    private String noPermissionMessage;
    private String alreadyClaimedMessage;
    private List<String> commands;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        // create the inventory
        String title = ChatColor.translateAlternateColorCodes('&', config.getString("title", "&lClaim menu"));
        claimInventory = getServer().createInventory(null, 9 * config.getInt("rows"), title);

        // fill background of inventory with gray stained glass
        ItemStackBuilder backGroundItemBuilder = new ItemStackBuilder(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.GRAY.getWoolData()));
        backGroundItemBuilder.setDisplayName("&f");
        ItemStack backGroundItem = backGroundItemBuilder.build();
        for(int i = 0; i < claimInventory.getSize(); i++)
            claimInventory.setItem(i, backGroundItem);

        // insert claim item into the middle of the inventory
        ItemStackBuilder claimItemBuilder = new ItemStackBuilder(new ItemStack(Material.valueOf(config.getString("gui-item.type", "PORTAL"))));
        claimItemBuilder.setDisplayName(config.getString("gui-item.name", "&aClick to claim reward!"));
        claimItemBuilder.setLore(config.getStringList("gui-item.lore"));
        claimItem = claimItemBuilder.build();
        claimInventory.setItem(claimInventory.getSize() / 2, claimItem);

        // cache config options for later
        permission = config.getString("permission", "darkrewards.claim");
        noPermissionMessage = config.getString("no-permission-msg", "&cYou do not have permission to claim the reward.");
        noPermissionMessage = ChatColor.translateAlternateColorCodes('&', noPermissionMessage);
        alreadyClaimedMessage = config.getString("already-claimed-msg", "&cYou have already claimed the reward.");
        alreadyClaimedMessage = ChatColor.translateAlternateColorCodes('&', alreadyClaimedMessage);
        commands = config.getStringList("commands");

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // close gui when plugin disables to prevent item spawning
        claimInventory.getViewers().forEach(HumanEntity::closeInventory);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player)
            ((Player) sender).openInventory(claimInventory);
        else
            sender.sendMessage(ChatColor.RED + "Only players may use this command.");
        return true;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if(!claimInventory.equals(event.getClickedInventory()))
            return;

        // cancel all clicking in the reward gui
        event.setCancelled(true);

        if(claimItem.equals(event.getCurrentItem())) {
            HumanEntity player = event.getWhoClicked();
            player.closeInventory();

            // check permission to claim reward
            if(!player.hasPermission(permission)) {
                player.sendMessage(noPermissionMessage);
                return;
            }

            File claimFile = new File(getDataFolder(), player.getUniqueId() + ".txt");

            // check if the player already claimed the reward
            if(claimFile.exists()) {
                player.sendMessage(alreadyClaimedMessage);
                return;
            }

            // create claim file to mark the player as having claimed their reward
            try {
                claimFile.createNewFile();
            }
            catch(IOException exception) {
                exception.printStackTrace();
            }

            // give rewards to the player
            Server        server     = getServer();
            CommandSender console    = server.getConsoleSender();
            String        playerName = player.getName();
            commands.forEach((command) -> server.dispatchCommand(console, command.replace("%player%", playerName)));
        }
    }
}
