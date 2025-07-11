package io.github.railroad.github;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.github.railroad.core.gson.GsonLocator;
import io.github.railroad.core.logger.LoggerServiceLocator;
import io.github.railroad.core.vcs.Repository;
import io.github.railroad.core.vcs.RepositoryTypes;
import io.github.railroad.core.vcs.connections.AbstractConnection;
import io.github.railroad.logger.Logger;
import javafx.scene.image.Image;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class GithubConnection extends AbstractConnection {
    private final GithubAccount account;

    public GithubConnection(GithubAccount profile) {
        this.account = profile;
    }

    private List<HttpResponse> readHTTP(String method, String postixUrl, String body) throws RuntimeException {
        return readHTTP(method, postixUrl, body, true);
    }

    private List<HttpResponse> readHTTP(String method, String postixUrl, String body, boolean enablePages) throws RuntimeException {
        if (account.getAccessToken().isEmpty())
            throw new RuntimeException("Missing Access Token");

        List<HttpResponse> result = new ArrayList<>();

        String requestUrl = "https://api.github.com/" + postixUrl;
        boolean finished = false;
        try {

            while (!finished) {
                URL url = new URI(requestUrl).toURL();
                var connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(method.toUpperCase());
                connection.setRequestProperty("Accept", "application/vnd.github+json");
                connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
                connection.setRequestProperty("Authorization", "Bearer " + account.getAccessToken());
                var in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String inputLine;
                var content = new StringBuilder();
                var statusCode = connection.getResponseCode();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                if (enablePages) {
                    if (connection.getHeaderField("link") != null) {
                        finished = true;
                        for (String element : connection.getHeaderField("link").split(",")) {
                            if (element.contains("rel=\"next\"")) {
                                requestUrl = element.substring(element.indexOf("<") + 1, element.indexOf(">"));
                                finished = false;
                            }
                        }
                    } else {
                        finished = true;
                    }
                } else {
                    finished = true;
                }

                in.close();
                result.add(new HttpResponse(content.toString(), statusCode));
            }

        } catch (IOException | URISyntaxException exception) {
            if (exception instanceof ProtocolException) {
                throw new RuntimeException("Protocol error: " + exception.getMessage(), exception);
            } else if (exception instanceof MalformedURLException) {
                throw new RuntimeException("URL is malformed: " + exception.getMessage(), exception);
            } else if (exception instanceof IOException) {
                throw new RuntimeException("I/O error: " + exception.getMessage(), exception);
            } else {
                throw new RuntimeException("URI syntax error: " + exception.getMessage(), exception);
            }
        }
        return result;
    }

    private List<Repository> getUserRepos() {
        List<Repository> repositoryList = new ArrayList<>();
        LoggerServiceLocator.getInstance().getLogger().debug("VCS - Github - Downloading repos");
        List<HttpResponse> output = readHTTP("GET", "user/repos?per_page=20", "");
        if (!output.isEmpty()) {
            for (HttpResponse response : output) {
                if (response.statusCode() == 200) {
                    if (!response.content().isBlank()) {
                        JsonArray repos = GsonLocator.getInstance().fromJson(response.content, JsonArray.class);
                        for (JsonElement element : repos) {
                            if (element.isJsonObject()) {
                                var repository = new Repository(RepositoryTypes.GIT);
                                repository.setRepositoryName(element.getAsJsonObject().get("name").getAsString());
                                repository.setRepositoryURL(element.getAsJsonObject().get("url").getAsString());
                                repository.setRepositoryCloneURL(element.getAsJsonObject().get("clone_url").getAsString());
                                repository.setIcon(Optional.of(new Image(element.getAsJsonObject().get("owner").getAsJsonObject().get("avatar_url").getAsString())));
                                repository.setConnection(this);
                                repositoryList.add(repository);
                            }
                        }
                    }
                }
            }
        }

        return repositoryList;
    }

    @Override
    public void downloadRepositories() {
        getRepositories().setAll(getUserRepos());
    }

    @Override
    public boolean updateRepo(Repository repo) {
        return false;
    }

    @Override
    public boolean cloneRepo(Repository repository, Path path) {
        Logger logger = LoggerServiceLocator.getInstance().getLogger();
        // check if the path is a directory and if its empty
        if (Files.exists(path) && !Files.isDirectory(path)) {
            logger.error("Path is not a directory at: {}", path);
            return false;
        }

        try {
            Files.createDirectories(path);
        } catch (IOException exception) {
            logger.error("Failed to create directory at: {}", path, exception);
            return false;
        }

        try(Stream<Path> files = Files.list(path)) {
            if (files.findAny().isPresent()) {
                logger.error("Path is not empty");
                return false;
            }
        } catch (IOException exception) {
            logger.error("Something went wrong while checking the path", exception);
            return false;
        }

        if (repository.getRepositoryType() == RepositoryTypes.GIT) {
            logger.info("Cloning Repo: {} to: {}", repository.getRepositoryCloneURL(), path.toAbsolutePath());
            var processBuilder = new ProcessBuilder();
            processBuilder.command("git", "clone", repository.getRepositoryCloneURL(), path.toAbsolutePath().resolve(repository.getRepositoryName()).toString());

            try {
                Process process = processBuilder.start();
                var thread = new Thread(() -> {
                    try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                         var errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            //updateOutput(line);
                        }
                        while ((line = errorReader.readLine()) != null) {
                            //updateOutput(line);
                        }
                    } catch (IOException exception) {
                        logger.error("Something went wrong trying to clone a github repo", exception);
                    }
                });

                thread.setDaemon(true);
                thread.start();

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    logger.info("Repository cloned successfully.");
                    return true;
                } else {
                    logger.error("Failed to clone the repository.");
                    return false;
                }
            } catch (IOException | InterruptedException exception) {
                logger.error("Something went wrong!", exception);
                return false;
            }
        }

        return false;
    }

    @Override
    public boolean validateProfile() {
        Logger logger = LoggerServiceLocator.getInstance().getLogger();
        logger.debug("VCS - Github - Validating profile");
        List<HttpResponse> output;
        try {
            output = readHTTP("GET", "user/repos?per_page=1", "", false);
        } catch (Exception exception) {
            logger.error("Github Validation", exception);
            return false;
        }

        if (output.isEmpty()) {
            return false;
        } else {
            return output.getFirst().statusCode() == 200;
        }
    }

    @Override
    public GithubAccount getProfile() {
        return this.account;
    }

    private record HttpResponse(String content, int statusCode) {
    }
}
