package com.shop.shop.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Service
public class R2StorageService {

    private S3Client s3Client;

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.s3.endpoint}")
    private String endpoint;

    @Value("${r2.bucket-name}")
    private String bucketName;

    @Value("${r2.public-url}")
    private String publicUrl;

    @PostConstruct
    public void init() {
        // AWS 자격증명
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        // Cloudflare R2 권장 설정:
        // - pathStyleAccessEnabled(true) : Cloudflare 예제와 일치
        // - chunkedEncodingEnabled(false) : aws-chunked 서명 사용 안함 -> SignatureDoesNotMatch 회피
        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .chunkedEncodingEnabled(false)   // <-- 중요: 청크 인코딩 비활성화
                .build();

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))   // 예: https://<account_id>.r2.cloudflarestorage.com
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("auto"))                // <-- 권장: "auto"
                .serviceConfiguration(serviceConfiguration)
                .build();
    }


    public String uploadFile(MultipartFile file, String directory) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = directory + "/" + UUID.randomUUID() + fileExtension;

        byte[] bytes = file.getBytes();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

        return publicUrl + "/" + fileName;
    }

    public void deleteFile(String fileUrl) {
        String prefix = endpoint + "/" + bucketName + "/";
        if (!fileUrl.startsWith(prefix)) {
            // 안전장치: URL 형식 예상과 다르면 바로 처리하지 않음
            throw new IllegalArgumentException("잘못된 파일 URL입니다.");
        }
        String key = fileUrl.substring(prefix.length());

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }
}

