package com.shenxu.demodynamicjobmultidatabase.Database;

public class MySQLDatabase extends Database {
    private String serverTimezone = "UTC";

    public String getServerTimezone() {
        return serverTimezone;
    }

    public void setServerTimezone(String serverTimezone) {
        this.serverTimezone = serverTimezone;
    }
}
