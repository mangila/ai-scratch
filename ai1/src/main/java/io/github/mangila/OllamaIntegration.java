package io.github.mangila;

import com.google.gson.JsonObject;

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
        final List<CompletableFuture<Void>> downloads = new ArrayList<>();
        for (String model : models) {
            IO.println("Pulling model: " + model);
            final JsonObject payload = new JsonObject();
            payload.addProperty("model", model);
            payload.addProperty("stream", false);
            final HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .header("Content-Type", "application/json")
                    .uri(OLLAMA_HOST.resolve("/api/pull"))
                    .build();
            CompletableFuture<Void> future = OLLAMA_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(IO::println);
            downloads.add(future);
        }
        CompletableFuture.allOf(downloads.toArray(new CompletableFuture[0])).join();
    }

    static CompletableFuture<String> chat(String payload) {
        final HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .header("Content-Type", "application/json")
                .header("Accept", "application/x-ndjson")
                .uri(OLLAMA_HOST.resolve("/api/chat"))
                .build();
        return OLLAMA_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

}
