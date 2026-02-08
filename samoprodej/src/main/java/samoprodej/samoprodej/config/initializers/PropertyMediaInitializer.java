package samoprodej.samoprodej.config.initializers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import samoprodej.samoprodej.entity.Property;
import samoprodej.samoprodej.entity.PropertyMedia;
import samoprodej.samoprodej.enums.MediaType;
import samoprodej.samoprodej.repository.PropertyMediaRepository;
import samoprodej.samoprodej.repository.PropertyRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class PropertyMediaInitializer {

    private final PropertyMediaRepository propertyMediaRepository;
    private final PropertyRepository propertyRepository;
    private final Random random = new Random();

    @Value("${app.media.min-per-property:3}")
    private int minImages;

    @Value("${app.media.max-per-property:6}")
    private int maxImages;

    public PropertyMediaInitializer(PropertyMediaRepository propertyMediaRepository,
                                    PropertyRepository propertyRepository) {
        this.propertyMediaRepository = propertyMediaRepository;
        this.propertyRepository = propertyRepository;
    }

    public void initialize() {
        if (propertyMediaRepository.count() == 0) {
            System.out.println("Initializing mock media properties...");
            createMediaForProperties();
        }
    }

    private void createMediaForProperties() {
        List<Property> properties = propertyRepository.findAll();

        if (properties.isEmpty()) {
            System.out.println("Skipping media initialization: No properties found.");
            return;
        }

        List<PropertyMedia> mediaList = new ArrayList<>();

        for (Property property : properties) {

            int imagesCount = random.nextInt(maxImages - minImages + 1) + minImages;

            for (int i = 0; i < imagesCount; i++) {
                int randomId = random.nextInt(1000);
                String imageUrl = "https://picsum.photos/id/" + randomId + "/800/600";
                String previewUrl = "https://picsum.photos/id/" + randomId + "/200/150";

                PropertyMedia media = new PropertyMedia(
                        property,
                        MediaType.PHOTO,
                        imageUrl
                );

                media.setPreviewUrl(previewUrl);

                media.setSortOrder(i);

                mediaList.add(media);
            }
        }

        propertyMediaRepository.saveAll(mediaList);
        System.out.println("Created " + mediaList.size() + " media items for " + properties.size() + " properties.");
    }
}