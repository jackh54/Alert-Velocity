package org.PandaScript.alert;

import com.google.inject.Inject;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.util.stream.Collectors;

@Plugin(id = "alert", name = "Alert", version = "1.0")
public class Alert {

    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public Alert(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getCommandManager().register("vbroadcast", new BroadcastCommand(server, logger));
        logger.info("Alert plugin loaded!");
    }

    public static class BroadcastCommand implements SimpleCommand {

        private final ProxyServer server;
        private final Logger logger;

        public BroadcastCommand(ProxyServer server, Logger logger) {
            this.server = server;
            this.logger = logger;
        }

        @Override
        public void execute(Invocation invocation) {
            if (invocation.arguments().length == 0) {
                invocation.source().sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /vbroadcast <message>"));
                return;
            }

            String message = String.join(" ", invocation.arguments());

            String formattedMessage = "<gold>» <bold><yellow>Broadcast</yellow></bold> <gold>«</gold> <gray>" + message;
            server.getAllPlayers().forEach(player -> player.sendMessage(MiniMessage.miniMessage().deserialize(formattedMessage)));

            logger.info("[Broadcast] " + message);
        }

        @Override
        public boolean hasPermission(Invocation invocation) {
            return invocation.source().hasPermission("alert.broadcast");
        }
    }
}
