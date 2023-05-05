package cz.jeme.programu.gungaming.items.ammo;

import cz.jeme.programu.gungaming.loot.Rarity;
import org.bukkit.Material;

public class Rocket extends Ammo {

    @Override
    protected void setup() {
        material = Material.BLUE_DYE;
        name = "Rocket";
        loreLine = "Rocket ammo for Rocket Launcher";
        rarity = Rarity.EPIC;
        minLoot = 1;
        maxLoot = 3;
    }

}
