package com.freetowear.infra;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Base64;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final List<String> PRODUCT_IMAGE_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/svg+xml"
    );
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    public String uploadPublic(MultipartFile file, String folder) throws IOException {
        validate(file);
        return upload(file, folder, "upload");
    }

    public String uploadProductImage(MultipartFile file, String folder) throws IOException {
        validate(file, PRODUCT_IMAGE_TYPES);
        return upload(file, folder, "upload");
    }

    private String upload(MultipartFile file, String folder, String type) throws IOException {
        Map<String, Object> options = Map.of(
                "folder", folder,
                "type", type,
                "resource_type", "image",
                "unique_filename", true,
                "overwrite", false
        );
        Map result = cloudinary.uploader().upload(file.getBytes(), options);
        return (String) result.get("public_id");
    }

    public String uploadPrivate(MultipartFile file, String folder) throws IOException {
        validate(file);
        Map<String, Object> options = Map.of(
                "folder", folder,
                "type", "authenticated",
                "resource_type", "image",
                "unique_filename", true,
                "overwrite", false
        );
        Map result = cloudinary.uploader().upload(file.getBytes(), options);
        return (String) result.get("public_id");
    }

    public String uploadPrivateDataUrl(String dataUrl, String folder) throws IOException {
        if (dataUrl == null || !dataUrl.startsWith("data:image/")) {
            throw new IllegalArgumentException("Invalid image data");
        }
        int separator = dataUrl.indexOf(",");
        if (separator < 0) throw new IllegalArgumentException("Invalid image data");
        String contentType = dataUrl.substring(5, dataUrl.indexOf(';'));
        if (!ALLOWED_TYPES.contains(contentType)) throw new IllegalArgumentException("Format not allowed: " + contentType);
        byte[] bytes = Base64.getDecoder().decode(dataUrl.substring(separator + 1));
        if (bytes.length > MAX_SIZE) throw new IllegalArgumentException("File exceeds 5MB limit");
        Map<String, Object> options = Map.of("folder", folder, "type", "authenticated",
                "resource_type", "image", "unique_filename", true, "overwrite", false);
        Map result = cloudinary.uploader().upload(bytes, options);
        return (String) result.get("public_id");
    }

    private void validate(MultipartFile file) {
        validate(file, ALLOWED_TYPES);
    }

    private void validate(MultipartFile file, List<String> allowedTypes) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("Image is required");
        if (!allowedTypes.contains(file.getContentType()))
            throw new IllegalArgumentException("Format not allowed: " + file.getContentType());
        if (file.getSize() > MAX_SIZE)
            throw new IllegalArgumentException("File exceeds 5MB limit");
    }

    public String buildUrl(String publicId) {
        if (publicId != null && (publicId.startsWith("data:image/") || publicId.startsWith("http://")
                || publicId.startsWith("https://"))) {
            return publicId;
        }
        return cloudinary.url()
                .transformation(new Transformation().width(800).crop("limit"))
                .generate(publicId);
    }

    public String buildPrivateUrl(String publicId) {
        return cloudinary.url()
                .type("authenticated")
                .transformation(new Transformation().width(800).crop("limit"))
                .signed(true)
                .generate(publicId);
    }

    public void delete(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, Map.of());
    }
}
