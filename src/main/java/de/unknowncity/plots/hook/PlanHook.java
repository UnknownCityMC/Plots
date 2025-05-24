package de.unknowncity.plots.hook;

import com.djrapitops.plan.query.QueryService;
import de.unknowncity.astralib.paper.api.hook.PaperPluginHook;
import de.unknowncity.plots.PlotsPlugin;

public class PlanHook extends PaperPluginHook {
    private QueryService queryService;
    public PlanHook(PlotsPlugin plugin) {
        super("Plan", plugin);
    }

    @Override
    public void initialize() {
        try {
            QueryService queryService = QueryService.getInstance();
        } catch (IllegalStateException planIsNotEnabled) {
            plugin.disableSelf();
        }
    }

    public QueryService queryService() {
        return queryService;
    }
}
