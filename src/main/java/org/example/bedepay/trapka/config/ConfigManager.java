package org.example.bedepay.trapka.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.example.bedepay.trapka.Trapka;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Менеджер конфигурации плагина
 */
public class ConfigManager {
    
    private final Trapka plugin;
    private FileConfiguration config;
    
    // Настройки ловушки
    private int trapSize;
    private int trapDuration;
    private int trapCooldown;
    private Material trapMaterial;
    private Set<Material> blacklistedBlocks;
    private List<String> allowedRegions;
    private boolean blockEnderpearls;
    private boolean debugMode;
    
    // Настройки платформы
    private int platformSize;
    private int platformDuration;
    private int platformSelectionTime;
    private int platformMaxCount;
    private int platformCooldown;
    private Material platformMaterial;
    private List<String> platformAllowedRegions;
    
    /**
     * Конструктор класса ConfigManager
     * @param plugin экземпляр главного класса плагина
     */
    public ConfigManager(Trapka plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * Загружает конфигурацию из файла
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        
        loadTrapSettings();
        loadPlatformSettings();
    }
    
    /**
     * Загрузка настроек ловушки
     */
    private void loadTrapSettings() {
        trapSize = config.getInt("trap.size", 3);
        trapDuration = config.getInt("trap.duration", 10);
        trapCooldown = config.getInt("trap.cooldown", 30);
        
        String materialName = config.getString("trap.material", "GLASS");
        try {
            trapMaterial = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Неизвестный материал для ловушки: " + materialName + ". Используется GLASS.");
            trapMaterial = Material.GLASS;
        }
        
        // Загружаем черный список блоков
        List<String> blacklistedNames = config.getStringList("trap.blacklisted_blocks");
        blacklistedBlocks = new HashSet<>();
        
        for (String name : blacklistedNames) {
            try {
                Material material = Material.valueOf(name);
                blacklistedBlocks.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Неизвестный материал в черном списке: " + name);
            }
        }
        
        blockEnderpearls = config.getBoolean("trap.block_enderpearls", true);
        debugMode = config.getBoolean("trap.debug_mode", false);
        
        allowedRegions = config.getStringList("trap.allowed_regions");
    }
    
    /**
     * Загрузка настроек платформы
     */
    private void loadPlatformSettings() {
        platformSize = config.getInt("platform.size", 3);
        platformDuration = config.getInt("platform.duration", 10);
        platformSelectionTime = config.getInt("platform.selection_time", 10);
        platformMaxCount = config.getInt("platform.max_count", 3);
        platformCooldown = config.getInt("platform.cooldown", 45);
        
        String materialName = config.getString("platform.material", "STONE_BRICKS");
        try {
            platformMaterial = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Неизвестный материал для платформы: " + materialName + ". Используется STONE_BRICKS.");
            platformMaterial = Material.STONE_BRICKS;
        }
        
        platformAllowedRegions = config.getStringList("platform.allowed_regions");
    }
    
    /**
     * Создает конфигурацию по умолчанию, если файл не существует
     */
    public void createDefaultConfig() {
        FileConfiguration config = plugin.getConfig();
        
        // Настройки ловушки
        config.set("trap.size", 3);
        config.set("trap.duration", 30);
        config.set("trap.cooldown", 60);
        config.set("trap.material", "GLASS");
        config.set("trap.blacklisted_blocks", List.of("BEDROCK", "OBSIDIAN", "END_PORTAL_FRAME"));
        config.set("trap.allowed_regions", List.of("pvp", "arena"));
        config.set("trap.block_enderpearls", true);
        config.set("trap.debug_mode", false);
        
        // Настройки платформы
        config.set("platform.size", 3);
        config.set("platform.duration", 10);
        config.set("platform.selection_time", 10);
        config.set("platform.max_count", 3);
        config.set("platform.material", "GLASS");
        
        // Сохраняем конфигурацию
        plugin.saveConfig();
    }
    
    // Геттеры для настроек ловушки
    
    public int getTrapSize() {
        return trapSize;
    }
    
    public int getTrapDuration() {
        return trapDuration;
    }
    
    public int getTrapCooldown() {
        return trapCooldown;
    }
    
    public Material getTrapMaterial() {
        return trapMaterial;
    }
    
    public Set<Material> getBlacklistedBlocks() {
        return blacklistedBlocks;
    }
    
    public List<String> getAllowedRegions() {
        return allowedRegions;
    }
    
    // Геттеры для настроек платформы
    
    public int getPlatformSize() {
        return platformSize;
    }
    
    public int getPlatformDuration() {
        return platformDuration;
    }
    
    public int getPlatformSelectionTime() {
        return platformSelectionTime;
    }
    
    public int getPlatformMaxCount() {
        return platformMaxCount;
    }
    
    public int getPlatformCooldown() {
        return platformCooldown;
    }
    
    public Material getPlatformMaterial() {
        return platformMaterial;
    }
    
    public boolean isBlockEnderpearls() {
        return blockEnderpearls;
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
    
    /**
     * Возвращает список разрешенных регионов для платформ
     * @return список разрешенных регионов для платформ
     */
    public List<String> getPlatformAllowedRegions() {
        return platformAllowedRegions;
    }
} 