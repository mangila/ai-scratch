package io.github.mangila;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.Reader;

final class JsonUtil {

    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static <T> T fromJson(String body, Class<T> clazz) {
        return GSON.fromJson(body, clazz);
    }

    static <T> T fromJson(Reader reader, Class<T> clazz) {
        return GSON.fromJson(reader, clazz);
    }

    static JsonObject fromJson(String body) {
        return fromJson(body, JsonObject.class);
    }

    static String toJson(Object object) {
        return GSON.toJson(object);
    }

}
