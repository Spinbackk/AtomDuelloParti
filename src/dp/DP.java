package dp;

import org.bukkit.plugin.java.*;
import me.realized.duels.api.*;
import net.milkbowl.vault.economy.*;
import dp.utls.*;
import me.realized.duels.api.arena.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.*;
import dp.evnts.*;
import org.bukkit.event.*;
import dp.cmds.*;
import org.bukkit.command.*;
import org.bukkit.plugin.*;
import java.util.*;

public class DP extends JavaPlugin
{
    public static Plugin dp;
    public static Duels ds;
    public static Economy eco;
    public static Map<UUID, Grp> grpMap;
    public static Map<UUID, Req> reqMap;
    public static Map<UUID, Mtc> mtcMap;
    public static Map<Arena, Boolean> busyMap;
    public static Map<UUID, Boolean> aliveMap;
    public static Map<UUID, UUID> specMap;

    public void onEnable() {
        this.rEco();
        this.rT();
        this.rDs();
        this.rLst();
        this.rCmds();
        this.gArena();
        this.memorySaver();

        /*Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this, (Runnable)new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(" ");
                Bukkit.broadcastMessage("§6§lDÜELLO PARTİ!");
                Bukkit.broadcastMessage("§e  Diğer partiler ile savaşabilmek için §6§n/dp§e komutunu kullanabilirsin.");
                Bukkit.broadcastMessage(" ");
                Bukkit.getOnlinePlayers().forEach(allah -> allah.playSound(allah.getLocation(), Sound.NOTE_PLING, 5F, 5F));
            }
        }, (long)600*20, 100L);*/
    }

    public void onDisable() {
        for (final UUID var : DP.mtcMap.keySet()) {
            final Mtc var2 = DP.mtcMap.get(var);
            final Grp sender = var2.getSender();
            final Grp reciever = var2.getReciever();
            if (var2.getBet() > 0) {
                final int balance = var2.getBet();
                DP.eco.bankDeposit(sender.getOwner().getName(), (double)balance);
                DP.eco.bankDeposit(reciever.getOwner().getName(), (double)balance);
            }
            for (final UUID var3 : sender.getMembers()) {
                final Player var4 = Bukkit.getPlayer(var3);
                if (var4 == null) {
                    continue;
                }
                if (Bukkit.getWorld("uzayarena") == null) {
                    return;
                }
                var4.teleport(Bukkit.getWorld("uzayarena").getSpawnLocation());
                var4.sendMessage("§6[Düello Parti] §cOyun iptal edildi.");
            }
            for (final UUID var3 : reciever.getMembers()) {
                final Player var4 = Bukkit.getPlayer(var3);
                if (var4 == null) {
                    continue;
                }
                if (Bukkit.getWorld("uzayarena") == null) {
                    return;
                }
                var4.teleport(Bukkit.getWorld("uzayarena").getSpawnLocation());
                var4.sendMessage("§6[Düello Parti] §cOyun iptal edildi.");
            }
        }
    }

    private void memorySaver() {
        new BukkitRunnable() {
            public void run() {
                if (DP.mtcMap.isEmpty()) {
                    DPListeners.placed.clear();
                }
            }
        }.runTaskTimer(DP.dp, 20L, 2400L);
    }

    private void gArena() {
        for (final Arena arena : DP.ds.getArenaManager().getArenas()) {
            arena.setDisabled(false);
        }
    }

    private void rLst() {
        final PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents((Listener)new DPListeners(), DP.dp);
    }

    private void rCmds() {
        this.getCommand("dp").setExecutor((CommandExecutor)new Aich());
    }

    private void rEco() {
        final RegisteredServiceProvider<Economy> rsp = (RegisteredServiceProvider<Economy>)this.getServer().getServicesManager().getRegistration((Class)Economy.class);
        DP.eco = (Economy)rsp.getProvider();
    }

    private void rDs() {
        DP.ds = (Duels)this.getServer().getPluginManager().getPlugin("Duels");
    }

    private void rT() {
        DP.dp = (Plugin)this;
    }

    static {
        DP.grpMap = new HashMap<UUID, Grp>();
        DP.reqMap = new HashMap<UUID, Req>();
        DP.mtcMap = new HashMap<UUID, Mtc>();
        DP.busyMap = new HashMap<Arena, Boolean>();
        DP.aliveMap = new HashMap<UUID, Boolean>();
        DP.specMap = new HashMap<UUID, UUID>();
    }
}
