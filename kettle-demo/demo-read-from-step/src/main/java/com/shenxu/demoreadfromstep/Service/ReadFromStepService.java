package com.shenxu.demoreadfromstep.Service;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepInterface;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReadFromStepService implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        KettleEnvironment.init();

        TransMeta transMeta = new TransMeta("etl/demo.ktr");
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);

        final List<RowMetaAndData> rows = new ArrayList<>();
        RowListener rowListener = new RowAdapter() {
            @Override
            public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
                rows.add(new RowMetaAndData(rowMeta, row));
            }
        };
        StepInterface stepInterface = trans.findRunThread("update table");
        if (stepInterface != null) {
            System.out.println("stepinterface is not null");
            stepInterface.addRowListener(rowListener);
        }

        trans.startThreads();
        trans.waitUntilFinished();

        if (trans.getErrors() != 0) {
            System.out.println("Error");
        } else {
            System.out.println(rows.size());
        }

        for (RowMetaAndData row : rows){
            System.out.println(row);
        }
    }
}
