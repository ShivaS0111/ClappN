package biz.craftline.server.thorough;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;

/**
 * Shared JSON/MD report writer for thorough slice ITs.
 */
final class ThoroughSliceReportHelper {

    private ThoroughSliceReportHelper() {}

    record Counts(int pass, int fail, int warn, int skip, int total) {}

    static Counts count(List<ObjectNode> rows) {
        int pass = 0, fail = 0, warn = 0, skip = 0;
        for (ObjectNode r : rows) {
            switch (r.path("verdict").asText()) {
                case "PASS" -> pass++;
                case "FAIL" -> fail++;
                case "SKIP" -> skip++;
                default -> warn++;
            }
        }
        return new Counts(pass, fail, warn, skip, rows.size());
    }

    static ObjectNode baseReport(ObjectMapper om, String title, String category, Counts c) {
        ObjectNode report = om.createObjectNode();
        report.put("title", title);
        report.put("generatedAt", Instant.now().toString());
        if (category != null) {
            report.put("category", category);
        }
        ObjectNode summary = report.putObject("summary");
        summary.put("pass", c.pass());
        summary.put("fail", c.fail());
        summary.put("warn", c.warn());
        summary.put("skip", c.skip());
        summary.put("total", c.total());
        return report;
    }

    static void writeFiles(ObjectMapper om, String baseName, ObjectNode report, List<ObjectNode> rows)
            throws Exception {
        ArrayNode results = report.putArray("results");
        results.addAll(rows);
        ArrayNode failures = report.putArray("failures");
        rows.stream().filter(r -> "FAIL".equals(r.path("verdict").asText())).forEach(failures::add);

        Path outJson = Path.of("target", baseName + ".json");
        Path outMd = Path.of("target", baseName + ".md");
        Path reportsDir = Path.of("reports");
        Files.createDirectories(outJson.getParent());
        Files.createDirectories(reportsDir);
        om.writerWithDefaultPrettyPrinter().writeValue(outJson.toFile(), report);
        Files.writeString(outMd, toMarkdown(report));
        Files.copy(outJson, reportsDir.resolve(baseName + ".json"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(outMd, reportsDir.resolve(baseName + ".md"), StandardCopyOption.REPLACE_EXISTING);
    }

    static String toMarkdown(ObjectNode report) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(report.path("title").asText("Report")).append("\n\n");
        sb.append("Generated: ").append(report.path("generatedAt").asText()).append("\n\n");
        if (report.hasNonNull("category")) {
            sb.append("Category: ").append(report.path("category").asText()).append("\n\n");
        }
        if (report.has("notes")) {
            sb.append("## Notes\n\n");
            for (JsonNode n : report.get("notes")) {
                sb.append("- ").append(n.asText()).append("\n");
            }
            sb.append("\n");
        }
        JsonNode s = report.get("summary");
        sb.append("## Summary\n\n");
        sb.append("| Metric | Value |\n|---|---|\n");
        sb.append("| Pass | ").append(s.path("pass").asInt()).append(" |\n");
        sb.append("| Fail | ").append(s.path("fail").asInt()).append(" |\n");
        sb.append("| Warn | ").append(s.path("warn").asInt()).append(" |\n");
        sb.append("| Skip | ").append(s.path("skip").asInt()).append(" |\n");
        sb.append("| Total | ").append(s.path("total").asInt()).append(" |\n\n");

        sb.append("## Failures\n\n");
        JsonNode failures = report.get("failures");
        if (failures == null || !failures.isArray() || failures.isEmpty()) {
            sb.append("_None_\n");
        } else {
            sb.append("| Probe | HTTP | Detail |\n|---|---|---|\n");
            for (JsonNode f : failures) {
                sb.append("| ").append(f.path("probe").asText())
                        .append(" | ").append(f.path("httpStatus").asInt())
                        .append(" | ").append(f.path("detail").asText().replace('|', '/'))
                        .append(" |\n");
            }
        }
        return sb.toString();
    }

    static ObjectNode row(ObjectMapper om, String user, String probe, String method, String path,
                          String category, String verdict, int status, String detail) {
        ObjectNode n = om.createObjectNode();
        n.put("user", user == null ? "" : user);
        n.put("probe", probe);
        n.put("method", method == null ? "" : method);
        n.put("path", path == null ? "" : path);
        n.put("category", category == null ? "" : category);
        n.put("verdict", verdict);
        n.put("httpStatus", status);
        n.put("detail", detail == null ? "" : detail);
        return n;
    }

    static String truncate(String s) {
        if (s == null) return "";
        String t = s.replace('\n', ' ');
        return t.length() > 160 ? t.substring(0, 160) + "…" : t;
    }

    /** Allow gate: 2xx–4xx except when expecting deny (401/403). */
    static String judge(boolean expectAllow, int status) {
        boolean denied = status == 401 || status == 403;
        boolean allowedGate = status >= 200 && status < 500;
        if (status == 500 || status == 0) return "WARN";
        if (expectAllow) {
            return allowedGate && !denied ? "PASS" : (denied ? "FAIL" : "WARN");
        }
        return denied ? "PASS" : (allowedGate ? "FAIL" : "WARN");
    }

    /** Pass if status is 4xx (client error), fail on 5xx or 2xx when deny-ish expected. */
    static String judgeClientError(int status) {
        if (status == 500 || status == 0) return "WARN";
        if (status >= 400 && status < 500) return "PASS";
        return "FAIL";
    }

    static String judgeDenyOrClientError(int status) {
        if (status == 500 || status == 0) return "WARN";
        if (status == 401 || status == 403 || (status >= 400 && status < 500)) return "PASS";
        return "FAIL";
    }
}
