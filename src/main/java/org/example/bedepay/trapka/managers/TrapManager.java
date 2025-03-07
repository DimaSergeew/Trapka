package org.example.bedepay.trapka.managers;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.example.bedepay.trapka.Trapka;
import org.example.bedepay.trapka.utils.LocationUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер управления ловушками
 */
public class TrapManager implements Listener {
    
    private final Trapka plugin;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> activeTasks = new ConcurrentHashMap<>();
    private final Map<UUID, List<Block>> changedBlocks = new ConcurrentHashMap<>();
    private final Map<UUID, List<Player>> trappedPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, BossBar> bossBars = new ConcurrentHashMap<>();
    private final Map<UUID, List<BlockState>> originalBlockStates = new ConcurrentHashMap<>();
    
    /**
     * Конструктор класса TrapManager
     * @param plugin экземпляр главного класса плагина
     */
    public TrapManager(Trapka plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Активирует ловушку между двумя игроками
     * @param attacker атакующий игрок
     * @param target целевой игрок
     * @return true, если ловушка была успешно создана
     */
    public boolean activateTrap(Player attacker, Player target) {
        // Проверяем, не находится ли атакующий в кулдауне
        if (isOnCooldown(attacker.getUniqueId())) {
            long remainingTime = getRemainingCooldown(attacker.getUniqueId());
            attacker.sendMessage(plugin.getMessageManager().getTrapOnCooldownMessage(remainingTime));
            return false;
        }
        
        // Проверяем, находятся ли игроки в одном мире
        if (!attacker.getWorld().equals(target.getWorld())) {
            attacker.sendMessage(plugin.getMessageManager().getTrapTargetDifferentWorldMessage());
            return false;
        }
        
        // Проверяем WorldGuard регионы (заглушка, реальная интеграция требует подключения WorldGuard API)
        if (!checkRegion(target.getLocation(), attacker)) {
            attacker.sendMessage(plugin.getMessageManager().getTrapRegionNotAllowedMessage());
            return false;
        }
        
        // Проверяем, находится ли атакующий уже в какой-либо ловушке
        if (isPlayerTrapped(attacker)) {
            attacker.sendMessage(plugin.getMessageManager().getTrapAlreadyTrappedMessage());
            return false;
        }
        
        // Получаем настройки ловушки
        int trapSize = plugin.getConfigManager().getTrapSize();
        int trapDuration = plugin.getConfigManager().getTrapDuration();
        Material trapMaterial = plugin.getConfigManager().getTrapMaterial();
        Set<Material> blacklistedBlocks = plugin.getConfigManager().getBlacklistedBlocks();
        
        // Создаем локацию для ловушки
        Location centerLocation = target.getLocation().clone();
        
        // Сохраняем информацию о блоках, которые будут заменены
        UUID trapId = UUID.randomUUID();
        List<Block> blocks = createTrapCube(centerLocation, trapSize, trapMaterial, blacklistedBlocks, trapId);
        
        if (blocks.isEmpty()) {
            attacker.sendMessage(plugin.getMessageManager().getTrapCannotCreateMessage());
            return false;
        }
        
        changedBlocks.put(trapId, blocks);
        
        // Перемещаем игроков внутрь ловушки
        teleportPlayers(attacker, target, centerLocation, trapSize);
        
        // Добавляем игроков в список пойманных
        List<Player> trapped = new ArrayList<>();
        trapped.add(attacker);
        trapped.add(target);
        trappedPlayers.put(trapId, trapped);
        
        // Воспроизводим эффекты создания ловушки
        plugin.getEffectsUtils().playTrapCreateEffects(centerLocation, trapped);
        
        // Отправляем заголовок игрокам
        Title title = Title.title(
                plugin.getMessageManager().getTrapTargetTrappedMessage(),
                plugin.getMessageManager().getTrapWillDisappearMessage(trapDuration),
                Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(2000), Duration.ofMillis(500))
        );
        target.showTitle(title);
        
        // Создаем полоску босса
        BossBar bossBar = BossBar.bossBar(
                plugin.getMessageManager().getTrapBossBarTitle(trapDuration),
                1.0f,
                BossBar.Color.RED,
                BossBar.Overlay.PROGRESS
        );
        
        // Показываем полоску босса всем игрокам в ловушке
        for (Player player : trapped) {
            player.showBossBar(bossBar);
        }
        
        bossBars.put(trapId, bossBar);
        
        // Устанавливаем кулдаун для атакующего
        setCooldown(attacker.getUniqueId());
        
        // Запускаем таймер для удаления ловушки
        BukkitTask task = new BukkitRunnable() {
            private int timeLeft = trapDuration;
            
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    // Удаляем ловушку
                    removeTrap(trapId);
                    cancel();
                    return;
                }
                
                // Обновляем полоску босса
                float progress = (float) timeLeft / trapDuration;
                bossBar.progress(progress);
                bossBar.name(plugin.getMessageManager().getTrapBossBarTitle(timeLeft));
                
                // Добавляем только звуковой эффект, который становится выше к концу
                if (timeLeft % 5 == 0 || timeLeft <= 3) {
                    List<Player> players = trappedPlayers.get(trapId);
                    if (players != null && !players.isEmpty()) {
                        Location center = players.get(0).getLocation().clone();
                        center.setY(center.getY() + 1);
                        
                        // Звуковой эффект, который становится выше к концу
                        float pitch = 0.8f + (1.0f - timeLeft / (float)trapDuration) * 0.8f;
                        center.getWorld().playSound(center, 
                                                 timeLeft <= 3 ? Sound.BLOCK_NOTE_BLOCK_PLING : Sound.BLOCK_NOTE_BLOCK_BELL, 
                                                 0.5f, pitch);
                    }
                }
                
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        activeTasks.put(trapId, task);
        
        return true;
    }
    
    /**
     * Создает куб из блоков ловушки
     * @param center центр ловушки
     * @param size размер ловушки
     * @param material материал ловушки
     * @param blacklistedBlocks черный список блоков
     * @param trapId идентификатор ловушки
     * @return список измененных блоков
     */
    private List<Block> createTrapCube(Location center, int size, Material material, Set<Material> blacklistedBlocks, UUID trapId) {
        List<Block> changedBlocks = new ArrayList<>();
        List<BlockState> originalStates = new ArrayList<>();
        World world = center.getWorld();
        
        int halfSize = size / 2;
        
        // Сначала очищаем всё внутреннее пространство, чтобы избежать застревания в блоках
        for (int x = -halfSize + 1; x < halfSize; x++) {
            for (int y = -halfSize + 1; y < halfSize; y++) {
                for (int z = -halfSize + 1; z < halfSize; z++) {
                    Block block = world.getBlockAt(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z);
                    
                    // Не очищаем блоки из черного списка
                    if (blacklistedBlocks.contains(block.getType())) {
                        continue;
                    }
                    
                    // Сохраняем оригинальное состояние и меняем на воздух
                    if (block.getType() != Material.AIR) {
                        originalStates.add(block.getState());
                        changedBlocks.add(block);
                        block.setType(Material.AIR);
                    }
                }
            }
        }
        
        // Затем создаем оболочку куба
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int y = -halfSize; y <= halfSize; y++) {
                for (int z = -halfSize; z <= halfSize; z++) {
                    // Проверяем, является ли блок частью оболочки куба
                    if (Math.abs(x) == halfSize || Math.abs(y) == halfSize || Math.abs(z) == halfSize) {
                        Block block = world.getBlockAt(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z);
                        
                        // Пропускаем блоки из черного списка
                        if (blacklistedBlocks.contains(block.getType())) {
                            continue;
                        }
                        
                        // Сохраняем исходное состояние блока перед заменой
                        originalStates.add(block.getState());
                        changedBlocks.add(block);
                        block.setType(material);
                    }
                }
            }
        }
        
        // Сохраняем оригинальные состояния блоков
        originalBlockStates.put(trapId, originalStates);
        
        // Специальные эффекты создания ловушки
        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.2f);
        
        return changedBlocks;
    }
    
    /**
     * Телепортирует игроков внутрь ловушки
     * @param attacker атакующий игрок
     * @param target целевой игрок
     * @param center центр ловушки
     * @param size размер ловушки
     */
    private void teleportPlayers(Player attacker, Player target, Location center, int size) {
        int halfSize = size / 2 - 1;
        
        // Создаем локации для телепортации
        Location attackerLoc = center.clone();
        attackerLoc.setX(center.getX() - halfSize);
        attackerLoc.setY(center.getY());
        attackerLoc.setYaw(90); // Направление на восток
        
        Location targetLoc = center.clone();
        targetLoc.setX(center.getX() + halfSize);
        targetLoc.setY(center.getY());
        targetLoc.setYaw(-90); // Направление на запад
        
        // Телепортируем игроков
        attacker.teleport(attackerLoc);
        target.teleport(targetLoc);
        
        // Добавляем эффекты телепортации для атакующего
        attacker.getWorld().spawnParticle(Particle.PORTAL, attacker.getLocation(), 40, 0.2, 1, 0.2, 0.1);
        attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.2f);
        
        // Добавляем эффекты телепортации для цели
        target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation(), 40, 0.2, 1, 0.2, 0.1);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.0f);
        
        // Эффект после телепортации
        attacker.getWorld().spawnParticle(Particle.EXPLOSION, attackerLoc, 10, 0.2, 0.2, 0.2, 0.05);
        target.getWorld().spawnParticle(Particle.EXPLOSION, targetLoc, 10, 0.2, 0.2, 0.2, 0.05);
    }
    
    /**
     * Удаляет ловушку и восстанавливает блоки
     * @param trapId идентификатор ловушки
     */
    public void removeTrap(UUID trapId) {
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("Начинаем удаление ловушки: " + trapId);
        }
        
        // Получаем локацию центра ловушки для телепортации
        Location safeLocation = null;
        List<Block> blocks = changedBlocks.get(trapId);
        
        if (blocks != null && !blocks.isEmpty()) {
            // Находим центр ловушки
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
            
            // Центр ловушки
            int centerX = (minX + maxX) / 2;
            int centerY = (minY + maxY) / 2;
            int centerZ = (minZ + maxZ) / 2;
            
            World world = blocks.get(0).getWorld();
            safeLocation = new Location(world, centerX + 0.5, centerY + 0.5, centerZ + 0.5);
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("Центр ловушки: " + centerX + "," + centerY + "," + centerZ);
            }
        }
        
        // Телепортируем игроков на безопасную локацию перед восстановлением блоков
        List<Player> players = trappedPlayers.get(trapId);
        if (players != null && safeLocation != null) {
            // Воспроизводим эффекты удаления ловушки
            plugin.getEffectsUtils().playTrapRemoveEffects(safeLocation, players);
            
            for (Player player : players) {
                if (player.isOnline()) {
                    // Телепортируем игрока немного выше центра ловушки, чтобы избежать застревания
                    Location teleportLoc = safeLocation.clone().add(0, 1, 0);
                    player.teleport(teleportLoc);
                    player.sendMessage(plugin.getMessageManager().getTrapDisappearedMessage());
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("Телепортирован игрок: " + player.getName() + " на " + teleportLoc.getX() + "," + teleportLoc.getY() + "," + teleportLoc.getZ());
                    }
                }
            }
        }
        
        // Убираем полоску босса
        BossBar bossBar = bossBars.get(trapId);
        if (bossBar != null) {
            if (players != null) {
                for (Player player : players) {
                    if (player.isOnline()) {
                        player.hideBossBar(bossBar);
                    }
                }
            }
            bossBars.remove(trapId);
        }
        
        // Восстанавливаем блоки
        List<BlockState> states = originalBlockStates.get(trapId);
        if (states != null && !states.isEmpty()) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("Восстанавливаем " + states.size() + " блоков");
            }
            
            // Сначала восстанавливаем внутренние блоки
            for (BlockState state : states) {
                try {
                    if (state != null && state.getWorld() != null) {
                        state.update(true, false);
                        if (plugin.getConfigManager().isDebugMode()) {
                            plugin.getLogger().info("Восстановлен блок: " + state.getType() + " на " + state.getX() + "," + state.getY() + "," + state.getZ());
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Ошибка при восстановлении блока: " + e.getMessage());
                }
            }
        } else {
            plugin.getLogger().warning("Не найдены оригинальные состояния блоков для ловушки: " + trapId);
            
            // Резервный вариант - если нет сохраненных состояний, просто очищаем блоки
            if (blocks != null) {
                for (Block block : blocks) {
                    block.setType(Material.AIR);
                }
            }
        }
        
        // Звуковой эффект исчезновения
        if (safeLocation != null) {
            safeLocation.getWorld().playSound(safeLocation, Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 0.8f);
        }
        
        // Очищаем данные
        changedBlocks.remove(trapId);
        originalBlockStates.remove(trapId);
        trappedPlayers.remove(trapId);
        
        // Отменяем задачу
        BukkitTask task = activeTasks.get(trapId);
        if (task != null) {
            task.cancel();
            activeTasks.remove(trapId);
        }
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("Ловушка успешно удалена: " + trapId);
        }
    }
    
    /**
     * Удаляет все активные ловушки
     */
    public void removeAllTraps() {
        for (UUID trapId : new ArrayList<>(changedBlocks.keySet())) {
            removeTrap(trapId);
        }
    }
    
    /**
     * Устанавливает кулдаун для игрока
     * @param playerId идентификатор игрока
     */
    private void setCooldown(UUID playerId) {
        cooldowns.put(playerId, System.currentTimeMillis() + (plugin.getConfigManager().getTrapCooldown() * 1000L));
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
     * @return оставшееся время в секундах
     */
    public long getRemainingCooldown(UUID playerId) {
        Long cooldownEnd = cooldowns.get(playerId);
        if (cooldownEnd == null) return 0;
        
        long remainingTime = (cooldownEnd - System.currentTimeMillis()) / 1000;
        return Math.max(0, remainingTime);
    }
    
    /**
     * Проверяет, можно ли создать ловушку в указанном регионе
     * @param location локация для проверки
     * @param player игрок, который пытается создать ловушку
     * @return true, если в этом регионе можно создать ловушку
     */
    private boolean checkRegion(Location location, Player player) {
        return plugin.getWorldGuardUtils().canUseTrapAtLocation(location, player);
    }
    
    // Добавим метод для проверки, находится ли игрок в какой-либо ловушке
    public boolean isPlayerTrapped(Player player) {
        for (List<Player> players : trappedPlayers.values()) {
            if (players.contains(player)) {
                return true;
            }
        }
        return false;
    }
    
    // Добавить слушатель для предотвращения разрушения блоков ловушки
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        
        // Проверяем, является ли блок частью активной ловушки
        for (List<Block> blocks : changedBlocks.values()) {
            if (blocks.contains(block)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Component.text("§cВы не можете разрушить блок ловушки!"));
                break;
            }
        }
    }
    
    /**
     * Обработчик события запуска снаряда (для блокировки эндер-перлов в ловушке)
     */
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        // Проверяем, что это эндер-перл
        if (event.getEntity() instanceof EnderPearl) {
            // Проверяем, что стрелок - игрок
            if (event.getEntity().getShooter() instanceof Player player) {
                // Проверяем, включена ли блокировка эндер-перлов
                if (plugin.getConfigManager().isBlockEnderpearls()) {
                    // Проверяем, находится ли игрок в ловушке
                    if (isPlayerTrapped(player)) {
                        // Отменяем событие
                        event.setCancelled(true);
                        // Отправляем сообщение
                        player.sendMessage(Component.text("§cВы не можете использовать эндер-перлы внутри ловушки!"));
                    }
                }
            }
        }
    }
    
    /**
     * Обработчик события употребления предмета (для блокировки хоруса в ловушке)
     */
    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Проверяем, что это хорус
        if (item.getType() == Material.CHORUS_FRUIT) {
            // Проверяем, включена ли блокировка эндер-перлов (та же настройка для хоруса)
            if (plugin.getConfigManager().isBlockEnderpearls()) {
                // Проверяем, находится ли игрок в ловушке
                if (isPlayerTrapped(player)) {
                    // Отменяем событие
                    event.setCancelled(true);
                    // Отправляем сообщение
                    player.sendMessage(Component.text("§cВы не можете использовать хорус внутри ловушки!"));
                }
            }
        }
    }
} 