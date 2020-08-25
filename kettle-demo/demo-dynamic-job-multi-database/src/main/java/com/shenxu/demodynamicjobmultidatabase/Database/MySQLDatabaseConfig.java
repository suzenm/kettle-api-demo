package com.shenxu.demodynamicjobmultidatabase.Database;

public class MySQLDatabaseConfig extends DatabaseConfig {
    private String serverTimezone = "UTC";

    public MySQLDatabaseConfig() {
    }

    public MySQLDatabaseConfig(String name, String access, String host, String database, String user, String password, String port, String type, String serverTimezone) {
        super(name, access, host, database, user, password, port, type);
        this.serverTimezone = serverTimezone;
    }

    public String getServerTimezone() {
        return serverTimezone;
    }

    public void setServerTimezone(String serverTimezone) {
        this.serverTimezone = serverTimezone;
    }
}
