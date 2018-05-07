package org.kitodo.mediaserver.importer.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;


/**
 * this configuration class is mainly needed to be able to automatically create database tables
 * needed by the Spring Batch itself internally
 * the definition for the tables is in spring own
 * classpath:org/springframework/batch/core/schema-mysql.sql
 * and classpath:org/springframework/batch/core/schema-drop-mysql.sql
 *
 * it also provides the  @Bean(name = "dataSource") for the rest of the importer
 *
 *  by default the database populator is turned on by the
 *  importer.recreate-batch-internal-tables-on-each-start
 *  which is by default set to TRUE  in the configuration
 *  but it needs later be turned off by setting the this property
 *  this improves the startup time and allows spring batch to remember its state between program reruns
 *
 *  essentially you could use this property to get reed of old spring batch state after something got really wrong
 *  and you wont to start over again
 *
 */
@Configuration
public class BatchTablesConfiguration {


    /**
     * the main property configuration of the importer
     *see inside its class for more details
     */
    @Autowired
    private ImporterConfig importerConfig;

    @Value("classpath:org/springframework/batch/core/schema-drop-mysql.sql")
    private Resource dropSchemaScript;
    @Value("classpath:org/springframework/batch/core/schema-mysql.sql")
    private Resource createSchemaScript;

    //@Autowired
    //private ImporterConfig importerConfig;

    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String dbUserName;
    @Value("${spring.datasource.password}")
    private String dbPassword;
    //private ImporterConfig importerConfig;

    @Bean(name = "dataSource")
    public DataSource dataSource() {
        DataSource dataSource = createDataSource();
        //recreate all Spring Batch internal tables on each start when this property is set
        if(importerConfig.isRecreateBatchInternalTablesOnEachStart()) {
            DatabasePopulatorUtils.execute(createDatabasePopulator(), dataSource);
        }
        return dataSource;
    }


    /**
     * gets the SimpleDriverDataSource using the url,username and password properties from the configuration (usually in .yaml file )
     * @return yes the datasource which is then returned by the  @Bean(name = "dataSource") after rerunning the databasepopulator if necessary
     */
    private SimpleDriverDataSource createDataSource() {
        SimpleDriverDataSource simpleDriverDataSource = new SimpleDriverDataSource();
        simpleDriverDataSource.setDriverClass(com.mysql.jdbc.Driver.class);
        simpleDriverDataSource.setUrl(dbUrl);
        simpleDriverDataSource.setUsername(dbUserName);
        simpleDriverDataSource.setPassword(dbPassword);
        return simpleDriverDataSource;
    }

    /**
     * get a database populator which can be run to Drop and recreate Spring Batches internal tables
     * @return the populator
     */
    private DatabasePopulator createDatabasePopulator() {
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.setContinueOnError(true);
        databasePopulator.addScript(dropSchemaScript);
        databasePopulator.addScript(createSchemaScript);
        //TODO: what was this for again? :) no time now this would alse create the Domain Tables for the core , do we need it here?
        //databasePopulator.addScript(new ClassPathResource("schema.sql"));
        return databasePopulator;
    }
}

