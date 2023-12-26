package isec.tp.das.onlinecompiler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;

@SpringBootApplication
public class OnlineCompilerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineCompilerApplication.class, args);
//		Connection connection = Database.initializeDatabase();
	}

}
