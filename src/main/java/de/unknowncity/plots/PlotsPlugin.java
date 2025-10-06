package de.unknowncity.plots;

import com.sk89q.worldguard.WorldGuard;
import de.unknowncity.astralib.common.configuration.YamlAstraConfiguration;
import de.unknowncity.astralib.common.database.StandardDataBaseProvider;
import de.unknowncity.astralib.common.message.lang.Localization;
import de.unknowncity.astralib.common.registry.registrable.ClosableRegistrable;
import de.unknowncity.astralib.common.service.ServiceRegistry;
import de.unknowncity.astralib.paper.api.hook.defaulthooks.PlaceholderApiHook;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.astralib.paper.api.plugin.PaperAstraPlugin;
import de.unknowncity.plots.command.admin.PlotAdminCommand;
import de.unknowncity.plots.command.land.LandCommand;
import de.unknowncity.plots.command.user.PlotCommand;
import de.unknowncity.plots.configuration.PlotsConfiguration;
import de.unknowncity.plots.listener.*;
import de.unknowncity.plots.plot.freebuild.LandEditSessionHandler;
import de.unknowncity.plots.service.EconomyService;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import de.unknowncity.plots.task.RentService;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionProvider;
import org.incendo.cloud.processors.cache.SimpleCache;
import org.incendo.cloud.processors.confirmation.ConfirmationConfiguration;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;
import org.spongepowered.configurate.NodePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public class PlotsPlugin extends PaperAstraPlugin {
    private ServiceRegistry<PlotsPlugin> serviceRegistry;
    private PlotsConfiguration configuration;
    private PaperMessenger messenger;
    private ConfirmationManager<CommandSender> confirmationManager;
    private LandEditSessionHandler landEditSessionHandler;

    @Override
    public void onPluginEnable() {
        onPluginReload();
        initializeDataServices();

        registerCommands();

        Permissions.ALL_PERMISSIONS.forEach(permission -> {
            Bukkit.getPluginManager().addPermission(new Permission(permission));
        });

        var pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlotInteractListener(this), this);
        pluginManager.registerEvents(new PlotSignLinkListener(this), this);
        pluginManager.registerEvents(new LandEditListener(this), this);
        pluginManager.registerEvents(new PlotSignInteractListener(this), this);
        pluginManager.registerEvents(new PlayerJoinListener(this), this);

        var sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        sessionManager.registerHandler(new PlotEntrySessionHandler.Factory(
                serviceRegistry.getRegistered(PlotService.class),
                messenger
        ), null);

        commandManager.captionRegistry().registerProvider(CaptionProvider.forCaption(Caption.of("argument.parse.failure.player"),
                sender -> messenger.getStringOrNotAvailable((Player) sender, NodePath.path("exception", "argument-parse", "player"))));

        landEditSessionHandler = new LandEditSessionHandler(this);
    }

    public void onPluginReload() {
        try {
            Files.createDirectories(getDataPath().resolve("schematics"));
            Files.createDirectories(getDataPath().resolve("schematics/backups"));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, e.getMessage());
        }
        initConfiguration();
        initializeMessenger();
    }

    @Override
    public void onPluginDisable() {
        serviceRegistry.getAllRegistered().forEach(ClosableRegistrable::shutdown);
    }

    public void registerCommands() {
        this.confirmationManager = ConfirmationManager.confirmationManager(
                ConfirmationConfiguration.<CommandSender>builder()
                        .cache(SimpleCache.of())
                        .noPendingCommandNotifier(sender -> messenger.sendMessage(
                                sender,
                                NodePath.path("command", "confirm", "no-pending"))
                        )
                        .confirmationRequiredNotifier((sender, context) -> {
                            var command = context.command().rootComponent().name();
                            messenger.sendMessage(
                                    sender,
                                    NodePath.path("command", "confirm", "notification"),
                                    Placeholder.unparsed("command", command)
                            );
                        })
                        .build()

        );

        this.commandManager.registerCommandPostProcessor(
                confirmationManager.createPostprocessor()
        );


        new PlotCommand(this).apply(commandManager);
        new LandCommand(this).apply(commandManager);
        new PlotAdminCommand(this).apply(commandManager);
    }

    public void initConfiguration() {
        var configOpt = YamlAstraConfiguration.loadFromFile(PlotsConfiguration.class);

        this.configuration = configOpt.orElseGet(PlotsConfiguration::new);
        this.configuration.save();
    }

    private void initializeMessenger() {
        var defaultLang = languageService.getDefaultLanguage();

        saveDefaultResource("lang/de_DE.yml", Path.of("lang", "de_DE.yml"));
        saveDefaultResource("lang/en_US.yml", Path.of("lang", "en_US.yml"));

        var localization = Localization.builder(getDataPath().resolve("lang")).buildAndLoad();

        this.messenger = PaperMessenger.builder(localization, getPluginMeta())
                .withDefaultLanguage(defaultLang)
                .withLanguageService(languageService)
                .withPlaceHolderAPI(hookRegistry.getRegistered(PlaceholderApiHook.class))
                .build();
    }

    private void initializeDataServices() {
        this.serviceRegistry = new ServiceRegistry<>(this);

        this.serviceRegistry.register(new RegionService());
        this.serviceRegistry.register(new EconomyService(configuration.economy()));

        var databaseSetting = configuration.database();

        var queryConfig = StandardDataBaseProvider.updateAndConnectToDataBase(databaseSetting, getClassLoader(), getDataPath());

        this.serviceRegistry.register(new PlotService(queryConfig, this));
        this.serviceRegistry.register(new RentService(this));

        this.serviceRegistry().getRegistered(PlotService.class).cacheAll();
    }

    public ServiceRegistry<PlotsPlugin> serviceRegistry() {
        return serviceRegistry;
    }

    public PaperMessenger messenger() {
        return messenger;
    }

    public ConfirmationManager<CommandSender> confirmationManager() {
        return confirmationManager;
    }

    public PlotsConfiguration configuration() {
        return configuration;
    }

    public LandEditSessionHandler landEditSessionHandler() {
        return landEditSessionHandler;
    }

}