package me.playground.data;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LoggingManager<O> {

    protected final PrivateLogger<O> logger; // Logger
    protected final PrivateDatasource datasource; // Generic PrivateDatasource reference
    protected final LoggingDatasource<O> logging; // Direct Logging reference

    public LoggingManager(@NotNull LoggingDatasource<O> datasource) {
        if (!(datasource instanceof PrivateDatasource)) throw new UnsupportedOperationException("All implementations of LoggingDatasource must be an instanceof PrivateDatasource.");
        this.datasource = (PrivateDatasource) datasource;
        this.logging = datasource;
        this.logger = new PrivateLogger<>(this);

        this.datasource.registerLogger(logger);
    }

    protected boolean saveLogs(@NotNull List<O> logs) {
        if (logs.isEmpty()) return true;
        return logging.saveLogs(logs);
    }

    public void addLog(@NotNull O log) {
        logger.addLog(log);
    }

    public int countPendingChanges() {
        return logger.countPendingChanges();
    }

}
