package cz.jeme.programu.gungaming;

import cz.jeme.programu.gungaming.game.Game;
import cz.jeme.programu.gungaming.item.CustomItem;
import cz.jeme.programu.gungaming.loot.generator.CrateGenerator;
import cz.jeme.programu.gungaming.util.Message;
import cz.jeme.programu.gungaming.util.registry.Groups;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class GGCommand extends Command {

    private static @Nullable GGCommand instance;

    public static final @NotNull Map<String, String> CORRECT_ARGS = Map.of(
            "RELOAD", "reload",
            "HELP", "help",
            "GIVE", "give",
            "GENERATE", "generate",
            "START", "start"
    );

    private GGCommand() {
        super("gg", "Main command for gungaming", "false", Collections.emptyList());
        setPermission("gungaming.gg");
        Bukkit.getCommandMap().register("gungaming", this);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length == 0) {
            help(sender);
            return true;
        }
        if (args[0].equals(CORRECT_ARGS.get("HELP"))) {
            help(sender);
            return true;
        }
        if (args[0].equals(CORRECT_ARGS.get("RELOAD"))) {
            sender.sendMessage(Message.prefix("<red>Reload TODO!</red>"));
            // TODO Reload
            return true;
        }
        if (args[0].equals(CORRECT_ARGS.get("GIVE"))) {
            give(sender, args);
            return true;
        }
        if (args[0].equals(CORRECT_ARGS.get("GENERATE"))) {
            generate(sender, args);
            return true;
        }
        if (args[0].equals(CORRECT_ARGS.get("START"))) {
            start(sender, args);
            return true;
        }
        sender.sendMessage(Message.prefix("<red>Unknown command!</red>"));
        return true;
    }

    private void generate(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2 && args[1].equals("CONFIRM")) {
            sender.sendMessage(Message.prefix("<green>Generating...</green>"));
            CrateGenerator.INSTANCE.generate(sender, -250, -250, 250, 250);
            return;
        }
        if (sender instanceof Player) {
            sender.sendMessage(Message.prefix(
                    "<red>Are you sure you want to generate crates?</red> " +
                            "<b><dark_gray>[<green>" +
                            "<hover:show_text:'<green>Click to generate!</green>'>" +
                            "<click:run_command:/gg generate CONFIRM>YES</click>" +
                            "</hover></green>]</dark_gray></b>"
            ));
        } else {
            sender.sendMessage(Message.prefix(
                    "<red>Are you sure you want to generate crates? " +
                            "To generate crates type \"<green>gg generate CONFIRM</green>\".</red>"
            ));
        }
    }

    private void help(@NotNull CommandSender sender) {
        sender.sendMessage(Message.prefix("<red>Help TODO!</red>"));
        // TODO Help
    }

    private void give(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 4) {
            sender.sendMessage(Message.prefix("<red>Not enough arguments!</red>"));
            return;
        }

        if (args.length > 5) {
            sender.sendMessage(Message.prefix("<red>Too many arguments!</red>"));
            return;
        }

        List<Player> players = new ArrayList<>();
        if (args[1].equals("@everyone")) {
            players = new ArrayList<>(Bukkit.getOnlinePlayers());
        } else {
            String playerName = args[1];
            Player player = Bukkit.getPlayer(playerName);
            if (player == null || !player.isOnline()) {
                // Player not found
                sender.sendMessage(Message.prefix("<red>This player is not online!</red>"));
                return;
            }
            players.add(player);
        }

        String groupName = args[2].toLowerCase();
        if (!Groups.groups.containsKey(groupName)) {
            sender.sendMessage(Message.prefix("<red>Unknown group name \"" + groupName + "\"!</red>"));
            return;
        }

        String itemName = args[3].toLowerCase();
        ItemStack item;

        Map<String, ? extends CustomItem> group = Groups.groups.get(groupName);

        if (!matchesLowercaseUnderscores(group.keySet(), itemName)) {
            sender.sendMessage(Message.prefix("<red>Unknown " + groupName + " name!</red>"));
            return;
        }

        CustomItem customItem = getLowercaseUnderscores(group, itemName);
        assert customItem != null;
        item = customItem.getItem();

        int count = 1;
        if (args.length == 5) {
            try {
                if (Integer.parseInt(args[4]) < 1) {
                    sender.sendMessage(Message.prefix("<red>Count can not be lower than 1!</red>"));
                    return;
                }
            } catch (NullPointerException | NumberFormatException e) {
                sender.sendMessage(Message.prefix("<red>Count is not valid!</red>"));
                return;
            }
            count = Integer.parseInt(args[4]);
        }
        for (int j = 0; j < count; j++) {
            for (Player player : players) {
                Map<Integer, ItemStack> exceeded = player.getInventory().addItem(item);
                if (!exceeded.isEmpty()) {
                    sender.sendMessage(Message.prefix("<gold>Count exceeded the inventory size!</gold>"));
                    return;
                }
            }
        }
    }

    private void start(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 4) {
            sender.sendMessage(Message.prefix("<red>Not enough arguments!</red>"));
            return;
        }
        if (args.length > 4) {
            sender.sendMessage(Message.prefix("<red>Too many arguments!</red>"));
            return;
        }
        int size;
        int centerX;
        int centerZ;
        try {
            size = Integer.parseInt(args[1]);
            centerX = Integer.parseInt(args[2]);
            centerZ = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Message.prefix("<red>That is not a valid number!</red>"));
            return;
        }
        Game game = Game.newInstance(size, centerX, centerZ, sender);
        if (game == null) {
            sender.sendMessage(Message.prefix("<red>There is a game already running!</red>"));
        } else {
            sender.sendMessage(Message.prefix("<green>Game started successfully!</green>"));
        }
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            return containsFilter(new ArrayList<>(CORRECT_ARGS.values()), args[0]);
        }
        if (args[0].equals(CORRECT_ARGS.get("GENERATE"))) {
            return List.of("CONFIRM");
        }
        if (args[0].equals(CORRECT_ARGS.get("GIVE"))) {
            return giveTabComplete(args);
        }
        return new ArrayList<>();
    }

    private static @NotNull List<String> giveTabComplete(@NotNull String[] args) {
        if (args.length == 2) {
            ArrayList<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            playerNames.add("@everyone");
            return containsFilter(playerNames, args[1]);
        }
        if (args.length == 3) {
            return containsFilter(new ArrayList<>(Groups.groups.keySet()), args[2]);
        }
        if (args.length == 4) {
            Map<String, ? extends CustomItem> group = Groups.groups.get(args[2]);
            if (group == null) return new ArrayList<>();
            List<String> itemNames = new ArrayList<>();
            for (CustomItem customItem : group.values()) {
                itemNames.add(customItem.getName().replace(' ', '_').toLowerCase());
            }
            return containsFilter(itemNames, args[3]);
        }
        return new ArrayList<>();
    }

    private static @NotNull List<String> containsFilter(@NotNull Collection<String> collection, @NotNull String pattern) {
        return collection.stream()
                .filter(item -> item.contains(pattern))
                .toList();
    }

    private static boolean matchesLowercaseUnderscores(@NotNull Collection<? extends String> collection, @NotNull String match) {
        for (String entry : collection) {
            if (entry.replace(' ', '_').toLowerCase().equals(match)) return true;
        }
        return false;
    }

    private static @Nullable <T> T getLowercaseUnderscores(Map<String, T> map, String match) {
        for (String entry : map.keySet()) {
            if (entry.replace(' ', '_').toLowerCase().equals(match)) return map.get(entry);
        }
        return null;
    }

    public static synchronized @NotNull GGCommand getInstance() {
        if (instance == null) instance = new GGCommand();
        return instance;
    }
}
