package io.github.railroad.github;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.github.railroad.core.gson.GsonLocator;
import io.github.railroad.core.vcs.connections.VCSProfile;

import java.util.List;

public class GithubAccount extends VCSProfile {
    public static List<GithubAccount> listFromJson(JsonElement json) {
        Gson gson = GsonLocator.getInstance();
        if (json.isJsonArray()) {
            return gson.fromJson(json, new TypeToken<List<GithubAccount>>() {}.getType());
        } else if (json.isJsonObject()) {
            return List.of(gson.fromJson(json, GithubAccount.class));
        } else {
            throw new IllegalArgumentException("Invalid JSON format for GithubAccount list: " + json);
        }
    }

    public static JsonElement listToJson(List<GithubAccount> accounts) {
        Gson gson = GsonLocator.getInstance();
        return gson.toJsonTree(accounts, new TypeToken<List<GithubAccount>>() {}.getType());
    }
}
