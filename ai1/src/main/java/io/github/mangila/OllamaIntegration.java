package io.github.mangila;

import com.google.gson.JsonObject;
import org.intellij.lang.annotations.Language;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

class OllamaIntegration {

    private static final URI OLLAMA_HOST = URI.create("http://127.0.0.1:11434");
    private static final HttpClient OLLAMA_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .version(HttpClient.Version.HTTP_2)
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .build();

    private OllamaIntegration() {
        throw new UnsupportedOperationException("Utility class");
    }

    static void pullModels(List<String> models) {
        IO.println("Pulling models, this might take some time...");
        final List<CompletableFuture<JsonObject>> downloads = new ArrayList<>();
        for (String model : models) {
            IO.println("Pulling model: %s".formatted(model));
            final JsonObject payload = new JsonObject();
            payload.addProperty("model", model);
            payload.addProperty("stream", false);
            final CompletableFuture<JsonObject> future = pull(payload.toString());
            downloads.add(future);
        }
        CompletableFuture.allOf(downloads.toArray(new CompletableFuture[0])).join();
    }

    static CompletableFuture<JsonObject> show(@Language("JSON") String json) {
        final HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(OLLAMA_HOST.resolve("/api/show"))
                .build();
        return OLLAMA_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(JsonUtil::fromJson);
    }

    static CompletableFuture<JsonObject> chat(@Language("JSON") String json) {
        final HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .header("Accept", "application/x-ndjson")
                .uri(OLLAMA_HOST.resolve("/api/chat"))
                .build();
        return OLLAMA_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(JsonUtil::fromJson);
    }

    static CompletableFuture<JsonObject> pull(@Language("JSON") String json) {
        final HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .uri(OLLAMA_HOST.resolve("/api/pull"))
                .build();
        return OLLAMA_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(JsonUtil::fromJson);
    }

}
