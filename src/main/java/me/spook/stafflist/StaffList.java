package me.spook.stafflist;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.spook.stafflist.cmd.StaffOnlineCommand;
import me.spook.stafflist.config.ConfigReader;
import me.spook.stafflist.config.defaults.DefaultConfig;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(id = "stafflist", name = "StaffList", version = BuildConstants.VERSION, authors = {"spook"})
public class StaffList {

    // ------------------------------------------------- //
    // FIELDS
    // ------------------------------------------------- //

    public ProxyServer server;
    public final Logger logger;
    private final Path dataDirectory;

    private ConfigReader<DefaultConfig> config;

    /**
     * Creates a new staff list plugin instance
     *
     * @param proxyServer   The proxy server
     * @param logger        The logger
     * @param dataDirectory The data directory
     */

    @Inject
    public StaffList(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }


    /**
     * Emitted when the proxy/plugin is initialized
     *
     * @param event The event
     */

    @SuppressWarnings("all")
    @Subscribe(order = PostOrder.LAST)
    public void onProxyInitialization(ProxyInitializeEvent event) {
        try {
            Files.createDirectories(dataDirectory);
            final Path configPath = dataDirectory.resolve("stafflist.toml");

            this.config = new ConfigReader<>(DefaultConfig.class, configPath);
        } catch (Exception e) {
            logger.error("Failed to create data directory. Please check your permissions.", e);
        }

        new StaffOnlineCommand(server, this, logger);
    }

    /**
     * Gets the default config
     *
     * @return The config container
     */

    public ConfigReader<DefaultConfig> getConfig() {
        return config;
    }

}
