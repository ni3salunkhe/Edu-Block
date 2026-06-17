package com.edu.util;

import org.springframework.stereotype.Component;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Component
public class FileDownloadHelper {

    public File downloadFromUrl(String fileUrl) throws Exception {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException("File URL cannot be empty");
        }
        URL url = new URL(fileUrl);
        File tempFile = File.createTempFile("edublock_download_", ".pdf");
        try (InputStream in = url.openStream()) {
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }
}
