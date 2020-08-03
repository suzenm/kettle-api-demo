package com.shenxu.demogeneratingtransformation.Service;

import org.apache.commons.io.FileUtils;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.insertupdate.InsertUpdateMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Properties;

@Component
public class GenerateTransformationService implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        KettleEnvironment.init();

        TransMeta transMeta = runGeneratingTransformation();
        String outputFileName = "etl/demo.ktr";
        String xml = transMeta.getXML();
        File file = new File(outputFileName);
        FileUtils.writeStringToFile(file, xml, "UTF-8");

    }

    public TransMeta runGeneratingTransformation() throws KettleDatabaseException {
        TransMeta transMeta = new TransMeta();
        transMeta.setName("dome transformation");

        PluginRegistry registry = PluginRegistry.getInstance();

        DatabaseMeta databaseMeta = new DatabaseMeta();
        databaseMeta.setDatabaseInterface(DatabaseMeta.getDatabaseInterface("MySQL"));
        databaseMeta.setValues("dome", "MySQL", "Native", "localhost", "db_example", "3306", "root", "admin");
        Properties properties = new Properties();
        properties.setProperty("EXTRA_OPTION_MYSQL.serverTimezone", "UTC");
        databaseMeta.setAttributes(properties);

        transMeta.addDatabase(databaseMeta);

        TableInputMeta tableInputMeta = new TableInputMeta();
        String tableInputPluginId = registry.getPluginId(StepPluginType.class, tableInputMeta);
        tableInputMeta.setDatabaseMeta(databaseMeta);
        tableInputMeta.setSQL("select * from user");

        StepMeta tableInputStepMeta = new StepMeta(tableInputPluginId, "table input", tableInputMeta);
        tableInputStepMeta.setDraw(true);
        tableInputStepMeta.setLocation(100, 100);

        transMeta.addStep(tableInputStepMeta);

        InsertUpdateMeta insertUpdateMeta = new InsertUpdateMeta();
        String updatePluginId = registry.getPluginId(StepPluginType.class, insertUpdateMeta);
        insertUpdateMeta.setDatabaseMeta(databaseMeta);
        insertUpdateMeta.setKeyStream(new String[] {"id", "name", "email"});//查询流字段1
        insertUpdateMeta.setKeyStream2(new String[] {"", "", ""});//查询流字段2
        insertUpdateMeta.setKeyLookup(new String[] {"id", "name", "email"});//查询表字段
        insertUpdateMeta.setKeyCondition(new String[] {"=", "=", "="});//查询比较符
        insertUpdateMeta.setUpdateStream(new String[] {"id", "name", "email"});//更新流字段
        insertUpdateMeta.setUpdateLookup(new String[] {"id", "name", "email"});//更新表字段
        insertUpdateMeta.setUpdate(new Boolean[] {true, true, true});//是否更新
        insertUpdateMeta.setTableName("test");

        StepMeta updateStepMeta = new StepMeta(updatePluginId, "update table", insertUpdateMeta);
        updateStepMeta.setDraw(true);
        updateStepMeta.setLocation(300, 100);

        transMeta.addStep(updateStepMeta);

        transMeta.addTransHop(new TransHopMeta(tableInputStepMeta, updateStepMeta));

        return transMeta;
    }
}
