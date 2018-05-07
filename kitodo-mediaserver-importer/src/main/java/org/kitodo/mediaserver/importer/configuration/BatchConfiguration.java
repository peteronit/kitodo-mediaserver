package org.kitodo.mediaserver.importer.configuration;

import org.kitodo.mediaserver.core.db.repositories.IdentifierRepository;
import org.kitodo.mediaserver.core.db.repositories.WorkRepository;
import org.kitodo.mediaserver.importer.WorkMetaInfo;
import org.kitodo.mediaserver.importer.WorkPojoWithMetaInfo;
import org.kitodo.mediaserver.importer.batch.FileMovingWriter;
import org.kitodo.mediaserver.importer.batch.JobCompletionNotificationListener;
import org.kitodo.mediaserver.importer.batch.JpaWorkPojoWithMetaInfoWriter;
import org.kitodo.mediaserver.importer.core.MetsModsFileProcessor;
import org.kitodo.mediaserver.importer.core.MetsModsReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Properties;

/**
 *  Main Java configuration class of the project
 *  providing the SPRING BATCH infrastructure with the job and  step
 *  the scheduling of this Job is done in the ScheduleConfiguration
 */
@Configuration
@EnableBatchProcessing
public class BatchConfiguration {


    /**
     * the main property configuration of the importer
     *see inside its class for more details
     */
    @Autowired
    private ImporterConfig importerConfig;

    /**
     * spring Batch internal Factory used below to create the importer job
     */
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    /**
     *  spring Batch internal Factory used below to create the Step1
     */
    @Autowired
    public StepBuilderFactory stepBuilderFactory;


    /**
     * creates and returns the main importing job consisting of only one Step1
     * the job does the importing  and is rerun on a schedule
     *
     * @param jobCompletionNotificationListener called after job completion to show something for now, might not be needed
     * @param step1                             the only step that actually does the whole job
     * @return the job bean
     */
    @Bean
    public Job importWorksJob(JobCompletionNotificationListener jobCompletionNotificationListener, Step step1) {
        return jobBuilderFactory.get("importWorksJob")
                .incrementer(new RunIdIncrementer())
                .listener(jobCompletionNotificationListener)
                .flow(step1)
                .end()
                .build();
    }

    /**
     * the only step that makes all the work
     * it needs the 3 bean injected as params
     *
     * @param metsModsFileProcessor
     * @param metsModsReader
     * @param workToDbWriterAndFileSystemMover
     * @return the step bean used in the Job Bean
     */
    @Bean
    public Step step1(
            MetsModsFileProcessor metsModsFileProcessor
            , MetsModsReader metsModsReader
            , ItemWriter<WorkPojoWithMetaInfo> workToDbWriterAndFileSystemMover) {
        // the chunk size(the number of items read and written out at once) is set to 1
        // though it might work with larger chunks to increase performance,
        // it is not thought through or tested yet, how it will behave if there is a
        // failure in the middle of chunk processing
        //StdOutWriter can be used for debugging istead of workToDbWriterAndFileSystemMover

        return stepBuilderFactory.get("step1")
                .<WorkMetaInfo, WorkPojoWithMetaInfo>chunk(1)
                .reader(metsModsReader)
                .processor(metsModsFileProcessor)
                .writer(workToDbWriterAndFileSystemMover)
                //.writer(new StdoutWriter())
                .build();
    }

    /**
     * gets a composite ItemWriter that
     * writes Out a list of imported Work Objects(including their) Identifiers into Db and moves the imported files to
     * target Folders (it essentially delegates the two work parts to writers it needs injected in the following parameters)
     *
     * @param jpaWorkPojoWithMetaInfoWriter a writer that Writes Out a list of imported Work Objects(including their) Identifiers into Db
     * @param fileMovingWriter              a writer that moves the imported files to target Folders
     * @return the composite Itemwriter
     */
    @Bean
    @Primary
    public ItemWriter<WorkPojoWithMetaInfo> WorkToDbWriterAndFileSystemMover(JpaWorkPojoWithMetaInfoWriter jpaWorkPojoWithMetaInfoWriter
            , FileMovingWriter fileMovingWriter) {

        CompositeItemWriter compositeItemWriter = new CompositeItemWriter();

        compositeItemWriter.setDelegates(Arrays.asList(
                fileMovingWriter,
                jpaWorkPojoWithMetaInfoWriter

        ));

        return compositeItemWriter;
    }

    /**
     * gets a JPA ItemWriter that
     * writes Out a list of imported Work Objects(including their) Identifiers into Db
     * needs two spring JPA CRUD repositories to be injected:
     *
     * @param workRepository       for writing Work Pojo
     * @param identifierRepository for writing Identifier Pojo
     * @return well the JPA ItemWriter itself
     */

    @Bean
    JpaWorkPojoWithMetaInfoWriter jpaWorkPojoWithMetaInfoWriter(
              @Autowired WorkRepository workRepository
            , @Autowired IdentifierRepository identifierRepository) {

        return new JpaWorkPojoWithMetaInfoWriter()
                .setWorkRepository(workRepository)
                .setIdentifierRepository(identifierRepository);
    }

    @Bean
    public FileMovingWriter fileMovingWriter()  {
        return new FileMovingWriter(importerConfig);

    }




//below are the beans to the hibernate Jpa running
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Autowired @Qualifier("dataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
        lef.setPackagesToScan("org.kitodo.mediaserver.core.db.entities");
        lef.setDataSource(dataSource);
        lef.setJpaVendorAdapter(jpaVendorAdapter());
        lef.setJpaProperties(new Properties());
        return lef;
    }


//TODO: might need to make the sqldialect and dbtype configurable foer different MySql versions and other DBs

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();

        jpaVendorAdapter.setGenerateDdl(true);
        //jpaVendorAdapter.setShowSql(true);
        jpaVendorAdapter.setDatabase(Database.MYSQL);
        jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        return jpaVendorAdapter;
    }

}

