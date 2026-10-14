package com.example.vaultrestocker;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Vault;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class VaultRestocker extends JavaPlugin {

    private BukkitTask task;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        startTask();
        getLogger().info("VaultRestocker enabled.");
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
        }
    }

    private void startTask() {
        if (task != null) {
            task.cancel();
        }
        long intervalTicks = Math.max(1L, getConfig().getLong("interval-seconds", 30L)) * 20L;
        task = Bukkit.getScheduler().runTaskTimer(this, this::restockAll, intervalTicks, intervalTicks);
    }

    private int restockAll() {
        List<String> allowed = getConfig().getStringList("worlds");
        boolean nearPlayers = getConfig().getBoolean("only-near-players", false);
        int restocked = 0;

        for (World world : Bukkit.getWorlds()) {
            if (!allowed.isEmpty() && !allowed.contains(world.getName())) {
                continue;
            }

            Chunk[] chunks;
            if (nearPlayers) {
                Set<Chunk> set = new HashSet<>();
                int view = Math.min(Bukkit.getViewDistance(), 10);
                for (Player p : world.getPlayers()) {
                    int cx = p.getLocation().getBlockX() >> 4;
                    int cz = p.getLocation().getBlockZ() >> 4;
                    for (int x = -view; x <= view; x++) {
                        for (int z = -view; z <= view; z++) {
                            if (world.isChunkLoaded(cx + x, cz + z)) {
                                set.add(world.getChunkAt(cx + x, cz + z));
                            }
                        }
                    }
                }
                chunks = set.toArray(new Chunk[0]);
            } else {
                chunks = world.getLoadedChunks();
            }

            for (Chunk chunk : chunks) {
                for (BlockState state : chunk.getTileEntities(false)) {
                    if (state instanceof Vault vault && resetVault(vault)) {
                        restocked++;
                    }
                }
            }
        }
        return restocked;
    }

    private boolean resetVault(Vault vault) {
        Set<UUID> rewarded = new HashSet<>(vault.getRewardedPlayers());
        if (rewarded.isEmpty()) {
            return false;
        }
        for (UUID id : rewarded) {
            vault.removeRewardedPlayer(id);
        }
        vault.update();
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("Usage: /" + label + " <now|reload>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "now" -> sender.sendMessage("Restocked " + restockAll() + " vault(s).");
            case "reload" -> {
                reloadConfig();
                startTask();
                sender.sendMessage("VaultRestocker config reloaded.");
            }
            default -> sender.sendMessage("Usage: /" + label + " <now|reload>");
        }
        return true;
    }
}
