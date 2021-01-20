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
import org.bukkit.scheduler.BukkitTask;

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

    List<RespawnBlock> respawnBlocks;
    public HashMap<Location, BukkitTask> broken = new HashMap<>();

    @Override
    public void onLoad() {
        saveDefaultConfig("respawnblocks.yml");

        FileConfiguration config = getConfig("respawnblocks.yml");
        respawnBlocks = new ArrayList<RespawnBlock>() {{
            for (String material : config.getConfigurationSection("Block_Regen").getKeys(false)) {
                Material mat = Material.getMaterial(material);
                if (mat == null) {
                    getPlugin().getLogger().severe("regen block " + material + " is not a valid material");
                    continue;
                }

                ConfigurationSection matSection = config.getConfigurationSection("Block_Regen." + material);

                int delay = matSection.getInt("resetDelay", 0);

                if (delay < 1) {
                    getPlugin().getLogger().severe("make sure that delay for " + material + " is greater than zero. (auto setting to 1)");
                    delay = 1;
                }

                List<String> worlds = matSection.getStringList("worlds");

                List<String> regions = null;
                if (Hook.isEnabled("WorldGuard")) {
                    regions = matSection.getStringList("regions");
                }

                Material placeholder = Material.getMaterial(matSection.getString("placeholder", "BEDROCK"));
                if (placeholder == null) {
                    getPlugin().getLogger().severe("regen placeholder block " + material + " is not a valid material");
                    continue;
                }

                add(new RespawnBlock(mat, placeholder, delay, worlds, regions));
            }
        }};
    }

    public RespawnBlock getRespawnBlock(Block block) {
        for (RespawnBlock respawnBlock : respawnBlocks) {
            if (respawnBlock.getMaterial() != block.getType()) continue;

            if (!respawnBlock.getWorlds().isEmpty() && !respawnBlock.getWorlds().contains(block.getWorld().getName()))
                continue;

            if (Hook.isEnabled("WorldGuard")) {
                if (respawnBlock.getRegions() != null && !respawnBlock.getRegions().isEmpty()) {
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
                        return null;
                    }
                }
            }

            return respawnBlock;
        }

        return null;
    }

    @Override
    public void onEnable() {
        getPlugin().getAddonHandler().registerListener(this, new Listener());
    }

    @Override
    public void onDisable() {
        // Stop tasks and reset all broken blocks
    }

    @Override
    public void reloadConfig() {
        reloadConfig("respawnblocks.yml");
    }
}
