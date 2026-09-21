package biz.craftline.server.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService service;

    @BeforeEach
    void setUp() {
        service = new FileStorageService();
        ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString());
        service.init();
    }

    @Test
    void storeFile_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("f", "pic.png", "image/png", "bytes".getBytes());
        String url = service.storeFile(file);
        assertTrue(url.startsWith("/uploads/"));
        assertTrue(Files.exists(tempDir.resolve(url.replace("/uploads/", ""))));
    }

    @Test
    void storeFile_rejectsEmptyAndNonImage() {
        MockMultipartFile empty = new MockMultipartFile("f", "x.png", "image/png", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> service.storeFile(empty));

        MockMultipartFile pdf = new MockMultipartFile("f", "doc.pdf", "application/pdf", "x".getBytes());
        assertThrows(IllegalArgumentException.class, () -> service.storeFile(pdf));
    }

    @Test
    void storeFiles_multiple() throws Exception {
        MockMultipartFile f1 = new MockMultipartFile("f1", "a.jpg", "image/jpeg", "1".getBytes());
        MockMultipartFile f2 = new MockMultipartFile("f2", "b.jpg", "image/jpeg", "2".getBytes());
        assertEquals(2, service.storeFiles(new MockMultipartFile[]{f1, f2}).size());
    }

    @Test
    void deleteFile_variants() throws Exception {
        MockMultipartFile file = new MockMultipartFile("f", "pic.png", "image/png", "bytes".getBytes());
        String url = service.storeFile(file);
        assertTrue(service.deleteFile(url));
        assertFalse(service.deleteFile(null));
        assertFalse(service.deleteFile("/other/path"));
        assertFalse(service.deleteFile("/uploads/missing-file.png"));
    }
}
