package dp.utls;

import dp.*;
import org.bukkit.*;
import java.util.*;
import org.bukkit.entity.*;

public class Grp
{
    private final UUID creator;
    private boolean inMtc;
    private final List<UUID> members;

    public Grp(final UUID creator) {
        this.inMtc = false;
        this.members = new ArrayList<UUID>();
        this.creator = creator;
    }

    public void addMember(final UUID u) {
        this.members.add(u);
        DP.ds.getQueueManager().removeFromQueue(Bukkit.getPlayer(u));
    }

    public void removeMember(final UUID u) {
        this.members.remove(u);
    }

    public List<UUID> getMembers() {
        return this.members;
    }

    public boolean isInMtc() {
        return this.inMtc;
    }

    public void setInMtc(final boolean inMtc) {
        this.inMtc = inMtc;
    }

    public void quit(final String name, final UUID uuid) {
        this.members.remove(uuid);
        if (this.creator == uuid) {
            this.close();
        }
        for (final UUID var : this.members) {
            final Player varP = Bukkit.getPlayer(var);
            varP.sendMessage("§6[Düello Parti] §6" + name + " §epartiden ayr\u0131ld\u0131.");
            varP.playSound(varP.getLocation(), Sound.VILLAGER_NO, 5F, 5F);
        }
    }

    public void open() {
        DP.grpMap.put(this.creator, this);
        this.addMember(this.creator);
    }

    private void close() {
        if (this.isInMtc()) {
            for (final UUID var : this.getMembers()) {
                final Player var2 = Bukkit.getPlayer(var);
                if (var2 == null) {
                    continue;
                }
                DP.aliveMap.put(var, false);
            }
        }
        DP.grpMap.remove(this.creator);
        for (final UUID var : this.members) {
            final Player varP = Bukkit.getPlayer(var);
            varP.sendMessage("§6[Düello Parti] §cParti sahibi oyundan ayrıldı, parti dağıldı!");
            varP.playSound(varP.getLocation(), Sound.VILLAGER_NO, 5F, 5F);
        }
    }

    public Player getOwner() {
        return Bukkit.getPlayer(this.creator);
    }
}
