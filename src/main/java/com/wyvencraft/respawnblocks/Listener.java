package com.wyvencraft.respawnblocks;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Listener implements org.bukkit.event.Listener {

    //(priority = EventPriority.MONITOR)
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;

        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (e.getBlock().hasMetadata("PLACED")) return;

        RespawnBlock respawnable = RespawnBlocks.instance.getRespawnBlock(e.getBlock());

        if (respawnable != null) {
            e.setCancelled(true);

            Block block = e.getBlock();

            Material respawn;
            if (block.hasMetadata("BROKEN")) {
                respawn = Material.getMaterial(block.getMetadata("BROKEN").get(0).asString());
                RespawnBlocks.instance.broken.get(block.getLocation()).cancel();
            } else {
                block.setMetadata("BROKEN", new FixedMetadataValue(RespawnBlocks.instance.getPlugin().getPlugin(), block.getType().name()));
                respawn = block.getType();
            }

            block.setType(respawnable.getPlaceholderBlock());
            RespawnBlocks.instance.broken.put(
                    block.getLocation(),
                    startRespawnTimer(
                            respawnable.getResetDelay(),
                            respawn,
                            block.getLocation()));
        }
    }

    public BukkitTask startRespawnTimer(int time, Material material, Location location) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                location.getBlock().removeMetadata("BROKEN", RespawnBlocks.instance.getPlugin().getPlugin());
                location.getBlock().setType(material);
                RespawnBlocks.instance.broken.remove(location);
            }
        }.runTaskLater(RespawnBlocks.instance.getPlugin().getPlugin(), 20L * time);
    }
}
