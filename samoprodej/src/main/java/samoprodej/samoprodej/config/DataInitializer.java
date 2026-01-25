package samoprodej.samoprodej.config;


import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner dbInitializer() {
        //to-do
        //add repository in dbInitializer parameter
        //add entity init,
        // (admin user,
        // mock property,
        // atd...)
        return args -> {};
    }
}
