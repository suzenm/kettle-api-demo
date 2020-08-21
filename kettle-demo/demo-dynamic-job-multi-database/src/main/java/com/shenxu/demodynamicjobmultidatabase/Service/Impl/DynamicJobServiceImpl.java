package com.shenxu.demodynamicjobmultidatabase.Service.Impl;

import com.shenxu.demodynamicjobmultidatabase.Database.Database;
import com.shenxu.demodynamicjobmultidatabase.Database.MySQLDatabase;
import com.shenxu.demodynamicjobmultidatabase.Service.DynamicJobService;
import org.apache.commons.io.FileUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class DynamicJobServiceImpl implements DynamicJobService {
    @Override
    public TransMeta generateTransformation(String name) {
        return null;
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
        return null;
    }

    @Override
    public JobMeta generateJob(String name) {
        return null;
    }

    @Override
    public DatabaseMeta generateDatabase(Database database) {
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
        if ("MySQL".equals(database.getType()) && database instanceof MySQLDatabase) {
            MySQLDatabase mySQLDatabase = (MySQLDatabase) database;
            Properties properties = new Properties();
            properties.setProperty("EXTRA_OPTION_MYSQL.serverTimezone", mySQLDatabase.getServerTimezone());
            databaseMeta.setAttributes(properties);
        }
        return databaseMeta;
    }

    @Override
    public String[] filterOfTable(String[] tables, String databaseName) {
        return new String[0];
    }

    @Override
    public String getSQLString(DatabaseMeta databaseMeta, TableInputMeta tii, TransMeta transMeta) {
        return null;
    }
}
