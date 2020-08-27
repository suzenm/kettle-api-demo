package com.shenxu.demodynamicjobmultidatabase.Service.Impl;

import com.shenxu.demodynamicjobmultidatabase.Database.DatabaseConfig;
import com.shenxu.demodynamicjobmultidatabase.Database.MySQLDatabaseConfig;
import com.shenxu.demodynamicjobmultidatabase.Database.OracleDatabaseConfig;
import com.shenxu.demodynamicjobmultidatabase.Service.DynamicJobService;
import org.apache.commons.io.FileUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.DatabaseMetaInformation;
import org.pentaho.di.core.database.Schema;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.sql.JobEntrySQL;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.ui.core.database.dialog.GetDatabaseInfoProgressDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DynamicJobServiceImpl implements DynamicJobService {

    private DatabaseConfig source;
    private DatabaseConfig target;
    private String schemaName;

    public DynamicJobServiceImpl() {
    }

    public DynamicJobServiceImpl(DatabaseConfig source, DatabaseConfig target) {
        this.source = source;
        this.target = target;
    }

    public void jobService() throws IOException, KettleException {
        JobMeta jobMeta = generateJob("copy_multi_database");
        boolean ok = saveFile("etl/", jobMeta.getFilename(), Const.STRING_JOB_DEFAULT_EXT, jobMeta);
        System.out.println(ok);
        JobMeta fileJobMeta = new JobMeta("etl/"+ jobMeta.getFilename() +".kjb", null);
        Result rs = executedJob(fileJobMeta);
    }

    @Override
    public Boolean saveFile(String dir, String filename, String extension, Object file) throws IOException, KettleException {
        if (filename == null || extension == null || file == null) {
            return false;
        }
        if (extension.equals(Const.STRING_TRANS_DEFAULT_EXT) && file instanceof TransMeta) {
            TransMeta transMeta = (TransMeta)file;
            String outputFilename = dir + filename + "." + Const.STRING_TRANS_DEFAULT_EXT;
            String xml = transMeta.getXML();
            File outputFile = new File(outputFilename);
            FileUtils.writeStringToFile(outputFile, xml, "UTF-8");
            return true;
        } else if (extension.equals(Const.STRING_JOB_DEFAULT_EXT) && file instanceof JobMeta) {
            JobMeta jobMeta = (JobMeta) file;
            String outputFilename = dir + filename + "." + Const.STRING_JOB_DEFAULT_EXT;
            String xml = jobMeta.getXML();
            File outputFile = new File(outputFilename);
            FileUtils.writeStringToFile(outputFile, xml, "UTF-8");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Result executedJob(JobMeta jobMeta) {
        Job job = new Job(null, jobMeta);
        job.start();
        job.waitUntilFinished();
        return job.getResult();
    }

    @Override
    public JobMeta generateJob(String name) {
        JobMeta jobMeta = new JobMeta();
        jobMeta.setName(name);
        jobMeta.setFilename(name + "_job");

        JobEntryCopy startCopy = JobMeta.createStartEntry();
        final Point location = new Point(50, 50);
        startCopy.setLocation(50,50);
        startCopy.setDrawn();
        jobMeta.addJobEntry(startCopy);

        DatabaseMeta sourceMeta = generateDatabase(source);
        DatabaseMeta targetMeta = generateDatabase(target);
        jobMeta.setDatabases(new ArrayList<DatabaseMeta>(){{
            add(sourceMeta);
            add(targetMeta);
        }});
        schemaName = "ALE97";
        String[] tablesWithSchema = new String[0];
        Database sourceDatabase = new Database(null, sourceMeta);
        try {
            sourceDatabase.connect();
            tablesWithSchema = sourceDatabase.getTablenames(true);
            DatabaseMetaInformation databaseMetaInformation = new GetDatabaseInfoProgressDialog(null,sourceMeta).open();
            System.out.println(databaseMetaInformation.getSchemas()[0].getSchemaName());
            for (Schema schema : databaseMetaInformation.getSchemas()) {
                if (schemaName.equals(schema.getSchemaName())) {
                    tablesWithSchema = schema.getItems();
                }
            }

        } catch (KettleDatabaseException e) {
            e.printStackTrace();
        } finally {
            sourceDatabase.disconnect();
        }
//        String[] tables = filterOfTable(tablesWithSchema, sourceMeta.getDatabaseName());
        String[] tables = tablesWithSchema;

        JobEntryCopy previous = startCopy;
        for (String table : tables) {
            System.out.println(table);
            TransMeta transMeta = new TransMeta();
            transMeta.setName(jobMeta.getName() + "_source" + table + "_to_target");
            transMeta.addDatabase(sourceMeta);
            transMeta.addDatabase(targetMeta);

            TableInputMeta tableInputMeta = new TableInputMeta();
            tableInputMeta.setDefault();
            tableInputMeta.setDatabaseMeta(sourceMeta);
            tableInputMeta.setSQL("select * from " + schemaName + "." + table);
            StepMeta tableInputStep = new StepMeta("table input", tableInputMeta);
            tableInputStep.setLocation(100, 300);
            tableInputStep.setDraw(true);
            transMeta.addStep(tableInputStep);

            TableOutputMeta tableOutputMeta = new TableOutputMeta();
            tableOutputMeta.setDefault();
            tableOutputMeta.setDatabaseMeta(targetMeta);
            tableOutputMeta.setTableName(table);
            StepMeta tableOutputStep = new StepMeta("table output", tableOutputMeta);
            tableOutputStep.setLocation(300, 300);
            tableOutputStep.setDraw(true);
            transMeta.addStep(tableOutputStep);

            transMeta.addTransHop(new TransHopMeta(tableInputStep, tableOutputStep));
            try {
                boolean ok = saveFile("etl/", transMeta.getName(), Const.STRING_TRANS_DEFAULT_EXT, transMeta);
            } catch (KettleException | IOException e) {
                e.printStackTrace();
            }

            location.x = 250;
            location.y += 100;
            String sql = getSQLString(sourceMeta, tableInputMeta, transMeta);
            System.out.println(sql);

            if (!Utils.isEmpty(sql)) {
                JobEntrySQL jobEntrySQL = new JobEntrySQL("create " + table);
                jobEntrySQL.setDatabase(targetMeta);
                jobEntrySQL.setSQL(sql);

                JobEntryCopy sqlJobEntryCopy = new JobEntryCopy();
                sqlJobEntryCopy.setEntry(jobEntrySQL);
                sqlJobEntryCopy.setLocation(new Point(location.x, location.y));
                sqlJobEntryCopy.setDrawn();
                jobMeta.addJobEntry(sqlJobEntryCopy);
                JobHopMeta sqlJobHopMeta = new JobHopMeta(previous, sqlJobEntryCopy);
                jobMeta.addJobHop(sqlJobHopMeta);
                previous = sqlJobEntryCopy;
            }

            JobEntryTrans transJobEntryTrans = new JobEntryTrans("trans " + table);
            transJobEntryTrans.setTransname(transMeta.getName());
            transJobEntryTrans.setSpecificationMethod(ObjectLocationSpecificationMethod.FILENAME);
            transJobEntryTrans.setFileName( Const.createFilename( "${"
                    + Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY + "}", transMeta.getName(), "."
                    + Const.STRING_TRANS_DEFAULT_EXT ) );
            System.out.println(transJobEntryTrans.getFilename());
//            transJobEntryTrans.setFileName("etl/" + transMeta.getName() + "." + Const.STRING_TRANS_DEFAULT_EXT);
            JobEntryCopy transJobEntryCopy = new JobEntryCopy(transJobEntryTrans);
            location.x += 400;
            transJobEntryCopy.setLocation(new Point(location.x, location.y));
            transJobEntryCopy.setDrawn();
            jobMeta.addJobEntry(transJobEntryCopy);

            JobHopMeta transJobHopMeta = new JobHopMeta(previous, transJobEntryCopy);
            jobMeta.addJobHop(transJobHopMeta);
            previous = transJobEntryCopy;
        }
        return jobMeta;
    }

    @Override
    public DatabaseMeta generateDatabase(DatabaseConfig database) {
        DatabaseMeta databaseMeta = new DatabaseMeta();
        databaseMeta.setValues(
                database.getName(),
                database.getType(),
                database.getAccess(),
                database.getHost(),
                database.getDatabase(),
                database.getPort(),
                database.getUser(),
                database.getPassword()
        );
        if ("Oracle".equals(database.getType()) && database instanceof OracleDatabaseConfig) {
            databaseMeta.setServername("ora11g");
        }
        if ("MySQL".equals(database.getType()) && database instanceof MySQLDatabaseConfig) {
            MySQLDatabaseConfig mySQLDatabase = (MySQLDatabaseConfig) database;
            Properties properties = new Properties();
            properties.setProperty("EXTRA_OPTION_MYSQL.serverTimezone", mySQLDatabase.getServerTimezone());
            databaseMeta.setAttributes(properties);
        }
        return databaseMeta;
    }

    @Override
    public String[] filterOfTable(String[] tables, String databaseName) {
        List<String> tablesOfDatabase = new ArrayList<>();
        if (tables == null) {
            return tablesOfDatabase.toArray(new String[0]);
        }
        for (String table : tables) {
            if (table.startsWith(databaseName)) {
                tablesOfDatabase.add(table.substring(databaseName.length() + 1));
            }
        }
        return tablesOfDatabase.toArray(new String[0]);
    }

    @Override
    public String getSQLString(DatabaseMeta sourceDbInfo, TableInputMeta tii, TransMeta transMeta) {
        // First set the limit to 1 to speed things up!
        String tmpSql = tii.getSQL();
        tii.setSQL( tii.getSQL() + sourceDbInfo.getLimitClause( 1 ) );
        String sql = null;
        try {
            sql = transMeta.getSQLStatementsString();
        } catch ( KettleStepException kse ) {
            kse.printStackTrace();
        }
        // remove the limit
        tii.setSQL( tmpSql );
        return sql;
    }
}
