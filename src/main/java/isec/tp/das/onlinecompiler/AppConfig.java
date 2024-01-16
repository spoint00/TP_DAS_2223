package isec.tp.das.onlinecompiler;

import isec.tp.das.onlinecompiler.services.ProjectDecorator;
import isec.tp.das.onlinecompiler.services.ProjectService;
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
    public ProjectService projectService() {
        return new
    }

    @Bean
    public ProjectDecorator projectDecorator(ProjectService projectService) {
        return new ProjectDecorator(projectService);
    }
}

