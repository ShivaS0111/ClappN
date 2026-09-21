package biz.craftline.server.thorough;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Full authentication suite (QA area 1 / report section 7).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("thorough")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("thorough")
class AuthFullIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ThoroughFeatureSeedService seedService;

    private ThoroughFeatureSeedService.SeedSnapshot seed;
    private final List<ObjectNode> rows = new ArrayList<>();

    @BeforeAll
    void setup() throws Exception {
        seed = seedService.seed();
        assertNotNull(seed.getSpecialEmails());
        assertNotNull(seed.getSpecialEmails().get(ThoroughFeatureSeedService.SPECIAL_DISABLED),
                "disabled user must be seeded");
    }

    @Test
    void authenticationFullSuite() throws Exception {
        String validEmail = ThoroughFeatureSeedService.emailForRole("STORE_MANAGER");
        if (seed.getSpecialEmails().containsKey(ThoroughFeatureSeedService.SPECIAL_STORE_A1)) {
            validEmail = seed.getSpecialEmails().get(ThoroughFeatureSeedService.SPECIAL_STORE_A1);
        }

        // Valid login → 200 + token
        MvcResult loginOk = loginRaw(validEmail, ThoroughFeatureSeedService.PASSWORD);
        int loginStatus = loginOk.getResponse().getStatus();
        String loginBody = loginOk.getResponse().getContentAsString();
        String token = extractToken(loginBody);
        String refresh = extractRefresh(loginBody);
        boolean loginPass = loginStatus == 200 && token != null && !token.isBlank();
        rows.add(row("valid-user", "Valid login → 200 + token", "POST", "/api/auth/login",
                "AUTH", loginPass ? "PASS" : "FAIL", loginStatus,
                "tokenPresent=" + (token != null) + " | " + ThoroughSliceReportHelper.truncate(loginBody)));

        // Invalid password → 401
        MvcResult badPw = loginRaw(validEmail, "WrongPassword!!");
        int badPwStatus = badPw.getResponse().getStatus();
        rows.add(row("valid-user", "Invalid password → 401", "POST", "/api/auth/login",
                "AUTH", badPwStatus == 401 ? "PASS" : "FAIL", badPwStatus,
                ThoroughSliceReportHelper.truncate(badPw.getResponse().getContentAsString())));

        // Unknown user → 401
        MvcResult unknown = loginRaw("thorough.nobody.exists@clapp.test", ThoroughFeatureSeedService.PASSWORD);
        int unkStatus = unknown.getResponse().getStatus();
        rows.add(row("unknown", "Unknown user → 401", "POST", "/api/auth/login",
                "AUTH", unkStatus == 401 ? "PASS" : "FAIL", unkStatus,
                ThoroughSliceReportHelper.truncate(unknown.getResponse().getContentAsString())));

        // No Authorization header
        MvcResult noAuth = mockMvc.perform(get("/api/stores/list").accept(MediaType.APPLICATION_JSON))
                .andReturn();
        int noAuthStatus = noAuth.getResponse().getStatus();
        rows.add(row("anonymous", "No Authorization → 401", "GET", "/api/stores/list",
                "AUTH", noAuthStatus == 401 ? "PASS" : "FAIL", noAuthStatus,
                ThoroughSliceReportHelper.truncate(noAuth.getResponse().getContentAsString())));

        // Garbage Bearer
        MvcResult garbage = mockMvc.perform(get("/api/stores/list")
                        .header("Authorization", "Bearer not.a.real.jwt.token")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        int garbageStatus = garbage.getResponse().getStatus();
        rows.add(row("garbage", "Garbage Bearer → 401", "GET", "/api/stores/list",
                "AUTH", garbageStatus == 401 ? "PASS" : "FAIL", garbageStatus,
                ThoroughSliceReportHelper.truncate(garbage.getResponse().getContentAsString())));

        // Disabled user
        String disabledEmail = seed.getSpecialEmails().get(ThoroughFeatureSeedService.SPECIAL_DISABLED);
        MvcResult disabled = loginRaw(disabledEmail, ThoroughFeatureSeedService.PASSWORD);
        int disabledStatus = disabled.getResponse().getStatus();
        boolean disabledOk = disabledStatus == 401 || disabledStatus == 403;
        rows.add(row(ThoroughFeatureSeedService.SPECIAL_DISABLED,
                "Disabled user login → 401/403", "POST", "/api/auth/login",
                "AUTH", disabledOk ? "PASS" : "FAIL", disabledStatus,
                ThoroughSliceReportHelper.truncate(disabled.getResponse().getContentAsString())));

        // Logout with refresh token (or SKIP + fake body)
        if (refresh != null && !refresh.isBlank()) {
            ObjectNode logoutBody = objectMapper.createObjectNode();
            logoutBody.put("refreshToken", refresh);
            MvcResult logout = mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(logoutBody.toString()))
                    .andReturn();
            int logoutStatus = logout.getResponse().getStatus();
            boolean logoutPass = logoutStatus >= 200 && logoutStatus < 300;
            rows.add(row("valid-user", "Logout with refreshToken", "POST", "/api/auth/logout",
                    "AUTH", logoutPass ? "PASS" : "FAIL", logoutStatus,
                    ThoroughSliceReportHelper.truncate(logout.getResponse().getContentAsString())));
        } else {
            rows.add(row("valid-user", "Logout with refreshToken", "POST", "/api/auth/logout",
                    "AUTH", "SKIP", 0, "refreshToken not returned by login"));
            ObjectNode fake = objectMapper.createObjectNode();
            fake.put("refreshToken", "");
            MvcResult fakeLogout = mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(fake.toString()))
                    .andReturn();
            int fakeStatus = fakeLogout.getResponse().getStatus();
            rows.add(row("anonymous", "Logout fake/empty body → 4xx not 500", "POST", "/api/auth/logout",
                    "AUTH", ThoroughSliceReportHelper.judgeClientError(fakeStatus), fakeStatus,
                    ThoroughSliceReportHelper.truncate(fakeLogout.getResponse().getContentAsString())));
        }

        // Always also probe invalid logout body for 4xx (not 500)
        ObjectNode blankLogout = objectMapper.createObjectNode();
        blankLogout.put("refreshToken", "");
        MvcResult blank = mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(blankLogout.toString()))
                .andReturn();
        int blankStatus = blank.getResponse().getStatus();
        rows.add(row("anonymous", "Logout blank refreshToken → 4xx", "POST", "/api/auth/logout",
                "AUTH", ThoroughSliceReportHelper.judgeClientError(blankStatus), blankStatus,
                ThoroughSliceReportHelper.truncate(blank.getResponse().getContentAsString())));

        // After login, GET /api/me/context
        // Re-login in case logout revoked session side-effects (JWT still valid until expiry)
        MvcResult reLogin = loginRaw(validEmail, ThoroughFeatureSeedService.PASSWORD);
        String token2 = extractToken(reLogin.getResponse().getContentAsString());
        if (token2 == null || token2.isBlank()) {
            rows.add(row("valid-user", "GET /api/me/context after login", "GET", "/api/me/context",
                    "AUTH", "FAIL", reLogin.getResponse().getStatus(), "re-login failed"));
        } else {
            MvcResult me = mockMvc.perform(get("/api/me/context")
                            .header("Authorization", "Bearer " + token2)
                            .accept(MediaType.APPLICATION_JSON))
                    .andReturn();
            int meStatus = me.getResponse().getStatus();
            rows.add(row("valid-user", "GET /api/me/context after login → 200", "GET", "/api/me/context",
                    "AUTH", meStatus == 200 ? "PASS" : "FAIL", meStatus,
                    ThoroughSliceReportHelper.truncate(me.getResponse().getContentAsString())));
        }

        // Password change optional SKIP
        rows.add(row("", "Password change", "POST", "/api/auth/change-password",
                "AUTH", "SKIP", 0, "optional — skipped (complex flow)"));

        ThoroughSliceReportHelper.Counts counts = ThoroughSliceReportHelper.count(rows);
        ObjectNode report = ThoroughSliceReportHelper.baseReport(objectMapper,
                "ClappN Auth Full Report", "1 Authentication", counts);
        ArrayNode notes = report.putArray("notes");
        notes.add("Password change: SKIP (optional)");
        notes.add("Disabled user email: " + ThoroughFeatureSeedService.DISABLED_EMAIL);
        ThoroughSliceReportHelper.writeFiles(objectMapper, "auth-full-report", report, rows);

        ServerSideQaAuthorizationReporter.writeCombined(seed);

        assertTrue(counts.fail() == 0,
                "AuthFullIT FAILs must be zero, got " + counts.fail());
    }

    private MvcResult loginRaw(String email, String password) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("username", email);
        body.put("password", password);
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andReturn();
    }

    private String extractToken(String body) {
        try {
            JsonNode token = objectMapper.readTree(body).path("data").path("tokenInfo").path("token");
            return token.isMissingNode() || token.isNull() ? null : token.asText();
        } catch (Exception e) {
            return null;
        }
    }

    private String extractRefresh(String body) {
        try {
            JsonNode refresh = objectMapper.readTree(body).path("data").path("tokenInfo").path("refreshToken");
            return refresh.isMissingNode() || refresh.isNull() ? null : refresh.asText();
        } catch (Exception e) {
            return null;
        }
    }

    private ObjectNode row(String user, String probe, String method, String path,
                           String category, String verdict, int status, String detail) {
        return ThoroughSliceReportHelper.row(objectMapper, user, probe, method, path,
                category, verdict, status, detail);
    }
}
