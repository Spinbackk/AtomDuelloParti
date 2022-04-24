package dp.utls;

import org.bukkit.entity.*;
import org.bukkit.*;
import dp.*;
import me.realized.duels.api.kit.*;
import org.bukkit.inventory.*;
import java.util.*;

public class Gui
{
    public Gui(final Player sender, final Player target, final int bet) {
        final Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, sender.getName() + " - " + target.getName() + " - " + bet);
        int current = 0;
        for (final Kit kit : DP.ds.getKitManager().getKits()) {
            inv.setItem(current, kit.getDisplayed());
            ++current;
        }
        sender.openInventory(inv);
    }
}
