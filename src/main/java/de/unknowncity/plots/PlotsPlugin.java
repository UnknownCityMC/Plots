package de.unknowncity.plots;

import de.unknowncity.astralib.common.configuration.YamlAstraConfiguration;
import de.unknowncity.astralib.common.database.StandardDataBaseProvider;
import de.unknowncity.astralib.common.message.lang.Localization;
import de.unknowncity.astralib.common.service.ServiceRegistry;
import de.unknowncity.astralib.paper.api.hook.defaulthooks.PlaceholderApiHook;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.astralib.paper.api.plugin.PaperAstraPlugin;
import de.unknowncity.plots.command.admin.PlotAdminCommand;
import de.unknowncity.plots.command.user.PlotCommand;
import de.unknowncity.plots.configurration.PlotsConfiguration;
import de.unknowncity.plots.data.dao.mariadb.*;
import de.unknowncity.plots.data.repository.PlotGroupRepository;
import de.unknowncity.plots.service.EconomyService;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.command.CommandSender;
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

    @Override
    public void onPluginEnable() {
        onPluginReload();
        initializeDataServices();

        registerCommands();


        var pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlotInteractListener(this), this);


    }

    public void onPluginReload() {
        try {
            Files.createDirectories(getDataPath().resolve("schematics"));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, e.getMessage());
        }
        initConfiguration();
        initializeMessenger();
    }

    @Override
    public void onPluginDisable() {

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
        this.serviceRegistry.register(new PlotService(
                new PlotGroupRepository(
                        new MariaDBGroupDao(queryConfig),
                        new MariaDBPlotDao(queryConfig),
                        new MariaDBPlotFlagDao(queryConfig),
                        new MariaDBPlotInteractablesDao(queryConfig),
                        new MariaDBPlotLocationDao(queryConfig),
                        new MariaDBPlotMemberDao(queryConfig)
                ), economyService, this));

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
}