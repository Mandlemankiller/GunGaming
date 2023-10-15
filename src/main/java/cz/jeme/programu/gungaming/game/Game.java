package cz.jeme.programu.gungaming.game;

import cz.jeme.programu.gungaming.GunGaming;
import cz.jeme.programu.gungaming.Namespace;
import cz.jeme.programu.gungaming.loot.generator.CrateGenerator;
import cz.jeme.programu.gungaming.loot.generator.TaskManager;
import cz.jeme.programu.gungaming.util.Message;
import cz.jeme.programu.gungaming.util.Sounds;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public final class Game {
    private static @Nullable Game game;
    private final int size;
    private final int centerX;
    private final int centerZ;
    private final @NotNull CommandSender sender;
    private final @NotNull List<Player> players;
    private static @Nullable World world;
    private boolean pvpEnabled = false;
    private static final @NotNull BossBar BOSS_BAR = BossBar.bossBar(
            Message.from("<b><#6786C8>Gun</#6786C8><#4C618D>Gaming</#4C618D> <#717B95>"
                    + GunGaming.getPlugin().getPluginMeta().getVersion()
                    + "</#717B95></b>"
            ),
            1f,
            BossBar.Color.BLUE,
            BossBar.Overlay.PROGRESS
    );

    public static final @NotNull Set<CreatureSpawnEvent.SpawnReason> DISABLED_SPAWNING = Set.of(
            CreatureSpawnEvent.SpawnReason.NATURAL,
            CreatureSpawnEvent.SpawnReason.LIGHTNING,
            CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN,
            CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM,
            CreatureSpawnEvent.SpawnReason.BUILD_WITHER,
            CreatureSpawnEvent.SpawnReason.NETHER_PORTAL,
            CreatureSpawnEvent.SpawnReason.SILVERFISH_BLOCK,
            CreatureSpawnEvent.SpawnReason.ENDER_PEARL,
            CreatureSpawnEvent.SpawnReason.RAID,
            CreatureSpawnEvent.SpawnReason.PATROL,
            CreatureSpawnEvent.SpawnReason.BEEHIVE
    );

    private Game(int size, int centerX, int centerZ, @NotNull CommandSender sender) {
        this.sender = sender;
        this.size = size;
        this.centerX = centerX;
        this.centerZ = centerZ;

        players = List.copyOf(Bukkit.getOnlinePlayers());

        getWorld().setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        getWorld().setTime(1000);
        getWorld().setClearWeatherDuration(1);

        getWorld().getEntities().stream()
                .filter(e -> e.getType() != EntityType.PLAYER)
                .forEach(Entity::remove);

        WorldBorder worldBorder = getWorld().getWorldBorder();
        worldBorder.setCenter(centerX, centerZ);
        worldBorder.setSize(size);

        for (Player player : players) {
            Location location = player.getLocation();
            location.setWorld(getWorld());
            player.teleport(location.set(centerX, 330, centerZ));
            player.setGameMode(GameMode.SPECTATOR);
            Namespace.FROZEN.set(player, true);
            player.clearActivePotionEffects();
            player.getInventory().clear();
            player.setHealth(20D);
            player.setAbsorptionAmount(0);
            player.setFoodLevel(20);
            player.setExp(0);
            player.setLevel(0);
            player.activeBossBars().forEach(player::hideBossBar);
            player.showBossBar(BOSS_BAR);
        }

        CrateGenerator.INSTANCE.generate(
                sender,
                centerX - size / 2,
                centerZ - size / 2,
                centerX + size / 2,
                centerZ + size / 2
        );

        new BukkitRunnable() {
            private int dotsCount = 1;

            @Override
            public void run() {
                if (TaskManager.INSTANCE.getQueueSize() > 0) {
                    if (dotsCount >= 3) {
                        dotsCount = 1;
                    } else {
                        dotsCount++;
                    }
                    Title title = Title.title(
                            Message.from("<aqua>Loading</aqua>"),
                            Message.from("<gold>Generating crates" + ".".repeat(dotsCount) + "</gold>"),
                            Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ZERO)
                    );

                    players.forEach(p -> p.showTitle(title));
                } else {
                    cancel();
                    new StartCountdown();
                }
            }
        }.runTaskTimer(GunGaming.getPlugin(), 0L, 4L);
    }

    private void startGame() {
        for (Player player : players) {
            Namespace.FROZEN.set(player, false);
            player.setGameMode(GameMode.SURVIVAL);
            player.setGliding(true);
            Namespace.GLIDING.set(player, true);
            Namespace.INVULNERABLE.set(player, true);
        }
        Bukkit.getScheduler().runTask(GunGaming.getPlugin(), GracePeriodTimer::new);
    }

    public static void onEntityToggleGlide(@NotNull EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Boolean gliding = Namespace.GLIDING.get(player);
        if (gliding == null || !gliding) return;
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate != null && chestplate.getType() == Material.ELYTRA) return;
        Material below = player.getWorld().getBlockAt(player.getLocation().subtract(0, 0.4, 0)).getType();
        if (below.isCollidable() || below == Material.LAVA || below == Material.WATER || below == Material.BUBBLE_COLUMN) {
            Namespace.GLIDING.set(player, false);
            Bukkit.getScheduler().runTaskLater(
                    GunGaming.getPlugin(),
                    () -> Namespace.INVULNERABLE.set(player, false),
                    20L * 3L
            );
            return;
        }
        event.setCancelled(true);
    }

    public static boolean isRunning() {
        return game != null;
    }

    public static @Nullable Game newInstance(int size, int centerX, int centerZ, @NotNull CommandSender sender) {
        if (isRunning()) return null;
        game = new Game(size, centerX, centerZ, sender);
        return game;
    }

    public static @Nullable Game getInstance() {
        return game;
    }

    private class StartCountdown extends BukkitRunnable {
        private static final int COUNTER_START = 30;
        private int counter = COUNTER_START;

        private StartCountdown() {
            runTaskTimer(GunGaming.getPlugin(), 0L, 20L);
        }

        @Override
        public void run() {
            Title title;
            Sound sound;
            if (counter == 0) {
                cancel();
                title = Title.title(
                        Message.from("<aqua>Good Luck!</aqua>"),
                        Message.from(""),
                        Title.Times.times(Duration.ZERO, Duration.ofMillis(500), Duration.ofSeconds(2))
                );
                sound = Sounds.getSound("game.start", 1);
                startGame();
            } else {
                title = Title.title(
                        Message.from(
                                "<transition:#FF0000:#00FF00:"
                                        + (float) counter / COUNTER_START
                                        + ">" + counter
                                        + "</transition>"),
                        Message.from("<gold>Game will start soon!</gold>"),
                        Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ZERO)
                );
                sound = Sounds.getSound("game.dong", 1);
            }

            for (Player player : players) {
                player.showTitle(title);
                player.playSound(sound, player);
            }
            counter--;
        }
    }

    private class GameTimer extends Timer {
        public GameTimer() {
            super(20 * 60 + 5, BOSS_BAR);
            BOSS_BAR.color(BossBar.Color.RED);
        }

        @Override
        protected void tick(long counter, float phase) {
            if ((counter < (60 * 5) && counter % 60 == 0) || counter % (60 * 5) == 0) {
                String minutePlr = counter == 60 ? "minute" : "minutes";
                for (Player player : players) {
                    player.sendMessage(
                            Message.from(
                                    "<transition:#FF0000:#00FF00:" + phase
                                            + ">Game ends in "
                                            + counter / 60 + " "
                                            + minutePlr + "!</transition>"
                            )
                    );
                    Sounds.ding(player);
                }
            } else if (counter <= 10) {
                String secondPlr = counter == 1 ? "second" : "seconds";
                for (Player player : players) {
                    player.sendMessage(
                            Message.from(
                                    "<transition:#FF0000:#00FF00:" + phase
                                            + ">Game ends in "
                                            + counter + " "
                                            + secondPlr + "!</transition>"
                            )
                    );
                    Sounds.ding(player);
                }
            }
        }

        @Override
        protected void expire() {

        }
    }

    private class GracePeriodTimer extends Timer {

        public GracePeriodTimer() {
            super(3 * 60 + 5, BOSS_BAR);
            BOSS_BAR.color(BossBar.Color.GREEN);
        }

        @Override
        protected void expire() {
            for (Player player : players) {
                player.sendMessage(
                        Message.from(
                                "<#FF0000>The grace period ended! PvP is now enabled.</#FF0000>"
                        )
                );
                Sounds.ding(player);
            }
            pvpEnabled = true;
            new GameTimer();
        }

        @Override
        protected void tick(long counter, float phase) {
            if (counter == 10) {
                for (Player player : players) {
                    player.sendMessage(
                            Message.from(
                                    "<transition:#FF0000:#00FF00:" + phase + ">"
                                            + "10 seconds of grace period remaining!</transition>"
                            )
                    );
                    Sounds.ding(player);
                }
            } else if (counter % 60 == 0) {
                String minutePlr = counter == 60 ? "minute" : "minutes";
                for (Player player : players) {
                    player.sendMessage(
                            Message.from(
                                    "<transition:#FF0000:#00FF00:" + phase + ">"
                                            + counter / 60 + " " + minutePlr
                                            + " of grace period remaining!</transition>"
                            )
                    );
                    Sounds.ding(player);
                }
            }
        }
    }

    public static @NotNull World getWorld() {
        if (world == null) {
            world = Bukkit.getWorlds().stream()
                    .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No overworld found!"));
        }
        return world;
    }

    public int getSize() {
        return size;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }
}
