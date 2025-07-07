package com.g47.cem.cemcontract.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.g47.cem.cemcontract.exception.BusinessException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;

import lombok.extern.slf4j.Slf4j;

/**
 * Service wrapper around Google Drive API (v3) for uploading and generating download links.
 */
@Service
@Slf4j
public class GoogleDriveService {

    private final Drive drive;
    private final String folderId;

    public GoogleDriveService(
            @Value("${google-drive.credentials-path}") String credentialsPath,
            @Value("${google-drive.application-name:CEM Contract Service}") String applicationName,
            @Value("${google-drive.folder-id}") String folderId) {
        this.folderId = folderId;
        try {
            // Build GoogleCredential from service-account JSON file.
            InputStream credentialStream = resolveCredentialsStream(credentialsPath);
            GoogleCredential credential = GoogleCredential.fromStream(credentialStream)
                    .createScoped(Collections.singleton("https://www.googleapis.com/auth/drive"));

            this.drive = new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName(applicationName)
                    .build();
        } catch (Exception e) {
            throw new BusinessException("Failed to initialise Google Drive client", e);
        }
    }

    private InputStream resolveCredentialsStream(String credentialsPath) throws IOException {
        if (credentialsPath.startsWith("classpath:")) {
            String classpathLocation = credentialsPath.substring("classpath:".length());
            InputStream stream = getClass().getClassLoader().getResourceAsStream(classpathLocation);
            if (stream == null) {
                throw new IOException("Credentials file not found in classpath: " + classpathLocation);
            }
            return stream;
        }
        // Treat as filesystem path
        java.nio.file.Path path = java.nio.file.Paths.get(credentialsPath);
        return java.nio.file.Files.newInputStream(path);
    }

    /**
     * Upload a MultipartFile to Google Drive inside the configured folder.
     * Returns the fileId of the uploaded file.
     */
    public String upload(MultipartFile multipartFile, String desiredName) {
        try {
            String originalFilename = multipartFile.getOriginalFilename();
            String fileName = desiredName != null ? desiredName : originalFilename;

            AbstractInputStreamContent mediaContent = new InputStreamContent(
                    multipartFile.getContentType(), multipartFile.getInputStream());

            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            if (folderId != null && folderId.length() > 20 && !folderId.contains(" ")) {
                fileMetadata.setParents(Collections.singletonList(folderId));
            }

            File uploadedFile = drive.files().create(fileMetadata, mediaContent)
                    .setFields("id, webViewLink, webContentLink")
                    .execute();

            // Make file readable for anyone with link
            Permission permission = new Permission();
            permission.setType("anyone");
            permission.setRole("reader");
            drive.permissions().create(uploadedFile.getId(), permission).execute();

            log.info("Uploaded file to Google Drive. id={}, name={}", uploadedFile.getId(), fileName);
            return uploadedFile.getId();
        } catch (Exception e) {
            throw new BusinessException("Failed to upload file to Google Drive", e);
        }
    }

    /**
     * Upload a File (not MultipartFile) to Google Drive
     */
    public String uploadFile(java.io.File file, String desiredName) {
        try {
            String fileName = desiredName != null ? desiredName : file.getName();

            com.google.api.client.http.AbstractInputStreamContent mediaContent = 
                new com.google.api.client.http.FileContent("application/pdf", file);

            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            if (folderId != null && folderId.length() > 20 && !folderId.contains(" ")) {
                fileMetadata.setParents(Collections.singletonList(folderId));
            }

            File uploadedFile = drive.files().create(fileMetadata, mediaContent)
                    .setFields("id, webViewLink, webContentLink")
                    .execute();

            // Make file readable for anyone with link
            Permission permission = new Permission();
            permission.setType("anyone");
            permission.setRole("reader");
            drive.permissions().create(uploadedFile.getId(), permission).execute();

            log.info("Uploaded file to Google Drive. id={}, name={}", uploadedFile.getId(), fileName);
            return uploadedFile.getId();
        } catch (Exception e) {
            throw new BusinessException("Failed to upload file to Google Drive", e);
        }
    }

    /**
     * Generate a public download URL (direct download) for the given Google Drive file ID.
     */
    public String getDownloadUrl(String fileId) {
        return "https://drive.google.com/uc?export=download&id=" + fileId;
    }

    /**
     * Download file content from Google Drive as byte array
     */
    public byte[] downloadFileContent(String fileId) {
        try {
            InputStream inputStream = drive.files().get(fileId).executeMediaAsInputStream();
            return inputStream.readAllBytes();
        } catch (Exception e) {
            log.error("Failed to download file content from Google Drive for fileId: {}", fileId, e);
            throw new BusinessException("Failed to download file from Google Drive", e);
        }
    }

    /**
     * Get file metadata from Google Drive
     */
    public File getFileMetadata(String fileId) {
        try {
            return drive.files().get(fileId).setFields("id, name, mimeType, size").execute();
        } catch (Exception e) {
            log.error("Failed to get file metadata from Google Drive for fileId: {}", fileId, e);
            throw new BusinessException("Failed to get file metadata from Google Drive", e);
        }
    }
} 