/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137.ChunkLoader;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 *
 * @author James
 */
class ChunkLoaderistener implements Listener {

    ChunkLoader plugin;

    public ChunkLoaderistener(ChunkLoader aThis) {
        plugin = aThis;
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {

        ChunkHolder myChunkLoader = plugin.isPartOfChunkLoaderCollection(event.getChunk());
        if (myChunkLoader == null) {
            return;
        }
        if (myChunkLoader.isPersonalAnchor()) {
            Player player = plugin.getServer().getPlayer(myChunkLoader.getOwner());
            if (player.isOnline()) {
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onChunkload(ChunkLoadEvent event) {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (ChunkLoader.playerAutoKeepchunksLoaded.contains(player.getName()) && player.getLocation().getChunk().equals(event.getChunk())) {
                ChunkHolder myChunkLoader = ChunkLoader.isPartOfChunkLoaderCollection(event.getChunk());
                if (myChunkLoader == null) {
                    myChunkLoader = new ChunkHolder(event.getChunk(), player.getName(), true);
                    ChunkLoader.myChunkHolders.add(myChunkLoader);
                }
                break;
            } else if (ChunkLoader.playerAutoSave.contains(player.getName()) && player.getLocation().getChunk().equals(event.getChunk())) {
                ChunkHolder myChunkLoader = ChunkLoader.isPartOfChunkLoaderCollection(event.getChunk());
                if (myChunkLoader == null) {
                    myChunkLoader = new ChunkHolder(event.getChunk(), player.getName(), true);
                    ChunkLoader.setNewChunk(event.getChunk(), player);
                }
                break;
            }
        }
    }

}
