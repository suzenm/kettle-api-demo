package com.shenxu.demotransformation.Service;

import org.apache.commons.lang.RandomStringUtils;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingBuffer;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TransformationService implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        KettleEnvironment.init();

        Trans trans = runTransfomationFromFileSystem("etl/copy_sourcefilm_category_to_target.ktr");

        LoggingBuffer appender = KettleLogStore.getAppender();
        String logText = appender.getBuffer(trans.getLogChannelId(), false).toString();

        System.out.println( "************************************************************************************************" );
        System.out.println( "LOG REPORT: Transformation generated the following log lines:\n" );
        System.out.println( logText );
        System.out.println( "END OF LOG REPORT" );
        System.out.println( "************************************************************************************************" );

    }

    public Trans runTransfomationFromFileSystem(String filename) throws KettleException {
        TransMeta transMeta = new TransMeta(filename, (Repository)null);
        String[] declaredParameters = transMeta.listParameters();

        for (String parameterName: declaredParameters) {
            String description = transMeta.getParameterDescription(parameterName);
            String defaultValue = transMeta.getParameterDefault(parameterName);

            String parameterValue = RandomStringUtils.randomAlphanumeric(10);

            String output = String.format("Setting parameter %s to \"%s\" [description: \"%s\", default: \"%s\"]",
                    parameterName, parameterValue, description, defaultValue);

            System.out.println(output);

            transMeta.setParameterValue(parameterName, parameterValue);
        }

        Trans transformation = new Trans(transMeta);
        transformation.setLogLevel(LogLevel.BASIC);

        transformation.execute(new String[0]);

        transformation.waitUntilFinished();

        Result result = transformation.getResult();

        String outcome = String.format( "\nTrans %s executed %s", filename,
                ( result.getNrErrors() == 0 ? "successfully" : "with " + result.getNrErrors() + " errors" ) );
        System.out.println( outcome );

        return transformation;
    }
}
