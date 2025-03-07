package org.example.bedepay.trapka.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.example.bedepay.trapka.Trapka;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер управления платформами
 */
public class PlatformManager {
    
    private final Trapka plugin;
    private final int selectionTime;
    
    // Карта активных сессий (игрок -> сессия)
    private final Map<UUID, PlatformSession> activeSessions = new ConcurrentHashMap<>();
    
    // Карта активных платформ (идентификатор платформы -> список блоков)
    private final Map<UUID, List<Block>> platformBlocks = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> platformTasks = new ConcurrentHashMap<>();
    
    // Карта кулдаунов (игрок -> время окончания кулдауна)
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    
    /**
     * Конструктор класса PlatformManager
     * @param plugin экземпляр главного класса плагина
     */
    public PlatformManager(Trapka plugin) {
        this.plugin = plugin;
        this.selectionTime = plugin.getConfigManager().getPlatformSelectionTime();
    }
    
    /**
     * Активирует платформу для игрока
     * @param player игрок
     * @return успешность активации платформы
     */
    public boolean activatePlatform(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Проверка наличия сессии
        if (hasActiveSession(playerId)) {
            player.sendMessage(Component.text("§cУ вас уже есть активная сессия размещения платформ!"));
            return false;
        }
        
        // Проверка кулдауна
        if (isOnCooldown(playerId)) {
            long remainingSeconds = getRemainingCooldown(playerId) / 1000;
            player.sendMessage(Component.text("§cВы можете создать следующую платформу через §e" + remainingSeconds + " §cсекунд!"));
            return false;
        }
        
        // Получаем предмет в руке и проверяем, что это платформа
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (!plugin.getNbtUtils().isPlatformItem(itemInHand)) {
            player.sendMessage(Component.text("§cВы должны держать платформу в руке!"));
            return false;
        }
        
        // Определяем максимальное количество платформ на основе разрешений
        int maxPlatforms = 1; // По умолчанию - одна платформа
        
        // Проверяем все возможные разрешения на использование нескольких платформ
        for (int i = 2; i <= 10; i++) {
            if (player.hasPermission("trapka.platform." + i)) {
                maxPlatforms = Math.max(maxPlatforms, i);
            }
        }
        
        // Определяем максимальное количество платформ на основе пермишенов и количества предметов
        int maxCount = Math.min(maxPlatforms, itemInHand.getAmount());
        maxCount = Math.min(maxCount, plugin.getConfigManager().getPlatformMaxCount());
        
        if (maxCount <= 0) {
            player.sendMessage(Component.text("§cУ вас недостаточно предметов платформы!"));
            return false;
        }
        
        // Создаем новую сессию
        PlatformSession session = new PlatformSession(maxCount);
        activeSessions.put(playerId, session);
        
        // Отправляем сообщение игроку
        player.sendMessage(Component.text("§aРежим размещения платформ активирован!"));
        player.sendMessage(Component.text("§7У вас есть §e" + selectionTime + " секунд§7 на размещение §e" + maxCount + " платформ§7."));
        
        // Воспроизводим звук
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        
        // Запускаем таймер для сессии
        BukkitTask task = new BukkitRunnable() {
            private int timeLeft = selectionTime;
            
            @Override
            public void run() {
                if (timeLeft <= 0 || !player.isOnline()) {
                    // Завершаем сессию и устанавливаем кулдаун
                    endSession(playerId);
                    setCooldown(playerId);
                    if (player.isOnline()) {
                        player.sendMessage(Component.text("§cВремя размещения платформ истекло!"));
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f);
                    }
                    cancel();
                    return;
                }
                
                if (timeLeft <= 5) {
                    // Отправляем предупреждение о скором завершении сессии
                    player.sendMessage(Component.text("§cОсталось §e" + timeLeft + " секунд§c на размещение платформ!"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                }
                
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        session.setTask(task);
        
        return true;
    }
    
    /**
     * Устанавливает кулдаун для игрока
     * @param playerId UUID игрока
     */
    public void setCooldown(UUID playerId) {
        cooldowns.put(playerId, System.currentTimeMillis() + plugin.getConfigManager().getPlatformCooldown() * 1000L);
    }
    
    /**
     * Проверяет, находится ли игрок в кулдауне
     * @param playerId идентификатор игрока
     * @return true, если игрок находится в кулдауне
     */
    public boolean isOnCooldown(UUID playerId) {
        Long cooldownEnd = cooldowns.get(playerId);
        return cooldownEnd != null && cooldownEnd > System.currentTimeMillis();
    }
    
    /**
     * Получает оставшееся время кулдауна для игрока
     * @param playerId идентификатор игрока
     * @return оставшееся время в миллисекундах
     */
    public long getRemainingCooldown(UUID playerId) {
        Long cooldownTime = cooldowns.get(playerId);
        if (cooldownTime == null) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long remainingTime = cooldownTime - currentTime;
        
        return Math.max(0, remainingTime);
    }
    
    /**
     * Размещает платформу для игрока
     * @param player игрок, размещающий платформу
     * @return успешность размещения платформы
     */
    public boolean placePlatform(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Получаем расположение игрока
        Location location = player.getLocation();
        World world = location.getWorld();
        
        // Проверяем наличие предмета платформы в инвентаре игрока
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (!plugin.getNbtUtils().isPlatformItem(itemInHand)) {
            player.sendMessage(plugin.getMessageManager().getPlatformNeedInHandMessage());
            return false;
        }
        
        // Проверяем расположение в разрешенном регионе
        if (!plugin.getWorldGuardUtils().canUsePlatformAtLocation(location, player)) {
            player.sendMessage(plugin.getMessageManager().getPlatformRegionNotAllowedMessage());
            return false;
        }
        
        // Получаем настройки платформы
        int platformSize = plugin.getConfigManager().getPlatformSize();
        int platformDuration = plugin.getConfigManager().getPlatformDuration();
        Material platformMaterial = plugin.getConfigManager().getPlatformMaterial();
        
        // Проверяем разрешение на использование платформы
        if (!player.hasPermission("trapka.platform.use")) {
            player.sendMessage(plugin.getMessageManager().getPlatformNoPermissionUseMessage());
            return false;
        }
        
        // Получаем позицию для создания платформы (перед игроком)
        Location platformLocation = getLocationInFront(player, 1);
        
        // Создаем платформу
        List<Block> blocks = createPlatform(platformLocation, platformSize, platformMaterial);
        
        if (blocks.isEmpty()) {
            player.sendMessage(plugin.getMessageManager().getPlatformCannotCreateMessage());
            return false;
        }
        
        // Проверяем, есть ли активная сессия
        PlatformSession session = activeSessions.get(playerId);
        if (session != null) {
            // Проверяем, не превышен ли лимит платформ
            if (session.getPlatformsPlaced() >= session.getMaxPlatforms()) {
                player.sendMessage(plugin.getMessageManager().getPlatformMaxPlatformsMessage());
                return false;
            }
            
            // Увеличиваем счетчик размещенных платформ
            session.incrementPlatformsPlaced();
            
            // Уменьшаем количество предметов в руке игрока
            if (itemInHand.getAmount() > 1) {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
            
            // Если достигли максимального количества, завершаем сессию
            if (session.getPlatformsPlaced() >= session.getMaxPlatforms()) {
                player.sendMessage(plugin.getMessageManager().getPlatformAllPlacedMessage());
                // Завершаем сессию досрочно, так как все платформы размещены
                endSession(playerId);
            } else {
                // Иначе сообщаем, сколько осталось
                int remaining = session.getMaxPlatforms() - session.getPlatformsPlaced();
                player.sendMessage(plugin.getMessageManager().getPlatformPlacedInfoMessage(remaining));
            }
        } else {
            // Если нет активной сессии, то уменьшаем количество предметов в руке игрока
            if (itemInHand.getAmount() > 1) {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
        }
        
        // Генерируем уникальный ID для платформы
        UUID platformId = UUID.randomUUID();
        platformBlocks.put(platformId, blocks);
        
        // Воспроизводим эффекты создания платформы
        plugin.getEffectsUtils().playPlatformCreateEffects(platformLocation);
        
        // Отправляем сообщение игроку
        player.sendMessage(plugin.getMessageManager().getPlatformCreatedMessage(platformDuration));
        
        // Запускаем таймер для удаления платформы
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                // Удаляем платформу
                removePlatform(platformId);
                player.sendMessage(plugin.getMessageManager().getPlatformDisappearedMessage());
            }
        }.runTaskLater(plugin, platformDuration * 20L);
        
        platformTasks.put(platformId, task);
        
        return true;
    }
    
    /**
     * Получает позицию перед игроком
     * @param player игрок
     * @param distance расстояние вперед
     * @return локация перед игроком
     */
    private Location getLocationInFront(Player player, int distance) {
        // Получаем направление взгляда игрока (только горизонтальная составляющая)
        Location location = player.getLocation().clone();
        double yaw = Math.toRadians(location.getYaw());
        
        // Вычисляем смещение по X и Z
        double dx = -Math.sin(yaw) * distance;
        double dz = Math.cos(yaw) * distance;
        
        // Создаем новую локацию перед игроком
        return location.add(dx, 0, dz);
    }
    
    /**
     * Создает платформу
     * @param location локация центра платформы
     * @param size размер платформы
     * @param material материал блоков платформы
     * @return список размещенных блоков
     */
    private List<Block> createPlatform(Location location, int size, Material material) {
        List<Block> blocks = new ArrayList<>();
        World world = location.getWorld();
        
        // Получаем расположение центрального блока (под ногами игрока)
        int centerX = location.getBlockX();
        int centerY = location.getBlockY() - 1; // Под ногами
        int centerZ = location.getBlockZ();
        
        // Рассчитываем смещение от центра
        int offset = size / 2;
        
        // Создаем платформу
        for (int x = centerX - offset; x <= centerX + offset; x++) {
            for (int z = centerZ - offset; z <= centerZ + offset; z++) {
                // Получаем блок на этой позиции
                Block block = world.getBlockAt(x, centerY, z);
                
                // Проверяем, не является ли блок уже твердым
                if (block.getType() == Material.AIR || block.isLiquid()) {
                    // Заменяем только воздух и жидкости
                    block.setType(material, false);
                    blocks.add(block);
                    
                    // Добавляем визуальный эффект при создании каждого блока
                    world.spawnParticle(Particle.CLOUD, 
                            block.getLocation().add(0.5, 1.0, 0.5), 
                            5, 0.2, 0.2, 0.2, 0);
                }
            }
        }
        
        // Если удалось создать хотя бы один блок, добавляем визуальные и звуковые эффекты
        if (!blocks.isEmpty()) {
            Location center = new Location(world, centerX, centerY, centerZ);
            
            // Звуковой эффект создания платформы
            world.playSound(center.clone().add(0, 1, 0), Sound.BLOCK_STONE_PLACE, 1.0f, 0.8f);
            world.playSound(center.clone().add(0, 1, 0), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            
            // Кольцо частиц вокруг платформы
            for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                double x = Math.cos(angle) * (offset + 1);
                double z = Math.sin(angle) * (offset + 1);
                
                Location particleLoc = center.clone().add(x, 0.5, z);
                world.spawnParticle(Particle.CRIT, particleLoc, 3, 0.1, 0.1, 0.1, 0);
            }
            
            // Световые столбы в углах платформы
            world.spawnParticle(Particle.END_ROD, center.clone().add(-offset, 0.5, -offset), 15, 0, 0.8, 0, 0.05);
            world.spawnParticle(Particle.END_ROD, center.clone().add(offset, 0.5, -offset), 15, 0, 0.8, 0, 0.05);
            world.spawnParticle(Particle.END_ROD, center.clone().add(-offset, 0.5, offset), 15, 0, 0.8, 0, 0.05);
            world.spawnParticle(Particle.END_ROD, center.clone().add(offset, 0.5, offset), 15, 0, 0.8, 0, 0.05);
        }
        
        return blocks;
    }
    
    /**
     * Удаляет платформу
     * @param platformId идентификатор платформы
     */
    public void removePlatform(UUID platformId) {
        List<Block> blocks = platformBlocks.get(platformId);
        if (blocks == null) return;
        
        // Вычисляем центр платформы для эффектов
        Location center = null;
        if (!blocks.isEmpty()) {
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
            
            for (Block block : blocks) {
                minX = Math.min(minX, block.getX());
                minY = Math.min(minY, block.getY());
                minZ = Math.min(minZ, block.getZ());
                maxX = Math.max(maxX, block.getX());
                maxY = Math.max(maxY, block.getY());
                maxZ = Math.max(maxZ, block.getZ());
            }
            
            int centerX = (minX + maxX) / 2;
            int centerY = (minY + maxY) / 2;
            int centerZ = (minZ + maxZ) / 2;
            
            center = new Location(blocks.get(0).getWorld(), centerX + 0.5, centerY + 0.5, centerZ + 0.5);
        }
        
        // Воспроизводим эффекты удаления платформы
        if (center != null) {
            plugin.getEffectsUtils().playPlatformRemoveEffects(center);
        }
        
        // Восстанавливаем блоки (в реальной реализации нужно сохранять их исходное состояние)
        for (Block block : blocks) {
            block.setType(Material.AIR);
        }
        
        // Очищаем данные
        platformBlocks.remove(platformId);
        
        // Отменяем задачу
        BukkitTask task = platformTasks.get(platformId);
        if (task != null) {
            task.cancel();
            platformTasks.remove(platformId);
        }
    }
    
    /**
     * Удаляет все размещенные платформы
     */
    public void removeAllPlatforms() {
        for (UUID platformId : new ArrayList<>(platformBlocks.keySet())) {
            removePlatform(platformId);
        }
    }
    
    /**
     * Завершает сессию размещения платформ для игрока
     * @param playerId идентификатор игрока
     */
    public void endSession(UUID playerId) {
        // Отменяем задачу
        PlatformSession session = activeSessions.get(playerId);
        if (session == null) {
            return;
        }
        
        BukkitTask task = session.getTask();
        if (task != null) {
            task.cancel();
        }
        
        // Устанавливаем кулдаун если были размещены платформы
        if (session.getPlatformsPlaced() > 0) {
            setCooldown(playerId);
        }
        
        // Удаляем сессию
        activeSessions.remove(playerId);
        
        // Отправляем сообщение игроку, если он онлайн
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null) {
            player.sendMessage(Component.text("§7Сессия размещения платформ завершена!"));
        }
    }
    
    /**
     * Проверяет, активна ли сессия размещения платформ для игрока
     * @param playerId идентификатор игрока
     * @return true, если сессия активна
     */
    public boolean hasActiveSession(UUID playerId) {
        return activeSessions.containsKey(playerId);
    }
    
    /**
     * Активирует сессию размещения платформ после первого размещения
     * @param player игрок
     * @param maxPlatforms максимальное количество платформ
     * @return true, если сессия успешно активирована
     */
    public boolean activatePlatformSessionAfterFirstPlacement(Player player, int maxPlatforms) {
        UUID playerId = player.getUniqueId();
        
        // Проверяем, не активна ли уже сессия
        if (activeSessions.containsKey(playerId)) {
            player.sendMessage(Component.text("§cУ вас уже активирована сессия размещения платформ!"));
            return false;
        }
        
        // Проверяем кулдаун
        if (isOnCooldown(playerId)) {
            long remainingTime = getRemainingCooldown(playerId);
            player.sendMessage(Component.text("§cВы сможете использовать платформу через §e" + remainingTime + "§c секунд!"));
            return false;
        }
        
        // Создаем новую сессию с учетом уже размещенной платформы
        PlatformSession session = new PlatformSession(maxPlatforms);
        session.incrementPlatformsPlaced(); // Учитываем уже размещенную платформу
        activeSessions.put(playerId, session);
        
        // Получаем время выбора из конфига
        int selectionTime = plugin.getConfigManager().getPlatformSelectionTime();
        
        // Отправляем сообщение игроку
        player.sendMessage(Component.text("§aСессия размещения платформ активирована! §7У вас есть §e" + selectionTime + 
                "§7 секунд, чтобы разместить §e" + (maxPlatforms - 1) + "§7 платформ."));
        
        // Запускаем таймер для сессии
        BukkitTask task = new BukkitRunnable() {
            private int timeLeft = selectionTime;
            
            @Override
            public void run() {
                timeLeft--;
                
                // Отправляем сообщение каждые 5 секунд или в последние 5 секунд
                if (timeLeft <= 5 || timeLeft % 5 == 0) {
                    player.sendMessage(Component.text("§7Осталось §e" + timeLeft + "§7 секунд для размещения платформ."));
                }
                
                // Если время вышло, завершаем сессию
                if (timeLeft <= 0) {
                    endSession(playerId);
                    player.sendMessage(Component.text("§cВремя размещения платформ истекло!"));
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
        
        // Сохраняем задачу в сессии
        session.setTask(task);
        
        return true;
    }
    
    /**
     * Внутренний класс для хранения информации о сессии размещения платформ
     */
    private static class PlatformSession {
        private final int maxPlatforms;
        private int platformsPlaced;
        private BukkitTask task;
        
        public PlatformSession(int maxPlatforms) {
            this.maxPlatforms = maxPlatforms;
            this.platformsPlaced = 0;
        }
        
        public int getMaxPlatforms() {
            return maxPlatforms;
        }
        
        public int getPlatformsPlaced() {
            return platformsPlaced;
        }
        
        public void incrementPlatformsPlaced() {
            platformsPlaced++;
        }
        
        public BukkitTask getTask() {
            return task;
        }
        
        public void setTask(BukkitTask task) {
            this.task = task;
        }
    }
} 