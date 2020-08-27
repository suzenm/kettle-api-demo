package com.shenxu.demodynamicjobmultidatabase;

import com.shenxu.demodynamicjobmultidatabase.Database.MySQLDatabaseConfig;
import com.shenxu.demodynamicjobmultidatabase.Database.OracleDatabaseConfig;
import com.shenxu.demodynamicjobmultidatabase.Service.DynamicJobService;
import com.shenxu.demodynamicjobmultidatabase.Service.Impl.DynamicJobServiceImpl;
import org.junit.jupiter.api.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class DemoDynamicJobMultiDatabaseApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void jobTest() throws KettleException, IOException {
        OracleDatabaseConfig source = new OracleDatabaseConfig("source", "Native", "10.20.32.146", "ora11g", "tg_qs", "tg_qs", "1521", "Oracle");
        MySQLDatabaseConfig target = new MySQLDatabaseConfig("target", "Native", "localhost", "oracle_test", "root", "admin", "3306", "MySQL", "UTC");
        DynamicJobServiceImpl dynamicJobService = new DynamicJobServiceImpl(source, target);

        KettleEnvironment.init();
        dynamicJobService.jobService();
    }

}
