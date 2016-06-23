/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137.ChunkLoader;

import java.io.Serializable;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;

/**
 *
 * @author James
 */
public class ChunkHolder implements Serializable{
    String ChunkWorldName;
    int ChunkX;
    int ChunkZ;
    String owner;
    boolean isPersonalAnchor;
    boolean keepLoaded = true;

    public ChunkHolder(ChunkLoader plugin, Chunk aThis,CommandSender sender, String[] args) {
        this.ChunkX = aThis.getX();
        this.ChunkZ = aThis.getZ();
        this.ChunkWorldName = aThis.getWorld().getName();
        this.owner = sender.getName();
        this.isPersonalAnchor = false;
    }
    public ChunkHolder(String Location, String owner, boolean isPersonalAnchor)
    {
        String[] split = Location.split(";");
        this.ChunkWorldName = split[0];
        this.ChunkX = Integer.parseInt(split[1]);
        this.ChunkZ = Integer.parseInt(split[2]);
        this.owner = owner;
        this.isPersonalAnchor = isPersonalAnchor; 
    }
    
    public ChunkHolder(Chunk chunk, String owner, boolean isPersonalAnchor)
    {
        
        this.ChunkWorldName = chunk.getWorld().getName();
        this.ChunkX = chunk.getX();
        this.ChunkZ = chunk.getZ();
        this.owner = owner;
        this.isPersonalAnchor = isPersonalAnchor; 
    }

    public String getOwner() {
        return owner;
    }

    public boolean isPersonalAnchor() {
        return isPersonalAnchor;
    }

    public boolean isSameChunk(Chunk compareChunk) {

        if (ChunkWorldName.equals(compareChunk.getWorld().getName())) {
            if (ChunkX == compareChunk.getX()) {
                if (ChunkZ == compareChunk.getZ()) {
                    return true;
                }
            }
        }

        return false;
    }

    Chunk getChunk() {
        return ChunkLoader.instance.getServer().getWorld(ChunkWorldName).getChunkAt(ChunkX, ChunkZ);
    }
    
    @Override
	   public String toString() {
    	   return new StringBuffer("World:")
    	   .append(this.ChunkWorldName)
    	   .append(" ChunkX: ")
    	   .append(ChunkX)
           .append(" ChunZ: ")
    	   .append(ChunkZ)
           .append(" Owner: ")
    	   .append(owner)
           .append(" isPersonalAnchor: ")
    	   .append(isPersonalAnchor).toString();
	   }

}
