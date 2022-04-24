package dp.cmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import dp.chck.*;
import dp.*;
import me.realized.duels.api.kit.*;
import net.md_5.bungee.api.chat.*;
import org.bukkit.scheduler.*;
import dp.utls.*;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.*;
import net.minecraft.server.v1_8_R3.*;
import java.util.*;

public class Aich implements CommandExecutor
{
    public static Cooldowns duelloArama = new Cooldowns();

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§6[Düello Parti] §aBu komut oyuncular i\u00e7indir.");
            return true;
        }
        final Player player = (Player)sender;
        final Checks checks = new Checks(player);
        if (args.length < 1) {
            player.sendMessage("§6[Düello Parti] §7Kullan\u0131labilecek arg\u00fcmanlar <§eolu\u015ftur, ayr\u0131l, davet, \u00e7\u0131kar, listele, d\u00fcello, izle, bul§7>");
            return true;
        }
        if (args[0].equalsIgnoreCase("listele")) {
            final Grp grp = checks.findGrp();
            if (grp == null) {
                player.sendMessage("§6[Düello Parti] §cHerhangi bir partiye sahip de\u011filsin.");
                return true;
            }
            player.sendMessage(" ");
            player.sendMessage("§6§lPartindeki Üyeler: §7(Üye: " + grp.getMembers().size() + ")");
            for (final UUID var : grp.getMembers()) {
                final Player var2 = Bukkit.getPlayer(var);
                player.sendMessage("§7  ● §e" + var2.getName());
            }
            player.sendMessage("§f ");
            return true;
        }
        else if (args[0].equalsIgnoreCase("bul")) {
            final Grp grp = DP.grpMap.getOrDefault(player.getUniqueId(), null);
            if (grp == null) {
                player.sendMessage("§6[Düello Parti] §cHerhangi bir partinin sahibi de\u011filsin.");
                return true;
            }
            if (duelloArama.onCooldown(player)) {
                player.sendMessage("§6[Düello Parti] §cBu komutu yakın zamanda zaten kullandın!");
                return true;
            }

            duelloArama.applyCooldown(player, 300);
            Bukkit.getScheduler().runTaskLater(DP.dp, new Runnable() {
                @Override
                public void run() {
                    duelloArama.removeCooldown(player);
                }
            }, 200*20);
            final TextComponent text = new TextComponent("§6[Düello Parti] §e" + grp.getOwner().getName() + "§f partisi §6" + grp.getMembers().size() + "vs" + grp.getMembers().size() + " §fiçin bir düello arıyor! §a(Düello göndermek için tıkla!)");
            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aDüello göndermek için tıkla!").create()));
            text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dp d\u00fcello " + grp.getOwner().getName()));
            Bukkit.getOnlinePlayers().forEach(allah -> allah.spigot().sendMessage((BaseComponent)text));
            return true;
        }
        else if (args[0].equalsIgnoreCase("olu\u015ftur")) {
            if (checks.checkGrp()) {
                player.sendMessage("§6[Düello Parti] §cZaten bir partiye sahipsin.");
                return true;
            }
            final Grp grp = new Grp(player.getUniqueId());
            grp.open();
            player.sendMessage("§6[Düello Parti] §aParti ba\u015far\u0131yla kuruldu.");
            return true;
        }
        else if (args[0].equalsIgnoreCase("ayr\u0131l")) {
            final Grp grp = checks.findGrp();
            if (grp == null) {
                player.sendMessage("§6[Düello Parti] §cHerhangi bir partiye sahip de\u011filsin.");
                return true;
            }
            grp.quit(player.getName(), player.getUniqueId());
            player.sendMessage("§6[Düello Parti] §ePartiden ayr\u0131ld\u0131n.");
            return true;
        }
        else if (args[0].equalsIgnoreCase("davet")) {
            final Grp grp = DP.grpMap.getOrDefault(player.getUniqueId(), null);
            if (grp == null) {
                player.sendMessage("§6[Düello Parti] §cHerhangi bir partinin sahibi de\u011filsin.");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage("§6[Düello Parti] §eKullan\u0131m\u0131: davet <hedef>.");
                return true;
            }
            final Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage("§6[Düello Parti] §cHedef al\u0131nan oyuncu bulunamad\u0131.");
                return true;
            }
            if (target.getName().equalsIgnoreCase(player.getName())) {
                player.sendMessage("§6[Düello Parti] §cKendini hedef alamazs\u0131n.");
                return true;
            }
            final Checks target_checks = new Checks(target);
            final Grp target_grp = target_checks.findGrp();
            if (target_grp != null) {
                player.sendMessage("§6[Düello Parti] §cHedef oyuncu ba\u015fka bir partide.");
                return true;
            }
            if (DP.reqMap.containsKey(target.getUniqueId())) {
                player.sendMessage("§6[Düello Parti] §cHedef oyuncunun mevcut bir iste\u011fi var.");
                return true;
            }
            if (grp.getMembers().size() >= 10) {
                player.sendMessage("§6[Düello Parti] §cParti s\u0131n\u0131r\u0131 10 \u00fcyedir.");
                return true;
            }
            final Req req = new Req(Req.ReqType.INVITE, player.getUniqueId(), target.getUniqueId(), null, -1);
            req.add();
            player.sendMessage("§6[Düello Parti] §a\u0130stek ba\u015far\u0131yla g\u00f6nderildi.");
            target.sendMessage("§6[Düello Parti] §2" + player.getName() + " §asana parti iste\u011fi g\u00f6nderdi.");
            final TextComponent text = new TextComponent("§a[Kabul etmek i\u00e7in t\u0131kla]");
            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§a[Kabul etmek i\u00e7in t\u0131kla]").create()));
            text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dp kabul"));
            final TextComponent text2 = new TextComponent("§c[Reddetmek i\u00e7in t\u0131kla]");
            text2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§c[Reddetmek i\u00e7in t\u0131kla]").create()));
            text2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dp reddet"));
            target.spigot().sendMessage((BaseComponent)text);
            target.spigot().sendMessage((BaseComponent)text2);
            new BukkitRunnable() {
                public void run() {
                    final Req vell_req = DP.reqMap.getOrDefault(target.getUniqueId(), null);
                    if (vell_req == null) {
                        this.cancel();
                        return;
                    }
                    if (!vell_req.getSender().equals(player.getUniqueId())) {
                        this.cancel();
                        return;
                    }
                    DP.reqMap.remove(target.getUniqueId());
                    if (player.isOnline()) {
                        player.sendMessage("§6[Düello Parti] §cParti iste\u011fin kabul edilmedi.");
                    }
                    if (target.isOnline()) {
                        target.sendMessage("§6[Düello Parti] §cParti iste\u011fini kabul etmedin.");
                    }
                }
            }.runTaskLater(DP.dp, 200L);
            return true;
        }
        else if (args[0].equalsIgnoreCase("\u00e7\u0131kar")) {
            final Grp grp = DP.grpMap.getOrDefault(player.getUniqueId(), null);
            if (grp == null) {
                player.sendMessage("§6[Düello Parti] §cHerhangi bir partinin sahibi de\u011filsin.");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage("§6[Düello Parti] §eKullan\u0131m\u0131: davet <hedef>.");
                return true;
            }
            final Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage("§6[Düello Parti] §cHedef al\u0131nan parti \u00fcyesi bulunamad\u0131.");
                return true;
            }
            if (target.getName().equalsIgnoreCase(player.getName())) {
                player.sendMessage("§6[Düello Parti] §cKendini hedef alamazs\u0131n.");
                return true;
            }
            if (!grp.getMembers().contains(target.getUniqueId())) {
                player.sendMessage("§6[Düello Parti] §cHedef al\u0131nan parti \u00fcyesi bulunamad\u0131.");
                return true;
            }
            grp.removeMember(target.getUniqueId());
            target.sendMessage("§6[Düello Parti] §4" + target.getName() + " §cpartiden at\u0131ld\u0131.");
            for (final UUID var3 : grp.getMembers()) {
                final Player var4 = Bukkit.getPlayer(var3);
                if (var4 == null) {
                    continue;
                }
                var4.sendMessage("§6[Düello Parti] §4" + target.getName() + " §cpartiden at\u0131ld\u0131.");
            }
            return true;
        }
        else if (args[0].equalsIgnoreCase("kabul")) {
            final Req req2 = DP.reqMap.getOrDefault(player.getUniqueId(), null);
            if (req2 == null) {
                player.sendMessage("§6[Düello Parti] §cSana istek g\u00f6nderen olmam\u0131\u015f.");
                return true;
            }
            if (req2.getReqType().equals(Req.ReqType.INVITE)) {
                if (checks.checkTeamHasDuel(req2.getSender())) {
                    player.sendMessage("§6[Düello Parti] §cKat\u0131lmaya \u00e7al\u0131\u015ft\u0131\u011f\u0131n parti me\u015fgul.");
                    return true;
                }
                DP.reqMap.remove(player.getUniqueId());
                final Player req_sender = Bukkit.getPlayer(req2.getSender());
                if (req_sender == null) {
                    player.sendMessage("§6[Düello Parti] §cKat\u0131lmaya \u00e7al\u0131\u015ft\u0131\u011f\u0131n parti bulunamad\u0131.");
                    return true;
                }
                final Grp grp2 = DP.grpMap.getOrDefault(req2.getSender(), null);
                if (grp2 == null) {
                    player.sendMessage("§6[Düello Parti] §cKat\u0131lmaya \u00e7al\u0131\u015ft\u0131\u011f\u0131n parti bulunamad\u0131.");
                    return true;
                }
                if (grp2.isInMtc()) {
                    player.sendMessage("§6[Düello Parti] §cKat\u0131lmaya \u00e7al\u0131\u015ft\u0131\u011f\u0131n parti me\u015fgul.");
                    return true;
                }
                grp2.addMember(player.getUniqueId());
                for (final UUID var5 : grp2.getMembers()) {
                    final Player var6 = Bukkit.getPlayer(var5);
                    if (var6 == null) {
                        continue;
                    }
                    var6.sendMessage("§6[Düello Parti] §2" + player.getName() + " §apartiye kat\u0131ld\u0131.");
                }
                return true;
            }
            else {
                if (!req2.getReqType().equals(Req.ReqType.DUEL)) {
                    return true;
                }
                final Grp reciever = DP.grpMap.getOrDefault(player.getUniqueId(), null);
                if (reciever == null) {
                    player.sendMessage("§6[Düello Parti] §cHerhangi bir partide de\u011filsin.");
                    return true;
                }
                DP.reqMap.remove(player.getUniqueId());
                final Player req_sender2 = Bukkit.getPlayer(req2.getSender());
                if (req_sender2 == null) {
                    player.sendMessage("§6[Düello Parti] §cD\u00fcello yollayan parti da\u011f\u0131lm\u0131\u015f.");
                    return true;
                }
                final Grp grp3 = DP.grpMap.getOrDefault(req2.getSender(), null);
                if (grp3 == null) {
                    player.sendMessage("§6[Düello Parti] §cD\u00fcello yollayan parti da\u011f\u0131lm\u0131\u015f.");
                    return true;
                }
                if (grp3.isInMtc()) {
                    player.sendMessage("§6[Düello Parti] §4D\u00fcello iste\u011fi g\u00f6nderen parti me\u015fgul.");
                    return true;
                }
                final Checks sender_checks = new Checks(grp3.getOwner());
                if (!sender_checks.checkEco(req2.getBalance()) || !checks.checkEco(req2.getBalance())) {
                    grp3.getOwner().sendMessage("birinizin parasi yetmiyor");
                    player.sendMessage("birinizin parasi yetmiyor");
                    return true;
                }
                grp3.setInMtc(true);
                reciever.setInMtc(true);
                final Mtc mtc = new Mtc(grp3, reciever, req2.getKit(), req2.getBalance());
                DP.mtcMap.put(req2.getSender(), mtc);
                mtc.tryStart();
                return true;
            }
        }
        else if (args[0].equalsIgnoreCase("reddet")) {
            final Req req2 = DP.reqMap.getOrDefault(player.getUniqueId(), null);
            DP.reqMap.remove(player.getUniqueId(), null);
            Player req_sender = Bukkit.getPlayer(req2.getSender());

            if (req_sender == null) {
                System.out.println("[DEBUG] " + req_sender.getName() + " is looking like null!");
                return true;
            }
            player.sendMessage("§6[Düello Parti] §4" + player.getName() + " §cteklifini reddetti.");
            return true;
        }
        else if (args[0].equalsIgnoreCase("d\u00fcello")) {
            if (args.length < 2) {
                player.sendMessage("§6[Düello Parti] §eKullan\u0131m\u0131: d\u00fcello <hedef> <bahis>");
                return true;
            }
            int bet = 0;
            if (args.length >= 3) {
                try {
                    bet = Integer.parseInt(args[2]);
                }
                catch (Exception ex) {}
            }
            final Grp grp4 = DP.grpMap.getOrDefault(player.getUniqueId(), null);
            if (grp4 == null) {
                player.sendMessage("§6[Düello Parti] §cHerhangi bir partinin sahibi de\u011filsin.");
                return true;
            }
            final Player target2 = Bukkit.getPlayer(args[1]);
            if (target2 == null) {
                player.sendMessage("§6[Düello Parti] §cHedef bulunamad\u0131.");
                return true;
            }
            if (target2.getName().equalsIgnoreCase(player.getName())) {
                player.sendMessage("§6[Düello Parti] §cKendini hedef alamazs\u0131n.");
                return true;
            }
            if (grp4.getMembers().contains(target2.getUniqueId())) {
                player.sendMessage("§6[Düello Parti] §cBu oyuncu senin partinde.");
                return true;
            }
            if (checks.checkSendedReq()) {
                player.sendMessage("§6[Düello Parti] §cZaten bir ba\u015fkas\u0131na d\u00fcello iste\u011fi yollam\u0131\u015fs\u0131n.");
                return true;
            }
            final Checks target_checks2 = new Checks(target2);
            final Grp target_grp2 = target_checks2.findGrp();
            if (target_grp2 == null) {
                player.sendMessage("§6[Düello Parti] §cBu oyuncu herhangi bir partide de\u011fil.");
                return true;
            }
            new Gui(player, target2, bet);
            return true;
        }
        else {
            if (!args[0].equalsIgnoreCase("izle")) {
                player.sendMessage("§6[Düello Parti] §7Kullan\u0131labilecek arg\u00fcmanlar <§eolu\u015ftur, ayr\u0131l, davet, \u00e7\u0131kar, listele, d\u00fcello, izle§7>");
                return true;
            }
            if (DP.specMap.containsKey(player.getUniqueId())) {
                DP.specMap.remove(player.getUniqueId());
                Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "mvtp " + player.getUniqueId() + " uzayarena");
                return true;
            }
            if (args.length != 2) {
                player.sendMessage("§6[Düello Parti] §eKullan\u0131m\u0131: izle <hedef>.");
                return true;
            }
            final Player target3 = Bukkit.getPlayer(args[1]);
            if (target3 == null) {
                player.sendMessage("§6[Düello Parti] §cHedef aktif de\u011fil.");
                return true;
            }
            Grp grp4 = null;
            for (final UUID var3 : DP.grpMap.keySet()) {
                final Grp var7 = DP.grpMap.get(var3);
                if (!var7.getMembers().contains(target3.getUniqueId())) {
                    continue;
                }
                grp4 = var7;
                break;
            }
            if (grp4 == null) {
                player.sendMessage("§6[Düello Parti] §cHedef oyuncu herhangi bir pd\u00fcelloda de\u011fil.");
                return true;
            }
            if (!grp4.isInMtc()) {
                player.sendMessage("§6[Düello Parti] §cHedef oyuncu herhangi bir pd\u00fcelloda de\u011fil.");
                return true;
            }
            Mtc mtc2 = null;
            for (final UUID var5 : DP.mtcMap.keySet()) {
                final Mtc var8 = DP.mtcMap.get(var5);
                if (!var8.allMembers().contains(target3.getUniqueId())) {
                    continue;
                }
                mtc2 = var8;
                break;
            }
            if (mtc2 == null) {
                player.sendMessage("§6[Düello Parti] §cHedef oyuncu herhangi bir pd\u00fcelloda de\u011fil.");
                return true;
            }
            if (mtc2.isFinished()) {
                player.sendMessage("§6[Düello Parti] §cHedef oyuncu herhangi bir pd\u00fcelloda de\u011fil.");
                return true;
            }
            if (mtc2.getPos1() == null) {
                player.sendMessage("§6[Düello Parti] §cBir hata olu\u015ftu.");
                return true;
            }
            player.sendMessage("§6[Düello Parti] §eTekrar /dp izle yazarak \u00e7\u0131k\u0131\u015f yapabilirsin.");
            final Mtc mtc3 = mtc2;
            player.teleport(mtc3.getPos1());
            DP.specMap.put(player.getUniqueId(), mtc3.getGameUUID());
            new BukkitRunnable() {
                public void run() {
                    player.setGameMode(GameMode.SPECTATOR);
                    final String msg = "§c\u0130ZLEY\u0130C\u0130";
                    final IChatBaseComponent chatTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + msg + "\",color:" + ChatColor.GOLD.name().toLowerCase() + "}");
                    final PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, chatTitle);
                    final PacketPlayOutTitle length = new PacketPlayOutTitle(5, 20, 5);
                    ((CraftPlayer)player).getHandle().playerConnection.sendPacket((Packet)title);
                    ((CraftPlayer)player).getHandle().playerConnection.sendPacket((Packet)length);
                }
            }.runTaskLater(DP.dp, 20L);
            return true;
        }
    }
}
