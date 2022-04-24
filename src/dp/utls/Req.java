package dp.utls;

import java.util.*;
import me.realized.duels.api.kit.*;
import dp.*;

public class Req
{
    private final ReqType reqType;
    private final UUID sender;
    private final UUID target;
    private final Kit kit;
    private final int balance;

    public Req(final ReqType reqType, final UUID sender, final UUID target, final Kit kit, final int balance) {
        this.reqType = reqType;
        this.sender = sender;
        this.target = target;
        this.kit = kit;
        this.balance = balance;
    }

    public void add() {
        DP.reqMap.put(this.target, this);
    }

    public UUID getSender() {
        return this.sender;
    }

    public ReqType getReqType() {
        return this.reqType;
    }

    public int getBalance() {
        return this.balance;
    }

    public Kit getKit() {
        return this.kit;
    }

    public enum ReqType
    {
        INVITE,
        DUEL;
    }
}
