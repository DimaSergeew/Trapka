package org.example.bedepay.trapka.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.example.bedepay.trapka.Trapka;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для работы с NBT тегами предметов
 */
public class NBTUtils {

    private final Trapka plugin;
    private final NamespacedKey trapKey;
    private final NamespacedKey platformKey;

    /**
     * Конструктор класса NBTUtils
     * @param plugin экземпляр главного класса плагина
     */
    public NBTUtils(Trapka plugin) {
        this.plugin = plugin;
        this.trapKey = new NamespacedKey(plugin, "trapka_trap");
        this.platformKey = new NamespacedKey(plugin, "trapka_platform");
    }

    /**
     * Создает предмет "Ловушка"
     * @param amount количество предметов
     * @return предмет "Ловушка"
     */
    public ItemStack createTrapItem(int amount) {
        ItemStack item = new ItemStack(Material.TRIPWIRE_HOOK, amount);
        ItemMeta meta = item.getItemMeta();
        
        // Добавляем NBT тег для идентификации предмета
        meta.getPersistentDataContainer().set(trapKey, PersistentDataType.INTEGER, 1);
        
        // Устанавливаем имя и описание предмета
        meta.displayName(Component.text("Ловушка").decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Нажмите на игрока, чтобы создать ловушку").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Кулдаун: " + plugin.getConfigManager().getTrapCooldown() + " секунд").decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        
        // Добавляем эффект зачарования для подсветки
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Создает предмет платформы
     * @param amount количество предметов
     * @return созданный предмет
     */
    public ItemStack createPlatformItem(int amount) {
        // Создаем новый предмет - используем каменную кнопку
        ItemStack item = new ItemStack(Material.STONE_BUTTON, amount);
        
        // Получаем мета-данные предмета
        ItemMeta meta = item.getItemMeta();
        
        // Устанавливаем имя и описание предмета
        meta.displayName(Component.text("§b§lПлатформа").decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Создает магическую платформу под ногами").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7Длительность: §e" + plugin.getConfigManager().getPlatformDuration() + " сек.").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7Размер: §e" + plugin.getConfigManager().getPlatformSize() + "x" + plugin.getConfigManager().getPlatformSize()).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§eПравый клик§7, чтобы создать платформу").decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        
        // Добавляем зачарование для блеска
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        
        // Скрываем стандартные подсказки о зачаровании
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        
        // Устанавливаем флаг, что это предмет платформы
        meta.getPersistentDataContainer().set(platformKey, PersistentDataType.BOOLEAN, true);
        
        // Применяем мета-данные к предмету
        item.setItemMeta(meta);
        
        return item;
    }

    /**
     * Проверяет, является ли предмет ловушкой
     * @param item проверяемый предмет
     * @return true, если предмет является ловушкой
     */
    public boolean isTrapItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.has(trapKey, PersistentDataType.INTEGER);
    }

    /**
     * Проверяет, является ли предмет платформой
     * @param item проверяемый предмет
     * @return true, если предмет является платформой
     */
    public boolean isPlatformItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.has(platformKey, PersistentDataType.BOOLEAN);
    }
} 