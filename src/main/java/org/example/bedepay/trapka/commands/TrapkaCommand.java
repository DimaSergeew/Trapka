package org.example.bedepay.trapka.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.example.bedepay.trapka.Trapka;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс для обработки команд плагина
 */
public class TrapkaCommand implements CommandExecutor, TabCompleter {
    
    private final Trapka plugin;
    
    /**
     * Конструктор класса TrapkaCommand
     * @param plugin экземпляр главного класса плагина
     */
    public TrapkaCommand(Trapka plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "trap":
                return handleTrapCommand(sender, args);
            case "platform":
                return handlePlatformCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    /**
     * Обрабатывает команду для выдачи ловушки
     * @param sender отправитель команды
     * @param args аргументы команды
     * @return успешность выполнения команды
     */
    private boolean handleTrapCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("trapka.admin")) {
            sender.sendMessage(Component.text("§cУ вас нет прав на использование этой команды!"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(Component.text("§cИспользование: /" + "trapka trap <игрок> [количество]"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("§cИгрок '" + args[1] + "' не найден или не в сети!"));
            return true;
        }
        
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1) {
                    amount = 1;
                } else if (amount > 64) {
                    amount = 64;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("§cНекорректное количество: " + args[2]));
                return true;
            }
        }
        
        ItemStack trapItem = plugin.getNbtUtils().createTrapItem(amount);
        target.getInventory().addItem(trapItem);
        
        sender.sendMessage(Component.text("§aВы выдали §e" + amount + " ловушек§a игроку §e" + target.getName() + "§a!"));
        target.sendMessage(Component.text("§aВы получили §e" + amount + " ловушек§a!"));
        
        return true;
    }
    
    /**
     * Обрабатывает команду для выдачи платформы
     * @param sender отправитель команды
     * @param args аргументы команды
     * @return успешность выполнения команды
     */
    private boolean handlePlatformCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("trapka.admin")) {
            sender.sendMessage(Component.text("§cУ вас нет прав на использование этой команды!"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(Component.text("§cИспользование: /" + "trapka platform <игрок> [количество]"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("§cИгрок '" + args[1] + "' не найден или не в сети!"));
            return true;
        }
        
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1) {
                    amount = 1;
                } else if (amount > 64) {
                    amount = 64;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("§cНекорректное количество: " + args[2]));
                return true;
            }
        }
        
        ItemStack platformItem = plugin.getNbtUtils().createPlatformItem(amount);
        target.getInventory().addItem(platformItem);
        
        sender.sendMessage(Component.text("§aВы выдали §e" + amount + " платформ§a игроку §e" + target.getName() + "§a!"));
        target.sendMessage(Component.text("§aВы получили §e" + amount + " платформ§a!"));
        
        return true;
    }
    
    /**
     * Обрабатывает команду для перезагрузки конфигурации
     * @param sender отправитель команды
     * @return успешность выполнения команды
     */
    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("trapka.admin")) {
            sender.sendMessage(Component.text("§cУ вас нет прав на использование этой команды!"));
            return true;
        }
        
        plugin.getConfigManager().loadConfig();
        sender.sendMessage(Component.text("§aКонфигурация плагина Trapka успешно перезагружена!"));
        
        return true;
    }
    
    /**
     * Отправляет справку по командам
     * @param sender отправитель команды
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("§6=== Помощь по плагину Trapka ==="));
        sender.sendMessage(Component.text("§e/" + "trapka trap <игрок> [количество] §7- Выдать ловушку"));
        sender.sendMessage(Component.text("§e/" + "trapka platform <игрок> [количество] §7- Выдать платформу"));
        sender.sendMessage(Component.text("§e/" + "trapka reload §7- Перезагрузить конфигурацию"));
    }
    
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("trapka.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Arrays.asList("trap", "platform", "reload").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("trap") || args[0].equalsIgnoreCase("platform")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
} 