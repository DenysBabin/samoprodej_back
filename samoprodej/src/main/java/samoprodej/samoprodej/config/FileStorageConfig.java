package samoprodej.samoprodej.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageConfig {
    private String path = "uploads/properties";
    private long maxSizePhoto = 10 * 1024 * 1024; // 10MB
    private long maxSizeVideo = 100 * 1024 * 1024; // 100MB
    private Preview preview = new Preview();
    private int maxMediaPerProperty = 50;

    public static class Preview {
        private int maxWidth = 800;
        private int maxHeight = 600;

        public int getMaxWidth() {
            return maxWidth;
        }

        public void setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
        }

        public int getMaxHeight() {
            return maxHeight;
        }

        public void setMaxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getMaxSizePhoto() {
        return maxSizePhoto;
    }

    public void setMaxSizePhoto(long maxSizePhoto) {
        this.maxSizePhoto = maxSizePhoto;
    }

    public long getMaxSizeVideo() {
        return maxSizeVideo;
    }

    public void setMaxSizeVideo(long maxSizeVideo) {
        this.maxSizeVideo = maxSizeVideo;
    }

    public Preview getPreview() {
        return preview;
    }

    public void setPreview(Preview preview) {
        this.preview = preview;
    }

    public int getMaxMediaPerProperty() {
        return maxMediaPerProperty;
    }

    public void setMaxMediaPerProperty(int maxMediaPerProperty) {
        this.maxMediaPerProperty = maxMediaPerProperty;
    }
}
