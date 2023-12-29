package isec.tp.das.onlinecompiler;

import isec.tp.das.onlinecompiler.services.factories.ConcreteProjectEntityFactory;
import isec.tp.das.onlinecompiler.services.factories.ProjectEntityFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public ProjectEntityFactory projectEntityFactory() {
        return new ConcreteProjectEntityFactory();
    }
}

