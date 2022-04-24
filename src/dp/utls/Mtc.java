package dp.utls;

import me.realized.duels.api.kit.*;
import me.realized.duels.api.arena.*;
import dp.*;
import org.bukkit.scheduler.*;
import org.bukkit.entity.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.*;
import org.bukkit.*;
import net.minecraft.server.v1_8_R3.*;
import java.util.*;
import org.bukkit.command.*;
import org.bukkit.event.player.*;
import org.bukkit.event.*;

public class Mtc
{
    private final UUID gameUUID;
    private boolean isFinished;
    private final Grp sender;
    private final Grp reciever;
    private final Kit kit;
    private final int bet;
    private Location pos1;
    private Arena arena;

    public Mtc(final Grp sender, final Grp reciever, final Kit kit, final int bet) {
        this.isFinished = false;
        this.gameUUID = sender.getOwner().getUniqueId();
        this.sender = sender;
        this.reciever = reciever;
        this.kit = kit;
        this.bet = bet;
    }

    public Location getPos1() {
        return this.pos1;
    }

    public UUID getGameUUID() {
        return this.gameUUID;
    }

    public List<UUID> allMembers() {
        final List<UUID> list = new ArrayList<UUID>();
        list.addAll(this.sender.getMembers());
        list.addAll(this.reciever.getMembers());
        return list;
    }

    public boolean isFinished() {
        return this.isFinished;
    }

    public void tryStart() {
        if (!this.teleportSafe()) {
            this.sender.setInMtc(false);
            this.reciever.setInMtc(false);
            DP.mtcMap.remove(this.gameUUID);
            this.sendMessage("§6[Düello Parti] §cTak\u0131m \u00fcyelerinden birisi limanda olmad\u0131\u011f\u0131 i\u00e7in oyun iptal edildi.");
            return;
        }
        final Arena arena = this.findArena();
        if (arena == null) {
            this.sender.setInMtc(false);
            this.reciever.setInMtc(false);
            DP.mtcMap.remove(this.gameUUID);
            this.sendMessage("§6[Düello Parti] §cM\u00fcsait arena bulunamad\u0131.");
            return;
        }
        this.arena = arena;
        DP.busyMap.put(arena, true);
        arena.setDisabled(true);
        this.pos1 = arena.getPosition(1);
        this.teleport(arena.getPosition(1), arena.getPosition(2));
        new BukkitRunnable() {
            public void run() {
                Mtc.this.equip();
            }
        }.runTaskLater(DP.dp, 3L);
        this.waitStart(arena.getPosition(1), arena.getPosition(2));
        //this.clearEffects(); TODO: gerekli olabilir
        this.takeBet(this.sender.getOwner(), this.reciever.getOwner());
        this.putDeath();
        new BukkitRunnable() {
            public void run() {
                if (Mtc.this.checkLose(Mtc.this.sender)) {
                    Mtc.this.isFinished = true;
                    Mtc.this.sendMessage("");
                    Mtc.this.sendMessage("§f● §6§lDüello Sonuçları");
                    Mtc.this.sendMessage("§f  Kazanan: §a" + Mtc.this.reciever.getOwner().getName() + " Partisi §7(" + Mtc.this.reciever.getMembers().size() + ")");
                    Mtc.this.sendMessage("§f  Kaybeden: §c" + Mtc.this.sender.getOwner().getName() + " Partisi §7(" + Mtc.this.sender.getMembers().size() + ")");
                    Mtc.this.sendMessage("§f  Bahis: §e" + Mtc.this.bet);
                    Mtc.this.sendMessage("");
                    Mtc.this.sender.setInMtc(false);
                    Mtc.this.reciever.setInMtc(false);
                    Mtc.this.giveWinnerBet(Mtc.this.reciever.getOwner());
                    Mtc.this.backSpawn();
                    Mtc.this.clearArena(arena);
                    DP.mtcMap.remove(Mtc.this.gameUUID);
                    Mtc.this.stopSpec();
                    this.cancel();
                    return;
                }
                if (Mtc.this.checkLose(Mtc.this.reciever)) {
                    Mtc.this.isFinished = true;
                    Mtc.this.sendMessage("");
                    Mtc.this.sendMessage("§f● §6§lDüello Sonuçları");
                    Mtc.this.sendMessage("§f  Kazanan: §a" + Mtc.this.sender.getOwner().getName() + " Partisi §7(" + Mtc.this.sender.getMembers().size() + ")");
                    Mtc.this.sendMessage("§f  Kaybeden: §c" + Mtc.this.reciever.getOwner().getName() + " Partisi §7(" + Mtc.this.reciever.getMembers().size() + ")");
                    Mtc.this.sendMessage("§f  Bahis: §e" + Mtc.this.bet);
                    Mtc.this.sendMessage("");
                    Mtc.this.sender.setInMtc(false);
                    Mtc.this.reciever.setInMtc(false);
                    Mtc.this.giveWinnerBet(Mtc.this.sender.getOwner());
                    Mtc.this.backSpawn();
                    Mtc.this.clearArena(arena);
                    DP.mtcMap.remove(Mtc.this.gameUUID);
                    Mtc.this.stopSpec();
                    this.cancel();
                }
            }
        }.runTaskTimer(DP.dp, 200L, 20L);
    }

    private void clearEffects() {
        for (final UUID all : this.reciever.getMembers()) {
            final Player allah = Bukkit.getPlayer(all);
            allah.getActivePotionEffects().clear();
        }
        //
        for (final UUID all2 : this.sender.getMembers()) {
            final Player allah2 = Bukkit.getPlayer(all2);
            allah2.getActivePotionEffects().clear();
        }
    }

    private void time_s(final int time) {
        final List<Player> players = new ArrayList<Player>();
        for (final UUID var : DP.specMap.keySet()) {
            final UUID var2 = DP.specMap.get(var);
            if (!var2.equals(this.getGameUUID())) {
                continue;
            }
            final Player p = Bukkit.getPlayer(var);
            players.add(p);
        }
        for (final UUID var : this.sender.getMembers()) {
            final Player var3 = Bukkit.getPlayer(var);
            if (var3 != null) {
                players.add(var3);
            }
        }
        for (final UUID var : this.reciever.getMembers()) {
            final Player var3 = Bukkit.getPlayer(var);
            if (var3 != null) {
                players.add(var3);
            }
        }
        final String message = "§6§lDÜELLO! " + time + " SANIYE!";
        for (final Player var4 : players) {
            final PacketPlayOutChat packet = new PacketPlayOutChat((IChatBaseComponent)new ChatComponentText(message), (byte)2);
            ((CraftPlayer)var4).getHandle().playerConnection.sendPacket((Packet)packet);
        }
    }

    private void stopSpec() {
        for (final UUID var : DP.specMap.keySet()) {
            final UUID var2 = DP.specMap.get(var);
            if (!var2.equals(this.getGameUUID())) {
                continue;
            }
            final Player p = Bukkit.getPlayer(var);
            if (p == null) {
                continue;
            }
            p.setGameMode(GameMode.SURVIVAL);
            final Location loc = Bukkit.getWorld("uzayarena").getSpawnLocation();
            if (loc == null) {
                continue;
            }
            p.teleport(loc);
            DP.specMap.remove(var);
        }
    }

    private void putDeath() {
        for (final UUID var : this.sender.getMembers()) {
            DP.aliveMap.put(var, true);
        }
        for (final UUID var : this.reciever.getMembers()) {
            DP.aliveMap.put(var, true);
        }
    }

    private boolean checkLose(final Grp grp) {
        boolean value = true;
        for (final UUID var : grp.getMembers()) {
            final boolean var2 = DP.aliveMap.getOrDefault(var, false);
            if (var2) {
                value = false;
                break;
            }
        }
        return value;
    }

    private void tieGame() {
        DP.eco.depositPlayer((OfflinePlayer)this.reciever.getOwner(), (double)this.getBet());
        DP.eco.depositPlayer((OfflinePlayer)this.sender.getOwner(), (double)this.getBet());
        this.sender.setInMtc(false);
        this.reciever.setInMtc(false);
        this.sendTitle("§c§lOYUN BİTTİ!");
        this.backSpawn();
        this.stopSpec();
        this.clearArena(this.arena);
    }

    private void takeBet(final Player senderOwner, final Player recieverOwner) {
        DP.eco.bankWithdraw(senderOwner.getName(), (double)this.getBet());
        DP.eco.bankWithdraw(recieverOwner.getName(), (double)this.getBet());
    }

    private void giveWinnerBet(final Player winner) {
        DP.eco.bankDeposit(winner.getName(), (double)(this.getBet() * 2));
    }

    private void waitStart(final Location senderLoc, final Location recieverLoc) {
        final int[] countdown = { 200 };
        new BukkitRunnable() {
            public void run() {
                for (final UUID var : Mtc.this.sender.getMembers()) {
                    final Player var2 = Bukkit.getPlayer(var);
                    if (var2 == null) {
                        continue;
                    }
                    final int mustX = senderLoc.getBlockX();
                    final int mustZ = senderLoc.getBlockZ();
                    final int nowX = var2.getLocation().getBlockX();
                    final int nowZ = var2.getLocation().getBlockZ();
                    if (mustX - nowX <= 3 && mustX - nowX >= -3 && mustZ - nowZ <= 3 && mustZ - nowZ >= -3) {
                        continue;
                    }
                    var2.teleport(senderLoc);
                }
                for (final UUID var : Mtc.this.reciever.getMembers()) {
                    final Player var2 = Bukkit.getPlayer(var);
                    if (var2 == null) {
                        continue;
                    }
                    final int mustX = recieverLoc.getBlockX();
                    final int mustZ = recieverLoc.getBlockZ();
                    final int nowX = var2.getLocation().getBlockX();
                    final int nowZ = var2.getLocation().getBlockZ();
                    if (mustX - nowX <= 3 && mustX - nowX >= -3 && mustZ - nowZ <= 3 && mustZ - nowZ >= -3) {
                        continue;
                    }
                    var2.teleport(recieverLoc);
                }
                final int[] val$countdown = countdown;
                final int n = 0;
                --val$countdown[n];
                if (countdown[0] <= 0) {
                    this.cancel();
                }
            }
        }.runTaskTimer(DP.dp, 1L, 1L);
        final int[] message = { 10 };
        new BukkitRunnable() {
            public void run() {
                Mtc.this.sendTitle("§6" + message[0] + " §esaniye kaldı!");
                final int[] val$message = message;
                final int n = 0;
                --val$message[n];
                if (message[0] <= 0) {
                    Mtc.this.sendTitle("§a§lOYUN BAŞLADI!");
                    this.cancel();
                }
            }
        }.runTaskTimer(DP.dp, 1L, 20L);
    }

    private void sendTitle(final String msg) {
        final IChatBaseComponent chatTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + msg + "\",color:" + ChatColor.GOLD.name().toLowerCase() + "}");
        final PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, chatTitle);
        final PacketPlayOutTitle length = new PacketPlayOutTitle(5, 20, 5);
        final List<UUID> tpList = new ArrayList<UUID>();
        tpList.addAll(this.sender.getMembers());
        tpList.addAll(this.getReciever().getMembers());
        for (final UUID var : tpList) {
            final Player var2 = Bukkit.getPlayer(var);
            ((CraftPlayer)var2).getHandle().playerConnection.sendPacket((Packet)title);
            ((CraftPlayer)var2).getHandle().playerConnection.sendPacket((Packet)length);
        }
    }

    private void sendMessage(final String msg) {
        final List<UUID> tpList = new ArrayList<UUID>();
        tpList.addAll(this.sender.getMembers());
        tpList.addAll(this.getReciever().getMembers());
        for (final UUID var : tpList) {
            final Player var2 = Bukkit.getPlayer(var);
            var2.sendMessage(msg);
        }
    }

    private Arena findArena() {
        Arena arena = null;
        for (final Arena ignored : DP.ds.getArenaManager().getArenas()) {
            final Random rand = new Random();
            final Arena randArena = DP.ds.getArenaManager().getArenas().get(rand.nextInt(DP.ds.getArenaManager().getArenas().size() - 1));
            if (randArena.isUsed()) {
                continue;
            }
            if (DP.busyMap.getOrDefault(randArena, false)) {
                continue;
            }
            if (randArena.isDisabled()) {
                continue;
            }
            arena = randArena;
            break;
        }
        return arena;
    }

    private void equip() {
        final List<UUID> pList = new ArrayList<UUID>();
        pList.addAll(this.sender.getMembers());
        pList.addAll(this.reciever.getMembers());
        for (final UUID var : pList) {
            final Player var2 = Bukkit.getPlayer(var);
            if (var2 == null) {
                continue;
            }
            this.kit.equip(var2);
        }
    }

    private void backSpawn() {
        new BukkitRunnable() {
            public void run() {
                final Location loc = Bukkit.getWorld("uzayarena").getSpawnLocation();
                if (loc == null) {
                    return;
                }
                for (final UUID var : Mtc.this.sender.getMembers()) {
                    final Player var2 = Bukkit.getPlayer(var);
                    if (var2 == null) {
                        continue;
                    }
                    new BukkitRunnable() {
                        public void run() {
                            if (var2.getWorld().getName().equalsIgnoreCase("uzayarena")) {
                                this.cancel();
                            }
                            Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "mvtp " + var2.getName() + " uzayarena");
                        }
                    }.runTaskTimer(DP.dp, 20L, 5L);
                }
                for (final UUID var : Mtc.this.reciever.getMembers()) {
                    final Player var2 = Bukkit.getPlayer(var);
                    if (var2 == null) {
                        continue;
                    }
                    new BukkitRunnable() {
                        public void run() {
                            if (var2.getWorld().getName().equalsIgnoreCase("uzayarena")) {
                                this.cancel();
                            }
                            Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "mvtp " + var2.getName() + " uzayarena");
                        }
                    }.runTaskTimer(DP.dp, 20L, 5L);
                }
            }
        }.runTaskLater(DP.dp, 20L);
    }

    private void clearArena(final Arena arena) {
        arena.setDisabled(false);
        DP.busyMap.remove(arena);
        //Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "duels ar reset " + arena.getName());
    }

    private void teleport(final Location senderLoc, final Location recieverLoc) {
        for (final UUID var : this.sender.getMembers()) {
            final Player var2 = Bukkit.getPlayer(var);
            if (var2 == null) {
                continue;
            }
            final PlayerTeleportEvent pte = new PlayerTeleportEvent(var2, var2.getLocation(), senderLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            DP.dp.getServer().getPluginManager().callEvent((Event)pte);
            pte.setCancelled(false);
            if (!pte.isCancelled()) {
                continue;
            }
            DP.dp.getServer().getPluginManager().callEvent((Event)pte);
            pte.setCancelled(false);
            if (!pte.isCancelled()) {
                continue;
            }
            DP.dp.getServer().getPluginManager().callEvent((Event)pte);
        }
        for (final UUID var : this.reciever.getMembers()) {
            final Player var2 = Bukkit.getPlayer(var);
            if (var2 == null) {
                continue;
            }
            final PlayerTeleportEvent pte = new PlayerTeleportEvent(var2, var2.getLocation(), recieverLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            DP.dp.getServer().getPluginManager().callEvent((Event)pte);
            pte.setCancelled(false);
            if (!pte.isCancelled()) {
                continue;
            }
            DP.dp.getServer().getPluginManager().callEvent((Event)pte);
            pte.setCancelled(false);
            if (!pte.isCancelled()) {
                continue;
            }
            DP.dp.getServer().getPluginManager().callEvent((Event)pte);
        }
    }

    private boolean teleportSafe() {
        boolean value = true;
        final List<UUID> tpList = new ArrayList<UUID>();
        tpList.addAll(this.sender.getMembers());
        tpList.addAll(this.getReciever().getMembers());
        for (final UUID var : tpList) {
            final Player var2 = Bukkit.getPlayer(var);
            if (!var2.getWorld().getName().equals("uzayarena")) {
                value = false;
                break;
            }
        }
        return value;
    }

    public int getBet() {
        return this.bet;
    }

    public Grp getSender() {
        return this.sender;
    }

    public Grp getReciever() {
        return this.reciever;
    }
}
