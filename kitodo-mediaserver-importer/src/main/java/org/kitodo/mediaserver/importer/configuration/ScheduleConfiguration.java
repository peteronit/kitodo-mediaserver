package org.kitodo.mediaserver.importer.configuration;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobInstanceAlreadyExistsException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableScheduling
public class ScheduleConfiguration {


    @Autowired
    @Qualifier(value = "importWorksJob")
    private Job job;

    @Autowired
    private JobLauncher jobLauncher;

    @Scheduled(cron = "${importer.run-on-cron-schedule}" , fixedDelayString = "${importer.pause-between-runs-in-milliseconds}")
    public void launch() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobInstanceAlreadyExistsException, NoSuchJobException {

         //launchtime needs to be added since spring batch will not restart the same job with the same jobparameters
        jobLauncher.run(job,new JobParametersBuilder()
                .addLong("launchTime", System.currentTimeMillis())
                .toJobParameters());
    }

    @Bean(name = "importWorksJobExecutorPool")
    public TaskExecutor singleThreadedJobExecutorPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(100500);
        executor.setThreadNamePrefix("import-works-job-batch-");
        return executor;
    }

    @Bean(name = "importWorksJobLauncher")
    public JobLauncher singleThreadedJobLauncher(JobRepository jobRepository)
    {
        SimpleJobLauncher sjl = new SimpleJobLauncher();
        sjl.setJobRepository(jobRepository);
        sjl.setTaskExecutor(singleThreadedJobExecutorPool());
        return sjl;
    }

}
