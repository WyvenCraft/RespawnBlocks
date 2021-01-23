package com.wyvencraft.respawnblocks;

import org.bukkit.block.BlockState;
import org.bukkit.scheduler.BukkitRunnable;

public class RespawnTimer extends BukkitRunnable {

    private final BlockState defaultState;

    public RespawnTimer(BlockState defaultState) {
        this.defaultState = defaultState;
    }

    @Override
    public void run() {
        this.resetBlockState();
    }

    public void resetBlockState() {
        defaultState.getBlock().removeMetadata("BROKEN", RespawnBlocks.instance.getPlugin().getPlugin());
        defaultState.update(true);
    }
}
