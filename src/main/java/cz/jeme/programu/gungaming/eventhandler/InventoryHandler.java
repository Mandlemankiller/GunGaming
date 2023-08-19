package cz.jeme.programu.gungaming.eventhandler;

import cz.jeme.programu.gungaming.Namespace;
import cz.jeme.programu.gungaming.item.attachment.AttachmentMenu;
import cz.jeme.programu.gungaming.manager.ReloadManager;
import cz.jeme.programu.gungaming.manager.ZoomManager;
import cz.jeme.programu.gungaming.util.item.Guns;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryHandler {

    private final @NotNull ReloadManager reloadManager = ReloadManager.getInstance();
    private final @NotNull ZoomManager zoomManager = ZoomManager.getInstance();
    private static final @NotNull Map<UUID, AttachmentMenu> ATTACHMENT_MENUS = new HashMap<>();

    public void onPlayerSwapHands(@NotNull PlayerSwapHandItemsEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (!Guns.isGun(item)) {
            return;
        }
        event.setCancelled(true);
        reloadManager.reload(event.getPlayer(), item);
    }

    public void onInventoryOpen(@NotNull InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            throw new IllegalArgumentException("HumanEntity is not instanceof Player!");
        }
        reloadManager.abortReloads((Player) event.getPlayer());
    }

    public void onPlayerDropItem(@NotNull PlayerDropItemEvent event) {
        reloadManager.abortReloads(event.getPlayer());
        zoomManager.zoomOut(event.getPlayer());
    }

    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            throw new IllegalArgumentException("HumanEntity is not instanceof Player!");
        }
        reloadManager.abortReloads(player);
        zoomManager.zoomOut(player);


        ItemStack cursor = event.getCursor();
        ItemStack item = event.getCurrentItem();

        if (event.getInventory().getType() == InventoryType.ANVIL) {
            if (item == null) return;
            if (item.getItemMeta() == null) return;
            if (Namespace.GG.has(item)) {
                event.setCancelled(true);
                return;
            }
        }

        boolean rightClick = event.getClick() == ClickType.RIGHT;
        boolean emptyClick = cursor == null || cursor.getType() == Material.AIR;
        boolean isGun = Guns.isGun(item);

        if (rightClick && emptyClick && isGun) {
            event.setCancelled(true);
            ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
            serverPlayer.inventoryMenu.sendAllDataToRemote();
            ATTACHMENT_MENUS.put(player.getUniqueId(), new AttachmentMenu(event));
            return;
        }
        AttachmentMenu menu = ATTACHMENT_MENUS.get(player.getUniqueId());
        if (menu == null) return;
        if (menu.hasOpenInventory(event)) {
            menu.click(event);
        } else {
            ATTACHMENT_MENUS.put(player.getUniqueId(), null);
        }
    }

    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        // When the AttachmentMenu is closed, it doesn't start remote updates to the player's inventory
        // This leads to client-server desync. This starts the updates again.
        // Took like 3 days to discover.
        ServerPlayer serverPlayer = ((CraftPlayer) event.getPlayer()).getHandle();
        serverPlayer.inventoryMenu.resumeRemoteUpdates();
    }
}
