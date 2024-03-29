package cz.jeme.programu.gungaming.loot.crate;

import cz.jeme.programu.gungaming.item.CustomItem;
import cz.jeme.programu.gungaming.item.attachment.Attachment;
import cz.jeme.programu.gungaming.item.gun.Gun;
import cz.jeme.programu.gungaming.loot.Rarity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class WoodenCrate extends Crate {
    @Override
    public @NotNull String getName() {
        return "Wooden Crate";
    }

    @Override
    public @NotNull Rarity getRarity() {
        return Rarity.UNCOMMON;
    }

    @Override
    protected @NotNull Map<Rarity, Integer> getChanceOverrides() {
        return Collections.emptyMap();
    }

    @Override
    public @NotNull Map<Class<? extends CustomItem>, Integer> getLimits() {
        return Map.of(
                Gun.class, 1,
                Attachment.class, 1
        );
    }

    @Override
    public @NotNull Set<Class<? extends CustomItem>> getFilter() {
        return Collections.emptySet();
    }

    @Override
    public @NotNull Consumer<Block> getBlockAction() {
        return getConditionalConsumer(true);
    }

    @Override
    public @NotNull Crate.FilterType getFilterType() {
        return FilterType.BLACKLIST;
    }

    @Override
    public float getFillPercentage() {
        return 0.25f;
    }

    @Override
    public float getSpawnPercentage() {
        return 0.0015f;
    }

    @Override
    public @NotNull Material getBlock() {
        return Material.CHAIN_COMMAND_BLOCK;
    }
}
