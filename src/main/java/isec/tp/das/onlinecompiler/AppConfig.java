package isec.tp.das.onlinecompiler;

import isec.tp.das.onlinecompiler.repository.ProjectRepository;
import isec.tp.das.onlinecompiler.services.SaveOutputProjectDecorator;
import isec.tp.das.onlinecompiler.services.DefaultProjectService;
import isec.tp.das.onlinecompiler.services.ProjectService;
import isec.tp.das.onlinecompiler.services.factories.ConcreteProjectEntityFactory;
import isec.tp.das.onlinecompiler.services.factories.ConcreteResultEntityFactory;
import isec.tp.das.onlinecompiler.services.factories.ProjectEntityFactory;
import isec.tp.das.onlinecompiler.services.factories.ResultEntityFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AppConfig {
    @Bean
    public ProjectEntityFactory projectEntityFactory() {
        return new ConcreteProjectEntityFactory();
    }

    @Bean
    public ResultEntityFactory resultEntityFactory() {
        return new ConcreteResultEntityFactory();
    }

    @Bean
    public ProjectService projectService(ProjectRepository projectRepository, ProjectEntityFactory projectFactory, ResultEntityFactory resultFactory) {
        return new DefaultProjectService(projectRepository, projectFactory, resultFactory);
    }

    @Bean
    public SaveOutputProjectDecorator projectDecorator(ProjectService projectService) {
        return new SaveOutputProjectDecorator(projectService);
    }

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("AsynchThread-");
        executor.initialize();
        return executor;
    }

//    @Bean
//    public BuildManager buildManager() {
//        return BuildManager.getInstance();
//    }
}

