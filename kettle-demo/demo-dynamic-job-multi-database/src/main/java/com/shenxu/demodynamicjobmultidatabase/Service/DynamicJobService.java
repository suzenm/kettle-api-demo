package com.shenxu.demodynamicjobmultidatabase.Service;

import com.shenxu.demodynamicjobmultidatabase.Database.DatabaseConfig;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;

import java.io.IOException;

public interface DynamicJobService {

    Boolean saveFile(String dir, String filename, String extension, Object file) throws IOException, KettleException;

    Result executedJob(JobMeta jobMeta);

    JobMeta generateJob(String name);

    DatabaseMeta generateDatabase(DatabaseConfig database);

    String[] filterOfTable(String[] tables, String databaseName);

    String getSQLString(DatabaseMeta sourceDbInfo, TableInputMeta tii, TransMeta transMeta);
}
