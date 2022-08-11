package me.playground.items.values;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Have this attempt to save logs much more frequently? Might not be too necessary since a force save can be performed with /save-all.
 */
public class ItemValueLogger {

    private final List<ItemValueLog> pendingLogs = new ArrayList<>();
    private final ItemValueManager manager;

    protected ItemValueLogger(ItemValueManager manager) {
        this.manager = manager;
    }

    protected void saveLogs() {
        List<ItemValueLog> logsToSave = List.copyOf(pendingLogs);
        if (manager.getDatasource().logItemValueChanges(logsToSave))
            pendingLogs.removeAll(logsToSave);
    }

    protected void addLog(ItemValueLog log) {
        this.pendingLogs.add(log);
    }

}
