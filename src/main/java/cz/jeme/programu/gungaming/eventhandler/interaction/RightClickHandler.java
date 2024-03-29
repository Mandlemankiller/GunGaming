package cz.jeme.programu.gungaming.eventhandler.interaction;

import cz.jeme.programu.gungaming.Namespace;
import cz.jeme.programu.gungaming.eventhandler.PlayerItemConsumeHandler;
import cz.jeme.programu.gungaming.item.gun.Gun;
import cz.jeme.programu.gungaming.item.throwable.Throwable;
import cz.jeme.programu.gungaming.loot.generator.CrateGenerator;
import cz.jeme.programu.gungaming.manager.CooldownManager;
import cz.jeme.programu.gungaming.manager.ReloadManager;
import cz.jeme.programu.gungaming.util.Inventories;
import cz.jeme.programu.gungaming.util.Materials;
import cz.jeme.programu.gungaming.util.Message;
import cz.jeme.programu.gungaming.util.Sounds;
import cz.jeme.programu.gungaming.util.registry.Consumables;
import cz.jeme.programu.gungaming.util.registry.Guns;
import cz.jeme.programu.gungaming.util.registry.Throwables;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class RightClickHandler {
    private RightClickHandler() {
        throw new AssertionError();
    }

    public static void air(@NotNull PlayerInteractEvent event) {
        interact(event);
    }

    public static void block(@NotNull PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (clickedBlock == null) {
            throw new NullPointerException("Clicked block is null!");
        }


        if (player.isSneaking()) {
            interact(event);
            if (Guns.isGun(item)) {
                event.setCancelled(true);
            }
            return;
        }

        if (CrateGenerator.INSTANCE.showInventory(clickedBlock, event.getPlayer())) {
            event.setCancelled(true);
            return;
        }

        Material material = clickedBlock.getType();
        if (!Materials.hasRightClick(material)) {
            interact(event);
            if (Guns.isGun(item)) {
                event.setCancelled(true);
            }
        }
    }

    private static void interact(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (Guns.isGun(item)) {
            shoot(event, player, item);
            return;
        }
        if (Throwables.isThrowable(item)) {
            doThrow(event, player, item);
            return;
        }
        if (Consumables.isConsumable(item)) {
            PlayerItemConsumeHandler.onStartConsume(event);
//            return;
        }
    }

    private static void shoot(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull ItemStack item) {
        event.setCancelled(true);

        Gun gun = Guns.getGun(item);
        if (player.getCooldown(item.getType()) != 0) {
            if (ReloadManager.INSTANCE.getReload(player.getUniqueId(), item.getType()) == null) return;
            ReloadManager.INSTANCE.abortReloads(player, false);
        }

        Integer heldAmmo = Namespace.GUN_AMMO_CURRENT.get(item);
        assert heldAmmo != null : "Held gun current ammo is null!";
        boolean isCreative = player.getGameMode() == GameMode.CREATIVE;
        if (heldAmmo == 0 && !isCreative) {
            if (Inventories.getItemCount(player.getInventory(), gun.getAmmo().getItem()) == 0) {
                player.sendActionBar(Message.from("<red>Out of ammo!</red>"));
                player.playSound(Sounds.getSound("gun.out_of_ammo", 2.5f));
            } else {
                player.sendActionBar(Message.from("<red>Press F to reload!</red>"));
                player.playSound(Sounds.getSound("gun.reload_required", 2.5f));
            }
            return;
        }

        CooldownManager.INSTANCE.setCooldown(player, item.getType(), gun.getShootCooldown());
        gun.shoot(event, item);
    }

    private static void doThrow(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull ItemStack heldItem) {
        event.setCancelled(true);

        if (player.getCooldown(heldItem.getType()) != 0) return;

        Throwable throwable = Throwables.getThrowable(heldItem);

        CooldownManager.INSTANCE.setCooldown(player, heldItem.getType(), throwable.getThrowCooldown());
        throwable.doThrow(event, heldItem);
    }
}
