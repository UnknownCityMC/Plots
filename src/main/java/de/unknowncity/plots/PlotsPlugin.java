package de.unknowncity.plots;

import de.unknowncity.astralib.common.configuration.setting.defaults.DataBaseSetting;
import de.unknowncity.astralib.common.configuration.setting.serializer.DatabaseSettingSerializer;
import de.unknowncity.astralib.common.database.DataBaseProvider;
import de.unknowncity.astralib.common.database.DataBaseUpdater;
import de.unknowncity.astralib.common.message.lang.Localization;
import de.unknowncity.astralib.common.service.ServiceRegistry;
import de.unknowncity.astralib.paper.api.hook.defaulthooks.PlaceholderApiHook;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.astralib.paper.api.plugin.PaperAstraPlugin;
import de.unknowncity.plots.command.PlotAdminCommand;
import de.unknowncity.plots.configurration.PlotsConfiguration;
import de.unknowncity.plots.configurration.serializer.PlotsConfigSerializer;
import de.unknowncity.plots.database.dao.MySQLPlotsDao;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

public class PlotsPlugin extends PaperAstraPlugin {
    private ServiceRegistry<PlotsPlugin> serviceRegistry;
    private PlotsConfiguration configuration;
    private PaperMessenger messenger;

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
        this.serviceRegistry.register(new PlotService(new MySQLPlotsDao()));
    }

    public void registerCommands() {
        new PlotAdminCommand(this).apply(commandManager);
    }

    public void initConfiguration() {
        this.configuration = new PlotsConfiguration(
                new DataBaseSetting()
        );

        configLoader.saveDefaultConfig(configuration, getDataFolder().toPath().resolve("config.yml"), builder -> {
            builder.register(PlotsConfiguration.class, new PlotsConfigSerializer());
            builder.register(DataBaseSetting.class, new DatabaseSettingSerializer());
        });

        configLoader.loadConfiguration(getDataFolder().toPath().resolve("config.yml"), PlotsConfiguration.class, builder -> {
            builder.register(PlotsConfiguration.class, new PlotsConfigSerializer());
            builder.register(DataBaseSetting.class, new DatabaseSettingSerializer());
        }).ifPresent(plotsConfiguration -> this.configuration = plotsConfiguration);
    }

    private void initializeMessenger() {
        var defaultLang = languageService.getDefaultLanguage();

        var localization = Localization.builder(getDataPath().resolve("lang")).buildAndLoad();

        this.messenger = PaperMessenger.builder(localization)
                .withDefaultLanguage(defaultLang)
                .withLanguageService(languageService)
                .withPlaceHolderAPI(hookRegistry.getRegistered(PlaceholderApiHook.class))
                .build();
    }

    private void initializeDataServices() {
        var databaseSettings = configuration.dataBaseSetting();

        var dataBaseProvider = new DataBaseProvider(databaseSettings);
        var dataSource = dataBaseProvider.createDataSource();
        dataBaseProvider.setup(dataSource, getLogger());

        var dataBaseUpdater = new DataBaseUpdater(dataSource, databaseSettings);
        try {
            dataBaseUpdater.update(getClassLoader());
        } catch (IOException | SQLException e) {
            this.getLogger().log(Level.SEVERE, "Failed to update database", e);
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    public ServiceRegistry<PlotsPlugin> serviceRegistry() {
        return serviceRegistry;
    }

    public PaperMessenger messenger() {
        return messenger;
    }
}
