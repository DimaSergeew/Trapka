package org.example.bedepay.trapka;

import org.bukkit.plugin.java.JavaPlugin;
import org.example.bedepay.trapka.commands.TrapkaCommand;
import org.example.bedepay.trapka.config.ConfigManager;
import org.example.bedepay.trapka.listeners.ItemListener;
import org.example.bedepay.trapka.managers.PlatformManager;
import org.example.bedepay.trapka.managers.TrapManager;
import org.example.bedepay.trapka.utils.NBTUtils;
import org.example.bedepay.trapka.utils.WorldGuardUtils;

/**
 * Главный класс плагина Trapka
 */
public final class Trapka extends JavaPlugin {
    
    private ConfigManager configManager;
    private TrapManager trapManager;
    private PlatformManager platformManager;
    private NBTUtils nbtUtils;
    private WorldGuardUtils worldGuardUtils;
    private static Trapka instance;
    
    @Override
    public void onEnable() {
        // Сохраняем экземпляр плагина для доступа из других классов
        instance = this;
        
        // Инициализируем менеджер конфигурации
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // Проверяем наличие конфигурационного файла, создаем если его нет
        saveDefaultConfig();
        
        // Инициализируем утилиты
        nbtUtils = new NBTUtils(this);
        worldGuardUtils = new WorldGuardUtils(this);
        
        // Инициализируем менеджеры
        trapManager = new TrapManager(this);
        platformManager = new PlatformManager(this);
        
        // Регистрируем команды
        getCommand("trapka").setExecutor(new TrapkaCommand(this));
        
        // Регистрируем слушателей событий
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        getServer().getPluginManager().registerEvents(trapManager, this);
        
        // Проверяем наличие WorldGuard
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            getLogger().info("WorldGuard обнаружен, интеграция активирована");
        } else {
            getLogger().warning("WorldGuard не обнаружен, функциональность интеграции отключена");
        }
        
        getLogger().info("Плагин Trapka успешно запущен!");
    }

    @Override
    public void onDisable() {
        // Очищаем все активные ловушки и платформы
        trapManager.removeAllTraps();
        platformManager.removeAllPlatforms();
        
        getLogger().info("Плагин Trapka успешно выключен!");
    }
    
    /**
     * Получение экземпляра плагина
     * @return экземпляр плагина
     */
    public static Trapka getInstance() {
        return instance;
    }
    
    /**
     * Получение менеджера конфигурации
     * @return экземпляр менеджера конфигурации
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Получение менеджера ловушек
     * @return экземпляр менеджера ловушек
     */
    public TrapManager getTrapManager() {
        return trapManager;
    }
    
    /**
     * Получение менеджера платформ
     * @return экземпляр менеджера платформ
     */
    public PlatformManager getPlatformManager() {
        return platformManager;
    }
    
    /**
     * Получение утилит для работы с NBT
     * @return экземпляр утилит NBT
     */
    public NBTUtils getNbtUtils() {
        return nbtUtils;
    }
    
    /**
     * Получение утилит для работы с WorldGuard
     * @return экземпляр утилит WorldGuard
     */
    public WorldGuardUtils getWorldGuardUtils() {
        return worldGuardUtils;
    }
}
