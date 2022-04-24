package dp.evnts;

import dp.*;
import net.minecraft.server.v1_8_R3.Material;
import org.bukkit.scheduler.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.*;
import net.minecraft.server.v1_8_R3.*;
import dp.chck.*;
import org.bukkit.event.block.*;
import org.bukkit.*;
import dp.utls.*;
import net.md_5.bungee.api.chat.*;
import me.realized.duels.api.kit.*;
import org.bukkit.event.player.*;
import org.bukkit.event.inventory.*;
import me.realized.duels.api.event.spectate.*;
import me.realized.duels.api.event.queue.*;
import me.realized.duels.api.event.request.*;
import org.bukkit.event.entity.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import java.util.*;

public class DPListeners implements Listener
{
    public static final List<Location> placed;

    private void makeSpec(final Player p) {
        Grp grp = null;
        for (final UUID var : DP.grpMap.keySet()) {
            final Grp var2 = DP.grpMap.get(var);
            if (!var2.getMembers().contains(p.getUniqueId())) {
                continue;
            }
            grp = var2;
            break;
        }
        if (grp == null) {
            return;
        }
        if (!grp.isInMtc()) {
            return;
        }
        Mtc mtc = null;
        for (final UUID var3 : DP.mtcMap.keySet()) {
            final Mtc var4 = DP.mtcMap.get(var3);
            if (!var4.allMembers().contains(p.getUniqueId())) {
                continue;
            }
            mtc = var4;
            break;
        }
        if (mtc == null) {
            return;
        }
        if (mtc.isFinished()) {
            return;
        }
        if (mtc.getPos1() == null) {
            return;
        }
        if (p.isDead()) {
            p.spigot().respawn();
        }
        final Mtc mtc2 = mtc;
        new BukkitRunnable() {
            public void run() {
                p.teleport(mtc2.getPos1());
                Bukkit.getScheduler().runTaskLater(DP.dp, new Runnable() {
                    @Override
                    public void run() {
                        p.spigot().respawn();
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode 3 " + p.getName());
                        p.setGameMode(GameMode.SPECTATOR);
                    }
                }, 2*20);
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode 3 " + p.getName());
                p.playSound(p.getLocation(), Sound.WOLF_DEATH, 5F, 5F);
                p.sendMessage("§cRakibin tarafından öldürüldün!");
                final String msg = "§c\u0130ZLEY\u0130C\u0130";
                final IChatBaseComponent chatTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + msg + "\",color:" + ChatColor.GOLD.name().toLowerCase() + "}");
                final PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, chatTitle);
                final PacketPlayOutTitle length = new PacketPlayOutTitle(5, 20, 5);
                ((CraftPlayer)p).getHandle().playerConnection.sendPacket((Packet)title);
                ((CraftPlayer)p).getHandle().playerConnection.sendPacket((Packet)length);
            }
        }.runTaskLater(DP.dp, 20L);
    }

    @EventHandler
    public void on(final PlayerRespawnEvent e) {
        final Object check = DP.aliveMap.getOrDefault(e.getPlayer().getUniqueId(), null);
        if (check == null) {
            return;
        }
        if (!(boolean)check) {
            return;
        }
        new BukkitRunnable() {
            public void run() {
                DPListeners.this.makeSpec(e.getPlayer());
            }
        }.runTaskLater(DP.dp, 20L);
    }

    @EventHandler
    public void on(final PlayerDeathEvent e) {
        final Object check = DP.aliveMap.getOrDefault(e.getEntity().getUniqueId(), null);
        if (check == null) {
            return;
        }
        if (!(boolean)check) {
            return;
        }
        DP.aliveMap.put(e.getEntity().getUniqueId(), false);
        e.getDrops().clear();
        new BukkitRunnable() {
            public void run() {
                DPListeners.this.makeSpec(e.getEntity());
            }
        }.runTaskLater(DP.dp, 20L);
    }

    @EventHandler
    public void on(final PlayerKickEvent e) {
        final Object check = DP.aliveMap.getOrDefault(e.getPlayer().getUniqueId(), null);
        final Checks checks = new Checks(e.getPlayer());
        final Grp grp = checks.findGrp();
        if (grp == null) {
            return;
        }
        grp.removeMember(e.getPlayer().getUniqueId());
        if (check == null) {
            return;
        }
        if (!(boolean)check) {
            return;
        }
        DP.aliveMap.put(e.getPlayer().getUniqueId(), false);
    }

    @EventHandler
    public void on(final PlayerQuitEvent e) {
        final Object check = DP.aliveMap.getOrDefault(e.getPlayer().getUniqueId(), null);
        final Checks checks = new Checks(e.getPlayer());
        final Grp grp = checks.findGrp();
        if (grp == null) {
            return;
        }
        grp.removeMember(e.getPlayer().getUniqueId());
        if (check == null) {
            return;
        }
        if (!(boolean)check) {
            return;
        }
        DP.aliveMap.put(e.getPlayer().getUniqueId(), false);
    }

    @EventHandler
    public void on(final PlayerJoinEvent e) {
        final Object check = DP.aliveMap.getOrDefault(e.getPlayer().getUniqueId(), null);
        if (check == null) {
            return;
        }
        new BukkitRunnable() {
            public void run() {
                DPListeners.this.makeSpec(e.getPlayer());
            }
        }.runTaskLater(DP.dp, 20L);
    }

    @EventHandler
    public void on(final BlockBreakEvent e) {
        final Checks check = new Checks(e.getPlayer());
        if (!check.checkGame()) {
            return;
        }
        if (e.getBlock().getType().equals((Object) Material.WEB)) {
            return;
        }
        if (DPListeners.placed.contains(e.getBlock().getLocation())) {
            DPListeners.placed.remove(e.getBlock().getLocation());
            return;
        }
        e.setCancelled(true);
        e.getPlayer().sendMessage("§6[Düello Parti] §4Arenaya zarar veremezsin!");
    }

    @EventHandler
    public void on(final BlockPlaceEvent e) {
        final Checks check = new Checks(e.getPlayer());
        if (!check.checkGame()) {
            return;
        }
        if (e.getBlock().getType().equals((Object)Material.WEB)) {
            return;
        }
        if (!DPListeners.placed.contains(e.getBlockPlaced().getLocation())) {
            DPListeners.placed.add(e.getBlockPlaced().getLocation());
        }
    }

    @EventHandler
    public void on(final InventoryClickEvent e) {
        if (e.getClickedInventory() == null) {
            return;
        }
        if (e.getClickedInventory().getTitle() == null) {
            return;
        }
        final String title = e.getClickedInventory().getTitle();
        if (title.split(" - ").length < 3) {
            return;
        }
        e.setCancelled(true);
        if (e.getCurrentItem() == null) {
            return;
        }
        if (!e.getCurrentItem().hasItemMeta()) {
            return;
        }
        String kit_name = e.getCurrentItem().getItemMeta().getDisplayName();
        if (kit_name == null) {
            return;
        }
        kit_name = kit_name.replace("§l", "");
        kit_name = kit_name.replace("§0", "");
        kit_name = kit_name.replace("§1", "");
        kit_name = kit_name.replace("§2", "");
        kit_name = kit_name.replace("§3", "");
        kit_name = kit_name.replace("§4", "");
        kit_name = kit_name.replace("§5", "");
        kit_name = kit_name.replace("§6", "");
        kit_name = kit_name.replace("§7", "");
        kit_name = kit_name.replace("§8", "");
        kit_name = kit_name.replace("§9", "");
        e.getWhoClicked().sendMessage(kit_name);
        final Kit kit = DP.ds.getKitManager().get(kit_name);
        if (kit == null) {
            return;
        }
        final String sender_name = title.split(" - ")[0];
        final String target_name = title.split(" - ")[1];
        final int bet = Integer.parseInt(title.split(" - ")[2]);
        final Player sender = Bukkit.getPlayer(sender_name);
        final Player target = Bukkit.getPlayer(target_name);
        e.getWhoClicked().closeInventory();
        if (sender == null || target == null) {
            e.getWhoClicked().sendMessage("§6[Düello Parti] §cHedef bulunamad\u0131.");
            return;
        }
        final Checks target_checks = new Checks(target);
        final Grp target_grp = target_checks.findGrp();
        if (target_grp.getOwner() == null) {
            e.getWhoClicked().sendMessage("§6[Düello Parti] §cHedef parti sahibi bulunamad\u0131.");
            return;
        }
        if (target_grp.getOwner().getName().equalsIgnoreCase(e.getWhoClicked().getName())) {
            e.getWhoClicked().sendMessage("§6[Düello Parti] §cKendini hedef alamazs\u0131n.");
            return;
        }
        if (target_checks.checkReq()) {
            sender.sendMessage("§6[Düello Parti] §cHedef partinin ba\u015fka bir iste\u011fi mevcut.");
            return;
        }
        final Grp grp = DP.grpMap.get(sender.getUniqueId());
        if (grp == null) {
            return;
        }
        final Req req = new Req(Req.ReqType.DUEL, sender.getUniqueId(), target_grp.getOwner().getUniqueId(), kit, bet);
        DP.reqMap.put(target_grp.getOwner().getUniqueId(), req);
        target_grp.getOwner().sendMessage(" ");
        target_grp.getOwner().sendMessage("§6§l" + sender.getName() + " Partisi");
        target_grp.getOwner().sendMessage("  §fKit: §a" + req.getKit().getName());
        target_grp.getOwner().sendMessage("  §fBahis: §e" + req.getBalance());
        target_grp.getOwner().sendMessage("  §fTakımın: §e" + target_grp.getMembers().size());
        target_grp.getOwner().sendMessage("  §fRakibin: §e" + grp.getMembers().size());
        target_grp.getOwner().sendMessage(" ");
        target_grp.getOwner().playSound(target_grp.getOwner().getLocation(), Sound.ORB_PICKUP, 5F, 5F);
        sender.sendMessage("§fPartinde toplam §6" + grp.getMembers().size() + "§f kişi bulunuyor.");
        sender.sendMessage("§fRakibin partisinde toplam §6" + target_grp.getMembers().size() + "§f kişi bulunuyor.");
        final TextComponent text = new TextComponent("§a§lKABUL ETMEK İÇİN TIKLA!");
        text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§a§lKABUL ETMEK İÇİN TIKLA!").create()));
        text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dp kabul"));
        final TextComponent text2 = new TextComponent("§c§lREDDETMEK İÇİN TIKLA!");
        text2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§c§lREDDETMEK İÇİN TIKLA!").create()));
        text2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dp reddet"));
        target_grp.getOwner().spigot().sendMessage((BaseComponent)text);
        target_grp.getOwner().spigot().sendMessage((BaseComponent)text2);
        sender.sendMessage("§6[Düello Parti] §eDüello isteği §6" + target_grp.getOwner().getName() + " §epartisine gönderildi.");
        new BukkitRunnable() {
            public void run() {
                Req vell_req = DP.reqMap.getOrDefault(target_grp.getOwner().getUniqueId(), null); //TODO: final
                if (vell_req == null) {
                    this.cancel();
                    return;
                }
                if (!vell_req.getSender().equals(sender.getUniqueId())) {
                    this.cancel();
                    return;
                }
                DP.reqMap.remove(target_grp.getOwner().getUniqueId());
                if (sender.isOnline()) {
                    sender.sendMessage("§6[Düello Parti] §cD\u00fcello iste\u011fin kabul edilmedi.");
                }
                if (target_grp.getOwner().isOnline()) {
                    target_grp.getOwner().sendMessage("§6[Düello Parti] §c" + sender.getName() + " adlı oyuncudan gelen düello isteğini kabul etmedin.");
                }
            }
        }.runTaskLater(DP.dp, 200L);
    }

    @EventHandler
    public void on(final PlayerTeleportEvent e) {
        final Checks check = new Checks(e.getPlayer());
        if (!check.checkGame()) {
            return;
        }
        if (e.getCause().equals((Object)PlayerTeleportEvent.TeleportCause.PLUGIN)) {
            return;
        }
        if (e.getCause().equals((Object)PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            return;
        }
        e.setCancelled(true);
        e.getPlayer().sendMessage("§6[Düello Parti] §4D\u00fcello s\u0131ras\u0131nda bunu yapamazs\u0131n!");
    }

    @EventHandler(ignoreCancelled = true)
    public void on_x(final PlayerTeleportEvent e) {
        if (DP.specMap.containsKey(e.getPlayer().getUniqueId()) && e.getCause().equals((Object)PlayerTeleportEvent.TeleportCause.SPECTATE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(final PlayerCommandPreprocessEvent e) {
        final String msg = e.getMessage().split(" ")[0].toLowerCase().replaceFirst("/", "");
        final Checks checks = new Checks(e.getPlayer());
        if (!checks.checkGame()) {
            return;
        }
        if (checks.checkCmd(msg)) {
            return;
        }
        e.setCancelled(true);
        e.getPlayer().sendMessage("§6[Düello Parti] §4D\u00fcello s\u0131ras\u0131nda bunu yapamazs\u0131n!");
    }

    @EventHandler
    public void on(final InventoryOpenEvent e) {
        final Checks checks = new Checks((Player)e.getPlayer());
        if (!checks.checkGame()) {
            return;
        }
        e.setCancelled(true);
        e.getPlayer().sendMessage("§6[Düello Parti] §4D\u00fcello s\u0131ras\u0131nda bunu yapamazs\u0131n!");
    }

    @EventHandler
    public void on(final SpectateStartEvent e) {
        final Player p = e.getSource();
        final Checks checks = new Checks(p);
        final Grp grp = checks.findGrp();
        if (grp == null) {
            return;
        }
        e.setCancelled(true);
        p.sendMessage("§6[Düello Parti] §cBunu yapabilmek i\u00e7in partiden ayr\u0131lmal\u0131s\u0131n!");
    }

    @EventHandler
    public void on(final QueueJoinEvent e) {
        final Player p = e.getSource();
        final Checks checks = new Checks(p);
        final Grp grp = checks.findGrp();
        if (grp == null) {
            return;
        }
        e.setCancelled(true);
        p.sendMessage("§6[Düello Parti] §cBunu yapabilmek i\u00e7in partiden ayr\u0131lmal\u0131s\u0131n!");
    }

    @EventHandler
    public void on(final RequestAcceptEvent e) {
        final Player p = e.getSource();
        final Checks checks = new Checks(p);
        final Grp grp = checks.findGrp();
        if (grp == null) {
            return;
        }
        e.setCancelled(true);
        p.sendMessage("§6[Düello Parti] §cBunu yapabilmek i\u00e7in partiden ayr\u0131lmal\u0131s\u0131n!");
    }

    @EventHandler
    public void on(final RequestSendEvent e) {
        final Player p = e.getSource();
        final Checks checks = new Checks(p);
        final Grp grp = checks.findGrp();
        if (grp == null) {
            return;
        }
        e.setCancelled(true);
        p.sendMessage("§6[Düello Parti] §cBunu yapabilmek i\u00e7in partiden ayr\u0131lmal\u0131s\u0131n!");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(final EntityDamageByEntityEvent e) {
        if (!e.getEntity().getType().equals((Object)EntityType.PLAYER)) {
            return;
        }
        if (!e.getDamager().getType().equals((Object)EntityType.PLAYER)) {
            return;
        }
        final Player v = (Player)e.getEntity();
        final Player a = (Player)e.getDamager();
        final Checks v_check = new Checks(v);
        final Grp grp = v_check.findGrp();
        if (grp == null) {
            return;
        }
        if (grp.isInMtc()) {
            e.setCancelled(grp.getMembers().contains(a.getUniqueId()));
        }
    }

    static {
        placed = new ArrayList<Location>();
    }

}
