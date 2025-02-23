package org.PandaScript.pandabroadcast.config;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BroadcastConfig {
    private final Path configPath;
    private final Path dataDirectory;
    private CommentedConfigurationNode config;
    private final Path logFile;

    public BroadcastConfig(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.configPath = dataDirectory.resolve("config.yml");
        this.logFile = dataDirectory.resolve("broadcasts.log");
        loadConfig();
    }

    public void loadConfig() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            if (!Files.exists(configPath)) {
                try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                    Files.copy(in, configPath);
                }
            }

            config = YamlConfigurationLoader.builder()
                    .path(configPath)
                    .build()
                    .load();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean useLegacyColors() {
        return config.node("format", "use-legacy-colors").getBoolean(false);
    }

    public String getPrefix() {
        return config.node("format", "prefix").getString("» ");
    }

    public String getTitle() {
        return config.node("format", "title").getString("Broadcast");
    }

    public String getSuffix() {
        return config.node("format", "suffix").getString(" « ");
    }

    public String getPrefixColor() {
        return config.node("format", "prefix-color").getString("<gold>");
    }

    public String getTitleColor() {
        return config.node("format", "title-color").getString("<yellow>");
    }

    public String getMessageColor() {
        return config.node("format", "message-color").getString("<gray>");
    }

    public boolean isLoggingEnabled() {
        return config.node("logging", "enabled").getBoolean(false);
    }

    public boolean shouldBroadcastToAllByDefault() {
        return "all".equalsIgnoreCase(config.node("targeting", "default_behavior").getString("all"));
    }

    public boolean shouldShowServerScope() {
        return config.node("targeting", "show_server_scope").getBoolean(true);
    }

    public boolean isScopePermissionOnly() {
        return config.node("targeting", "scope_permission_only").getBoolean(false);
    }

    public String getScopeFormat() {
        return config.node("targeting", "scope_format").getString("<gray>(Sent to: %servers%)");
    }

    public void logBroadcast(String sender, String message) {
        if (!isLoggingEnabled()) return;

        try {
            if (!Files.exists(logFile)) {
                Files.createFile(logFile);
            }

            String format = config.node("logging", "format").getString("[%date%] %player%: %message%");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String logMessage = format
                    .replace("%date%", timestamp)
                    .replace("%player%", sender)
                    .replace("%message%", message);

            Files.write(logFile, (logMessage + System.lineSeparator()).getBytes(),
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String formatMessage(String message) {
        return String.format("%s%s%s%s%s%s%s",
                useLegacyColors() ? convertToLegacy(getPrefixColor()) : getPrefixColor(),
                getPrefix(),
                useLegacyColors() ? convertToLegacy(getTitleColor()) : getTitleColor(),
                getTitle(),
                useLegacyColors() ? convertToLegacy(getPrefixColor()) : getPrefixColor(),
                getSuffix(),
                useLegacyColors() ? convertToLegacy(getMessageColor()) : getMessageColor()) + message;
    }

    public String formatMessage(String message, List<String> targetServers) {
        String baseMessage = formatMessage(message);
        
        if (shouldShowServerScope() && targetServers != null) {
            String serverList = String.join(", ", targetServers);
            String scopeMessage = getScopeFormat().replace("%servers%", serverList);
            return baseMessage + " " + (useLegacyColors() ? convertToLegacy(scopeMessage) : scopeMessage);
        }

        return baseMessage;
    }

    private String convertToLegacy(String miniMessage) {
        return miniMessage
                .replace("<gold>", "&6")
                .replace("<yellow>", "&e")
                .replace("<gray>", "&7")
                .replace("<red>", "&c")
                .replace("<green>", "&a")
                .replace("<blue>", "&9")
                .replace("<white>", "&f")
                .replace("<black>", "&0")
                .replace("<bold>", "&l")
                .replace("<italic>", "&o")
                .replace("<underline>", "&n")
                .replace("<strike>", "&m")
                .replace("<reset>", "&r");
    }
} 