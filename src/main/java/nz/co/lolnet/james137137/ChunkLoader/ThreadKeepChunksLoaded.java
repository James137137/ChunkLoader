/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137.ChunkLoader;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Chunk;

/**
 *
 * @author James
 */
public class ThreadKeepChunksLoaded implements Runnable {

    boolean run = false;
    public ThreadKeepChunksLoaded() {
        run = true;
        start();
    }

    @Override
    public void run() {
        while (run) {
            try {
                for (ChunkHolder chunkHolder : ChunkLoader.myChunkHolders) {
                    if (chunkHolder.keepLoaded) {
                        Chunk chunk = chunkHolder.getChunk();
                        if (!chunk.isLoaded()) {
                            boolean load = chunk.load();   
                            if (!load) {
                                System.out.println("Failed to load Chunk: " + chunk.toString());
                            } else {
                            }
                        } else {
                        }
                    }

                }
            } catch (Exception e) {
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(ThreadKeepChunksLoaded.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void start() {
        Thread t = new Thread(this);
        t.start();
    }

}
