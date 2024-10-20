package me.spook.stafflist.cmd;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.spook.stafflist.StaffList;
import me.spook.stafflist.config.defaults.DefaultConfig;
import me.spook.stafflist.obj.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

@SuppressWarnings("all")
public record StaffOnlineCommand(@Nonnull ProxyServer proxyServer, @Nonnull StaffList plugin,
                                 @Nonnull Logger logger) implements SimpleCommand {

    // ------------------------------------------------- //
    // FIELDS
    // ------------------------------------------------- //

    private static DefaultConfig config;

    /**
     * Staff Online Command
     *
     * @param proxyServer The proxy server instance
     * @param plugin      The plugin
     * @param logger      The logger
     */

    public StaffOnlineCommand(ProxyServer proxyServer, StaffList plugin, Logger logger) {
        this.proxyServer = proxyServer;
        this.plugin = plugin;
        this.logger = logger;

        this.config = plugin.getConfig().instance().orNull();

        Preconditions.checkNotNull(config, "Config is null");

        final CommandManager commandManager = proxyServer.getCommandManager();
        commandManager.register(
                commandManager
                        .metaBuilder("staffonline")
                        .aliases(plugin.getConfig().instance()
                                .get()
                                .staff_online_aliases.toArray(new String[0]))
                        .build(),
                this
        );
    }

    /**
     * Execute Method
     *
     * @param invocation - Command Source
     */

    @Override
    public void execute(Invocation invocation) {
        final CommandSource source = invocation.source();
        final Pair<Component, Integer> pair = getEntries();

        final MiniMessage miniMessage = MiniMessage.miniMessage();
        final Component component = miniMessage.deserialize(config.staff_online_header)
                .append(Component.text("\n"))
                .append(pair.left)
                .append(Component.text("\n"))
                .append(miniMessage.deserialize(config.staff_online_footer.replace("{count}", String.valueOf(pair.right))));

        source.sendMessage(component);
    }


    /**
     * Gets Entries Separate
     *
     * @return Component and amount of online staff players
     */

    private Pair<Component, Integer> getEntries() {
        Component component = Component.empty();
        int staffCount = 0;
        MiniMessage miniMessage = MiniMessage.miniMessage();

        for (RegisteredServer server : proxyServer.getAllServers()) {
            final String serverName = server.getServerInfo().getName();
            final List<String> onlinePlayers = getOnlinePlayers(serverName, player -> player.hasPermission(config.staff_permission));

            if (!onlinePlayers.isEmpty()) {
                staffCount += onlinePlayers.size();
                component = component.append(miniMessage.deserialize(config.staff_online_entry
                                .replace("{server_name}", serverName)
                                .replace("{entries}", String.join(", ", onlinePlayers))))
                        .append(Component.text("\n"));
            }
        }

        return new Pair<>(component, staffCount);
    }


    /**
     * Get prettified online players that match a predicate
     *
     * @param server    Server Name
     * @param predicate Predicate
     * @return List of online players by username
     */

    private List<String> getOnlinePlayers(@Nonnull String server, @Nonnull Predicate<Player> predicate) {
        final List<String> onlinePlayers = proxyServer
                .getServer(server)
                .map(s -> s.getPlayersConnected())
                .map(p -> p.stream().filter(f -> predicate.test(f)).map(Player::getUsername).toList())
                .orElse(List.of());


        return onlinePlayers;
    }
}
