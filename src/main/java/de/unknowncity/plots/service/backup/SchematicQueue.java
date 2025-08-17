package de.unknowncity.plots.service.backup;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import de.unknowncity.astralib.common.structure.KeyValue;
import de.unknowncity.plots.plot.SchematicManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class SchematicQueue {

    private final JavaPlugin plugin;
    private final Queue<KeyValue<EditSession, Operation>> operations = new LinkedList<>();

    public SchematicQueue(JavaPlugin plugin, SchematicManager schematicManager) {
        this.plugin = plugin;
    }

    public void addSchematic(KeyValue<EditSession, Operation> operation) {
        operations.add(operation);
    }

    public CompletableFuture<Void> startPasting(int perTick, Runnable afterPaste) {
        var future = new CompletableFuture<Void>();

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            for (int i = 0; i < perTick && !operations.isEmpty(); i++) {
                var operation = operations.poll();
                try {
                    Operations.complete(operation.value());
                    operation.key().close();
                    afterPaste.run();
                } catch (WorldEditException e) {
                    plugin.getLogger().severe(e.getMessage());
                    future.completeExceptionally(e);
                    task.cancel();
                    return;
                }
            }

            if (operations.isEmpty()) {
                task.cancel();
                future.complete(null); // hier ist das CompletableFuture fertig
            }
        }, 10L, 1L);

        return future;
    }
}
