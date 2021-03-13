package com.github.showwno.mallprotect;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MallProtect extends JavaPlugin implements Listener {
    private Location loc1;
    private Location loc2;

    @Override
    public void onEnable() {
        this.loc1 = getLoc(1);
        this.loc2 = getLoc(2);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    private Location getLoc(Integer num){
        if (num.equals(1)){
            if (getConfig().getLocation("loc.1") == null) return null;
            return getConfig().getLocation("loc.1");
        }
        else if (num.equals(2)) {
            if (getConfig().getLocation("loc.2") == null) return null;
            return getConfig().getLocation("loc.2");
        }
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender s, Command cmd, @NotNull String label, @NotNull String[] args) {
        Player p = (Player) s;
        if (cmd.getName().equals("mallprotect")) {
            if (args.length == 0) {
                if (checkConfig()) {
                    p.sendMessage("§7[§eMallProtect§7] §f現在: §a§l有効");
                    Location loc1 = this.loc1;
                    p.sendMessage("§7 - §f座標§7[§b1§7]§f: §b"+loc1.getBlockX()+", "+loc1.getBlockY()+", "+loc1.getBlockZ());
                    Location loc2 = this.loc2;
                    p.sendMessage("§7 - §f座標§7[§b2§7]§f: §b"+loc2.getBlockX()+", "+loc2.getBlockY()+", "+loc2.getBlockZ());
                } else {
                    p.sendMessage("§7[§eMallProtect§7] §f現在: §c§l無効");
                }
                p.sendMessage("§7 - §fコマンドの使用方法は §e/mallprotect help §fで確認できます!");
                return true;
            }
            switch (args[0]) {
                case "1":
                    getConfig().set("loc.1", p.getLocation());
                    this.loc1 = p.getLocation();
                    return true;
                case "2":
                    getConfig().set("loc.2", p.getLocation());
                    this.loc2 = p.getLocation();
                    return true;
                case "reload":
                    reloadConfig();
                    p.sendMessage("§7[§eMallProtect§7] §fConfigをリロードしました!");
                    return true;
                case "help":
                    p.sendMessage("§7 - §e/mallprotect 1 §f> 座標§7[§b1§7] §fを設定!");
                    p.sendMessage("§7 - §e/mallprotect 2 §f> 座標§7[§b2§7] §fを設定!");
                    p.sendMessage("§7 - §e/mallprotect reload §f> Configをリロード!");
                    return true;
            }
        }
        return true;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent ev) {
        if (ev.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        if (checkRegion(ev.getBlock().getLocation())) {
            ev.getPlayer().sendMessage("§7» §cここでブロック破壊は出来ません!");
            ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent ev) {
        if (ev.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        if (checkRegion(ev.getBlock().getLocation())) {
            ev.getPlayer().sendMessage("§7» §cここでブロック設置は出来ません!");
            ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent ev) {
        List<Block> removeBlock = new ArrayList<>();
        for (Block b: ev.blockList()) {
            if (b != null) {
                if (checkRegion(b.getLocation())) removeBlock.add(b);
            }
        }
        ev.blockList().removeAll(removeBlock);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent ev) {
        List<Block> removeBlock = new ArrayList<>();
        for (Block b: ev.blockList()) {
            if (b != null) {
                if (checkRegion(b.getLocation())) removeBlock.add(b);
            }
        }
        ev.blockList().removeAll(removeBlock);
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent ev) {
        if (ev.getEntity().getType().equals(EntityType.PLAYER)) return;
        if (!ev.getEntity().getType().isAlive()) return;
        if (checkRegion(ev.getEntity().getLocation())) {
            ev.setCancelled(true);
        }
    }

    private boolean checkRegion(Location evLoc) {
        if (!checkConfig()) {
            return false;
        }
        final Location loc1 = this.loc1;
        final Location loc2 = this.loc2;
        int[] x = new int[] {evLoc.getBlockX(), loc1.getBlockX(), loc2.getBlockX()};
        Arrays.sort(x);
        if (x[1] == evLoc.getBlockX()) {
            int[] y = new int[]{evLoc.getBlockY(), loc1.getBlockY(), loc2.getBlockY()};
            Arrays.sort(y);
            if (y[1] == evLoc.getBlockY()) {
                int[] z = new int[]{evLoc.getBlockZ(), loc1.getBlockZ(), loc2.getBlockZ()};
                Arrays.sort(z);
                return (z[1] == evLoc.getBlockZ());
            }
        }
        return false;
    }

    private boolean checkConfig() {
        return (this.loc1!=null&&this.loc2!=null);
    }
}
