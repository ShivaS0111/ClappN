package biz.craftline.server.smoke;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class StagingHttp {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final String baseUrl;

    public StagingHttp(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public record Response(int status, String body, Map<String, String> headers) {}

    public Response get(String path, String bearer, Long storeId, Long businessId) throws Exception {
        return exchange("GET", path, bearer, storeId, businessId, null);
    }

    public Response post(String path, String bearer, Long storeId, Long businessId, String jsonBody) throws Exception {
        return exchange("POST", path, bearer, storeId, businessId, jsonBody);
    }

    public Response postRaw(String path, String jsonBody, Map<String, String> extraHeaders) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
        if (extraHeaders != null) {
            extraHeaders.forEach(b::header);
        }
        HttpRequest req = b.POST(HttpRequest.BodyPublishers.ofString(jsonBody != null ? jsonBody : "")).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return new Response(resp.statusCode(), resp.body(), Map.of());
    }

    public Response exchange(String method, String path, String bearer, Long storeId, Long businessId, String jsonBody)
            throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json");
        if (bearer != null && !bearer.isBlank()) {
            b.header("Authorization", "Bearer " + bearer);
        }
        if (storeId != null) {
            b.header("X-Store-Id", String.valueOf(storeId));
        }
        if (businessId != null) {
            b.header("X-Business-Id", String.valueOf(businessId));
        }
        if (jsonBody != null) {
            b.header("Content-Type", "application/json");
            b.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
        } else {
            b.method(method, HttpRequest.BodyPublishers.noBody());
        }
        HttpResponse<String> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofString());
        return new Response(resp.statusCode(), resp.body(), Map.of());
    }
}
