package io.github.mangila;

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
        final Optional<String> modelOptional = modelDecisionDialog(models);
        if (modelOptional.isEmpty()) {
            return;
        }
        final String model = modelOptional.get();
        final String topic = topicDecisionDialog(model);
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            final ChatSession session = ChatSession.create(model, topic);
            executorService.submit(session);
            final String _ = POISON_PILL.take();
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    private static String topicDecisionDialog(String model) {
        IO.println("Add System Context: (e.g., tech, history, code, fitness)");
        String topic = IO.readln("%s > ".formatted(model)).trim().toLowerCase();
        return switch (topic) {
            case "tech", "technology" -> "You are a helpful IT specialist. Explain concepts simply and accurately.";
            case "history"          -> "You are an expert historian. Provide rich historical context and factual timelines.";
            case "code", "coding"    -> "You are a senior software engineer. Provide idiomatic code snippets and minimal prose.";
            case "fitness", "health" -> "You are a certified fitness coach. Offer practical, actionable health advice.";
            default                  -> "You are a helpful, direct, and concise AI assistant.";
        };
    }

    private static Optional<String> modelDecisionDialog(List<String> models) {
        final StringBuilder menuBuilder = new StringBuilder();
        for (int i = 1; i <= models.size(); i++) {
            final String model = models.get(i - 1);
            menuBuilder.append(i)
                    .append('.')
                    .append(model)
                    .append(System.lineSeparator());
        }
        IO.println("""
                ========================================
                       OLLAMA JAVA CHAT CLIENT
                ========================================
                %s
                Type 'exit' to quit
                ----------------------------------------
                """.formatted(menuBuilder));
        IO.println("Select model: 1 - %d".formatted(models.size()));
        String decision = IO.readln("> ");
        if (decision.equalsIgnoreCase("exit")) {
            return Optional.empty();
        }
        return parseModel(decision, models);
    }

    private static Optional<String> parseModel(String decision, List<String> models) {
        try {
            final int index = Integer.parseInt(decision);
            final String model = models.get(index - 1);
            return Optional.of(model);
        } catch (NumberFormatException _) {
            IO.println("Please enter a valid number.");
            return Optional.empty();
        } catch (IndexOutOfBoundsException _) {
            IO.println("Please enter a number between 1 and " + models.size());
            return Optional.empty();
        }
    }
}
