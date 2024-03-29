package cz.jeme.programu.gungaming.item.attachment.magazine;

import cz.jeme.programu.gungaming.Namespace;
import cz.jeme.programu.gungaming.item.ammo.TwelveGauge;
import cz.jeme.programu.gungaming.item.attachment.Attachment;
import cz.jeme.programu.gungaming.item.gun.Gun;
import cz.jeme.programu.gungaming.util.Message;
import cz.jeme.programu.gungaming.util.registry.Attachments;
import cz.jeme.programu.gungaming.util.registry.Guns;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public abstract class Magazine extends Attachment {
    public abstract float getMagazineSizePercentage();

    {
        ItemMeta scopeMeta = placeHolder.getItemMeta();
        scopeMeta.displayName(Message.from("<!italic><gray>Magazine</gray></!italic>"));
        scopeMeta.setCustomModelData(1);
        placeHolder.setItemMeta(scopeMeta);
    }

    @Override
    public final int getSlotId() {
        return 1;
    }

    @Override
    public final @NotNull Namespace getNamespace() {
        return Namespace.GUN_MAGAZINE;
    }

    @Override
    public final @NotNull Material getMaterial() {
        return Material.FLOWER_BANNER_PATTERN;
    }

    public static void update(@NotNull ItemStack item) {
        String magazineName = Namespace.GUN_MAGAZINE.get(item);
        assert magazineName != null : "Magazine name is null!";
        Gun gun = Guns.getGun(item);
        if (magazineName.isEmpty()) {
            Namespace.GUN_RELOAD_COOLDOWN.set(item, gun.getReloadCooldown());
            return;
        }
        Magazine magazine = (Magazine) Attachments.getAttachment(magazineName);
        assert magazine != null : "Magazine is null!";
        if (gun.getAmmoType() != TwelveGauge.class) {
            float newReloadCooldown = gun.getReloadCooldown() * magazine.getMagazineSizePercentage();
            Namespace.GUN_RELOAD_COOLDOWN.set(item, Math.round(newReloadCooldown));
        }
    }

    @Override
    protected final @NotNull Class<? extends Attachment> getAttachmentType() {
        return Magazine.class;
    }
}
