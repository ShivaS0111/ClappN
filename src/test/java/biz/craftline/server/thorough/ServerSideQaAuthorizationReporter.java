package biz.craftline.server.thorough;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes {@code reports/server-side-qa-authorization-report.md} with the 26-section outline.
 * Aggregates slice JSON reports via {@link #writeCombined}.
 */
final class ServerSideQaAuthorizationReporter {

    private static final String[] SLICE_FILES = {
            "full-api-role-authz-report.json",
            "tenant-isolation-report.json",
            "auth-full-report.json",
            "crud-idor-report.json",
            "template-catalog-admin-report.json",
            "indirect-integrity-report.json"
    };

    private ServerSideQaAuthorizationReporter() {}

    /**
     * Legacy entry used by TenantIsolationIT before writeCombined existed.
     * Delegates to {@link #writeCombined}.
     */
    static void write(
            ThoroughFeatureSeedService.SeedSnapshot seed,
            int tenantPass,
            int tenantFail,
            List<Map<String, Object>> tenantRows) throws Exception {
        writeCombined(seed);
    }

    /**
     * Reads all known slice report JSON files under {@code reports/} and writes the combined
     * 26-section markdown. Overall PASS only when required suites exist and every executed
     * suite has fail=0; FAIL if any fail&gt;0; otherwise PARTIAL.
     */
    static void writeCombined(ThoroughFeatureSeedService.SeedSnapshot seed) throws Exception {
        Path reportsDir = Path.of("reports");
        Files.createDirectories(reportsDir);
        ObjectMapper mapper = new ObjectMapper();

        Map<String, JsonNode> reports = new LinkedHashMap<>();
        for (String name : SLICE_FILES) {
            Path p = reportsDir.resolve(name);
            if (Files.exists(p)) {
                reports.put(name, mapper.readTree(p.toFile()));
            }
        }

        JsonNode authz = reports.get("full-api-role-authz-report.json");
        JsonNode tenant = reports.get("tenant-isolation-report.json");
        JsonNode auth = reports.get("auth-full-report.json");
        JsonNode crud = reports.get("crud-idor-report.json");
        JsonNode template = reports.get("template-catalog-admin-report.json");
        JsonNode indirect = reports.get("indirect-integrity-report.json");

        // Required for overall PASS: auth + crud + template + indirect reports exist
        boolean requiredPresent = auth != null && crud != null && template != null && indirect != null;
        int anyFail = 0;
        List<String> executed = new ArrayList<>();
        for (Map.Entry<String, JsonNode> e : reports.entrySet()) {
            executed.add(e.getKey());
            anyFail += e.getValue().path("summary").path("fail").asInt(0);
            if ("full-api-role-authz-report.json".equals(e.getKey())
                    && e.getValue().path("summary").has("authzOk")
                    && !e.getValue().path("summary").path("authzOk").asBoolean(true)) {
                anyFail += 1;
            }
        }

        // PASS: all executed suites fail=0 AND auth+crud+template+indirect exist
        // FAIL: any fail>0; otherwise PARTIAL (or PENDING if nothing executed)
        String overall;
        if (executed.isEmpty()) {
            overall = "PENDING";
        } else if (anyFail > 0) {
            overall = "FAIL";
        } else if (requiredPresent) {
            overall = "PASS";
        } else {
            overall = "PARTIAL";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("# SERVER-SIDE QA & AUTHORIZATION REPORT\n\n");
        sb.append("Generated: ").append(Instant.now()).append("\n\n");
        sb.append("**Overall status: ").append(overall).append("**\n\n");

        // 1
        sb.append("## 1. Executive Summary\n\n");
        sb.append("Combined thorough authorization suite aggregating slice reports under `reports/`.\n\n");
        appendSliceLine(sb, "Permission matrix", authz);
        appendSliceLine(sb, "Tenant isolation", tenant);
        appendSliceLine(sb, "Authentication", auth);
        appendSliceLine(sb, "CRUD / IDOR / Products", crud);
        appendSliceLine(sb, "Template / Catalog / Admin", template);
        appendSliceLine(sb, "Indirect integrity", indirect);
        sb.append("- Overall: **").append(overall).append("**\n\n");

        // 2
        sb.append("## 2. Test Environment\n\n");
        sb.append("- Profile: `thorough`\n");
        sb.append("- Stack: Spring Boot + MockMvc + JWT login\n");
        sb.append("- Password (seed users): `Test@12345`\n\n");

        // 3
        sb.append("## 3. Test Dataset\n\n");
        if (seed != null) {
            sb.append("```text\n");
            sb.append("Business A (").append(ThoroughFeatureSeedService.BUSINESS_A_NAME).append(") id=")
                    .append(seed.getBusinessId()).append("\n");
            sb.append("  Store A1 (").append(ThoroughFeatureSeedService.STORE_A).append(") id=")
                    .append(seed.getStoreAId()).append("\n");
            sb.append("  Store A2 (").append(ThoroughFeatureSeedService.STORE_B).append(") id=")
                    .append(seed.getStoreBId()).append("\n");
            sb.append("  Store A3 (").append(ThoroughFeatureSeedService.STORE_A3).append(") id=")
                    .append(seed.getStoreA3Id()).append("\n");
            sb.append("Business B (").append(ThoroughFeatureSeedService.BUSINESS_B_NAME).append(") id=")
                    .append(seed.getBusinessBId()).append("\n");
            sb.append("  Store B1 (").append(ThoroughFeatureSeedService.STORE_B1).append(") id=")
                    .append(seed.getStoreB1Id()).append("\n");
            sb.append("  Store B2 (").append(ThoroughFeatureSeedService.STORE_B2).append(") id=")
                    .append(seed.getStoreB2Id()).append("\n");
            sb.append("```\n\n");
            sb.append("Special users:\n\n");
            sb.append("| Key | Email | User id |\n|---|---|---|\n");
            if (seed.getSpecialEmails() != null) {
                for (Map.Entry<String, String> e : seed.getSpecialEmails().entrySet()) {
                    Long id = seed.getSpecialUserIds() != null
                            ? seed.getSpecialUserIds().get(e.getKey()) : null;
                    sb.append("| `").append(e.getKey()).append("` | ")
                            .append(e.getValue()).append(" | ")
                            .append(id).append(" |\n");
                }
            }
            sb.append("\n");
        } else {
            sb.append("_Seed snapshot unavailable_\n\n");
        }

        // 4
        sb.append("## 4. User / Role / Permission Matrix\n\n");
        if (authz != null) {
            JsonNode s = authz.path("summary");
            sb.append("From FullApiRoleAuthzIT: pass=").append(s.path("pass").asInt())
                    .append(", fail=").append(s.path("fail").asInt())
                    .append(", warn=").append(s.path("warn").asInt())
                    .append(". See `reports/full-api-role-authz-report.md`.\n\n");
        } else {
            sb.append("**PENDING** — run FullApiRoleAuthzIT.\n\n");
        }

        // 5
        sb.append("## 5. Membership / Employee Scope Matrix\n\n");
        sb.append("| Special user | Role | Business | Store scopes |\n|---|---|---|---|\n");
        sb.append("| scope.business_a | BUSINESS_OWNER | A | empty (= all A stores) |\n");
        sb.append("| scope.store_a1 | STORE_MANAGER | A | A1 only |\n");
        sb.append("| scope.store_a1_a2 | STORE_MANAGER | A | A1+A2 |\n");
        sb.append("| scope.business_b | BUSINESS_OWNER | B | empty (= all B stores) |\n");
        sb.append("| scope.none | (none) | — | no membership |\n");
        sb.append("| scope.inactive | BUSINESS_OWNER | A | INACTIVE membership |\n");
        sb.append("| auth.disabled | (none) | — | enabled=false |\n\n");

        // 6
        sb.append("## 6. Endpoint Coverage\n\n");
        sb.append("Permission matrix (FullApiRoleAuthzIT), tenant isolation, auth full, ");
        sb.append("CRUD/IDOR/products, template/catalog/admin, and indirect integrity slices.\n\n");

        // 7 Auth
        sb.append("## 7. Authentication Results\n\n");
        appendSectionFromReport(sb, auth, "auth-full-report");

        // 8
        sb.append("## 8. Authorization Results\n\n");
        if (authz != null && authz.path("summary").path("authzOk").asBoolean(false)) {
            sb.append("**PASS** (executed permission matrix) — authzOk=true.\n\n");
        } else if (authz != null && failOf(authz) == 0) {
            sb.append("**PASS** — permission matrix fail=0.\n\n");
        } else if (authz != null) {
            sb.append("**FAIL** or incomplete — see full-api-role-authz-report.\n\n");
        } else {
            sb.append("**PENDING**\n\n");
        }

        // 9–11 tenant
        sb.append("## 9. Tenant Isolation Results\n\n");
        appendSectionFromReport(sb, tenant, "tenant-isolation-report");

        sb.append("## 10. Business-Level Access Results\n\n");
        sb.append(tenant != null
                ? "Covered by TenantIsolationIT (business_a / business_b special users). See section 9.\n\n"
                : "**PENDING**\n\n");

        sb.append("## 11. Store-Level Access Results\n\n");
        sb.append(tenant != null
                ? "Covered by TenantIsolationIT (store_a1 / store_a1_a2 / X-Store-Id). See section 9.\n\n"
                : "**PENDING**\n\n");

        // 12 CRUD, 13 Products from crud report
        sb.append("## 12. CRUD Results\n\n");
        appendFiltered(sb, crud, "CRUD", "PERMISSION");

        sb.append("## 13. Product Results\n\n");
        appendFiltered(sb, crud, "PRODUCT");

        sb.append("## 14. Service Results\n\n");
        sb.append("**PENDING** — dedicated service-template mutation suite not in this run.\n\n");

        // 15 Templates, 16 Admin
        sb.append("## 15. Template / Catalog Results\n\n");
        appendFiltered(sb, template, "SHARED_CATALOG");
        if (template != null && template.has("notes")) {
            sb.append("Notes:\n");
            for (JsonNode n : template.get("notes")) {
                sb.append("- ").append(n.asText()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("## 16. Admin-Only API Results\n\n");
        appendFiltered(sb, template, "ADMIN");

        // 17 IDOR
        sb.append("## 17. IDOR Results\n\n");
        appendFiltered(sb, crud, "IDOR");

        // 18 Indirect
        sb.append("## 18. Indirect Authorization Results\n\n");
        appendSectionFromReport(sb, indirect, "indirect-integrity-report");

        // 19
        sb.append("## 19. Validation Results\n\n");
        sb.append("Exercised via empty POST bodies and cross-tenant order payloads ");
        sb.append("(expect 4xx not 500). See CRUD / Indirect reports.\n\n");

        // 20 Integrity
        sb.append("## 20. Data Integrity Results\n\n");
        appendFiltered(sb, crud, "INTEGRITY");
        if (indirect != null) {
            sb.append("Also see IndirectIntegrityIT mutation denies.\n\n");
        }

        // 21
        sb.append("## 21. Bugs / Findings\n\n");
        List<String> findingLines = new ArrayList<>();
        for (Map.Entry<String, JsonNode> e : reports.entrySet()) {
            JsonNode failures = e.getValue().path("failures");
            if (!failures.isArray()) continue;
            for (JsonNode f : failures) {
                findingLines.add("| " + e.getKey()
                        + " | " + f.path("probe").asText()
                        + " | " + f.path("httpStatus").asInt()
                        + " | " + f.path("detail").asText().replace('|', '/') + " |");
            }
        }
        if (findingLines.isEmpty()) {
            sb.append("_No failures in executed slices._\n\n");
        } else {
            sb.append("| Slice | Probe | HTTP | Detail |\n|---|---|---|---|\n");
            for (String line : findingLines) sb.append(line).append("\n");
            sb.append("\n");
        }

        sb.append("## 22. Security Findings\n\n");
        sb.append("### Finding categories\n\n");
        sb.append("| Code | Name | Question |\n|---|---|---|\n");
        sb.append("| A | Tenant security | Prevented from other business/store tenant data? |\n");
        sb.append("| B | Permission security | Has required permission for the operation? |\n");
        sb.append("| C | Shared catalog authorization | Allowed to use global / business-type catalog items? |\n\n");

        sb.append("## 23. Regression Findings\n\n");
        sb.append("**PENDING** — compare against prior thorough reports when available.\n\n");

        // 24
        sb.append("## 24. Coverage Statistics\n\n");
        sb.append("| Slice | Status | Pass | Fail |\n|---|---|---|---|\n");
        coverageRow(sb, "Permission matrix (FullApiRoleAuthzIT)", authz);
        coverageRow(sb, "Tenant + business/store scope (TenantIsolationIT)", tenant);
        coverageRow(sb, "Authentication (AuthFullIT)", auth);
        coverageRow(sb, "CRUD / IDOR / Products (CrudIdorProductsIT)", crud);
        coverageRow(sb, "Template / Catalog / Admin (TemplateCatalogAdminIT)", template);
        coverageRow(sb, "Indirect integrity (IndirectIntegrityIT)", indirect);
        sb.append("\n");

        // 25
        sb.append("## 25. Recommendations\n\n");
        sb.append("1. Keep tagged thorough ITs green in CI.\n");
        sb.append("2. Classify findings A/B/C before treating shared catalog access as a tenant leak.\n");
        sb.append("3. Extend service-template mutation coverage when ready.\n\n");

        // 26
        sb.append("## 26. Final PASS / FAIL Assessment\n\n");
        sb.append("| Executed slice | Result |\n|---|---|\n");
        finalRow(sb, "Permission matrix", authz);
        finalRow(sb, "Tenant isolation", tenant);
        finalRow(sb, "Authentication", auth);
        finalRow(sb, "CRUD / IDOR / Products", crud);
        finalRow(sb, "Template / Catalog / Admin", template);
        finalRow(sb, "Indirect integrity", indirect);
        sb.append("| **Overall** | **").append(overall).append("** |\n");

        Files.writeString(reportsDir.resolve("server-side-qa-authorization-report.md"), sb.toString());

        // Lightweight JSON companion
        ObjectNode meta = mapper.createObjectNode();
        meta.put("overall", overall);
        meta.put("generatedAt", Instant.now().toString());
        meta.put("anyFail", anyFail);
        meta.put("requiredPresent", requiredPresent);
        var slices = meta.putObject("slices");
        for (String name : SLICE_FILES) {
            JsonNode n = reports.get(name);
            var s = slices.putObject(name);
            s.put("present", n != null);
            if (n != null) {
                s.put("pass", n.path("summary").path("pass").asInt());
                s.put("fail", n.path("summary").path("fail").asInt());
                s.put("skip", n.path("summary").path("skip").asInt(0));
            }
        }
        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(reportsDir.resolve("server-side-qa-authorization-report.json").toFile(), meta);
    }

    private static int failOf(JsonNode report) {
        return report == null ? 0 : report.path("summary").path("fail").asInt(0);
    }

    private static void appendSliceLine(StringBuilder sb, String label, JsonNode report) {
        if (report == null) {
            sb.append("- ").append(label).append(": _missing_\n");
        } else {
            JsonNode s = report.path("summary");
            sb.append("- ").append(label).append(": pass=").append(s.path("pass").asInt())
                    .append(", fail=").append(s.path("fail").asInt())
                    .append(", skip=").append(s.path("skip").asInt(0)).append("\n");
        }
    }

    private static void appendSectionFromReport(StringBuilder sb, JsonNode report, String name) {
        if (report == null) {
            sb.append("**PENDING** — run suite that writes `reports/").append(name).append(".json`.\n\n");
            return;
        }
        JsonNode s = report.path("summary");
        String status = s.path("fail").asInt(0) == 0 ? "PASS" : "FAIL";
        sb.append("**").append(status).append("** — pass=").append(s.path("pass").asInt())
                .append(", fail=").append(s.path("fail").asInt())
                .append(", skip=").append(s.path("skip").asInt(0))
                .append(", warn=").append(s.path("warn").asInt(0)).append("\n\n");
        appendResultTable(sb, report.path("results"), null, 40);
    }

    private static void appendFiltered(StringBuilder sb, JsonNode report, String... categories) {
        if (report == null) {
            sb.append("**PENDING**\n\n");
            return;
        }
        JsonNode results = report.path("results");
        int shown = appendResultTable(sb, results, categories, 50);
        if (shown == 0) {
            // Fall back to whole summary if category tags differ
            JsonNode s = report.path("summary");
            sb.append("(no rows matched filter; suite summary pass=")
                    .append(s.path("pass").asInt())
                    .append(" fail=").append(s.path("fail").asInt()).append(")\n\n");
        } else {
            sb.append("\n");
        }
    }

    private static int appendResultTable(StringBuilder sb, JsonNode results, String[] categories, int max) {
        sb.append("| Verdict | User | Probe | HTTP |\n|---|---|---|---|\n");
        int n = 0;
        if (results != null && results.isArray()) {
            for (JsonNode r : results) {
                if (categories != null && categories.length > 0) {
                    String cat = r.path("category").asText("");
                    boolean match = false;
                    for (String c : categories) {
                        if (cat.equalsIgnoreCase(c) || cat.toUpperCase().contains(c.toUpperCase())) {
                            match = true;
                            break;
                        }
                    }
                    if (!match) continue;
                }
                if (n++ >= max) {
                    sb.append("| … | … | truncated | … |\n");
                    break;
                }
                sb.append("| ").append(r.path("verdict").asText())
                        .append(" | ").append(r.path("user").asText())
                        .append(" | ").append(r.path("probe").asText())
                        .append(" | ").append(r.path("httpStatus").asInt())
                        .append(" |\n");
            }
        }
        if (n == 0) {
            sb.append("| — | — | _no rows_ | — |\n");
        }
        return n;
    }

    private static void coverageRow(StringBuilder sb, String label, JsonNode report) {
        if (report == null) {
            sb.append("| ").append(label).append(" | PENDING | — | — |\n");
        } else {
            JsonNode s = report.path("summary");
            String st = s.path("fail").asInt(0) == 0 ? "EXECUTED/PASS" : "EXECUTED/FAIL";
            sb.append("| ").append(label).append(" | ").append(st)
                    .append(" | ").append(s.path("pass").asInt())
                    .append(" | ").append(s.path("fail").asInt()).append(" |\n");
        }
    }

    private static void finalRow(StringBuilder sb, String label, JsonNode report) {
        if (report == null) {
            sb.append("| ").append(label).append(" | PENDING |\n");
        } else {
            sb.append("| ").append(label).append(" | ")
                    .append(failOf(report) == 0 ? "PASS" : "FAIL").append(" |\n");
        }
    }
}
