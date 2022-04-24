package dp.chck;

import org.bukkit.entity.*;
import dp.*;
import dp.utls.*;
import java.util.*;

public class Checks
{
    private final Player p;
    private final List<String> cmdL;

    public Checks(final Player p) {
        this.cmdL = Arrays.asList("report", "mute", "sus", "sustur", "ban", "eban", "kick", "reports", "ungod", "god", "godoff", "effect");
        this.p = p;
    }

    public boolean checkP() {
        return this.p != null;
    }

    public boolean checkEco(final int bl) {
        return this.checkP() && DP.eco.has(this.p.getName(), (double)bl);
    }

    public boolean checkReq() {
        if (!this.checkP()) {
            return true;
        }
        final Req req = DP.reqMap.getOrDefault(this.p.getUniqueId(), null);
        return req != null;
    }

    public Grp findGrp() {
        Grp grp = null;
        if (!this.checkP()) {
            return null;
        }
        for (final UUID key : DP.grpMap.keySet()) {
            final Grp grp2 = DP.grpMap.get(key);
            if (grp2.getMembers().contains(this.p.getUniqueId())) {
                grp = grp2;
                break;
            }
        }
        return grp;
    }

    public boolean checkSendedReq() {
        boolean value = false;
        for (final UUID var : DP.reqMap.keySet()) {
            final Req var2 = DP.reqMap.get(var);
            if (var2 == null) {
                continue;
            }
            if (var2.getSender().equals(this.p.getUniqueId())) {
                value = true;
                break;
            }
        }
        return value;
    }

    public boolean checkTeamHasDuel(final UUID var) {
        boolean value = false;
        for (final UUID var2 : DP.reqMap.keySet()) {
            final Req var3 = DP.reqMap.get(var2);
            if (var3 == null) {
                continue;
            }
            if (!var3.getSender().equals(var)) {
                continue;
            }
            if (var3.getReqType().equals(Req.ReqType.DUEL)) {
                value = true;
                break;
            }
            break;
        }
        if (DP.reqMap.containsKey(var)) {
            value = true;
        }
        return value;
    }

    public boolean checkGrp() {
        if (!this.checkP()) {
            return false;
        }
        for (final UUID key : DP.grpMap.keySet()) {
            final Grp grp = DP.grpMap.get(key);
            if (grp.getMembers().contains(this.p.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public boolean checkGame() {
        if (!this.checkP()) {
            return false;
        }
        for (final UUID key : DP.grpMap.keySet()) {
            final Grp grp = DP.grpMap.get(key);
            if (grp.getMembers().contains(this.p.getUniqueId())) {
                return grp.isInMtc();
            }
        }
        return false;
    }

    public boolean checkCmd(final String cmd) {
        return this.checkP() && this.cmdL.contains(cmd);
    }
}
