package interview.level4_java9to17.http_client;

// LEVEL: Senior
// =============================================================================
// INTERVIEW Q&A: Java 11 HttpClient API
// =============================================================================
//
// Q: "What replaced Apache HttpClient in modern Java?"
// A: "Java 11 introduced java.net.http.HttpClient as a built-in, modern HTTP
//     client. It supports HTTP/1.1 and HTTP/2, synchronous and asynchronous
//     requests, WebSocket, and reactive streams. It replaces both the legacy
//     HttpURLConnection and the need for Apache HttpClient/OkHttp in many cases."
//
// Q: "What are the key classes in the new HttpClient API?"
// A: "Three main classes: (1) HttpClient — the client instance, configured via
//     builder (connection timeout, HTTP version, redirect policy, executor).
//     (2) HttpRequest — the request, built with URI, method, headers, body.
//     (3) HttpResponse — the response, with status code, headers, body."
//
// Q: "How does async HTTP work with HttpClient?"
// A: "Use sendAsync() which returns CompletableFuture<HttpResponse>. You can
//     chain transformations with thenApply/thenAccept, combine multiple requests
//     with CompletableFuture.allOf, and handle errors with exceptionally()."
//
// Q: "Is HttpClient thread-safe?"
// A: "Yes. HttpClient is immutable and thread-safe. You should create one
//     instance and reuse it across threads. It manages its own connection pool."
//
// =============================================================================

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HttpClientDemo {

    // Shared client — thread-safe, reusable
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)         // prefer HTTP/2
            .connectTimeout(Duration.ofSeconds(10))     // connection timeout
            .followRedirects(HttpClient.Redirect.NORMAL) // follow redirects
            .build();

    // -------------------------------------------------------------------------
    // 1. Creating an HttpClient — builder pattern
    // -------------------------------------------------------------------------
    static void createClient() {
        System.out.println("=== 1. Creating HttpClient ===");

        // Simple client
        HttpClient simpleClient = HttpClient.newHttpClient();
        System.out.println("  Simple client: " + simpleClient);

        // Configured client (already created above as CLIENT)
        System.out.println("  Configured client version: " + CLIENT.version());
        System.out.println("  Connect timeout: " + CLIENT.connectTimeout().orElse(null));
        System.out.println("  Follow redirects: " + CLIENT.followRedirects());

        // Available configurations:
        System.out.println("\n  Builder options:");
        System.out.println("    .version(HTTP_1_1 | HTTP_2)");
        System.out.println("    .connectTimeout(Duration)");
        System.out.println("    .followRedirects(NEVER | NORMAL | ALWAYS)");
        System.out.println("    .proxy(ProxySelector)");
        System.out.println("    .authenticator(Authenticator)");
        System.out.println("    .executor(Executor)");
        System.out.println("    .sslContext(SSLContext)");
        System.out.println("    .cookieHandler(CookieHandler)");
    }

    // -------------------------------------------------------------------------
    // 2. Building HTTP Requests
    // -------------------------------------------------------------------------
    static void buildRequests() {
        System.out.println("\n=== 2. Building HTTP Requests ===");

        // GET request
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://httpbin.org/get"))
                .header("Accept", "application/json")
                .GET()  // default method
                .build();
        System.out.println("  GET: " + getRequest.uri());

        // POST request with JSON body
        String jsonBody = """
                {
                    "name": "Alice",
                    "email": "alice@example.com"
                }
                """;
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://httpbin.org/post"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(30))
                .build();
        System.out.println("  POST: " + postRequest.uri());
        System.out.println("  POST timeout: " + postRequest.timeout().orElse(null));

        // PUT request
        HttpRequest putRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://httpbin.org/put"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString("{\"id\": 1, \"name\": \"Updated\"}"))
                .build();
        System.out.println("  PUT: " + putRequest.uri());

        // DELETE request
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://httpbin.org/delete"))
                .DELETE()
                .build();
        System.out.println("  DELETE: " + deleteRequest.uri());

        // Body publishers
        System.out.println("\n  BodyPublishers:");
        System.out.println("    .ofString(String)       — for text/JSON");
        System.out.println("    .ofByteArray(byte[])    — for binary data");
        System.out.println("    .ofFile(Path)           — stream from file");
        System.out.println("    .ofInputStream(Supplier) — lazy stream");
        System.out.println("    .noBody()               — empty body");
    }

    // -------------------------------------------------------------------------
    // 3. Synchronous request
    // -------------------------------------------------------------------------
    static void synchronousRequest() {
        System.out.println("\n=== 3. Synchronous Request ===");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://httpbin.org/get?demo=java11"))
                .header("Accept", "application/json")
                .build();

        try {
            // send() blocks until response is received
            HttpResponse<String> response = CLIENT.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            System.out.println("  Status: " + response.statusCode());
            System.out.println("  Headers: " + response.headers().map().keySet());
            System.out.println("  Body length: " + response.body().length() + " chars");
            System.out.println("  Body (first 200 chars):");
            System.out.println("    " + response.body().substring(0, Math.min(200, response.body().length())));
        } catch (Exception e) {
            System.out.println("  Request failed (expected if no internet): " + e.getMessage());
            showFallbackDemo();
        }
    }

    // -------------------------------------------------------------------------
    // 4. Asynchronous request
    // -------------------------------------------------------------------------
    static void asynchronousRequest() {
        System.out.println("\n=== 4. Asynchronous Request ===");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://httpbin.org/delay/1"))
                .build();

        try {
            // sendAsync() returns CompletableFuture
            CompletableFuture<HttpResponse<String>> future = CLIENT.sendAsync(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            System.out.println("  Request sent (non-blocking)...");

            // Chain transformations
            CompletableFuture<Integer> statusFuture = future
                    .thenApply(HttpResponse::statusCode)
                    .exceptionally(e -> {
                        System.out.println("  Error: " + e.getMessage());
                        return -1;
                    });

            // Wait for result
            int status = statusFuture.join();
            System.out.println("  Async status: " + status);
        } catch (Exception e) {
            System.out.println("  Async request failed (expected if no internet): " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 5. Multiple concurrent requests
    // -------------------------------------------------------------------------
    static void concurrentRequests() {
        System.out.println("\n=== 5. Concurrent Requests ===");

        List<String> urls = List.of(
                "https://httpbin.org/get?id=1",
                "https://httpbin.org/get?id=2",
                "https://httpbin.org/get?id=3"
        );

        try {
            // Fire all requests concurrently
            List<CompletableFuture<HttpResponse<String>>> futures = urls.stream()
                    .map(url -> HttpRequest.newBuilder().uri(URI.create(url)).build())
                    .map(req -> CLIENT.sendAsync(req, HttpResponse.BodyHandlers.ofString()))
                    .toList();

            // Wait for all to complete
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

            // Collect results
            for (int i = 0; i < futures.size(); i++) {
                HttpResponse<String> resp = futures.get(i).join();
                System.out.println("  URL %d: status=%d, body=%d chars".formatted(
                        i + 1, resp.statusCode(), resp.body().length()));
            }
        } catch (Exception e) {
            System.out.println("  Concurrent requests failed (expected if no internet): " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 6. Response body handlers
    // -------------------------------------------------------------------------
    static void bodyHandlers() {
        System.out.println("\n=== 6. Response Body Handlers ===");

        System.out.println("  BodyHandlers available:");
        System.out.println("    .ofString()          — body as String");
        System.out.println("    .ofByteArray()       — body as byte[]");
        System.out.println("    .ofFile(Path)        — save to file");
        System.out.println("    .ofLines()           — body as Stream<String>");
        System.out.println("    .ofInputStream()     — body as InputStream");
        System.out.println("    .discarding()        — ignore body");
        System.out.println("    .ofString(Charset)   — body as String with charset");

        // Demonstrate discarding body (only care about status)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://httpbin.org/status/200"))
                .build();

        try {
            HttpResponse<Void> response = CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            System.out.println("\n  Discarding handler — status: " + response.statusCode());
        } catch (Exception e) {
            System.out.println("  Request failed (expected if no internet)");
        }
    }

    // -------------------------------------------------------------------------
    // 7. Before vs After comparison
    // -------------------------------------------------------------------------
    static void comparison() {
        System.out.println("\n=== 7. HttpURLConnection vs HttpClient ===");
        System.out.println("  ┌────────────────────┬────────────────────────┬─────────────────────────┐");
        System.out.println("  │ Feature            │ HttpURLConnection      │ HttpClient (Java 11)    │");
        System.out.println("  ├────────────────────┼────────────────────────┼─────────────────────────┤");
        System.out.println("  │ API style          │ Imperative, verbose    │ Builder, fluent         │");
        System.out.println("  │ HTTP/2             │ No                     │ Yes                     │");
        System.out.println("  │ Async              │ Manual threads         │ CompletableFuture       │");
        System.out.println("  │ WebSocket          │ No                     │ Yes                     │");
        System.out.println("  │ Immutability       │ Mutable                │ Immutable, thread-safe  │");
        System.out.println("  │ Timeouts           │ Awkward                │ Builder + per-request   │");
        System.out.println("  │ Body handling      │ InputStream only       │ Typed BodyHandlers      │");
        System.out.println("  │ Redirect           │ Manual                 │ Built-in policy         │");
        System.out.println("  └────────────────────┴────────────────────────┴─────────────────────────┘");

        System.out.println("\n  When to still use Apache HttpClient / OkHttp:");
        System.out.println("    - Connection pooling configuration");
        System.out.println("    - Retry policies");
        System.out.println("    - Interceptors / middleware");
        System.out.println("    - Multipart form uploads (not built into java.net.http)");
        System.out.println("    - Cookie management with persistence");
    }

    // -------------------------------------------------------------------------
    // Fallback demo when network is unavailable
    // -------------------------------------------------------------------------
    private static void showFallbackDemo() {
        System.out.println("\n  --- Offline Demo: Code Structure ---");
        System.out.println("  // Sync request:");
        System.out.println("  HttpResponse<String> resp = client.send(request, BodyHandlers.ofString());");
        System.out.println("  int status = resp.statusCode();");
        System.out.println("  String body = resp.body();");
        System.out.println();
        System.out.println("  // Async request:");
        System.out.println("  client.sendAsync(request, BodyHandlers.ofString())");
        System.out.println("      .thenApply(HttpResponse::body)");
        System.out.println("      .thenAccept(System.out::println)");
        System.out.println("      .exceptionally(e -> { log(e); return null; })");
        System.out.println("      .join();");
    }

    // -------------------------------------------------------------------------
    // 8. Common patterns and best practices
    // -------------------------------------------------------------------------
    static void bestPractices() {
        System.out.println("\n=== 8. Best Practices ===");

        System.out.println("  1. Reuse HttpClient — create once, share across threads");
        System.out.println("  2. Set connectTimeout on client, request timeout on each request");
        System.out.println("  3. Use sendAsync for multiple concurrent requests");
        System.out.println("  4. Use BodyHandlers.discarding() when you only need status code");
        System.out.println("  5. Use BodyHandlers.ofFile() for large downloads");
        System.out.println("  6. Handle CompletableFuture errors with exceptionally()");
        System.out.println("  7. Prefer HTTP/2 (automatic downgrade to 1.1 if not supported)");
        System.out.println("  8. Close the client in Java 21+ (implements AutoCloseable)");
    }

    // -------------------------------------------------------------------------
    // main
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   Java 11: HttpClient API                       ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        createClient();
        buildRequests();
        synchronousRequest();
        asynchronousRequest();
        concurrentRequests();
        bodyHandlers();
        comparison();
        bestPractices();
    }
}
