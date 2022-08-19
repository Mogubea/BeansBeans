package me.playground.punishments;

import me.playground.data.PrivateDatasource;
import me.playground.main.Main;

public class PunishmentDatasource extends PrivateDatasource {

    private final PunishmentManager manager;

    protected PunishmentDatasource(PunishmentManager manager, Main plugin) {
        super(plugin);
        this.manager = manager;
    }

    @Override
    public void loadAll() {

    }

    @Override
    public void saveAll() throws Exception {

    }
}
