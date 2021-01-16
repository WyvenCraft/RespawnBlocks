package com.wyvencraft.respawnblocks;

import org.bukkit.Material;

import java.util.List;

public class RespawnBlock {
    private final Material material;
    private final Material placeholderBlock;
    private final int resetDelay;
    private final List<String> worlds;
    private final List<String> regions;

    public RespawnBlock(Material material, Material placeholderBlock, int resetDelay, List<String> worlds, List<String> regions) {
        this.material = material;
        this.placeholderBlock = placeholderBlock;
        this.resetDelay = resetDelay;
        this.worlds = worlds;
        this.regions = regions;
    }

    public Material getMaterial() {
        return material;
    }

    public Material getPlaceholderBlock() {
        return placeholderBlock;
    }

    public int getResetDelay() {
        return resetDelay;
    }

    public List<String> getWorlds() {
        return worlds;
    }

    public List<String> getRegions() {
        return regions;
    }
}
