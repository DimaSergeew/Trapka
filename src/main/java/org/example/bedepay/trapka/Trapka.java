package org.example.bedepay.trapka;

import org.bukkit.plugin.java.JavaPlugin;
import org.example.bedepay.trapka.commands.TrapkaCommand;
import org.example.bedepay.trapka.config.ConfigManager;
import org.example.bedepay.trapka.config.MessageManager;
import org.example.bedepay.trapka.listeners.ItemListener;
import org.example.bedepay.trapka.managers.PlatformManager;
import org.example.bedepay.trapka.managers.TrapManager;
import org.example.bedepay.trapka.utils.NBTUtils;
import org.example.bedepay.trapka.utils.WorldGuardUtils;
import org.example.bedepay.trapka.utils.EffectsUtils;

/**
 * Главный класс плагина Trapka
 */
public final class Trapka extends JavaPlugin {
    
    private ConfigManager configManager;
    private TrapManager trapManager;
    private PlatformManager platformManager;
    private NBTUtils nbtUtils;
    private WorldGuardUtils worldGuardUtils;
    private MessageManager messageManager;
    private EffectsUtils effectsUtils;
    private static Trapka instance;
    
    @Override
    public void onEnable() {
        // Сохраняем экземпляр плагина для доступа из других классов
        instance = this;
        
        // Создаем и сохраняем конфигурацию по умолчанию
        saveDefaultConfig();
        
        // Инициализируем менеджер конфигурации
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // Инициализируем менеджер сообщений
        messageManager = new MessageManager(this);
        
        // Инициализируем утилиты
        nbtUtils = new NBTUtils(this);
        worldGuardUtils = new WorldGuardUtils(this);
        effectsUtils = new EffectsUtils(this);
        
        // Инициализируем менеджеры
        trapManager = new TrapManager(this);
        platformManager = new PlatformManager(this);
        
        // Регистрируем команды
        getCommand("trapka").setExecutor(new TrapkaCommand(this));
        
        // Регистрируем слушатели событий
        getServer().getPluginManager().registerEvents(trapManager, this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        
        // Проверяем наличие WorldGuard
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            getLogger().info("WorldGuard обнаружен, интеграция активирована");
        } else {
            getLogger().warning("WorldGuard не обнаружен, функциональность интеграции отключена");
        }
        
        // Выводим сообщение о загрузке плагина
        getLogger().info("Плагин Trapka успешно загружен!");
        getLogger().info("Размер ловушки: " + configManager.getTrapSize());
        getLogger().info("Размер платформы: " + configManager.getPlatformSize());
    }

    @Override
    public void onDisable() {
        // Удаляем все активные ловушки при выключении сервера
        if (trapManager != null) {
            trapManager.removeAllTraps();
        }
        
        // Удаляем все активные платформы при выключении сервера
        if (platformManager != null) {
            platformManager.removeAllPlatforms();
        }
        
        getLogger().info("Плагин Trapka успешно выгружен!");
    }
    
    /**
     * Получает экземпляр плагина
     * @return экземпляр плагина
     */
    public static Trapka getInstance() {
        return instance;
    }
    
    /**
     * Получает менеджер конфигурации
     * @return менеджер конфигурации
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Получает менеджер ловушек
     * @return менеджер ловушек
     */
    public TrapManager getTrapManager() {
        return trapManager;
    }
    
    /**
     * Получает менеджер платформ
     * @return менеджер платформ
     */
    public PlatformManager getPlatformManager() {
        return platformManager;
    }
    
    /**
     * Получает утилиту для работы с NBT
     * @return утилита для работы с NBT
     */
    public NBTUtils getNbtUtils() {
        return nbtUtils;
    }
    
    /**
     * Получает утилиту для работы с WorldGuard
     * @return утилита для работы с WorldGuard
     */
    public WorldGuardUtils getWorldGuardUtils() {
        return worldGuardUtils;
    }

    /**
     * Получает менеджер сообщений
     * @return менеджер сообщений
     */
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    /**
     * Получает утилиту для работы с эффектами
     * @return утилита для работы с эффектами
     */
    public EffectsUtils getEffectsUtils() {
        return effectsUtils;
    }
    
    /**
     * Перезагружает конфигурацию плагина
     */
    public void reloadPlugin() {
        reloadConfig();
        configManager.loadConfig();
        messageManager.loadMessages();
    }
}
