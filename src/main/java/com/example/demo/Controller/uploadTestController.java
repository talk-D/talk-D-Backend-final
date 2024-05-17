package com.example.demo.Controller;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Bucket;
import java.io.InputStream;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.StorageOptions;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;


@Controller
public class uploadTestController {
    @Value("${spring.cloud.gcp.storage.credentials.location}")
    private String keyFileName;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String gcsBucketName ;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile multipartFile) {
        try (InputStream keyFile = ResourceUtils.getURL(keyFileName).openStream()) {
            // Google Cloud Storage 서비스 객체 생성
            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(keyFile))
                    .build()
                    .getService();

            // 파일이 비어 있는지 확인
            if (multipartFile.isEmpty()) {
                return null;
            }

            // UUID로 파일 이름 생성
            //String uuid = UUID.randomUUID().toString();
            String ext = multipartFile.getContentType();
            String fileName = "commonIcoTheme.png";
            //String fileName = uuid + "." + getFileExtension(multipartFile.getOriginalFilename());


            // Google Cloud Storage에 파일 업로드
            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(gcsBucketName , "default/Images/" + fileName))
                    .setContentType(ext)
                    .build();
            Blob blob = storage.create(blobInfo, multipartFile.getInputStream());

            // Blob 객체가 성공적으로 생성되었으면 URL 반환
            if (blob != null) {
                return "https://storage.googleapis.com/" + gcsBucketName  + "/default/Images/" + fileName;
            } else {
                return "Error: Failed to upload file";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error uploading file: " + e.getMessage();
        }
    }

    // 파일 이름에서 확장자 추출하는 메서드
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : fileName.substring(lastDotIndex + 1);
    }

    @PostMapping("/makeThemeZip")
    public ResponseEntity<Map<String, String>> makeThemeZip() {
        Map<String, String> response = new HashMap<>();
        String BUCKET_NAME = "talkdimg";
        String FOLDER_NAME = "default";
        String zipFileName = FOLDER_NAME + ".zip";
        String kthemeFileName = FOLDER_NAME + ".ktheme";
        try  (InputStream keyFile = ResourceUtils.getURL(keyFileName).openStream()){
            // Initialize Google Cloud Storage

                Storage storage = StorageOptions.newBuilder()
                        .setCredentials(GoogleCredentials.fromStream(keyFile))
                        .build()
                        .getService();


            //Storage storage = StorageOptions.getDefaultInstance().getService();

            // Create a ByteArrayOutputStream to store the zip file contents
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);

            List<Blob> blobs = new ArrayList<>();
            Iterable<Blob> blobIterable = storage.list(BUCKET_NAME, Storage.BlobListOption.prefix(FOLDER_NAME + "/")).iterateAll();
            blobIterable.forEach(blobs::add);

            for (Blob blob : blobs) {
                if (!blob.isDirectory()) {
                    String entryName = blob.getName().substring(FOLDER_NAME.length() + 1); // Extract relative path
                    ZipEntry zipEntry = new ZipEntry(entryName);
                    zos.putNextEntry(zipEntry);
                    zos.write(blob.getContent());
                    zos.closeEntry();
                }
            }
            // Finish writing the zip file
            zos.close();
            baos.close();

            // Convert the ByteArrayOutputStream to a byte array
            byte[] zipContents = baos.toByteArray();

            // Create a BlobId for the zip file
            BlobId blobId = BlobId.of(BUCKET_NAME, zipFileName);

            // Create a BlobInfo for the zip file
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/zip").build();

            // Upload the zip file to Google Cloud Storage
            storage.create(blobInfo, zipContents);

            System.out.println("Zip file created and uploaded successfully.");

            // .zip -> .ktheme 확장자 변경



            response.put("message", "압축 완료!");
        } catch (IOException e) {
            e.printStackTrace();
            response.put("error", "압축 실패");
        }

        return ResponseEntity.ok(response);
    }



}