package io.github.mangila;

import com.google.gson.JsonArray;

public record ChatSessionContext(String model, JsonArray tools) {
}
