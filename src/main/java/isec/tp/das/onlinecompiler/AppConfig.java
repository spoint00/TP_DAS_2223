package isec.tp.das.onlinecompiler;

import isec.tp.das.onlinecompiler.repository.ProjectRepository;
import isec.tp.das.onlinecompiler.repository.ResultEntityRepository;
import isec.tp.das.onlinecompiler.services.BuildManager;
import isec.tp.das.onlinecompiler.services.DefaultProjectDecorator;
import isec.tp.das.onlinecompiler.services.DefaultProjectService;
import isec.tp.das.onlinecompiler.services.factories.ConcreteProjectEntityFactory;
import isec.tp.das.onlinecompiler.services.factories.ConcreteResultEntityFactory;
import isec.tp.das.onlinecompiler.services.factories.ProjectEntityFactory;
import isec.tp.das.onlinecompiler.services.factories.ResultEntityFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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
    public DefaultProjectService projectService(ProjectRepository projectRepository, ResultEntityRepository resultRepository, ProjectEntityFactory projectFactory, ResultEntityFactory resultFactory) {
        return new DefaultProjectService(projectRepository, resultRepository, projectFactory, resultFactory);
    }

    @Bean
    public DefaultProjectDecorator projectDecorator(DefaultProjectService defaultProjectService) {
        return new DefaultProjectDecorator(defaultProjectService);
    }

    @Bean
    public BuildManager buildManager() {
        return BuildManager.getInstance();
    }
}

