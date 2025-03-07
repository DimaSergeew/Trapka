package org.example.bedepay.trapka.config;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
    
    // Настройки эффектов ловушки
    private boolean trapSoundsEnabled;
    private Sound trapCreateSound;
    private float trapCreateSoundVolume;
    private float trapCreateSoundPitch;
    private Sound trapRemoveSound;
    private float trapRemoveSoundVolume;
    private float trapRemoveSoundPitch;
    
    private boolean trapParticlesEnabled;
    private Particle trapCreateParticle;
    private int trapCreateParticleCount;
    private Particle trapRemoveParticle;
    private int trapRemoveParticleCount;
    
    // Настройки платформы
    private int platformSize;
    private int platformDuration;
    private int platformSelectionTime;
    private int platformMaxCount;
    private int platformCooldown;
    private Material platformMaterial;
    private List<String> platformAllowedRegions;
    
    // Настройки эффектов платформы
    private boolean platformSoundsEnabled;
    private Sound platformCreateSound;
    private float platformCreateSoundVolume;
    private float platformCreateSoundPitch;
    private Sound platformRemoveSound;
    private float platformRemoveSoundVolume;
    private float platformRemoveSoundPitch;
    
    private boolean platformParticlesEnabled;
    private Particle platformCreateParticle;
    private int platformCreateParticleCount;
    private Particle platformRemoveParticle;
    private int platformRemoveParticleCount;
    
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
        loadEffectsSettings();
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
     * Загрузка настроек эффектов
     */
    private void loadEffectsSettings() {
        // Загрузка настроек звуков и частиц для ловушки
        trapSoundsEnabled = config.getBoolean("trap.effects.sounds.enabled", true);
        
        String trapCreateSoundName = config.getString("trap.effects.sounds.create", "ENTITY_ENDERMAN_TELEPORT");
        try {
            trapCreateSound = Sound.valueOf(trapCreateSoundName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Неизвестный звук создания ловушки: " + trapCreateSoundName + ". Используется ENTITY_ENDERMAN_TELEPORT.");
            trapCreateSound = Sound.ENTITY_ENDERMAN_TELEPORT;
        }
        
        trapCreateSoundVolume = (float) config.getDouble("trap.effects.sounds.create_volume", 1.0);
        trapCreateSoundPitch = (float) config.getDouble("trap.effects.sounds.create_pitch", 1.0);
        
        String trapRemoveSoundName = config.getString("trap.effects.sounds.remove", "BLOCK_GLASS_BREAK");
        try {
            trapRemoveSound = Sound.valueOf(trapRemoveSoundName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Неизвестный звук удаления ловушки: " + trapRemoveSoundName + ". Используется BLOCK_GLASS_BREAK.");
            trapRemoveSound = Sound.BLOCK_GLASS_BREAK;
        }
        
        trapRemoveSoundVolume = (float) config.getDouble("trap.effects.sounds.remove_volume", 1.0);
        trapRemoveSoundPitch = (float) config.getDouble("trap.effects.sounds.remove_pitch", 1.0);
        
        trapParticlesEnabled = config.getBoolean("trap.effects.particles.enabled", true);
        
        String trapCreateParticleName = config.getString("trap.effects.particles.create", "WITCH");
        try {
            trapCreateParticle = Particle.valueOf(trapCreateParticleName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Неизвестная частица создания ловушки: " + trapCreateParticleName + ". Используется WITCH.");
            trapCreateParticle = Particle.WITCH;
        }
        
        trapCreateParticleCount = config.getInt("trap.effects.particles.create_count", 50);
        
        String trapRemoveParticleName = config.getString("trap.effects.particles.remove", "CLOUD");
        try {
            trapRemoveParticle = Particle.valueOf(trapRemoveParticleName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Неизвестная частица удаления ловушки: " + trapRemoveParticleName + ". Используется CLOUD.");
            trapRemoveParticle = Particle.CLOUD;
        }
        
        trapRemoveParticleCount = config.getInt("trap.effects.particles.remove_count", 30);
        
        // Загрузка настроек звуков и частиц для платформы
        platformSoundsEnabled = config.getBoolean("platform.effects.sounds.enabled", true);
        
        String platformCreateSoundName = config.getString("platform.effects.sounds.create", "BLOCK_STONE_PLACE");
        try {
            platformCreateSound = Sound.valueOf(platformCreateSoundName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Неизвестный звук создания платформы: " + platformCreateSoundName + ". Используется BLOCK_STONE_PLACE.");
            platformCreateSound = Sound.BLOCK_STONE_PLACE;
        }
        
        platformCreateSoundVolume = (float) config.getDouble("platform.effects.sounds.create_volume", 1.0);
        platformCreateSoundPitch = (float) config.getDouble("platform.effects.sounds.create_pitch", 0.8);
        
        String platformRemoveSoundName = config.getString("platform.effects.sounds.remove", "BLOCK_STONE_BREAK");
        try {
            platformRemoveSound = Sound.valueOf(platformRemoveSoundName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Неизвестный звук удаления платформы: " + platformRemoveSoundName + ". Используется BLOCK_STONE_BREAK.");
            platformRemoveSound = Sound.BLOCK_STONE_BREAK;
        }
        
        platformRemoveSoundVolume = (float) config.getDouble("platform.effects.sounds.remove_volume", 1.0);
        platformRemoveSoundPitch = (float) config.getDouble("platform.effects.sounds.remove_pitch", 0.8);
        
        platformParticlesEnabled = config.getBoolean("platform.effects.particles.enabled", true);
        
        String platformCreateParticleName = config.getString("platform.effects.particles.create", "CRIT");
        try {
            platformCreateParticle = Particle.valueOf(platformCreateParticleName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Неизвестная частица создания платформы: " + platformCreateParticleName + ". Используется CRIT.");
            platformCreateParticle = Particle.CRIT;
        }
        
        platformCreateParticleCount = config.getInt("platform.effects.particles.create_count", 40);
        
        String platformRemoveParticleName = config.getString("platform.effects.particles.remove", "EXPLOSION");
        try {
            platformRemoveParticle = Particle.valueOf(platformRemoveParticleName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Неизвестная частица удаления платформы: " + platformRemoveParticleName + ". Используется EXPLOSION.");
            platformRemoveParticle = Particle.EXPLOSION;
        }
        
        platformRemoveParticleCount = config.getInt("platform.effects.particles.remove_count", 20);
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
        
        // Настройки эффектов ловушки
        config.set("trap.effects.sounds.enabled", true);
        config.set("trap.effects.sounds.create", "ENTITY_ENDERMAN_TELEPORT");
        config.set("trap.effects.sounds.create_volume", 1.0);
        config.set("trap.effects.sounds.create_pitch", 1.0);
        config.set("trap.effects.sounds.remove", "BLOCK_GLASS_BREAK");
        config.set("trap.effects.sounds.remove_volume", 1.0);
        config.set("trap.effects.sounds.remove_pitch", 1.0);
        
        config.set("trap.effects.particles.enabled", true);
        config.set("trap.effects.particles.create", "WITCH");
        config.set("trap.effects.particles.create_count", 50);
        config.set("trap.effects.particles.remove", "CLOUD");
        config.set("trap.effects.particles.remove_count", 30);
        
        // Настройки платформы
        config.set("platform.size", 3);
        config.set("platform.duration", 10);
        config.set("platform.selection_time", 10);
        config.set("platform.max_count", 3);
        config.set("platform.material", "GLASS");
        
        // Настройки эффектов платформы
        config.set("platform.effects.sounds.enabled", true);
        config.set("platform.effects.sounds.create", "BLOCK_STONE_PLACE");
        config.set("platform.effects.sounds.create_volume", 1.0);
        config.set("platform.effects.sounds.create_pitch", 0.8);
        config.set("platform.effects.sounds.remove", "BLOCK_STONE_BREAK");
        config.set("platform.effects.sounds.remove_volume", 1.0);
        config.set("platform.effects.sounds.remove_pitch", 0.8);
        
        config.set("platform.effects.particles.enabled", true);
        config.set("platform.effects.particles.create", "CRIT");
        config.set("platform.effects.particles.create_count", 40);
        config.set("platform.effects.particles.remove", "EXPLOSION");
        config.set("platform.effects.particles.remove_count", 20);
        
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
    
    // Геттеры для настроек эффектов ловушки
    
    public boolean isTrapSoundsEnabled() {
        return trapSoundsEnabled;
    }
    
    public Sound getTrapCreateSound() {
        return trapCreateSound;
    }
    
    public float getTrapCreateSoundVolume() {
        return trapCreateSoundVolume;
    }
    
    public float getTrapCreateSoundPitch() {
        return trapCreateSoundPitch;
    }
    
    public Sound getTrapRemoveSound() {
        return trapRemoveSound;
    }
    
    public float getTrapRemoveSoundVolume() {
        return trapRemoveSoundVolume;
    }
    
    public float getTrapRemoveSoundPitch() {
        return trapRemoveSoundPitch;
    }
    
    public boolean isTrapParticlesEnabled() {
        return trapParticlesEnabled;
    }
    
    public Particle getTrapCreateParticle() {
        return trapCreateParticle;
    }
    
    public int getTrapCreateParticleCount() {
        return trapCreateParticleCount;
    }
    
    public Particle getTrapRemoveParticle() {
        return trapRemoveParticle;
    }
    
    public int getTrapRemoveParticleCount() {
        return trapRemoveParticleCount;
    }
    
    // Геттеры для настроек эффектов платформы
    
    public boolean isPlatformSoundsEnabled() {
        return platformSoundsEnabled;
    }
    
    public Sound getPlatformCreateSound() {
        return platformCreateSound;
    }
    
    public float getPlatformCreateSoundVolume() {
        return platformCreateSoundVolume;
    }
    
    public float getPlatformCreateSoundPitch() {
        return platformCreateSoundPitch;
    }
    
    public Sound getPlatformRemoveSound() {
        return platformRemoveSound;
    }
    
    public float getPlatformRemoveSoundVolume() {
        return platformRemoveSoundVolume;
    }
    
    public float getPlatformRemoveSoundPitch() {
        return platformRemoveSoundPitch;
    }
    
    public boolean isPlatformParticlesEnabled() {
        return platformParticlesEnabled;
    }
    
    public Particle getPlatformCreateParticle() {
        return platformCreateParticle;
    }
    
    public int getPlatformCreateParticleCount() {
        return platformCreateParticleCount;
    }
    
    public Particle getPlatformRemoveParticle() {
        return platformRemoveParticle;
    }
    
    public int getPlatformRemoveParticleCount() {
        return platformRemoveParticleCount;
    }
} 