package samoprodej.samoprodej.config.initializers;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserInitializer userInitializer;
    private final PropertyInitializer propertyInitializer;
    private final PropertyMediaInitializer propertyMediaInitializer;
    private final ListingInitializer listingInitializer;
    public DataInitializer(UserInitializer userInitializer, PropertyInitializer propertyInitializer, PropertyMediaInitializer propertyMediaInitializer, ListingInitializer listingInitializer) {
        this.userInitializer = userInitializer;
        this.propertyInitializer = propertyInitializer;
        this.propertyMediaInitializer = propertyMediaInitializer;
        this.listingInitializer = listingInitializer;

    }

    @Override
    public void run(String... args) throws Exception {
        userInitializer.initialize();
        propertyInitializer.initialize();
        propertyMediaInitializer.initialize();
        listingInitializer.initialize();
    }
}