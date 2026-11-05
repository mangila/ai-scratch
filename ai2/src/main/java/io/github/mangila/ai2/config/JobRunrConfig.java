package io.github.mangila.ai2.config;

import org.jobrunr.jobs.mappers.JobMapper;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.server.BackgroundJobServer;
import org.jobrunr.server.BackgroundJobServerConfiguration;
import org.jobrunr.storage.InMemoryStorageProvider;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.utils.mapper.jackson3.Jackson3JsonMapper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Configuration
public class JobRunrConfig {

    @Bean
    public Jackson3JsonMapper jackson3JsonMapper() {
        return new Jackson3JsonMapper();
    }

    @Bean
    public JobMapper jobMapper(Jackson3JsonMapper jackson3JsonMapper) {
        return new JobMapper(jackson3JsonMapper);
    }

    @Bean
    public StorageProvider storageProvider(JobMapper jobMapper) {
        final StorageProvider storageProvider = new InMemoryStorageProvider();
        storageProvider.setJobMapper(jobMapper);
        return storageProvider;
    }

    @Bean
    public JobScheduler jobScheduler(StorageProvider storageProvider) {
        return new JobScheduler(storageProvider);
    }

    /**
     * NOTE: JobRunr bootstrap since via Spring Shell does not fire the event ApplicationReadyEvent
     */
    @Component
    public static class Bootstrap implements DisposableBean {

        private final BackgroundJobServer backgroundJobServer;

        public Bootstrap(StorageProvider storageProvider, Jackson3JsonMapper jackson3JsonMapper, ApplicationContext applicationContext) {
            this.backgroundJobServer = new BackgroundJobServer(storageProvider, jackson3JsonMapper, applicationContext::getBean, BackgroundJobServerConfiguration.usingStandardBackgroundJobServerConfiguration());
        }

        @EventListener(ApplicationStartedEvent.class)
        public void handleApplicationStarted() {
            backgroundJobServer.start();
        }

        @Override
        public void destroy() throws Exception {
            backgroundJobServer.stop();
        }

    }
}
