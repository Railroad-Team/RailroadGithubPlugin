package dev.railroadide.githubplugin.http;

import com.google.gson.JsonObject;
import dev.railroadide.core.gson.GsonLocator;
import dev.railroadide.githubplugin.GithubPlugin;
import dev.railroadide.githubplugin.data.GithubRepository;
import dev.railroadide.githubplugin.data.GithubUser;
import lombok.NonNull;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GithubRequests {
    private static final String CLIENT_ID = "Ov23liunwjVrHc9sKh3I";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final Pattern LINK_PATTERN =
            Pattern.compile("<([^>]*)>\\s*;\\s*rel=\"(\\w+)\"");

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

    public static GithubUser requestUser(@NonNull String accessToken) {
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

            return GithubUser.fromJson(json);
        } catch (Exception exception) {
            GithubPlugin.LOGGER.error("Error while requesting user information", exception);
            return null;
        }
    }

    public static GithubUser requestUser(int userId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/user/" + userId))
                .header("Accept", "application/vnd.github.v3+json")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject json = GsonLocator.getInstance().fromJson(response.body(), JsonObject.class);
            if (json == null)
                throw new IllegalArgumentException("Invalid JSON response: " + response.body());

            return GithubUser.fromJson(json);
        } catch (Exception exception) {
            throw new RuntimeException("Error while requesting user information by ID", exception);
        }
    }

    /**
     * Extracts the URL for rel="next" from the GitHub Link header.
     *
     * @param linkHeader the full value of the "Link" response header
     * @return the URL for the next page, or null if none present
     */
    private static String extractNextPageUrl(String linkHeader) {
        if (linkHeader == null || linkHeader.isEmpty()) {
            return null;
        }

        // Split on commas *outside* of the angle brackets
        String[] parts = linkHeader.split(",\\s*");
        for (String part : parts) {
            Matcher m = LINK_PATTERN.matcher(part);
            if (m.matches()) {
                String url = m.group(1);
                String rel = m.group(2);
                if ("next".equals(rel)) {
                    return url;
                }
            }
        }

        return null;
    }

    public static Flow.Publisher<List<GithubRepository>> requestUserRepositoriesPublisher(String token,
                                                                                          boolean usePages, int perPage) {
        SubmissionPublisher<List<GithubRepository>> pub = new SubmissionPublisher<>();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        fetchPagePublisher(token, usePages,
                "https://api.github.com/user/repos?per_page=" + perPage,
                pub, scheduler);
        pub.getSubscribers().forEach(Flow.Subscriber::onComplete);
        return pub;
    }

    private static void fetchPagePublisher(
            String token, boolean usePages, String url,
            SubmissionPublisher<List<GithubRepository>> pub, ScheduledExecutorService scheduler) {

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HTTP_CLIENT.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() != 200) {
                        pub.closeExceptionally(
                                new RuntimeException("Failed: " + response.body()));
                        scheduler.shutdown();
                        return;
                    }

                    JsonObject[] arr = GsonLocator.getInstance()
                            .fromJson(response.body(), JsonObject[].class);
                    List<GithubRepository> page = Arrays.stream(arr)
                            .map(GithubRepository::fromJson)
                            .collect(Collectors.toList());
                    pub.submit(page);

                    if (usePages) {
                        String nextUrl = extractNextPageUrl(
                                response.headers().firstValue("link").orElse(""));
                        if (nextUrl != null) {
                            // chain next page after delay
                            scheduler.schedule(() ->
                                            fetchPagePublisher(token, true, nextUrl, pub, scheduler),
                                    500, TimeUnit.MILLISECONDS);
                            return;
                        }
                    }

                    // no more pages -> close
                    pub.close();
                    scheduler.shutdown();
                })
                .exceptionally(ex -> {
                    pub.closeExceptionally(ex);
                    scheduler.shutdown();
                    return null;
                });
    }
}
