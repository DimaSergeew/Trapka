package org.example.bedepay.trapka.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.example.bedepay.trapka.Trapka;

/**
 * Класс для обработки событий взаимодействия с предметами
 */
public class ItemListener implements Listener {
    
    private final Trapka plugin;
    
    /**
     * Конструктор класса ItemListener
     * @param plugin экземпляр главного класса плагина
     */
    public ItemListener(Trapka plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обрабатывает событие удара по сущности
     * @param event событие удара по сущности
     */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Проверяем, что атакующий - игрок
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }
        
        // Проверяем, что цель - игрок
        if (!(event.getEntity() instanceof Player target)) {
            return;
        }
        
        // Получаем предмет в руке
        ItemStack itemInHand = attacker.getInventory().getItemInMainHand();
        
        // Проверяем, что предмет - ловушка
        if (plugin.getNbtUtils().isTrapItem(itemInHand)) {
            // Проверяем разрешение на использование
            if (!attacker.hasPermission("trapka.trap.use")) {
                attacker.sendMessage(Component.text("§cУ вас нет прав на использование ловушки!"));
                event.setCancelled(true);
                return;
            }
            
            // Активируем ловушку
            boolean success = plugin.getTrapManager().activateTrap(attacker, target);
            
            if (success) {
                // Уменьшаем количество предметов
                if (itemInHand.getAmount() > 1) {
                    itemInHand.setAmount(itemInHand.getAmount() - 1);
                } else {
                    attacker.getInventory().setItemInMainHand(null);
                }
            }
            
            // Отменяем событие удара
            event.setCancelled(true);
        }
    }
    
    /**
     * Обрабатывает событие взаимодействия с сущностью
     * @param event событие взаимодействия с сущностью
     */
    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        
        // Проверяем, что цель - игрок
        if (!(event.getRightClicked() instanceof Player target)) {
            return;
        }
        
        // Получаем предмет в руке
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        
        // Проверяем, что предмет - ловушка
        if (plugin.getNbtUtils().isTrapItem(itemInHand)) {
            // Проверяем разрешение на использование
            if (!player.hasPermission("trapka.trap.use")) {
                player.sendMessage(Component.text("§cУ вас нет прав на использование ловушки!"));
                event.setCancelled(true);
                return;
            }
            
            // Активируем ловушку
            boolean success = plugin.getTrapManager().activateTrap(player, target);
            
            if (success) {
                // Уменьшаем количество предметов
                if (itemInHand.getAmount() > 1) {
                    itemInHand.setAmount(itemInHand.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand(null);
                }
            }
            
            // Отменяем событие взаимодействия
            event.setCancelled(true);
        }
    }
    
    /**
     * Обрабатывает событие взаимодействия игрока
     * @param event событие взаимодействия игрока
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = event.getItem();
        
        // Пропускаем, если нет предмета в руке
        if (itemInHand == null) {
            return;
        }
        
        // Проверяем, что предмет - платформа
        if (plugin.getNbtUtils().isPlatformItem(itemInHand)) {
            // Проверяем, что это правый клик
            if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }
            
            event.setCancelled(true);
            
            // Проверяем базовое разрешение на использование
            if (!player.hasPermission("trapka.platform.use")) {
                player.sendMessage(Component.text("§cУ вас нет прав на использование платформы!"));
                return;
            }
            
            // Проверяем кулдаун для всех игроков
            if (plugin.getPlatformManager().isOnCooldown(player.getUniqueId())) {
                long remainingMillis = plugin.getPlatformManager().getRemainingCooldown(player.getUniqueId());
                long remainingSeconds = remainingMillis / 1000;
                player.sendMessage(Component.text("§cВы не можете использовать платформу еще §e" + remainingSeconds + " §cсекунд!"));
                return;
            }
            
            // Проверяем, есть ли уже активная сессия
            if (plugin.getPlatformManager().hasActiveSession(player.getUniqueId())) {
                // Если сессия уже активна, размещаем платформу
                plugin.getPlatformManager().placePlatform(player);
                return;
            }
            
            // Определяем максимальное количество платформ на основе разрешений
            int maxPlatforms = 1; // По умолчанию - одна платформа
            
            // Проверяем все возможные разрешения на использование нескольких платформ
            for (int i = 2; i <= 10; i++) {
                if (player.hasPermission("trapka.platform." + i)) {
                    maxPlatforms = Math.max(maxPlatforms, i);
                }
            }
            
            // Сначала размещаем первую платформу
            boolean platformPlaced = plugin.getPlatformManager().placePlatform(player);
            
            if (platformPlaced) {
                // Если игрок может использовать только одну платформу, 
                // уменьшаем количество предметов в руке и устанавливаем кулдаун
                if (maxPlatforms == 1) {
                    // Уменьшаем количество предметов
                    if (itemInHand.getAmount() > 1) {
                        itemInHand.setAmount(itemInHand.getAmount() - 1);
                    } else {
                        player.getInventory().setItemInMainHand(null);
                    }
                    
                    // Устанавливаем кулдаун для игрока
                    plugin.getPlatformManager().setCooldown(player.getUniqueId());
                } 
                // Если у игрока есть права на размещение нескольких платформ,
                // активируем сессию размещения для оставшихся платформ
                else if (maxPlatforms > 1) {
                    // Активируем сессию размещения платформ с учетом уже размещенной платформы
                    plugin.getPlatformManager().activatePlatformSessionAfterFirstPlacement(player, maxPlatforms);
                }
            }
            
            event.setCancelled(true);
        }
    }
    
    /**
     * Обрабатывает событие повреждения предмета
     * @param event событие повреждения предмета
     */
    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        
        // Проверяем, что предмет - ловушка или платформа
        if (plugin.getNbtUtils().isTrapItem(item) || plugin.getNbtUtils().isPlatformItem(item)) {
            // Отменяем событие повреждения
            event.setCancelled(true);
        }
    }
    
    /**
     * Обрабатывает событие снятия зачарований с ловушки
     * @param event событие взаимодействия игрока
     */
    @EventHandler
    public void onEnchantmentRemove(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            
            // Если предмет - платформа или ловушка, и был нажат блок с наковальней или точильным камнем
            if ((plugin.getNbtUtils().isTrapItem(item) || plugin.getNbtUtils().isPlatformItem(item)) &&
                    event.getClickedBlock() != null &&
                    (event.getClickedBlock().getType() == Material.ANVIL ||
                     event.getClickedBlock().getType() == Material.CHIPPED_ANVIL ||
                     event.getClickedBlock().getType() == Material.DAMAGED_ANVIL ||
                     event.getClickedBlock().getType() == Material.GRINDSTONE)) {
                
                player.sendMessage(Component.text("§cВы не можете изменять этот предмет!"));
                event.setCancelled(true);
            }
        }
    }
} 