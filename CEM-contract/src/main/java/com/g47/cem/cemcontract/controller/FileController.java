package com.g47.cem.cemcontract.controller;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.g47.cem.cemcontract.dto.response.ApiResponse;
import com.g47.cem.cemcontract.exception.BusinessException;
import com.g47.cem.cemcontract.service.FileStorageService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for File Management
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
    
    private final FileStorageService fileStorageService;
    
    /**
     * Upload contract file
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("contractNumber") String contractNumber,
            HttpServletRequest request) {
        
        log.info("Uploading file for contract: {}", contractNumber);
        
        // Validate file
        if (file.isEmpty()) {
            throw new BusinessException("Please select a file to upload");
        }
        
        if (!fileStorageService.isValidContractFile(file)) {
            throw new BusinessException("Invalid file type. Only PDF, DOC, DOCX, JPG, JPEG, PNG files are allowed");
        }
        
        if (file.getSize() > fileStorageService.getMaxFileSize()) {
            throw new BusinessException("File size exceeds maximum limit of 10MB");
        }
        
        try {
            String fileName = fileStorageService.storeFile(file, contractNumber);
            
            log.info("File uploaded successfully: {}", fileName);
            
            return ResponseEntity.ok(ApiResponse.success(fileName, "File uploaded successfully"));
            
        } catch (Exception e) {
            log.error("Failed to upload file", e);
            throw new BusinessException("Could not upload file: " + e.getMessage());
        }
    }
    
    /**
     * Download contract file
     */
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileName,
            HttpServletRequest request) {
        
        log.info("Downloading file: {}", fileName);
        
        try {
            // Validate file exists
            if (!fileStorageService.fileExists(fileName)) {
                throw new BusinessException("File not found: " + fileName);
            }
            
            Path filePath = fileStorageService.getFilePath(fileName);
            Resource resource = new UrlResource(filePath.toUri());
            
            // Try to determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                log.info("Could not determine file type.");
            }
            
            // Fallback to the default content type if type could not be determined
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Failed to download file: {}", fileName, e);
            throw new BusinessException("Could not download file: " + e.getMessage());
        }
    }
    
    /**
     * Get file info
     */
    @GetMapping("/info/{fileName:.+}")
    public ResponseEntity<ApiResponse<Object>> getFileInfo(@PathVariable String fileName) {
        
        log.info("Getting file info: {}", fileName);
        
        try {
            if (!fileStorageService.fileExists(fileName)) {
                throw new BusinessException("File not found: " + fileName);
            }
            
            long fileSize = fileStorageService.getFileSize(fileName);
            
            var fileInfo = new java.util.HashMap<String, Object>();
            fileInfo.put("fileName", fileName);
            fileInfo.put("fileSize", fileSize);
            fileInfo.put("fileSizeFormatted", formatFileSize(fileSize));
            fileInfo.put("exists", true);
            
            return ResponseEntity.ok(ApiResponse.success(fileInfo, "File info retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Failed to get file info: {}", fileName, e);
            throw new BusinessException("Could not get file info: " + e.getMessage());
        }
    }
    
    /**
     * Delete file
     */
    @PostMapping("/delete/{fileName:.+}")
    public ResponseEntity<ApiResponse<String>> deleteFile(@PathVariable String fileName) {
        
        log.info("Deleting file: {}", fileName);
        
        try {
            if (!fileStorageService.fileExists(fileName)) {
                throw new BusinessException("File not found: " + fileName);
            }
            
            fileStorageService.deleteFile(fileName);
            
            return ResponseEntity.ok(ApiResponse.success(fileName, "File deleted successfully"));
            
        } catch (Exception e) {
            log.error("Failed to delete file: {}", fileName, e);
            throw new BusinessException("Could not delete file: " + e.getMessage());
        }
    }
    
    /**
     * Format file size in human readable format
     */
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        
        final String[] units = new String[] {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
} 