package de.unknowncity.plots;

import de.unknowncity.astralib.common.configuration.YamlAstraConfiguration;
import de.unknowncity.astralib.common.database.StandardDataBaseProvider;
import de.unknowncity.astralib.common.message.lang.Localization;
import de.unknowncity.astralib.common.service.ServiceRegistry;
import de.unknowncity.astralib.paper.api.hook.defaulthooks.PlaceholderApiHook;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.astralib.paper.api.plugin.PaperAstraPlugin;
import de.unknowncity.plots.command.PlotAdminCommand;
import de.unknowncity.plots.command.PlotAdminGroupCommand;
import de.unknowncity.plots.configurration.PlotsConfiguration;
import de.unknowncity.plots.data.dao.mariadb.*;
import de.unknowncity.plots.data.repository.PlotGroupRepository;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.processors.cache.SimpleCache;
import org.incendo.cloud.processors.confirmation.ConfirmationConfiguration;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;
import org.spongepowered.configurate.NodePath;

public class PlotsPlugin extends PaperAstraPlugin {
    private ServiceRegistry<PlotsPlugin> serviceRegistry;
    private PlotsConfiguration configuration;
    private PaperMessenger messenger;
    private ConfirmationManager<CommandSender> confirmationManager;

    @Override
    public void onPluginEnable() {
        initConfiguration();
        initializeDataServices();

        registerServices();
        registerCommands();
        initializeMessenger();
    }

    @Override
    public void onPluginDisable() {

    }

    private void registerServices() {
        this.serviceRegistry = new ServiceRegistry<>(this);

        this.serviceRegistry.register(new RegionService());
        this.serviceRegistry.register(new PlotService(new PlotGroupRepository(
                new MariaDBGroupDao(),
                new MariaDBPlotDao(),
                new MariaDBPlotFlagDao(),
                new MariaDBPlotLocationDao(),
                new MariaDBPlotMemberDao()
        )));
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

        new PlotAdminCommand(this).apply(commandManager);
        new PlotAdminGroupCommand(this).apply(commandManager);
    }

    public void initConfiguration() {
        var configOpt = YamlAstraConfiguration.loadFromFile(PlotsConfiguration.class);

        this.configuration = configOpt.orElseGet(PlotsConfiguration::new);
        this.configuration.save();
    }

    private void initializeMessenger() {
        var defaultLang = languageService.getDefaultLanguage();

        var localization = Localization.builder(getDataPath().resolve("lang")).buildAndLoad();

        this.messenger = PaperMessenger.builder(localization, getPluginMeta())
                .withDefaultLanguage(defaultLang)
                .withLanguageService(languageService)
                .withPlaceHolderAPI(hookRegistry.getRegistered(PlaceholderApiHook.class))
                .build();
    }

    private void initializeDataServices() {
        var databaseSetting = configuration.database();

        StandardDataBaseProvider.updateAndConnectToDataBase(databaseSetting, getClassLoader(), getDataPath());
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