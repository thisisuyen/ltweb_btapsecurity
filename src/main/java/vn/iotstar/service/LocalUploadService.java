package vn.iotstar.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class LocalUploadService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.url-prefix:/uploads}")
    private String urlPrefix;

    public String saveImage(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) return null;

        String original = file.getOriginalFilename();
        String ext = StringUtils.getFilenameExtension(original);
        if (ext == null) ext = "png";

        String filename = UUID.randomUUID() + "." + ext;

        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path folder = (subFolder == null || subFolder.isBlank()) ? base : base.resolve(subFolder);
        try {
            Files.createDirectories(folder);
            Path target = folder.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Upload local failed: " + e.getMessage(), e);
        }

        String prefix = urlPrefix.startsWith("/") ? urlPrefix : ("/" + urlPrefix);
        if (!prefix.endsWith("/")) prefix += "/";

        String sub = (subFolder == null) ? "" : subFolder.trim();
        sub = sub.replace("\\", "/");
        if (!sub.isBlank() && !sub.endsWith("/")) sub += "/";

        return prefix + sub + filename; 
    }

    public void deleteByUrl(String url) {
    }
}