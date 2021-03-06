package com.shenxu.demodynamicjobmultidatabase.Database;

public abstract class DatabaseConfig {
    private String name;
    private String access;
    private String host;
    private String database;
    private String user;
    private String password;
    private String port;
    private String type;

    public DatabaseConfig() {
    }

    public DatabaseConfig(String name, String access, String host, String database, String user, String password, String port, String type) {
        this.name = name;
        this.access = access;
        this.host = host;
        this.database = database;
        this.user = user;
        this.password = password;
        this.port = port;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
