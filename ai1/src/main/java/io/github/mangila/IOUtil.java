package io.github.mangila;

import java.util.List;
import java.util.Optional;

final class IOUtil {

    private IOUtil() {
        throw new IllegalStateException("Utility class");
    }

    static String input(String model) {
        return IO.readln("%s > ".formatted(model));
    }

    static String contextDecisionDialog(String model) {
        IO.println("Add System Context: (e.g., tech, history, code, fitness)");
        final String context = input(model);
        return switch (context) {
            case "tech", "technology" -> "You are a helpful IT specialist. Explain concepts simply and accurately.";
            case "history" -> "You are an expert historian. Provide rich historical context and factual timelines.";
            case "code", "coding" ->
                    "You are a senior software engineer. Provide idiomatic code snippets and minimal prose.";
            case "fitness", "health" -> "You are a certified fitness coach. Offer practical, actionable health advice.";
            case "weather" -> "You are a meteorologist. Provide accurate and up-to-date weather information.";
            default -> "You are a helpful, direct, and concise AI assistant.";
        };
    }

    static Optional<String> modelDecisionDialog(List<String> models) {
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
                Type 'info' for model information
                Type 'exit' to quit
                ----------------------------------------
                """.formatted(menuBuilder));
        IO.println("Select model: 1 - %d".formatted(models.size()));
        final String decision = input("");
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
