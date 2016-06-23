/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137.ChunkLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author James
 */
public class ChunkLoader extends JavaPlugin {

    static final Logger log = Bukkit.getLogger();
    protected static ArrayList<ChunkHolder> myChunkHolders = new ArrayList<ChunkHolder>();
    public static ChunkLoader instance;
    ThreadKeepChunksLoaded threadKeepChunksLoaded;
    static HashSet<String> playerAutoKeepchunksLoaded = new HashSet<String>();
    static HashSet<String> playerAutoSave = new HashSet<String>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        boolean useMySQL = this.getConfig().getBoolean("MysqlEnabled");
        MysqlMethods.setupMysql(this, useMySQL);
        String version = Bukkit.getServer().getPluginManager().getPlugin(this.getName()).getDescription().getVersion();
        log.log(Level.INFO, this.getName() + " : Version {0} enabled", version);
        getServer().getPluginManager().registerEvents(new ChunkLoaderistener(this), this);
        instance = this;
        loadFromMysql();
        threadKeepChunksLoaded = new ThreadKeepChunksLoaded();
    }

    @Override
    public void onDisable() {
        threadKeepChunksLoaded.run = false;
        log.log(Level.INFO, "{0}: disabled", this.getName());
    }

    public void reloadChunkDatabase() {
        threadKeepChunksLoaded.run = false;
        loadFromMysql();
        threadKeepChunksLoaded = new ThreadKeepChunksLoaded();
        log.log(Level.INFO, "[{0}] Database reloaded!", this.getName());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String commandName = command.getName().toLowerCase();
        if (commandName.equalsIgnoreCase("ChunkLoader") || commandName.equalsIgnoreCase("CL")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "command missing arguments");
                sender.sendMessage("For help please type /ChunkLoader help");
                return true;
            } else if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("help")) {
                    showHelp(sender, args);
                } else if (args[0].equalsIgnoreCase("set") && sender.hasPermission("ChunkLoader.set")) {
                    if (args.length >= 2 && args[1].equalsIgnoreCase("auto")) {
                        if (ChunkLoader.playerAutoKeepchunksLoaded.contains(sender.getName())) {
                            ChunkLoader.playerAutoKeepchunksLoaded.remove(sender.getName());
                            sender.sendMessage("removed for auto set.");
                        } else {
                            ChunkLoader.playerAutoKeepchunksLoaded.remove(sender.getName());
                            sender.sendMessage("Auto for auto set. Chunks that you have been to will remain loaded while you are logged in");
                        }
                    } else if (args.length >= 2 && args[1].equalsIgnoreCase("autosave")) {
                        if (ChunkLoader.playerAutoSave.contains(sender.getName())) {
                            ChunkLoader.playerAutoSave.remove(sender.getName());
                            sender.sendMessage("removed for auto set.");
                        } else {
                            ChunkLoader.playerAutoSave.remove(sender.getName());
                            sender.sendMessage("Auto for auto set. Chunks that you have been to will remain loaded while you are logged in");
                        }
                    } else {
                        setNewChunk(sender, args);
                    }
                } else if (args[0].equalsIgnoreCase("remove") && sender.hasPermission("ChunkLoader.remove")) {
                    removeChunk(sender);
                } else if (args[0].equalsIgnoreCase("list") && sender.hasPermission("ChunkLoader.list")) {
                    if (myChunkHolders.isEmpty()) {
                        sender.sendMessage(ChatColor.RED + "there are no chunks added");
                        return true;
                    }
                    for (ChunkHolder chunkHolder : myChunkHolders) {
                        sender.sendMessage(chunkHolder.toString());
                    }

                } else if (args[0].equalsIgnoreCase("info") && sender.hasPermission("ChunkLoader.info")) {
                    infoChunk(sender);
                } else if (args[0].equalsIgnoreCase("reloadDatabase")) {
                    reloadChunkDatabase();
                    sender.sendMessage("Database reloaded!");
                } else if (args[0].equalsIgnoreCase("debug") && sender.hasPermission("ChunkLoader.debug")) {
                    List<World> worlds = Bukkit.getServer().getWorlds();
                    for (World world : worlds) {
                        Chunk[] loadedChunks = world.getLoadedChunks();
                        for (Chunk chunk : loadedChunks) {
                            chunk.unload();
                        }
                    }

                }
                return true;
            }
        }
        return false;
    }

    /**
     * @param args the command line arguments
     */
    private void showHelp(CommandSender sender, String[] args) {
        int pageNumbers = 1;
        boolean isPageRequest;
        if (args.length == 1) {
            sender.sendMessage(ChatColor.YELLOW + "====ChunkLoader====");
            sender.sendMessage("/ChunkLoader list");
            sender.sendMessage("/ChunkLoader set");
            sender.sendMessage("/ChunkLoader remove");
            sender.sendMessage("/ChunkLoader info");
            sender.sendMessage("Page 1 of " + pageNumbers);
            return;
        } else {

            int page = 1;
            try {
                page = Integer.parseInt(args[1]);
                isPageRequest = true;
            } catch (Exception e) {
                isPageRequest = false;
            }
            if (isPageRequest) {
                if (page > pageNumbers) {
                    sender.sendMessage("Invailed page number.");
                    return;
                }
                if (page == 1) {
                }
            }
        }
    }

    public boolean UnloadChunk(int id) {
        if (myChunkHolders.size() <= id) {
            Chunk tempChunk = myChunkHolders.get(id).getChunk();
            myChunkHolders.remove(id);
            if (tempChunk.isLoaded()) {
                return tempChunk.unload(true, true);
            }
            return false;
        }
        return false;
    }

    public boolean UnloadChunk(String PlayerName, boolean forceUnload) {
        boolean success = false;
        for (ChunkHolder chunkHolder : myChunkHolders) {
            if (chunkHolder.getOwner().equalsIgnoreCase(PlayerName)) {
                success = true;
                if (!chunkHolder.isPersonalAnchor || forceUnload) {
                    Chunk tempChunk = chunkHolder.getChunk();
                    tempChunk.unload(true, true);
                }

            }
        }
        return success;
    }

    private void setNewChunk(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Chunk thisChunk = player.getLocation().getChunk();
            if (isPartOfChunkLoaderCollection(thisChunk) == null) {
                MysqlMethods.addChunk(thisChunk, player.getName(), false);
                player.sendMessage(ChatColor.GREEN + "Chunk is now part of Chunk loader go /chunkloader info");
            } else {
                player.sendMessage(ChatColor.RED + "This Chunk is already part of ChunkLoader go /chunkloader info");
                return;
            }
            myChunkHolders.add(new ChunkHolder(this, thisChunk, sender, args));
        }
    }
    
    public static void setNewChunk(Chunk chunk, Player player) {
        
            
            if (isPartOfChunkLoaderCollection(chunk) == null) {
                MysqlMethods.addChunk(chunk, player.getName(), false);
            } else {
                return;
            }
            myChunkHolders.add(new ChunkHolder(chunk, player.getName(), false));
        
    }

    private void loadFromMysql() {
        myChunkHolders = MysqlMethods.GetAllChunks();

    }

    public static ChunkHolder isPartOfChunkLoaderCollection(Chunk chunk) {
        for (ChunkHolder chunkHolder : myChunkHolders) {
            if (chunkHolder.isSameChunk(chunk)) {
                return chunkHolder;
            }
        }
        return null;
    }

    private void removeChunk(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Chunk thisChunk = player.getLocation().getChunk();
            ChunkHolder partOfChunkLoaderCollection = isPartOfChunkLoaderCollection(thisChunk);
            if (partOfChunkLoaderCollection == null) {
                player.sendMessage(ChatColor.RED + "This Chunk is not part of ChunkLoader go /chunkloader info");
                return;
            }
            MysqlMethods.RemoveChunk(thisChunk);
            myChunkHolders.remove(partOfChunkLoaderCollection);
            player.sendMessage(ChatColor.GREEN + "removed!");

        }
    }

    private void infoChunk(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Chunk thisChunk = player.getLocation().getChunk();
            ChunkHolder partOfChunkLoaderCollection = isPartOfChunkLoaderCollection(thisChunk);
            if (partOfChunkLoaderCollection == null) {
                player.sendMessage(ChatColor.RED + "This Chunk is not part of ChunkLoader go /chunkloader info");
                return;
            }
            Chunk chunk = partOfChunkLoaderCollection.getChunk();
            player.sendMessage("Chunk Location (x/z):" + chunk.getX() + ":" + chunk.getZ());
            player.sendMessage("Chunk Owner = " + ChatColor.GREEN + partOfChunkLoaderCollection.getOwner());
            player.sendMessage("Is personalAnchor = " + ChatColor.GREEN + partOfChunkLoaderCollection.isPersonalAnchor());
        }
    }
}
