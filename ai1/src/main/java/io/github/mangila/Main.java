package io.github.mangila;

import org.intellij.lang.annotations.Language;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

    private static final URI OLLAMA_HOST = URI.create("http://127.0.0.1:11434");
    private static final List<String> MODELS = List.of("llama3.2", "qwen2.5:0.5b", "deepseek-r1:1.5b");
    private static final HttpClient OLLAMA_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .version(HttpClient.Version.HTTP_2)
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .build();

    static void main() {
        pullModels();
        String menu = "";
        for (int i = 1; i <= MODELS.size(); i++) {
            menu += "%d. %s\n".formatted(i, MODELS.get(i - 1));
        }
        IO.println("""
                ========================================
                       OLLAMA JAVA CHAT CLIENT         
                ========================================
                %s
                Type 'exit' to quit
                ----------------------------------------
                """.formatted(menu));
        final String decision = IO.readln("> ");
        if (decision.equalsIgnoreCase("exit")) {
            Thread.currentThread().interrupt();
            return;
        }
        final String model = MODELS.get(Integer.parseInt(decision) - 1);
        IO.println("Chatting with model: %s".formatted(model));
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            final BlockingQueue<String> request = new ArrayBlockingQueue<>(1);
            final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);
            final ChatSession session = new ChatSession(model, request, response);
            final Future<?> worker = executor.submit(session);
            while (!Thread.currentThread().isInterrupted()) {
                String prompt = IO.readln("> ");
                if (prompt.equalsIgnoreCase("exit")) {
                    Thread.currentThread().interrupt();
                    worker.cancel(true);
                    break;
                }
                final boolean ok = request.offer(prompt);
                if (!ok) {
                    IO.println("Queue is full");
                    continue;
                }
                final String modelResponse = response.take();
                IO.println(modelResponse);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Main Thread Interrupted");
        }
    }

    private static void pullModels() {
        IO.println("Pulling models, this might take some time...");
        List<CompletableFuture<Void>> downloads = new ArrayList<>();
        for (String model : MODELS) {
            @Language("JSON") final String body = """
                    {
                    "model" : "%s",
                    "stream" : false
                    }
                    """.formatted(model);
            final HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .uri(OLLAMA_HOST.resolve("/api/pull"))
                    .build();
            var future = OLLAMA_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(IO::println);
            downloads.add(future);
        }
        CompletableFuture.allOf(downloads.toArray(new CompletableFuture[0])).join();
    }

    public static class ChatSession implements Runnable {

        private final String model;
        private final BlockingQueue<String> request;
        private final BlockingQueue<String> response;

        public ChatSession(String model, BlockingQueue<String> request, BlockingQueue<String> response) {
            this.model = model;
            this.request = request;
            this.response = response;
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    final String prompt = request.take();
                    @Language("JSON") final String body = """
                            {
                            "model" : "%s",
                            "prompt" : "%s",
                            "stream" : false
                            }
                            """.formatted(model, prompt);
                    final HttpRequest request = HttpRequest.newBuilder()
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .uri(OLLAMA_HOST.resolve("/api/generate"))
                            .build();
                    final String modelResponse = OLLAMA_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                            .thenApply(HttpResponse::body)
                            .join();
                    response.put(modelResponse);
                }
            } catch (InterruptedException _) {
                IO.println("Session canceled");
                Thread.currentThread().interrupt();
            }
        }

    }

}
