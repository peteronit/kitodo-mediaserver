package org.kitodo.mediaserver.importer.batch;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

   /* private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }*/

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("FINISHED: importing completed");
        /*
            jdbcTemplate.queryForList("SELECT * FROM work")
                    .forEach(person -> log.info("Found <" + person + "> in the database."));
            jdbcTemplate.queryForList("SELECT * FROM identifier")
                    .forEach(person -> log.info("Found <" + person + "> in the database."));
        */}
    }
}