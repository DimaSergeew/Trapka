package org.example.bedepay.trapka.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.example.bedepay.trapka.Trapka;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс для управления сообщениями плагина
 */
public class MessageManager {

    private final Trapka plugin;
    private FileConfiguration config;
    private final String DEFAULT_PREFIX = "&6[Trapka] &r";
    private String prefix;

    /**
     * Конструктор класса MessageManager
     * @param plugin экземпляр главного класса плагина
     */
    public MessageManager(Trapka plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    /**
     * Загружает сообщения из конфигурации
     */
    public void loadMessages() {
        this.config = plugin.getConfig();
        try {
            this.prefix = getString("messages.prefix", DEFAULT_PREFIX);
            if (this.prefix == null || this.prefix.isEmpty()) {
                plugin.getLogger().warning("Префикс не найден, используется значение по умолчанию");
                this.prefix = DEFAULT_PREFIX;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при загрузке сообщений: " + e.getMessage());
            this.prefix = DEFAULT_PREFIX;
        }
    }

    /**
     * Получает строку из конфига и форматирует её
     * @param path путь к сообщению в конфиге
     * @param defaultValue значение по умолчанию, если сообщение не найдено
     * @param placeholders плейсхолдеры для замены в сообщении
     * @return форматированную строку
     */
    public String getString(String path, String defaultValue, Object... placeholders) {
        String message = config.getString(path, defaultValue);
        
        // Проверка на null, чтобы избежать NullPointerException
        if (message == null) {
            plugin.getLogger().warning("Сообщение по пути " + path + " не найдено, используется значение по умолчанию");
            message = defaultValue;
            // Если даже значение по умолчанию null, используем пустую строку
            if (message == null) {
                message = "";
            }
        }
        
        // Заменяем {prefix} на значение префикса
        message = message.replace("{prefix}", prefix != null ? prefix : DEFAULT_PREFIX);
        
        // Если есть дополнительные плейсхолдеры, обрабатываем их
        if (placeholders.length > 0 && placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                String placeholder = String.valueOf(placeholders[i]);
                String value = String.valueOf(placeholders[i + 1]);
                message = message.replace("{" + placeholder + "}", value);
            }
        }
        
        return message;
    }

    /**
     * Получает Component с сообщением из конфига
     * @param path путь к сообщению в конфиге
     * @param defaultValue значение по умолчанию, если сообщение не найдено
     * @param placeholders плейсхолдеры для замены в сообщении
     * @return Component с сообщением
     */
    public Component getMessage(String path, String defaultValue, Object... placeholders) {
        String message = getString(path, defaultValue, placeholders);
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    // Общие сообщения
    public Component getNoPermissionMessage() {
        return getMessage("messages.no_permission", "{prefix}&cУ вас нет прав на использование этой команды!");
    }

    public Component getPlayerNotFoundMessage(String playerName) {
        return getMessage("messages.player_not_found", "{prefix}&cИгрок '{player}' не найден или не в сети!", 
                "player", playerName);
    }

    public Component getInvalidAmountMessage(String amount) {
        return getMessage("messages.invalid_amount", "{prefix}&cНекорректное количество: {amount}", 
                "amount", amount);
    }

    public Component getConfigReloadedMessage() {
        return getMessage("messages.config_reloaded", "{prefix}&aКонфигурация плагина Trapka успешно перезагружена!");
    }

    // Сообщения ловушки
    public Component getTrapOnCooldownMessage(long seconds) {
        return getMessage("messages.trap.on_cooldown", "{prefix}&cВы не можете использовать ловушку ещё {time} секунд!", 
                "time", seconds);
    }

    public Component getTrapTargetDifferentWorldMessage() {
        return getMessage("messages.trap.target_different_world", "{prefix}&cЦель находится в другом мире!");
    }

    public Component getTrapRegionNotAllowedMessage() {
        return getMessage("messages.trap.region_not_allowed", "{prefix}&cВы не можете создать ловушку в этом регионе!");
    }

    public Component getTrapAlreadyTrappedMessage() {
        return getMessage("messages.trap.already_trapped", "{prefix}&cВы не можете активировать ловушку, находясь внутри другой ловушки!");
    }

    public Component getTrapCannotCreateMessage() {
        return getMessage("messages.trap.cannot_create", "{prefix}&cНевозможно создать ловушку в этом месте!");
    }

    public Component getTrapTargetTrappedMessage() {
        return getMessage("messages.trap.target_trapped", "{prefix}&cВас поймали в ловушку!");
    }

    public Component getTrapWillDisappearMessage(int time) {
        return getMessage("messages.trap.trap_will_disappear", "{prefix}&7Ловушка исчезнет через {time} секунд", 
                "time", time);
    }

    public Component getTrapBossBarTitle(int time) {
        return getMessage("messages.trap.bossbar_title", "Ловушка исчезнет через: {time} секунд", 
                "time", time);
    }

    public Component getTrapDisappearedMessage() {
        return getMessage("messages.trap.trap_disappeared", "{prefix}&aЛовушка исчезла!");
    }

    public Component getTrapCannotBreakBlockMessage() {
        return getMessage("messages.trap.cannot_break_block", "{prefix}&cВы не можете разрушить блок ловушки!");
    }

    public Component getTrapCannotUseEnderpearlMessage() {
        return getMessage("messages.trap.cannot_use_enderpearl", "{prefix}&cВы не можете использовать эндер-перлы внутри ловушки!");
    }

    public Component getTrapCannotUseChorusMessage() {
        return getMessage("messages.trap.cannot_use_chorus", "{prefix}&cВы не можете использовать хорус внутри ловушки!");
    }

    public Component getTrapNoPermissionUseMessage() {
        return getMessage("messages.trap.no_permission_use", "{prefix}&cУ вас нет прав на использование ловушки!");
    }

    public Component getTrapItemModifiedMessage() {
        return getMessage("messages.trap.item_modified", "{prefix}&cВы не можете изменять этот предмет!");
    }

    public Component getTrapGivenToPlayerMessage(int amount, String playerName) {
        return getMessage("messages.trap.given_to_player", "{prefix}&aВы выдали &e{amount} ловушек&a игроку &e{player}&a!",
                "amount", amount, "player", playerName);
    }

    public Component getTrapReceivedMessage(int amount) {
        return getMessage("messages.trap.received", "{prefix}&aВы получили &e{amount} ловушек&a!",
                "amount", amount);
    }

    public Component getTrapUsageMessage() {
        return getMessage("messages.trap.usage", "{prefix}&cИспользование: /trapka trap <игрок> [количество]");
    }

    // Сообщения платформы
    public Component getPlatformActiveSessionMessage() {
        return getMessage("messages.platform.active_session", "{prefix}&cУ вас уже есть активная сессия размещения платформ!");
    }

    public Component getPlatformOnCooldownMessage(long seconds) {
        return getMessage("messages.platform.on_cooldown", "{prefix}&cВы можете создать следующую платформу через &e{time} &cсекунд!",
                "time", seconds);
    }

    public Component getPlatformNeedInHandMessage() {
        return getMessage("messages.platform.need_in_hand", "{prefix}&cВы должны держать платформу в руке!");
    }

    public Component getPlatformNotEnoughItemsMessage() {
        return getMessage("messages.platform.not_enough_items", "{prefix}&cУ вас недостаточно предметов платформы!");
    }

    public Component getPlatformModeActivatedMessage() {
        return getMessage("messages.platform.mode_activated", "{prefix}&aРежим размещения платформ активирован!");
    }

    public Component getPlatformPlacementInfoMessage(int time, int count) {
        return getMessage("messages.platform.placement_info", "{prefix}&7У вас есть &e{time} секунд&7 на размещение &e{count} платформ&7.",
                "time", time, "count", count);
    }

    public Component getPlatformTimeExpiredMessage() {
        return getMessage("messages.platform.time_expired", "{prefix}&cВремя размещения платформ истекло!");
    }

    public Component getPlatformTimeLeftMessage(int time) {
        return getMessage("messages.platform.time_left", "{prefix}&cОсталось &e{time} секунд&c на размещение платформ!",
                "time", time);
    }

    public Component getPlatformNoPermissionUseMessage() {
        return getMessage("messages.platform.no_permission_use", "{prefix}&cУ вас нет прав на использование платформы!");
    }

    public Component getPlatformRegionNotAllowedMessage() {
        return getMessage("messages.platform.region_not_allowed", "{prefix}&cВы не можете использовать платформу в этом регионе!");
    }

    public Component getPlatformCannotCreateMessage() {
        return getMessage("messages.platform.cannot_create", "{prefix}&cНевозможно создать платформу в этом месте!");
    }

    public Component getPlatformMaxPlatformsMessage() {
        return getMessage("messages.platform.max_platforms", "{prefix}&cВы уже разместили максимальное количество платформ!");
    }

    public Component getPlatformAllPlacedMessage() {
        return getMessage("messages.platform.all_placed", "{prefix}&aВы разместили все доступные платформы!");
    }

    public Component getPlatformPlacedInfoMessage(int remaining) {
        return getMessage("messages.platform.placed_info", "{prefix}&aПлатформа размещена! &7Осталось: &e{remaining}&7.",
                "remaining", remaining);
    }

    public Component getPlatformCreatedMessage(int time) {
        return getMessage("messages.platform.created", "{prefix}&aПлатформа создана! &7Она исчезнет через &e{time}&7 секунд.",
                "time", time);
    }

    public Component getPlatformDisappearedMessage() {
        return getMessage("messages.platform.disappeared", "{prefix}&7Ваша платформа исчезла!");
    }

    public Component getPlatformSessionEndedMessage() {
        return getMessage("messages.platform.session_ended", "{prefix}&7Сессия размещения платформ завершена!");
    }

    public Component getPlatformUsageMessage() {
        return getMessage("messages.platform.usage", "{prefix}&cИспользование: /trapka platform <игрок> [количество]");
    }

    public Component getPlatformGivenToPlayerMessage(int amount, String playerName) {
        return getMessage("messages.platform.given_to_player", "{prefix}&aВы выдали &e{amount} платформ&a игроку &e{player}&a!",
                "amount", amount, "player", playerName);
    }

    public Component getPlatformReceivedMessage(int amount) {
        return getMessage("messages.platform.received", "{prefix}&aВы получили &e{amount} платформ&a!",
                "amount", amount);
    }

    // Сообщения помощи
    public Component getHelpHeaderMessage() {
        return getMessage("messages.help.header", "&6=== Помощь по плагину Trapka ===");
    }

    public Component getHelpTrapCommandMessage() {
        return getMessage("messages.help.trap_command", "&e/trapka trap <игрок> [количество] &7- Выдать ловушку");
    }

    public Component getHelpPlatformCommandMessage() {
        return getMessage("messages.help.platform_command", "&e/trapka platform <игрок> [количество] &7- Выдать платформу");
    }

    public Component getHelpReloadCommandMessage() {
        return getMessage("messages.help.reload_command", "&e/trapka reload &7- Перезагрузить конфигурацию");
    }
} 