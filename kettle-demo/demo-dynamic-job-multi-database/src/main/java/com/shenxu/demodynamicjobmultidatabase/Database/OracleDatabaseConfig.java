package com.shenxu.demodynamicjobmultidatabase.Database;

public class OracleDatabaseConfig extends DatabaseConfig{
    public OracleDatabaseConfig() {
    }

    public OracleDatabaseConfig(String name, String access, String host, String database, String user, String password, String port, String type) {
        super(name, access, host, database, user, password, port, type);
    }
}
