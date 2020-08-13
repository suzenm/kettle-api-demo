package com.shenxu.demodynamictransformation.Service;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

@Component
public class DynamicTransformationService implements CommandLineRunner {

    private static final String STEP_TABLE_INPUT = "table input";
    private static final String STEP_TABLE_OUTPUT = "table output";

    @Override
    public void run(String... args) throws Exception {
        KettleEnvironment.init();
        TransMeta transMeta = GeneratingTransformation();
        Trans trans = new Trans(transMeta);
        trans.execute(null);
        if (trans.getErrors() != 0) {
            System.out.println("Error");
        } else {
            System.out.println("Success");
        }
    }

    public TransMeta GeneratingTransformation() throws KettleDatabaseException {
        TransMeta transMeta = new TransMeta();
        transMeta.setName("demo_dynamic");

        DatabaseMeta sourceDatabaseMeta = new DatabaseMeta();
        sourceDatabaseMeta.setDatabaseInterface(DatabaseMeta.getDatabaseInterface("MySql"));
        sourceDatabaseMeta.setValues("source", "MySQL", "Native", "localhost", "sakila", "3306", "root", "admin");
        Properties databaseProperties = new Properties();
        databaseProperties.setProperty("EXTRA_OPTION_MYSQL.serverTimezone", "UTC");
        sourceDatabaseMeta.setAttributes(databaseProperties);

        DatabaseMeta targetDatabaseMeta = new DatabaseMeta();
        targetDatabaseMeta.setDatabaseInterface(DatabaseMeta.getDatabaseInterface("MySql"));
        targetDatabaseMeta.setValues("target", "MySQL", "Native", "localhost", "test", "3306", "root", "admin");
        targetDatabaseMeta.setAttributes(databaseProperties);

        TableInputMeta tableInputMeta = new TableInputMeta();
        tableInputMeta.setDefault();
        tableInputMeta.setDatabaseMeta(sourceDatabaseMeta);
        tableInputMeta.setSQL("select * from actor");
        StepMeta tableInputStep = new StepMeta(STEP_TABLE_INPUT, tableInputMeta);
        transMeta.addStep(tableInputStep);

        TableOutputMeta tableOutputMeta = new TableOutputMeta();
        tableOutputMeta.setDefault();
        tableOutputMeta.setDatabaseMeta(targetDatabaseMeta);
        tableOutputMeta.setTableName("actor");
        StepMeta tableOutputStep = new StepMeta(STEP_TABLE_OUTPUT, tableOutputMeta);
        transMeta.addStep(tableOutputStep);

        transMeta.addTransHop(new TransHopMeta(tableInputStep, tableOutputStep));
        return transMeta;
    }

    public String[] describeTable(Database database, String table) throws KettleDatabaseException, SQLException {
        ArrayList<String> tableField = new ArrayList<>();
        database.connect();
        ResultSet resultSet = database.openQuery("describe " + table);
        if (resultSet != null) {
            while (resultSet.next()) {
                tableField.add(resultSet.getString(1));
            }
            database.closeQuery(resultSet);
        }
        database.disconnect();
        return tableField.toArray(new String[0]);
    }

    public ArrayList<String> describeDatabase(Database database) throws KettleDatabaseException, SQLException {
        ArrayList<String> tableName = new ArrayList<>();
        database.connect();
        ResultSet resultSet = database.openQuery("show tables");
        if (resultSet != null) {
            while (resultSet.next()) {
//                System.out.println(resultSet.getString(1));
                tableName.add(resultSet.getString(1));
            }
            database.closeQuery(resultSet);
        }
        database.disconnect();
        return tableName;
    }
}
