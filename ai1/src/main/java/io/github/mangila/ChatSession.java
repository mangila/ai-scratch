package io.github.mangila;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import static io.github.mangila.Main.POISON_PILL;

class ChatSession implements Runnable {

    private final ChatSessionContext sessionContext;

    private ChatSession(ChatSessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }

    static ChatSession create(ChatSessionContext chatSessionContext) {
        return new ChatSession(chatSessionContext);
    }

    @Override
    public void run() {
        final Semaphore backpressure = new Semaphore(1);
        final JsonArray history = new JsonArray();
        final AtomicLong tokenCounter = new AtomicLong(0);
        final String context = IOUtil.contextDecisionDialog(sessionContext.model());
        addToHistory("system", context, history);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                backpressure.acquire();
                final String prompt = IOUtil.input(sessionContext.model());
                if ("exit".equalsIgnoreCase(prompt)) {
                    Thread.currentThread().interrupt();
                    POISON_PILL.offer("");
                    break;
                }
                if ("info".equalsIgnoreCase(prompt)) {
                    final JsonObject payload = new JsonObject();
                    payload.addProperty("model", sessionContext.model());
                    final JsonObject info = OllamaIntegration.show(JsonUtil.toJson(payload))
                            .join();
                    final String infoPrettyString = JsonUtil.toJson(info);
                    IO.println(infoPrettyString);
                    continue;
                }
                addToHistory("user", prompt, history);
                final JsonObject payload = createChatPayload(history);
                final JsonObject response = OllamaIntegration.chat(payload.toString())
                        .join();
                IO.println(response);
                addToHistory("assistant", response.toString(), history);
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

    private JsonObject createChatPayload(JsonArray history) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("model", sessionContext.model());
        jsonObject.addProperty("stream", false);
        jsonObject.add("messages", history);
        jsonObject.add("tools", sessionContext.tools());
        return jsonObject;
    }

    private static String getContent(JsonObject response) {
        return response.getAsJsonObject("message")
                .getAsJsonPrimitive("content")
                .getAsString();
    }

}
