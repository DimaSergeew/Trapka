package org.example.bedepay.trapka.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import org.example.bedepay.trapka.Trapka;
import com.sk89q.worldguard.LocalPlayer;

import java.util.List;

/**
 * Утилитарный класс для работы с WorldGuard
 */
public class WorldGuardUtils {

    private final Trapka plugin;
    private boolean worldGuardEnabled = false;

    /**
     * Конструктор класса WorldGuardUtils
     * @param plugin экземпляр главного класса плагина
     */
    public WorldGuardUtils(Trapka plugin) {
        this.plugin = plugin;
        checkWorldGuard();
    }
    
    /**
     * Проверяет наличие WorldGuard на сервере
     */
    private void checkWorldGuard() {
        Plugin worldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard");
        worldGuardEnabled = worldGuard != null && worldGuard.isEnabled();
        
        if (worldGuardEnabled) {
            plugin.getLogger().info("WorldGuard обнаружен и интеграция активирована");
        } else {
            plugin.getLogger().warning("WorldGuard не обнаружен или отключен, функциональность интеграции недоступна");
        }
    }
    
    /**
     * Проверяет, разрешено ли использование ловушки в регионе
     * @param location локация для проверки
     * @param player игрок, который пытается использовать ловушку
     * @return true, если ловушка может быть использована в данном регионе
     */
    public boolean canUseTrapAtLocation(Location location, Player player) {
        if (!worldGuardEnabled) {
            return true; // Если WorldGuard отключен, разрешаем везде
        }
        
        // Получаем список разрешенных регионов из конфига
        List<String> allowedRegions = plugin.getConfigManager().getAllowedRegions();
        
        // Получаем контейнер регионов WorldGuard
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        
        // Получаем менеджер регионов для мира
        RegionManager regionManager = container.get(BukkitAdapter.adapt(location.getWorld()));
        if (regionManager == null) {
            return true; // Если нет менеджера регионов для этого мира, разрешаем
        }
        
        // Получаем набор регионов в локации
        ApplicableRegionSet regions = regionManager.getApplicableRegions(
                BukkitAdapter.asBlockVector(location));
        
        // Если нет регионов, то разрешаем
        if (regions.size() == 0) {
            return true;
        }
        
        // Проверяем, находится ли локация в разрешенном регионе
        for (ProtectedRegion region : regions) {
            if (allowedRegions.contains(region.getId())) {
                return true;
            }
        }
        
        // Проверяем флаг PVP (если PVP разрешен в регионе, то разрешаем использование ловушки)
        LocalPlayer localPlayer = player != null ? WorldGuardPlugin.inst().wrapPlayer(player) : null;
        StateFlag.State pvpState = regions.queryState(localPlayer, Flags.PVP);
        return pvpState == StateFlag.State.ALLOW;
    }
    
    /**
     * Проверяет, разрешено ли использование платформы в регионе
     * @param location локация для проверки
     * @param player игрок, который пытается использовать платформу
     * @return true, если платформа может быть использована в данном регионе
     */
    public boolean canUsePlatformAtLocation(Location location, Player player) {
        if (!worldGuardEnabled) {
            return true; // Если WorldGuard отключен, разрешаем везде
        }
        
        // Получаем список разрешенных регионов из конфига
        List<String> allowedRegions = plugin.getConfigManager().getPlatformAllowedRegions();
        
        // Получаем контейнер регионов WorldGuard
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        
        // Получаем менеджер регионов для мира
        RegionManager regionManager = container.get(BukkitAdapter.adapt(location.getWorld()));
        if (regionManager == null) {
            return true; // Если нет менеджера регионов для этого мира, разрешаем
        }
        
        // Получаем набор регионов в локации
        ApplicableRegionSet regions = regionManager.getApplicableRegions(
                BukkitAdapter.asBlockVector(location));
        
        // Если нет регионов, то разрешаем
        if (regions.size() == 0) {
            return true;
        }
        
        // Проверяем, находится ли локация в разрешенном регионе
        for (ProtectedRegion region : regions) {
            if (allowedRegions.contains(region.getId())) {
                return true;
            }
        }
        
        // Проверяем флаг BUILD (если строительство разрешено в регионе, то разрешаем использование платформы)
        LocalPlayer localPlayer = player != null ? WorldGuardPlugin.inst().wrapPlayer(player) : null;
        return regions.testState(localPlayer, Flags.BUILD);
    }
    
    /**
     * Проверяет, включен ли WorldGuard
     * @return true, если WorldGuard включен
     */
    public boolean isWorldGuardEnabled() {
        return worldGuardEnabled;
    }
} 