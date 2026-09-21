package biz.craftline.server.thorough;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Seeds + aggregates all slice JSON reports into the combined 26-section QA report.
 * Does not re-run probe suites — run Auth/CRUD/Template/Indirect/Tenant/Authz ITs first
 * (or in the same Maven {@code -Dtest=} list).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("thorough")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("thorough")
class ServerSideQaReportIT {

    @Autowired ThoroughFeatureSeedService seedService;

    @Test
    void writeCombinedAuthorizationReport() throws Exception {
        ThoroughFeatureSeedService.SeedSnapshot seed = seedService.seed();
        ServerSideQaAuthorizationReporter.writeCombined(seed);
        assertTrue(Files.exists(Path.of("reports", "server-side-qa-authorization-report.md")),
                "combined markdown report missing");
        assertTrue(Files.exists(Path.of("reports", "server-side-qa-authorization-report.json")),
                "combined json report missing");
    }
}
