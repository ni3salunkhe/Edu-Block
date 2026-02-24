package com.edu.servicesImpl;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.edu.services.StorageService;

@Service
public class CloudinaryStorageServiceImpl implements StorageService {

    private final Cloudinary cloudinary;

    public CloudinaryStorageServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Cloudinary getCloudinary() {
        return cloudinary;
    }

    @Override
    public String upload(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }

            // Use "raw" for PDFs so Cloudinary serves them with the correct content-type.
            // Use "image" for image types. "auto" can misclassify PDFs as images.
            String resourceType = extension.equals(".pdf") ? "raw" : "image";

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", resourceType,
                // Do NOT include the extension in public_id — Cloudinary appends it automatically,
                // causing a double extension (e.g. uuid.pdf.pdf) if you include it here.
                "public_id", "edublock/" + UUID.randomUUID().toString()
            ));

            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary upload failed", e);
        }
    }

    @Override
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary delete failed", e);
        }
    }
}
