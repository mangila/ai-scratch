package io.github.mangila;

import com.google.gson.JsonArray;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Main {

    static final BlockingQueue<String> POISON_PILL = new ArrayBlockingQueue<>(1);

    static void main() {
        final List<String> models = List.of("llama3.2", "qwen2.5:0.5b", "deepseek-r1:1.5b");
        OllamaIntegration.pullModels(models);
        final JsonArray tools = Tools.loadTools();
            try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
                while (true) {
                    final Optional<String> modelOptional = IOUtil.modelDecisionDialog(models);
                    if (modelOptional.isEmpty()) {
                        break;
                    }
                    final String model = modelOptional.get();
                    final ChatSession session = ChatSession.create(
                            new ChatSessionContext(model, tools)
                    );
                    executorService.submit(session);
                    final String _ = POISON_PILL.take();
                }
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            }
    }
}
