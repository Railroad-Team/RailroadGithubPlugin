package io.github.railroad.github.http;

import com.google.gson.JsonObject;
import io.github.railroad.core.gson.GsonLocator;
import lombok.NonNull;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class GithubRequests {
    private static final String CLIENT_ID = "Ov23liunwjVrHc9sKh3I";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static DeviceCodeResponse requestDeviceCode(String scope) {
        String form = "client_id=" + CLIENT_ID + "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://github.com/login/device/code"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            if (response.statusCode() == 200) {
                JsonObject json = GsonLocator.getInstance().fromJson(body, JsonObject.class);
                if (json == null)
                    throw new IllegalArgumentException("Invalid JSON response: " + body);

                return DeviceCodeResponse.fromJson(json);
            } else {
                throw new RuntimeException("Failed to request device code: " + body);
            }
        } catch (Exception exception) {
            throw new RuntimeException("Error while requesting device code", exception);
        }
    }

    public static AccessTokenResponse requestAccessToken(@NonNull String deviceCode) {
        String form = "grant_type=urn:ietf:params:oauth:grant-type:device_code" +
                "&client_id=" + CLIENT_ID +
                "&device_code=" + deviceCode;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://github.com/login/oauth/access_token"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject json = GsonLocator.getInstance().fromJson(response.body(), JsonObject.class);
            if (json == null)
                throw new IllegalArgumentException("Invalid JSON response: " + response.body());

            return AccessTokenResponse.fromJson(json);
        } catch (Exception exception) {
            throw new RuntimeException("Error while requesting access token", exception);
        }
    }

    public static UserResponse requestUser(@NonNull String accessToken) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/user"))
                .header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", "Bearer " + accessToken)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject json = GsonLocator.getInstance().fromJson(response.body(), JsonObject.class);
            if (json == null)
                throw new IllegalArgumentException("Invalid JSON response: " + response.body());

            return UserResponse.fromJson(json);
        } catch (Exception exception) {
            throw new RuntimeException("Error while requesting user information", exception);
        }
    }
}
