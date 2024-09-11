package de.unknowncity.plots;

import de.unknowncity.astralib.common.database.DataBaseProvider;
import de.unknowncity.astralib.common.database.DataBaseUpdater;
import de.unknowncity.astralib.common.message.lang.Localization;
import de.unknowncity.astralib.common.service.ServiceRegistry;
import de.unknowncity.astralib.paper.api.hook.defaulthooks.PlaceholderApiHook;
import de.unknowncity.astralib.paper.api.lib.AstraLibPaper;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.astralib.paper.api.plugin.PaperAstraPlugin;
import de.unknowncity.astralib.paper.plugin.AstraLibPaperPlugin;
import de.unknowncity.plots.configurration.PlotsConfiguration;
import de.unknowncity.plots.database.dao.MySQLPlotsDao;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

public class PlotsPlugin extends PaperAstraPlugin {
    private ServiceRegistry<PlotsPlugin> serviceRegistry;
    private PlotsConfiguration configuration;
    private AstraLibPaperPlugin astraLibPaper;
    private PaperMessenger messenger;

    @Override
    public void onPluginEnable() {
        this.astraLibPaper = AstraLibPaper.getAstraLibPlugin();
        initializeDataServices();
        initializeMessenger();
        initializeCommandManager(astraLibPaper.messenger(), astraLibPaper.languageService());

        registerServices();
        registerCommands();
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

    }

    private void initializeMessenger() {
        var defaultLang = astraLibPaper.languageService().getDefaultLanguage();

        var localization = new Localization(getDataPath().resolve("lang"));
        localization.loadLanguageFiles(getLogger());

        this.messenger = new PaperMessenger(
                localization,
                defaultLang,
                astraLibPaper.languageService(),
                hookRegistry.getRegistered(PlaceholderApiHook.class).isAvailable(getServer())
        );
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
