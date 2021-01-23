package com.wyvencraft.respawnblocks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.wyvencraft.api.addon.Addon;
import com.wyvencraft.api.hooks.Hook;
import com.wyvencraft.api.integration.WyvenAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RespawnBlocks extends Addon {
    public static RespawnBlocks instance;

    public RespawnBlocks(WyvenAPI plugin) {
        super(plugin);
        instance = this;
    }

    Map<Material, List<RespawnBlock>> respawnBlocks = new HashMap<>();
    public HashMap<Location, RespawnTimer> broken = new HashMap<>();

    @Override
    public void onLoad() {
        saveDefaultConfig("respawnblocks.yml");
    }

    public RespawnBlock getRespawnBlock(Block block) {
        if (!respawnBlocks.containsKey(block.getType())) return null;

        for (RespawnBlock respawnBlock : respawnBlocks.get(block.getType())) {
            if (respawnBlock.getMaterial() != block.getType()) continue;

            if (!respawnBlock.getWorlds().isEmpty() && !respawnBlock.getWorlds().contains(block.getWorld().getName()))
                continue;

            if (Hook.isEnabled("WorldGuard")) {
                if (!respawnBlock.getRegions().isEmpty()) {
                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                    RegionManager regions = container.get(BukkitAdapter.adapt(block.getWorld()));

                    if (regions != null) {
                        for (String regionName : respawnBlock.getRegions()) {
                            if (regions.getRegions().containsKey(regionName)) {
                                if (regions.getRegion(regionName).contains(block.getX(), block.getY(), block.getZ())) {
                                    return respawnBlock;
                                }
                            }
                        }
                    }
                    return null;
                }
            }

            return respawnBlock;
        }

        return null;
    }

    @Override
    public void onEnable() {
        getPlugin().getAddonHandler().registerListener(this, new Listener());

        FileConfiguration config = getConfig("respawnblocks.yml");
        for (String matName1 : config.getKeys(false)) {

            Material key = Material.getMaterial(matName1.toUpperCase());
            if (key == null) {
                getPlugin().getLogger().severe("regen block " + matName1 + " is not a valid material");
                continue;
            }

            if (respawnBlocks.containsKey(key)) continue;

            List<RespawnBlock> value = new ArrayList<>();
            for (String matName2 : config.getKeys(false)) {
                if (!matName1.equalsIgnoreCase(matName2)) continue;

                Material mat = Material.getMaterial(matName2.toUpperCase());
                if (mat == null) {
                    getPlugin().getLogger().severe("regen block " + matName2 + " is not a valid material");
                    continue;
                }

                ConfigurationSection matSection = config.getConfigurationSection(matName2);
                if (matSection == null) continue;

                int delay = matSection.getInt("resetDelay", 0);
                if (delay < 1) {
                    getPlugin().getLogger().severe("make sure that delay for " + matName2 + " is greater than zero. (auto setting to 1)");
                    delay = 1;
                }

                List<String> worlds = matSection.getStringList("worlds");

                List<String> regions = new ArrayList<>();
                if (Hook.isEnabled("WorldGuard")) {
                    regions = matSection.getStringList("regions");
                }

                Material placeholder = Material.getMaterial(matSection.getString("placeholder", "BEDROCK"));
                if (placeholder == null) {
                    getPlugin().getLogger().severe("regen placeholder block " + matName2 + " is not a valid material");
                    continue;
                }

                value.add(new RespawnBlock(mat, placeholder, delay, worlds, regions));
            }

            respawnBlocks.put(key, value);
        }
    }

    @Override
    public void onDisable() {
        int fixedAmount = 0;
        // Stop tasks and reset all broken blocks
        for (Map.Entry<Location, RespawnTimer> broken : broken.entrySet()) {
            broken.getValue().cancel();
            broken.getValue().resetBlockState();
            fixedAmount++;
        }

        if (fixedAmount > 0)
            getPlugin().getLogger().info("Cleared " + fixedAmount + " blocks");
    }

    @Override
    public void reloadConfig() {
        reloadConfig("respawnblocks.yml");
    }
}
