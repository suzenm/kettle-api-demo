package com.shenxu.demodatebase.service;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.Properties;

@Component
public class DataBaseDemo implements CommandLineRunner {
    @Value("${connection.name}")
    private String name;

    @Value("${database.host}")
    private String hostName;

    @Value("${database.DBName}")
    private String DBName;

    @Value("${database.DBPort}")
    private String DBPort;

    @Value("${database.user}")
    private String user;

    @Value("${database.password}")
    private String password;

    @Value("${database.serverTimezone}")
    private String serverTimezone;


    @Override
    public void run(String... args) throws Exception {
        try {
            KettleEnvironment.init();
            DatabaseMeta dbm = new DatabaseMeta();

            dbm.setDatabaseInterface(DatabaseMeta.getDatabaseInterface("MySQL"));
            dbm.setValues(name, "MySQL", "Native", hostName, DBName, DBPort, user, password);
            Properties additionOpt = new Properties();
            additionOpt.put("EXTRA_OPTION_MYSQL.serverTimezone", serverTimezone);
            dbm.setAttributes(additionOpt);

            Database db = new Database(null, dbm);
            db.connect();
            ResultSet rs = db.openQuery("select * from test");

            if (rs != null){
                while (rs.next()){
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String email = rs.getString("email");

                    System.out.println(id + name + email);
                }
                db.closeQuery(rs);
            }

            db.disconnect();

        } catch (KettleException e) {
            e.printStackTrace();
        }
    }
}
