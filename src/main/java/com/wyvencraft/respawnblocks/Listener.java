package com.wyvencraft.respawnblocks;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

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

            BlockState defaultState = block.getState();
            if (block.hasMetadata("BROKEN")) {
                RespawnBlocks.instance.broken.get(block.getLocation()).cancel();
                defaultState.setType(Material.getMaterial(block.getMetadata("BROKEN").get(0).asString()));
            } else {
                block.setMetadata("BROKEN", new FixedMetadataValue(RespawnBlocks.instance.getPlugin().getPlugin(), block.getType().name()));
            }

            e.setDropItems(true);

            // Drop drops at for available spot around mined block
            for (BlockFace face : BlockFace.values()) {
                Block freeSpot = block.getRelative(face);
                if (freeSpot.getType() == Material.AIR) {

                    for (ItemStack drop : e.getBlock().getDrops())
                        block.getWorld().dropItem(freeSpot.getLocation().toCenterLocation(), drop);
                    break;
                }
            }

            block.setType(respawnable.getPlaceholderBlock());

            RespawnTimer timer = new RespawnTimer(defaultState);
            timer.runTaskLater(RespawnBlocks.instance.getPlugin().getPlugin(), 20L * respawnable.getResetDelay());

            RespawnBlocks.instance.broken.put(block.getLocation(), timer);
        }
    }

//    public BukkitTask startRespawnTimer(int time, BlockState state) {
//        return new BukkitRunnable() {
//            @Override
//            public void run() {
//                state.getBlock().removeMetadata("BROKEN", RespawnBlocks.instance.getPlugin().getPlugin());
//                System.out.println("before: " + state.getBlock().getType().name());
//                state.update(true);
//                System.out.println("after: " + state.getBlock().getType().name());
//
//                RespawnBlocks.instance.broken.remove(state.getLocation());
//            }
//        }.runTaskLater(RespawnBlocks.instance.getPlugin().getPlugin(), 20L * time);
//    }
}
