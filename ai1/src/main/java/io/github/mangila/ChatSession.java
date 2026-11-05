package io.github.mangila;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static io.github.mangila.Main.POISON_PILL;

class ChatSession implements Runnable {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final String model;
    private final String context;

    private ChatSession(String model, String context) {
        this.model = model;
        this.context = context;
    }

    static ChatSession create(String model, String topic) {
        return new ChatSession(model, topic);
    }

    @Override
    public void run() {
        final Semaphore backpressure = new Semaphore(1);
        final JsonArray history = new JsonArray();
        final AtomicLong tokenCounter = new AtomicLong(0);
        addToHistory("system", context, history);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                backpressure.acquire();
                final String prompt = IO.readln("%s > ".formatted(model));
                if ("exit".equalsIgnoreCase(prompt)) {
                    Thread.currentThread().interrupt();
                    POISON_PILL.offer("");
                    break;
                }
                addToHistory("user", prompt, history);
                final String payload = createChatPayload(history);
                final String response = OllamaIntegration.chat(payload)
                        .join()
                        .lines()
                        .map(ChatSession::parseJson)
                        .peek(jsonObject -> {
                            if (jsonObject.has("eval_count")) {
                                final JsonPrimitive evalCount = jsonObject.getAsJsonPrimitive("eval_count");
                                tokenCounter.addAndGet(evalCount.getAsLong());
                            }
                        })
                        .map(ChatSession::getContent)
                        .peek(line -> {
                            try {
                                TimeUnit.MILLISECONDS.sleep(100);
                            } catch (InterruptedException _) {
                                Thread.currentThread().interrupt();
                            }
                            IO.print(line);
                        })
                        .collect(Collectors.joining());
                addToHistory("assistant", response, history);
                IO.println();
                IO.println("-----");
                IO.println("Context: %s".formatted(context));
                IO.println("Token Count: %d".formatted(tokenCounter.get()));
                IO.println("-----");
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            } finally {
                backpressure.release();
            }
        }
    }

    private void addToHistory(String role, String content, JsonArray history) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("role", role);
        jsonObject.addProperty("content", content);
        history.add(jsonObject);
    }

    private String createChatPayload(JsonArray history) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("model", model);
        jsonObject.addProperty("stream", true);
        jsonObject.add("messages", history);
        return GSON.toJson(jsonObject);
    }

    private static String getContent(JsonObject response) {
        return response.getAsJsonObject("message")
                .getAsJsonPrimitive("content")
                .getAsString();
    }

    private static JsonObject parseJson(String body) {
        return GSON.fromJson(body, JsonObject.class);
    }

}
