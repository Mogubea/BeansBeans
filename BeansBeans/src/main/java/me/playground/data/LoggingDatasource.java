package me.playground.data;

import java.util.List;

public interface LoggingDatasource<O> {

    boolean saveLogs(List<O> logs);

}
