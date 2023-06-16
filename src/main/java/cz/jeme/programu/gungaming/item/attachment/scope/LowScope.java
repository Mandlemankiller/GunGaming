package cz.jeme.programu.gungaming.item.attachment.scope;

import cz.jeme.programu.gungaming.item.attachment.ModifiersInfo;
import cz.jeme.programu.gungaming.loot.Rarity;
import org.bukkit.Material;

public class LowScope extends Scope {
    @Override
    protected void setup() {
        material = Material.WOODEN_PICKAXE;
        name = "Low Scope";
        info = "A basic scope for close-range";
        rarity = Rarity.RARE;
        minLoot = 1;
        maxLoot = 1;
        scope = 2D;
        modifiersInfo = new ModifiersInfo();
        modifiersInfo.addBuff("2× scope");
    }
}
