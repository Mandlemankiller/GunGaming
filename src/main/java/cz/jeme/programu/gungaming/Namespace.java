package cz.jeme.programu.gungaming;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Namespace<T, Z> implements PersistentDataType<T, Z> {
    public static final @NotNull Namespace<Byte, Boolean> GG = new Namespace<>("GG", BOOLEAN);
    public static final @NotNull Namespace<String, String> RARITY = new Namespace<>("RARITY", STRING);
    public static final @NotNull Namespace<String, String> INFO = new Namespace<>("INFO", STRING);
    // Gun namespaces
    public static final @NotNull Namespace<String, String> GUN = new Namespace<>("GUN", STRING);
    public static final @NotNull Namespace<Integer, Integer> GUN_AMMO_CURRENT = new Namespace<>("GUN_AMMO_CURRENT", INTEGER);
    public static final @NotNull Namespace<Integer, Integer> GUN_AMMO_MAX = new Namespace<>("GUN_AMMO_MAX", INTEGER);
    public static final @NotNull Namespace<Integer, Integer> GUN_RELOAD_COOLDOWN = new Namespace<>("GUN_RELOAD_COOLDOWN", INTEGER);
    public static final @NotNull Namespace<String, String> GUN_SCOPE = new Namespace<>("GUN_SCOPE", STRING);
    public static final @NotNull Namespace<String, String> GUN_MAGAZINE = new Namespace<>("GUN_MAGAZINE", STRING);
    public static final @NotNull Namespace<String, String> GUN_STOCK = new Namespace<>("GUN_STOCK", STRING);
    public static final @NotNull Namespace<Float, Float> GUN_RECOIL = new Namespace<>("GUN_RECOIL", FLOAT);
    public static final @NotNull Namespace<Float, Float> GUN_INACCURACY = new Namespace<>("GUN_INACCURACY", FLOAT);
    // Ammo namespaces
    public static final @NotNull Namespace<String, String> AMMO = new Namespace<>("AMMO", STRING);
    // Bullet (entity ammo) namespaces
    public static final @NotNull Namespace<String, String> BULLET = new Namespace<>("BULLET", STRING);
    public static final @NotNull Namespace<Double, Double> BULLET_DAMAGE = new Namespace<>("BULLET_DAMAGE", DOUBLE);
    public static final @NotNull Namespace<String, String> BULLET_GUN_NAME = new Namespace<>("BULLET_GUN_NAME", STRING);
    // Misc namespaces
    public static final @NotNull Namespace<String, String> MISC = new Namespace<>("MISC", STRING);
    // Attachment namespaces
    public static final @NotNull Namespace<String, String> ATTACHMENT = new Namespace<>("ATTACHMENT", STRING);
    // Throwable namespaces
    public static final @NotNull Namespace<String, String> THROWABLE = new Namespace<>("THROWABLE", STRING);
    // Thrown (entity throwable) namespaces
    public static final @NotNull Namespace<String, String> THROWN = new Namespace<>("THROWN", STRING);
    public static final @NotNull Namespace<Double, Double> THROWN_DAMAGE = new Namespace<>("THROWN_DAMAGE", DOUBLE);
    // Consumable namespaces
    public static final @NotNull Namespace<String, String> CONSUMABLE = new Namespace<>("CONSUMABLE", STRING);
    // Grappling Hook namespaces
    public static final @NotNull Namespace<Byte, Boolean> HOOKED = new Namespace<>("HOOKED", BOOLEAN);
    public static final @NotNull Namespace<Long, Long> GRAPPLING_LAST_SUBTRACT = new Namespace<>("GRAPPLING_LAST_SUBTRACT", LONG);
    // Player namespaces
    public static final @NotNull Namespace<Byte, Boolean> FROZEN = new Namespace<>("FROZEN", BOOLEAN);
    public static final @NotNull Namespace<Byte, Boolean> GLIDING = new Namespace<>("GLIDING", BOOLEAN);
    // Entity namespaces
    public static final @NotNull Namespace<Byte, Boolean> INVULNERABLE = new Namespace<>("INVULNERABLE", BOOLEAN);


    public final @NotNull NamespacedKey namespacedKey;
    public final @NotNull PersistentDataType<T, Z> type;
    public final @NotNull String name;

    private Namespace(@NotNull String name, @NotNull PersistentDataType<T, Z> type) {
        namespacedKey = GunGaming.namespacedKey(name.toLowerCase().replace(' ', '_'));
        this.name = name;
        this.type = type;
    }

    public void set(@NotNull PersistentDataHolder holder, @NotNull Z value) {
        holder.getPersistentDataContainer().set(namespacedKey, type, value);
    }

    public void set(@NotNull ItemStack item, @NotNull Z value) {
        ItemMeta meta = item.getItemMeta();
        set(meta, value);
        item.setItemMeta(meta);
    }

    public @Nullable Z get(@NotNull PersistentDataHolder holder) {
        return holder.getPersistentDataContainer().get(namespacedKey, type);
    }

    public @Nullable Z get(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return get(meta);
    }

    public boolean has(@Nullable PersistentDataHolder holder) {
        if (holder == null) return false;
        return holder.getPersistentDataContainer().has(namespacedKey);
    }

    public boolean has(@Nullable ItemStack item) {
        if (item == null) return false;
        return has(item.getItemMeta());
    }

    public @NotNull NamespacedKey getNamespacedKey() {
        return namespacedKey;
    }

    public @NotNull PersistentDataType<T, Z> getType() {
        return type;
    }

    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull Class<T> getPrimitiveType() {
        return type.getPrimitiveType();
    }

    @Override
    public @NotNull Class<Z> getComplexType() {
        return type.getComplexType();
    }

    @Override
    public @NotNull T toPrimitive(@NotNull Z complex, @NotNull PersistentDataAdapterContext context) {
        return type.toPrimitive(complex, context);
    }

    @Override
    public @NotNull Z fromPrimitive(@NotNull T primitive, @NotNull PersistentDataAdapterContext context) {
        return type.fromPrimitive(primitive, context);
    }
}
