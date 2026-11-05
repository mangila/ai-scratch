package io.github.mangila;

import com.google.gson.JsonArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

final class Tools {

    private Tools() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    static JsonArray loadTools() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (final InputStream inputStream = classLoader.getResourceAsStream("tools.json")) {
            if (inputStream == null) {
                throw new IllegalArgumentException("tools.json not found in classpath");
            }
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return JsonUtil.fromJson(reader, JsonArray.class);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load tools.json", e);
        }
    }

    static String getCurrentWeather() {
        return "Sunny";
    }

}
