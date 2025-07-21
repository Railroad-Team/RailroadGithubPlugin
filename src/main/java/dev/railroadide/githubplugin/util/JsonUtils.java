package dev.railroadide.githubplugin.util;

import com.google.gson.JsonObject;

public class JsonUtils {
    public static String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    public static int getAsInt(JsonObject json, String key) {
        return getAsIntOrDefault(json, key, 0);
    }

    public static int getAsIntOrDefault(JsonObject json, String key, int defaultValue) {
        Integer value = getIntegerOrNull(json, key);
        return value != null ? value : defaultValue;
    }

    public static Integer getIntegerOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : null;
    }

    public static boolean getAsBoolean(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() && json.get(key).getAsBoolean();
    }

    public static boolean getAsBooleanOrDefault(JsonObject json, String key, boolean defaultValue) {
        Boolean value = getBooleanOrNull(json, key);
        return value != null ? value : defaultValue;
    }

    public static Boolean getBooleanOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsBoolean() : null;
    }
}
