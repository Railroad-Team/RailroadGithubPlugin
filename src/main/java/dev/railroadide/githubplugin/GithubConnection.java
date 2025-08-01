package dev.railroadide.githubplugin;

import dev.railroadide.core.vcs.Repository;
import dev.railroadide.core.vcs.connections.AbstractConnection;
import dev.railroadide.githubplugin.data.GithubAccount;
import dev.railroadide.githubplugin.data.GithubRepository;
import dev.railroadide.githubplugin.http.GithubRequests;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.NullProgressMonitor;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

public class GithubConnection extends AbstractConnection {
    private final GithubAccount account;

    public GithubConnection(GithubAccount profile) {
        this.account = profile;
    }

    @Override
    public void fetchRepositories() {
        getRepositories().clear();

        var subscriber = new Flow.Subscriber<List<GithubRepository>>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(List<GithubRepository> page) {
                getRepositories().addAll(page);
                subscription.request(1);
            }

            @Override
            public void onError(Throwable error) {
                GithubPlugin.getLogger().error("Failed to fetch repositories for account: " + account.getAlias(), error);
                if (subscription != null) {
                    subscription.cancel();
                }
            }

            @Override
            public void onComplete() {
                GithubPlugin.getLogger().info("Fetched " + getRepositories().size() + " repositories for account: " + account.getAlias());
            }
        };

        String token = GithubPlugin.TOKEN_STORAGE.getToken("railroad_github_access_token_" + account.getUserId());
        GithubRequests.requestUserRepositoriesPublisher(token, true, 30).subscribe(subscriber);
    }

    @Override
    public CompletableFuture<Boolean> cloneRepo(Repository repository, Path path) {
        return CompletableFuture.supplyAsync(() -> {
            try (Git ignored = Git.cloneRepository()
                    .setURI(repository.getRepositoryCloneURL())
                    .setDirectory(path.toFile())
                    .setProgressMonitor(NullProgressMonitor.INSTANCE)
                    .call()) {
                return true;
            } catch (GitAPIException exception) {
                GithubPlugin.getLogger().error("Failed to clone repository: " + repository.getRepositoryName(), exception);
                return false;
            }
        });
    }
}