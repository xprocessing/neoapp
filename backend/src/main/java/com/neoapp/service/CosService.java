package com.neoapp.service;

import com.neoapp.config.CosProperties;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CosService {

    private final CosProperties cosProperties;

    private COSClient cosClient;

    @PostConstruct
    public void init() {
        COSCredentials cred = new BasicCOSCredentials(cosProperties.getSecretId(), cosProperties.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosProperties.getRegion()));
        this.cosClient = new COSClient(cred, clientConfig);
        log.debug("COS client initialized for bucket: {}", cosProperties.getBucket());
    }

    @PreDestroy
    public void destroy() {
        if (cosClient != null) {
            cosClient.shutdown();
            log.debug("COS client shutdown");
        }
    }

    /**
     * 上传头像图片到 COS
     * @param file  上传的图片文件
     * @param userId 用户ID，用作目录前缀
     * @return COS 访问 URL
     */
    public String uploadAvatar(MultipartFile file, Long userId) {
        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedImageType(contentType)) {
            throw new IllegalArgumentException("不支持的图片格式，仅支持 JPG、PNG、GIF、WebP");
        }

        // 校验文件大小（最大 5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("图片大小不能超过 5MB");
        }

        // 生成对象 Key: avatar/{userId}/{uuid}.{ext}
        String ext = getExtension(file.getOriginalFilename(), contentType);
        String key = "avatar/" + userId + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;

        // 上传
        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(file.getSize());

            PutObjectRequest putRequest = new PutObjectRequest(
                cosProperties.getBucket(), key, inputStream, metadata
            );
            cosClient.putObject(putRequest);

            log.debug("Avatar uploaded: key={}, size={}", key, file.getSize());
        } catch (Exception e) {
            log.error("COS upload failed for user: {}", userId, e);
            throw new RuntimeException("头像上传失败，请稍后重试", e);
        }

        // 返回完整访问 URL
        return "https://" + cosProperties.getBucket() + ".cos." + cosProperties.getRegion() + ".myqcloud.com/" + key;
    }

    private boolean isAllowedImageType(String contentType) {
        return "image/jpeg".equals(contentType)
            || "image/png".equals(contentType)
            || "image/gif".equals(contentType)
            || "image/webp".equals(contentType);
    }

    private String getExtension(String filename, String contentType) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        }
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}
