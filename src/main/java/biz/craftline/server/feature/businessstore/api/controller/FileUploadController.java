package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.config.security.RequirePermission;
import biz.craftline.server.util.APIResponse;
import biz.craftline.server.util.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * REST controller for file upload operations.
 * Supports single and multiple image uploads for banners and galleries.
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    /**
     * Upload a single image file.
     * Returns the URL of the uploaded file.
     */
    @Operation(summary = "Upload a single image", description = "Uploads a single image file and returns its URL.")
    @ApiResponse(responseCode = "200", description = "File uploaded successfully.")
    @PostMapping("/upload")
    @RequirePermission("file.upload")
    public ResponseEntity<APIResponse<Map<String, String>>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        try {
            String url = fileStorageService.storeFile(file);
            return APIResponse.success(
                    Map.of("url", url, "originalName", file.getOriginalFilename() != null ? file.getOriginalFilename() : ""),
                    "File uploaded successfully"
            );
        } catch (IllegalArgumentException e) {
            return APIResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            log.error("File upload failed", e);
            return APIResponse.error("File upload failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Upload multiple image files.
     * Returns a list of URLs for the uploaded files.
     */
    @Operation(summary = "Upload multiple images", description = "Uploads multiple image files and returns their URLs.")
    @ApiResponse(responseCode = "200", description = "Files uploaded successfully.")
    @PostMapping("/upload-multiple")
    @RequirePermission("file.upload")
    public ResponseEntity<APIResponse<Map<String, Object>>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files) {
        try {
            List<String> urls = fileStorageService.storeFiles(files);
            return APIResponse.success(
                    Map.of("urls", urls, "count", urls.size()),
                    "Files uploaded successfully"
            );
        } catch (IllegalArgumentException e) {
            return APIResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            log.error("Multiple file upload failed", e);
            return APIResponse.error("File upload failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete a previously uploaded file.
     */
    @Operation(summary = "Delete an uploaded file", description = "Deletes a file by its URL path.")
    @ApiResponse(responseCode = "200", description = "File deleted successfully.")
    @DeleteMapping
    @RequirePermission("file.delete")
    public ResponseEntity<APIResponse<String>> deleteFile(@RequestParam("url") String url) {
        boolean deleted = fileStorageService.deleteFile(url);
        if (deleted) {
            return APIResponse.success("File deleted successfully");
        } else {
            return APIResponse.error("File not found or could not be deleted", HttpStatus.NOT_FOUND);
        }
    }
}

