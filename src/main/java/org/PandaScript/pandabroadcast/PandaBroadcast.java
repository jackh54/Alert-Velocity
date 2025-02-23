package org.PandaScript.pandabroadcast;

import com.google.inject.Inject;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bstats.velocity.Metrics;
import org.bstats.charts.SimplePie;
import org.slf4j.Logger;
import org.PandaScript.pandabroadcast.config.BroadcastConfig;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Plugin(
    id = "pandabroadcast",
    name = "PandaBroadcast",
    version = "1.1",
    description = "A powerful broadcast plugin for Velocity servers",
    authors = {"PandaScript"}
)
public class PandaBroadcast {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Metrics.Factory metricsFactory;
    private BroadcastConfig config;
    private final AtomicInteger totalBroadcasts = new AtomicInteger(0);

    @Inject
    public PandaBroadcast(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        config = new BroadcastConfig(dataDirectory);
        Metrics metrics = metricsFactory.make(this, 24894);
        metrics.addCustomChart(new SimplePie("total_broadcasts", () -> String.valueOf(totalBroadcasts.get())));
        
        server.getCommandManager().register("pandabroadcast", new MainCommand(server, logger, config, totalBroadcasts));
        server.getCommandManager().register("broadcast", new BroadcastCommand(server, logger, config, totalBroadcasts));
        
        logger.info("PandaBroadcast plugin loaded!");
    }

    public static class MainCommand implements SimpleCommand {
        private final ProxyServer server;
        private final Logger logger;
        private final BroadcastConfig config;
        private final AtomicInteger totalBroadcasts;

        public MainCommand(ProxyServer server, Logger logger, BroadcastConfig config, AtomicInteger totalBroadcasts) {
            this.server = server;
            this.logger = logger;
            this.config = config;
            this.totalBroadcasts = totalBroadcasts;
        }

        @Override
        public void execute(Invocation invocation) {
            if (invocation.arguments().length == 0) {
                showHelp(invocation);
                return;
            }

            String subCommand = invocation.arguments()[0].toLowerCase();
            if (subCommand.equals("reload") && invocation.source().hasPermission("pandabroadcast.reload")) {
                config.loadConfig();
                invocation.source().sendMessage(MiniMessage.miniMessage().deserialize("<green>PandaBroadcast configuration reloaded!"));
                logger.info("PandaBroadcast configuration reloaded by " + 
                    (invocation.source() instanceof Player ? 
                        ((Player) invocation.source()).getUsername() : "Console"));
            } else {
                showHelp(invocation);
            }
        }

        private void showHelp(Invocation invocation) {
            invocation.source().sendMessage(MiniMessage.miniMessage().deserialize("<gold>PandaBroadcast Commands:"));
            invocation.source().sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/broadcast <message> <gray>- Send a message to all servers"));
            invocation.source().sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/broadcast <servers> <message> <gray>- Send a message to specific servers"));
            if (invocation.source().hasPermission("pandabroadcast.reload")) {
                invocation.source().sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/pandabroadcast reload <gray>- Reload the configuration"));
            }
        }

        @Override
        public boolean hasPermission(Invocation invocation) {
            return true;
        }
    }

    public static class BroadcastCommand implements SimpleCommand {
        private final ProxyServer server;
        private final Logger logger;
        private final BroadcastConfig config;
        private final AtomicInteger totalBroadcasts;

        public BroadcastCommand(ProxyServer server, Logger logger, BroadcastConfig config, AtomicInteger totalBroadcasts) {
            this.server = server;
            this.logger = logger;
            this.config = config;
            this.totalBroadcasts = totalBroadcasts;
        }

        @Override
        public void execute(Invocation invocation) {
            if (invocation.arguments().length == 0) {
                invocation.source().sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /broadcast [server1,server2,...] <message>"));
                return;
            }

            String[] args = invocation.arguments();
            String message;
            List<String> targetServerNames = new ArrayList<>();
            List<RegisteredServer> targetServers = new ArrayList<>();

            if (args[0].contains(",") || server.getServer(args[0]).isPresent()) {
                String[] serverNames = args[0].split(",");
                for (String serverName : serverNames) {
                    Optional<RegisteredServer> targetServer = server.getServer(serverName.trim());
                    if (targetServer.isPresent()) {
                        targetServers.add(targetServer.get());
                        targetServerNames.add(serverName.trim());
                    }
                }
                message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            } else {
                message = String.join(" ", args);
                if (config.shouldBroadcastToAllByDefault()) {
                    targetServers.addAll(server.getAllServers());
                    targetServerNames.addAll(server.getAllServers().stream()
                            .map(s -> s.getServerInfo().getName())
                            .collect(Collectors.toList()));
                } else {
                    invocation.source().sendMessage(MiniMessage.miniMessage().deserialize("<red>Error: No servers specified. Use: /broadcast <server1,server2,...> <message>"));
                    return;
                }
            }

            if (targetServers.isEmpty() && !config.shouldBroadcastToAllByDefault()) {
                invocation.source().sendMessage(MiniMessage.miniMessage().deserialize("<red>Error: No valid servers specified"));
                return;
            }

            String baseMessage = config.formatMessage(message);
            String scopeMessage = config.formatMessage(message, targetServerNames);

            if (config.useLegacyColors()) {
                final String legacyBaseMessage = baseMessage.replace('&', '§');
                final String legacyScopeMessage = scopeMessage.replace('&', '§');
                
                for (RegisteredServer targetServer : targetServers) {
                    targetServer.getPlayersConnected().forEach(player -> {
                        boolean showScope = !config.isScopePermissionOnly() || player.hasPermission("pandabroadcast.broadcast");
                        String finalMessage = showScope ? legacyScopeMessage : legacyBaseMessage;
                        player.sendMessage(LegacyComponentSerializer.legacySection().deserialize(finalMessage));
                    });
                }
            } else {
                for (RegisteredServer targetServer : targetServers) {
                    targetServer.getPlayersConnected().forEach(player -> {
                        boolean showScope = !config.isScopePermissionOnly() || player.hasPermission("pandabroadcast.broadcast");
                        String finalMessage = showScope ? scopeMessage : baseMessage;
                        player.sendMessage(MiniMessage.miniMessage().deserialize(finalMessage));
                    });
                }
            }

            totalBroadcasts.incrementAndGet();
            String senderName = invocation.source() instanceof Player ? 
                ((Player) invocation.source()).getUsername() : "Console";
            config.logBroadcast(senderName, message + " (Servers: " + String.join(", ", targetServerNames) + ")");

            logger.info("[Broadcast] " + message + " (Servers: " + String.join(", ", targetServerNames) + ")");
        }

        @Override
        public boolean hasPermission(Invocation invocation) {
            return invocation.source().hasPermission("pandabroadcast.broadcast");
        }
    }
}
