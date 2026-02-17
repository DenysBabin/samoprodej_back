package samoprodej.samoprodej.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import samoprodej.samoprodej.config.FileStorageConfig;
import samoprodej.samoprodej.enums.ErrorCode;
import samoprodej.samoprodej.enums.MediaType;
import samoprodej.samoprodej.exception.BusinessException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageConfig config;

    public String saveFile(MultipartFile file, UUID propertyId, UUID mediaId, MediaType mediaType) throws IOException {
        validateFile(file, mediaType);

        Path propertyDir = Paths.get(config.getPath(), propertyId.toString());
        Path mediaDir = propertyDir.resolve(mediaId.toString());
        Files.createDirectories(mediaDir);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = "media" + extension;
        Path filePath = mediaDir.resolve(filename);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String relativePath = Paths.get(config.getPath(), propertyId.toString(), mediaId.toString(), filename).toString();
        log.info("File saved: {}", relativePath);
        return "/" + relativePath.replace("\\", "/");
    }

    public String generatePreview(MultipartFile imageFile, UUID propertyId, UUID mediaId) throws IOException {
        Path propertyDir = Paths.get(config.getPath(), propertyId.toString());
        Path mediaDir = propertyDir.resolve(mediaId.toString());
        Files.createDirectories(mediaDir);

        String originalFilename = imageFile.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String previewFilename = "preview" + extension;
        Path previewPath = mediaDir.resolve(previewFilename);

        Thumbnails.of(imageFile.getInputStream())
                .size(config.getPreview().getMaxWidth(), config.getPreview().getMaxHeight())
                .keepAspectRatio(true)
                .toFile(previewPath.toFile());

        String relativePath = Paths.get(config.getPath(), propertyId.toString(), mediaId.toString(), previewFilename).toString();
        log.info("Preview generated: {}", relativePath);
        return "/" + relativePath.replace("\\", "/");
    }

    public String generatePreviewFromFile(String filePath, UUID propertyId, UUID mediaId) throws IOException {
        Path propertyDir = Paths.get(config.getPath(), propertyId.toString());
        Path mediaDir = propertyDir.resolve(mediaId.toString());
        Files.createDirectories(mediaDir);

        Path sourceFilePath = Paths.get(filePath.startsWith("/") ? filePath.substring(1) : filePath);
        if (!Files.exists(sourceFilePath)) {
            throw new IOException("Source file not found: " + filePath);
        }

        String extension = getFileExtension(sourceFilePath.getFileName().toString());
        String previewFilename = "preview" + extension;
        Path previewPath = mediaDir.resolve(previewFilename);

        Thumbnails.of(sourceFilePath.toFile())
                .size(config.getPreview().getMaxWidth(), config.getPreview().getMaxHeight())
                .keepAspectRatio(true)
                .toFile(previewPath.toFile());

        String relativePath = Paths.get(config.getPath(), propertyId.toString(), mediaId.toString(), previewFilename).toString();
        log.info("Preview generated from file: {}", relativePath);
        return "/" + relativePath.replace("\\", "/");
    }

    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        try {
            Path path = Paths.get(filePath.startsWith("/") ? filePath.substring(1) : filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("File deleted: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Error deleting file: {}", filePath, e);
        }
    }

    public void deleteMediaDirectory(UUID propertyId, UUID mediaId) {
        Path mediaDir = Paths.get(config.getPath(), propertyId.toString(), mediaId.toString());
        try {
            if (Files.exists(mediaDir)) {
                Files.walk(mediaDir)
                        .sorted((a, b) -> b.compareTo(a))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.error("Error deleting file: {}", path, e);
                            }
                        });
                log.info("Media directory deleted: {}", mediaDir);
            }
        } catch (IOException e) {
            log.error("Error deleting media directory: {}", mediaDir, e);
        }
    }

    public String getFileUrl(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        return filePath.startsWith("/") ? filePath : "/" + filePath;
    }

    private void validateFile(MultipartFile file, MediaType mediaType) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "File is empty");
        }

        long maxSize = mediaType == MediaType.PHOTO ? config.getMaxSizePhoto() : config.getMaxSizeVideo();
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "File size exceeds maximum allowed size: ...");
        }

        String contentType = file.getContentType();
        if (!isValidFileType(contentType, mediaType)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Invalid file type for ...");
        }
    }

    private boolean isValidFileType(String contentType, MediaType mediaType) {
        if (contentType == null) {
            return false;
        }

        return switch (mediaType) {
            case PHOTO -> contentType.startsWith("image/") &&
                    (contentType.equals("image/jpeg") || contentType.equals("image/jpg") ||
                     contentType.equals("image/png") || contentType.equals("image/webp"));
            case VIDEO -> contentType.startsWith("video/") &&
                    (contentType.equals("video/mp4") || contentType.equals("video/webm"));
            case TOUR3D -> contentType.startsWith("application/") || contentType.startsWith("model/");
        };
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
