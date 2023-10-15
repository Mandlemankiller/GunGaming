package cz.jeme.programu.gungaming;

import cz.jeme.programu.gungaming.game.Game;
import cz.jeme.programu.gungaming.item.ammo.*;
import cz.jeme.programu.gungaming.item.attachment.magazine.BigMagazine;
import cz.jeme.programu.gungaming.item.attachment.magazine.HugeMagazine;
import cz.jeme.programu.gungaming.item.attachment.magazine.SmallMagazine;
import cz.jeme.programu.gungaming.item.attachment.scope.HighScope;
import cz.jeme.programu.gungaming.item.attachment.scope.LowScope;
import cz.jeme.programu.gungaming.item.attachment.scope.MediumScope;
import cz.jeme.programu.gungaming.item.attachment.stock.MetalStock;
import cz.jeme.programu.gungaming.item.attachment.stock.PlasticStock;
import cz.jeme.programu.gungaming.item.attachment.stock.WoodenStock;
import cz.jeme.programu.gungaming.item.consumable.heal.Bandage;
import cz.jeme.programu.gungaming.item.consumable.heal.Medkit;
import cz.jeme.programu.gungaming.item.consumable.adrenaline.Pills;
import cz.jeme.programu.gungaming.item.consumable.adrenaline.Soda;
import cz.jeme.programu.gungaming.item.gun.*;
import cz.jeme.programu.gungaming.item.misc.Concrete;
import cz.jeme.programu.gungaming.item.misc.GrapplingHook;
import cz.jeme.programu.gungaming.item.misc.PlayerTracker;
import cz.jeme.programu.gungaming.item.misc.Radar;
import cz.jeme.programu.gungaming.item.throwable.MolotovCocktail;
import cz.jeme.programu.gungaming.item.throwable.grenade.FragGrenade;
import cz.jeme.programu.gungaming.item.throwable.grenade.MIRVGrenade;
import cz.jeme.programu.gungaming.item.throwable.grenade.SmallGrenade;
import cz.jeme.programu.gungaming.item.throwable.grenade.SmokeGrenade;
import cz.jeme.programu.gungaming.loot.LootManager;
import cz.jeme.programu.gungaming.loot.crate.AmmoCrate;
import cz.jeme.programu.gungaming.loot.crate.GoldenCrate;
import cz.jeme.programu.gungaming.loot.crate.WoodenCrate;
import cz.jeme.programu.gungaming.loot.generator.CrateGenerator;
import cz.jeme.programu.gungaming.manager.ZoomManager;
import cz.jeme.programu.gungaming.util.Message;
import cz.jeme.programu.gungaming.util.registry.*;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;

public final class GunGaming extends JavaPlugin {
    @Override
    public void onEnable() {
        registerItems();
        registerGroups();
        registerCrates();
        LootManager.INSTANCE.registerGroups();
        GGCommand.getInstance(); // register the /gg command

        PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        pluginManager.registerEvents(EventListener.INSTANCE, this);

        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        ZoomManager.INSTANCE.zoomOutAll();
        Boolean daylightCycle = Game.getWorld().getGameRuleDefault(GameRule.DO_DAYLIGHT_CYCLE);
        assert daylightCycle != null : "Daylight cycle default value is null!";
        Game.getWorld().setGameRule(GameRule.DO_DAYLIGHT_CYCLE, daylightCycle);
        CrateGenerator.INSTANCE.removeCrates();
    }


    /**
     * Register all the custom items for GunGaming
     */
    private static void registerItems() {
        Ammos.register(new NineMm());
        Ammos.register(new SevenSixTwoMm());
        Ammos.register(new Rocket());
        Ammos.register(new TwelveGauge());
        Ammos.register(new ThreeZeroEightSubsonicWinchester());

        Guns.register(new M9());
        Guns.register(new NagantM1895());
        Guns.register(new RocketLauncher());
        Guns.register(new AK47());
        Guns.register(new M134Minigun());
        Guns.register(new RemingtonM870());
        Guns.register(new DragunovSVU());
        Guns.register(new SV98());
        Guns.register(new Prickskyttegevar90());

        Miscs.register(new Concrete());
        Miscs.register(new GrapplingHook());
        Miscs.register(new Radar());
        Miscs.register(new PlayerTracker());

        Attachments.register(new LowScope());
        Attachments.register(new MediumScope());
        Attachments.register(new HighScope());

        Attachments.register(new WoodenStock());
        Attachments.register(new PlasticStock());
        Attachments.register(new MetalStock());

        Attachments.register(new SmallMagazine());
        Attachments.register(new BigMagazine());
        Attachments.register(new HugeMagazine());

        Throwables.register(new FragGrenade());
        Throwables.register(new SmokeGrenade());
        Throwables.register(new MIRVGrenade());
        Throwables.register(new SmallGrenade());
        Throwables.register(new MolotovCocktail());

        Consumables.register(new Bandage());
        Consumables.register(new Medkit());
        Consumables.register(new Soda());
        Consumables.register(new Pills());

        Guns.registered();
        Ammos.registered();
        Miscs.registered();
        Attachments.registered();
        Throwables.registered();
        Consumables.registered();
    }

    /**
     * Register all the item groups for GunGaming
     */
    private static void registerGroups() {
        Groups.register("gun", Guns.guns);
        Groups.register("ammo", Ammos.ammos);
        Groups.register("misc", Miscs.miscs);
        Groups.register("attachment", Attachments.attachments);
        Groups.register("throwable", Throwables.throwables);
        Groups.register("consumable", Consumables.consumables);

        Groups.registered();
    }

    /**
     * Register all the loot crates for GunGaming
     */
    private static void registerCrates() {
        Crates.register(new WoodenCrate());
        Crates.register(new AmmoCrate());
        Crates.register(new GoldenCrate());
        Crates.registered();
    }

    /**
     * Log a message with the plugin prefix to the console.
     *
     * @param level   Message severitiy identifier
     * @param message The message to log
     */
    public static void serverLog(@NotNull Level level, @NotNull String message) {
        Bukkit.getLogger().log(level, Message.strip(Message.PREFIX) + message);
    }

    public static void serverLog(@NotNull Level level, @NotNull String message, @NotNull Exception exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        String stackTraceStr = stringWriter.toString();
        serverLog(level, message + "\n" + stackTraceStr);
    }

    public static void serverLog(@NotNull String message, @NotNull Exception exception) {
        serverLog(Level.SEVERE, message, exception);
    }

    /**
     * Provides a static and fast access to the plugin object.
     *
     * @return GunGaming plugin object
     */
    public static @NotNull GunGaming getPlugin() {
        return JavaPlugin.getPlugin(GunGaming.class);
    }

    /**
     * Provides a fast way to create namespaced keys assigned to this plugin.
     *
     * @param key The namespaced key string key
     * @return The namespaced key created
     */
    public static @NotNull NamespacedKey namespacedKey(@NotNull String key) {
        return new NamespacedKey(getPlugin(), key);
    }
}