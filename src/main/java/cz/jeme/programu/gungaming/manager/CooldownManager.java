package cz.jeme.programu.gungaming.manager;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CooldownManager {
    private static @Nullable CooldownManager instance = null;
    private final @NotNull Map<UUID, Map<Material, Long>> cooldowns = new HashMap<>();

    private CooldownManager() {
        // Singleton class
    }

    public void setCooldown(@NotNull Player player, @NotNull Material material, int duration) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(player.getUniqueId())) {
            cooldowns.put(uuid, new HashMap<>());
        }
        cooldowns.get(uuid).put(material, System.currentTimeMillis() + duration);
        player.setCooldown(material, duration / 50);
    }

    public long getCooldown(@NotNull Player player, @NotNull ItemStack item) {
        UUID uuid = player.getUniqueId();
        Material material = item.getType();

        if (!cooldowns.containsKey(uuid)) {
            return 0;
        }

        Map<Material, Long> playerCooldowns = cooldowns.get(uuid);

        if (!playerCooldowns.containsKey(material)) {
            return 0;
        }

        long endTimeStamp = playerCooldowns.get(material);

        if (System.currentTimeMillis() >= endTimeStamp) {
            return 0;
        }

        return endTimeStamp - System.currentTimeMillis();
    }

    public static synchronized @NotNull CooldownManager getInstance() {
        if (instance == null) instance = new CooldownManager();
        return instance;
    }
}
