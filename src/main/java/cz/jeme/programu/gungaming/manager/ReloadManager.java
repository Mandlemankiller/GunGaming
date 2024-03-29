package cz.jeme.programu.gungaming.manager;

import cz.jeme.programu.gungaming.GunGaming;
import cz.jeme.programu.gungaming.Namespace;
import cz.jeme.programu.gungaming.item.ammo.Ammo;
import cz.jeme.programu.gungaming.item.gun.Gun;
import cz.jeme.programu.gungaming.runnable.Reload;
import cz.jeme.programu.gungaming.util.Inventories;
import cz.jeme.programu.gungaming.util.Message;
import cz.jeme.programu.gungaming.util.Sounds;
import cz.jeme.programu.gungaming.util.registry.Guns;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public enum ReloadManager {
    INSTANCE;
    private final @NotNull Map<UUID, Map<Material, Reload>> reloads = new HashMap<>();

    public void reload(@NotNull Player player, @NotNull ItemStack item) {
        UUID uuid = player.getUniqueId();
        if (!Guns.isGun(item)) {
            return;
        }

        Integer currentAmmo = Namespace.GUN_AMMO_CURRENT.get(item);
        Integer maxAmmo = Namespace.GUN_AMMO_MAX.get(item);
        Integer reloadCooldown = Namespace.GUN_RELOAD_COOLDOWN.get(item);

        assert currentAmmo != null : "Current gun ammo is null!";
        assert maxAmmo != null : "Max gun ammo is null!";
        assert reloadCooldown != null : "Gun reload cooldown is null!";

        if (currentAmmo.intValue() == maxAmmo.intValue()) {
            return;
        }

        Material material = item.getType();

        if (!reloads.containsKey(uuid)) {
            reloads.put(uuid, new HashMap<>());
        }

        if (reloads.get(uuid).containsKey(material)) {
            return;
        }

        Gun gun = Guns.getGun(item);
        Ammo ammo = gun.getAmmo();

        PlayerInventory inventory = player.getInventory();

        int ammoFound = Inventories.getItemCount(inventory, ammo.getItem());
        int ammoRequired = maxAmmo - currentAmmo;

        boolean isCreative = player.getGameMode() == GameMode.CREATIVE;

        if (ammoFound == 0 && !isCreative) {
            player.sendActionBar(Message.from("<red>Out of ammo!</red>"));
            player.getWorld().playSound(Sounds.getSound("gun.out_of_ammo", 2.5f), player);
            return;
        }

        int ammoAdd;
        if (ammoFound >= ammoRequired || isCreative) {
            ammoAdd = ammoRequired;
        } else {
            ammoAdd = ammoFound;
        }

        Reload reload = new Reload(item, player, ammoAdd, ammo, gun, isCreative, reloadCooldown);
        reloads.get(uuid).put(material, reload);
        reload.runTaskTimer(GunGaming.getPlugin(GunGaming.class), 0, reloadCooldown / 50);
    }

    public void abortReload(@NotNull Player player, @NotNull ItemStack item) {
        Gun gun = Guns.getGun(item);
        UUID uuid = player.getUniqueId();
        if (!reloads.containsKey(uuid)) {
            return;
        }

        Map<Material, Reload> reloadMap = reloads.get(uuid);

        Material material = item.getType();

        if (!reloadMap.containsKey(material)) {
            return;
        }

        CooldownManager.INSTANCE.setCooldown(player, gun.getItem().getType(), 0);
        Reload reload = reloadMap.get(material);
        if (reload != null) {
            reload.cancel();
        }
        removeReload(player, material);
        player.sendActionBar(Message.from("<red>Reload aborted!</red>"));
        player.playSound(Sounds.getSound("gun.reload_required", 2.5f));
    }

    public void abortReloads(@NotNull Player player, boolean actionNotify) {
        UUID uuid = player.getUniqueId();
        if (!reloads.containsKey(uuid)) {
            return;
        }
        Map<Material, Reload> reloadMap = reloads.get(uuid);
        if (reloadMap.isEmpty()) return;
        for (Material material : reloadMap.keySet()) {
            CooldownManager.INSTANCE.setCooldown(player, material, 0);
            Reload reload = reloadMap.get(material);
            player.getWorld().stopSound(Sounds.getGunReloadSound(reload.item));
            reload.cancel();
            removeReload(player, material);
            if (actionNotify) {
                player.playSound(Sounds.getSound("gun.reload_required", 2.5f));
                player.sendActionBar(Message.from("<red>Reload aborted!</red>"));
            }
        }
    }

    public void abortReloads(@NotNull Player player) {
        abortReloads(player, true);
    }

    public void removeReload(@NotNull Player player, @NotNull Material material) {
        UUID uuid = player.getUniqueId();
        Map<Material, Reload> reloadMap = reloads.get(uuid);
        if (reloadMap.isEmpty()) return;
        reloadMap.remove(material);
        player.sendActionBar(Message.from(""));
    }

    public @Nullable Reload getReload(@NotNull UUID uuid, @NotNull Material material) {
        if (!reloads.containsKey(uuid)) return null;
        return reloads.get(uuid).get(material);
    }
}
