package de.unknowncity.plots;

import de.unknowncity.astralib.common.configuration.YamlAstraConfiguration;
import de.unknowncity.astralib.common.database.StandardDataBaseProvider;
import de.unknowncity.astralib.common.message.lang.Localization;
import de.unknowncity.astralib.common.service.ServiceRegistry;
import de.unknowncity.astralib.paper.api.hook.defaulthooks.PlaceholderApiHook;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.astralib.paper.api.plugin.PaperAstraPlugin;
import de.unknowncity.plots.command.admin.PlotAdminCommand;
import de.unknowncity.plots.command.mod.PlotModCommand;
import de.unknowncity.plots.command.user.PlotCommand;
import de.unknowncity.plots.configurration.PlotsConfiguration;
import de.unknowncity.plots.data.dao.mariadb.*;

import de.unknowncity.plots.data.model.plot.PlotLocations;
import de.unknowncity.plots.data.repository.PlotGroupRepository;
import de.unknowncity.plots.listener.PlotCreateListener;
import de.unknowncity.plots.listener.PlotInteractListener;
import de.unknowncity.plots.listener.PlotSignLinkListener;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.plot.flag.FlagRegistry;
import de.unknowncity.plots.plot.flag.type.block.IceMeltFlag;
import de.unknowncity.plots.service.EconomyService;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import de.unknowncity.plots.task.RentTask;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.processors.cache.SimpleCache;
import org.incendo.cloud.processors.confirmation.ConfirmationConfiguration;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;
import org.spongepowered.configurate.NodePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class PlotsPlugin extends PaperAstraPlugin {
    private ServiceRegistry<PlotsPlugin> serviceRegistry;
    private PlotsConfiguration configuration;
    private PaperMessenger messenger;
    private ConfirmationManager<CommandSender> confirmationManager;
    private RentTask rentTask;
    public HashMap<UUID, Plot> signLinkPlayers = new HashMap<>();
    public HashMap<UUID, PlotLocations> createPlotPlayers = new HashMap<>();

    @Override
    public void onPluginEnable() {
        onPluginReload();
        initializeDataServices();

        registerCommands();

        var pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlotInteractListener(this), this);
        pluginManager.registerEvents(new PlotSignLinkListener(this), this);
        pluginManager.registerEvents(new PlotCreateListener(this), this);


        rentTask = new RentTask(this, serviceRegistry.getRegistered(PlotService.class), serviceRegistry.getRegistered(EconomyService.class));
        rentTask.start();
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
        rentTask.cancel();
    }

    public void registerCommands() {
        this.confirmationManager = ConfirmationManager.confirmationManager(
                ConfirmationConfiguration.<CommandSender>builder()
                        .cache(SimpleCache.of())
                        .noPendingCommandNotifier(sender -> messenger.sendMessage(
                                sender,
                                NodePath.path("command", "confirm", "no-pending"))
                        )
                        .confirmationRequiredNotifier((sender, paperCommandSourceConfirmationContext) -> {
                            var command = paperCommandSourceConfirmationContext.command().rootComponent().name();
                            messenger.sendMessage(
                                    sender,
                                    NodePath.path("command", command, "confirm", "notification")
                            );
                        })
                        .build()

        );

        this.commandManager.registerCommandPostProcessor(
                confirmationManager.createPostprocessor()
        );


        new PlotCommand(this).apply(commandManager);
        new PlotModCommand(this).apply(commandManager);
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
        var economyService = new EconomyService(configuration.economy());
        this.serviceRegistry.register(economyService);

        var databaseSetting = configuration.database();

        var queryConfig = StandardDataBaseProvider.updateAndConnectToDataBase(databaseSetting, getClassLoader(), getDataPath());

        this.serviceRegistry.register(new PlotService(queryConfig, economyService, this));


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
}