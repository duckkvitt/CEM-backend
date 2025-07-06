package com.g47.cem.cemcontract.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.g47.cem.cemcontract.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for handling file storage operations for contracts
 */
@Service
@Slf4j
public class FileStorageService {
    
    private final Path fileStorageLocation;
    
    public FileStorageService(@Value("${app.file.upload-dir:./uploads/contracts}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new BusinessException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }
    
    /**
     * Store uploaded file and return the file path
     */
    public String storeFile(MultipartFile file, String contractNumber) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new BusinessException("Sorry! Filename contains invalid path sequence " + fileName);
            }
            
            // Generate unique filename with contract number
            String fileExtension = getFileExtension(fileName);
            String uniqueFileName = contractNumber + "_" + UUID.randomUUID().toString() + fileExtension;
            
            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("File stored successfully: {}", uniqueFileName);
            return uniqueFileName;
            
        } catch (IOException ex) {
            throw new BusinessException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }
    
    /**
     * Store signature file (base64 data)
     */
    public String storeSignatureFile(String base64Data, String contractNumber, String signerName) {
        try {
            // Decode base64 data
            byte[] data = java.util.Base64.getDecoder().decode(base64Data);
            
            // Generate unique filename for signature
            String uniqueFileName = contractNumber + "_signature_" + 
                    signerName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + 
                    UUID.randomUUID().toString() + ".png";
            
            // Save file
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.write(targetLocation, data);
            
            log.info("Signature file stored successfully: {}", uniqueFileName);
            return uniqueFileName;
            
        } catch (Exception ex) {
            throw new BusinessException("Could not store signature file. Please try again!", ex);
        }
    }
    
    /**
     * Get file path for reading
     */
    public Path getFilePath(String fileName) {
        return fileStorageLocation.resolve(fileName).normalize();
    }
    
    public Path loadFileAsPath(String fileName) {
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
        if (Files.exists(filePath)) {
            return filePath;
        } else {
            throw new BusinessException("File not found " + fileName);
        }
    }

    /**
     * Delete a file
     */
    public void deleteFile(String fileName) {
        try {
            Path filePath = fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted successfully: {}", fileName);
        } catch (IOException ex) {
            log.error("Could not delete file: {}", fileName, ex);
        }
    }
    
    /**
     * Check if file exists
     */
    public boolean fileExists(String fileName) {
        Path filePath = fileStorageLocation.resolve(fileName).normalize();
        return Files.exists(filePath);
    }
    
    /**
     * Get file size
     */
    public long getFileSize(String fileName) {
        try {
            Path filePath = fileStorageLocation.resolve(fileName).normalize();
            return Files.size(filePath);
        } catch (IOException ex) {
            log.error("Could not get file size: {}", fileName, ex);
            return 0;
        }
    }
    
    /**
     * Validate file type for contracts
     */
    public boolean isValidContractFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) return false;
        
        String extension = getFileExtension(fileName).toLowerCase();
        return extension.equals(".pdf") || 
               extension.equals(".doc") || 
               extension.equals(".docx") ||
               extension.equals(".jpg") ||
               extension.equals(".jpeg") ||
               extension.equals(".png");
    }
    
    /**
     * Get maximum allowed file size (in bytes)
     */
    public long getMaxFileSize() {
        return 10 * 1024 * 1024; // 10MB
    }
    
    private String getFileExtension(String fileName) {
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf("."));
        } else {
            return "";
        }
    }
}