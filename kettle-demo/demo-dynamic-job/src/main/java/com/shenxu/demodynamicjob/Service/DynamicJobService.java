package com.shenxu.demodynamicjob.Service;

import org.apache.commons.io.FileUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
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
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
public class DynamicJobService implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {

        KettleEnvironment.init();

        JobMeta jobMeta = GenerateJob("copy");
        boolean ok = SaveFile("etl/", jobMeta.getFilename(), Const.STRING_JOB_DEFAULT_EXT, jobMeta);
        JobMeta fileJobMeta = new JobMeta("etl/"+ jobMeta.getFilename() +".kjb", null);
        Result rs = ExecutedJob(fileJobMeta);

    }

    public boolean SaveFile(String dir, String filename, String extension, Object file) throws KettleException, IOException {

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

    public Result ExecutedJob (JobMeta jobMeta) {
        Job job = new Job(null, jobMeta);
        job.start();
        job.waitUntilFinished();
        return job.getResult();
    }

    public JobMeta GenerateJob(String name) {
//        step1.新建job
        final JobMeta jobMeta = new JobMeta();
        jobMeta.setName(name);
        jobMeta.setFilename("demo_job");
//        step2.创建start实体
        JobEntryCopy startCopy = JobMeta.createStartEntry();
        final Point location = new Point(50, 50);
        startCopy.setLocation(location.x, location.y);
        startCopy.setDrawn();
        jobMeta.addJobEntry(startCopy);
//        step3.创建数据库元数据，source和target
        DatabaseMeta sourceDatabaseMeta = GenerateDatabase("source", "sakila");
        DatabaseMeta targetDatabaseMeta = GenerateDatabase("target", "test");
        jobMeta.setDatabases(new ArrayList<DatabaseMeta>(){{
            add(sourceDatabaseMeta);
            add(targetDatabaseMeta);
        }});
//        step4.从source数据库中获取表名
        String[] tablesWithSchema = new String[0];
        Database sourceDatabase = new Database(null, sourceDatabaseMeta);
        try {
            sourceDatabase.connect();
            tablesWithSchema = sourceDatabase.getTablenames(true);
        } catch (KettleDatabaseException e) {
            e.printStackTrace();
        }
        String[] tables = filterOfTable(tablesWithSchema, sourceDatabaseMeta.getDatabaseName());
//        step5.根据表名创建每张表的transformation
        JobEntryCopy previous = startCopy;
        for (String table : tables) {
            TransMeta transMeta = new TransMeta();
            transMeta.setName(jobMeta.getName() + "_source" + table + "_to_target");
            transMeta.addDatabase(sourceDatabaseMeta);
            transMeta.addDatabase(targetDatabaseMeta);

            TableInputMeta tableInputMeta =new TableInputMeta();
            tableInputMeta.setDefault();
            tableInputMeta.setDatabaseMeta(sourceDatabaseMeta);
            tableInputMeta.setSQL("select * from " + table);
            StepMeta tableInputStep = new StepMeta("table input", tableInputMeta);
            tableInputStep.setLocation(100, 300);
            tableInputStep.setDraw(true);
            transMeta.addStep(tableInputStep);

            TableOutputMeta tableOutputMeta = new TableOutputMeta();
            tableOutputMeta.setDefault();
            tableOutputMeta.setDatabaseMeta(targetDatabaseMeta);
            tableOutputMeta.setTableName(table);
            StepMeta tableOutputStep = new StepMeta("table output", tableOutputMeta);
            tableOutputStep.setLocation(300, 300);
            tableOutputStep.setDraw(true);
            transMeta.addStep(tableOutputStep);

            transMeta.addTransHop(new TransHopMeta(tableInputStep, tableOutputStep));
//            step6.保存transformation到文件
            try {
                boolean ok = SaveFile("etl/", transMeta.getName(), Const.STRING_TRANS_DEFAULT_EXT, transMeta);
            } catch (KettleException | IOException e) {
                e.printStackTrace();
            }

//        step7.为target库生成source库中的表,并加入job
            location.x = 250;
            location.y += 100;
            String sql = getSQLString(sourceDatabaseMeta, tableInputMeta, transMeta);

            if (!Utils.isEmpty(sql)) {
                JobEntrySQL jobEntrySQL = new JobEntrySQL("create " + table);
                jobEntrySQL.setDatabase(targetDatabaseMeta);
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

//        step8.将transformation加入job
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

    public DatabaseMeta GenerateDatabase(String name, String database) {
        DatabaseMeta databaseMeta = new DatabaseMeta();
        databaseMeta.setValues(name, "MySQL", "Native", "localhost", database, "3306", "root", "admin");
        Properties databaseProperties = new Properties();
        databaseProperties.setProperty("EXTRA_OPTION_MYSQL.serverTimezone", "UTC");
        databaseMeta.setAttributes(databaseProperties);
        return  databaseMeta;
    }

    public String[] filterOfTable(String[] tables, String database) {
        List<String> tablesOfDatabase = new ArrayList<>();
        if (tables == null) {
            return tablesOfDatabase.toArray(new String[0]);
        }
        for (String table : tables) {
            if (table.startsWith(database)) {
                tablesOfDatabase.add(table.substring(database.length() + 1));
            }
        }
        return tablesOfDatabase.toArray(new String[0]);
    }

    public String getSQLString( DatabaseMeta sourceDbInfo, TableInputMeta tii, TransMeta transMeta ) {
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
