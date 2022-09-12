package me.playground.data;

import java.util.ArrayList;
import java.util.List;

public class PrivateLogger<O> {

    private final List<O> pendingLogs = new ArrayList<>();
    private final LoggingManager<O> manager;

    public PrivateLogger(LoggingManager<O> manager) {
        this.manager = manager;
    }

    public void addLog(O log) {
        this.pendingLogs.add(log);
    }

    protected void saveLogs() {
        List<O> logsToSave = List.copyOf(pendingLogs);
        if (manager.saveLogs(logsToSave))
            pendingLogs.removeAll(logsToSave);
    }

    public int countPendingChanges() {
        return pendingLogs.size();
    }

}
