package com.shenxu.demodynamicjobmultidatabase.Service;

import com.shenxu.demodynamicjobmultidatabase.Database.Database;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;

import java.io.IOException;

public interface DynamicJobService {
    TransMeta generateTransformation(String name);

    Boolean saveFile(String dir, String filename, String extension, Object file) throws IOException, KettleException;

    Result executedJob(JobMeta jobMeta);

    JobMeta generateJob(String name);

    DatabaseMeta generateDatabase(Database database);

    String[] filterOfTable(String[] tables, String databaseName);

    String getSQLString(DatabaseMeta databaseMeta, TableInputMeta tii, TransMeta transMeta);
}
