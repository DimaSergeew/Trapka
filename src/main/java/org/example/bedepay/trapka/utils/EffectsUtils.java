package org.example.bedepay.trapka.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.example.bedepay.trapka.Trapka;

import java.util.List;

/**
 * Класс для управления звуковыми и визуальными эффектами
 */
public class EffectsUtils {

    private final Trapka plugin;

    /**
     * Конструктор класса EffectsUtils
     * @param plugin экземпляр главного класса плагина
     */
    public EffectsUtils(Trapka plugin) {
        this.plugin = plugin;
    }

    /**
     * Воспроизводит звук создания ловушки
     * @param location локация воспроизведения звука
     */
    public void playTrapCreateSound(Location location) {
        if (!plugin.getConfigManager().isTrapSoundsEnabled()) return;
        
        location.getWorld().playSound(
            location,
            plugin.getConfigManager().getTrapCreateSound(),
            plugin.getConfigManager().getTrapCreateSoundVolume(),
            plugin.getConfigManager().getTrapCreateSoundPitch()
        );
    }

    /**
     * Воспроизводит звук удаления ловушки
     * @param location локация воспроизведения звука
     */
    public void playTrapRemoveSound(Location location) {
        if (!plugin.getConfigManager().isTrapSoundsEnabled()) return;
        
        location.getWorld().playSound(
            location,
            plugin.getConfigManager().getTrapRemoveSound(),
            plugin.getConfigManager().getTrapRemoveSoundVolume(),
            plugin.getConfigManager().getTrapRemoveSoundPitch()
        );
    }

    /**
     * Создает эффект частиц при создании ловушки
     * @param location локация создания частиц
     */
    public void showTrapCreateParticles(Location location) {
        if (!plugin.getConfigManager().isTrapParticlesEnabled()) return;
        
        location.getWorld().spawnParticle(
            plugin.getConfigManager().getTrapCreateParticle(),
            location,
            plugin.getConfigManager().getTrapCreateParticleCount(),
            0.5, 0.5, 0.5, 0.1
        );
    }

    /**
     * Создает эффект частиц при удалении ловушки
     * @param location локация создания частиц
     */
    public void showTrapRemoveParticles(Location location) {
        if (!plugin.getConfigManager().isTrapParticlesEnabled()) return;
        
        location.getWorld().spawnParticle(
            plugin.getConfigManager().getTrapRemoveParticle(),
            location,
            plugin.getConfigManager().getTrapRemoveParticleCount(),
            0.5, 0.5, 0.5, 0.1
        );
    }

    /**
     * Воспроизводит звук создания платформы
     * @param location локация воспроизведения звука
     */
    public void playPlatformCreateSound(Location location) {
        if (!plugin.getConfigManager().isPlatformSoundsEnabled()) return;
        
        location.getWorld().playSound(
            location,
            plugin.getConfigManager().getPlatformCreateSound(),
            plugin.getConfigManager().getPlatformCreateSoundVolume(),
            plugin.getConfigManager().getPlatformCreateSoundPitch()
        );
    }

    /**
     * Воспроизводит звук удаления платформы
     * @param location локация воспроизведения звука
     */
    public void playPlatformRemoveSound(Location location) {
        if (!plugin.getConfigManager().isPlatformSoundsEnabled()) return;
        
        location.getWorld().playSound(
            location,
            plugin.getConfigManager().getPlatformRemoveSound(),
            plugin.getConfigManager().getPlatformRemoveSoundVolume(),
            plugin.getConfigManager().getPlatformRemoveSoundPitch()
        );
    }

    /**
     * Создает эффект частиц при создании платформы
     * @param location локация создания частиц
     */
    public void showPlatformCreateParticles(Location location) {
        if (!plugin.getConfigManager().isPlatformParticlesEnabled()) return;
        
        location.getWorld().spawnParticle(
            plugin.getConfigManager().getPlatformCreateParticle(),
            location,
            plugin.getConfigManager().getPlatformCreateParticleCount(),
            0.5, 0.5, 0.5, 0.1
        );
    }

    /**
     * Создает эффект частиц при удалении платформы
     * @param location локация создания частиц
     */
    public void showPlatformRemoveParticles(Location location) {
        if (!plugin.getConfigManager().isPlatformParticlesEnabled()) return;
        
        location.getWorld().spawnParticle(
            plugin.getConfigManager().getPlatformRemoveParticle(),
            location,
            plugin.getConfigManager().getPlatformRemoveParticleCount(),
            0.5, 0.5, 0.5, 0.1
        );
    }
    
    /**
     * Проигрывает звуковые эффекты и показывает частицы при создании ловушки
     * @param location локация ловушки
     * @param blocks список блоков ловушки
     */
    public void playTrapCreateEffects(Location location, List<Player> players) {
        playTrapCreateSound(location);
        showTrapCreateParticles(location);
        
        // Проигрываем звук захваченным игрокам
        if (players != null) {
            for (Player player : players) {
                playTrapCreateSound(player.getLocation());
            }
        }
    }
    
    /**
     * Проигрывает звуковые эффекты и показывает частицы при удалении ловушки
     * @param location локация ловушки
     * @param blocks список блоков ловушки
     */
    public void playTrapRemoveEffects(Location location, List<Player> players) {
        playTrapRemoveSound(location);
        showTrapRemoveParticles(location);
        
        // Проигрываем звук захваченным игрокам
        if (players != null) {
            for (Player player : players) {
                playTrapRemoveSound(player.getLocation());
            }
        }
    }
    
    /**
     * Проигрывает звуковые эффекты и показывает частицы при создании платформы
     * @param location локация платформы
     */
    public void playPlatformCreateEffects(Location location) {
        playPlatformCreateSound(location);
        showPlatformCreateParticles(location);
    }
    
    /**
     * Проигрывает звуковые эффекты и показывает частицы при удалении платформы
     * @param location локация платформы
     */
    public void playPlatformRemoveEffects(Location location) {
        playPlatformRemoveSound(location);
        showPlatformRemoveParticles(location);
    }
} 