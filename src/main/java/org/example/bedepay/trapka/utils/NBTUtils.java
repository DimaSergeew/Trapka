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
        meta.displayName(Component.text("§c§l✧ Магическая Ловушка ✧").decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7Создаёт временную клетку из блоков").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7вокруг указанного игрока.").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§8» §eРазмер§7: §f" + plugin.getConfigManager().getTrapSize() + "x" + plugin.getConfigManager().getTrapSize() + "x" + plugin.getConfigManager().getTrapSize()).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§8» §eДлительность§7: §f" + plugin.getConfigManager().getTrapDuration() + " сек.").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§8» §eПерезарядка§7: §f" + plugin.getConfigManager().getTrapCooldown() + " сек.").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§6✧ §eНажмите на игрока§6, чтобы активировать §6✧").decoration(TextDecoration.ITALIC, false));
        
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
        meta.displayName(Component.text("§b§l☼ Магическая Платформа ☼").decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7Создаёт временную платформу из блоков").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7в указанном направлении.").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§8» §eРазмер§7: §f" + plugin.getConfigManager().getPlatformSize() + "x" + plugin.getConfigManager().getPlatformSize()).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§8» §eДлительность§7: §f" + plugin.getConfigManager().getPlatformDuration() + " сек.").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§8» §eМакс. количество§7: §f" + plugin.getConfigManager().getPlatformMaxCount()).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§8» §eПерезарядка§7: §f" + plugin.getConfigManager().getPlatformCooldown() + " сек.").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§3☼ §bПравый клик§3, чтобы активировать §3☼").decoration(TextDecoration.ITALIC, false));
        
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