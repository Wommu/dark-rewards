package dev.wommu.darkrewards;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class ItemStackBuilder {

    private final ItemStack item;
    private final ItemMeta  meta;

    public ItemStackBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = this.item.getItemMeta();
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

    public ItemStackBuilder setDisplayName(String name) {
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        return this;
    }

    public ItemStackBuilder setLore(Collection<String> lines) {
        List<String> lore = lines.stream()
                                 .map((line) -> ChatColor.translateAlternateColorCodes('&', line))
                                 .collect(Collectors.toList());
        meta.setLore(lore);
        return this;
    }
}
